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
package org.eclipse.team.internal.ccvs.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.AdaptableResourceList;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Select the target parent folder and folder name for the 'Checkout Into" operation.
 */
public class CheckoutIntoProjectSelectionPage extends CVSWizardPage {
	
	private TreeViewer tree;
	private Text nameField;
	private Combo filterList;
	private Button recurseCheck;
	
	private IResource[] resources;
	private IResource selection;
	private ICVSRemoteFolder remoteFolder;
	private String folderName;
	private boolean recurse;
	private int filter;

	
	/**
	 * Constructor for CheckoutIntoProjectSelectionPage.
	 * @param pageName
	 */
	public CheckoutIntoProjectSelectionPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor for CheckoutIntoProjectSelectionPage.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public CheckoutIntoProjectSelectionPage(String pageName, String title, ImageDescriptor titleImage, String description) {
		super(pageName, title, titleImage, description);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite= createComposite(parent, 2);
		setControl(composite);
		
		WorkbenchHelp.setHelp(composite, IHelpContextIds.CHECKOUT_INTO_RESOURCE_SELECTION_PAGE);
		
		createLabel(composite, Policy.bind("CheckoutIntoProjectSelectionPage.name")); //$NON-NLS-1$
		nameField = createTextField(composite);
		nameField.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				folderName = nameField.getText();
				updateWidgetEnablements();
			}
		});
		
		createWrappingLabel(composite, Policy.bind("CheckoutIntoProjectSelectionPage.treeLabel"), 0, 2); //$NON-NLS-1$
		
		tree = createResourceSelectionTree(composite, IResource.PROJECT | IResource.FOLDER, 2 /* horizontal span */);
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleResourceSelection(event);
			}
		});

		Composite filterComposite = createComposite(composite, 2);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		filterComposite.setLayoutData(data);
		createLabel(filterComposite, Policy.bind("CheckoutIntoProjectSelectionPage.showLabel")); //$NON-NLS-1$
		filterList = createCombo(filterComposite);
		filterList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleFilterSelection();
			}
		});
		
		createWrappingLabel(composite, "", 0, 2); //$NON-NLS-1$
				
		// Should subfolders of the folder be checked out?
		recurseCheck = createCheckBox(composite, Policy.bind("CheckoutIntoProjectSelectionPage.recurse")); //$NON-NLS-1$
		recurseCheck.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				recurse = recurseCheck.getSelection();
				updateWidgetEnablements();
			}
		});
		
		initializeValues();
		updateWidgetEnablements();
		tree.getControl().setFocus();
	}

	/**
	 * Method initializeValues.
	 */
	private void initializeValues() {
		nameField.setText(remoteFolder.getName());
		tree.setInput(ResourcesPlugin.getWorkspace().getRoot());
		recurse = true;
		recurseCheck.setSelection(recurse);
		filter = 0;
		updateTreeContents(filter);
		filterList.add(Policy.bind("CheckoutIntoProjectSelectionPage.showAll")); //$NON-NLS-1$
		filterList.add(Policy.bind("CheckoutIntoProjectSelectionPage.showUnshared")); //$NON-NLS-1$
		filterList.add(Policy.bind("CheckoutIntoProjectSelectionPage.showSameRepo")); //$NON-NLS-1$
		filterList.select(filter);
	}

	private void handleResourceSelection(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			this.selection = null;
		} else if (selection instanceof IStructuredSelection) {
			this.selection = (IResource)((IStructuredSelection)selection).getFirstElement();
		}
		updateWidgetEnablements();
	}
	
	/**
	 * Method updateWidgetEnablement.
	 */
	private void updateWidgetEnablements() {
		if (!Path.EMPTY.isValidSegment(folderName)) {
			setPageComplete(false);
			setErrorMessage(Policy.bind("CheckoutIntoProjectSelectionPage.invalidFolderName", folderName)); //$NON-NLS-1$
			return;
		}
		boolean complete = selection != null && selection.getType() != IResource.FILE;
		setErrorMessage(null);
		setPageComplete(complete);
	}
	
	/**
	 * Returns the selection.
	 * @return IResource
	 */
	public IResource getSelection() {
		return selection;
	}
	
	/**
	 * Sets the remoteFolder.
	 * @param remoteFolder The remoteFolder to set
	 */
	public void setRemoteFolder(ICVSRemoteFolder remoteFolder) {
		this.remoteFolder = remoteFolder;
	}

	/**
	 * Returns the folderName.
	 * @return String
	 */
	public String getFolderName() {
		return folderName;
	}

	private void updateTreeContents(int selected) {
		try {
			if (selected == 0) {
				tree.setInput(new AdaptableResourceList(getProjects(remoteFolder.getFolderSyncInfo().getRoot(), true)));
			} else if (selected == 1) {
				tree.setInput(new AdaptableResourceList(getProjects(null, true)));
			} else if (selected == 2) {
				tree.setInput(new AdaptableResourceList(getProjects(remoteFolder.getFolderSyncInfo().getRoot(), false)));
			}
		} catch (CVSException e) {
			CVSUIPlugin.log(e.getStatus());
		}
	}
			
	/**
	 * Method getValidTargetProjects returns the et of projects that match the provided criteria.
	 * @return IResource
	 */
	private IProject[] getProjects(String root, boolean unshared) throws CVSException {
		List validTargets = new ArrayList();
		try {
			IResource[] projects = ResourcesPlugin.getWorkspace().getRoot().members();
			for (int i = 0; i < projects.length; i++) {
				IResource resource = projects[i];
				if (resource instanceof IProject) {
					IProject project = (IProject) resource;
					if (project.isAccessible()) {
						RepositoryProvider provider = RepositoryProvider.getProvider(project);
						if (provider == null && unshared) {
							validTargets.add(project);
						} else if (provider != null && provider.getID().equals(CVSProviderPlugin.getTypeId())) {
							ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(project);
							FolderSyncInfo info = cvsFolder.getFolderSyncInfo();
							if (root != null && root.equals(info.getRoot())) {
								validTargets.add(project);
							}
						}
					}
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
		return (IProject[]) validTargets.toArray(new IProject[validTargets.size()]);
	}
	
	public IContainer getLocalFolder() {
		if (Path.EMPTY.isValidSegment(folderName)) {
			return ((IContainer)getSelection()).getFolder(new Path(folderName));
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the recurse.
	 * @return boolean
	 */
	public boolean isRecurse() {
		return recurse;
	}
	
	private void handleFilterSelection() {
		filter = filterList.getSelectionIndex();
		updateTreeContents(filter);
	}
}
