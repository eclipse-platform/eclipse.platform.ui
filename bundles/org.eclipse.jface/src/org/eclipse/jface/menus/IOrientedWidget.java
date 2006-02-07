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

package org.eclipse.jface.menus;

/**
 * This extension to <code>IWidget</code> interface allows clients adding elements to the trim to
 * receive notifications if the User moves the widget to another trim area.
 * 
 * The client is expected to take the appropriate action to format their widget based
 * on the given orientation. Note that in order to avoid having to re-create the widget conrol
 * if it's of a class (i.e. <code>ToolBar</code>) that supports orientation in its style bits
 * it is recommended that such controls be placed in a Composite.  
 * <p>
 * The orientation can be one of:
 * <ul>
 * <li><code>SWT.TOP</code></li>
 * <li><code>SWT.BOTTOM</code></li>
 * <li><code>SWT.LEFT</code></li>
 * <li><code>SWT.RIGHT</code></li>
 * </ul>
 * </p>
 * @since 3.2
 *
 */
public interface IOrientedWidget extends IWidget {
	/**
	 * Informs the client that the widget has moved to a new trim location.
	 * 
	 * The client is expected to take the appropriate action to format their widget based
	 * on the given orientation. Note that in order to avoid having to re-create the widget conrol
	 * if it's of a class (i.e. <code>ToolBar</code>) that supports orientation in its style bits
	 * it is recommended that such controls be placed in a Composite.  
	 * <p>
	 * The orientation can be one of:
	 * <ul>
	 * <li><code>SWT.TOP</code></li>
	 * <li><code>SWT.BOTTOM</code></li>
	 * <li><code>SWT.LEFT</code></li>
	 * <li><code>SWT.RIGHT</code></li>
	 * </ul>
	 * </p><p>
	 * @param oldOrientation
	 * @param newOrientation
	 * </p>
	 * @since 3.2
	 */
	public void orientationChanged(int oldOrientation, int newOrientation);
}
