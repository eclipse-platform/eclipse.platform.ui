/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.core;

import java.net.*;

import org.eclipse.core.runtime.*;

/**
 * Site archive interface.
 * Site archive is a representation of a packaged archive (file) located
 * on an update site. It allows a "symbolic" path used to identify
 * a plug-in or non-plug-in feature entry to be explicitly mapped
 * to a specific URL. 
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.update.core.ArchiveReference
 * @since 2.0
 */
public interface IArchiveReference extends IAdaptable {

	/** 
	 * 
	 * @return the archive "symbolic" path, or <code>null</code>
	 * @since 2.0 
	 */
	public String getPath();

	/**
	 * Retrieve the site archive URL 
	 * 
	 * @return the archive URL, or <code>null</code>
	 * @since 2.0 
	 */
	public URL getURL();
}
