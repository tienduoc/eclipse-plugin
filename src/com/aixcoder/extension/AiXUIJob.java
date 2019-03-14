package com.aixcoder.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import com.aixcoder.utils.Predict.PredictResult;
import com.aixcoder.core.PredictCache;
import com.aixcoder.core.PredictContext;
import com.aixcoder.utils.RenderedInfo;
import com.aixcoder.utils.TokenUtils;

/**
 * Eclipse's UIJob to insert
 */
public class AiXUIJob extends UIJob {

	private ProposalFactory proposalFactory;
	private ITextViewer viewer;
	private PredictResult predictResult;
	private PredictContext predictContext;

	public AiXUIJob(Display jobDisplay, String name, ITextViewer viewer, ProposalFactory proposalFactory,
			PredictResult predictResult, PredictContext predictContext) {
		super(jobDisplay, name);
		this.viewer = viewer;
		this.proposalFactory = proposalFactory;
		this.predictResult = predictResult;
		this.predictContext = predictContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		try {
			Field fContentAssistantField = SourceViewer.class.getDeclaredField("fContentAssistant");
			fContentAssistantField.setAccessible(true);
			ContentAssistant fContentAssistant = (ContentAssistant) fContentAssistantField.get(viewer);

			// set sorter
			Field fSorterField = ContentAssistant.class.getDeclaredField("fSorter");
			fSorterField.setAccessible(true);
			ICompletionProposalSorter fSorter = (ICompletionProposalSorter) fSorterField.get(fContentAssistant);
			if (!(fSorter instanceof AiXSorter)) {
				AiXSorter mySorter = new AiXSorter(fSorter);
				fContentAssistant.setSorter(mySorter);
			}

			// add proposal
			// step 1: get proposal list => fComputedProposal
			Field fProposalPopupField = fContentAssistant.getClass().getDeclaredField("fProposalPopup");
			fProposalPopupField.setAccessible(true);
			Object fProposalPopup = fProposalPopupField.get(fContentAssistant);
			Class<?> completionProposalPopupClz = fProposalPopup.getClass();
			Field fComputedProposalsField = completionProposalPopupClz.getDeclaredField("fComputedProposals");
			fComputedProposalsField.setAccessible(true);
			List<ICompletionProposal> fComputedProposal = (List<ICompletionProposal>) fComputedProposalsField
					.get(fProposalPopup);
			if (fComputedProposal == null) {
				// 系统框已经关闭，原因：用户在aixcoder网络返回之前就继续输入，然后系统框里没有匹配的候选了
				// TODO: 是否要重新弹出提示框?
			} else {
				// insert aixcoder proposal
				fComputedProposal = new ArrayList<ICompletionProposal>(fComputedProposal);

				Point selection = viewer.getSelectedRange();
				String newPrefix = viewer.getDocument().get(0, selection.x);

				String lastLine = newPrefix.substring(newPrefix.lastIndexOf("\n") + 1);
				// step 3: render results
				predictResult = PredictCache.getInstance().get(newPrefix);
				System.out.println("predictResult" + (predictResult == null ? "null" : predictResult.toString()));
				if (predictResult == null) {
					if (!predictContext.prefix.equals(newPrefix)) {
						IRegion line = viewer.getDocument().getLineInformationOfOffset(selection.x);
						String remainingText = viewer.getDocument().get(selection.x,
								line.getOffset() + line.getLength() - selection.x);
						// 文本变化，重新发起请求
						PredictContext newPredictContext = new PredictContext(newPrefix, predictContext.proj,
								predictContext.filename);
						new AiXFetchJob(newPredictContext, remainingText, proposalFactory).schedule();
					} // else 预测结果为空
				} else {
					ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(predictResult.tokens));
					RenderedInfo rendered = TokenUtils.renderTokens("java", lastLine, tokens, predictResult.current);

					ICompletionProposal proposal = proposalFactory.createProposal(rendered.display, rendered.insert,
							predictResult.rCompletions);

					fComputedProposal.add(0, proposal);
					// call proposal table update function
					Method setProposals = completionProposalPopupClz.getDeclaredMethod("setProposals", List.class,
							boolean.class);
					setProposals.setAccessible(true);
					setProposals.invoke(fProposalPopup, fComputedProposal, false);
					Method dislayProposals = completionProposalPopupClz.getDeclaredMethod("displayProposals");
					dislayProposals.setAccessible(true);
					dislayProposals.invoke(fProposalPopup);
				}
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
	}

}