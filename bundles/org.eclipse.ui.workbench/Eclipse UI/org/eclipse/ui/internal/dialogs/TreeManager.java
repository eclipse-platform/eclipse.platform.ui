/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Manages a tree which provides "standard checkbox tree behavior". I.e. it
 * follows these rules:
 * <ol>
 * 	<li>If a check box is checked, all its children must be checked.</li>
 * 	<li>If a check box is unchecked, all its children must be unchecked.</li>
 * 	<li>If a check box is grayed, each of its children may be either checked or
 * 		unchecked, however, there must be one of each.</li>
 * 	<li>If a user checks a check box, its children or parents must change state
 * 		accordingly.</li>
 * </ol>
 * <p>
 * <b>Note:</b> be sure to call dispose()
 * </p>
 * @since 3.5
 *
 */
public class TreeManager {
	static final int CHECKSTATE_UNCHECKED = 0;
	static final int CHECKSTATE_GRAY = 1;
	static final int CHECKSTATE_CHECKED = 2;
	
	private static ICheckStateProvider checkStateProvider = null;
	private static IBaseLabelProvider labelProvider = null;
	private static ICheckStateListener viewerCheckListener = null;
	private static ITreeContentProvider treeContentProvider = null;
	
	private List listeners = new ArrayList();
	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * Instances of this interface will handle changes in the model
	 * representation of checkstates.
	 */
	public interface CheckListener {
		/**
		 * Invoked when a {@link TreeManager.TreeItem}'s check state has changed.
		 * 
		 * @param changedItem The item whose check state has changed
		 */
		public void checkChanged(TreeItem changedItem);
	}

	/**
	 * Implementation of {@link TreeManager.CheckListener} for use in a {@link CheckboxTreeViewer}.
	 */
	public static class ModelListenerForCheckboxTree implements CheckListener {
		private CheckboxTreeViewer treeViewer;
		public ModelListenerForCheckboxTree(TreeManager manager, CheckboxTreeViewer treeViewer) {
			this.treeViewer = treeViewer;
			manager.addListener(this);
		}
		
		public void checkChanged(TreeItem changedItem) {
			treeViewer.update(changedItem, null);
		}
	}

	/**
	 * Implementation of {@link TreeManager}CheckListener for use in a
	 * {@link CheckboxTableViewer}.
	 */
	public static class ModelListenerForCheckboxTable implements CheckListener {
		private CheckboxTableViewer tableViewer;
		public ModelListenerForCheckboxTable(TreeManager manager, CheckboxTableViewer tableViewer) {
			this.tableViewer = tableViewer;
			manager.addListener(this);
		}
		
		public void checkChanged(TreeItem changedItem) {
			tableViewer.update(changedItem, null);
		}
	}

	public static class ViewerCheckStateListener implements ICheckStateListener {
		public void checkStateChanged(CheckStateChangedEvent event) {
			Object checked = event.getElement();
			if(checked instanceof TreeItem) {
				((TreeItem)checked).setChangedByUser(true);
				((TreeItem)checked).setCheckState(event.getChecked());
			}
		}
	}

	/**
	 * An {@link ICheckStateProvider} which properly provides checkbox state on
	 * {@link TreeManager.TreeItem}s.
	 */
	public static class CheckStateProvider implements ICheckStateProvider {
		public boolean isChecked(Object element) {
			return ((TreeItem)element).checkState != CHECKSTATE_UNCHECKED;
		}
		
		public boolean isGrayed(Object element) {
			return ((TreeItem)element).checkState == CHECKSTATE_GRAY;
		}
	}

	/**
	 * A {@link IBaseLabelProvider} for {@link TreeManager.TreeItem}s.
	 */
	public static class TreeItemLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((TreeItem)element).getLabel();
		}

		public Image getImage(Object element) {
			return ((TreeItem)element).getImage();
		}
	}

	/**
	 * An {@link ITreeContentProvider} for {@link TreeManager.TreeItem}s - will completely build the
	 * tree structure represented by {@link TreeManager.TreeItem}s.
	 */
	public static class TreeItemContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			return ((TreeItem)parentElement).getChildren().toArray();
		}

		public Object getParent(Object element) {
			return ((TreeItem)element).getParent();
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}

	/**
	 * @return	an {@link ICheckStateProvider} which will operate on
	 * 		{@link TreeItem}s
	 */
	public static ICheckStateProvider getCheckStateProvider() {
		if(checkStateProvider == null) {
			checkStateProvider = new CheckStateProvider();
		}
		return checkStateProvider;
	}
	
	/**
	 * @return	an {@link IBaseLabelProvider} which will provide the labels and
	 * 		images of {@link TreeItem}s
	 */
	public static IBaseLabelProvider getLabelProvider() {
		if(labelProvider == null) {
			labelProvider = new TreeItemLabelProvider();
		}
		return labelProvider;
	}
	
	/**
	 * @return	an {@link ITreeContentProvider} which will provide
	 * 		{@link TreeItem} content in tree format.
	 */
	public static ITreeContentProvider getTreeContentProvider() {
		if(treeContentProvider == null)
			treeContentProvider = new TreeItemContentProvider();
		return treeContentProvider;
	}
	
	/**
	 * @return	an {@link ICheckStateListener} which will respond to
	 * 		{@link CheckStateChangedEvent}s by updating the model to reflect
	 * 		them
	 */
	public ICheckStateListener getViewerCheckStateListener() {
		if(viewerCheckListener == null)
			viewerCheckListener = new ViewerCheckStateListener();
		return viewerCheckListener;
	}

	/**
	 * A single item in a tree of managed checkbox states.
	 */
	public class TreeItem {
		private String label;
		private ImageDescriptor imageDescriptor;
		private Image image;
		private TreeItem parent;
		private List children;
		private int checkState;
		
		private boolean changedByUser = false;
		
		public TreeItem(String label) {
			this.label = label;
			this.children = new ArrayList();
		}
		
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Image getImage() {
			if(image == null) {
				if(imageDescriptor == null) {
					return null;
				}
				image = resourceManager.createImage(imageDescriptor);
			}
			return image;
		}

		public void setImageDescriptor(ImageDescriptor imageDescriptor) {
			this.imageDescriptor = imageDescriptor;
		}
		
		public void addChild(TreeItem newChild) {
            newChild.parent = this;
            children.add(newChild);
            synchParents(newChild);
        }
		
		public List getChildren() {
			return children;
		}
		
		public TreeItem getParent() {
			return parent;
		}

        /**
         * An internal call that forwards the change events but does <b>not</b>
         * cause any iterative synchronization to take place.
         * 
         * @param newState
         */
        private void internalSetCheckState(int newState) {
			if (newState == checkState)
				return;
			
			checkState = newState;
			fireListeners(this);
        }

		/**
		 * External call to explicitly set the particular state of a {@link TreeManager.TreeItem}.
		 * This is usually a response to an SWT check changed event generated by a Tree/Table.
		 * 
		 * @param checked
		 */
		public void setCheckState(boolean checked) {
			int newState = checked ? CHECKSTATE_CHECKED : CHECKSTATE_UNCHECKED;
			if (checkState == newState)
				return;
			// Actually set the state and fire the CheckChangeEvent
			internalSetCheckState(newState);
			
			// Enforce the SWT rules for checked/gray behavior
			synchChildren(this);
			synchParents(this);
		}

		/**
		 * From the client's perspective the state is a boolean.
		 * 
		 * @return <code>true</code> if the state is not UNCHECKED
		 */
		public boolean getState() {
			return !(checkState == CHECKSTATE_UNCHECKED);
		}
		
		int getCheckState() {
			return checkState;
		}
		
		/**
		 * If the new state is not "GRAY" then force all children to match that
		 * state (recursively).
		 * 
		 * @param changedItem
		 */
		private void synchChildren(TreeItem changedItem) {
			int newState = changedItem.checkState;
			
			// if the new state is 'GRAY'
			if (newState != CHECKSTATE_GRAY) {
				for (Iterator iterator = changedItem.children.iterator(); iterator
						.hasNext();) {
					TreeItem curItem = (TreeItem) iterator.next();
					curItem.internalSetCheckState(newState);
					curItem.setChangedByUser(changedItem.isChangedByUser());
					
					synchChildren(curItem);
				}
			}
		}
		
		/**
		 * Set the parent's state based on the aggregate state of its children
		 * using the following rules:
		 * <ul>
		 * 	<li>All children checked...parent checked</li>
		 * 	<li>All children unchecked...parent unchecked</li>
		 * 	<li>else...parent GRAY</li>
		 * </ul>
		 * 
		 * @param changedItem
		 */
		private void synchParents(TreeItem changedItem) {
			if(changedItem.parent == null)
				return;
			
			int newState = changedItem.checkState;
			
			if (newState == CHECKSTATE_GRAY) {
				// if the new state is 'GRAY' then -ALL- the parents are gray
				while (changedItem.parent != null && changedItem.parent.checkState != CHECKSTATE_GRAY) {
					changedItem.parent.internalSetCheckState(CHECKSTATE_GRAY);
					changedItem = changedItem.parent;
				}
			} else {
				// compute the parent's state - checked if all children are
				// checked, unchecked if all children are unchecked, gray if
				// some of each
				boolean checkedFound = newState == CHECKSTATE_CHECKED;
				boolean uncheckedFound = newState == CHECKSTATE_UNCHECKED;
				for (Iterator i = changedItem.parent.children.iterator(); i.hasNext() && (!checkedFound || !uncheckedFound);) {
					TreeItem item = (TreeItem) i.next();
					switch(item.checkState) {
					case CHECKSTATE_CHECKED: {
						checkedFound = true;
						break;
					} case CHECKSTATE_GRAY: {
						checkedFound = uncheckedFound = true;
						break;
					} case CHECKSTATE_UNCHECKED: {
						uncheckedFound = true;
						break;
					}}
				}
				
				int oldState = changedItem.parent.checkState;
				if(checkedFound && uncheckedFound) {
					changedItem.parent.internalSetCheckState(CHECKSTATE_GRAY);
				} else if (checkedFound) {
					changedItem.parent.internalSetCheckState(CHECKSTATE_CHECKED);
				} else {
					changedItem.parent.internalSetCheckState(CHECKSTATE_UNCHECKED);
				}
				if(oldState != changedItem.parent.checkState) {
					synchParents(changedItem.parent);
				}
			}
		}

		/**
		 * @param changedByUser The changedByUser to set.
		 */
		public void setChangedByUser(boolean changedByUser) {
			this.changedByUser = changedByUser;
		}

		/**
		 * @return Returns the changedByUser.
		 */
		public boolean isChangedByUser() {
			return changedByUser;
		}
	}
	
	/**
	 * Creates a new {@link TreeManager}.
	 */
	public TreeManager() {
		listeners = new ArrayList();
	}

	/**
	 * Add a {@link CheckListener} whose {@link CheckListener#checkChanged(TreeManager.TreeItem)}
	 * method will be invoked when a {@link TreeItem} created in this {@link TreeManager} has a
	 * check state change.
	 * 
	 * @param listener
	 */
	public void addListener(CheckListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Provides a {@link CheckListener} which updates a viewer whenever the
	 * {@link TreeManager} model changes.
	 * @param viewer	The viewer whose check states will be appropriately
	 * 		updated on a change to the model.
	 * @return	The created {@link CheckListener}.
	 */
	public CheckListener getCheckListener(ICheckable viewer) {
		if(viewer instanceof CheckboxTreeViewer)
			return new ModelListenerForCheckboxTree(this, (CheckboxTreeViewer)viewer);
		if(viewer instanceof CheckboxTableViewer)
			return new ModelListenerForCheckboxTable(this, (CheckboxTableViewer)viewer);
		return null;
	}
	
	/**
	 * Sets up this {@link TreeManager} for standard interaction with the
	 * provided {@link CheckboxTreeViewer}. In particular:
	 * <ul>
	 * 	<li>Adds a Label Provider for {@link TreeItem}s which provides both
	 * 		labels and images.</li>
	 * 	<li>Adds a {@link CheckStateProvider} for {@link TreeItem}s.</li>
	 * 	<li>Adds an {@link IContentProvider} to build a tree from input
	 * 		{@link TreeItem}s.</li>
	 * 	<li>Adds an {@link ICheckStateListener} to the viewer to update the
	 * 		appropriate {@link TreeItem}s upon a check box state change in the
	 * 		viewer.</li>
	 * 	<li>Adds a {@link CheckListener} to the {@link TreeManager} which will
	 * 		automatically update the viewer on a {@link TreeItem} check state
	 * 		change.</li>
	 * </ul>
	 * @param viewer	the viewer to configure with this TreeManager.
	 */
	public void attachAll(CheckboxTreeViewer viewer) {
		viewer.setLabelProvider(getLabelProvider());
		viewer.setCheckStateProvider(getCheckStateProvider());
		viewer.setContentProvider(getTreeContentProvider());
		viewer.addCheckStateListener(getViewerCheckStateListener());
		getCheckListener(viewer);
	}
	
	/**
	 * Sets up this {@link TreeManager} for standard interaction with the
	 * provided {@link CheckboxTableViewer}. In particular:
	 * <ul>
	 * 	<li>Adds a Label Provider for {@link TreeItem}s which provides both
	 * 		labels and images.</li>
	 * 	<li>Adds a {@link CheckStateProvider} for {@link TreeItem}s.</li>
	 * 	<li>Adds an {@link IContentProvider} to build a tree from input
	 * 		{@link TreeItem}s.</li>
	 * 	<li>Adds an {@link ICheckStateListener} to the viewer to update the
	 * 		appropriate {@link TreeItem}s upon a check box state change in the
	 * 		viewer.</li>
	 * 	<li>Adds a {@link CheckListener} to the {@link TreeManager} which will
	 * 		automatically update the viewer on a {@link TreeItem} check state
	 * 		change.</li>
	 * </ul>
	 * @param viewer	the viewer to configure with this TreeManager.
	 */
	public void attachAll(CheckboxTableViewer viewer) {
		viewer.setLabelProvider(getLabelProvider());
		viewer.setCheckStateProvider(getCheckStateProvider());
		viewer.setContentProvider(getTreeContentProvider());
		viewer.addCheckStateListener(getViewerCheckStateListener());
		getCheckListener(viewer);
	}

	/**
	 * Dissociates a listener.
	 * @param listener	The listener to remove.
	 */
	public void removeListener(CheckListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Fires all listeners.
	 * @param item	The {@link TreeItem} that changed.
	 */
	private void fireListeners(TreeItem item) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			CheckListener listener = (CheckListener) i.next();
			listener.checkChanged(item);
		}
	}
	
	public void dispose() {
		resourceManager.dispose();
		resourceManager = null;
		listeners.clear();
		listeners = null;
	}
}
