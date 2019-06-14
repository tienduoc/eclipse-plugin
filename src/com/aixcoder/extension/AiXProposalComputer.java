package com.aixcoder.extension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import com.aixcoder.core.PredictContext;
import com.aixcoder.extension.jobs.AiXFetchJob;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.zipfile.ProjectScan;

/**
 * Extension class to extend org.eclipse.jdt.ui.javaCompletionProposalComputer.
 */
@SuppressWarnings("restriction")
public class AiXProposalComputer extends JavaAllCompletionProposalComputer {
	static long t;

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (Preference.isActive()) {
			t = Calendar.getInstance().getTimeInMillis();
			ProposalFactory proposalFactory = new ProposalFactory(context);
			try {
				// step 1: get text before cursor
				int offset = context.getInvocationOffset();
				IEditorPart editor = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
				IEditorInput input = editor.getEditorInput();
				IFile file = (IFile) input.getAdapter(IFile.class);
				String filePath;
				if (file == null) {
					IFileStore f = input.getAdapter(IFileStore.class);
					filePath = f.toURI().getPath();
				} else {
					filePath = file.getFullPath().toString();
				}
				IProject project = (IProject) input.getAdapter(IProject.class);
				if (project == null) {
					IResource resource = (IResource) input.getAdapter(IResource.class);
					if (resource != null) {
						project = resource.getProject();
					}
				}
				if (project != null) {
					ProjectScan.getInstance(project).start();
				}

				IDocument document = context.getDocument();
				String prefix = document.get(0, offset);
				IRegion line = document.getLineInformationOfOffset(offset);
				String remainingText = document.get(offset, line.getOffset() + line.getLength() - offset);
//				System.out.println("computeCompletionProposals + " + prefix.substring(Math.max(0, prefix.length() - 50)));
//				System.out.println("==============");
				// step 2: send request
				// Eclipse's way of using its thread pool
				String projName = project == null ? "tmp" : project.getName();
				new AiXFetchJob(new PredictContext(prefix, projName, filePath), remainingText,
						proposalFactory).schedule();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		List<ICompletionProposal> superProposals = super.computeCompletionProposals(context, monitor);
		return new ArrayList<ICompletionProposal>();
	}
}
