/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Iterator;

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
	private boolean supportsMultiSelection;
	
	private boolean isFetchingModules = false;
	private Object fetchingModulesLock = new Object();
	
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
				updateEnablements(false);
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
		updateEnablements(false);
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
	
	protected void updateEnablements(boolean updateModulesList) {
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
			if (moduleList.getInput() == null || updateModulesList) {
				boolean fetchModules = false;
				// The input is set after the page is shown to avoid
				// fetching if the user wants to specify the name manually
				try {
					// This can be called from different events in the event loop.
					// Ensure that we only fetch the input once
					synchronized (fetchingModulesLock) {
						if (!isFetchingModules) {
							// This the first thread in so fetch the modules
							fetchModules = true;
							isFetchingModules = true;
						}
					}
					if (fetchModules) {
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
						setModuleListInput();
					}
				} catch (InvocationTargetException e) {
					if (!badLocation) {
						badLocation = true;
						CVSUIPlugin.openError(getShell(), null, null, e);
						// This will null the module list input
						setModuleListInput();
					}
				} catch (InterruptedException e) {
					// Canceled by the user
				} finally {
					synchronized (fetchingModulesLock) {
						if (fetchModules) {
							isFetchingModules = false;
						}
					}
				}
			}
			setPageComplete(internalGetSelectedModules().length > 0);
		}
	}

	private ICVSRemoteFolder[] internalGetSelectedModules() {
		if (moduleList != null && moduleList.getControl().isEnabled()) {
			ISelection selection = moduleList.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				ArrayList result = new ArrayList();
				for (Iterator iter = ss.iterator(); iter.hasNext();) {
					Object element = iter.next();
					if (element instanceof ICVSRemoteFolder) {
						result.add(element);
					}
					
				}
				return (ICVSRemoteFolder[]) result.toArray(new ICVSRemoteFolder[result.size()]);
			}
		} else {
			ICVSRemoteFolder folder = null;
			if (moduleName != null) {
				folder = internalCreateModuleHandle(moduleName);
			} else {
				if (project != null) {
					folder = internalCreateModuleHandle(project.getName());
				}
			}
			if (folder != null) {
				return new ICVSRemoteFolder[] { folder };
			}
		} 
		return new ICVSRemoteFolder[0];
	}
	
	private ICVSRemoteFolder internalCreateModuleHandle(String name) {
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
		ICVSRemoteFolder[] selectedModules = getSelectedModules();
		if (selectedModules.length > 0) {
			return selectedModules[0];
		} else {
			return null;
		}
	}
	
	public ICVSRemoteFolder[] getSelectedModules() {
		final ICVSRemoteFolder[][] folder = new ICVSRemoteFolder[][] { null };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				folder[0] = internalGetSelectedModules();
			}
		});
		return folder[0];
	}
	
	private TreeViewer createModuleTree(Composite composite, int horizontalSpan) {
		Tree tree = new Tree(composite, (supportsMultiSelection ? SWT.MULTI : SWT.SINGLE) | SWT.BORDER);
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
				updateEnablements(false);
				ICVSRemoteFolder[] modules = internalGetSelectedModules();
				if (modules.length == 1) {
					// There is at 1 module selected
					ICVSRemoteFolder selectedModule = modules[0];
					String repositoryRelativePath = selectedModule.getRepositoryRelativePath();
					if (!repositoryRelativePath.equals(FolderSyncInfo.VIRTUAL_DIRECTORY)) {
						text.setText(repositoryRelativePath);
					}
				} else {
					text.setText(""); //$NON-NLS-1$
				}
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
		boolean refresh = location != null && !location.equals(this.location);
		this.location = location;
		badLocation = false;
		if (moduleList != null) {
			updateEnablements(refresh);
		}
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	public void setSupportsMultiSelection(boolean supportsMultiSelection) {
		this.supportsMultiSelection = supportsMultiSelection;
	}
}
