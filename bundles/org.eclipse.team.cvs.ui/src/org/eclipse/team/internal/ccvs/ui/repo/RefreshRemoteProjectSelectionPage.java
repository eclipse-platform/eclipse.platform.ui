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
package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.Arrays;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.ListSelectionArea;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.WorkingSetSelectionArea;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Page that allows the user to select the remote projects whose tags should be
 * refreshed.
 */
public class RefreshRemoteProjectSelectionPage extends CVSWizardPage {
    
	private ICVSRemoteResource[] rootFolders;
	private ListSelectionArea listArea;
	private WorkingSetSelectionArea workingSetArea;
	private IWorkingSet workingSet;
	private IDialogSettings settings;

	/**
	 * Custom input provider which returns the list of root folders
	 */
	private class InputElement implements IWorkbenchAdapter, IAdaptable {
		public Object[] getChildren(Object o) {
			return rootFolders;
		}
		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}
		public String getLabel(Object o) {
			return null;
		}
		public Object getParent(Object o) {
			return null;
		}
		public Object getAdapter(Class adapter) {
			if (adapter == IWorkbenchAdapter.class) return this;
			return null;
		}
	}
	
	/**
	 * Constructor for RemoteProjectSelectionPage.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 * @param description
	 */
	public RefreshRemoteProjectSelectionPage(
			String pageName,
			String title,
			ImageDescriptor titleImage,
			String description, 
			IDialogSettings settings,
			ICVSRepositoryLocation root,
			ICVSRemoteResource[] rootFolders) {
		super(pageName, title, titleImage, description);
		this.settings = settings;
		this.rootFolders = rootFolders;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		setControl(composite);
		
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.REFRESH_REMOTE_PROJECT_SELECTION_PAGE);
		
		listArea = new ListSelectionArea( 
			new InputElement(), 
			new RemoteContentProvider(), 
			new WorkbenchLabelProvider(), 
			Policy.bind("RefreshRemoteProjectSelectionPage.selectRemoteProjects")); //$NON-NLS-1$
		listArea.createArea(composite);

		listArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateEnablement();
			}
		});
		listArea.getViewer().setSorter(new RepositorySorter());
		
		workingSetArea = new WorkingSetSelectionArea(getShell(), Policy.bind("RefreshRemoteProjectSelectionPage.noWorkingSet"), Policy.bind("RefreshRemoteProjectSelectionPage.workingSet"), settings); //$NON-NLS-1$ //$NON-NLS-2$
		setWorkingSet(workingSet);
		workingSetArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				workingSet = (IWorkingSet)event.getNewValue();
				handleWorkingSetChange();
			}
		});
		workingSetArea.createArea(composite);
        Dialog.applyDialogFont(parent);
	}

	/**
	 * Sets the working set that should be selected in the most recently
	 * used working set list.
	 *
	 * @param workingSet the working set that should be selected.
	 * 	has to exist in the list returned by
	 * 	org.eclipse.ui.IWorkingSetManager#getRecentWorkingSets().
	 * 	Must not be null.
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;

		if (workingSetArea != null) {
			workingSetArea.setWorkingSet(workingSet);
		}
	}
	
	private void handleWorkingSetChange() {
		if (workingSet != null) {
			// check any projects in the working set
			listArea.getViewer().setAllChecked(false);
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			ICVSRemoteResource[] resources = manager.filterResources(workingSet, rootFolders);
			for (int i = 0; i < resources.length; i++) {
				ICVSRemoteResource resource = resources[i];
				listArea.getViewer().setChecked(resource, true);
			}
		}
	}
	
	private void updateEnablement() {
		boolean atLeastOne = listArea.getViewer().getCheckedElements().length > 0;
		setPageComplete(atLeastOne);
	}
	
	/**
	 * Method getSelectedRemoteProject.
	 * @return ICVSRemoteResource[]
	 */
	public ICVSRemoteResource[] getSelectedRemoteProject() {
		Object[] checked = listArea.getViewer().getCheckedElements();
		return (ICVSRemoteResource[]) Arrays.asList(checked).toArray(new ICVSRemoteResource[checked.length]);
	}
}
