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
 * This interface must be provided by Show In... targets (parts listed
 * in the Show In... prompter).
 * The part can either directly implement this interface, or provide it
 * via <code>IAdaptable.getAdapter(IShowInTarget.class)</code>.
 * 
 * @see org.eclipse.ui.IPageLayout#addShowInPart
 * 
 * @deprecated moved to org.eclipse.ui.part
 */
public interface IShowInTarget extends org.eclipse.ui.part.IShowInTarget {
}
