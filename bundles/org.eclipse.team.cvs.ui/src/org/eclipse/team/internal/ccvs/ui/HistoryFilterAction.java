package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class HistoryFilterAction implements IViewActionDelegate {
	private HistoryView view;
	private HistoryFilter filter;
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		this.view = (HistoryView)view;
	}
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		HistoryFilterDialog historyDialog = new HistoryFilterDialog(view);
		if (filter != null) {
			historyDialog.setFilter(filter);
		}
		if (historyDialog.open() == Window.CANCEL) {
			return;
		}
		if (filter != null) {
			view.getViewer().removeFilter(filter);
		}
		filter = historyDialog.getFilter();
		//don't add the filter if it is blank
		if (!(filter.hasAuthor() || filter.hasDate() || filter.hasComment())) {
			return;
		}
		view.getViewer().addFilter(filter);
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}