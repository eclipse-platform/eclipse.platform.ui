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


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.repo.RepositorySorter;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ModuleSelectionPage extends CVSWizardPage {
	Button useProjectNameButton;
	Button useSpecifiedNameButton;
	private Button selectModuleButton;
	Text text;
	TreeViewer moduleList;
	
	String moduleName;
	
	// The project being associated with the remote module (or null)
	private IProject project;
	private ICVSRepositoryLocation location;
	private boolean badLocation = false;
	private String helpContextId;
	
	public ModuleSelectionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public void setHelpContxtId(String helpContextId) {
		this.helpContextId = helpContextId;
	}
	
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);

		if (helpContextId != null)
			WorkbenchHelp.setHelp(composite, helpContextId);
		
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				updateEnablements();
			}
		};
		
		if (project != null) {
			useProjectNameButton = createRadioButton(composite, Policy.bind("ModuleSelectionPage.moduleIsProject"), 2); //$NON-NLS-1$
			useProjectNameButton.addListener(SWT.Selection, listener);
		}
		useSpecifiedNameButton = createRadioButton(composite, Policy.bind("ModuleSelectionPage.specifyModule"), 1); //$NON-NLS-1$
		useSpecifiedNameButton.addListener(SWT.Selection, listener);

		text = createTextField(composite);
		text.addListener(SWT.Modify, listener);
		
		selectModuleButton = createRadioButton(composite, Policy.bind("ModuleSelectionPage.2"), 2); //$NON-NLS-1$
		selectModuleButton.addListener(SWT.Selection, listener);
		moduleList = createModuleTree(composite, 2);
		
		// Set the initial enablement
		if (useProjectNameButton != null) {
			useProjectNameButton.setSelection(true);
			useSpecifiedNameButton.setSelection(false);
		} else {
			useSpecifiedNameButton.setSelection(true);
		}
		selectModuleButton.setSelection(false);
		updateEnablements();
		setControl(composite);
        Dialog.applyDialogFont(parent);
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			if (useProjectNameButton != null && useProjectNameButton.getSelection()) {
				useProjectNameButton.setFocus();
			} else if (useSpecifiedNameButton.getSelection()) {
				text.setFocus();
			} else {
				moduleList.getControl().setFocus();
			}
		}
	}
	
	protected void updateEnablements() {
		if (useProjectNameButton != null && useProjectNameButton.getSelection()) {
			text.setEnabled(false);
			moduleList.getControl().setEnabled(false);
			moduleName = null;
			setPageComplete(true);
		} else if (useSpecifiedNameButton.getSelection()) {
			text.setEnabled(true);
			moduleList.getControl().setEnabled(false);
			moduleName = text.getText();
			if (moduleName.length() == 0) {
				moduleName = null;
				setPageComplete(false);
			} else {
				setPageComplete(true);
			}
		} else if (!badLocation){
			text.setEnabled(false);
			moduleList.getControl().setEnabled(true);
			moduleName = null;
			if (moduleList.getInput() == null) {
				// The input is set after the page is shown to avoid
				// fetching if the user wants to specify the name manually
				try {
					// Validate the location first since the module fecthing is
					// done in a deferred fashion
					getContainer().run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								location.validateConnection(monitor);
							} catch (CVSException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InvocationTargetException e) {
					if (!badLocation) {
						badLocation = true;
						CVSUIPlugin.openError(getShell(), null, null, e);
					}
				} catch (InterruptedException e) {
					// Canceled by the user
				}
				setModuleListInput();
			}
			ICVSRemoteFolder folder = internalGetSelectedModule();
			setPageComplete(folder != null);
		}
	}

	private ICVSRemoteFolder internalGetSelectedModule() {
		if (moduleList != null && moduleList.getControl().isEnabled()) {
			ISelection selection = moduleList.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				Object firstElement = ss.getFirstElement();
				if (firstElement instanceof ICVSRemoteFolder) {
					return (ICVSRemoteFolder)firstElement;
				}
			}
		}
		return null;
	}
	
	private ICVSRemoteFolder internalCreateModule(String name) {
		ICVSRepositoryLocation location = getLocation();
		if (location == null) return null;
		return location.getRemoteFolder(name, CVSTag.DEFAULT);
	}
	
	/**
	 * Return the selected existing remote folder. If this
	 * method returns <code>null</code>, then <code>getModuleName()</code>
	 * can be used to get the name entered manually by the use.
	 * @return the selected exisiting remote module
	 */
	public ICVSRemoteFolder getSelectedModule() {
		final ICVSRemoteFolder[] folder = new ICVSRemoteFolder[] { null };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				folder[0] = internalGetSelectedModule();
				if (folder[0] == null) {
					if (moduleName != null) {
						folder[0] = internalCreateModule(moduleName);
					} else {
						if (project != null) {
							folder[0] = internalCreateModule(project.getName());
						}
					}
				}
			}
		});
		return folder[0];
	}
	
	public boolean isSelectedModuleExists() {
		return internalGetSelectedModule() != null;
	}
	
	private TreeViewer createModuleTree(Composite composite, int horizontalSpan) {
		Tree tree = new Tree(composite, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = horizontalSpan;
		tree.setLayoutData(data);	
		TreeViewer result = new TreeViewer(tree) {
			/*
			 * Fix to allow filtering to be used without triggering fetching 
			 * of the contents of all children (see bug 62268)
			 */
			public boolean isExpandable(Object element) {
				ITreeContentProvider cp = (ITreeContentProvider) getContentProvider();
				if(cp == null)
					return false;
				
				return cp.hasChildren(element);
			}
		};
		result.setContentProvider(new RemoteContentProvider());
		result.setLabelProvider(new WorkbenchLabelProvider());
		result.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return !(element instanceof ICVSRemoteFile);
			}
		});
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablements();
				ICVSRemoteFolder selectedModule = getSelectedModule();
				if (selectedModule == null) return;
				String repositoryRelativePath = selectedModule.getRepositoryRelativePath();
				if (repositoryRelativePath.equals(FolderSyncInfo.VIRTUAL_DIRECTORY)) return;
				text.setText(repositoryRelativePath);
			}
		});
		result.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if (getSelectedModule() != null) {
					ModuleSelectionPage.this.getContainer().showPage(getNextPage());
				}
			}
		});
		result.setSorter(new RepositorySorter());
		return result;
	}
	
	private void setModuleListInput() {
		ICVSRepositoryLocation location = getLocation();
		if (location == null || badLocation) return;
		moduleList.setInput(location.getRemoteFolder(ICVSRemoteFolder.REPOSITORY_ROOT_FOLDER_NAME, CVSTag.DEFAULT));
	}

	private ICVSRepositoryLocation getLocation() {
		return location;
	}
	
	public void setLocation(ICVSRepositoryLocation location) {
		this.location = location;
		badLocation = false;
		if (moduleList != null) {
			updateEnablements();
		}
	}

	public void setProject(IProject project) {
		this.project = project;
	}
}
