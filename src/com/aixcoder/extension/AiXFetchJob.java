package com.aixcoder.extension;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.utils.Predict;
import com.aixcoder.utils.Predict.PredictResult;

public class AiXFetchJob extends Job {

	private ProposalFactory proposalFactory;
	private String prefix;
	private String remainingText;

	public AiXFetchJob(String prefix, String remainingText, ProposalFactory proposalFactory) {
		super("aixcoder fetch");
		this.prefix = prefix;
		this.remainingText = remainingText;
		this.proposalFactory = proposalFactory;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		PredictResult predictResult = PredictCache.getInstance().get(prefix);
		if (predictResult == null) {
			predictResult = Predict.predict(prefix, remainingText);
			PredictCache.getInstance().put(prefix, predictResult);
		}
		// TODO: format result
		// CodeFormatter formatter = getCodeFormatter(context);
		// String lineSeparator = prefix.charAt(prefix.length() - lastLine.length()) ==
		// '\r' ? "\r\n" : "\n";
		// step 4: add proposal to list
		AiXUIJob job = new AiXUIJob(Display.getDefault(), "aixcoder async insertion",
				proposalFactory.context.getViewer(), proposalFactory, predictResult, prefix);
		job.schedule();
		return Status.OK_STATUS;
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