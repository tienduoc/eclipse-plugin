package com.aixcoder.extension;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class SearchHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			JavaEditor editor = (JavaEditor) window.getActivePage().getActiveEditor();
			ISourceViewer viewer = editor.getViewer();
			TextSelection selection = (TextSelection) editor.getSite().getSelectionProvider().getSelection();
			String t;
			if (selection.getLength() > 0) {
				t = viewer.getDocument().get(selection.getOffset(), selection.getLength());
			} else {
				t = viewer.getDocument().get();
				int s = selection.getOffset();
				while (s >= 0 && Character.isJavaIdentifierPart(t.charAt(s))) {
					s--;
				}
				int e = selection.getOffset();
				while (e <= t.length() && Character.isJavaIdentifierPart(t.charAt(e))) {
					e++;
				}
				t = t.substring(s + 1, e);
			}
			WebView.init().doSearch(t);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
