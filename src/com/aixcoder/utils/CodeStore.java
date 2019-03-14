package com.aixcoder.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存上一次给服务器发送的代码，服务器会维护一份一样的数据。 通过比较保存的数据和即将发送的数据，CodeStore避免发送相同的代码前缀部分，减少网络传输
 */
public class CodeStore {
	/**
	 * 最多检查的字符数量
	 */
	static final int CHECK_LENGTH = 800;
	static final int REDUNDANCY_LENGTH = 10;
	protected static CodeStore instance;
	/**
	 * 上次的项目，每次打开新项目的时候清空
	 */
	String project = "";
	/**
	 * 当前项目各个文件的缓存情况
	 */
	Map<String, String> store = new HashMap<String, String>();

	protected CodeStore() {
	}

	public static CodeStore getInstance() {
		if (instance == null) {
			instance = new CodeStore();
		}
		return instance;
	}

	/**
	 * 获得即将发送内容和上次发送内容开始不同的下标，只发送下标往后的部分，下标本身作为offset参数发送
	 *
	 * @param fileID  文件id
	 * @param content 文件内容
	 * @return 内容开始不同的下标
	 */
	public int getDiffPosition(String fileID, String content) {
		int i;
		if (this.store.containsKey(fileID)) {
			String lastSent = this.store.get(fileID);
			// lastSent: 1000 -> [201: 1000]
			// content: 1010 -> [201: 1010]
			int initialI = Math.min(lastSent.length() - CHECK_LENGTH, content.length() - CHECK_LENGTH);
			i = Math.max(0, initialI);
			for (; i < content.length() && i < lastSent.length(); i++) {
				if (lastSent.charAt(i) != content.charAt(i)) {
					break;
				}
			}
			if (i - initialI < 3) {
				// 只匹配了两个或更少的字符
				i = 0;
			}
		} else {
			i = 0;
		}
		return Math.max(0, i - REDUNDANCY_LENGTH);
	}

	/**
	 * 发送成功之后，保存
	 *
	 * @param project 当前项目
	 * @param fileID  文件id
	 * @param content 文件内容
	 */
	public void saveLastSent(String project, String fileID, String content) {
		if (this.project == null || !this.project.equals(project)) {
			this.project = project;
			this.store.clear();
		}
		this.store.put(fileID, content);
	}

	/**
	 * 删除一个文件的缓存
	 *
	 * @param project 当前项目
	 * @param fileID  文件id
	 */
	public void invalidateFile(String project, String fileID) {
		if (this.project != null && this.project.equals(project)) {
			synchronized (this.store) {
				this.store.remove(fileID);
			}
		}
	}
}
