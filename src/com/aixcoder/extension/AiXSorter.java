package com.aixcoder.extension;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;

/**
 * Sort instances of {@link AiXCompletionProposal} to the top. p1 < p2 means p1
 * is before p2.
 */
public class AiXSorter implements ICompletionProposalSorter {

	private ICompletionProposalSorter sorter;

	public AiXSorter(ICompletionProposalSorter sorter) {
		this.sorter = sorter;
	}

	@Override
	public int compare(ICompletionProposal p1, ICompletionProposal p2) {
		if (p1 instanceof AiXCompletionProposal)
			return -1;
		if (p2 instanceof AiXCompletionProposal)
			return 1;
		return sorter.compare(p1, p2);
	}

}