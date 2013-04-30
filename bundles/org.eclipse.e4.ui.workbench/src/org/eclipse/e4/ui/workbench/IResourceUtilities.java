/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench;

import org.eclipse.emf.common.util.URI;

/**
 * This interface describes a utility that is used to load ImageDesc's from {@link URI}s
 * 
 * @param <ImageDesc>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.0
 */
public interface IResourceUtilities<ImageDesc> {

	/**
	 * Loads an ImageDesc from the given {@link URI}.
	 * 
	 * @param iconPath
	 * @return the ImageDesc from the given URI or <code>null</code>
	 */
	public ImageDesc imageDescriptorFromURI(URI iconPath);

}