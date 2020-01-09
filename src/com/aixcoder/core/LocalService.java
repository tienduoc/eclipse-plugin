package com.aixcoder.core;

import static com.aixcoder.i18n.Localization.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.progress.UIJob;

import com.aixcoder.i18n.Localization;
import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.AiXUpdater;
import com.aixcoder.utils.HttpHelper;
import com.aixcoder.utils.HttpHelper.HTTPMethod;
import com.aixcoder.utils.shims.Consumer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * types of Operating Systems
 */
enum OSType {
	Windows, MacOS, Linux, Other
};

/**
 * helper class to check the operating system this Java VM runs in
 *
 * please keep the notes below as a pseudo-license
 *
 * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
 * compare to
 * http://svn.terracotta.org/svn/tc/dso/tags/2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
 * http://www.docjar.com/html/api/org/apache/commons/lang/SystemUtils.java.html
 */
final class OsCheck {
	// cached result of OS detection
	protected static OSType detectedOS;

	/**
	 * detect the operating system from the os.name System property and cache the
	 * result
	 * 
	 * @returns - the operating system detected
	 */
	public static OSType getOperatingSystemType() {
		if (detectedOS == null) {
			String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
				detectedOS = OSType.MacOS;
			} else if (OS.contains("win")) {
				detectedOS = OSType.Windows;
			} else if (OS.contains("nux")) {
				detectedOS = OSType.Linux;
			} else {
				detectedOS = OSType.Other;
			}
		}
		return detectedOS;
	}
}

interface ProgressListener {
	void update(long total, int done);
}

interface ErrorListener {
	void onError(Exception e);
}

public class LocalService {
	static String homedir;
	volatile private static Integer lastOpenFailed = 0;
	private static boolean serverStarting;

	public static boolean isServerStarting() {
		return serverStarting;
	}

	static {
		homedir = System.getProperty("user.home");
		if (OsCheck.getOperatingSystemType() == OSType.MacOS) {
			homedir = FilenameUtils.concat(FilenameUtils.concat(homedir, "Library"), "Application Support");
		}
	}

	public static String getAixcoderInstallUserPath() {
		return FilenameUtils.concat(FilenameUtils.concat(homedir, "aiXcoder"), "installer");
	}

	public static String getActivePid(String pid) {
		if (pid == null) {
			return null;
		}
		String result = null;
		try {
			switch (OsCheck.getOperatingSystemType()) {
			case Windows:
				Process p = Runtime.getRuntime().exec(String.format(
						"wmic process where processid=%s get executablepath, name, processid | findstr %s", pid, pid));
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String tmpLine;
				while ((tmpLine = input.readLine()) != null) {
					if (tmpLine.contains(pid)) {
						String[] resultSplits = tmpLine.trim().split("\\s+");
						if (resultSplits.length >= 2) {
							result = resultSplits[resultSplits.length - 1].trim();
							break;
						}
					}
				}
				p.waitFor();
				break;
			case MacOS:
				p = Runtime.getRuntime().exec(String.format("ps -ax | awk '$1 == %s", pid));
				input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String output = "";
				while ((tmpLine = input.readLine()) != null) {
					output += tmpLine + "\n";
				}

				String[] resultSplits = output.trim().split("\\s+", 2);
				if (resultSplits.length > 0) {
					result = resultSplits[0].trim();
				}
				p.waitFor();
				break;
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getLocalServerPid() {
		String lockfile = FilenameUtils.concat(FilenameUtils.concat(homedir, "aiXcoder"), ".router.lock");
		try {
			String startPid = FileUtils.readFileToString(new File(lockfile), "utf-8").trim();
			return getActivePid(startPid);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getExePath() {
		String localserver = FilenameUtils
				.concat(FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current").toString();
		if (OsCheck.getOperatingSystemType() == OSType.Windows) {
			return FilenameUtils.concat(FilenameUtils.concat(localserver, "server"), "aixcoder.bat").toString();
		} else {
			return FilenameUtils.concat(FilenameUtils.concat(localserver, "server"), "aixcoder.sh").toString();
		}
	}

	public static void launchLocalServer() {
		String exePath = getExePath();
		try {
			String javaHome = System.getProperty("java.home");
			String[] commands;
			if (Preference.getParams().contains("localconsole=1")) {
				if (OsCheck.getOperatingSystemType() == OSType.Windows) {
					commands = new String[] { "cmd", "/C", "start", exePath };
				} else if (OsCheck.getOperatingSystemType() == OSType.MacOS) {
					commands = new String[] { "open", "-a", "Terminal", exePath };
				} else {
					commands = new String[] { "gnome-terminal", "--", exePath };
				}
			} else {
				commands = new String[] { exePath };
			}
			ProcessBuilder pb = new ProcessBuilder(commands);
			Map<String, String> env = pb.environment();
			env.put("Path", env.get("Path") + ";" + FilenameUtils.concat(javaHome, "bin").toString());
			pb.start();
		} catch (IOException e) {
			lastOpenFailed = 1;
			e.printStackTrace();
		}
	}

	public static void authorize() {
		String osName = System.getProperty("os.name");
		if (!osName.toLowerCase().contains("win")) {
			ProcessBuilder processBuilder = new ProcessBuilder(
					new String[] { "chmod", "-R", "777", getAixcoderInstallUserPath() });
			try {
				processBuilder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static long lastStartTime = 0;

	public static Job startLocalService(boolean soft) {
		if (lastOpenFailed != 0 || !Preference.isActive()) {
			return null;
		}
		String aixcoderPath = FilenameUtils.concat(
				FilenameUtils.concat(FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current"),
				"server");
		new File(aixcoderPath).mkdirs();
		if (soft) {
			try {
				String r = HttpHelper.request(HTTPMethod.GET, Preference.getDefaultLocalEndpoint(), null,
						new Consumer<HttpRequest>() {

							@Override
							public void apply(HttpRequest t) {
								t.connectTimeout(1000).readTimeout(1000);
							}
						});
				if (r != null) {
					lastOpenFailed = 0;
					serverStarting = false;
					return null;
				}
				// server not running
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		if (System.currentTimeMillis() - lastStartTime < 1000 * 30) {
			return null;
		}
		lastStartTime = System.currentTimeMillis();
		serverStarting = true;
		authorize();
		launchLocalServer();
		Job j = new Job("Launching aiXcoder service") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				long startTime = System.currentTimeMillis();
				boolean succeed = false;
				while (System.currentTimeMillis() - startTime < 60 * 1000) {
					try {
						Thread.sleep(3000);
						HttpHelper.request(HTTPMethod.GET, Preference.getDefaultLocalEndpoint(), null,
								new Consumer<HttpRequest>() {

									@Override
									public void apply(HttpRequest t) {
										t.connectTimeout(1000).readTimeout(1000);
									}
								});
						succeed = true;
						break;
					} catch (Exception e) {
						continue;
					}
				}
				if (succeed) {
					monitor.done();
					lastOpenFailed = 0;
					return Status.OK_STATUS;
				} else {
					monitor.setCanceled(true);
					new UIJob("aiXcoder service failed to start") {

						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							MessageDialog dialog = new MessageDialog(null, R(Localization.localServerAutoStartTitle),
									null, R(Localization.localServerAutoStartQuestion), MessageDialog.ERROR,
									new String[] {}, 0);
							dialog.open();
							return Status.OK_STATUS;
						}
					}.schedule();
					return Status.CANCEL_STATUS;
				}
			}

		};
		j.schedule();
		return j;
	}

	public static String getVersion() {
		String aixcoderPath = FilenameUtils.concat(
				FilenameUtils.concat(FilenameUtils.concat(
						FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current"), "server"),
				"version");
		String version;
		try {
			version = FileUtils.readFileToString(new File(aixcoderPath), "utf-8").trim();
		} catch (IOException e) {
			aixcoderPath = FilenameUtils.concat(
					FilenameUtils.concat(FilenameUtils.concat(
							FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current"), "server"),
					".version");
			try {
				version = FileUtils.readFileToString(new File(aixcoderPath), "utf-8").trim();
			} catch (IOException ex) {
				version = "0.0.0";
			}
		}
		return version;
	}

	private static void kill() throws IOException, InterruptedException {
		String lockfile = FilenameUtils.concat(FilenameUtils.concat(homedir, "aiXcoder"), ".router.lock");
		String prevPid = FileUtils.readFileToString(new File(lockfile), "utf-8").trim();
		if (OsCheck.getOperatingSystemType() == OSType.Windows) {
			Runtime.getRuntime().exec(String.format("taskkill /F /PID %s", prevPid));
		} else {
			Runtime.getRuntime().exec(String.format("kill %s", prevPid));
		}

		int tries = 10;
		while (getActivePid(prevPid) != null) {
			if (tries == 0) {
				throw new InterruptedException("failed to kill process");
			}
			tries--;
			Thread.sleep(1000);
		}
	}

	public static String humanReadableByteCountBin(long bytes) {
		long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		return b < 1024L ? bytes + " B"
				: b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
						: b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
								: b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
										: b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
												: b <= 0xfffccccccccccccL
														? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
														: String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
	}

	public static void forceUpdate(final String localVersion, final String remoteVersion) {
		new Job("Downloading aiXcoder local service") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final String aixcoderPath = FilenameUtils.concat(FilenameUtils.concat(
						FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current"), "server");
				final String releasePath = "https://github.com/aixcoder-plugin/localservice/releases";
				new File(aixcoderPath).mkdirs();

				String ball;
				ErrorListener onErr = new ErrorListener() {
					@Override
					public void onError(final Exception e) {
						e.printStackTrace();
						monitor.setCanceled(true);

						new UIJob("Prompt aiXcoder update") {

							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								MessageDialog dialog = new MessageDialog(null, R(Localization.localDownloadTitle), null,
										String.format(R(Localization.localDownloadQuestion), releasePath, aixcoderPath)
												+ "\nCause:\n" + e.getMessage(),
										MessageDialog.QUESTION,
										new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
								int choice = dialog.open();
								if (choice == 0) {
									try {
										java.awt.Desktop.getDesktop().browse(java.net.URI.create(releasePath));
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
								return Status.OK_STATUS;
							}

						}.schedule();
					}
				};

				String patchball;
				String rawRemoteVersion = remoteVersion.startsWith("v") ? remoteVersion.substring(1) : remoteVersion;
				if (OsCheck.getOperatingSystemType() == OSType.Windows) {
					ball = "server-win.zip";
					patchball = String.format("win-patch_%s_%s_full.zip", localVersion, rawRemoteVersion);
				} else if (OsCheck.getOperatingSystemType() == OSType.MacOS) {
					ball = "server-osx.zip";
					patchball = String.format("osx-patch_%s_%s_full.zip", localVersion, rawRemoteVersion);
				} else {
					ball = "server-linux.tar.gz";
					patchball = String.format("linux-patch_%s_%s_full.tar.gz", localVersion, rawRemoteVersion);
				}
				String updatedVersion;
				try {
					final AiXUpdater.CancellationToken token = new AiXUpdater.CancellationToken();
					updatedVersion = AiXUpdater.simplePatch(aixcoderPath, new String[] {
							String.format("https://github.com/aixcoder-plugin/localservice/releases/latest/download/%s",
									patchball),
							String.format("http://image.aixcoder.com/localservice/releases/download/%s/%s",
									remoteVersion, patchball) },
							new String[] { String.format(
									"https://github.com/aixcoder-plugin/localservice/releases/latest/download/%s",
									ball),
									String.format("http://image.aixcoder.com/localservice/releases/download/%s/%s",
											remoteVersion, ball), },
							new AiXUpdater.ProgressListener() {
								boolean started = false;
								int totalDone = 0;
								long lastUpdated = 0;

								@Override
								public void update(long total, int done, long speed) {
									if (monitor.isCanceled()) {
										token.cancel("user cancel");
										return;
									}
									if (!started) {
										started = true;
									}
									totalDone += done;
									monitor.worked(done);
									if (System.currentTimeMillis() - lastUpdated > 100) {
										lastUpdated = System.currentTimeMillis();
										final int currentProgress = (int) ((((double) totalDone) / ((double) total))
												* 100000d);
										System.out.println(String.format("progress: %d/%d +%d %.2f%%", totalDone, total,
												done, currentProgress / 1000.0));
										monitor.setTaskName("Downloading AiXcoder Local Service "
												+ humanReadableByteCountBin(speed) + "/s");
									}
								}
							}, new Runnable() {

								@Override
								public void run() {
									synchronized (lastOpenFailed) {
										lastOpenFailed = 1;
									}
									try {
										kill();
									} catch (IOException e) {
										e.printStackTrace();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									monitor.setTaskName("Unzipping aiXcoder...");
								}
							}, token);
					synchronized (lastOpenFailed) {
						lastOpenFailed = 0;
					}
					System.out.println("aiXcoder is updated to " + updatedVersion);
				} catch (Exception e1) {
					onErr.onError(e1);
                    updatedVersion = null;
				}
				monitor.done();
				lastOpenFailed = 0;
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	public static int getServiceStatus(String ext) {
		try {
			String resp = HttpHelper.request(HTTPMethod.GET,
					Preference.getDefaultLocalEndpoint() + "/getSaStatus?ext=" + ext, null, new Consumer<HttpRequest>() {

						@Override
						public void apply(HttpRequest t) {
							t.connectTimeout(1000).readTimeout(1000);
						}
					});
			JsonObject o = new Gson().fromJson(resp, JsonObject.class);
			return o.get("status").getAsInt();
		} catch (URISyntaxException e) {
			return 0;
		}
	}

	public static void switchToLocal(boolean local) {
		String s = local ? "true" : "false";
		// @formatter:off
		try {
			FileUtils.writeStringToFile(new File(Preference.localserver), "{\n" + 
					"  \"models\": [\n" + 
					"    {\n" + 
					"      \"name\": \"java(Java)\",\n" + 
					"      \"active\": "+ s +"\n" + 
					"    },\n" + 
					"    {\n" + 
					"      \"name\": \"python(Python)\",\n" + 
					"      \"active\": "+ s +"\n" + 
					"    },\n" + 
					"    {\n" + 
					"      \"name\": \"typescript(Typescript)\",\n" + 
					"      \"active\": "+ s +"\n" + 
					"    },\n" + 
					"    {\n" + 
					"      \"name\": \"javascript(Javascript)\",\n" + 
					"      \"active\": "+ s +"\n" + 
					"    },\n" + 
					"    {\n" + 
					"      \"name\": \"cpp(Cpp)\",\n" + 
					"      \"active\": "+ s +"\n" + 
					"    },\n" + 
					"    {\n" + 
					"      \"name\": \"go(Go)\",\n" + 
					"      \"active\": "+ s +"\n" + 
					"    },\n" + 
					"    {\n" + 
					"      \"name\": \"php(Php)\",\n" + 
					"      \"active\": "+ s +"\n" + 
					"    }\n" + 
					"  ]\n" + 
					"}", "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// @formatter:on
	}
}
