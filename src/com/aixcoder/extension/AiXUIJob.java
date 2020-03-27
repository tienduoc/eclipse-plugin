package com.aixcoder.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
	long t = Calendar.getInstance().getTimeInMillis();

	protected void log(String s) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		System.out.println(timeStamp + " " + this + " " + s);
	}

	public AiXUIJob(Display jobDisplay, String name, ITextViewer viewer) {
		super(jobDisplay, name);
		this.viewer = viewer;
		setPriority(INTERACTIVE);
//		log("AiXUIJob constructor " + name);
//		log(viewer.getDocument().get().substring(Math.max(0, viewer.getDocument().getLength() - 100)));
	}

	public abstract void computeProposals(List<ICompletionProposal> fComputedProposal,
			List<ICompletionProposal> fFilteredProposals, AiXCoder fSorter) throws AiXAbortInsertionException;

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		System.out.println(getName() + " schedule took " + (Calendar.getInstance().getTimeInMillis() - t) + "ms");
		try {
			Field fContentAssistantField = SourceViewer.class.getDeclaredField("fContentAssistant");
			fContentAssistantField.setAccessible(true);
			ContentAssistant fContentAssistant = (ContentAssistant) fContentAssistantField.get(viewer);
//			log("runInUIThread");
//			log(viewer.getDocument().get().substring(Math.max(0, viewer.getDocument().getLength() - 100)));
			// set sorter
			Field fSorterField = ContentAssistant.class.getDeclaredField("fSorter");
			fSorterField.setAccessible(true);
			ICompletionProposalSorter fSorter = (ICompletionProposalSorter) fSorterField.get(fContentAssistant);
			if (!(fSorter instanceof AiXCoder)) {
				fSorter = new AiXCoder(fSorter);
				fContentAssistant.setSorter(fSorter);
			}

			// add proposal
			// step 1: get proposal list => fComputedProposal
			Field fProposalPopupField = fContentAssistant.getClass().getDeclaredField("fProposalPopup");
			fProposalPopupField.setAccessible(true);
			Object fProposalPopup = fProposalPopupField.get(fContentAssistant);
			Class<?> completionProposalPopupClz = fProposalPopup.getClass();
			List<ICompletionProposal> fComputedProposal = getProposalList(fProposalPopup, completionProposalPopupClz,
					"fComputedProposals");
			List<ICompletionProposal> fFilteredProposal = getProposalList(fProposalPopup, completionProposalPopupClz,
					"fFilteredProposals");
			if (fComputedProposal == null) {
				// 绯荤粺妗嗗凡缁忓叧闂紝鍘熷洜锛氱敤鎴峰湪aixcoder缃戠粶杩斿洖涔嬪墠灏辩户缁緭鍏ワ紝鐒跺悗绯荤粺妗嗛噷娌℃湁鍖归厤鐨勫�欓�変簡
				// TODO: 鏄惁瑕侀噸鏂板脊鍑烘彁绀烘?
			} else {
				// insert aixcoder proposal
				fComputedProposal = new ArrayList<ICompletionProposal>(fComputedProposal);
				fFilteredProposal = new ArrayList<ICompletionProposal>(fFilteredProposal);

				try {
					computeProposals(fComputedProposal, fFilteredProposal, (AiXCoder) fSorter);

					// remove stub
					int stub = -1;
					for (int i = 0; i < fComputedProposal.size(); i++) {
						if (fComputedProposal.get(i).getDisplayString().equals(" ")) {
							stub = i;
							break;
						}
					}
					if (stub >= 0) {
						fComputedProposal.remove(stub);
					}

					if (fComputedProposal.size() == 0) {
						Method hide = completionProposalPopupClz.getDeclaredMethod("hide");
						hide.setAccessible(true);
						hide.invoke(fProposalPopup);
					} else {
						Field fIsFilterPendingField = completionProposalPopupClz.getDeclaredField("fIsFilterPending");
						fIsFilterPendingField.setAccessible(true);
						boolean fIsFilterPending = false;
						try {
							Object fIsFilterPendingObj = fIsFilterPendingField.get(fProposalPopup);
							if (fIsFilterPendingObj instanceof java.lang.Boolean) {
								// before Eclipse2020
								fIsFilterPending = ((java.lang.Boolean) fIsFilterPendingObj).booleanValue();
							} else if (fIsFilterPendingObj instanceof java.util.concurrent.atomic.AtomicBoolean) {
								// Eclipse2020
								fIsFilterPending = ((java.util.concurrent.atomic.AtomicBoolean) fIsFilterPendingObj).get();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (fIsFilterPending) {
							Field fFilterRunnableField = completionProposalPopupClz.getDeclaredField("fFilterRunnable");
							fFilterRunnableField.setAccessible(true);
							Runnable fFilterRunnable = (Runnable) fFilterRunnableField.get(fProposalPopup);
							fFilterRunnable.run();
						}

						setProposalList(fProposalPopup, completionProposalPopupClz, "fFilteredProposals",
								fFilteredProposal);

						// call proposal table update function
//						log("setProposals: " + fComputedProposal);
//						log(viewer.getDocument().get().substring(Math.max(0, viewer.getDocument().getLength() - 100)));
						try {
							Method setProposals = completionProposalPopupClz.getDeclaredMethod("setProposals",
									List.class, boolean.class);
							setProposals.setAccessible(true);
							setProposals.invoke(fProposalPopup, fComputedProposal, false);
						} catch (NoSuchMethodException e) {
							Method setProposals = completionProposalPopupClz.getDeclaredMethod("setProposals",
									Class.forName("[Lorg.eclipse.jface.text.contentassist.ICompletionProposal;"),
									boolean.class);
							setProposals.setAccessible(true);
							setProposals.invoke(fProposalPopup, fComputedProposal.toArray(new ICompletionProposal[0]),
									false);
						}
						Field fIsFilteredSubsetField = completionProposalPopupClz.getDeclaredField("fIsFilteredSubset");
						fIsFilteredSubsetField.setAccessible(true);
						if (fIsFilteredSubsetField.getBoolean(fProposalPopup)) {
//							log("filterProposals");
							Method filterProposalsMethod = completionProposalPopupClz
									.getDeclaredMethod("filterProposals");
							filterProposalsMethod.setAccessible(true);
							filterProposalsMethod.invoke(fProposalPopup);
						} else {
//							log("displayProposals" + fComputedProposal);
							Method dislayProposals = completionProposalPopupClz.getDeclaredMethod("displayProposals");
							dislayProposals.setAccessible(true);
							dislayProposals.invoke(fProposalPopup);
						}
					}
				} catch (AiXAbortInsertionException e) {
					System.out.println(e);
				}
			}
			System.out.println(this.getName() + " took "
					+ (Calendar.getInstance().getTimeInMillis() - AiXProposalComputer.t) + "ms");
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
	}

	private void setProposalList(Object fProposalPopup, Class<?> completionProposalPopupClz, String fieldName,
			List<ICompletionProposal> fFilteredProposal)
			throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field fComputedProposalsField = completionProposalPopupClz.getDeclaredField(fieldName);
		fComputedProposalsField.setAccessible(true);
		Object fFilteredProposalObj;
		if (fComputedProposalsField.getType().getSimpleName().equals("ICompletionProposal[]")) {
			fFilteredProposalObj = fFilteredProposal.toArray(new ICompletionProposal[0]);
		} else {
			fFilteredProposalObj = fFilteredProposal;
		}
		fComputedProposalsField.set(fProposalPopup, fFilteredProposalObj);
	}

	@SuppressWarnings("unchecked")
	private List<ICompletionProposal> getProposalList(Object fProposalPopup, Class<?> completionProposalPopupClz,
			String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field fComputedProposalsField = completionProposalPopupClz.getDeclaredField(fieldName);
		fComputedProposalsField.setAccessible(true);
		Object fComputedProposals = fComputedProposalsField.get(fProposalPopup);
		List<ICompletionProposal> fComputedProposal;
		if (fComputedProposals == null) {
			if (fComputedProposalsField.getType().getSimpleName().equals("ICompletionProposal[]")) {
				fComputedProposals = new ICompletionProposal[0];
			} else {
				fComputedProposals = new ArrayList<ICompletionProposal>();
			}
		}
		if (fComputedProposals instanceof List) {
			fComputedProposal = (List<ICompletionProposal>) fComputedProposals;
		} else {
			fComputedProposal = Arrays.asList((ICompletionProposal[]) fComputedProposals);
		}
		return fComputedProposal;
	}

}