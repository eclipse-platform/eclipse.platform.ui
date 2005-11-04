/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Bug 114664
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup.browsers;

import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * The browser for adding an external folder source container.
 * @since 3.0
 */
public class DirectorySourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		ISourceContainer[] containers = new ISourceContainer[1];
		DirectorySourceContainerDialog dialog = new DirectorySourceContainerDialog(shell);
		if (dialog.open() == Window.OK) {
			String directory = dialog.getDirectory();
			if(directory !=null) {
				containers[0] = new DirectorySourceContainer(new Path(directory), dialog.isSearchSubfolders());			
				return containers;			
			}
		}		
		return new ISourceContainer[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return containers.length == 1 && DirectorySourceContainer.TYPE_ID.equals(containers[0].getType().getId());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		if (containers.length == 1 && DirectorySourceContainer.TYPE_ID.equals(containers[0].getType().getId()) ) {
			DirectorySourceContainer c = (DirectorySourceContainer)containers[0];
			DirectorySourceContainerDialog dialog = new DirectorySourceContainerDialog(shell, c.getDirectory().getPath(), c.isComposite());
			if (dialog.open() == Window.OK) {
				String directory = dialog.getDirectory();
				if(directory !=null) {
					containers[0].dispose();
					return new ISourceContainer[]{ new DirectorySourceContainer(new Path(directory), dialog.isSearchSubfolders())};			
				}
			}
		}
		return containers;
	}
	
}
