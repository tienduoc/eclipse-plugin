package com.aixcoder.extension.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.core.PredictCache;
import com.aixcoder.core.PredictContext;
import com.aixcoder.core.ReportType;
import com.aixcoder.extension.AiXAbortInsertionException;
import com.aixcoder.extension.AiXSorter;
import com.aixcoder.extension.AiXUIJob;
import com.aixcoder.extension.ProposalFactory;
import com.aixcoder.utils.Predict.PredictResult;
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
			List<ICompletionProposal> fFilteredProposals, AiXSorter fSorter) throws AiXAbortInsertionException {
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
							predictContext.filename);
					new AiXFetchJob(newPredictContext, remainingText, proposalFactory).schedule();
				} // else 预测结果为空
				throw new AiXAbortInsertionException();
			} else {
				ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(predictResult.tokens));
				RenderedInfo rendered = TokenUtils.renderTokens("java", lastLine, tokens, predictResult.current);
				ICompletionProposal proposal = proposalFactory.createProposal(selection.x, rendered.display,
						rendered.insert, predictResult.current, predictResult.rCompletions);

				fSorter.longProposal = null;
				String longDisplay = proposal.getDisplayString().trim();
				for (ICompletionProposal p : fFilteredProposals) {
					if (p.getDisplayString().startsWith(longDisplay)) {
						fSorter.longProposal = p;
						break;
					}
				}
				fSorter.list = predictResult.sortResults;
				if (predictResult.sortResults.length == 0 || !rendered.display.matches("[a-zA-Z0-9_$]+(\\()?")) {
					fFilteredProposals.add(0, proposal);
					fComputedProposal.add(0, proposal);	
				}
				new AiXReportJob(ReportType.SHOW).schedule();
			}
		} catch (AiXAbortInsertionException e) {
			new AiXReportJob(ReportType.INTERRUPT).schedule();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			new AiXReportJob(ReportType.ERROR).schedule();
			throw new AiXAbortInsertionException(e);
		}
	}

}
