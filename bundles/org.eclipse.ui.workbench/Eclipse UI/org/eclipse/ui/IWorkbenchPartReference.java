/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui;

import org.eclipse.swt.graphics.Image;
/**
 * Implements a reference to a IWorkbenchPart.
 * The IWorkbenchPart will not be instanciated until the part 
 * becomes visible or the API getPart is sent with true;
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IWorkbenchPartReference {
	/**
	 * Returns the IWorkbenchPart referenced by this object.
	 * Returns <code>null</code> if the editors was not instantiated or
	 * it failed to be restored. Tries to restore the editor
	 * if <code>restore</code> is true.
	 */
	public IWorkbenchPart getPart(boolean restore);

	/**
	 * @see IWorkbenchPart#getTitle
	 */	
	public String getTitle();

	/**
	 * @see IWorkbenchPart#getTitleImage
	 */	
	public Image getTitleImage();

	/**
	 * @see IWorkbenchPart#getTitleToolTip
	 */		
	public String getTitleToolTip();

	/**
	 * @see IWorkbenchPartSite#getId
	 */		
	public String getId();

	/**
	 * @see IWorkbenchPart#addPropertyListener
	 */
	public void addPropertyListener(IPropertyListener listener);
	
	/**
	 * @see IWorkbenchPart#removePropertyListener
	 */
	public void removePropertyListener(IPropertyListener listener);
	/**
	 * Returns the workbench page that contains this part
	 */
	public IWorkbenchPage getPage();
}
