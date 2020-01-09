package com.aixcoder.utils;

import static com.aixcoder.i18n.Localization.R;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.progress.UIJob;

import com.aixcoder.utils.shims.Consumer;

public class PromptUtils {
	public static void promptQuestion(String jobName, final String title, final String message,
			final Consumer<Boolean> action) {
		new UIJob(jobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				boolean r = MessageDialog.openQuestion(null, R(title), R(message));
				action.apply(r);
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	public static void promptYesNoQuestion(String jobName, final String title, final String message,
			final Consumer<Boolean> action) {
		new UIJob(jobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialog dialog = new MessageDialog(null, R(title), null, R(message), MessageDialog.QUESTION,
						new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
				int choice = dialog.open();
				action.apply(choice == 0 ? true : choice == 1 ? false : null);
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	public static void promptYesNOError(String jobName, final String title, final String message,
			final Consumer<Boolean> action) {
		new UIJob(jobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialog dialog = new MessageDialog(null, R(title), null, R(message), MessageDialog.ERROR,
						new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
				int choice = dialog.open();
				action.apply(choice == 0 ? true : choice == 1 ? false : null);
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	public static void promptQuestion(String jobName, final String title, final String message, final String[] choices,
			final Consumer<String> action) {
		final ArrayList<String> dispalyLabels = new ArrayList<String>();
		for (String c : choices) {
			dispalyLabels.add(R(c));
		}
		new UIJob(jobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialog dialog = new MessageDialog(null, R(title), null, R(message), MessageDialog.QUESTION,
						dispalyLabels.toArray(new String[0]), 0);
				int choice = dialog.open();
				if (choice >= 0 && choice < choices.length) {
					action.apply(choices[choice]);
				} else {
					action.apply(null);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	public static void promptMessage(String jobName, final String title, final String message) {

		new UIJob(jobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialog.openInformation(null, R(title), R(message));
				return Status.OK_STATUS;
			}
		}.schedule();
	}

}
