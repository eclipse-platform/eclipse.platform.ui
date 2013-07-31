/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Drop down action in the console to select the console to display.
 */
class ConsoleDropDownAction extends Action implements IMenuCreator, IConsoleListener, IUpdate {

	private IConsoleView fView;
	private Menu fMenu;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	@Override
	public void update() {
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		setEnabled(consoles.length > 1);
	}

	public ConsoleDropDownAction(IConsoleView view) {
		fView= view;
		setText(ConsoleMessages.ConsoleDropDownAction_0);
		setToolTipText(ConsoleMessages.ConsoleDropDownAction_1);
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IConsoleConstants.IMG_VIEW_CONSOLE));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CONSOLE_DISPLAY_CONSOLE_ACTION);
		setMenuCreator(this);
		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(this);
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	@Override
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}

		fView= null;
		ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}

		fMenu= new Menu(parent);
		IConsole[] consoles= ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		IConsole current = fView.getConsole();
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			Action action = new ShowConsoleAction(fView, console);
			action.setChecked(console.equals(current));
			addActionToMenu(fMenu, action, i + 1);
		}
		return fMenu;
	}

	private void addActionToMenu(Menu parent, Action action, int accelerator) {
	    if (accelerator < 10) {
		    StringBuffer label= new StringBuffer();
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
        ConsoleView consoleView = (ConsoleView) fView;
        boolean pinned = consoleView.isPinned();
        try {
	        if (pinned) {
	            consoleView.setPinned(false);
	        }
	        IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
			IConsole current = fView.getConsole();
			int idx = 0;
			for (int i = 0; i < consoles.length; i++) {
				idx = i;
				if(consoles[i] == current) {
					break;
				}
			}
			int next = idx+1;
        	if(next >= consoles.length) {
        		next = 0;
        	}
        	fView.display(consoles[next]);
        }
        finally {
	        if (pinned) {
	            consoleView.setPinned(true);
	        }
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleListener#consolesAdded(org.eclipse.ui.console.IConsole[])
	 */
	@Override
	public void consolesAdded(IConsole[] consoles) {
		UIJob job = new UIJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				update();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	/* (non-Javadoc)
	 *
	 * Dispose the menu when a launch is removed, such that the actions in this
	 * menu do not hang on to associated resources.
	 *
	 * @see org.eclipse.ui.console.IConsoleListener#consolesRemoved(org.eclipse.ui.console.IConsole[])
	 */
	@Override
	public void consolesRemoved(IConsole[] consoles) {
		UIJob job = new UIJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (fMenu != null) {
					fMenu.dispose();
				}
				update();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}
