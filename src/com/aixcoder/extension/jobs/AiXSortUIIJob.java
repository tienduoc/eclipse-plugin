package com.aixcoder.extension.jobs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.extension.AiXAbortInsertionException;
import com.aixcoder.extension.AiXSorter;
import com.aixcoder.extension.AiXUIJob;
import com.aixcoder.utils.Pair;

public class AiXSortUIIJob extends AiXUIJob {

	private static List<Pair<String, List<Pair<Double, String>>>> listCache = new LinkedList<Pair<String, List<Pair<Double, String>>>>();
	public static String lastPrefix;
	public static String lastUUID;

	List<Pair<Double, String>> list;

	synchronized void saveToCache(String prefix, List<Pair<Double, String>> l) {
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

	synchronized List<Pair<Double, String>> getFromCache(String prefix) {
		for (Pair<String, List<Pair<Double, String>>> pair : listCache) {
			if (prefix.startsWith(pair.first)) {
				String diff = prefix.substring(pair.first.length());
				if (diff.matches("[a-zA-Z0-9_$]*")) {
					return pair.second;
				}
			}
		}
		return null;
	}

	public AiXSortUIIJob(Display jobDisplay, ITextViewer viewer, List<Pair<Double, String>> list, String prefix,
			String uuid) {
		super(jobDisplay, "aiXcoder async sorting", viewer);
		if (prefix == null) {
			prefix = lastPrefix;
		}
		if (list != null) {
			saveToCache(prefix, list);
			if (lastUUID == null || lastUUID.equals(uuid)) {
				System.out.println("sort !!");
				this.list = list;
			} else {
				this.list = null;
			}
		} else {
			this.list = getFromCache(prefix);
		}
	}

	@Override
	public void computeProposals(List<ICompletionProposal> fComputedProposal,
			List<ICompletionProposal> fFilteredProposal, AiXSorter fSorter) throws AiXAbortInsertionException {
		try {
			System.out.println("AiXSortUIIJob:" + list);
			fSorter.list = list;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiXAbortInsertionException(e);
		}
	}

}
