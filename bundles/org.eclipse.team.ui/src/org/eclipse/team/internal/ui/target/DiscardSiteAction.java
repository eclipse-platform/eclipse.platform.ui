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
package org.eclipse.team.internal.ui.target;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.ui.DetailsDialog;
import org.eclipse.team.internal.ui.Policy;

/**
 * Discards a remembered Site from Target Management. Before a Site can be
 * discarded all resources using this site must be deconfigured. This action will
 * prompt the user if a Site cannot be discarded because of existing connections.
 */
public class DiscardSiteAction extends TargetAction {

	private class AlreadyMappedDialog extends DetailsDialog {
		private IProject[] projects;
		private org.eclipse.swt.widgets.List detailsList;
		private Button unmap;
		
		public AlreadyMappedDialog(Shell shell, IProject[] projects) {
			super(shell, Policy.bind("SiteExplorerView.unmapDialogTitle")); //$NON-NLS-1$
			this.projects = projects;
		}
			
		protected Composite createDropDownDialogArea(Composite parent) {
			// create a composite with standard margins and spacing
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setFont(parent.getFont());
			
			detailsList = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);	 
			GridData data = new GridData ();		
			data.heightHint = 75;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			detailsList.setLayoutData(data);
			
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				TargetProvider target;
				try {
					target = TargetManager.getProvider(project);
				} catch (TeamException e) {
					continue;
				}
				detailsList.add(Policy.bind("SiteExplorerView.mappedProjects", project.getName(), target.getURL().toExternalForm())); //$NON-NLS-1$
			}			
			return composite;
		}

		protected void createMainDialogArea(Composite top) {
			Composite parent = new Composite(top, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 2;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(GridData.FILL_BOTH));
			parent.setFont(parent.getFont());
			
			// create image
			Image image = getImage(DLG_IMG_WARNING);
			if (image != null) {
				Label label = new Label(parent, 0);
				image.setBackground(label.getBackground());
				label.setImage(image);
				label.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_CENTER |
					GridData.VERTICAL_ALIGN_BEGINNING));
			}
			
			Label label = new Label(parent, SWT.WRAP);
			label.setText(Policy.bind("SiteExplorerView.projectsAlreadyMapped")); //$NON-NLS-1$
			GridData data = new GridData(
				GridData.GRAB_HORIZONTAL |
				GridData.GRAB_VERTICAL |
				GridData.HORIZONTAL_ALIGN_FILL |
				GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
						
			unmap = new Button(parent, SWT.CHECK);
			data = new GridData(
				GridData.GRAB_HORIZONTAL |
				GridData.GRAB_VERTICAL |
				GridData.HORIZONTAL_ALIGN_FILL |
				GridData.VERTICAL_ALIGN_CENTER);
			data.horizontalSpan = 2;
			unmap.setLayoutData(data);
			unmap.setText(Policy.bind("SiteExplorerView.unmapProjectsAndDisconnect")); //$NON-NLS-1$
			unmap.setSelection(false);
			unmap.addListener(SWT.Selection, new Listener() {				
				public void handleEvent(Event event) {
					updateEnablements();
				}
			});
			setPageComplete(false);
			updateEnablements();
		}

		protected void updateEnablements() {
			setPageComplete(unmap.getSelection());
		}
	}

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedSites().length > 0;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		deletedSelected();
	}
	
	private void deletedSelected() {
		try {
			Site[] sites = getSelectedSites();
			if(sites.length > 0) {
				
				// sites are already mapped, ask if they want to unmap projects first
				IProject[] mappedProjects = projectsMappedToSite(sites);
				if( mappedProjects.length > 0 ) {
					AlreadyMappedDialog dialog = new AlreadyMappedDialog(getShell(), mappedProjects);
					if(dialog.open() == dialog.OK) {
						for (int i = 0; i < mappedProjects.length; i++) {
							TargetManager.unmap(mappedProjects[i]);
						}
					} else {
						// nothing to do
						return;
					}
				} else {
					MessageDialog d = new MessageDialog(getShell(),
								Policy.bind("SiteExplorerView.promptForDeletingSitesTitle"), //$NON-NLS-1$
								null,
								Policy.bind("SiteExplorerView.promptForDeletingSites", new Integer(sites.length).toString()), //$NON-NLS-1$
								MessageDialog.QUESTION, 
								new String[] {
									IDialogConstants.YES_LABEL,
									IDialogConstants.CANCEL_LABEL }, 0);
					if( d.open() != d.OK ) {
						return;
					}
				}
				// sites aren't mapped, just ask then delete them form the view 
				// and from the target manager.
				for (int i = 0; i < sites.length; i++) {
					TargetManager.removeSite(sites[i]);
				}					
			}
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(),
				Policy.bind("Error"), //$NON-NLS-1$
				Policy.bind("CreateNewFolderAction.errorDeletingSites"), //$NON-NLS-1$
				e.getStatus());
		}
	}
	
	private IProject[] projectsMappedToSite(Site[] sites) throws TeamException {
		List mappedProjects = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			TargetProvider target = TargetManager.getProvider(project);
			for (int j = 0; j < sites.length; j++) {
				if(target != null && target.getSite().equals(sites[0])) {
					mappedProjects.add(project);	
				}
			}
		}
		return (IProject[]) mappedProjects.toArray(new IProject[mappedProjects.size()]);
	}
}