package com.aixcoder.extension;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.utils.Pair;

public class AiXSortUIIJob extends AiXUIJob {

	private static List<Pair<String, List<Pair<Double, String>>>> listCache = new LinkedList<Pair<String, List<Pair<Double, String>>>>();
	public static String lastPrefix;

	List<Pair<Double, String>> list;

	void saveToCache(String prefix, List<Pair<Double, String>> l) {
		for (Pair<String, List<Pair<Double, String>>> pair : listCache) {
			if (prefix.equals(pair.first)) {
				pair.second = l;
				return;
			}
		}
		listCache.add(new Pair<String, List<Pair<Double, String>>>(prefix, l));
		if (listCache.size() > 10) {
			listCache.remove(0);
		}
	}

	List<Pair<Double, String>> getFromCache(String prefix) {
		for (Pair<String, List<Pair<Double, String>>> pair : listCache) {
			if (prefix.equals(pair.first)) {
				return pair.second;
			}
		}
		return null;
	}

	public AiXSortUIIJob(Display jobDisplay, ITextViewer viewer, List<Pair<Double, String>> list, String prefix) {
		super(jobDisplay, "aiXcoder async sorting", viewer);
		if (prefix == null) {
			prefix = lastPrefix;
		}
		if (list != null) {
			this.list = list;
			saveToCache(prefix, list);
		} else {
			this.list = getFromCache(prefix);
		}
	}

	@Override
	public void computeProposals(List<ICompletionProposal> fComputedProposal, AiXSorter fSorter)
			throws AiXAbortInsertionException {
		try {
			System.out.println(list);
			fSorter.list = list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiXAbortInsertionException(e);
		}
	}

}
