/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;

/**
 * The common interface between the workbench and its parts, including pages within parts.
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * @see IWorkbenchPartSite
 * @see IPageSite
 * @since 2.0
 */
public interface IWorkbenchSite {
/**
 * Returns the page containing this workbench site.
 *
 * @return the page containing this workbench site
 */
public IWorkbenchPage getPage();
/**
 * Returns the selection provider for this workbench site.
 *
 * @return the selection provider, or <code>null</code> if none
 */
public ISelectionProvider getSelectionProvider();
/**
 * Returns the shell for this workbench site.
 *
 * @return the shell for this workbench site
 */
public Shell getShell();
/**
 * Returns the workbench window containing this workbench site.
 *
 * @return the workbench window containing this workbench site
 */
public IWorkbenchWindow getWorkbenchWindow();
/**
 * Sets the selection provider for this workbench site.
 *
 * @param provider the selection provider, or <code>null</code> to clear it
 */
public void setSelectionProvider(ISelectionProvider provider);
}
