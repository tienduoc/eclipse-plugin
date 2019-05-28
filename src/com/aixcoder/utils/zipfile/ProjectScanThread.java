package com.aixcoder.utils.zipfile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aixcoder.core.API;
import com.aixcoder.lib.HttpRequest.HttpRequestException;

public class ProjectScanThread extends Thread {
	private final HashMap<String, Long> lastChecked = new HashMap<String, Long>();
	private String projectPath;
	private String project;

	public ProjectScanThread(String project, String projectPath) {
		this.projectPath = projectPath;
		this.project = project;
	}

	public byte[] read(File file) throws IOException {
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ((read = ios.read(buffer)) != -1) {
				ous.write(buffer, 0, read);
			}
		} finally {
			try {
				if (ous != null)
					ous.close();
			} catch (IOException e) {
			}

			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
			}
		}
		return ous.toByteArray();
	}

	public String readFile(File file) throws IOException {
		if (!file.getName().endsWith(".java"))
			return null;
		String text = null;
		byte[] bytesArr = read(file);
		text = new String(bytesArr, "utf-8");
		return text;
	}

	public List<File> getListFile(File file) {
		List<File> dataList = new ArrayList<File>();
		getListFile(new File(projectPath), dataList);
		return dataList;
	}

	public void getListFile(File file, List<File> dataList) {
		File[] listFiles = file.listFiles();
		if (listFiles == null || listFiles.length <= 0)
			return;
		for (File f : listFiles) {
			if (f.isFile()) {
				String absolutePath = f.getAbsolutePath();
				if (absolutePath.endsWith(".java")) {
					long fLastModified = f.lastModified();
					if (!lastChecked.containsKey(absolutePath) || lastChecked.get(absolutePath) < fLastModified) {
						dataList.add(f);
						lastChecked.put(absolutePath, fLastModified);
					}
				}
			} else {
				getListFile(f, dataList);// 递归
			}
		}
	}

	public void run() {
		while (true) {
			try {
				// scan files for change
				List<File> dataList = getListFile(new File(projectPath));
				Thread.sleep(0);
				String[] data = new String[dataList.size() * 2];
				for (int i = 0; i < dataList.size(); i++) {
					File f = dataList.get(i);
					handleSingleFile(data, i, f);
				}
				// send zip file
				API.zipFile(project, data);
				Thread.sleep(10 * 60 * 1000);
			} catch (InterruptedException e) {
				break;
			} catch (HttpRequestException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleSingleFile(String[] data, int i, File f) {
		try {
			String text = readFile(f);
			String path = f.getAbsolutePath();
			if (!path.startsWith(projectPath)) {
				System.err.println("ProjectScanThread: " + path + " does not start with " + projectPath);
				return;
			}
			path = path.substring(projectPath.length());
			data[i * 2] = path;
			data[i * 2 + 1] = text;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}