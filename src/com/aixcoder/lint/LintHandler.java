package com.aixcoder.lint;

import java.util.Enumeration;
import java.util.Set;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;

@SuppressWarnings("restriction")
public class LintHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		new Job("aiXcoder: lint") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// TODO Auto-generated method stub
				JavaEditor editor = (JavaEditor) window.getActivePage().getActiveEditor();
				IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
				IPath projPath = file.getProject().getFullPath();
				String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
				Path projFullPath = Paths.get(workspacePath, projPath.toOSString());
				ArrayList<LintResult> results = LinterManager.getInstance().lint(
						projFullPath.toAbsolutePath().toString(),
						file.getFullPath().makeRelativeTo(projPath).toOSString());
				try {
					clearMarker(file);
					for (LintResult r : results) {
						if (r.offset == -1) {
							IDocumentProvider provider = editor.getDocumentProvider();
							IEditorInput input = editor.getEditorInput();
							IDocument document = provider.getDocument(input);
							r.offset = document.getLineOffset(r.beginLine - 1) + r.beginColumn - 1;
							r.length = document.getLineOffset(r.endLine - 1) + r.endColumn - 1 - r.offset;
						}
						if (r.beginLine == -1) {
							IDocumentProvider provider = editor.getDocumentProvider();
							IEditorInput input = editor.getEditorInput();
							IDocument document = provider.getDocument(input);
							r.beginLine = document.getLineOfOffset(r.offset) + 1;
							r.beginColumn = r.offset - document.getLineOffset(r.beginLine - 1) + 1;
							r.endLine = document.getLineOfOffset(r.offset + r.length) + 1;
							r.endColumn = r.offset + r.length - document.getLineOffset(r.endLine - 1) + 1;
						}
						createMarker(file, r.detail, r.severity, r.offset, r.length, r.beginLine, r.beginColumn,
								r.endLine, r.endColumn);
					}
				} catch (CoreException e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
		return null;
	}

	public static void clearMarker(IResource res) throws CoreException {
		IMarker[] markers = res.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		for (IMarker marker : markers) {
			if (marker.getAttribute("aixcoder") == Boolean.TRUE) {
				marker.delete();
			}
		}
	}

	public static IMarker createMarker(IResource res, String detail, Severity severity, int start, int length,
			int beginLine, int beginColumn, int endLine, int endColumn) throws CoreException {
		IMarker marker = null;
		// note: you use the id that is defined in your plugin.xml
		int type = IMarker.SEVERITY_WARNING;
		if (severity == Severity.ERROR) {
			type = IMarker.SEVERITY_ERROR;
		} else if (severity == Severity.INFO) {
			type = IMarker.SEVERITY_INFO;
		}
		marker = res.createMarker(IMarker.PROBLEM);
		marker.setAttribute("aixcoder", true);
		marker.setAttribute(IMarker.SEVERITY, type);
		marker.setAttribute(IMarker.LOCATION,
				"line " + beginLine + ":" + beginColumn + " - line " + endLine + ":" + endColumn);
		marker.setAttribute(IMarker.CHAR_START, start);
		marker.setAttribute(IMarker.CHAR_END, start + length);
		// note: you can also use attributes from your supertype
		marker.setAttribute(IMarker.MESSAGE, detail);
		return marker;
	}
}
