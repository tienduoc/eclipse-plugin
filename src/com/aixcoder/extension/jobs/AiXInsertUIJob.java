package com.aixcoder.extension.jobs;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.core.PredictCache;
import com.aixcoder.core.PredictContext;
import com.aixcoder.core.ReportType;
import com.aixcoder.extension.AiXAbortInsertionException;
import com.aixcoder.extension.AiXCoder;
import com.aixcoder.extension.AiXUIJob;
import com.aixcoder.extension.ProposalFactory;
import com.aixcoder.lang.LangOptions;
import com.aixcoder.utils.Predict;
import com.aixcoder.utils.Predict.PredictResult;
import com.aixcoder.utils.Predict.SortResult;
import com.aixcoder.utils.RenderedInfo;
import com.aixcoder.utils.TokenUtils;

public class AiXInsertUIJob extends AiXUIJob {

	private ProposalFactory proposalFactory;
	private PredictResult predictResult;
	private PredictContext predictContext;

	public AiXInsertUIJob(Display jobDisplay, ITextViewer viewer, ProposalFactory proposalFactory,
			PredictResult predictResult, PredictContext predictContext) {
		super(jobDisplay, "aiXcoder async insertion", viewer);
		this.proposalFactory = proposalFactory;
		this.predictResult = predictResult;
		this.predictContext = predictContext;
	}

	@Override
	public void computeProposals(List<ICompletionProposal> fComputedProposal,
			List<ICompletionProposal> fFilteredProposals, AiXCoder fSorter) throws AiXAbortInsertionException {
		try {
			// insert aixcoder proposal
			Point selection = viewer.getSelectedRange();
			String newPrefix = viewer.getDocument().get(0, selection.x);

			String lastLine = newPrefix.substring(newPrefix.lastIndexOf("\n") + 1);
			// step 3: render results
			predictResult = PredictCache.getInstance().get(newPrefix);
			log("predictResult: " + (predictResult == null ? "null" : predictResult.toString()));
			if (predictResult == null) {
				if (!predictContext.prefix.equals(newPrefix)) {
					IRegion line = viewer.getDocument().getLineInformationOfOffset(selection.x);
					String remainingText = viewer.getDocument().get(selection.x,
							line.getOffset() + line.getLength() - selection.x);
					// 文本变化，重新发起请求
					log("文本变化，重新发起请求");
					PredictContext newPredictContext = new PredictContext(newPrefix, predictContext.proj,
							predictContext.filename, predictContext.projRoot);
					new AiXFetchJob(newPredictContext, remainingText, proposalFactory).schedule();
				} // else 预测结果为空
				throw new AiXAbortInsertionException();
			} else {
				ArrayList<ICompletionProposal> longPredicts = new ArrayList<ICompletionProposal>(
						predictResult.longPredicts.length);
				String current = null;
				for (Predict.LongPredictResult longPredict : predictResult.longPredicts) {
					if (current == null) {
						current = longPredict.current;
					}
					ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(longPredict.tokens));
					LangOptions langOptions = LangOptions.getInstance("java");
					RenderedInfo rendered = TokenUtils.renderTokens("java", lastLine, tokens, longPredict.current,
							langOptions);
					ICompletionProposal proposal = proposalFactory.createProposal(selection.x, rendered.display,
							rendered.insert, longPredict.current, longPredict.rCompletions, longPredict.rescues,
							langOptions);
					longPredicts.add(proposal);
				}

				fSorter.longProposal = new ArrayList<ICompletionProposal>();
				for (ICompletionProposal proposal : longPredicts) {
					String longDisplay = proposal.getDisplayString().trim();
					boolean dup = false;
					for (ICompletionProposal p : fFilteredProposals) {
						if (p.getDisplayString().startsWith(longDisplay)) {
							fSorter.longProposal.add(p);
							dup = true;
							break;
						}
					}
					if (!dup) {
						fFilteredProposals.add(0, proposal);
						fComputedProposal.add(0, proposal);
					}
				}
				if (predictResult.sortResults != null) {
					Map<ICompletionProposal, Double> scoreMap = new HashMap<ICompletionProposal, Double>();
					Map<SortResult, Set<ICompletionProposal>> sortResultMap = new HashMap<SortResult, Set<ICompletionProposal>>();
					for (SortResult sortResult : predictResult.sortResults) {
						boolean matched = false;
						for (ICompletionProposal p : fComputedProposal) {
							String s = p.getDisplayString();
							if (s.equals(sortResult.word) || s.startsWith(sortResult.word + " ")
									|| s.startsWith(sortResult.word + "(")) {
								matched = true;
								scoreMap.put(p, sortResult.prob);
								if (!sortResultMap.containsKey(sortResult)) {
									sortResultMap.put(sortResult, new HashSet<ICompletionProposal>());
								}
								sortResultMap.get(sortResult).add(p);
							}
						}
						if (sortResult.options != null && sortResult.options.forced && !matched) {
							ICompletionProposal forcedProposal = proposalFactory.createForcedSortProposal(selection.x,
									sortResult.word, current, sortResult.prob);
							fFilteredProposals.add(0, forcedProposal);
							fComputedProposal.add(0, forcedProposal);
							scoreMap.put(forcedProposal, sortResult.prob);
							if (!sortResultMap.containsKey(sortResult)) {
								sortResultMap.put(sortResult, new HashSet<ICompletionProposal>());
							}
							sortResultMap.get(sortResult).add(forcedProposal);
						}
					}
					for (Entry<SortResult, Set<ICompletionProposal>> e : sortResultMap.entrySet()) {
						SortResult sortResult = e.getKey();
						if (e.getValue().size() > 1 && sortResult.options != null && sortResult.options.filters != null
								&& sortResult.options.filters.length > 0) {
							String[] filters = sortResult.options.filters;
							ICompletionProposal best = null;
							int bestRank = 999;
							for (ICompletionProposal p : e.getValue()) {
								String s = p.getDisplayString();
								if (s.contains(" - ")) {
									int rank = 0;
									for (String filter : filters) {
										if (filter.endsWith("." + sortResult.word)) {
											filter = filter.substring(0,
													filter.length() - sortResult.word.length() - 1);
										}
										if (s.contains(filter)) {
											break;
										}
										rank++;
									}
									if (rank < bestRank) {
										bestRank = rank;
										best = p;
									}
								}
							}
							if (best != null) {
								for (ICompletionProposal p : e.getValue()) {
									if (p != best) {
										scoreMap.remove(p);
									}
								}
							}
						}
					}
					ArrayList<Entry<ICompletionProposal, Double>> sortedResults = new ArrayList<Entry<ICompletionProposal, Double>>(
							scoreMap.entrySet());
					Collections.sort(sortedResults, new Comparator<Entry<ICompletionProposal, Double>>() {
						@Override
						public int compare(Entry<ICompletionProposal, Double> o1,
								Entry<ICompletionProposal, Double> o2) {
							return (int) Math.signum(o2.getValue() - o1.getValue());
						}
					});
					for (int i = 0; i < sortedResults.size(); i++) {
						sortedResults.get(i).setValue((double) (sortedResults.size() - i));
					}
					fSorter.scoreMap = scoreMap;
				}

				new AiXReportJob(ReportType.LONG_SHOW, 0, 0).schedule();
			}
		} catch (AiXAbortInsertionException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiXAbortInsertionException(e);
		}
	}

}
