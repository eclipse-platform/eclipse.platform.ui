package org.eclipse.update.internal.ui.forms;

import org.eclipse.update.configuration.*;
import org.eclipse.update.configuration.IProblemHandler;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

/**
 *
 */
public class UIProblemHandler implements IProblemHandler {
	private static final String KEY_TITLE = "Revert.ProblemDialog.title";

	/*
	 * @see IProblemHandler#reportProblem(String)
	 */
	public boolean reportProblem(String problemText) {
		String title = UpdateUIPlugin.getResourceString(KEY_TITLE);
		return MessageDialog.openQuestion(UpdateUIPlugin.getActiveWorkbenchShell(), title, problemText);
	}
	/*
	 * @see IProblemHandler#reportProblem(String, MultiStatus)
	 */
	public boolean reportProblem(String problemText, MultiStatus status) {
		String title = UpdateUIPlugin.getResourceString(KEY_TITLE);
		return MessageDialog.openQuestion(UpdateUIPlugin.getActiveWorkbenchShell(), title, problemText);
	}

}
