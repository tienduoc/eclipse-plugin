package com.aixcoder.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

interface ICancelHandler {
	void onCancelled(String reason);
}

abstract class SpeedListener {
	double testSpeed = 0;

	abstract void update(long elapsed, long transferred, long speed);
}

interface ErrorListener {
	void onError(Exception e);
}

interface ConnectionFactory {
	HttpURLConnection getConnection(String urlString) throws IOException;
}

class DefaultConnectionFactory implements ConnectionFactory {

	@Override
	public HttpURLConnection getConnection(String urlString) throws IOException {
		URL url = new URL(urlString);
		return (HttpURLConnection) (url.openConnection());
	}

}

public class AiXUpdater {

	public static class CancelledException extends Exception {
		public CancelledException() {
			super("cancelled");
		}
	}

	public interface ProgressListener {
		void update(long total, int done, long speed);
	}

	public static class CancellationToken {
		boolean cancelled;
		String reason;
		List<ICancelHandler> listeners;

		public CancellationToken() {
			this.listeners = new ArrayList<ICancelHandler>();
			this.cancelled = false;
		}

		public void cancel(String reason) {
			this.reason = reason;
			this.cancelled = true;
			for (ICancelHandler element : this.listeners) {
				element.onCancelled(reason);
			}
		}

		public void onCancellationRequested(ICancelHandler handler) {
			this.listeners.add(handler);
		}

		public void removeHandler(ICancelHandler handler) {
			this.listeners.remove(handler);
		}
	}

	static int nSimpleStatus = 3;
	static int nStatus = 6;

	static ExecutorService pool = Executors.newFixedThreadPool(10);
	static Random random = new Random();

	private static void download(String urlString, String target, ProgressListener listener, SpeedListener onSpeed,
			DefaultConnectionFactory connFactory, CancellationToken token) throws IOException {
		long speedTestStart = 0;
		HttpURLConnection httpConnection = connFactory.getConnection(urlString);
		long completeFileSize = httpConnection.getContentLength();

		java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
		if (new File(target).isDirectory()) {
			target += File.separator + urlString.substring(urlString.lastIndexOf("/") + 1);
		}
		java.io.FileOutputStream fos = new java.io.FileOutputStream(target);
		java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
		byte[] data = new byte[1024];
		long downloadedFileSize = 0;
		int x = 0;
		speedTestStart = System.currentTimeMillis();
		while ((x = in.read(data, 0, data.length)) >= 0) {
			if (token.cancelled) {
				break;
			}
			downloadedFileSize += x;
			long elapsed = Math.max(System.currentTimeMillis() - speedTestStart, 1);
			if (listener != null) {
				listener.update(completeFileSize, x, downloadedFileSize * 1000 / elapsed);
			}
			if (onSpeed != null) {
				onSpeed.update(elapsed, downloadedFileSize, downloadedFileSize * 1000 / elapsed);
			}
			bout.write(data, 0, x);
		}
		bout.close();
		in.close();
	}

	public static double getDownloadSpeed(String url, DefaultConnectionFactory connFactory,
			final CancellationToken cancellationToken) {
		String tmpPath = "speedtest." + random.nextInt() + ".tmp";
		String tempFolder = FilenameUtils.concat(FileUtils.getTempDirectory().getAbsolutePath(), tmpPath);
		final CancellationToken childToken = new CancellationToken();
		ICancelHandler onCancelled = new ICancelHandler() {

			@Override
			public void onCancelled(String reason) {
				childToken.cancel(reason);
			}
		};
		cancellationToken.onCancellationRequested(onCancelled);
		SpeedListener onSpeed = new SpeedListener() {
			@Override
			public void update(long elapsed, long transferred, long speed) {
				testSpeed = speed;
				if (elapsed > 3000 || transferred > 100 * 1024) {
					childToken.cancel("speedLow");
				}
			}
		};
		try {
			download(url, tempFolder, null, onSpeed, connFactory, childToken);
			cancellationToken.removeHandler(onCancelled);
			return onSpeed.testSpeed;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				FileUtils.forceDelete(new File(tempFolder));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String selectBestMirror(List<String> urlList, final CancellationToken token) {
		return selectBestMirror(urlList, new DefaultConnectionFactory(), token);
	}

	public static String selectBestMirror(List<String> urlList, final DefaultConnectionFactory connFactory,
			final CancellationToken token) {
		if (urlList.size() == 1) {
			return urlList.get(0);
		}
		ArrayList<Callable<Double>> tasks = new ArrayList<Callable<Double>>();
		for (final String url : urlList) {
			tasks.add(new Callable<Double>() {
				
				@Override
				public Double call() throws Exception {
					return getDownloadSpeed(url, connFactory, token);
				}
			});
		}
		try {
			List<Future<Double>> futures = pool.invokeAll(tasks);
			int bestI = 0;
			for (int i = 0; i < futures.size(); i++) {
				if (futures.get(i).get() > futures.get(bestI).get()) {
					bestI = i;
				}
			}
			return futures.get(bestI).get() > 0 ? urlList.get(bestI) : null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

//    public static void main(String[] args) {
//        try {
//            String version = simplePatch("C:\\work\\weihe\\apache-tomcat-8.5.37-windows-x64\\apache-tomcat-8.5.37\\webapps/v1-back",
//                    "http://localhost:8080/simplePatch/patch_0.0.1_0.0.2_full.zip",
//                    "http://localhost:8080/simplePatch/0.0.2.zip",
//                    null,
//                    null);
//            System.out.println(version);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

	public static String simplePatch(String localPath, String[] patchUrls, String[] fullDownloadUrls,
			ProgressListener progressListener, Runnable beforeUnzip, CancellationToken token)
			throws CancelledException, IOException {
		List<String> patchUrlList = new ArrayList<String>(Arrays.asList(patchUrls));
		List<String> fullDownloadUrlList = new ArrayList<String>(Arrays.asList(fullDownloadUrls));
		if (null == token) {
			token = new CancellationToken();
		}

		String patchUrl = selectBestMirror(patchUrlList, token);
		String localVersion = getCurrentLocalVersion(localPath);
		String localPathParent = FilenameUtils.concat(localPath, "..");
		if (null != patchUrl) {
			String filename = patchUrl.substring(patchUrl.lastIndexOf("/") + 1);
			try {
				download(patchUrl, localPathParent, progressListener, null,
						new DefaultConnectionFactory(), token);
				checkCancelled(token);

				String patchFolder = FilenameUtils.concat(localPathParent, "/_" + filename + "__");
				decompress(FilenameUtils.concat(localPathParent, filename), patchFolder);
				checkCancelled(token);
				try {
					String versionFileContent = FileUtils.readFileToString(new File(FilenameUtils.concat(patchFolder, ".update")), "utf-8");
					String[] versions = versionFileContent.split("\t");
					if (!versions[0].equals(localVersion)) {
						throw new Exception("Local version " + localVersion
								+ " does not match patch applicable version " + versions[0]);
					}
					checkCancelled(token);
					String manifestContent = FileUtils.readFileToString(new File(FilenameUtils.concat(patchFolder, ".manifest")), "utf-8");
					List<FileInfo> fileList = parseManifest(manifestContent);
					List<PatchInfo> patches = new ArrayList<PatchInfo>();

					for (FileInfo fileInfo : fileList) {
						checkCancelled(token);
						String patchPath = patchFolder + "/" + fileInfo.path;
						if (new File(patchPath).exists()) {
							patches.add(new PatchInfo(FilenameUtils.concat(localPath, fileInfo.path), patchPath, "",
									fileInfo.digest));
						}
					}
					checkCancelled(token);

					if (beforeUnzip != null) {
						beforeUnzip.run();
					}
					applyAllPatches(patches, false, true, token);
					return getCurrentLocalVersion(localPath);
				} finally {
					delFile(new File(patchFolder));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				delFile(new File(FilenameUtils.concat(localPathParent, filename)));
			}

		}
		checkCancelled(token);
		String fullDownloadUrl = selectBestMirror(fullDownloadUrlList, token);
		String fullFileName = fullDownloadUrl.substring(fullDownloadUrl.lastIndexOf("/") + 1);
		String downloadTarget = FilenameUtils.concat(localPathParent, fullFileName).toString();
		download(fullDownloadUrl, downloadTarget, progressListener, null, new DefaultConnectionFactory(), token);
		String patchFolder = FilenameUtils.concat(localPathParent, "__" + fullFileName + "__").toString();
		checkCancelled(token);

		if (beforeUnzip != null) {
			beforeUnzip.run();
		}
		decompress(downloadTarget, patchFolder);
		checkCancelled(token);
		if (!localVersion.equals("0.0.0")) {
			move(localPath, FilenameUtils.concat(localPathParent, localVersion).toString());
		}
		move(patchFolder, localPath);
		return getCurrentLocalVersion(localPath);
	}

	private static void applyAllPatches(List<PatchInfo> patches, boolean verifyOld, boolean verifyNew,
			CancellationToken token) throws CancelledException, IOException {
		for (PatchInfo patchInfo : patches) {
			checkCancelled(token);
			String patchFile = patchInfo.patchFile;
			if (patchFile.length() > 0) {
				FileUtils.copyFile(new File(patchFile), new File(patchInfo.file));
			}
		}
	}

	static class PatchInfo {
		String file;
		String patchFile;
		String oldDigest;
		String newDigest;

		public PatchInfo(String file, String patchFile, String oldDigest, String newDigest) {
			this.file = file;
			this.patchFile = patchFile;
			this.oldDigest = oldDigest;
			this.newDigest = newDigest;
		}
	}

	static void move(String localPath, String targetPath) {
		try {
			File file = new File(localPath); // 源文件
			File targetPathFile = new File(targetPath);
			FileUtils.deleteDirectory(targetPathFile);
			// 源文件移动至目标文件目录
			FileUtils.moveDirectory(file, targetPathFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class FileInfo {
		String path;
		String digest;

		public FileInfo(String path, String digest) {
			this.path = path;
			this.digest = digest;
		}
	}

	static List<FileInfo> parseManifest(String manifestString) {
		List<FileInfo> fileList = new ArrayList<FileInfo>();
		for (String fileLine : manifestString.trim().split("\n")) {
			fileLine = fileLine.trim();
			if (fileLine.length() > 0) {
				final String[] childLine = fileLine.split("\t");
				fileList.add(new FileInfo(childLine[0], childLine[1]));
			}
		}
		return fileList;
	}

	static String getCurrentLocalVersion(String localPath) {
		String versionFile =FilenameUtils.concat(localPath, ".version");
		String version;
		try {
			version = FileUtils.readFileToString(new File(versionFile), "utf-8");
		} catch (IOException e) {
			return "0.0.0";
		}
		return version;
	}

	static void checkCancelled(CancellationToken token) throws CancelledException {
		if (token.cancelled) {
			throw new CancelledException();
		}
	}

	static void decompress(String zipPath, String targetPath) {
		// 删除目录下内容
		delFile(new File(targetPath));
		// 解压到该目录
		if (zipPath.endsWith(".tar.gz")) {
			// 解压tar.gz
			try {
				unTarGz(new File(zipPath), targetPath);
			} catch (IOException e) {
				System.err.println("解压失败...");
			} catch (InterruptedException e) {
				System.err.println("解压失败...");
			}
		} else {
			// 解压zip
			try {
				unZip(new File(zipPath), targetPath);
			} catch (IOException e) {
				System.err.println("解压失败...");
			}
		}

	}

	static boolean delFile(File file) {
		if (!file.exists()) {
			return false;
		}

		if (!file.isFile()) {
			File[] files = file.listFiles();
			assert files != null;
			for (File f : files) {
				delFile(f);
			}
		}
		return file.delete();
	}

	/**
	 * 解压tar.gz 文件
	 *
	 * @param file      要解压的tar.gz文件对象
	 * @param outputDir 要解压到某个指定的目录下
	 */
	public static void unTarGz(File file, String outputDir) throws IOException, InterruptedException {
		new File(outputDir).mkdirs();
		final ProcessBuilder builder = new ProcessBuilder();
	    builder.command("tar", "zxf", file.getAbsolutePath(), "-C", outputDir);
	    final Process unzipProcess = builder.start();
	    int code = unzipProcess.waitFor();
	    System.out.println("tar zxf exited with code " + code);
//		Runtime.getRuntime().exec(String.format("tar zxf \"%s\" -C \"%s\"", file.getAbsolutePath(), outputDir))
//				.waitFor();
	}

	/**
	 * 解压缩zipFile
	 *
	 * @param file      要解压的zip文件对象
	 * @param outputDir 要解压到某个指定的目录下
	 */
	public static void unZip(File file, String outputDir) throws IOException {
		ZipFile zipFile = null;

		try {
			// Charset CP866 = Charset.forName("CP866"); // specifying alternative (non UTF-8) charset
			// ZipFile zipFile = new ZipFile(zipArchive, CP866);
			zipFile = new ZipFile(file);
			createDirectory(outputDir, null);// 创建输出目录

			Enumeration<?> enums = zipFile.entries();
			while (enums.hasMoreElements()) {

				ZipEntry entry = (ZipEntry) enums.nextElement();
//                System.out.println("解压." +  entry.getName());  

				if (entry.isDirectory() || entry.getName().endsWith("\\")) {// 是目录
					createDirectory(outputDir, entry.getName());// 创建空目录
				} else {// 是文件
					File tmpFile = new File(outputDir + "/" + entry.getName());
					createDirectory(tmpFile.getParent() + "/", null);// 创建输出目录

					InputStream in = null;
					OutputStream out = null;
					try {
						in = zipFile.getInputStream(entry);
						;
						out = new FileOutputStream(tmpFile);
						int length = 0;

						byte[] b = new byte[2048];
						while ((length = in.read(b)) != -1) {
							out.write(b, 0, length);
						}

					} finally {
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					}
				}
			}

		} catch (IOException e) {
			throw new IOException("解压缩文件出现异常", e);
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
				}
			} catch (IOException ex) {
				throw new IOException("关闭zipFile出现异常", ex);
			}
		}
	}

	/**
	 * 构建目录
	 */
	public static void createDirectory(String outputDir, String subDir) {
		File file = new File(outputDir);
		if (!(subDir == null || subDir.trim().equals(""))) {// 子目录不为空
			file = new File(outputDir + "/" + subDir);
		}
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.mkdirs();
		}
	}

	static class UpdateProgress {
		Object downloadProgress;
		int status;
		int step;
		int totalSteps;

		void constructor(int status, int totalSteps, Object downloadProgress) {
			this.status = status;
			this.step = status + 1;
			this.totalSteps = totalSteps;
			this.downloadProgress = downloadProgress;
		}
	}

	interface Promise {
		void handler();
	}

	class UpdateStatus {
		public final static int READ_LOCAL_VERSION = 0;
		public final static int FETCH_REMOTE_VERSION = 1;
		public final static int FETCH_MANIFEST = 2;
		public final static int VERIFY_LOCAL_FILES = 3;
		public final static int DOWNLOAD_PATCH = 4;
		public final static int PATCH_FILE = 5;
		public final static int SIMPLE_READ_LOCAL_VERSION = 0;
		public final static int SIMPLE_DOWNLOAD_PATCH = 1;
		public final static int SIMPLE_PATCH_FILE = 2;
		public final static int SIMPLE_DOWNLOAD_FULL = 1;
	}

}
