/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.*;

/**
 * The <code>NavigationHistoryAction</code> moves navigation history 
 * back and forward.
 */
public class NavigationHistoryAction extends PageEventAction implements IWorkbenchWindowPulldownDelegate2, IMenuCreator {
	private boolean forward;
	private Menu historyMenu;
	private int MAX_HISTORY_LENGTH = 9;
	
	/**
	 * Create a new instance of <code>NavigationHistoryAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 * @param forward if this action should move history forward of backward
	 */
	public NavigationHistoryAction(IWorkbenchWindow window,boolean forward) {
		super("",window); //$NON-NLS-1$
		if(forward) {
			setText(WorkbenchMessages.getString("NavigationHistoryAction.forward.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.forward.toolTip")); //$NON-NLS-1$
		} else {
			setText(WorkbenchMessages.getString("NavigationHistoryAction.backward.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.backward.toolTip")); //$NON-NLS-1$
		}
		// WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_PAGES_ACTION);
		setEnabled(false);
		this.forward = forward;
		setMenuCreator(this);
	}
	/* (non-Javadoc)
	 * Method declared on PageEventAction.
	 */		
	public void pageClosed(IWorkbenchPage page) {
		super.pageClosed(page);
		setEnabled(false);
	}
	public void dispose() {
		if (historyMenu != null) {
			for (int i=0; i<historyMenu.getItemCount(); i++) {
				MenuItem menuItem = historyMenu.getItem(i);
				menuItem.setData(null);
			}
			historyMenu.dispose();
			historyMenu = null;
		}
	}
	public Menu getMenu(Menu parent) {
		return null;
	}
	public Menu getMenu(Control parent) {
		dispose();
		historyMenu = new Menu(parent);
		IWorkbenchPage page = getWorkbenchWindow().getActivePage();
		if(page == null)
			return historyMenu;

		final NavigationHistory history = (NavigationHistory)getWorkbenchWindow().getActivePage().getNavigationHistory();
		NavigationHistoryEntry[] entries;
		if (forward) entries = history.getForwardEntries();
		else entries = history.getBackwardEntries();
		int entriesCount[] = new int[entries.length];
		for (int i = 0; i < entriesCount.length; i++)
			entriesCount[i] = 1;
		entries = colapseEntries(entries,entriesCount);
		for (int i=0; i<entries.length; i++) {
			if (i > MAX_HISTORY_LENGTH) break;
			String text = entries[i].getHistoryText();
			if (text != null) {
				MenuItem item = new MenuItem(historyMenu, SWT.NONE);
				item.setData(entries[i]);
				if(entriesCount[i] > 1)
					text = WorkbenchMessages.format("NavigationHistoryAction.locations", new String[] {text,new Integer(entriesCount[i]).toString()}); //$NON-NLS-1$
				item.setText(text);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						history.shiftCurrentEntry((NavigationHistoryEntry)e.widget.getData());
					}
				});
			}
		}
		return historyMenu;
	}
	
	private NavigationHistoryEntry[] colapseEntries(NavigationHistoryEntry[] entries,int entriesCount[]) {
		ArrayList allEntries = new ArrayList(Arrays.asList(entries));
		NavigationHistoryEntry priviousEntry = null;
		int i = -1;
		for (Iterator iter = allEntries.iterator(); iter.hasNext();) {
			NavigationHistoryEntry entry = (NavigationHistoryEntry) iter.next();
			if(priviousEntry != null) {
				String text = priviousEntry.getHistoryText();
				if(text != null) {
					if(text.equals(entry.getHistoryText()) && priviousEntry.editorInfo == entry.editorInfo) {
						iter.remove();
						entriesCount[i]++;
						continue;
					}
				}
			}
			priviousEntry = entry;
			i++;
		}
		entries = new NavigationHistoryEntry[allEntries.size()];
		return (NavigationHistoryEntry[])allEntries.toArray(entries);
	}
	
	public void init(IWorkbenchWindow window){
	}
	/* (non-Javadoc)
	 * Method declared on PageEventAction.
	 */	
	public void pageActivated(IWorkbenchPage page) {
		super.pageActivated(page);
		NavigationHistory nh = (NavigationHistory)page.getNavigationHistory();
		if(forward)
			nh.setForwardAction(this);
		else
			nh.setBackwardAction(this);
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage)getActivePage();
		if (page != null) {
			NavigationHistory nh = (NavigationHistory)page.getNavigationHistory();
			if(forward)
				nh.forward();
			else
				nh.backward();
		}
	}
	public void run(IAction action) {
	}
	public void selectionChanged(IAction action, ISelection selection) {
	}
	public void update() {
		// Set the enabled state of the action and set the tool tip text.  The tool tip
		// text is set to reflect the item that one will move back/forward to.
		WorkbenchPage page = (WorkbenchPage)getActivePage();
		if (page == null) return;
		NavigationHistory history = (NavigationHistory)page.getNavigationHistory();
		NavigationHistoryEntry[] entries;
		if (forward) {
			setEnabled(history.canForward());
			entries = history.getForwardEntries();
			if (entries.length > 0) {
				NavigationHistoryEntry entry = entries[0];
				String text = WorkbenchMessages.format("NavigationHistoryAction.forward.toolTipName", new String[] {entry.getHistoryText()}); //$NON-NLS-1$
				setToolTipText(text);
			} else {
				setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.forward.toolTip")); //$NON-NLS-1$
			}
		}
		else {
			setEnabled(history.canBackward());
			entries = history.getBackwardEntries();
			if (entries.length > 0) {
				NavigationHistoryEntry entry = entries[0];
				String text = WorkbenchMessages.format("NavigationHistoryAction.backward.toolTipName", new String[] {entry.getHistoryText()}); //$NON-NLS-1$
				setToolTipText(text);
			} else {
				setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.backward.toolTip")); //$NON-NLS-1$
			}
		}
	}	
}