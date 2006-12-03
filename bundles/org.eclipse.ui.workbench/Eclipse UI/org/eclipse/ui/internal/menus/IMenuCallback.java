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

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This is a callback interface that can be found on appropriate widgets in SWT
 * Events.
 * 
 * @since 3.3
 * 
 */
public interface IMenuCallback {
	/**
	 * Used to retrieve this instance. ex:
	 * <code>(IMenuData) event.widget.getData(IMenuData.CALLBACK)</code>.
	 */
	public static final String CALLBACK = "IMenuData.callback"; //$NON-NLS-1$

	public void setLabel(String text);

	public void setTooltip(String text);

	public void setIcon(ImageDescriptor icon);
}
