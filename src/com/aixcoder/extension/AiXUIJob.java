package com.aixcoder.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * Eclipse's UIJob to insert
 */
public abstract class AiXUIJob extends UIJob {

	protected ITextViewer viewer;

	public AiXUIJob(Display jobDisplay, String name, ITextViewer viewer) {
		super(jobDisplay, name);
		this.viewer = viewer;
	}

	public abstract void computeProposals(List<ICompletionProposal> fComputedProposal, AiXSorter fSorter)
			throws AiXAbortInsertionException;

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
				fSorter = new AiXSorter(fSorter);
				fContentAssistant.setSorter(fSorter);
			}

			// add proposal
			// step 1: get proposal list => fComputedProposal
			Field fProposalPopupField = fContentAssistant.getClass().getDeclaredField("fProposalPopup");
			fProposalPopupField.setAccessible(true);
			Object fProposalPopup = fProposalPopupField.get(fContentAssistant);
			Class<?> completionProposalPopupClz = fProposalPopup.getClass();
			Field fComputedProposalsField = completionProposalPopupClz.getDeclaredField("fComputedProposals");
			fComputedProposalsField.setAccessible(true);
			Object fComputedProposals = fComputedProposalsField.get(fProposalPopup);
			List<ICompletionProposal> fComputedProposal;
			if (fComputedProposals == null) {
				if (fComputedProposalsField.getType().getSimpleName().equals("ICompletionProposal[]")) {
					fComputedProposals = new ICompletionProposal[0];
				} else{
					fComputedProposals = new ArrayList<ICompletionProposal>();
				}
			}
			if (fComputedProposals instanceof List) {
				fComputedProposal = (List<ICompletionProposal>) fComputedProposals;
			} else {
				fComputedProposal = Arrays.asList((ICompletionProposal[])fComputedProposals);
			}
			if (fComputedProposal == null) {
				// 绯荤粺妗嗗凡缁忓叧闂紝鍘熷洜锛氱敤鎴峰湪aixcoder缃戠粶杩斿洖涔嬪墠灏辩户缁緭鍏ワ紝鐒跺悗绯荤粺妗嗛噷娌℃湁鍖归厤鐨勫�欓�変簡
				// TODO: 鏄惁瑕侀噸鏂板脊鍑烘彁绀烘?
			} else {
				// insert aixcoder proposal
				fComputedProposal = new ArrayList<ICompletionProposal>(fComputedProposal);

				try {
					computeProposals(fComputedProposal, (AiXSorter)fSorter);

					// call proposal table update function
					try {
					Method setProposals = completionProposalPopupClz.getDeclaredMethod("setProposals", List.class,
							boolean.class);
					setProposals.setAccessible(true);
					setProposals.invoke(fProposalPopup, fComputedProposal, false);
					} catch(NoSuchMethodException e) {
						Method setProposals = completionProposalPopupClz.getDeclaredMethod("setProposals", Class.forName("[Lorg.eclipse.jface.text.contentassist.ICompletionProposal;"),
								boolean.class);
						setProposals.setAccessible(true);
						setProposals.invoke(fProposalPopup, fComputedProposal.toArray(new ICompletionProposal[0]), false);
					}
					Method dislayProposals = completionProposalPopupClz.getDeclaredMethod("displayProposals");
					dislayProposals.setAccessible(true);
					dislayProposals.invoke(fProposalPopup);
				} catch (AiXAbortInsertionException e) {
				}
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
	}

}