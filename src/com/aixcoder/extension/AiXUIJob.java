package com.aixcoder.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
public class AiXUIJob extends UIJob {

	private ICompletionProposal proposal;
	private ITextViewer viewer;

	public AiXUIJob(Display jobDisplay, String name, ITextViewer viewer, ICompletionProposal proposal) {
		super(jobDisplay, name);
		this.viewer = viewer;
		this.proposal = proposal;
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
			// insert aixcoder proposal
			fComputedProposal = new ArrayList<ICompletionProposal>(fComputedProposal);
			fComputedProposal.add(0, proposal);
			// call proposal table update function
			Method setProposals = completionProposalPopupClz.getDeclaredMethod("setProposals", List.class,
					boolean.class);
			setProposals.setAccessible(true);
			setProposals.invoke(fProposalPopup, fComputedProposal, false);
			Method dislayProposals = completionProposalPopupClz.getDeclaredMethod("displayProposals");
			dislayProposals.setAccessible(true);
			dislayProposals.invoke(fProposalPopup);
			return Status.OK_STATUS;
		} catch (Exception e) {
			return Status.CANCEL_STATUS;
		}
	}

}