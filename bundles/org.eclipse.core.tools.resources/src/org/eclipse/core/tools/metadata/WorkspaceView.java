/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.File;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tools.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Implements the Workspace Spy view. This view allows the user to browse the
 * known files in a .metadata directory. "Known files" are those whose names
 * are known by the DumperFactory, thus they can be dumped.
 */
public class WorkspaceView extends SpyView {

	/** 
	 * A JFace widget used to display a tree corresponding to the .metadata
	 * directory structure (containing only files of known types). 
	 */
	protected AbstractTreeViewer viewer;

	/** The current selected workspace's metadata dir. */
	protected File metadataPath;

	/** The id by which this view is known in the plug-in registry */
	public static final String VIEW_ID = WorkspaceView.class.getName();

	/**
	 * Dump file action implementation.
	 */
	private class DumpFileAction extends Action {
		/**
		 * Constructs a DumpFileAction, initializing text and tool tip text.
		 */
		DumpFileAction() {
			this.setText("Dump Selected File"); //$NON-NLS-1$
			this.setToolTipText("Dump selected file"); //$NON-NLS-1$
		}

		/**
		 * This action activates the Dump Contents view with the current selected
		 * file in this view (if any)
		 *
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			IStructuredSelection sel = (IStructuredSelection) WorkspaceView.this.viewer.getSelection();
			if (sel == null || sel.isEmpty())
				return;

			TreeContentProviderNode fileNode = (TreeContentProviderNode) sel.getFirstElement();

			File file = (File) fileNode.getValue();
			// we have no interest in directories
			if (!file.isFile())
				return;

			try {
				DumpContentsView dumpView = (DumpContentsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DumpContentsView.VIEW_ID);
				dumpView.setFile(file);
			} catch (PartInitException e) {
				ErrorUtil.showErrorMessage(e.getMessage(), "Error opening view"); //$NON-NLS-1$
				ErrorUtil.logException(e, "Error opening view"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Select workspace action implementation.
	 */
	private class SelectWorkspaceAction extends Action {
		/**
		 * Initializes action's text and tool tip text.
		 */
		SelectWorkspaceAction() {
			this.setText("Select Workspace..."); //$NON-NLS-1$
			this.setToolTipText("Select workspace..."); //$NON-NLS-1$
		}

		/**
		 * Executes action, opening a file dialog so the user can select which
		 * workspace .metadata directory will be browsed.
		 *
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			DirectoryDialog dirDialog = new DirectoryDialog(viewer.getControl().getShell());

			dirDialog.setText("Open .metadata directory"); //$NON-NLS-1$
			dirDialog.setMessage("Please choose a Eclipse workspace .metadata directory"); //$NON-NLS-1$
			dirDialog.setFilterPath(metadataPath.getAbsolutePath());

			String dirPath = dirDialog.open();

			if (dirPath == null)
				return;

			setMetadataRoot(new File(dirPath));
		}
	}

	/** 
	 * Constructs a WorkspaceView.
	 */
	public WorkspaceView() {
		// initializes the default .metadata location with the current platform 
		// instance .metadata location
		this.metadataPath = new File(Platform.getLocation().toFile(), ".metadata"); //$NON-NLS-1$
	}

	/**
	 * Creates this view widget and actions.
	 * 
	 * @param parent the parent control
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);

		// sets a content provider for the viewer
		DumperFactory factory = DumperFactory.getInstance();
		String[] fileNames = factory.getRegisteredFileNames();
		WorkspaceContentProvider contentProvider;
		contentProvider = new WorkspaceContentProvider(fileNames);
		viewer.setContentProvider(contentProvider);

		// creates actions
		final IAction dumpFileAction = new DumpFileAction();
		final IAction selectWorkspaceAction = new SelectWorkspaceAction();

		// adds actions to the menu bar
		IMenuManager barMenuManager = getViewSite().getActionBars().getMenuManager();
		barMenuManager.add(selectWorkspaceAction);
		barMenuManager.add(dumpFileAction);

		// creates a context menu with actions and adds it to the viewer control
		MenuManager menuMgr = new MenuManager();
		menuMgr.add(selectWorkspaceAction);
		menuMgr.add(dumpFileAction);

		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// associates double-click to dump file action
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				dumpFileAction.run();
			}
		});
	}

	/**
	 * Rebuilds the viewer content provider.
	 * 
	 * @param file
	 * @see WorkspaceContentProvider#inputChanged(Viewer, Object, Object)
	 */
	protected void setMetadataRoot(File file) {
		if (!file.isDirectory()) {
			ErrorUtil.showErrorMessage("Selected item is not a directory", "Error selecting workspace"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (!file.getName().equals(".metadata")) { //$NON-NLS-1$
			ErrorUtil.showErrorMessage("Selected directory must be .metadata", "Error selecting workspace"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		this.metadataPath = file;
		this.viewer.setInput(file);
	}

}