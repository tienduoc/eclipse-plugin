package com.aixcoder.core;

import static com.aixcoder.i18n.Localization.R;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import com.aixcoder.utils.HttpHelper;
import com.aixcoder.utils.HttpHelper.HTTPMethod;
import com.aixcoder.utils.shims.Consumer;

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

class UnzipFiles {
	public static void unzip(String zipFilePath, String destDir) {
		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if (!dir.exists())
			dir.mkdirs();
		FileInputStream fis;
		// buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFilePath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(destDir + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				if (ze.isDirectory()) {
					newFile.mkdirs();
				} else {
					System.out.println("Unzipping to " + newFile.getAbsolutePath());
					// create directories for sub directories in zip
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
				// close this ZipEntry
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			// close last ZipEntry
			zis.closeEntry();
			zis.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
	private static boolean lastOpenFailed;
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
		String localserver = FilenameUtils.concat(FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current").toString();
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
			if (Preference.getParams().contains("localconsole")) {
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
			new Job("Launching aiXcoder service") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					long startTime = System.currentTimeMillis();
					boolean succeed = false;
					while (System.currentTimeMillis() - startTime < 60 * 1000) {
						try {
							Thread.sleep(3000);
							HttpHelper.request(HTTPMethod.GET, Preference.getEndpoint(), null,
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
						lastOpenFailed = false;
						return Status.OK_STATUS;
					} else {
						monitor.setCanceled(true);
						new UIJob("aiXcoder service failed to start") {

							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								MessageDialog dialog = new MessageDialog(null,
										R(Localization.localServerAutoStartTitle), null,
										R(Localization.localServerAutoStartQuestion), MessageDialog.ERROR, new String[] {}, 0);
								dialog.open();
								return Status.OK_STATUS;
							}
						}.schedule();
						return Status.CANCEL_STATUS;
					}
				}

			}.schedule();
		} catch (IOException e) {
			lastOpenFailed = true;
			e.printStackTrace();
		}
	}

	public static void openurl(String url) {
		if (lastOpenFailed) {
			return;
		}
		if (!url.equals("aixcoder://localserver")) {
			return;
		}
		String aixcoderPath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current"), "server");
		new File(aixcoderPath).mkdirs();
		lastOpenFailed = true;
		launchLocalServer();
	}

	public static String getVersion() {
		String aixcoderPath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current"), "server"), ".version");
		String version;
		try {
			version = FileUtils.readFileToString(new File(aixcoderPath), "utf-8").trim();
		} catch (IOException e) {
			version = "0.0.0";
		}
		return version;
	}

	private static void download(String urlString, String target, ProgressListener listener, ErrorListener onErr) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
			long completeFileSize = httpConnection.getContentLength();

			java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
			if (new File(target).isDirectory()) {
				target += File.separator + urlString.substring(urlString.lastIndexOf("/") + 1);
			}
			java.io.FileOutputStream fos = new java.io.FileOutputStream(target);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
			byte[] data = new byte[4096];
//			long downloadedFileSize = 0;
			int x = 0;
			while ((x = in.read(data, 0, 4096)) >= 0) {
//				downloadedFileSize += x;

				// calculate progress
//				final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize))
//						* 100000d);
				listener.update(completeFileSize, x);

				bout.write(data, 0, x);
			}
			bout.close();
			in.close();
		} catch (Exception e) {
			onErr.onError(e);
		}
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

	public static void forceUpdate() {
		new Job("Downloading aiXcoder local service") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final String aixcoderPath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(getAixcoderInstallUserPath(), "localserver"), "current"), "server")
						.toString();
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
										String.format(R(Localization.localDownloadQuestion), releasePath, aixcoderPath) + "\nCause:\n" + e.getMessage(),
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

				ProgressListener onProgress = new ProgressListener() {
					boolean started = false;
					int totalDone = 0;

					@Override
					public void update(long total, int done) {
						if (!started) {
							monitor.beginTask("download from https://github.com/aixcoder-plugin/localservice/releases",
									(int) total);
							started = true;
						}
						totalDone += done;
						monitor.worked(done);
						final int currentProgress = (int) ((((double) totalDone) / ((double) total)) * 100000d);
						System.out.println(String.format("progress: %d/%d +%d %.2f%%", totalDone, total, done,
								currentProgress / 1000.0));
					}
				};
				if (OsCheck.getOperatingSystemType() == OSType.Windows) {
					ball = "server-win.zip";
					download(releasePath + "/latest/download/" + ball, aixcoderPath, onProgress, onErr);
				} else if (OsCheck.getOperatingSystemType() == OSType.MacOS) {
					ball = "server-osx.zip";
					download(releasePath + "/latest/download/" + ball, aixcoderPath, onProgress, onErr);
				} else {
					ball = "server-linux.tar.gz";
					download(releasePath + "/latest/download/" + ball, aixcoderPath, onProgress, onErr);
				}
				try {
					kill();
					if (ball.endsWith(".tar.gz")) {
						Process p = Runtime.getRuntime().exec(String.format("tar zxf \"%s\" -C \"%s\"",
								FilenameUtils.concat(aixcoderPath, ball), aixcoderPath));
						p.waitFor();
					} else if (ball.endsWith(".zip")) {
						UnzipFiles.unzip(FilenameUtils.concat(aixcoderPath, ball), aixcoderPath);
					}
				} catch (Exception e1) {
					onErr.onError(e1);
				}
				monitor.done();
				lastOpenFailed = false;
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
