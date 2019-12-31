package com.aixcoder.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class Learner {
	private String learnFilesFolder;
	private String learnFilesRegistry;
	private Thread saver;
	private Map<String, Set<String>> cached = new HashMap<String, Set<String>>();

	public Learner() {
		String aixFolder = FilenameUtils.concat(System.getProperty("user.home"), "aiXcoder");
		this.learnFilesFolder = FilenameUtils.concat(aixFolder, "learnFiles");
		this.learnFilesRegistry = FilenameUtils.concat(learnFilesFolder, "registry");
		final Learner _this = this;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					_this.save();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		this.saver = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000 * 10);
					} catch (InterruptedException e) {
						break;
					}
					try {
						_this.save();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		this.saver.start();
	}

	public void learn(String ext, String file) {
		if (!this.cached.containsKey(ext)) {
			this.cached.put(ext, new HashSet<String>());
		}
		this.cached.get(ext).add(file);
	}

	public Map<String, Map<String, String>> readRegistry() {
		HashMap<String, Map<String, String>> savedFiles = new HashMap<String, Map<String, String>>();
		try {
			List<String> registry = FileUtils.readLines(new File(learnFilesRegistry), "utf-8");
			for (String line : registry) {
				if (line.length() > 0) {
					String[] splits = line.split("\t");
					String ext = splits[0];
					String file = splits[1];
					String cachedPath = splits[2];
					if (!savedFiles.containsKey(ext)) {
						savedFiles.put(ext, new HashMap<String, String>());
					}
					savedFiles.get(ext).put(file, cachedPath);
				}
			}
		} catch (IOException e) {
			// registry not exist
		}
		return savedFiles;
	}

	public void save() throws UnsupportedEncodingException, IOException {
		if (this.cached.size() != 0) {
			Map<String, Map<String, String>> savedFiles = readRegistry();
			for (Map.Entry<String, Set<String>> entry : this.cached.entrySet()) {
				String ext = entry.getKey();
				Set<String> cached = entry.getValue();
				for (String file : cached) {
					String cachedPath = this.normalizePathToFileName(file);
					if (!savedFiles.containsKey(ext)) {
						savedFiles.put(ext, new HashMap<String, String>());
					}
					if (!savedFiles.get(ext).containsKey(file)) {
						savedFiles.get(ext).put(file, cachedPath);
					}
				}
			}
			ArrayList<String> newRegistryContent = new ArrayList<String>();
			for (Map.Entry<String, Map<String, String>> entry : savedFiles.entrySet()) {
				String ext = entry.getKey();
				Map<String, String> cached = entry.getValue();
				for (Map.Entry<String, String> entry2 : cached.entrySet()) {
					String file = entry2.getKey();
					String cachedPath = entry2.getValue();
					newRegistryContent.add(ext + "\t" + file + "\t" + cachedPath);
				}
			}
			FileUtils.writeLines(new File(learnFilesRegistry), newRegistryContent, "utf-8");
			this.cached.clear();
			System.out.println("saved");

			String url_open = "aixcoder://upload";
			try {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(url_open));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private String normalizePathToFileName(String p) {
		p = p.replaceAll("[^a-zA-Z0-9-._]+", "_");
		return p.substring(Math.max(p.length() - 128, 0));
	}

}
