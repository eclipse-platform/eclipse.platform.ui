/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.actions;

import org.eclipse.swt.widgets.Menu;

/**
 * Interface representing an object that can contribute to a menu
 */

public interface IMenuContributor {
	/**
	 * Contribute zero or more items to a menu
	 * @param menu The menu to add the contributions
	 * @param index The index before cintributions are added
	 * @return the index after contributions are added
	 */
	public int contributeToViewMenu(Menu menu, int index);
}
