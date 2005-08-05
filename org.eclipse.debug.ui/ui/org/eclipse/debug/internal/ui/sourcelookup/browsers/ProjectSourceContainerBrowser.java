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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.internal.ui.sourcelookup.BasicContainerContentProvider;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIMessages;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The browser for creating project source containers.
 * 
 * @since 3.0
 */
public class ProjectSourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell,org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		Object input = ResourcesPlugin.getWorkspace().getRoot();
		IStructuredContentProvider contentProvider=new BasicContainerContentProvider();
		ILabelProvider labelProvider = new WorkbenchLabelProvider();
		Dialog dialog = new ProjectSourceContainerDialog(shell,input, contentProvider, labelProvider,
				SourceLookupUIMessages.projectSelection_chooseLabel); 
		if(dialog.open() == Window.OK){		
			Object[] elements= ((ListSelectionDialog)dialog).getResult();
			ArrayList res= new ArrayList();
			for (int i= 0; i < elements.length; i++) {
				if(!(elements[i] instanceof IProject))
					continue;				
				res.add(new ProjectSourceContainer((IProject)elements[i], ((ProjectSourceContainerDialog)dialog).isAddRequiredProjects()));				
			}
			return (ISourceContainer[])res.toArray(new ISourceContainer[res.size()]);	
		}	
		return new ISourceContainer[0];
	}
	
}
