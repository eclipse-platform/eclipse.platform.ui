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
	 * 
	 * @return the location of the given object or <code>null</code>
	 */
	IPath getPath(Object element);
}
