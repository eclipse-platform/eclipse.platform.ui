/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * This contribution item contains the standard set of items that
 * a StackPresentation will contribute to the system menu.
 * 
 * @since 3.0
 */
public class StandardSystemContribution extends ContributionItem {
	
	private IStackPresentationSite site;
	private IPresentablePart currentPart;
	
	public StandardSystemContribution(IStackPresentationSite site) {
		this.site = site;
	}

	/**
	 * Returns the site.
	 */
	protected IStackPresentationSite getSite() {
	    return site;
	}
	
	/**
	 * Returns the current part, or <code>null</code> if not set.
	 */
	protected IPresentablePart getPart() {
	    return currentPart;
	}

	/**
	 * @issue Why is this not set in the constructor?  
	 *   Is the receiver somehow shared across parts? 
	 */ 
	public void setPart(IPresentablePart part) {
		currentPart = part;
		IContributionManager parent = getParent();
		
		if (parent != null) {
			parent.markDirty();
		}
	}
	
	public boolean isDynamic() {
		return true;
	}
	
	public void fill(Menu menu, int index) {
		// add view context menu items
		addStateContribution(menu, WorkbenchMessages.getString("PartPane.restore"), IStackPresentationSite.STATE_RESTORED); //$NON-NLS-1$
		addMoveMenuItem(menu, getMovePaneName(), 
				WorkbenchMessages.getString("ViewPane.moveFolder")); //$NON-NLS-1$
		addSizeMenuItem(menu);
		addStateContribution(menu, WorkbenchMessages.getString("ViewPane.minimizeView"), IStackPresentationSite.STATE_MINIMIZED); //$NON-NLS-1$
		addStateContribution(menu, WorkbenchMessages.getString("PartPane.maximize"), IStackPresentationSite.STATE_MAXIMIZED); //$NON-NLS-1$
		addCloseMenuItem(menu);
	}
	
	protected void addStateContribution(Menu menu, String name, final int state) {
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(WorkbenchMessages.getString(name));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				site.setState(state);
			}
		});
		item.setEnabled(site.getState() != state);
	}
	
	protected String getMovePaneName() {
		return WorkbenchMessages.getString("ViewPane.moveView"); //$NON-NLS-1$
	}

	protected void addMoveMenuItem (Menu menu, String movePartName, String moveSiteName) {
		//Add move menu
		MenuItem item = new MenuItem(menu, SWT.CASCADE);
		item.setText(WorkbenchMessages.getString("PartPane.move")); //$NON-NLS-1$
		Menu moveMenu = new Menu(menu);
		item.setMenu(moveMenu);
		addMoveItems(moveMenu, movePartName, moveSiteName);
	}
	
	/**
	 * Add the View and Tab Group items to the Move menu.
	 */
	private void addMoveItems(Menu moveMenu, String movePane, String moveSite) {

		final Display display = moveMenu.getDisplay();
		
		// Add move view only menu item
		MenuItem item = new MenuItem(moveMenu, SWT.NONE);
		
		item.setText(movePane); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				site.dragStart(currentPart, display.getCursorLocation(), true);
			}
		});
		item.setEnabled(currentPart != null && site.isMoveable(currentPart));

		// Add move view's tab folder menu item
		item = new MenuItem(moveMenu, SWT.NONE);
		item.setText(moveSite); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				site.dragStart(display.getCursorLocation(), true);
			}
		});
		item.setEnabled(true);
	}
	
	protected void addSizeMenuItem(Menu menu) {
	    // do nothing
	}
	
	protected void addCloseMenuItem (Menu menu) {
		// add close item
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(WorkbenchMessages.getString("PartPane.close")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				site.close(currentPart);
			}
		});
		
		item.setEnabled(currentPart != null && site.isCloseable(currentPart));
	}
}
