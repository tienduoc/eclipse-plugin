package com.aixcoder.extension;

import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class ProposalFactory {
	ContentAssistInvocationContext context;
	ImageDescriptor image;

	public ProposalFactory(ContentAssistInvocationContext context, ImageDescriptor image) {
		super();
		this.context = context;
		this.image = image;
	}

	public ICompletionProposal createProposal(String display, String insert) {
		return new AiXCompletionProposal(insert, context.getInvocationOffset(), 0, insert.length(),
				image.createImage(), display, null, "additionalProposalInfo");
	}
}