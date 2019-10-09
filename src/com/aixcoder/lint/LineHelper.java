package com.aixcoder.lint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LineHelper {
	int[] start;
	int[] length;

	public LineHelper(String projectPath, String filePath) {
		String[] content;
		try {
			content = new String(Files.readAllBytes(Paths.get(projectPath, filePath))).split("(?<=\n)");
			start = new int[content.length];
			length = new int[content.length];
			int offset = 0;
			for (int i = 0; i < content.length; i++) {
				String line = content[i];
				start[i] = offset;
				offset += line.length();
				length[i] = line.length();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getLineOffset(int line) {
		return start[line];
	}

	public int getLineLength(int line) {
		return length[line];
	}

	public int getLineOfOffset(int offset) {
		int line = 0;
		for (int i = 0; i < start.length; i++) {
			if (start[i] > offset) {
				return line;
			}
			line = i;
		}
		return start.length - 1;
	}
}
