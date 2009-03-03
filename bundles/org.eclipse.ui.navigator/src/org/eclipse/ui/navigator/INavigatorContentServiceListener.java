/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;



/**
 * <p>
 * Used by clients who would like to listen for the load event of am
 * {@link INavigatorContentExtension}.
 * 
 * @since 3.2
 */
public interface INavigatorContentServiceListener {

	
	/**
	 * Notifies the listener that the given extension
	 * has now been loaded. Extensions are loaded as 
	 * needed as the user expands nodes in the tree viewer. 
	 * 
	 * @param anExtension The extension that was loaded.
	 */
	void onLoad(INavigatorContentExtension anExtension);
}
