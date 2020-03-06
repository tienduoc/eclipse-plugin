package com.aixcoder.extension;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.aixcoder.core.ReportType;
import com.aixcoder.extension.jobs.AiXReportJob;
import com.aixcoder.lang.LangOptions;
import com.aixcoder.utils.Rescue;
import com.aixcoder.utils.shims.CollectionUtils;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

/**
 * This represents a single proposal item in the proposals (a.k.a. suggestions)
 * list. Code copied from
 * {@link org.eclipse.jface.text.contentassist.CompletionProposal}
 */
public class AiXCompletionProposal implements ICompletionProposal, ICompletionProposalExtension5 {

	/** The string to be displayed in the completion proposal popup. */
	private String fDisplayString;
	/** The replacement string. */
	private String fReplacementString;
	/** The replacement offset. */
	private int fReplacementOffset;
	/** The replacement length. */
	private int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	/** bug: there might be other completion auto added by IDE(eg. import xxx). So, do not use the input fCursorPosition. **/
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	private String[] fRCompletion;
	private Rescue[] fRescues;
	private LangOptions fLangOptions;
	private int newOffset;
	private boolean cursorShouldMove;

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
			String[] rCompletion, Rescue[] rescues, LangOptions langOptions) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		fReplacementString = replacementString;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
//		fCursorPosition = cursorPosition;
		fImage = image;
		fDisplayString = displayString;
		if (rCompletion != null && rCompletion.length > 0) {
			fDisplayString += "..." + CollectionUtils.join("", rCompletion);
		}
		fContextInformation = contextInformation;
		fRCompletion = rCompletion;
		fRescues = rescues;
		fLangOptions = langOptions;
		newOffset = fReplacementOffset;
		cursorShouldMove = true;
	}
	IDocumentListener aixlistener = new IDocumentListener() {
		public void documentAboutToBeChanged(DocumentEvent event) {
			// do nothing
		}
		public void documentChanged(DocumentEvent event) {
			if (cursorShouldMove && event.getOffset() <= newOffset) {
				newOffset += event.getText().length();
			}
		}
	};

	@Override
	public void apply(IDocument document) {
		try {
			document.addDocumentListener(aixlistener);
//			final String updateCategory = "aixcoder-completion";
//			IPositionUpdater aixUpdater = new DefaultPositionUpdater(updateCategory);
//			Position aixPosition = new Position(fReplacementOffset, fReplacementLength);
//			document.addPositionCategory(updateCategory);
////			document.insertPositionUpdater(aixUpdater, 0);
//			document.addPositionUpdater(aixUpdater);
//			document.addPosition(updateCategory, aixPosition);
			cursorShouldMove = true;
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
			cursorShouldMove = true;

//			DocumentEvent aixEvent = new DocumentEvent(document, fReplacementOffset, fReplacementLength, fReplacementString);
//			aixUpdater.update(aixEvent);
//			document.removePosition(updateCategory, aixPosition);
//			document.removePositionUpdater(aixUpdater);
//			document.removePositionCategory(updateCategory);
			if (fRCompletion != null) {
				cursorShouldMove = false;
				document.replace(fReplacementOffset + fReplacementString.length(), 0,
						CollectionUtils.join("", fRCompletion));
				cursorShouldMove = true;
			}
			if (fRescues != null && fRescues.length > 0) {
				fLangOptions.rescue(document, fRescues);
			}
			new AiXReportJob(ReportType.LONG_USE, fDisplayString.split("\\b").length, fDisplayString.length()).schedule();
		} catch (BadLocationException x) {
			// ignore
			x.printStackTrace();
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		document.removeDocumentListener(aixlistener);
		return new Point(newOffset, 0);
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
//		monitor.setTaskName("aix fetch");
//		try {
//			// wait up to 500ms by default
//			Thread.sleep(300);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return "long long result " + Math.random();
		return null;
	}

	@Override
	public String toString() {
		return "AiXCompletionProposal: " + fReplacementString;
	}
}