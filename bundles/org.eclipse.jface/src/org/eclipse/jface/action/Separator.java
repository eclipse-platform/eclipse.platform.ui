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
package org.eclipse.jface.action;

import org.eclipse.pde.api.tools.annotations.NoExtend;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A separator is a special kind of contribution item which acts
 * as a visual separator and, optionally, acts as a group marker.
 * Unlike group markers, separators do have a visual representation for
 * menus and toolbars.
 * <p>
 * This class may be instantiated; it is not intended to be
 * subclassed outside the framework.
 * </p>
 */
@NoExtend
public class Separator extends AbstractGroupMarker {
	/**
	 * Creates a separator which does not start a new group.
	 */
	public Separator() {
		super();
	}

	/**
	 * Creates a new separator which also defines a new group having the given group name.
	 * The group name must not be <code>null</code> or the empty string.
	 * The group name is also used as the item id.
	 *
	 * @param groupName the group name of the separator
	 */
	public Separator(String groupName) {
		super(groupName);
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
	 * The <code>Separator</code> implementation of this <code>IContributionItem</code>
	 * method returns <code>true</code>
	 */
	@Override
	public boolean isSeparator() {
		return true;
	}
}
