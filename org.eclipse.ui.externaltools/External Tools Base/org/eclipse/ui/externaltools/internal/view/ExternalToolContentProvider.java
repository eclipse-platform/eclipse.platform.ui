package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolRegistry;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolType;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolTypeRegistry;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.externaltools.model.IStorageListener;

/**
 * Provides the external tools and types as content.
 */
public class ExternalToolContentProvider implements ITreeContentProvider {
	private static final Object[] EMPTY_LIST = new Object[0];
	
	private ExternalToolTypeRegistry typeRegistry;
	private ExternalToolRegistry toolRegistry;
	private StorageListener listener;

	/**
	 * Create a new external tools content provider.
	 * 
	 * @param shell the shell to use for displaying any errors
	 * 		when loading external tool definitions from storage.
	 */
	public ExternalToolContentProvider(Shell shell) {
		super();
		typeRegistry = ExternalToolsPlugin.getDefault().getTypeRegistry();
		toolRegistry = ExternalToolsPlugin.getDefault().getToolRegistry(shell);
		listener = new StorageListener();
		listener.provider = this;
	}

	/* (non-Javadoc)
	 * Method declared on ITreeContentProvider.
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ExternalToolTypeRegistry) {
			return typeRegistry.getToolTypes();
		}
		
		if (parentElement instanceof ExternalToolType) {
			ExternalToolType type = (ExternalToolType) parentElement;
			return toolRegistry.getToolsOfType(type.getId());
		}
		
		return EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * Method declared on ITreeContentProvider.
	 */
	public Object getParent(Object element) {
		if (element instanceof ExternalTool) {
			ExternalTool tool = (ExternalTool) element;
			return typeRegistry.getToolType(tool.getType());
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on ITreeContentProvider.
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof ExternalToolTypeRegistry) {
			return typeRegistry.getTypeCount() > 0;
		}
		
		if (element instanceof ExternalToolType) {
			ExternalToolType type = (ExternalToolType) element;
			return toolRegistry.getToolCountOfType(type.getId()) > 0;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * Method declared on IStructuredContentProvider.
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * Method declared on IContentProvider.
	 */
	public void dispose() {
		typeRegistry = null;
		toolRegistry = null;
		ExternalToolStorage.removeStorageListener(listener);
		listener.viewer = null;
		listener.provider = null;
	}

	/* (non-Javadoc)
	 * Method declared on IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput == null && newInput != null) {
			listener.viewer = (TreeViewer)viewer;
			listener.provider = this;
			ExternalToolStorage.addStorageListener(listener);
		}
		if (oldInput != null && newInput == null) {
			ExternalToolStorage.removeStorageListener(listener);
			listener.viewer = null;
			listener.provider = null;
		}
	}
	
	
	/**
	 * Internal listener for changes in the tool storage.
	 */
	private static class StorageListener implements IStorageListener {
		public TreeViewer viewer = null;
		public ITreeContentProvider provider = null;
		
		public void toolDeleted(ExternalTool tool) {
			if (provider == null)
				return;
				
			Object parentElement = provider.getParent(tool);
			refresh(parentElement, false);
		}
		
		public void toolCreated(ExternalTool tool) {
			if (provider == null)
				return;
				
			Object parentElement = provider.getParent(tool);
			refresh(parentElement, false);
		}
	
		public void toolModified(ExternalTool tool) {
			if (provider == null)
				return;
				
			refresh(tool, true);
		}
		
		public void toolsRefreshed() {
			refresh(null, true);
		}
		
		private void refresh(final Object element, final boolean allLabels) {
			if (viewer == null)
				return;
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (element == null)
							viewer.refresh();
						else
							viewer.refresh(element, allLabels);
					}
				});
			}
		}
	}
}
