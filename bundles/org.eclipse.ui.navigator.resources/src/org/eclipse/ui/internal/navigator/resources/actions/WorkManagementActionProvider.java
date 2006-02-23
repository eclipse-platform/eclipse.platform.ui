package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.AddTaskAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * Supports Add Task and Add Bookmark actions.
 * 
 * @since 3.2
 * 
 */
public class WorkManagementActionProvider extends CommonActionProvider {

	private AddTaskAction addTaskAction;

	private AddBookmarkAction addBookmarkAction;

	public void init(ICommonActionExtensionSite aSite) {
		Shell shell = aSite.getViewSite().getShell();
		addBookmarkAction = new AddBookmarkAction(shell);
		addTaskAction = new AddTaskAction(shell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
				addBookmarkAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(),
				addTaskAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#setContext(org.eclipse.ui.actions.ActionContext)
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (context != null && context.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sSel = (IStructuredSelection) context
					.getSelection();
			addBookmarkAction.selectionChanged(sSel);
			addTaskAction.selectionChanged(sSel);
		} else {
			addBookmarkAction.selectionChanged(StructuredSelection.EMPTY);
			addTaskAction.selectionChanged(StructuredSelection.EMPTY);
		}
	}

}
