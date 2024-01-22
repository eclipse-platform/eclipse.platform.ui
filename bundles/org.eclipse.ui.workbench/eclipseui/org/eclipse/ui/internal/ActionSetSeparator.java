/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This class represents a pseudo-group defined by an action set. It is not a
 * real group ( aka GroupMarker ) because that would interfere with the
 * pre-existing groups in a menu or toolbar.
 */
public class ActionSetSeparator extends ContributionItem implements IActionSetContributionItem {
	private String actionSetId;

	/**
	 * Constructs a new group marker.
	 */
	public ActionSetSeparator(String groupName, String newActionSetId) {
		super(groupName);
		actionSetId = newActionSetId;
	}

	@Override
	public void fill(Menu menu, int index) {
		if (index >= 0) {
			new MenuItem(menu, SWT.SEPARATOR, index);
		} else {
			new MenuItem(menu, SWT.SEPARATOR);
		}
	}

	@Override
	public void fill(ToolBar toolbar, int index) {
		if (index >= 0) {
			new ToolItem(toolbar, SWT.SEPARATOR, index);
		} else {
			new ToolItem(toolbar, SWT.SEPARATOR);
		}
	}

	/**
	 * Returns the action set id.
	 */
	@Override
	public String getActionSetId() {
		return actionSetId;
	}

	/**
	 * The <code>Separator</code> implementation of this
	 * <code>IContributionItem</code> method returns <code>true</code>
	 */
	@Override
	public boolean isSeparator() {
		return true;
	}

	/**
	 * Sets the action set id.
	 */
	@Override
	public void setActionSetId(String newActionSetId) {
		actionSetId = newActionSetId;
	}
}
