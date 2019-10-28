package com.aixcoder.lint;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

@SuppressWarnings("restriction")
public class LintHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {
			ISelectionService service = window.getSelectionService();
			// set structured selection
			IStructuredSelection structured = (IStructuredSelection) service.getSelection();

			if (structured.getFirstElement() instanceof ICompilationUnit) {
				// check if it is an ICompilationUnit
				ICompilationUnit cu = (ICompilationUnit) structured.getFirstElement();
				final IFile file = (IFile) cu.getResource();
				new Job("aiXcoder: lint") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						lintSingleFile(file);
						return Status.OK_STATUS;
					}
				}.schedule();
			} else if (structured.getFirstElement() instanceof JavaElement) {
				// check if it is an IFile
				// get the selected file
				final JavaElement pkg = (JavaElement) structured.getFirstElement();
				new UIJob("aiXcoder: lint - list files") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						final ArrayList<IFile> files = lintSinglePackage(pkg);
						new Job("aiXcoder: lint") {

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								for (IFile f : files) {
									lintSingleFile(f);
								}
								return null;
							}

						}.schedule();
						return Status.OK_STATUS;
					}
				}.schedule();
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			new Job("aiXcoder: lint") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					JavaEditor editor = (JavaEditor) window.getActivePage().getActiveEditor();
					IFile file = editor.getEditorInput().getAdapter(IFile.class);;
					lintSingleFile(file);
					return Status.OK_STATUS;
				}
			}.schedule();
		}
		return null;
	}

	public ArrayList<IFile> lintSinglePackage(final JavaElement pkg) {
		ArrayList<IFile> files = new ArrayList<IFile>();
		try {
			IJavaElement[] children = pkg.getChildren();
			for (int i = 0; i < children.length; i++) {
				IJavaElement child = children[i];
				if (child instanceof ICompilationUnit) {
					ICompilationUnit cu = (ICompilationUnit) child;
					final IFile file = (IFile) cu.getResource();
					files.add(file);
				} else if (child instanceof JavaElement) {
					files.addAll(lintSinglePackage((JavaElement) child));
				}
			}
			return files;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return files;
	}

	public void lintSingleFile(IFile file) {
		try {
			IPath projPath = file.getProject().getLocation();
			String projectPath = projPath.toOSString();
			String filePath = file.getFullPath().makeRelativeTo(file.getProject().getFullPath()).toOSString();
			ArrayList<LintResult> results = LinterManager.getInstance().lint(projectPath, filePath);
			clearMarker(file);
			for (LintResult r : results) {
				if (r.offset == -1) {
					LineHelper document = new LineHelper(projectPath, filePath);
					int lineOffset = document.getLineOffset(r.beginLine - 1);
					int lineEnd = lineOffset + document.getLineLength(r.beginLine - 1);
					r.offset = lineOffset + r.beginColumn - 1;
					if (r.offset >= lineEnd) {
						r.offset = lineOffset;
						r.length = lineEnd - lineOffset;
					} else {
						r.length = document.getLineOffset(r.endLine - 1) + r.endColumn - 1 - r.offset;
						if (r.length > lineEnd - r.offset) {
							r.length = lineEnd - r.offset;
						}
					}
				}
				if (r.beginLine == -1) {
					LineHelper document = new LineHelper(projectPath, filePath);
					r.beginLine = document.getLineOfOffset(r.offset) + 1;
					r.beginColumn = r.offset - document.getLineOffset(r.beginLine - 1) + 1;
					r.endLine = document.getLineOfOffset(r.offset + r.length) + 1;
					r.endColumn = r.offset + r.length - document.getLineOffset(r.endLine - 1) + 1;
				}
				createMarker(file, r.detail, r.severity, r.offset, r.length, r.beginLine, r.beginColumn, r.endLine,
						r.endColumn);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
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
