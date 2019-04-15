package com.aixcoder.extension.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.aixcoder.core.API;

public class AiXReportJob extends Job {

	private String type;

	public AiXReportJob(String type) {
		super("AiX Report " + type);
		this.type = type;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		API.report(type);
		return Status.OK_STATUS;
	}

}
