/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPerspectiveDescriptor;

/**
 * A dynamic contribution item which shows all opened perspectives
 * in the window's active page.
 */
public class OpenedPerspectivesMenu extends ContributionItem {
	private WorkbenchWindow window;
	private boolean showSeparator;

	private static final int MAX_TEXT_LENGTH = 40;

	/**
	 * Create a new instance.
	 */
	public OpenedPerspectivesMenu(WorkbenchWindow window, boolean showSeparator) {
		super("Opened perspectives"); //$NON-NLS-1$
		this.window = window;
		this.showSeparator = showSeparator;
	}
	
	/**
	 * Returns the text for a perspective. This may be truncated to fit
	 * within the MAX_TEXT_LENGTH.
	 */
	private String calcText(int number, IPerspectiveDescriptor persp) {
		StringBuffer sb = new StringBuffer();
		if (number < 10)
			sb.append('&');
		sb.append(number);
		sb.append(' ');
		String suffix = persp.getLabel();
		if (suffix.length() <= MAX_TEXT_LENGTH) {
			sb.append(suffix);
		} else {
			sb.append(suffix.substring(0, MAX_TEXT_LENGTH / 2));
			sb.append("..."); //$NON-NLS-1$
			sb.append(suffix.substring(suffix.length() - MAX_TEXT_LENGTH / 2));
		}
		return sb.toString();
	}

	/**
	 * Fills the given menu with menu items for all opened perspectives.
	 */
	public void fill(Menu menu, int index) {
		final WorkbenchPage page = window.getActiveWorkbenchPage();
		if (page == null)
			return;

		// Add separator.
		if (showSeparator) {
			new MenuItem(menu, SWT.SEPARATOR, index);
			++index;
		}

		// Add one item for each opened perspective.
		IPerspectiveDescriptor activePersp = page.getPerspective();
		IPerspectiveDescriptor descriptors[] = page.getOpenedPerspectives();
		int count = 1;
		for (int i = 0; i < descriptors.length; i++) {
			final IPerspectiveDescriptor desc = (IPerspectiveDescriptor)descriptors[i];
			MenuItem mi = new MenuItem(menu, SWT.RADIO, index);
			mi.setSelection(desc == activePersp);
			mi.setText(calcText(count, desc));
			// avoid hanging onto page or perspective directly in menu
			mi.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					WorkbenchPage page = window.getActiveWorkbenchPage();
					if (page != null) {
						page.setPerspective(desc);
					}
				}
			});
		
			index++;
			count++;
		}
	}
	
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}
}
