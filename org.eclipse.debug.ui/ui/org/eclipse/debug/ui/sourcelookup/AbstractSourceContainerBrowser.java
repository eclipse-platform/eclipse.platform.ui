/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.sourcelookup;

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.swt.widgets.Shell;

/**
 * Common implementation for source container browsers.
 * <p>
 * Clients implementing <code>ISourceContainerBrowser</code> should
 * subclass this class.
 * </p>
 * @since 3.0
 */
public class AbstractSourceContainerBrowser implements ISourceContainerBrowser {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#addSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		return new ISourceContainer[0];
	}
	/* (non-Javadoc)
	 * 
	 * Generally, a source container browser can add source containers. Subclasses
	 * should override as required.
	 * 
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#canAddSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public boolean canAddSourceContainers(ISourceLookupDirector director) {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		return new ISourceContainer[0];
	}
	/* (non-Javadoc)
	 * 
	 * Not all source containers can be edited. Subclasses should override
	 * as required.
	 * 
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return false;
	}
}
