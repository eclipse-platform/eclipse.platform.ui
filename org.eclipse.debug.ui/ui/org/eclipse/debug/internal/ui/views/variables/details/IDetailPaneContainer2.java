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
package org.eclipse.debug.internal.ui.views.variables.details;

import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * A detail pane container which allows to set the selection provider of the detail pane.
 *
 * @since 3.5
 */
public interface IDetailPaneContainer2 extends IDetailPaneContainer {

	/**
	 * Set the selection provider of the detail pane.  Allows the container to 
	 * forward the selection provider events to the container's workbench site.
	 * 
	 * @param provider  the selection provider or <code>null</code>
	 */
	public void setSelectionProvider(ISelectionProvider provider);
}
