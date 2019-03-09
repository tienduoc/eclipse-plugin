package com.aixcoder.extension;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.utils.Predict;
import com.aixcoder.utils.RenderedInfo;
import com.aixcoder.utils.TokenUtils;
import com.aixcoder.utils.Predict.PredictResult;

public class AiXFetchJob extends Job {

	private ProposalFactory proposalFactory;
	private String prefix;

	public AiXFetchJob(String prefix, ProposalFactory proposalFactory) {
		super("aixcoder fetch");
		this.prefix = prefix;
		this.proposalFactory = proposalFactory;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		PredictResult predictResult = Predict.predict(prefix);
		String lastLine = prefix.substring(prefix.lastIndexOf("\n") + 1);
		// step 3: render results
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(predictResult.tokens));
		RenderedInfo rendered = TokenUtils.renderTokens("java", lastLine, tokens, predictResult.current);
		// TODO: format result
		// CodeFormatter formatter = getCodeFormatter(context);
		// String lineSeparator = prefix.charAt(prefix.length() - lastLine.length()) ==
		// '\r' ? "\r\n" : "\n";
		if (rendered != null) {
			// step 4: add proposal to list
			ICompletionProposal proposal = proposalFactory.createProposal(rendered.display, rendered.insert);
			AiXUIJob job = new AiXUIJob(Display.getDefault(), "aixcoder async insertion",
					proposalFactory.context.getViewer(), proposal);
			job.schedule();
			System.out.println("job scheduled");
		}
		return Status.OK_STATUS;
	}


	static CodeFormatter formatter;

	static CodeFormatter getCodeFormatter(ContentAssistInvocationContext context) {
		if (formatter == null) {
			JavaContentAssistInvocationContext jcontext = (JavaContentAssistInvocationContext) context;
			IJavaProject jproj = jcontext.getCompilationUnit().getJavaProject();
			HashMap<String, String> options = new HashMap<>(jproj.getOptions(true));
			formatter = ToolFactory.createCodeFormatter(options);
		}
		return formatter;
	}

}