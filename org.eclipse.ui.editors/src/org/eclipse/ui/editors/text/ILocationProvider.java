/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.editors.text;

import org.eclipse.core.runtime.IPath;

/**
 * @since 3.0
 */
public interface ILocationProvider {
	
	/**
	 * Returns the location of the given object or <code>null</code>.
	 * <p>
	 * The provided location must fulfill the following requirements:
	 * </p>
	 * <ul>
	 * <li>It is given as an absolute path in the local file system.</li>
	 * <li>If the element is a workspace resource, the location describes the
	 * location inside the workspace. I.e. for linked resources the location is
	 * the unresolved location and the not the resolved location to the lnked
	 * resource's local content.</li>
	 * </ul>
	 * 
	 * @return the location of the given object or <code>null</code>
	 */
	IPath getPath(Object element);
}
