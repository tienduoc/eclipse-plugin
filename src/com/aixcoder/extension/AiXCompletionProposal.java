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
import com.aixcoder.utils.shims.CollectionUtils;

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
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	private String[] fRCompletion;

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
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null, null);
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
			String additionalProposalInfo, String[] rCompletion) {
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
		if (rCompletion != null && rCompletion.length > 0) {
			fDisplayString += "..." + CollectionUtils.join("", rCompletion);
		}
		fContextInformation = contextInformation;
		fRCompletion = rCompletion;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
			if (fRCompletion != null) {
				document.replace(fReplacementOffset + fReplacementString.length(), 0, CollectionUtils.join("", fRCompletion));
			}
			new AiXReportJob(ReportType.USE).schedule();
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