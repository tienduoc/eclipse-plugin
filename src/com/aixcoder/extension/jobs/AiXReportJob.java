package com.aixcoder.extension.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.aixcoder.core.API;
import com.aixcoder.core.ReportType;

public class AiXReportJob extends Job {

	private ReportType type;
	private int tokenNum;
	private int charNum;

	public AiXReportJob(ReportType type, int tokenNum, int charNum) {
		super("AiX Report " + type);
		this.type = type;
		this.tokenNum = tokenNum;
		this.charNum = charNum;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		API.report(type, tokenNum, charNum);
		return Status.OK_STATUS;
	}

}
