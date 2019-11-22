package com.aixcoder.extension.jobs;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.core.PredictCache;
import com.aixcoder.core.PredictContext;
import com.aixcoder.extension.AiXUIJob;
import com.aixcoder.extension.ProposalFactory;
import com.aixcoder.utils.Predict;
import com.aixcoder.utils.Predict.PredictResult;

public class AiXFetchJob extends Job {

	private ProposalFactory proposalFactory;
	private PredictContext predictContext;
	private String remainingText;
	private static AiXFetchJob lastInstance;
	long t = Calendar.getInstance().getTimeInMillis();

	public AiXFetchJob(PredictContext predictContext, String remainingText, ProposalFactory proposalFactory) {
		super("aiXcoder Fetch");
		this.predictContext = predictContext;
		this.remainingText = remainingText;
		this.proposalFactory = proposalFactory;
		AiXFetchJob.lastInstance = this;
		setPriority(INTERACTIVE);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		System.out.println("AiXFetchJob schedule took " + (Calendar.getInstance().getTimeInMillis() - t) + "ms");
		if (lastInstance != this)
			return Status.CANCEL_STATUS;
		try {
			PredictResult predictResult = PredictCache.getInstance().get(predictContext.prefix);
			ITextViewer viewer = proposalFactory.context.getViewer();
			if (predictResult == null) {
				System.out.println("HTTP!!!");
				String lastUUID = UUID.randomUUID().toString();
				predictResult = Predict.predict(predictContext, remainingText, lastUUID);
				if (predictResult != null) {
					PredictCache.getInstance().put(predictContext.prefix, predictResult);
				}
			}
			if (predictResult != null) {
				// TODO: format result
				// CodeFormatter formatter = getCodeFormatter(context);
				// String lineSeparator = prefix.charAt(prefix.length() - lastLine.length()) ==
				// '\r' ? "\r\n" : "\n";
				// step 4: add proposal to list
				AiXUIJob job = new AiXInsertUIJob(Display.getDefault(), viewer, proposalFactory, predictResult,
						predictContext);
				job.schedule();
				return Status.OK_STATUS;
			} else {
				return Status.CANCEL_STATUS;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Status.CANCEL_STATUS;
	}

	static CodeFormatter formatter;

	static CodeFormatter getCodeFormatter(ContentAssistInvocationContext context) {
		if (formatter == null) {
			JavaContentAssistInvocationContext jcontext = (JavaContentAssistInvocationContext) context;
			IJavaProject jproj = jcontext.getCompilationUnit().getJavaProject();
			HashMap<String, String> options = new HashMap<String, String>(jproj.getOptions(true));
			formatter = ToolFactory.createCodeFormatter(options);
		}
		return formatter;
	}

}