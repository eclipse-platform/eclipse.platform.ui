/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ILabelProvider;

/**
 * 
 * Allows extensions to vary their behavior based on properties in the extension
 * model and the given memento.
 * 
 * <p>
 * Clients should refer to the <b>org.eclipse.ui.navigator.navigatorContent</b>
 * extension point for more information on building a content extension.
 * </p>
 * <p>
 * Clients need not implement this interface if there is no cause to do so.
 * {@link org.eclipse.jface.viewers.ILabelProvider} is respected by the Common
 * Navigator.
 * </p>
 * 
 * 
 * @since 3.2
 */
public interface ICommonLabelProvider extends ILabelProvider, IMementoAware,
		IDescriptionProvider {

	/**
	 * Initialize the label provider with the given configuration.
	 * 
	 * @param aConfig
	 *            The extension site provides information that some extensions
	 *            will find useful to configure themselves properly in a
	 *            particular viewer.
	 * 
	 * @see ICommonContentProvider
	 */
	void init(ICommonContentExtensionSite aConfig);

}
