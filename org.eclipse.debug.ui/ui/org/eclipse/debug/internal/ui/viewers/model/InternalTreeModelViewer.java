/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech (Wind River) - added support for a virtual tree model viewer (Bug 242489)
 *     Patrick Chuong (Texas Instruments) - added support for checkbox (Bug 286310)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.core.commands.Request;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IStateUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyTreePathContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;

/**
 * A tree viewer that displays a model.
 * 
 * @since 3.3
 */
public class InternalTreeModelViewer extends TreeViewer 
    implements ITreeModelViewer,  ITreeModelContentProviderTarget, ITreeModelLabelProviderTarget, ITreeModelCheckProviderTarget
{
	
	private IPresentationContext fContext;
	
    /**
     * Current column presentation or <code>null</code>
     */
    private IColumnPresentation fColumnPresentation = null;	
    
    /**
     * Map of columns presentation id to its visible columns id's (String[])
     * When a columns presentation is not in the map, default settings are used.
     */
    private Map fVisibleColumns = new HashMap();
    
    /**
     * Map of column id's to persisted sizes
     */
    private Map fColumnSizes = new HashMap();
    
    /**
     * Map of column presentation id's to an array of integers representing the column order
     * for that presentation, or <code>null</code> if default.
     */
    private Map fColumnOrder = new HashMap();
    
    /**
     * Map of column presentation id to whether columns should be displayed
     * for that presentation (the user can toggle columns on/off when a 
     * presentation is optional.
     */
    private Map fShowColumns = new HashMap();    
    
    /**
     * Item's tree path cache 
     */
    private static final String TREE_PATH_KEY = "TREE_PATH_KEY"; //$NON-NLS-1$
    
    /**
	 * Memento type for column sizes. Sizes are keyed by column presentation id 
	 */
	private static final String COLUMN_SIZES = "COLUMN_SIZES"; //$NON-NLS-1$
	/**
	 * Memento type for the column order for a presentation context.
	 * A memento is created for each column presentation
	 */
	private static final String COLUMN_ORDER = "COLUMN_ORDER";     //$NON-NLS-1$	
	/**
	 * Memento type for the visible columns for a presentation context.
	 * A memento is created for each column presentation keyed by column number
	 */
	private static final String VISIBLE_COLUMNS = "VISIBLE_COLUMNS";     //$NON-NLS-1$
	/**
	 * Memento type for whether columns are visible for a presentation context.
	 * Booleans are keyed by column presentation id
	 */
	private static final String SHOW_COLUMNS = "SHOW_COLUMNS";     //$NON-NLS-1$	
	/**
	 * Memento key for the number of visible columns in a VISIBLE_COLUMNS memento
	 * or for the width of a column
	 */
	private static final String SIZE = "SIZE";	 //$NON-NLS-1$
	/**
	 * Memento key prefix a visible column
	 */
	private static final String COLUMN = "COLUMN";	 //$NON-NLS-1$	    
	
	/**
	 * True while performing an insert... we allow insert with filters
	 */
	private boolean fInserting = false;
	
	/**
	 * Whether to notify the content provider when an element is unmapped
	 */
	private boolean fNotifyUnmap = true;
	
	/**
	 * Persist column sizes when they change.
	 * 
	 * @since 3.2
	 */
	class ColumnListener implements ControlListener {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
		 */
		public void controlMoved(ControlEvent e) {
			persistColumnOrder();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
		 */
		public void controlResized(ControlEvent e) {
			persistColumnSizes();
		}
	}
	
	private ColumnListener fListener = new ColumnListener();    

	/**
	 * Proxy to cell modifier/editor support
	 */
	class CellModifierProxy implements ICellModifier {
		
		private ICellModifier fModifier;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			IElementEditor editor = ViewerAdapterService.getElementEditor(element);
			if (editor != null) {
				fModifier = editor.getCellModifier(getPresentationContext(), element);
				if (fModifier != null) {
					if (fModifier.canModify(element, property)) {
						// install cell editor
						CellEditor cellEditor = editor.getCellEditor(getPresentationContext(), property, element, (Composite)getControl());
		                if (cellEditor != null) {
			                disposeCellEditors();
			                CellEditor[] newEditors = new CellEditor[getVisibleColumns().length];
			                for (int i = 0; i < newEditors.length; i++) {
								newEditors[i] = cellEditor;
							}
			                setCellEditors(newEditors);
			                return true;
		                }
					}
				}
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			if (fModifier != null) {
				return fModifier.getValue(element, property);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			if (fModifier != null) {
				if (element instanceof Item) {
					element = ((Item)element).getData();
				}
				fModifier.modify(element, property, value);
			}
		}
		
		/**
		 * Disposes client's column editor and cell editors
		 */
		protected void dispose() {
			fModifier = null;
			disposeCellEditors();
			setCellEditors(null);
		}

		/**
		 * Disposes current cell editors
		 */
		protected void disposeCellEditors() {
			CellEditor[] cellEditors = getCellEditors();
			if (cellEditors != null) {
				for (int i = 0; i < cellEditors.length; i++) {
					CellEditor editor = cellEditors[i];
					if (editor != null) {
						editor.dispose();
					}
				}
			}
		}
		
	}
	
	private CellModifierProxy fCellModifier;
	
	/**
	 * A handle to an element in a model.
	 */
	class VirtualElement {
		/**
		 * Tree item associated with the element, or <code>null</code> for the root element
		 */
		private TreeItem fItem;
		
		/**
		 * Model element (can be <code>null</code> until retrieved)
		 */
		private Object fElement;
						
		/**
		 * Associated label update or <code>null</code> if the element was already
		 * present in the tree. 
		 */
		private VirtualLabelUpdate fLabel;
		
		/**
		 * Children elements or <code>null</code> if none
		 */
		private VirtualElement[] fChildren = null;
		
		/**
		 * Whether this element would be filtered from the viewer
		 */
		private boolean fFiltered = false;
		
		/**
		 * Listens for update when populating this virtual element in the tree viewer
		 */
		class UpdateListener implements IViewerUpdateListener {
			
			/**
			 * The parent pending update 
			 */
			private TreePath fParentPath;
			
			/**
			 * The child index pending update
			 */
			private int fIndex;
			
			/**
			 * Whether the update has completed
			 */
			private boolean fDone = false;

			/**
			 * Constructs a new listener waiting 
			 * @param parentPath the parent path
			 * @param childIndex the current child index
			 */
			UpdateListener(TreePath parentPath, int childIndex) {
				fParentPath = parentPath;
				fIndex = childIndex;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener#updateComplete(org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
			 */
			public void updateComplete(IViewerUpdate update) {
				if (update instanceof IChildrenUpdate) {
					IChildrenUpdate cu = (IChildrenUpdate) update;
					if (cu.getElementPath().equals(fParentPath)) {
						if (fIndex >= cu.getOffset() && fIndex <= (cu.getOffset() + cu.getLength())) {
							fDone = true;
						}
					}
				}
			}
			
			/**
			 * Returns whether the update has completed.
			 * 
			 * @return whether the update has completed
			 */
			boolean isComplete() {
				return fDone;
			}
			
			public void updateStarted(IViewerUpdate update) {
			}
			public void viewerUpdatesBegin() {
			}
			public void viewerUpdatesComplete() {
			}			
		}
		
		/**
		 * Constructs a new virtual element for the given tree item and all
		 * of its expanded children. Has to be called in the UI thread. 
		 * The model is used to count unrealized elements.
		 * 
		 * Note only never re-use objects already in the tree as they may be out
		 * of synch.
		 * 
		 * @param item tree item
		 * @param model virtual model
		 * @param root subtree to consider or <code>null</code> if all
		 * @param indexes children to consider or <code>null</code>
		 */
		VirtualElement(TreeItem item, VirtualModel model, TreePath root, int[] indexes) {
			fItem = item;
			model.incVirtual();
			if (item.getExpanded()) {
				TreeItem[] items = item.getItems();
				fChildren = createChildren(items, model, root, indexes);
			}
		}
		
		/**
		 * Constructs a new virtual element for the given tree and all expanded
		 * children. The model is passed in to count unrealized elements.
		 * 
		 * @param tree tree
		 * @param model virtual model
		 * @param root subtree scope or <code>null</code> for all
		 * @param indexes child indexes to consider or <code>null</code>
		 */
		VirtualElement(Tree tree, VirtualModel model, TreePath root, int[] indexes) {
			fElement = tree.getData();
			TreeItem[] items = tree.getItems();
			if (items.length > 0) {
				fChildren = createChildren(items, model, root, indexes);
			}
		}
		
		/**
		 * Creates and returns children elements.
		 * 
		 * @param items tree items
		 * @param model model
		 * @param root subtree to consider or all if <code>null</code>
		 * @param indexes children of the root to consider or <code>null</code>
		 * @return children
		 */
		private VirtualElement[] createChildren(TreeItem[] items, VirtualModel model, TreePath root, int[] indexes) {
			VirtualElement[] kids = new VirtualElement[items.length];
			if (root == null || root.getSegmentCount() == 0) {
				if (indexes == null) {
					for (int i = 0; i < items.length; i++) {
						kids[i] = new VirtualElement(items[i], model, null, null); 
					}
				} else {
					for (int i = 0; i < indexes.length; i++) {
						int index = indexes[i];
						kids[index] = new VirtualElement(items[index], model, null, null);
					}
				}
			} else {
				for (int i = 0; i < items.length; i++) {
					TreeItem treeItem = items[i];
					if (treeItem.getData() != null) {
						TreePath path = getTreePathFromItem(treeItem);
						if (root.startsWith(path, null)) {
							if (root.getSegmentCount() > path.getSegmentCount()) {
								kids[i] = new VirtualElement(treeItem, model, root, indexes);
							} else {
								kids[i] = new VirtualElement(treeItem, model, null, indexes);
							}
							break;
						}
					}
				}
			}			
			return kids;
		}
		
		/**
		 * Returns whether this element would be filtered from the viewer.
		 * 
		 * @return whether filtered
		 */
		public boolean isFiltered() {
			return fFiltered;
		}
		
		/**
		 * Causes the tree item associated with this model element to be realized.
		 * Must be called in the UI thread.
		 * 
		 * @return tree path to associated element, or <code>null</code> if the operation
		 * fails
		 */
		public TreePath realize() {
			if (fItem.getData() != null) {
				return getTreePathFromItem(fItem);
			}
			int index = -1;
			TreePath parentPath = null;
			if (fItem.getParentItem() == null) {
				index = getTree().indexOf(fItem);
				parentPath = TreePath.EMPTY;
			} else {
				index = fItem.getParentItem().indexOf(fItem);
				parentPath = getTreePathFromItem(fItem.getParentItem());
			}
			UpdateListener listener = new UpdateListener(parentPath, index);
			addViewerUpdateListener(listener);
			((ILazyTreePathContentProvider)getContentProvider()).updateElement(parentPath, index);
			Display display = getTree().getDisplay();
			while (!listener.isComplete()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			removeViewerUpdateListener(listener);
			if (fItem.getData() != null) {
				return getTreePathFromItem(fItem);
			} 
			return null;
		}	
		
		/**
		 * Schedules updates to retrieve unrealized children of this node.
		 * 
		 * @param parentPath path to this element
		 * @param model model 
		 */
		void retrieveChildren(TreePath parentPath, VirtualModel model) {
			VirtualChildrenUpdate update = null;
			if (fChildren != null) {
				int prevModelIndex = Integer.MAX_VALUE;
				for (int i = 0; i < fChildren.length; i++) {
					VirtualElement element = fChildren[i];
					if (element == null) {
						if (update != null) {
							// non-consecutive updates, schedule and keep going
							model.scheduleUpdate(update);
							update = null;
						}
					} else {
						int modelIndex = ((ITreeModelContentProvider)getContentProvider()).viewToModelIndex(parentPath, i);
						if (update == null) {
							update = new VirtualChildrenUpdate(parentPath, this, model);
						} else if ((modelIndex - prevModelIndex) > 1) {
							// non-consecutive updates, schedule and keep going
							model.scheduleUpdate(update);
							update = new VirtualChildrenUpdate(parentPath, this, model);
						}
						update.add(modelIndex);
						prevModelIndex = modelIndex;
					}
				}
			}
			if (update != null) {
				model.scheduleUpdate(update);
			}
		}
		
		/**
		 * Sets the underlying model object.
		 * @param data the model object
		 */
		void setElement(Object data) {
			fElement = data;
		}
		
		/**
		 * Sets the label update associated with this element
		 * 
		 * @param update the new label update
		 */
		void setLabelUpdate(VirtualLabelUpdate update) {
			fLabel = update;
		}

		/**
		 * Returns the image for this element or <code>null</code> if none
		 * 
		 * @return image or <code>null</code> if none
		 */
		public Image getImage() {
			if (fLabel == null) {
				return fItem.getImage();
			} else {
				return ((ITreeModelLabelProvider)getLabelProvider()).getImage(fLabel.fImage);
			}
		}

		/**
		 * Returns the labels for the element - one for each column requested.
		 * 
		 * @return column labels
		 */
		public String[] getLabel() {
		    if (fLabel == null) {
		        String[] visibleColumns = getVisibleColumns();
		        String[] label = new String[visibleColumns == null ? 1 : visibleColumns.length];
		        for (int i = 0; i < label.length; i++) {
		            label[i] = fItem.getText(i);
		        }
		        return label;
		    }
			return fLabel.fText;

		}
		
		/**
		 * Returns the children of this element or <code>null</code> if none.
		 * 
		 * @return children or <code>null</code> if none
		 */
		public VirtualElement[] getChildren() {
			return fChildren;
		}
		
	}
	
	/**
	 * Common function for virtual updates.
	 */
	class VirtualUpdate extends Request implements IViewerUpdate {
		
		/**
		 * Path to the element being updated, or EMPTY for viewer input.
		 */
		private TreePath fPath;
		
		/**
		 * Element being updated.
		 */
		VirtualElement fVirtualElement;
		
		/**
		 * Associated virtual model
		 */
		VirtualModel fModel = null;
		
		/**
		 * Creates a new update based for the given element and model
		 * @param model the backing model
		 * @param element the element to update
		 * @param elementPath the path of the element
		 */
		public VirtualUpdate(VirtualModel model, VirtualElement element, TreePath elementPath) {
			fPath = elementPath;
			fModel = model;
			fVirtualElement = element;
		}
		
		/**
		 * Returns the associated virtual element.
		 * 
		 * @return virtual element
		 */
		protected VirtualElement getVirtualElement() {
			return fVirtualElement;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElement()
		 */
		public Object getElement() {
			if (fPath.getSegmentCount() == 0) {
				return getViewerInput();
			}
			return fPath.getLastSegment();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElementPath()
		 */
		public TreePath getElementPath() {
			return fPath;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getPresentationContext()
		 */
		public IPresentationContext getPresentationContext() {
			return InternalTreeModelViewer.this.getPresentationContext();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getViewerInput()
		 */
		public Object getViewerInput() {
			return InternalTreeModelViewer.this.getInput();
		}
		
	}
	
	/**
	 * Represents all expanded items in this viewer's tree. The model is virtual - i.e. not
	 * all items have their associated model elements/labels retrieved from the model. The
	 * model can be populated in a non-UI thread.
	 */
	class VirtualModel {
		
		/**
		 * Update requests in progress (children updates and label updates)
		 */
		private List fPendingUpdates = new ArrayList();
		
		/**
		 * Whether population has been canceled.
		 */
		private boolean fCanceled = false;
		
		/**
		 * Root element in the model.
		 */
		private VirtualElement fRoot = null;
		
		/**
		 * Progress monitor to use during retrieval of virtual elements
		 */
		private IProgressMonitor fMonitor = null;
		
		/**
		 * Column IDs to generate labels for or <code>null</code> if no columns.
		 */
		private String[] fColumnIds;
				
		/**
		 * Count of the number of virtual (unrealized) elements in this model
		 * when it was created.
		 */
		private int fVirtualCount = 0;
				
		/**
		 * Creates a virtual model for this tree viewer limited to the given
		 * subtrees. Includes the entire model if root is <code>null</code>.
		 * 
		 * @param root limits model to given subtree scope, or <code>null</code>
		 * @param childIndexes children of the root to consider or <code>null</code>
		 */
		VirtualModel(TreePath root, int[] childIndexes) {
			fRoot = new VirtualElement(getTree(), this, root, childIndexes);
		}
		
		/** 
		 * Increments the counter of virtual elements in the model.
		 */
		void incVirtual() {
			fVirtualCount++;
		}
		
		/**
		 * update progress monitor
		 * 
		 * @param work the number of units worked
		 */
		void worked(int work) {
			fMonitor.worked(work);
		}
		
		/**
		 * Schedules a children update.
		 * @param update the update to schedule
		 */
		private synchronized void scheduleUpdate(IChildrenUpdate update) {
			Object element = update.getElement();
			if (element == null) {
				element = getInput();
			}
			IElementContentProvider provider = ViewerAdapterService.getContentProvider(element);
			if (provider != null) {
				fPendingUpdates.add(update);
				provider.update(new IChildrenUpdate[]{update});
			}
		}	
		
		/**
		 * Populates this models elements that have not yet been retrieved from the model,
		 * returning all elements in the model, or <code>null</code> if canceled.
		 * 
		 * @param monitor progress monitor for progress reporting and for checking if canceled
		 * @param taskName used in progress monitor, for example "Find..."
		 * @param columnIds labels to generate or <code>null</code> if no columns are to be used
		 * @return model elements or <code>null</code> if canceled
		 */
		public VirtualElement populate(IProgressMonitor monitor, String taskName, String[] columnIds) {
			fMonitor = monitor;
			fColumnIds = columnIds;
			monitor.beginTask(taskName, fVirtualCount * 2);
			boolean done = false;
			fRoot.retrieveChildren(TreePath.EMPTY, this);
			synchronized (this) {
				done = fPendingUpdates.isEmpty();
			}
			while (!done) {
				synchronized (this) {
					try {
						wait(500);
					} catch (InterruptedException e) {
					}
				}
				if (monitor.isCanceled()) {
					cancel();
					return null;
				}
				synchronized (this) {
					done = fPendingUpdates.isEmpty();
				}	
			}
			monitor.done();
			return fRoot;
		}
		
		/**
		 * Cancels all pending updates.
		 */
		void cancel() {
			synchronized (this) {
				fCanceled = true;
				Iterator iterator = fPendingUpdates.iterator();
				while (iterator.hasNext()) {
					IViewerUpdate update = (IViewerUpdate) iterator.next();
					update.cancel();
				}
				fPendingUpdates.clear();
			}
		}
		private synchronized boolean isCanceled() {
			return fCanceled;
		}
		
		/**
		 * Notification the given children update is complete. Schedules associated label
		 * updates if the request or the population of the model has not been canceled.
		 * 
		 * @param update completed children update request
		 */
		synchronized void done(VirtualChildrenUpdate update) {
			if (!isCanceled()) {
				fPendingUpdates.remove(update);
				if (!update.isCanceled()) {
					VirtualElement[] children = update.fVirtualElement.fChildren;
					TreePath parent = update.getElementPath();
					IElementLabelProvider provider = null;
					List requests = new ArrayList();
					int start = update.getOffset();
					int end = start + update.getLength();
					for (int i = start; i < end; i++) {
						int viewIndex = ((ITreeModelContentProvider)getContentProvider()).modelToViewIndex(parent, i);
						VirtualElement proxy = children[viewIndex];
						if (proxy.fFiltered) {
							fMonitor.worked(1); // don't need the label, this one is already done
						} else {
							Object element = proxy.fElement;
							if (element != null) { // null indicates other updates coming later
								VirtualLabelUpdate labelUpdate = new VirtualLabelUpdate(update.fModel, proxy, parent.createChildPath(element));
								proxy.setLabelUpdate(labelUpdate);
								IElementLabelProvider next = ViewerAdapterService.getLabelProvider(element);
								if (next != null) {
									fPendingUpdates.add(labelUpdate);
								}
								if (provider == null) {
									provider = next;
									requests.add(labelUpdate);
								} else if (next != null) {
									if (provider.equals(next)) {
										requests.add(labelUpdate);
									} else {
										// schedule queued requests, label provider has changed
										provider.update((ILabelUpdate[])requests.toArray(new ILabelUpdate[requests.size()]));
										requests.clear();
										requests.add(labelUpdate);
										provider = next;
									}
								}
							}
						}
					}
					if (provider != null && !requests.isEmpty()) {
						provider.update((ILabelUpdate[])requests.toArray(new ILabelUpdate[requests.size()]));
					}					
				}
				notifyAll();
			}
		}
		
		/**
		 * Notification the given label update is complete. Updates progress reporting
		 * and updates pending updates.
		 * 
		 * @param update label update that was completed
		 */
		synchronized void done(VirtualLabelUpdate update) {
			if (!isCanceled()) {
				fPendingUpdates.remove(update);
				fMonitor.worked(1);
			}
			if (fPendingUpdates.isEmpty()) {
				notifyAll();
			}
		}
	}
	
	/**
	 * Request to update a range of children.
	 */
	class VirtualChildrenUpdate extends VirtualUpdate implements IChildrenUpdate {
		
		private int fOffset = -1;
		private int fLength = 0;
		
		/**
		 * @param parentPath the parent path
		 * @param parent the parent element
		 * @param model the model
		 */
		public VirtualChildrenUpdate(TreePath parentPath, VirtualElement parent, VirtualModel model) {
			super(model, parent, parentPath);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#getLength()
		 */
		public int getLength() {
			return fLength;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#getOffset()
		 */
		public int getOffset() {
			return fOffset;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#setChild(java.lang.Object, int)
		 */
		public void setChild(Object child, int offset) {
			int viewIndex = ((ITreeModelContentProvider)getContentProvider()).modelToViewIndex(getElementPath(), offset);
			VirtualElement virtualChild = getVirtualElement().fChildren[viewIndex];
			virtualChild.setElement(child);
			ITreeModelContentProvider provider = (ITreeModelContentProvider) getContentProvider();
			virtualChild.fFiltered = provider.shouldFilter(getElementPath(), child);
			if (!virtualChild.fFiltered) {
				virtualChild.retrieveChildren(getElementPath().createChildPath(child), fModel);
			}
			fModel.worked(1);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.core.commands.Request#done()
		 */
		public void done() {
			fModel.done(this);
		}

		/**
		 * @param i the new offset
		 */
		void add(int i) {
			if (fOffset == -1) {
				fOffset = i;
			}
			fLength++;
		}
		
	}
	
	class VirtualLabelUpdate extends VirtualUpdate implements ILabelUpdate {
		
		/**
		 * Constructs a label request for the given element;
		 * @param coordinator  the model
		 * @param element the element to update
		 * @param elementPath the element path
		 */
		public VirtualLabelUpdate(VirtualModel coordinator, VirtualElement element, TreePath elementPath) {
			super(coordinator, element, elementPath);
		}

		private String[] fText;
		private ImageDescriptor fImage;

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#getColumnIds()
		 */
		public String[] getColumnIds() {
			return fModel.fColumnIds;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setBackground(org.eclipse.swt.graphics.RGB, int)
		 */
		public void setBackground(RGB background, int columnIndex) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setFontData(org.eclipse.swt.graphics.FontData, int)
		 */
		public void setFontData(FontData fontData, int columnIndex) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setForeground(org.eclipse.swt.graphics.RGB, int)
		 */
		public void setForeground(RGB foreground, int columnIndex) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor, int)
		 */
		public void setImageDescriptor(ImageDescriptor image, int columnIndex) {
			fImage = image;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setLabel(java.lang.String, int)
		 */
		public void setLabel(String text, int columnIndex) {
			if (fText == null) {
				if (getColumnIds() == null) {
					fText = new String[1];
				} else {
					fText = new String[getColumnIds().length];
				}
			}
			fText[columnIndex] = text;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.core.commands.Request#done()
		 */
		public void done() {
			fModel.done(this);
		}

	}
	
	/**
	 * @param parent the parent composite
	 * @param style the widget style bits
	 * @param context the presentation context
	 */
	public InternalTreeModelViewer(Composite parent, int style, IPresentationContext context) {
		super(parent, style);
		if ((style & SWT.VIRTUAL) == 0) {
			throw new IllegalArgumentException("style must include SWT.VIRTUAL"); //$NON-NLS-1$
		}
		setUseHashlookup(true);
		fCellModifier = new CellModifierProxy();
		fContext = context;
		setContentProvider(createContentProvider());
		setLabelProvider(createLabelProvider());
		
		// A pop-up viewer is transient and does not automatically expand
		// and select elements up when requested by the model  
		if ((style & SWT.POP_UP) != 0) {
		    ((ITreeModelContentProvider)getContentProvider()).setModelDeltaMask(
		        ~ITreeModelContentProvider.CONTROL_MODEL_DELTA_FLAGS);
		}
        if ((style & SWT.CHECK) != 0) {
            context.setProperty(ICheckUpdate.PROP_CHECK, Boolean.TRUE);
        }
	}
	
	/**
	 * @return content provider for this tree viewer
	 */
	protected ITreeModelContentProvider createContentProvider()
	{
		return new TreeModelContentProvider();
	}
	
	/**
	 * @return label provider for this tree viewer
	 */
	protected ITreeModelLabelProvider createLabelProvider()
	{
		return new TreeModelLabelProvider(this);
	}
	
	/* (non-Javadoc)
	 * 
	 * Workaround for bug 159461: when an item is cleared it's label is cleared. To avoid
	 * flashing, restore its label to its previous value.
	 * 
	 * @see org.eclipse.jface.viewers.TreeViewer#hookControl(org.eclipse.swt.widgets.Control)
	 */
	protected void hookControl(Control control) {
		Tree treeControl = (Tree) control;
		treeControl.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				// to avoid flash, reset previous label data
				TreeItem item = (TreeItem) event.item;
				preserveItem(item);
			}
		});
		super.hookControl(control);
	}
	
	/**
	 * @param item the item 
	 */
	private void preserveItem(TreeItem item) {
		Object[] labels = (Object[]) item.getData(PREV_LABEL_KEY);
		if (labels != null) {
			for (int i = 0; i < labels.length; i++) {
				if (labels[i] != null) {
					item.setText(i, (String)labels[i]);
				}
			}
		}
		Object[] images = (Object[]) item.getData(PREV_IMAGE_KEY);
		if (images != null) {
			for (int i = 0; i < images.length; i++) {
				item.setImage(i, (Image) images[i]);
			}
		}
		Object[] fonts = (Object[]) item.getData(PREV_FONT_KEY);
		if (fonts != null) {
			for (int i = 0; i < fonts.length; i++) {
				item.setFont(i, (Font) fonts[i]);
			}
		}
		Object[] foregrounds = (Object[]) item.getData(PREV_FOREGROUND_KEY);
		if (foregrounds != null) {
			for (int i = 0; i < foregrounds.length; i++) {
				item.setForeground(i, (Color) foregrounds[i]);
			}
		}
		Object[] backgrounds = (Object[]) item.getData(PREV_BACKGROUND_KEY);
		if (backgrounds != null) {
			for (int i = 0; i < backgrounds.length; i++) {
				item.setBackground(i, (Color) backgrounds[i]);
			}
		}
        Boolean checked = (Boolean) item.getData(PREV_CHECKED_KEY);
        if (checked != null) {
            item.setChecked(checked.booleanValue());
	    }
        Boolean grayed = (Boolean) item.getData(PREV_GRAYED_KEY);
        if (grayed != null) {
            item.setGrayed(grayed.booleanValue());
        }
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.StructuredViewer#handleInvalidSelection
     * 
     * Override the default handler for invalid selection to allow model 
     * selection policy to select the new selection.
     */
	protected void handleInvalidSelection(ISelection selection, ISelection newSelection) {
	    IModelSelectionPolicy selectionPolicy = ViewerAdapterService.getSelectionPolicy(selection, getPresentationContext());
	    if (selectionPolicy != null) {
            while (!selection.equals(newSelection)) {
                ISelection temp = newSelection;
                selection = selectionPolicy.replaceInvalidSelection(selection, newSelection);
                if (selection == null) {
                    selection = TreeSelection.EMPTY;
                }
                if (!temp.equals(selection)) {
                    setSelectionToWidget(selection, false);
                    newSelection = getSelection();
                } else {
                    break;
                }
            }
	    }
	    super.handleInvalidSelection(selection, newSelection);
	}
        

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ContentViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
	 */
	protected void handleDispose(DisposeEvent event) {
		if (fColumnPresentation != null) {
			fColumnPresentation.dispose();
		}
		fCellModifier.dispose();
		
		super.handleDispose(event);
	}
	
	/**
	 * Returns this viewer's presentation context.
	 * 
	 * @return presentation context
	 */
	public IPresentationContext getPresentationContext() {
		return fContext;
	}
	
	protected void unmapElement(Object element, Widget widget) {
		if (fNotifyUnmap) {
			// TODO: should we update the filter with the "new non-identical element"?
		    ((ITreeModelContentProvider) getContentProvider()).unmapPath((TreePath) widget.getData(TREE_PATH_KEY));
		}
		super.unmapElement(element, widget);
	}
	
	protected void associate(Object element, Item item) {
		// see AbstractTreeViewer.associate(...)
		Object data = item.getData();
		if (data != null && data != element && equals(data, element)) {
			// elements are equal but not identical
			// -> being removed from map, but should not change filters
			try {
				fNotifyUnmap = false;
				super.associate(element, item);
			} finally {
				fNotifyUnmap = true;
			}
		} else {
			super.associate(element, item);
		}
	}

	/* (non-Javadoc)
	 * 
	 * We need tree paths when disposed/unmapped in any order so cache the tree path.
	 * 
	 * @see org.eclipse.jface.viewers.TreeViewer#mapElement(java.lang.Object, org.eclipse.swt.widgets.Widget)
	 */
	protected void mapElement(Object element, Widget widget) {
		super.mapElement(element, widget);
		if (widget instanceof Item) {
			widget.setData(TREE_PATH_KEY, getTreePathFromItem((Item)widget));
		} else {
			widget.setData(TREE_PATH_KEY, ModelContentProvider.EMPTY_TREE_PATH);
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * Override because we allow inserting with filters present.
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#insert(java.lang.Object, java.lang.Object, int)
	 */
	public void insert(Object parentElementOrTreePath, Object element, int position) {
		try {
			fInserting = true;
			super.insert(parentElementOrTreePath, element, position);
		} finally {
			fInserting = false;
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * Override because we allow inserting with filters present.
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#hasFilters()
	 */
	protected boolean hasFilters() {
		if (fInserting) {
			return false;
		}
		return super.hasFilters();
	}
	
	protected void unmapAllElements() {
	    // Do nothing when called from StructuredViewer.setInput(), to avoid 
	    // clearing elements before viewer state is saved.
	    // Bug 326917
	    if (getControl().isDisposed()) {
	        unmapAllElements();
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	protected void inputChanged(Object input, Object oldInput) {
        ((ITreeModelContentProvider)getContentProvider()).inputAboutToChange(this, oldInput, input);
        // Clear items map now that we've called inputAboutToChange.
        // Bug 326917
        super.unmapAllElements();
		super.inputChanged(input, oldInput);
		resetColumns(input);
	}

	/**
     * Configures the columns for the given viewer input.
     * 
     * @param input the viewer input
     */
    protected void resetColumns(Object input) {
    	if (input != null) {
    		// only change columns if the input is non-null (persist when empty)
	    	IColumnPresentationFactory factory = ViewerAdapterService.getColumnPresentationFactory(input);
	    	PresentationContext context = (PresentationContext) getPresentationContext();
	    	String type = null;
	    	if (factory != null) {
	    		type = factory.getColumnPresentationId(context, input);
	    	}
			if (type != null && factory != null) {
				if (fColumnPresentation != null) {
					if (!fColumnPresentation.getId().equals(type)) {
						// dispose old, create new
						fColumnPresentation.dispose();
						fColumnPresentation = null;
					}
				}
				if (fColumnPresentation == null) {
					fColumnPresentation = factory.createColumnPresentation(context, input);
					if (fColumnPresentation != null) {
						fColumnPresentation.init(context);
						configureColumns();
					}
				}
			} else {
				if (fColumnPresentation != null) {
					fColumnPresentation.dispose();
					fColumnPresentation = null;
					configureColumns();
				}
			}
    	}
    }    
        
    /**
     * Configures the columns based on the current settings.
     */
    protected void configureColumns() {
    	if (fColumnPresentation != null) {
			IColumnPresentation build = null;
			if (isShowColumns(fColumnPresentation.getId())) {
				build = fColumnPresentation;
			}
			buildColumns(build);					
		} else {
			// get rid of columns
			buildColumns(null);
		}
    }
    
	/**
	 * Toggles columns on/off for the current column presentation, if any.
	 * 
	 * @param show whether to show columns if the current input supports
	 * 	columns
	 */
	public void setShowColumns(boolean show) {
		if (show) {
			if (!isShowColumns()) {
				fShowColumns.remove(fColumnPresentation.getId());
			}
		} else {
			if (isShowColumns()){
				fShowColumns.put(fColumnPresentation.getId(), Boolean.FALSE);
			}
		}
		refreshColumns();
	}
	
	/**
	 * Resets any persisted column size for the given columns
	 * @param columnIds the identifiers of the columns to reset
	 */
	public void resetColumnSizes(String[] columnIds) {
		for (int i = 0; i < columnIds.length; i++) {
			fColumnSizes.remove(columnIds[i]);
		}
	}
	
	/**
	 * Sets the id's of visible columns, or <code>null</code> to set default columns.
	 * Only affects the current column presentation.
	 * 
	 * @param ids visible columns
	 */
	public void setVisibleColumns(String[] ids) {
		if (ids != null && ids.length == 0) {
			ids = null;
		}
		IColumnPresentation presentation = getColumnPresentation();
		if (presentation != null) {
			fColumnOrder.remove(presentation.getId());
			fVisibleColumns.remove(presentation.getId());
			if (ids != null) {
				// put back in table if not default
				String[] columns = presentation.getInitialColumns();
				if (columns.length == ids.length) {
					for (int i = 0; i < columns.length; i++) {
						if (!ids[i].equals(columns[i])) {
							fVisibleColumns.put(presentation.getId(), ids);
							break;
						}
					}
				} else {
					fVisibleColumns.put(presentation.getId(), ids);
				}
			}
			PresentationContext presentationContext = (PresentationContext) getPresentationContext();
			presentationContext.setColumns(getVisibleColumns());
			refreshColumns();
		}
	}	
	
    /**
     * Refreshes the columns in the view, based on the viewer input.
     */
    protected void refreshColumns() {
    	configureColumns();
    	refresh();
    }
    
	/**
	 * Returns whether columns are being displayed currently.
	 * 
	 * @return if columns are being shown 
	 */
	public boolean isShowColumns() {
		if (fColumnPresentation != null) {
			return isShowColumns(fColumnPresentation.getId());
		}
		return false;
	}
	
	/**
	 * Returns whether columns can be toggled on/off for the current input.
	 * 
	 * @return whether columns can be toggled on/off for the current input
	 */
	public boolean canToggleColumns() {
		return fColumnPresentation != null && fColumnPresentation.isOptional();
	}
	
	protected boolean isShowColumns(String columnPresentationId) {
		Boolean bool = (Boolean) fShowColumns.get(columnPresentationId);
		if (bool == null) {
			return true;
		}
		return bool.booleanValue();
	}    
    
    /**
     * Creates new columns for the given presentation.
     * 
     * TODO: does this need to be asynchronous?
     * 
     * @param presentation the column presentation to build from
     */
    protected void buildColumns(IColumnPresentation presentation) {
    	// dispose current columns, persisting their weights
    	Tree tree = getTree();
		final TreeColumn[] columns = tree.getColumns();
		String[] visibleColumnIds = getVisibleColumns();
		// remove all listeners before disposing - see bug 223233
    	for (int i = 0; i < columns.length; i++) {
    		columns[i].removeControlListener(fListener);
    	}
    	for (int i = 0; i < columns.length; i++) {
			columns[i].dispose();
		}
    	PresentationContext presentationContext = (PresentationContext) getPresentationContext();
    	if (presentation != null) {	    	
	    	for (int i = 0; i < visibleColumnIds.length; i++) {
				String id = visibleColumnIds[i];
				String header = presentation.getHeader(id);
				// TODO: allow client to specify style
				TreeColumn column = new TreeColumn(tree, SWT.LEFT, i);
				column.setMoveable(true);
				column.setText(header);
				column.setWidth(1);
				ImageDescriptor image = presentation.getImageDescriptor(id);
				if (image != null) {
					column.setImage(((ITreeModelLabelProvider)getLabelProvider()).getImage(image));
				}
				column.setData(id);
			}
	    	int[] order = (int[]) fColumnOrder.get(presentation.getId());
	    	if (order != null) {
	    		tree.setColumnOrder(order);
	    	}
	    	tree.setHeaderVisible(true);
	    	tree.setLinesVisible(true);
	    	presentationContext.setColumns(visibleColumnIds);
	    	setColumnProperties(visibleColumnIds);
	    	setCellModifier(fCellModifier);
    	} else {
    		tree.setHeaderVisible(false);
    		tree.setLinesVisible(false);
    		presentationContext.setColumns(null);
    		setCellModifier(null);
    		setColumnProperties(null);
    	}
    	
    	int treeWidgetWidth = tree.getSize().x;
    	int avg = treeWidgetWidth;
    	if (visibleColumnIds != null)
    		avg /= visibleColumnIds.length;
    	
        if (avg == 0) {
            tree.addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent e) {
                    Tree tree2 = getTree();
                    String[] visibleColumns = getVisibleColumns();
                    if (visibleColumns != null) {
                    	int treeWidgetWidth1 = tree2.getSize().x;
						int avg1 = treeWidgetWidth1 / visibleColumns.length;
	                    initColumns(avg1, treeWidgetWidth1, visibleColumns);
                    }
                    tree2.removePaintListener(this);
                }
            });
        } else {
            initColumns(avg, treeWidgetWidth, visibleColumnIds);
        }
    }

    private void initColumns(int widthHint, int treeWidgetWidth, String[] visibleColumnIds) {
        TreeColumn[] columns = getTree().getColumns();
        for (int i = 0; i < columns.length; i++) {
            TreeColumn treeColumn = columns[i];
            Object colData = treeColumn.getData();
            String columnId = colData instanceof String ? (String) colData : null;            
            Integer width = (Integer) fColumnSizes.get(colData);
            if (width == null) {
            	int ans = getInitialColumnWidth(columnId, treeWidgetWidth, visibleColumnIds);
            	if (ans == -1) {
            		treeColumn.setWidth(widthHint);
            	} else {
            		treeColumn.setWidth(ans);
            	}
            } else {
                treeColumn.setWidth(width.intValue());
            }
            treeColumn.addControlListener(fListener);
        }
    }    
    
	/**
	 * Returns the current column presentation for this viewer, or <code>null</code>
	 * if none.
	 * 
	 * @return column presentation or <code>null</code>
	 */
	public IColumnPresentation getColumnPresentation() {
		return fColumnPresentation;
	}
	
	/**
	 * Returns identifiers of the visible columns in this viewer, or <code>null</code>
	 * if there is currently no column presentation.
	 *  
	 * @return visible columns or <code>null</code>
	 */
	public String[] getVisibleColumns() {
		if (isShowColumns()) {
			IColumnPresentation presentation = getColumnPresentation();
			if (presentation != null) {
				String[] columns = (String[]) fVisibleColumns.get(presentation.getId());
				if (columns == null) {
					return presentation.getInitialColumns();
				} else {
				    String[] available = presentation.getAvailableColumns();
				    for (int i = 0; i < columns.length; i++) {
				        boolean columnAvailable = false;
				        for (int j = 0; j < available.length; j++) {
				            if (columns[i].equals(available[j])) columnAvailable = true;
				        }
				        
				        if (!columnAvailable || presentation.getHeader(columns[i]) == null) {
    				        // We found a column ID which is not in current list of available column IDs.
				            // Or the presentation cannot return a header title for the given column.
    				        // Clear out saved column data for given column presentation.
                            fVisibleColumns.remove(presentation.getId());
                            fColumnOrder.remove(presentation.getId());
                            fColumnSizes.remove(presentation.getId());
                            return presentation.getInitialColumns();
				        }
				    }
				}
				return columns;
			}
		}
		return null;
	}    
	
	/**
	 * Returns initial column width of a given column, or -1
	 * @param columnId column Id
	 * @param treeWidgetWidth tree widget width
	 * @param visibleColumnIds visible columns
	 *  
	 * @return column width
	 */
	public int getInitialColumnWidth(String columnId, int treeWidgetWidth, String[] visibleColumnIds) {
		if (isShowColumns()) {
			IColumnPresentation presentation = getColumnPresentation();
			if (presentation instanceof IColumnPresentation2) {
				int ans = ((IColumnPresentation2) presentation).getInitialColumnWidth(columnId, treeWidgetWidth, visibleColumnIds);
				return ans;
			}
		}
		return -1;
	}

	/**
     * Persists column sizes in cache
     */
    protected void persistColumnSizes() { 
		Tree tree = getTree();
		TreeColumn[] columns = tree.getColumns();
		for (int i = 0; i < columns.length; i++) {
			TreeColumn treeColumn = columns[i];
			Object id = treeColumn.getData();
			fColumnSizes.put(id, new Integer(treeColumn.getWidth()));
		}
    }
    
    /**
     * Persists column ordering
     */
    protected void persistColumnOrder() {
    	IColumnPresentation presentation = getColumnPresentation();
    	if (presentation != null) {
	    	Tree tree = getTree();
	    	int[] order = tree.getColumnOrder();
	    	if (order.length > 0) {
	    		for (int i = 0; i < order.length; i++) {
					if (i != order[i]) {
						// non default order
						fColumnOrder.put(presentation.getId(), order);
						return;
					}
				}
	    	}
	    	// default order
	    	fColumnOrder.remove(presentation.getId());
    	}
    }
    
	/**
	 * Save viewer state into the given memento.
	 * 
	 * @param memento the {@link IMemento} to save to
	 */
	public void saveState(IMemento memento) {
		if (!fColumnSizes.isEmpty()) {
			Iterator iterator = fColumnSizes.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				IMemento sizes = memento.createChild(COLUMN_SIZES, (String)entry.getKey());
				sizes.putInteger(SIZE, ((Integer)entry.getValue()).intValue());
			}
		}
		if (!fShowColumns.isEmpty()) {
			Iterator iterator = fShowColumns.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				IMemento sizes = memento.createChild(SHOW_COLUMNS, (String)entry.getKey());
				sizes.putString(SHOW_COLUMNS, ((Boolean)entry.getValue()).toString());
			}			
		}
		if (!fVisibleColumns.isEmpty()) {
			Iterator iterator = fVisibleColumns.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				String id = (String) entry.getKey();
				IMemento visible = memento.createChild(VISIBLE_COLUMNS, id);
				String[] columns = (String[]) entry.getValue();
				visible.putInteger(SIZE, columns.length);
				for (int i = 0; i < columns.length; i++) {
					visible.putString(COLUMN+Integer.toString(i), columns[i]);
				}
			}
		}
		if (!fColumnOrder.isEmpty()) {
			Iterator iterator = fColumnOrder.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				String id = (String) entry.getKey();
				IMemento orderMemento = memento.createChild(COLUMN_ORDER, id);
				int[] order = (int[]) entry.getValue();
				orderMemento.putInteger(SIZE, order.length);
				for (int i = 0; i < order.length; i++) {
					orderMemento.putInteger(COLUMN+Integer.toString(i), order[i]);
				}
			}
		}
		// save presentation context properties
		IPresentationContext context = getPresentationContext();
		if (context instanceof PresentationContext) {
			PresentationContext pc = (PresentationContext) context;
			pc.saveProperites(memento);
			
		}
	}    
	
	/**
	 * Initializes viewer state from the memento
	 * 
	 * @param memento the {@link IMemento} to read from
	 */
	public void initState(IMemento memento) {
		IMemento[] mementos = memento.getChildren(COLUMN_SIZES);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Integer size = child.getInteger(SIZE);
			if (size != null) {
				fColumnSizes.put(id, size);
			}
		}
		mementos = memento.getChildren(SHOW_COLUMNS);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Boolean bool = Boolean.valueOf(child.getString(SHOW_COLUMNS));
			if (!bool.booleanValue()) {
				fShowColumns.put(id, bool);
			}
		}
		mementos = memento.getChildren(VISIBLE_COLUMNS);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Integer integer = child.getInteger(SIZE);
			if (integer != null) {
				int length = integer.intValue();
				String[] columns = new String[length];
				for (int j = 0; j < length; j++) {
					columns[j] = child.getString(COLUMN+Integer.toString(j));
				}
				fVisibleColumns.put(id, columns);
			}
		}
		mementos = memento.getChildren(COLUMN_ORDER);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Integer integer = child.getInteger(SIZE);
			if (integer != null) {
				int length = integer.intValue();
				int[] order = new int[length];
				for (int j = 0; j < length; j++) {
					order[j] = child.getInteger(COLUMN+Integer.toString(j)).intValue();
				}
				fColumnOrder.put(id, order);
			}
		}
		// restore presentation context properties
		// save presentation context properties
		IPresentationContext context = getPresentationContext();
		if (context instanceof PresentationContext) {
			PresentationContext pc = (PresentationContext) context;
			pc.initProperties(memento);
		}
	}
	
	/**
	 * Returns whether the candidate selection should override the current
	 * selection.
	 * @param current the current selection
	 * @param candidate the candidate for the new selection
	 * @return if the current selection should be replaced with the candidate selection
	 */
	public boolean overrideSelection(ISelection current, ISelection candidate) {
		IModelSelectionPolicy selectionPolicy = ViewerAdapterService.getSelectionPolicy(current, getPresentationContext());
		if (selectionPolicy == null) {
			return true;
		}
		if (selectionPolicy.contains(candidate, getPresentationContext())) {
			return selectionPolicy.overrides(current, candidate, getPresentationContext());
		}
		return !selectionPolicy.isSticky(current, getPresentationContext());
	}

	/* (non-Javadoc)
	 * 
	 * Consider selection policy
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		if (!overrideSelection(getSelection(), selection)) {
			return;
		}
		super.setSelection(selection, reveal);
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean, boolean)
	 */
	public void setSelection(ISelection selection, boolean reveal, boolean force) {
		trySelection(selection, reveal, force);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer#trySelection(org.eclipse.jface.viewers.ISelection, boolean, boolean)
	 */
	public boolean trySelection(ISelection selection, boolean reveal, boolean force) {
		if (force || overrideSelection(getSelection(), selection)) {
			super.setSelection(selection, reveal);
			return true;
		}
		return false;
	}
	/**
 	 * Registers the specified listener for view update notifications.
	 * 
	 * @param listener listener
	 */
	public void addViewerUpdateListener(IViewerUpdateListener listener) {
		((ITreeModelContentProvider)getContentProvider()).addViewerUpdateListener(listener);
	}
	
	/**
	 * Removes the specified listener from update notifications.
	 * 
	 * @param listener listener
	 */
	public void removeViewerUpdateListener(IViewerUpdateListener listener) {
	    ITreeModelContentProvider cp = (ITreeModelContentProvider)getContentProvider();
		if (cp !=  null) {
			cp.removeViewerUpdateListener(listener);
		}
	}
	
	/**
	 * Registers the given listener for model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void addModelChangedListener(IModelChangedListener listener) {
		((ITreeModelContentProvider)getContentProvider()).addModelChangedListener(listener); 
	}
	
	/**
	 * Unregisters the given listener from model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void removeModelChangedListener(IModelChangedListener listener) {
	    ITreeModelContentProvider cp = (ITreeModelContentProvider)getContentProvider();
		if (cp !=  null) {
			cp.removeModelChangedListener(listener);
		}
	}

    public void addStateUpdateListener(IStateUpdateListener listener) {
        ((ITreeModelContentProvider)getContentProvider()).addStateUpdateListener(listener);
    }
    
    public void removeStateUpdateListener(IStateUpdateListener listener) {
        ITreeModelContentProvider cp = (ITreeModelContentProvider)getContentProvider();
        if (cp !=  null) {
            cp.removeStateUpdateListener(listener);
        }
    }
    
	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void doUpdateItem(final Item item, Object element) {
		if (!(item instanceof TreeItem)) {
			return;
		}
		TreeItem treeItem = (TreeItem) item;
		if (treeItem.isDisposed()) {
			unmapElement(element, treeItem);
			return;
		}
		
		if ( !((ITreeModelLabelProvider)getLabelProvider()).update(getTreePathFromItem(item)) ) {
            if (element instanceof String) {
                item.setData(PREV_LABEL_KEY, new String[] { (String)element } );
            }		    
		}
		    

		// As it is possible for user code to run the event
		// loop check here.
		if (item.isDisposed()) {
			unmapElement(element, item);
		}
	}
	
	/**
	 * Collects all expanded items from this tree viewer and returns them as part of
	 * a virtual model. This must be called in a UI thread to traverse the tree items.
	 * The model can the be populated in a non-UI thread.
	 * 
	 * Alternatively a root element can be specified with a set of child indexes to
	 * consider. All children of the specified children are added to the model.
	 * 
	 * @param root subtree to consider or <code>null</code> if all
	 * @param childIndexes indexes of root element to consider, or <code>null</code> if all
	 * @return virtual model
	 */
	VirtualModel buildVirtualModel(TreePath root, int[] childIndexes) {
		return new VirtualModel(root, childIndexes);
	}
		
	public void addLabelUpdateListener(ILabelUpdateListener listener) {
	    ((ITreeModelLabelProvider)getLabelProvider()).addLabelUpdateListener(listener);
	}
	
	public void removeLabelUpdateListener(ILabelUpdateListener listener) {
	    if (!getControl().isDisposed()) {
	        ((ITreeModelLabelProvider)getLabelProvider()).removeLabelUpdateListener(listener);
	    }
	}
	
	/**
	 * Returns the item for the element at the given tree path or <code>null</code>
	 * if none.
	 * 
	 * @param path tree path
	 * @return item or <code>null</code>
	 */
	public Widget findItem(TreePath path) {
		if (path.getSegmentCount() == 0) {
			return getTree();
		}
		Widget[] items = super.findItems(path.getLastSegment());
		if (items.length == 1) {
			return items[0];
		}
		for (int i = 0; i < items.length; i++) {
			if (getTreePathFromItem((Item)items[i]).equals(path)) {
				return items[i];
			}
		}
		return null;
	}
	
	public Item[] getChildren(Widget widget) {
		return super.getChildren(widget);
	}
	
	/**
	 * Returns the tree path for the given item.
	 * @param item the item to compute the {@link TreePath} for
	 * @return {@link TreePath}
	 */
	protected TreePath getTreePathFromItem(Item item) {
		return super.getTreePathFromItem(item);
	}	

//**************************************************************************	
// These methods were copied from TreeViewer as a workaround for bug 183463:
// 		Expanded nodes in tree viewer flash on refresh
	
	/*
	 * (non-Javadoc)
	 * 
	 * workaround for bug 183463
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#internalRefreshStruct(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, boolean)
	 */
	protected void internalRefreshStruct(Widget widget, Object element,
			boolean updateLabels) {
		// clear all starting with the given widget
		if (widget instanceof Tree) {
			((Tree) widget).clearAll(true);
		} else if (widget instanceof TreeItem) {
			((TreeItem) widget).clearAll(true);
		}
		int index = 0;
		Widget parent = null;
		if (widget instanceof TreeItem) {
			TreeItem treeItem = (TreeItem) widget;
			parent = treeItem.getParentItem();
			if (parent == null) {
				parent = treeItem.getParent();
			}
			if (parent instanceof Tree) {
				index = ((Tree) parent).indexOf(treeItem);
			} else {
				index = ((TreeItem) parent).indexOf(treeItem);
			}
		}
		virtualRefreshExpandedItems(parent, widget, element, index);
	}	
	
	/**
	 * Traverses the visible (expanded) part of the tree and updates child
	 * counts.
	 * <p>
	 * workaround for bug 183463
	 * </p>
	 * @param parent the parent of the widget, or <code>null</code> if the widget is the tree
	 * @param widget the parent widget
	 * @param element the underlying object
	 * @param index the index of the widget in the children array of its parent, or 0 if the widget is the tree
	 */
	private void virtualRefreshExpandedItems(Widget parent, Widget widget, Object element, int index) {
		if (widget instanceof Tree) {
			if (element == null) {
				((Tree) widget).setItemCount(0);
				return;
			}
			virtualLazyUpdateChildCount(widget, getChildren(widget).length);
		} else if (((TreeItem) widget).getExpanded()) {
			// prevent SetData callback
			preserveItem((TreeItem)widget);
			//((TreeItem)widget).setText(" "); //$NON-NLS-1$
			virtualLazyUpdateWidget(parent, index);
		} else {
			return;
		}
		Item[] items = getChildren(widget);
		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			Object data = item.getData();
			virtualRefreshExpandedItems(widget, item, data, i);
		}
	}
	
	/**
	 * workaround for bug 183463
	 * 
	 * Update the child count
	 * @param widget the widget
	 * @param currentChildCount the current child count
	 */
	private void virtualLazyUpdateChildCount(Widget widget, int currentChildCount) {
		TreePath treePath;
		if (widget instanceof Item) {
			treePath = getTreePathFromItem((Item) widget);
		} else {
			treePath = TreePath.EMPTY;
		}
		((ILazyTreePathContentProvider) getContentProvider())
				.updateChildCount(treePath, currentChildCount);
	}
	
	/**
	 * Update the widget at index.
	 * <p>
	 * workaround for bug 183463
	 * </p>
	 * @param widget the widget
	 * @param index the index to update
	 */
	private void virtualLazyUpdateWidget(Widget widget, int index) {
		TreePath treePath;
		if (widget instanceof Item) {
			if (widget.getData() == null) {
				// we need to materialize the parent first
				// see bug 167668
				// however, that would be too risky
				// see bug 182782 and bug 182598
				// so we just ignore this call altogether
				// and don't do this: virtualMaterializeItem((TreeItem) widget);
				return;
			}
			treePath = getTreePathFromItem((Item) widget);
		} else {
			treePath = TreePath.EMPTY;
		}
		((ILazyTreePathContentProvider) getContentProvider())
				.updateElement(treePath, index);
	}	
	
//**************************************************************************    
// Another couple of methods copied from TreeViewer to workaround the UI bug 266189.
// 	
    protected void createChildren(Widget widget) {
        Object element = widget.getData();
        if (element == null && widget instanceof TreeItem) {
            // parent has not been materialized
            virtualMaterializeItem((TreeItem) widget);
            // try getting the element now that updateElement was called
            element = widget.getData();
        }
        if (element ==  null) {
            // give up because the parent is still not materialized
            return;
        }
        Item[] children = getChildren(widget);
        if (children.length == 1 && children[0].getData() == null) {
            // found a dummy node
            virtualLazyUpdateChildCount(widget, children.length);
            children = getChildren(widget);
        }
        // DO NOT touch all children
        return;
    }

    private void virtualMaterializeItem(TreeItem treeItem) {
        if (treeItem.getData() != null) {
            // already materialized
            return;
        }

        int index;
        Widget parent = treeItem.getParentItem();
        if (parent == null) {
            parent = treeItem.getParent();
        }
        Object parentElement = parent.getData();
        if (parentElement != null) {
            if (parent instanceof Tree) {
                index = ((Tree) parent).indexOf(treeItem);
            } else {
                index = ((TreeItem) parent).indexOf(treeItem);
            }
            virtualLazyUpdateWidget(parent, index);
        }
    }


	/**
	 * Performs auto expand on an element at the specified path if the auto expand
	 * level dictates the element should be expanded.
	 * 
	 * @param elementPath tree path to element to consider for expansion
	 */
	public void autoExpand(TreePath elementPath) {
		int level = getAutoExpandLevel();
		if (level > 0 || level == ITreeModelViewer.ALL_LEVELS) {
			if (level == ITreeModelViewer.ALL_LEVELS || level >= elementPath.getSegmentCount()) {
				expandToLevel(elementPath, 1);
			}
		}
	}

    public int findElementIndex(TreePath parentPath, Object element) {
        Widget parentItem = findItem(parentPath);
        if (parentItem != null) {
            Item[] children = getChildren(parentItem);
            for (int i = 0; i < children.length; i++) {
                Item item = children[i];
                Object data = item.getData();
                if ( (element != null && element.equals(data)) || (element == null && data == null) ) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean getElementChildrenRealized(TreePath parentPath) {
        Widget parentItem = findItem(parentPath);
        if (parentItem != null) {
            Item[] children = getChildren(parentItem);
            for (int i = 0; i < children.length; i++) {
                if (children[i].getData() == null) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public Display getDisplay() {
        Control control = getControl();
        if (control != null) {
            return  control.getDisplay();
        }
        return null;
    }

    protected static final String[] STATE_PROPERTIES = new String[]{ IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE };
    
    public void update(Object element) {
        update(element, STATE_PROPERTIES);
    }
    
    /**
     * Label data cache keys
     * TODO: workaround for bug 159461
     */
    static String PREV_LABEL_KEY = "PREV_LABEL_KEY"; //$NON-NLS-1$
    static String PREV_IMAGE_KEY = "PREV_IMAGE_KEY"; //$NON-NLS-1$
    static String PREV_FONT_KEY = "PREV_FONT_KEY"; //$NON-NLS-1$
    static String PREV_FOREGROUND_KEY = "PREV_FOREGROUND_KEY"; //$NON-NLS-1$
    static String PREV_BACKGROUND_KEY = "PREV_BACKGROUND_KEY"; //$NON-NLS-1$
    static String PREV_CHECKED_KEY = "PREV_CHECKED_KEY"; //$NON-NLS-1$
    static String PREV_GRAYED_KEY = "PREV_GRAYED_KEY"; //$NON-NLS-1$

    public void setElementData(TreePath path, int numColumns, String[] labels, ImageDescriptor[] imageDescriptors,
        FontData[] fontDatas, RGB[] _foregrounds, RGB[] _backgrounds) 
    {
        Widget widget = findItem(path);
        String[] columnIds = getVisibleColumns();
        
        if (widget != null && widget instanceof TreeItem && !widget.isDisposed()) {
            TreeItem item = (TreeItem)widget;
            /*Object data = item.getData();
            int itemCount = item.getItemCount();
            item.clearAll(false);
            item.setData(data);
            item.setItemCount(itemCount);*/
            
            for (int i=0; i<numColumns; i++){
                // text might be null if the launch has been terminated
                item.setText(i,(labels[i] == null ? IInternalDebugCoreConstants.EMPTY_STRING : labels[i]));
            }
            item.setData(PREV_LABEL_KEY, labels);
            
            if (imageDescriptors == null) {
                for (int i=0; i<numColumns; i++){
                    item.setImage(i,null);
                }
                item.setData(PREV_IMAGE_KEY, null);
            } else {
                Image[] images = new Image[imageDescriptors.length];
                for (int i = 0; i < imageDescriptors.length; i++) {
                    images[i] = ((ITreeModelLabelProvider)getLabelProvider()).getImage(imageDescriptors[i]);
                }
                if (columnIds == null) {
                    item.setImage(images[0]);
                } else {
                    item.setImage(images);
                }
                item.setData(PREV_IMAGE_KEY, images);
            }
            
            if (_foregrounds == null) { 
                for (int i=0; i<numColumns; i++){
                    item.setForeground(i,null);
                }
                item.setData(PREV_FOREGROUND_KEY, null);
            } else {
                Color[] foregrounds = new Color[_foregrounds.length];
                for (int i = 0; i< foregrounds.length; i++) {
                    foregrounds[i] = ((ITreeModelLabelProvider)getLabelProvider()).getColor(_foregrounds[i]);
                }
                if (columnIds == null) {
                    item.setForeground(0,foregrounds[0]);
                } else {
                    for (int i = 0; i< foregrounds.length; i++) {
                        item.setForeground(i, foregrounds[i]);
                    }
                }
                item.setData(PREV_FOREGROUND_KEY, foregrounds);
            }
            
            if (_backgrounds == null) {
                for (int i=0; i<numColumns; i++){
                    item.setBackground(i,null);
                }
                item.setData(PREV_BACKGROUND_KEY, null);
            } else {
                Color[] backgrounds = new Color[_backgrounds.length];
                for (int i = 0; i< backgrounds.length; i++) {
                    backgrounds[i] = ((ITreeModelLabelProvider)getLabelProvider()).getColor(_backgrounds[i]);
                }
                if (columnIds == null) {
                    item.setBackground(0,backgrounds[0]);
                } else {
                    for (int i = 0; i< backgrounds.length; i++) {
                        item.setBackground(i, backgrounds[i]);
                    }
                }
                item.setData(PREV_BACKGROUND_KEY, backgrounds);
            }
            
            if (fontDatas == null) {
                for (int i=0; i<numColumns; i++){
                    item.setFont(i,null);
                }
                item.setData(PREV_FONT_KEY, null);
            } else {
                Font[] fonts = new Font[fontDatas.length];
                for (int i = 0; i < fontDatas.length; i++) {
                    fonts[i] = ((ITreeModelLabelProvider)getLabelProvider()).getFont(fontDatas[i]);
                }
                if (columnIds == null) {
                    item.setFont(0,fonts[0]);
                } else {
                    for (int i = 0; i < fonts.length; i++) {
                        item.setFont(i, fonts[i]);
                    }
                }
                item.setData(PREV_FONT_KEY, fonts);
            }
        }
    }
    
    public ViewerLabel getElementLabel(TreePath path, String columnId) {
        if (path.getSegmentCount() == 0) {
            return null;
        }
        
        int columnIdx = -1;
        String[] visibleColumns = getVisibleColumns();
        if (columnId != null && visibleColumns != null) {
            int i = 0;
            for (i = 0; i < visibleColumns.length; i++) {
                if (columnId.equals(getVisibleColumns()[i])) {
                    columnIdx = i;
                    break;
                }
            }
            if (i == visibleColumns.length) {
                return null;
            }
        } else {
            columnIdx = 0;
        }
        TreeItem item = (TreeItem)findItem(path);
        
        if (item != null) {
            ViewerLabel label = new ViewerLabel(item.getText(columnIdx), item.getImage(columnIdx));
            label.setFont(item.getFont(columnIdx));
            label.setBackground(item.getBackground(columnIdx));
            label.setForeground(item.getForeground(columnIdx));
            return label;
        }
        return null;
    }

    public void reveal(TreePath path, int index) {
        Widget item = findItem(path);
        TreeItem[] children = null;
        if (item instanceof TreeItem) {
            children = ((TreeItem)item).getItems();
        } else if (item instanceof Tree) {
            children = ((Tree)item).getItems();
        }
        if (children != null && index < children.length) {
            getTree().setTopItem(children[index]);
        }
    }
    
    public int getChildCount(TreePath path) {
        if (path.getSegmentCount() == 0) {
            return ((Tree)getControl()).getItemCount();
        } else {
            Widget[] items = internalFindItems(path);
            if (items.length > 0) {
                if (items[0] instanceof TreeItem) {
                    return ((TreeItem)items[0]).getItemCount();
                }
            }   
        }
        return -1;
    }
    
    public Object getChildElement(TreePath path, int index) {
        TreeItem childItem = null;
        if (path.getSegmentCount() == 0) {
            Tree tree = (Tree)getControl();
            try {
                childItem = tree.getItem(index);
            } catch (IllegalArgumentException e) {}
        } else {
            try {
                Widget[] items = internalFindItems(path);
                if (items.length > 0) {
                    if (items[0] instanceof TreeItem) {
                        childItem = ((TreeItem)items[0]).getItem(index);
                    }
                }
            } catch (IllegalArgumentException e) {}
        }
        if (childItem != null) {
            return childItem.getData();
        } 
        return null;
    }

    public TreePath getTopElementPath() {
        TreeItem topItem = ((Tree)getControl()).getTopItem();
        if (topItem != null && topItem.getData() != null) {
            return getTreePathFromItem(topItem);
        }
        return null;
    }
    
    public boolean saveElementState(TreePath path, ModelDelta delta, int flagsToSave) {
        Tree tree = (Tree) getControl();
        TreeItem[] selection = tree.getSelection();
        Set set = new HashSet();
        for (int i = 0; i < selection.length; i++) {
            set.add(selection[i]);
        }
        
        TreeItem[] items = null;
        Widget w = internalGetWidgetToSelect(path);
        if (w instanceof Tree) {
            delta.setChildCount(
                ((ITreeModelContentProvider)getContentProvider()).viewToModelCount(path, tree.getItemCount()));
            if ((flagsToSave & IModelDelta.EXPAND) != 0) {
                delta.setFlags(delta.getFlags() | IModelDelta.EXPAND);
            }
            items = tree.getItems(); 
        } else if (w instanceof TreeItem) {
            TreeItem item = (TreeItem)w;
            if (item.getExpanded()) {
                int itemCount = item.getData() != null ? item.getItemCount() : -1;
                delta.setChildCount(((ITreeModelContentProvider)getContentProvider()).viewToModelCount(path, itemCount));
                if ((flagsToSave & IModelDelta.EXPAND) != 0) {
                    delta.setFlags(delta.getFlags() | IModelDelta.EXPAND);
                }
            } else if ((flagsToSave & IModelDelta.COLLAPSE) != 0){
                delta.setFlags(delta.getFlags() | IModelDelta.COLLAPSE);
            }
            
            if (set.contains(item) && (flagsToSave & IModelDelta.SELECT) != 0) {
                delta.setFlags(delta.getFlags() | IModelDelta.SELECT);
            }
            items = ((TreeItem)w).getItems();
        }
        if (items != null && items.length != 0) {
            for (int i = 0; i < items.length; i++) {
                doSaveElementState(path, delta, items[i], set, i, flagsToSave);
            }
            return true;
        } else {
            return false;
        }
    }
    
    private void doSaveElementState(TreePath parentPath, ModelDelta delta, TreeItem item, Collection set, int index, int flagsToSave) {
        Object element = item.getData();
        if (element != null) {
            boolean expanded = item.getExpanded();
            boolean selected = set.contains(item);
            int flags = IModelDelta.NO_CHANGE;
            if (expanded && (flagsToSave & IModelDelta.EXPAND) != 0) {
                flags = flags | IModelDelta.EXPAND;
            } 
            if (!expanded && (flagsToSave & IModelDelta.COLLAPSE) != 0) {
                flags = flags | IModelDelta.COLLAPSE;
            }
            if (selected && (flagsToSave & IModelDelta.SELECT) != 0) {
                flags = flags | IModelDelta.SELECT;
            }
            if (expanded || flags != IModelDelta.NO_CHANGE) {
                int modelIndex = ((ITreeModelContentProvider)getContentProvider()).viewToModelIndex(parentPath, index);
                TreePath elementPath = parentPath.createChildPath(element);
                ModelDelta childDelta = delta.addNode(element, modelIndex, flags, -1);
                if (expanded) {
                    // Only get the item count if the item is expanded.  Getting
                    // item count triggers an update of the element (bug 335734).
                    int itemCount = item.getItemCount();                
                    int numChildren = ((ITreeModelContentProvider)getContentProvider()).viewToModelCount(elementPath, itemCount);
                    childDelta.setChildCount(numChildren);
                    TreeItem[] items = item.getItems();
                    for (int i = 0; i < items.length; i++) {
                        doSaveElementState(elementPath, childDelta, items[i], set, i, flagsToSave);
                    }
                }
            }
        }
    }
    
    public void updateViewer(IModelDelta delta) {
        ((ITreeModelContentProvider)getContentProvider()).updateModel(delta, ITreeModelContentProvider.ALL_MODEL_DELTA_FLAGS);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.ITreeModelCheckProvider#setElementChecked(org.eclipse.jface.viewers.TreePath, boolean, boolean)
     */
	public void setElementChecked(TreePath path, boolean checked, boolean grayed) {
	   	 Widget widget = findItem(path);
		 
		 if (widget != null && widget instanceof TreeItem && !widget.isDisposed()) {
	         TreeItem item = (TreeItem)widget;
	         
	         item.setChecked(checked);
	         item.setGrayed(grayed);
	         
	         item.setData(PREV_CHECKED_KEY, checked ? Boolean.TRUE : Boolean.FALSE);
             item.setData(PREV_GRAYED_KEY, grayed ? Boolean.TRUE : Boolean.FALSE);
		 }
	}
	
    public boolean getElementChecked(TreePath path) {
        Widget widget = findItem(path);
        
        if (widget != null && widget instanceof TreeItem && !widget.isDisposed()) {
            TreeItem item = (TreeItem)widget;
            
            return item.getChecked();
        }        
        return false;
    }

    /**
     * Retrieves the element's check box grayed state.
     * 
     * @param path the path of the element to set grayed
     * @return grayed
     */
    public boolean getElementGrayed(TreePath path) {
        Widget widget = findItem(path);
        
        if (widget != null && widget instanceof TreeItem && !widget.isDisposed()) {
            TreeItem item = (TreeItem)widget;
            
            return item.getGrayed();
        }        
        return false;
    }

    public boolean getHasChildren(Object elementOrTreePath) {
        if (elementOrTreePath instanceof TreePath && 
            ((TreePath)elementOrTreePath).getSegmentCount() == 0) 
        {
            return getTree().getItemCount() > 0;
        }
        
        Widget[] items = internalFindItems(elementOrTreePath);
        if (items != null && items.length > 0) {
            if (items[0] instanceof TreeItem) {
                return ((TreeItem)items[0]).getItemCount() > 0;
            } else {
                return ((Tree)items[0]).getItemCount() > 0;
            }
        }
        
        return false;
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#handleSelect(org.eclipse.swt.events.SelectionEvent)
	 */
	protected void handleSelect(SelectionEvent event) {
        super.handleSelect(event);

        TreeItem item = (TreeItem) event.item;
        if (item != null) { // item can be null when de-selected (bug 296703) 
	        Object element = item.getData();
	        IContentProvider contentProvider = getContentProvider();
	        if (element != null && contentProvider instanceof TreeModelContentProvider) {
	            TreePath path = getTreePathFromItem(item);
	
	            if (event.detail == SWT.CHECK) {
	                boolean checked = item.getChecked();	            	
	            	boolean accepted = false;
	        		IModelProxy elementProxy = ((TreeModelContentProvider) contentProvider).getElementProxy(path);
	        		if (elementProxy instanceof ICheckboxModelProxy) {
	        			accepted = ((ICheckboxModelProxy) elementProxy).setChecked(getPresentationContext(), getInput(), path, checked);
	        		}	            		
	
	        	    // if the listen rejects the change or there is not ICheckboxModelProxy, than revert the check state
	            	if (!accepted) {
	            		item.setChecked(!checked);
	            	} else {
	            	    item.setData(PREV_CHECKED_KEY, new Boolean(checked));
	            	}
	            } else {
		            ((TreeModelContentProvider) contentProvider).cancelRestore(path, IModelDelta.SELECT|IModelDelta.REVEAL);
	    		}
	        }
        }
	}
	
	protected void handleTreeExpand(TreeEvent event) {
        super.handleTreeExpand(event);
        IContentProvider contentProvider = getContentProvider();
        if (contentProvider instanceof TreeModelContentProvider && event.item.getData() != null) {
            TreePath path = getTreePathFromItem((TreeItem)event.item);
            ((TreeModelContentProvider) contentProvider).cancelRestore(path, IModelDelta.COLLAPSE);
        }
	}
	
	protected void handleTreeCollapse(TreeEvent event) {
	    super.handleTreeCollapse(event);
        IContentProvider contentProvider = getContentProvider();
        if (contentProvider instanceof TreeModelContentProvider && event.item.getData() != null) {
            TreePath path = getTreePathFromItem((TreeItem)event.item);
            ((TreeModelContentProvider) contentProvider).cancelRestore(path, IModelDelta.EXPAND);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget#clearSelectionQuiet()
	 */
	public void clearSelectionQuiet() {
		getTree().setSelection(new TreeItem[0]);
	}
}
