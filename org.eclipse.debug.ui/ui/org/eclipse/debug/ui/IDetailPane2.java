/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * An extension to the detail pane interface which allows implementors to
 * provide a selection provider instead of setting the selection provider of the
 * view's {@link org.eclipse.ui.IWorkbenchPartSite site} directly.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.5
 */
public interface IDetailPane2 extends IDetailPane {

	/**
	 * Request the selection provider of this detail pane.
	 * <p>
	 * This method is called on every focus change of the <code>Control</code>
	 * returned by {@link #createControl(org.eclipse.swt.widgets.Composite)
	 * createControl(Composite)}.
	 * </p>
	 * 
	 * @return the selection provider of this detail pane or <code>null</code>
	 */
	public ISelectionProvider getSelectionProvider();
}
