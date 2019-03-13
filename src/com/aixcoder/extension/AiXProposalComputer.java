package com.aixcoder.extension;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Extension class to extend org.eclipse.jdt.ui.javaCompletionProposalComputer.
 */
@SuppressWarnings("restriction")
public class AiXProposalComputer extends JavaAllCompletionProposalComputer {
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		ProposalFactory proposalFactory = new ProposalFactory(context);
		try {
			// step 1: get text before cursor
			int offset = context.getInvocationOffset();
			String prefix = context.getDocument().get(0, offset);
			IRegion line = context.getDocument().getLineInformationOfOffset(offset);
			String remainingText = context.getDocument().get(offset, line.getOffset() + line.getLength() - offset);
//			System.out.println("computeCompletionProposals + " + prefix.substring(Math.max(0, prefix.length() - 50)));
//			System.out.println("==============");
			// step 2: send request
			// Eclipse's way of using its thread pool
			new AiXFetchJob(prefix, remainingText, proposalFactory).schedule();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		List<ICompletionProposal> superProposals = super.computeCompletionProposals(context, monitor);
		return new ArrayList<ICompletionProposal>();
	}
}
