/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import org.eclipse.core.runtime.IPath;


/**
 * This class gets the location for a given
 * object.
 *
 * @since 3.0
 */
public interface ILocationProvider {

	/**
	 * Returns the location of the given object or <code>null</code>.
	 * <p>
	 * The provided location is either a full path of a workspace resource or
	 * an absolute path in the local file system.
	 * </p>
	 *
	 * @param element the object for which to get the location
	 * @return the location of the given object or <code>null</code>
	 */
	IPath getPath(Object element);
}
