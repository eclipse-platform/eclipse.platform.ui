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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.model.CVSFileElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSRootFolderElement;
import org.eclipse.team.internal.ccvs.ui.model.RemoteContentProvider;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Page that allows the user to select the files used to refresh the tags for
 * the previously selected remote projects.
 */
public class RefreshFileSelectionPage extends CVSWizardPage {
	
	private ICVSRepositoryLocation root;
	private CVSRootFolderElement folders;
	private CheckboxTreeViewer treeViewer;
	private ICheckStateListener checkStateListener;
	// Map of ICVSRemoteFolder -> Set of ICVSRemoteFile
	private Map checkedFiles;

	class FileSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			boolean oneIsFile = e1 instanceof CVSFileElement;
			boolean twoIsFile = e2 instanceof CVSFileElement;
			if (oneIsFile != twoIsFile) {
				return oneIsFile ? 1 : -1;
			}
			return super.compare(viewer, e1, e2);
		}
	}
	
	/**
	 * Constructor for RefreshFileSelectionPage.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 * @param description
	 */
	public RefreshFileSelectionPage(
			String pageName,
			String title,
			ImageDescriptor titleImage,
			String description,
			ICVSRepositoryLocation root) {
		super(pageName, title, titleImage, description);
		this.folders = new CVSRootFolderElement(new ICVSFolder[0]);
		this.root = root;
		this.checkedFiles = new HashMap();
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		setControl(composite);
		// set F1 help
		//WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARING_FINISH_PAGE);
		
		treeViewer = new CheckboxTreeViewer(composite);
		treeViewer.setContentProvider(new RemoteContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		GridData data = new GridData (GridData.FILL_BOTH);
		data.heightHint = 150;
		treeViewer.getTree().setLayoutData(data);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateShownTags();
				updateWidgetEnablements();
			}
		});
		
		checkStateListener = new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				RefreshFileSelectionPage.this.handleChecked(event.getElement(), event.getChecked());
			}
		};
		treeViewer.addCheckStateListener(checkStateListener);
		treeViewer.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				// Ignore
			}
			public void treeExpanded(TreeExpansionEvent event) {
				RefreshFileSelectionPage.this.handleExpansion(event.getElement());
			}
		});
		
		initializeValues();
	}
	
	/**
	 * Callback from check state listener
	 */
	public void handleChecked(Object object, boolean checked) {
		if (object instanceof ICVSRemoteFile) {
			ICVSRemoteFile file = (ICVSRemoteFile)object;
			ICVSRemoteFolder parent = getRootFolder(file);
			Set files = (Set)checkedFiles.get(parent);
			if (checked) {
				files.add(file);
			} else {
				files.remove(file);
			}
		}
		updateWidgetEnablements();
	}
	
	/**
	 * Method getParent.
	 * @param file
	 * @return ICVSRemoteFolder
	 */
	private ICVSRemoteFolder getRootFolder(ICVSRemoteResource resource) {
		ICVSRemoteFolder parent = (ICVSRemoteFolder)resource.getParent();
		if (parent == null) return (ICVSRemoteFolder)resource;
		return getRootFolder(parent);
	}
	
	/**
	 * Callback from expansion listener
	 */
	public void handleExpansion(Object element) {
		treeViewer.setGrayed(element, true);
		updateCheckStates();
	}
	
	private void updateCheckStates() {
		for (Iterator iter = checkedFiles.keySet().iterator(); iter.hasNext();) {
			ICVSRemoteFolder folder = (ICVSRemoteFolder) iter.next();
			Set files = (Set)checkedFiles.get(folder);
			for (Iterator iterator = files.iterator(); iterator.hasNext();) {
				ICVSRemoteFile file = (ICVSRemoteFile) iterator.next();
				treeViewer.setChecked(file, true);
				checkAndGrayAncestors(file);
			}
		}
	}
	
	/**
	 * Method checkAndGrayAncestors.
	 * @param file
	 */
	private void checkAndGrayAncestors(ICVSRemoteResource resource) {
		ICVSRemoteFolder parent = (ICVSRemoteFolder)resource.getParent();
		if (parent == null) return;
		treeViewer.setGrayChecked(parent, true);
		checkAndGrayAncestors(parent);
	}

	/**
	 * Method initializeValues.
	 */
	private void initializeValues() {
		treeViewer.setInput(folders);
		initializeCheckedFiles();
	}

	private void initializeCheckedFiles() {
		checkedFiles.clear();
		ICVSFolder[] folders = this.folders.getRoots();
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		RepositoryRoot repoRoot = manager.getRepositoryRootFor(root);
		for (int i = 0; i < folders.length; i++) {
			ICVSFolder folder = folders[i];
			String[] filePaths = repoRoot.getAutoRefreshFiles(((ICVSRemoteFolder)folder).getRepositoryRelativePath());
			Set files = new HashSet();
			for (int j = 0; j < filePaths.length; j++) {
				String filePath = filePaths[j];
				ICVSRemoteFile file = root.getRemoteFile(filePath, CVSTag.DEFAULT);
				files.add(file);
			}
			checkedFiles.put(folder, files);
		}
	}
	
	private void updateShownTags() {
		// todo
	}
	
	private void updateWidgetEnablements() {
		// update the check state of repository locations and modules categories
		treeViewer.removeCheckStateListener(checkStateListener);
		try {
			updateCheckStates();
			// todo: perform enablement checks
		} finally {
			treeViewer.addCheckStateListener(checkStateListener);
		}
	}
	
	public void setRootFolders(ICVSFolder[] folders) {
		this.folders.setRoots(folders);
		if (treeViewer != null) {
			treeViewer.refresh();
			initializeCheckedFiles();
			updateWidgetEnablements();
		}
	}
	
	public void setRootFolders(ICVSRemoteResource[] resources) {
		List result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			ICVSRemoteResource resource = resources[i];
			if (resource.isFolder()) {
				result.add(resource);
			}
		}
		setRootFolders((ICVSFolder[]) result.toArray(new ICVSFolder[result.size()]));
	}

}
