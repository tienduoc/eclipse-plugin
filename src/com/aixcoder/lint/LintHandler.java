package com.aixcoder.lint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("restriction")
public class LintHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof ViewPart) {
			if (selection instanceof IStructuredSelection) {
				try {
					handlerForMutiFiles((IStructuredSelection) selection);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (part instanceof EditorPart) {
			IEditorInput editorInput = HandlerUtil.getActiveEditorInput(event);
			if (editorInput instanceof IFileEditorInput) {
				Set<IResource> set = new HashSet<IResource>();
				set.add(((IFileEditorInput) editorInput).getFile());
				handlerIfile(set);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void handlerForMutiFiles(IStructuredSelection selection) throws PartInitException {
		final Set<IResource> resources = new LinkedHashSet<IResource>();
		for (Object it: selection.toList()) {
			if (null != it) {

				if (it instanceof IWorkingSet) {
					resources.add(((IWorkingSet) it).getAdapter(IResource.class));
				}
				if (it instanceof IAdaptable) {
					if (null != ((IAdaptable) it).getAdapter(IResource.class)) {
						resources.add(((IAdaptable) it).getAdapter(IResource.class));
					}
				}
			}
		}
		handlerIfile(resources);
	}

	public void handlerIfile(final Set<IResource> resources) {
		new Job("aiXcoder: lint") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final FileCollectVisitor fileVisitor = new FileCollectVisitor();
				for (IResource it : resources) {
					if (it.isAccessible()) {
						try {
							it.accept(fileVisitor);
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				for (IFile ifile : fileVisitor.getFileSet()) {
					lintSingleFile(ifile);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
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

	class FileCollectVisitor implements IResourceVisitor {
		private final LinkedHashSet<IFile> fileSet = new LinkedHashSet<IFile>();

		public LinkedHashSet<IFile> getFileSet() {
			return fileSet;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (null == resource) {
				return false;
			}
			IFile file = resource.getAdapter(IFile.class);
			if (null == file) {
				return true;
			} else if (file.exists() && file.getFileExtension().equals("java")
					|| file.getFileExtension().equals("vm")) {
				fileSet.add(file);
			}
			return false;
		}

	}
}
