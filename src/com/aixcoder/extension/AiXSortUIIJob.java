package com.aixcoder.extension;

import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.utils.Pair;

public class AiXSortUIIJob extends AiXUIJob {

	private static List<Pair<Double, String>> list;

	public AiXSortUIIJob(Display jobDisplay, ITextViewer viewer, List<Pair<Double, String>> list) {
		super(jobDisplay, "aiXcoder async sorting", viewer);
		if (list != null) {
			AiXSortUIIJob.list = list;
		}
	}

	@Override
	public void computeProposals(List<ICompletionProposal> fComputedProposal, AiXSorter fSorter) throws AiXAbortInsertionException {
		try {
			System.out.println(list);
			fSorter.list = list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiXAbortInsertionException(e);
		}
	}

}
