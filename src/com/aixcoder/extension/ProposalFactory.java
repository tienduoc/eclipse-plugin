package com.aixcoder.extension;

import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class ProposalFactory {
	ContentAssistInvocationContext context;
	static final ImageDescriptor image = Activator
			.imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(), "icons/aix_log.png");;

	public ProposalFactory(ContentAssistInvocationContext context) {
		super();
		this.context = context;
	}

	public ICompletionProposal createProposal(int invocationOffset, String display, String insert, String[] rCompletion) {
		return new AiXCompletionProposal(insert, invocationOffset, 0, insert.length(), image.createImage(),
				display, null, "additionalProposalInfo", rCompletion);
	}
}