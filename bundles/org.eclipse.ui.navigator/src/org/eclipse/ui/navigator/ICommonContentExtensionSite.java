/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.ui.IMemento;

/**
 * 
 * Provides initialization data for a content extension. Supplied in the
 * <code>init()</code> methods of various interfaces allowed by the framework.
 * 
 * @since 3.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @see ICommonLabelProvider
 * @see ICommonContentProvider
 * 
 */
public interface ICommonContentExtensionSite {

	/**
	 * The extension state model allows an extension to isolate all of the
	 * dynamic state information that affects how it presents content or
	 * displays actions. Clients may use this state model to drive values from
	 * actions that will cause label or content providers to change their
	 * behavior.
	 * 
	 * @return The state model associated with this logical extension.
	 */
	IExtensionStateModel getExtensionStateModel();

	/**
	 * Advanced extensions may expose user-customizeable properties that affect
	 * the structure or behavior of the extension. Clients may use the given
	 * memento to restore or persist these settings between sessions.
	 * 
	 * @return A memento which can be used to restore or persist settings
	 *         between workbench sessions.
	 */
	IMemento getMemento(); 
  
	/**
	 * 
	 * @return The extension instance. Clients may use their extension
	 *         instance to get access to other components defined by the logical
	 *         extension.
	 */
	INavigatorContentExtension getExtension();
	
	/**
	 * 
	 * @return The content service associated with this extension site.
	 * @since 3.3
	 */
	INavigatorContentService getService();
}
