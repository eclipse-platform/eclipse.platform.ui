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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.model.ModulesCategory;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CVSWorkingSetPage extends CVSWizardPage implements IWorkingSetPage {

	private IWorkingSet workingSet;
	private String workingSetName;
	private Set checkedFolders;
	private Map modulesCategoryCache = new HashMap();
	private boolean editing = false;

	private Text nameField;
	private CheckboxTreeViewer tree;
	private ICheckStateListener checkStateListener;

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage#CVSWizardPage(String, String, ImageDescriptor, String)
	 */
	public CVSWorkingSetPage(String pageName, String title, ImageDescriptor titleImage, String description) {
		super(pageName, title, titleImage, description);
		checkedFolders = new HashSet();
		workingSetName = ""; //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite= createComposite(parent, 2);
		setControl(composite);

		WorkbenchHelp.setHelp(composite, IHelpContextIds.WORKING_SET_FOLDER_SELECTION_PAGE);

		createLabel(composite, Policy.bind("CVSWorkingSetFolderSelectionPage.name")); //$NON-NLS-1$
		nameField = createTextField(composite);
		nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				workingSetName = nameField.getText();
				updateWidgetEnablements();
			}
		});

		createWrappingLabel(composite, Policy.bind("CVSWorkingSetFolderSelectionPage.treeLabel"), 0, 2); //$NON-NLS-1$

		tree = createFolderSelectionTree(composite, 2);
		checkStateListener = new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				CVSWorkingSetPage.this.handleChecked(event.getElement(), event.getChecked());
			}
		};
		tree.addCheckStateListener(checkStateListener);
		tree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				// Ignore
			}
			public void treeExpanded(TreeExpansionEvent event) {
				CVSWorkingSetPage.this.handleExpansion(event.getElement());
			}
		});

		createLabel(composite, ""); //$NON-NLS-1$
		Button refresh = new Button(composite, SWT.PUSH);
		refresh.setText(Policy.bind("CVSWorkingSetFolderSelectionPage.refresh")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		data.horizontalSpan = 1;
		refresh.setLayoutData(data);
		refresh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CVSWorkingSetPage.this.refreshTree();
			}
		});

		initializeValues();
		updateWidgetEnablements();
		nameField.setFocus();
	}
	/**
	 * Method handleExpansion.
	 * @param object
	 */
	public void handleExpansion(Object element) {
		if (element instanceof ICVSRepositoryLocation) {
			ICVSRepositoryLocation location = (ICVSRepositoryLocation) element;
			tree.setGrayed(CVSWorkingSetPage.this.getModuleCategory(location), true);
		}
		updateCheckState(element);
	}

	/**
	 * Method handleChecked.
	 * @param object
	 * @param b
	 */
	public void handleChecked(Object object, boolean checked) {
		if (object instanceof ICVSRepositoryLocation) {
			ICVSRepositoryLocation location = (ICVSRepositoryLocation) object;
			if (checked) {
				repositoryLocationChecked(location);
			} else {
				repositoryLocationUnchecked(location);
			}
		} else if (object instanceof ModulesCategory) {
			ModulesCategory category = (ModulesCategory) object;
			if (checked) {
				modulesCategoryChecked(category);
			} else {
				modulesCategoryUnchecked(category);
			}
		} else if (object instanceof ICVSRemoteFolder) {
			ICVSRemoteFolder folder = (ICVSRemoteFolder) object;
			if (checked) {
				folderChecked(folder);
			} else {
				folderUnchecked(folder);
			}
		}
		updateWidgetEnablements();
	}

	public ModulesCategory getModuleCategory(ICVSRepositoryLocation location) {
		ModulesCategory category = (ModulesCategory)modulesCategoryCache.get(location.getLocation());
		if (category == null) {
			category = new ModulesCategory(location);
			modulesCategoryCache.put(location.getLocation(), category);
			category.setRunnableContext(getContainer());
		}
		return category;
	}

	private void folderUnchecked(ICVSRemoteFolder folder) {
		checkedFolders.remove(folder);
	}

	private void folderChecked(ICVSRemoteFolder folder) {
		checkedFolders.add(folder);
	}

	private void repositoryLocationChecked(ICVSRepositoryLocation location) {
		Object[] elements = tree.getCheckedElements();
		boolean stayChecked = false;
		for (int i = 0; i < elements.length; i++) {
			Object object = elements[i];
			if (object instanceof ICVSRemoteFolder) {
				ICVSRemoteFolder folder = (ICVSRemoteFolder) object;
				if (folder.getRepository().equals(location)) {
					stayChecked = true;
				}
			}
		}
		if (!stayChecked) {
			tree.setChecked(location, false);
		}
	}

	private void repositoryLocationUnchecked(ICVSRepositoryLocation location) {
		for (Iterator iter = checkedFolders.iterator(); iter.hasNext();) {
			ICVSRemoteFolder folder = (ICVSRemoteFolder) iter.next();
			if (folder.getRepository().equals(location)) {
				tree.setChecked(folder, false);
				iter.remove();
			}
		}
	}

	private void modulesCategoryChecked(ModulesCategory category) {
		ICVSRepositoryLocation location = category.getRepository();
		Object[] elements = tree.getCheckedElements();
		boolean stayChecked = false;
		for (int i = 0; i < elements.length; i++) {
			Object object = elements[i];
			if (object instanceof ICVSRemoteFolder) {
				ICVSRemoteFolder folder = (ICVSRemoteFolder) object;
				if (folder.getRepository().equals(location) && (folder.isDefinedModule())) {
					stayChecked = true;
				}
			}
		}
		if (!stayChecked) {
			tree.setChecked(category, false);
		}
	}

	private void modulesCategoryUnchecked(ModulesCategory category) {
		ICVSRepositoryLocation location = category.getRepository();
		for (Iterator iter = checkedFolders.iterator(); iter.hasNext();) {
			ICVSRemoteFolder folder = (ICVSRemoteFolder) iter.next();
			if (folder.getRepository().equals(location) && folder.isDefinedModule()) {
				tree.setChecked(folder, false);
				iter.remove();
			}
		}
	}

	/**
	 * Method refreshTree.
	 */
	public void refreshTree() {
		tree.refresh();
	}

	/**
	 * Method initializeValues.
	 */
	private void initializeValues() {
		nameField.setText(workingSetName);
		tree.setGrayedElements(getRepositoryManager().getKnownRepositoryLocations());
	}

	/**
	 * Method updateWidgetEnablement.
	 */
	private void updateWidgetEnablements() {
		// update the check state of repository locations and modules categories
		tree.removeCheckStateListener(checkStateListener);
		try {
			updateCheckState(null);
			// Make sure the name is valid
			if (workingSetName.length() == 0) {
				setPageComplete(false);
				setErrorMessage(null);
			} else if (!isValidName(workingSetName)) {
				setPageComplete(false);
				setErrorMessage(Policy.bind("CVSWorkingSetFolderSelectionPage.invalidWorkingSetName", workingSetName)); //$NON-NLS-1$
			} else if (isDuplicateName(workingSetName)) {
				setPageComplete(false);
				setErrorMessage(Policy.bind("CVSWorkingSetFolderSelectionPage.duplicateWorkingSetName", workingSetName)); //$NON-NLS-1$
			} else if (checkedFolders.isEmpty()) {
				setErrorMessage(Policy.bind("CVSWorkingSetFolderSelectionPage.mustSelectFolder")); //$NON-NLS-1$
				setPageComplete(false);
			} else {
				setPageComplete(true);
				setErrorMessage(null);
			}
		} finally {
			tree.addCheckStateListener(checkStateListener);
		}
	}

	private void updateCheckState(Object expanded) {
		ICVSRepositoryLocation[] locations = getRepositoryManager().getKnownRepositoryLocations();
		Set checkedLocations = new HashSet();
		Set checkedCategories = new HashSet();
		List expandedParents = new ArrayList();
		expandedParents.addAll(Arrays.asList(tree.getExpandedElements()));
		if (expanded != null)
			expandedParents.add(expanded);
		for (Iterator iter = checkedFolders.iterator(); iter.hasNext();) {
			ICVSRemoteFolder folder = (ICVSRemoteFolder) iter.next();
			checkedLocations.add(folder.getRepository());
			if (folder.isDefinedModule()) {
				ModulesCategory category = getModuleCategory(folder.getRepository());
				checkedCategories.add(getModuleCategory(folder.getRepository()));
				if (expandedParents.contains(category))
					tree.setChecked(folder, true);
			} else if (expandedParents.contains(folder.getRepository())) {
				tree.setChecked(folder, true);
			}
		}
		for (int i = 0; i < locations.length; i++) {
			ICVSRepositoryLocation location = locations[i];
			tree.setChecked(location, checkedLocations.contains(location));
		}
		for (Iterator iter = modulesCategoryCache.values().iterator(); iter.hasNext();) {
			ModulesCategory element = (ModulesCategory) iter.next();
			if (expandedParents.contains(element.getRepository()))
				tree.setChecked(element, checkedCategories.contains(element));
		}
	}

	/**
	 * Method isValidName.
	 * @param workingSetName
	 * @return boolean
	 */
	private boolean isValidName(String workingSetName) {
		if (workingSetName.length() == 0)
			return false;
		if (workingSetName.startsWith(" ")) //$NON-NLS-1$
			return false;
		if (workingSetName.endsWith(" ")) //$NON-NLS-1$
			return false;
		for (int i = 0; i < workingSetName.length(); i++) {
			char c = workingSetName.charAt(i);
			if (! Character.isLetterOrDigit(c) && ! Character.isSpaceChar(c))
				return false;
		}
		return true;
	}

	/**
	 * Method isDuplicateName.
	 * @param workingSetName
	 * @return boolean
	 */
	private boolean isDuplicateName(String workingSetName) {
		String originalWorkingSetName = workingSet.getName();
		if (originalWorkingSetName != null && workingSetName.equals(originalWorkingSetName)) {
			return false;
		}
		IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
		for (int i= 0; i < workingSets.length; i++) {
			if (workingSetName.equals(workingSets[i].getName())) {
				return true;
			}
		}
		return false;
	}

	protected CheckboxTreeViewer createFolderSelectionTree(Composite composite, int span) {
		CheckboxTreeViewer tree = new CheckboxTreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tree.setUseHashlookup(true);
		tree.setContentProvider(new WorkbenchContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof CVSWorkingSetPage) {
					return getRepositoryManager().getKnownRepositoryLocations();
				}
				if (parentElement instanceof ICVSRepositoryLocation) {
					ICVSRepositoryLocation location = (ICVSRepositoryLocation)parentElement;
					List result = new ArrayList();
					result.add(getModuleCategory(location));
					result.addAll(Arrays.asList(CVSWorkingSetPage.this.getChildren((ICVSRepositoryLocation)parentElement)));
					return (Object[]) result.toArray(new Object[result.size()]);
				}
				if (parentElement instanceof ModulesCategory) {
					ModulesCategory modules = (ModulesCategory)parentElement;
					return modules.getChildren(modules);
				}
				return new Object[0];
			}
			public Object getParent(Object element) {
				if (element instanceof ICVSRemoteFolder) {
					return ((ICVSRemoteFolder)element).getRepository();
				}
				return null;
			}
			public boolean hasChildren(Object parentElement) {
				return parentElement instanceof CVSWorkingSetPage || parentElement instanceof ICVSRepositoryLocation || parentElement instanceof ModulesCategory;
			}
		});
		tree.setLabelProvider(new WorkbenchLabelProvider());
		tree.setSorter(new RepositorySorter());
		tree.setInput(this);

		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		data.heightHint = LIST_HEIGHT_HINT;
		data.horizontalSpan = span;
		tree.getControl().setLayoutData(data);
		return tree;
	}

	/**
	 * Method getChildren.
	 * @param iCVSRepositoryLocation
	 * @return Object[]
	 */
	public ICVSRemoteResource[] getChildren(final ICVSRepositoryLocation location) {
		final ICVSRemoteResource[][] result = new ICVSRemoteResource[1][0];
		result[0] = null;
		try {
			getContainer().run(false /* fork */, true /* cancelable */, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						result[0] = location.members(CVSTag.DEFAULT, false /* show modules */, monitor);
					} catch (CVSException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			CVSUIPlugin.openError(getContainer().getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC);
		} catch (InterruptedException e) {
		}
		return result[0];
	}

	public ICVSRepositoryLocation[] getRepositoryLocationsWithCheckedChildren() {
		Set locations = new HashSet();
		for (Iterator iter = checkedFolders.iterator(); iter.hasNext();) {
			ICVSRemoteFolder element = (ICVSRemoteFolder) iter.next();
			locations.add(element.getRepository());
		}
		return (ICVSRepositoryLocation[]) locations.toArray(new ICVSRepositoryLocation[locations.size()]);
	}
	/**
	 * Returns the workingSetName.
	 * @return String
	 */
	public String getWorkingSetName() {
		return workingSetName;
	}

	public ICVSRemoteFolder[] getSelectedFolders() {
		return (ICVSRemoteFolder[]) checkedFolders.toArray(new ICVSRemoteFolder[checkedFolders.size()]);
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
	 */
	public void finish() {
		// todo
	}
	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
	 */
	public IWorkingSet getSelection() {
		return workingSet;
	}
	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(org.eclipse.ui.IWorkingSet)
	 */
	public void setSelection(IWorkingSet workingSet) {
		this.workingSet = workingSet;
		workingSetName = workingSet.getName();
	}

}
