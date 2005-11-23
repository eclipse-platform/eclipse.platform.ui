/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup.browsers;

import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The browser for adding a folder source container.
 * 
 * @since 3.0
 */
public class FolderSourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		FolderSourceContainerDialog dialog = new FolderSourceContainerDialog(shell,  new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		
		if (dialog.open() == Window.OK) {
			Object[] selection= ((ElementTreeSelectionDialog)dialog).getResult();
			ArrayList containers = new ArrayList();			
			for (int i= 0; i < selection.length; i++) {
				if(selection[i] instanceof IFolder) {
					containers.add(new FolderSourceContainer((IFolder)selection[i], dialog.isSearchSubfolders()));
				}
			}
			return (ISourceContainer[])containers.toArray(new ISourceContainer[containers.size()]);	
		}			
		return new ISourceContainer[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return containers.length == 1 && containers[0].getType().getId().equals(FolderSourceContainer.TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		FolderSourceContainerDialog dialog = new FolderSourceContainerDialog(shell,  new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		FolderSourceContainer container = (FolderSourceContainer) containers[0];
		dialog.setSearchSubfolders(container.isComposite());
		dialog.setInitialSelection(container.getContainer());
		if (dialog.open() == Window.OK) {
			container.dispose();
			Object[] selection= ((ElementTreeSelectionDialog)dialog).getResult();
			ArrayList list = new ArrayList();			
			for (int i= 0; i < selection.length; i++) {
				if(selection[i] instanceof IFolder) {
					list.add(new FolderSourceContainer((IFolder)selection[i], dialog.isSearchSubfolders()));
				}
			}
			return (ISourceContainer[])list.toArray(new ISourceContainer[list.size()]);	
		}			
		return new ISourceContainer[0];
	}
	
	
	
}
