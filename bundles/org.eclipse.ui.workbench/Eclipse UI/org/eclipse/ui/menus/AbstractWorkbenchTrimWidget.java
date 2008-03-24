/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.menus;

import org.eclipse.jface.menus.AbstractTrimWidget;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Extension for trim widgets contributed to the workbench.
 * The extension point handler will call the <code>init</code>
 * method to inform the contributed widgets as to which
 * workbench window they're currently being hosted in.
 * 
 * @since 3.2
 *
 */
public abstract class AbstractWorkbenchTrimWidget extends AbstractTrimWidget implements IWorkbenchWidget {
	
	private IWorkbenchWindow wbWindow;

	/**
	 * 
	 */
	public AbstractWorkbenchTrimWidget() {
		super();
	}
	

	/**
	 * Define the IWorkbenchWindow that this trim is being hosted in.
	 * Note that subclasses may extend but should not override. The
	 * base implementation caches the value for access through the
	 * <code>getWorkbenchWindow</code> method.
	 * 
	 * @see org.eclipse.ui.menus.IWorkbenchWidget#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow workbenchWindow) {
		wbWindow = workbenchWindow;
	}
	
	/**
	 * Convenience method to get the IWorkbenchWindow that is
	 * hosting this widget.
	 * 
	 * @return The IWorkbenchWindow hosting this widget.
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return wbWindow;
	}
	
	/**
	 * @return The preferred size of this item
	 * @since 3.3
	 */
	public Point getPreferredSize() {
		return null;
	}
}
