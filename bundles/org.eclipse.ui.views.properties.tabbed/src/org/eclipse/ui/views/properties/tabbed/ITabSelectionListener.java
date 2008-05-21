/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed;

/**
 * A listener interested in tab selection events that occur for the tabbed
 * property sheet page.
 * 
 * @author Anthony Hunter 
 */
public interface ITabSelectionListener {

	/**
	 * Notifies this listener that the selected tab has changed.
	 * 
	 * @param tabDescriptor
	 *            the selected tab descriptor.
	 * @since 3.4
	 */
	public void tabSelected(ITabDescriptor tabDescriptor);
}
