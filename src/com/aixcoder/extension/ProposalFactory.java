package com.aixcoder.extension;

import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

import com.aixcoder.lang.LangOptions;
import com.aixcoder.utils.Rescue;

public class ProposalFactory {
	public ContentAssistInvocationContext context;
	static final Image image = Activator
			.imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(), "icons/aix_log.png")
			.createImage();

	public ProposalFactory(ContentAssistInvocationContext context) {
		super();
		this.context = context;
	}

	public ICompletionProposal createProposal(int invocationOffset, String display, String insert, String current,
			String[] rCompletion, Rescue[] rescues, LangOptions langOptions) {
		return new AiXCompletionProposal(current + insert, invocationOffset - current.length(), current.length(),
				current.length() + insert.length(), image, display, null, rCompletion, rescues, langOptions);
	}

	public ICompletionProposal createForcedSortProposal(int invocationOffset, String word, String current,
			double score) {
		return new AiXForcedSortCompletionProposal(word, invocationOffset - current.length(), current.length(),
				word.length(), null, word, null, score);
	}
}