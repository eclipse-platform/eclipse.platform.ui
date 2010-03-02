/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.jface.viewers.ILabelProvider;

/**
 * Label provider for menu / toolbar items
 * 
 * @since 3.3
 *
 */
public interface IMenuLabelProvider extends ILabelProvider {
	/**
	 * @return The Tooltip to use for this menu item
	 */
	public String getToolTip();
}
