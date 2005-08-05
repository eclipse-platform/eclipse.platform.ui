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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIMessages;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * The dialog for selecting the folder for which a source container will be created.
 * 
 * @since 3.0
 */
public class FolderSourceContainerDialog extends ElementTreeSelectionDialog { 
	
	public FolderSourceContainerDialog(
			Shell parent,
			ILabelProvider labelProvider,
			ITreeContentProvider contentProvider) {
		super(parent, labelProvider, contentProvider);
		setTitle(SourceLookupUIMessages.folderSelection_title);	//	
		setInput(ResourcesPlugin.getWorkspace().getRoot());
		setSorter(new ResourceSorter(ResourceSorter.NAME));
		ISelectionStatusValidator validator= new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				for (int i= 0; i < selection.length; i++) {
					if (!(selection[i] instanceof IFolder)) {
						return new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), -1, SourceLookupUIMessages.sourceSearch_folderSelectionError, null); // 
					} 				
				}
				return new Status(IStatus.OK, DebugUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
			}			
		};
		setValidator(validator);	
		setDoubleClickSelects(true);
		setAllowMultiple(true);
		setMessage(SourceLookupUIMessages.folderSelection_label); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,  IDebugHelpContextIds.ADD_FOLDER_CONTAINER_DIALOG);
	}
	
}
