/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.ListSelectionArea;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.WorkingSetSelectionArea;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.model.RemoteProjectsElement;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RefreshRemoteProjectSelectionPage extends CVSWizardPage {

	private Dialog parentDialog;
	private ICVSRepositoryLocation root;
	private ListSelectionArea listArea;
	private WorkingSetSelectionArea workingSetArea;
	private IWorkingSet workingSet;

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
			Dialog parentDialog, 
			ICVSRepositoryLocation root) {
		super(pageName, title, titleImage, description);
		this.parentDialog = parentDialog;
		this.root = root;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		setControl(composite);
		// set F1 help
		//WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARING_FINISH_PAGE);
		
		listArea = new ListSelectionArea(parentDialog, 
			new RemoteProjectsElement(root), 
			new RemoteContentProvider(), 
			new WorkbenchLabelProvider(), 
			Policy.bind("RemoteProjectSelectionPage.selectRemoteProjects"));
		listArea.createArea(composite);
		
		workingSetArea = new WorkingSetSelectionArea(parentDialog);
		setWorkingSet(workingSet);
		workingSetArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				workingSet = (IWorkingSet)event.getNewValue();
				handleWorkingSetChange();
			}
		});
		workingSetArea.createArea(composite);
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
			IAdaptable[] adaptables = workingSet.getElements();
			for (int i = 0; i < adaptables.length; i++) {
				// XXX needs to be modified for remote resources
				IAdaptable adaptable = adaptables[i];
				Object adapted = adaptable.getAdapter(IResource.class);
				if (adapted != null) {
					// Can this code be generalized?
					IProject project = ((IResource)adapted).getProject();
					listArea.getViewer().setChecked(project, true);
				}
			}
		}
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
