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
package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * Provides context for extensions including a valid shell, a selection
 * provider, and a unique identifer corresponding to the abstract viewer behind
 * the viewer site.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.2
 */
public interface ICommonViewerSite extends IAdaptable {

	/**
	 * 
	 * @return The unique identifier associated with the defined abstract
	 *         viewer. In general, this will be the id of the
	 *         <b>org.eclipse.ui.views</b> extension that defines the view
	 *         part.
	 */
	String getId();


	/**
	 * 
	 * @return A valid shell corresponding to the shell of the
	 *         {@link CommonViewer}
	 */
	Shell getShell();

	/**
	 * 
	 * @return The selection provider that can provide a current, valid
	 *         selection. The default selection provider is the
	 *         {@link CommonViewer}.
	 */
	ISelectionProvider getSelectionProvider();

	/**
	 * Sets the selection provider for this common viewer site.
	 * 
	 * @param provider
	 *            the selection provider, or <code>null</code> to clear it
	 */
	public void setSelectionProvider(ISelectionProvider provider);
	
}
