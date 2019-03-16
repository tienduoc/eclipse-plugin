package com.aixcoder.utils.zipfile;

import org.eclipse.core.resources.IProject;

public class ProjectScan {

	static ProjectScan instance;
	private IProject proj;
	private Thread t;

	public ProjectScan(IProject proj) {
		this.proj = proj;
	}

	public static ProjectScan getInstance(IProject project) {
		if (instance == null) {
			instance = new ProjectScan(project);
		}
		if (!instance.proj.getName().equals(project.getName())) {
			instance.dispose();
			instance = new ProjectScan(project);
		}
		return instance;
	}

	public void start() {
		if (t == null) {
			t = new ProjectScanThread(proj.getName(), proj.getLocation().toOSString());
			t.start();
		}
	}

	public void dispose() {
		t.interrupt();
	}
}
