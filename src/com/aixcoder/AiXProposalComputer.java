package com.aixcoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * This represents a single proposal item in the proposals (a.k.a. suggestions)
 * list. Code copied from
 * {@link org.eclipse.jface.text.contentassist.CompletionProposal}
 */
@SuppressWarnings("restriction")
class AiXCompletionProposal implements ICompletionProposal, ICompletionProposalExtension5 {

	/** The string to be displayed in the completion proposal popup. */
	private String fDisplayString;
	/** The replacement string. */
	private String fReplacementString;
	/** The replacement offset. */
	private int fReplacementOffset;
	/** The replacement length. */
	private int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;

	/**
	 * Creates a new completion proposal based on the provided information. The
	 * replacement string is considered being the display string too. All remaining
	 * fields are set to <code>null</code>.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition    the position of the cursor following the insert
	 *                          relative to replacementOffset
	 */
	public AiXCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on the
	 * provided information.
	 *
	 * @param replacementString      the actual string to be inserted into the
	 *                               document
	 * @param replacementOffset      the offset of the text to be replaced
	 * @param replacementLength      the length of the text to be replaced
	 * @param cursorPosition         the position of the cursor following the insert
	 *                               relative to replacementOffset
	 * @param image                  the image to display for this proposal
	 * @param displayString          the string to be displayed for the proposal
	 * @param contextInformation     the context information associated with this
	 *                               proposal
	 * @param additionalProposalInfo the additional information associated with this
	 *                               proposal
	 */
	public AiXCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
			String additionalProposalInfo) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		fReplacementString = replacementString;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
		fCursorPosition = cursorPosition;
		fImage = image;
		fDisplayString = displayString;
		fContextInformation = contextInformation;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	@Override
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public String getDisplayString() {
		if (fDisplayString != null)
			return fDisplayString;
		return fReplacementString;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	/**
	 * This allows asynchronous fetch of additional info, displayed in html. Can be
	 * used to display search result.
	 */
	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
		monitor.setTaskName("aix fetch");
		try {
			// wait up to 500ms by default
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "long long result " + Math.random();
	}
}

/**
 * Sort instances of {@link AiXCompletionProposal} to the top. p1 < p2 means p1
 * is before p2.
 */
class AiXSorter implements ICompletionProposalSorter {

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

/**
 * Eclipse's UIJob to insert
 */
class AiXUIJob extends UIJob {

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

/**
 * Extension class to extend org.eclipse.jdt.ui.javaCompletionProposalComputer.
 */
@SuppressWarnings("restriction")
public class AiXProposalComputer extends JavaAllCompletionProposalComputer {
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		// take default Eclipse formatting options
//		FormatterProfileManager.getProjectSettings(context.getDocument().)
		JavaContentAssistInvocationContext jcontext = (JavaContentAssistInvocationContext) context;
		IJavaProject jproj = jcontext.getCompilationUnit().getJavaProject();
		HashMap<String, String> options = new HashMap<>(jproj.getOptions(true));
		ITextViewer viewer = context.getViewer();

		ImageDescriptor image = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/aix_log.png");
		// Eclipse's way of using its thread pool
		new Job("aixcoder remote fetch") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					int offset = viewer.getSelectedRange().x;
					String prefix = viewer.getDocument().get(0, offset);
					String[] tokens = Predict.predict(prefix);
					AiXCompletionProposal proposal = new AiXCompletionProposal("lala hello",
							context.getInvocationOffset(), 0, 5, image.createImage(), "lala displayString",
							new IContextInformation() {
								@Override
								public String getInformationDisplayString() {
									// TODO Auto-generated method stub
									return "getInformationDisplayString";
								}

								@Override
								public Image getImage() {
									// TODO Auto-generated method stub
									return image.createImage();
								}

								@Override
								public String getContextDisplayString() {
									// TODO Auto-generated method stub
									return "getContextDisplayString";
								}
							}, "additionalProposalInfo");
					AiXUIJob job = new AiXUIJob(Display.getDefault(), "aixcoder async insertion", viewer, proposal);
					job.schedule();
					System.out.println("job scheduled");
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		}.schedule();

		List<ICompletionProposal> superProposals = super.computeCompletionProposals(context, monitor);
		return superProposals;
	}
}
