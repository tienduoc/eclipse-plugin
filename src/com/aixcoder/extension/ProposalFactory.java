package com.aixcoder.extension;

import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

public class ProposalFactory {
	ContentAssistInvocationContext context;
	static final Image image = Activator
			.imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(), "icons/aix_log.png").createImage();

	public ProposalFactory(ContentAssistInvocationContext context) {
		super();
		this.context = context;
	}

	public ICompletionProposal createProposal(int invocationOffset, String display, String insert, String current, String[] rCompletion) {
		return new AiXCompletionProposal(current + insert, invocationOffset - current.length(), current.length(), current.length() + insert.length(), image,
				display, null, "additionalProposalInfo", rCompletion);
	}
}