package org.eclipse.team.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.TeamResourceDecorator;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.views.navigator.ResourceNavigator;

/**
 * This action adds the team decorator to the navigator
 */
public class ToggleNavigatorDecorations implements IViewActionDelegate {

	private IViewPart part;
	
	/*
	 * Method declared on IViewActionDelegate.
	 */
	public void init(IViewPart part) {
		this.part = part;
		if (part instanceof ResourceNavigator) {
			final ResourceNavigator navigator = (ResourceNavigator)part;
			final Shell shell = part.getSite().getShell();
			BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
				public void run() {
					navigator.setLabelDecorator(new TeamResourceDecorator(shell));
				}
			});
		}
	}
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		if (part instanceof ResourceNavigator) {
			final ResourceNavigator navigator = (ResourceNavigator)part;
			if (action.isChecked()) {
				final Shell shell = part.getSite().getShell();
				BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
					public void run() {
						navigator.setLabelDecorator(new TeamResourceDecorator(shell));
					}
				});
			} else {
				navigator.setLabelDecorator(null);
			}
		}
	}
	/*
	 * Method declared on IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
