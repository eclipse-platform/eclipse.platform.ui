/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.dialogs;

/**
 * Parts which need to provide a particular context to a Show In...
 * target can provide this interface.
 * The part can either directly implement this interface, or provide it
 * via <code>IAdaptable.getAdapter(IShowInSource.class)</code>.
 * 
 * @see IShowInTarget
 * 
 * @deprecated moved to org.eclipse.ui.part
 */
public interface IShowInSource {
	/**
	 * Returns the context to show, or <code>null</code> if there is 
	 * currently no valid context to show.
	 * 
	 * @return the context to show, or <code>null</code>
	 */
    public ShowInContext getShowInContext();

}
