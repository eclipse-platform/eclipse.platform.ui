/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.viewers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxyFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelSelectionPolicyAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A viewer that retrieves labels and content asynchronously via adapters and supports
 * duplicate elements in the viewer. Retrieving conetnt and labels asynchrnously allows
 * for arbitrary latency without blocking the UI thread.
 * <p>
 * This viewer uses adapters to retreive labels and content rather than
 * a label provider and content provider. As such, the label provider for this viewer
 * is <code>null</code> by default. The content provider returned by this viewer is
 * non-<code>null</code> to conform to the viewer specification, but performs no 
 * useful function.
 * </p>
 * <p>
 * The selection in this viewer is also set asynchronously. When the selection is set,
 * the viewer attempts to perform the selection. If the elements in the specified selection
 * are not yet in the viewer, the portion of the selection that could not be honored
 * becomes a pending selection. As more elements are added to viewer, the pending selection
 * is attempted to be set.  
 * </p>
 * @since 3.2
 */
public abstract class AsynchronousModelViewer extends StructuredViewer {

	public static boolean DEBUG_CACHE;
	
	/**
	 * Model of elements for this viewer
	 */
	private AsynchronousModel fModel;

	/**
	 * Cache of images used for elements in this tree viewer. Label updates
	 * use the method <code>getImage(...)</code> to cache images for
	 * image descriptors. The images are disposed when this viewer is disposed.
	 */
	private Map fImageCache = new HashMap();

	/**
	 * Cache of the fonts used for elements in this tree viewer. Label updates
	 * use the method <code>getFont(...)</code> to cache fonts for
	 * FontData objects. The fonts are disposed with the viewer.
	 */
	private Map fFontCache = new HashMap();

	/**
	 * Cache of the colors used for elements in this tree viewer. Label updates
	 * use the method <code>getColor(...)</code> to cache colors for
	 * RGB values. The colors are disposed with the viewer.
	 */
	private Map fColorCache = new HashMap();

	/**
	 * The context in which this viewer is being used - i.e. what part it is contained
	 * in any any preference settings associated with it.
	 */
	private IPresentationContext fContext;

	private ISelection fPendingSelection;

	private ISelection fCurrentSelection;
	
	/**
	 * The update policy for this viewer.
	 */
	private IModelUpdatePolicy fUpdatePolicy;
	
	/**
	 * Creates a new viewer 
	 */
	protected AsynchronousModelViewer() {
		setContentProvider(new NullContentProvider());
	}
	
	/**
	 * Clients must call this methods when this viewer is no longer needed
	 * so it can perform cleanup.
	 */
	public synchronized void dispose() {
		Iterator images = fImageCache.values().iterator();
		while (images.hasNext()) {
			Image image = (Image) images.next();
			image.dispose();
		}
		fImageCache.clear();
		
		Iterator fonts = fFontCache.values().iterator();
		while (fonts.hasNext()) {
			Font font = (Font) fonts.next();
			font.dispose();
		}
		fFontCache.clear();
		
		Iterator colors = fColorCache.values().iterator();
		while (colors.hasNext()) {
			Color color = (Color) colors.next();
			color.dispose();
		}
		fColorCache.clear();
		
		if (fModel != null) {
			fModel.dispose();
		}
		if (fUpdatePolicy != null) {
			fUpdatePolicy.dispose();
		}
	}

	/**
	 * Updates all occurrences of the given element in this viewer.
	 * 
	 * @param element element to update
	 */
	public void update(Object element) {
		if (element == getInput()) {
			return; // the input is not displayed
		}
		Widget[] items = getWidgets(element);
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				updateLabel(element, items[i]);
			}
		}
	}
	
	/**
	 * Updates the label for a specific element and item.
	 * 
	 * @param element element to update
	 * @param item its associated item
	 */
	protected void updateLabel(Object element, Widget item) {
		if (item instanceof Item) {
			ModelNode node = getModel().getNode(item);
			if (node != null) {
				getModel().updateLabel(node);
			}
		}
	}
		
	/**
	 * Returns the presentation context to be used in update requests.
	 * Clients may override this method if required to provide special
	 * implementations of contexts.
	 * 
	 * @return presentation contenxt
	 */
	public IPresentationContext getPresentationContext() {
		return fContext;
	}

//	/**
//	 * Refreshes all occurrences of the given element in this tree, and visible
//	 * children.
//	 * 
//	 * @param element element to refresh
//	 */
//	public void refresh(final Object element) {
//		// TODO: preserving selection currently has to be UI thread
////		preservingSelection(new Runnable() {
////			public void run() {
//				internalRefresh(element);
////			}
////		});		
//	}	
	
	/**
	 * Returns the model proxy factory for the given element of <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapters for
	 * @return model proxy factory adapter or <code>null</code>
	 */
	protected IModelProxyFactoryAdapter getModelProxyFactoryAdapter(Object element) {
		IModelProxyFactoryAdapter adapter = null;
		if (element instanceof IModelProxyFactoryAdapter) {
			adapter = (IModelProxyFactoryAdapter) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IModelProxyFactoryAdapter) adaptable.getAdapter(IModelProxyFactoryAdapter.class);
		}
		return adapter;
	}

	/**
	 * Returns the widgets associated with the given element or
	 * <code>null</code>.
	 * 
	 * @param element element to retrieve widgets for
	 * @return widgets or <code>null</code> if none
	 */
	protected synchronized Widget[] getWidgets(Object element) {
        // TODO: we likely want to return all nodes and force mapping to widget
		if (element == null) {
			return null;
		}
		ModelNode[] nodes = getModel().getNodes(element);
        if (nodes == null) {
            return null;
        }
        List widgets = new ArrayList();
        for (int i = 0; i < nodes.length; i++) {
            Widget widget = nodes[i].getWidget();
            if (widget != null) {
                widgets.add(widget);
            }
        }
        if (widgets.isEmpty()) {
            return null;
        }
        return (Widget[]) widgets.toArray(new Widget[widgets.size()]);
	}
	
	/**
	 * Returns the element associated with the given widget or
	 * <code>null</code>.
	 * 
	 * @param widget widget to retrieve element for
	 * @return element or <code>null</code> if none
	 */
	protected synchronized Object getElement(Widget widget) {
		ModelNode node = getModel().getNode(widget);
		if (node != null) {
			return node.getElement();
		}
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#unmapAllElements()
	 */
	protected synchronized void unmapAllElements() {
		final AsynchronousModel model = getModel();
		if (model != null) {
			preservingSelection(new Runnable() {
				public void run() {
					ModelNode root = model.getModelNode(getControl());
					if (root != null) {
						ModelNode[] childrenNodes = root.getChildrenNodes();
						if (childrenNodes != null) {
							for (int i = 0; i < childrenNodes.length; i++) {
								nodeDisposed(childrenNodes[i]);
							}
						}
					}				
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	protected synchronized void inputChanged(Object input, Object oldInput) {
		if (fUpdatePolicy == null) {
			fUpdatePolicy = createUpdatePolicy();
            fUpdatePolicy.init(this);
		}
		if (fModel != null) {
			fModel.dispose();
		}
		fModel = createModel();
		fModel.init(input, getControl());
        refresh();		
	}
	
	/**
	 * Creaets a new emptyu model for this viewer that
	 * is *not* initialized.
	 * 
	 * @return a new model
	 */
	protected abstract AsynchronousModel createModel();

	public abstract IModelUpdatePolicy createUpdatePolicy();

	Image[] getImages(ImageDescriptor[] descriptors) {
		if (descriptors == null || descriptors.length == 0) {
			return new Image[0];
		}
		Image[] images = new Image[descriptors.length];
		for (int i = 0; i < images.length; i++) {
			images[i] = getImage(descriptors[i]);
		}
		return images;
	}
	
	/**
	 * Returns an image for the given image descriptor or <code>null</code>. Adds the image
	 * to a cache of images if it does not already exist. The cache is cleared when this viewer
	 * is disposed. 
	 * 
	 * @param descriptor image descriptor or <code>null</code>
	 * @return image or <code>null</code>
	 */
	Image getImage(ImageDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		Image image = (Image) fImageCache.get(descriptor);
		if (image == null) {
			image = new Image(getControl().getDisplay(), descriptor.getImageData());
			fImageCache.put(descriptor, image);
		}
		return image;
	}

	Font[] getFonts(FontData[] fontDatas) {
		if (fontDatas == null) {
			return new Font[0];
		}
		
		Font[] fonts = new Font[fontDatas.length];
		for (int i = 0; i < fonts.length; i++) {
			fonts[i] = getFont(fontDatas[i]);
		}
		return fonts;
	}
	
	/**
	 * Returns a font for the given font data or <code>null</code>. Adds the font to this viewer's font 
	 * cache which is disposed when this viewer is disposed.
	 * 
	 * @param fontData font data or <code>null</code>
	 * @return font font or <code>null</code>
	 */
	Font getFont(FontData fontData) {
		if (fontData == null) {
			return null;
		}
		Font font = (Font) fFontCache.get(fontData);
		if (font == null) {
			font = new Font(getControl().getDisplay(), fontData);
			fFontCache.put(fontData, font);
		}
		return font;
	}
	
	Color[] getColor(RGB[] rgb) {
		if (rgb == null) {
			return new Color[0];
		}
		Color[] colors = new Color[rgb.length];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = getColor(rgb[i]);
		}
		return colors;
	}
	/**
	 * Returns a color for the given RGB or <code>null</code>. Adds the color to this viewer's color 
	 * cache which is disposed when this viewer is disposed.
	 * 
	 * @param rgb RGB or <code>null</code>
	 * @return color or <code>null</code>
	 */
	Color getColor(RGB rgb) {
		if (rgb == null) {
			return null;
		}
		Color color = (Color) fColorCache.get(rgb);
		if (color == null) {
			color = new Color(getControl().getDisplay(), rgb);
			fColorCache.put(rgb, color);
		}
		return color;
	}
	
	/**
	 * Sets the context for this viewer. 
	 * 
	 * @param context
	 */
	public void setContext(IPresentationContext context) {
		fContext = context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
	 */
	protected Widget doFindItem(Object element) {
		Widget[] widgets = getWidgets(element);
		if (widgets != null && widgets.length > 0) {
			return widgets[0];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget, java.lang.Object, boolean)
	 */
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		updateLabel(element, item);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	protected void internalRefresh(Object element) {
		Widget[] items = getWidgets(element);
		if (items == null) {
			return;
		}
		for (int i = 0; i < items.length; i++) {
			internalRefresh(element, items[i]);
		}
	}
	
	/**
	 * Refreshes a specific occurrence of an element.
	 * 
	 * @param element element to update
	 * @param item item to update
	 */
	protected void internalRefresh(Object element, Widget item) {
		updateLabel(element, item);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
	 */
	public synchronized void setSelection(ISelection selection, boolean reveal) {
		setSelection(selection, reveal, false);
	}
	
	/**
	 * Sets the selection in this viewer.
	 * 
	 * @param selection new selection
	 * @param reveal whether to reveal the selection
	 * @param force whether to force the selection change without consulting the model
	 *  selection policy
	 */
	public synchronized void setSelection(ISelection selection, final boolean reveal, boolean force) {
		Control control = getControl();
		if (control == null || control.isDisposed()) {
			return;
		}
		if (!acceptsSelection(selection)) {
			selection = getEmptySelection();
		}
		if (!force && !overrideSelection(fCurrentSelection, selection)) {
			return;
		}
		
		fPendingSelection = selection;
		
		if (getControl().getDisplay().getThread() == Thread.currentThread()) {
			attemptSelection(reveal);
		} else {
			WorkbenchJob job = new WorkbenchJob("attemptSelection") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					attemptSelection(reveal);
					return Status.OK_STATUS;
				}
				
			};
			job.setSystem(true);
			job.schedule();
		}		
	}	
	
	
	/**
	 * Returns whether the candidate selection should override the current
	 * selection.
	 * 
	 * @param current
	 * @param curr
	 * @return
	 */
	protected boolean overrideSelection(ISelection current, ISelection candidate) {
		IModelSelectionPolicyAdapter selectionPolicy = getSelectionPolicy(current);
		if (selectionPolicy == null) {
			return true;
		}
		if (selectionPolicy.contains(candidate, getPresentationContext())) {
			return selectionPolicy.overrides(current, candidate, getPresentationContext());
		}
		return !selectionPolicy.isSticky(current, getPresentationContext());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelection()
	 */
	public ISelection getSelection() {
		Control control = getControl();
		if (control == null || control.isDisposed() || fCurrentSelection == null) {
			return StructuredSelection.EMPTY;
		}
		return fCurrentSelection;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#handleSelect(org.eclipse.swt.events.SelectionEvent)
	 */
	protected void handleSelect(SelectionEvent event) {
		// handle case where an earlier selection listener disposed the control.
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			updateSelection(newSelectionFromWidget());
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#handlePostSelect(org.eclipse.swt.events.SelectionEvent)
	 */
	protected void handlePostSelect(SelectionEvent e) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, newSelectionFromWidget());
		firePostSelectionChanged(event);
	}	
	
	/**
	 * Creates and returns a new seletion from this viewer, based on the selected
	 * elements in the widget.
	 * 
	 * @return a new selection
	 */
	protected abstract ISelection newSelectionFromWidget();
	
	/**
	 * Returns the selection policy associated with the given selection
	 * or <code>null</code> if none.
	 * 
	 * @param selection or <code>null</code>
	 * @return selection policy or <code>null</code>
	 */
	protected IModelSelectionPolicyAdapter getSelectionPolicy(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				return (IModelSelectionPolicyAdapter) adaptable.getAdapter(IModelSelectionPolicyAdapter.class);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(org.eclipse.jface.viewers.ISelection, boolean)
	 */
	final protected void setSelectionToWidget(ISelection selection, final boolean reveal) {
		// NOT USED
		throw new IllegalArgumentException("This method should not be called"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.List, boolean)
	 */
	final protected void setSelectionToWidget(List l, boolean reveal) {
		// NOT USED
		throw new IllegalArgumentException("This method should not be called"); //$NON-NLS-1$
	}	
		
	/**
	 * Attempts to update any pending selection.
	 * 
	 * @param reveal whether to reveal the selection
	 */
	protected void attemptSelection(boolean reveal) {
		ISelection currentSelection = null;
		synchronized (this) {
			if (fPendingSelection != null) {
				ISelection remaining = doAttemptSelectionToWidget(fPendingSelection, reveal);
				if (remaining.isEmpty()) {
					remaining = null;
				}
				if (!fPendingSelection.equals(remaining)) {
					fPendingSelection = remaining;
					currentSelection = newSelectionFromWidget();
					if (isSuppressEqualSelections() && currentSelection.equals(fCurrentSelection)) {
						return;
					}
				}
			}
		}
		if (currentSelection != null) {
			updateSelection(currentSelection);
		}
	}
	
	/**
	 * Controls whether selection change notification is sent even when
	 * successive selections are equal.
	 * 
	 * TODO: what we really want is to fire selection change on ACTIVATE model
	 * change, even when selection is the same.
	 * 
	 * @return whether to suppress change notification for equal successive
	 *         selections
	 */
	protected boolean isSuppressEqualSelections() {
		return true;
	}
	
	/**
	 * Attempts to selection the specified selection and returns a selection
	 * representing the portion of the selection that could not be honored
	 * and still needs to be selected.
	 * 
	 * @param selection selection to attempt
	 * @param reveal whether to reveal the selection
	 * @return remaining selection
	 */
	protected abstract ISelection doAttemptSelectionToWidget(ISelection selection, boolean reveal);
	
	/**
	 * Returns whether this viewer supports the given selection.
	 * 
	 * @param selection a selection
	 * @return whether this viewer supports the given selection
	 */
	protected abstract boolean acceptsSelection(ISelection selection);
	
	/**
	 * Returns an empty selection supported by this viewer.
	 * 
	 * @return an empty selection supported by this viewer
	 */
	protected abstract ISelection getEmptySelection();
	
	/**
	 * A content provider that does nothing.
	 */
	private class NullContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			return null;
		}
	}

	/**
	 * Notification that a presentation update has failed.
	 * Subclasses may override as required. The default implementation
	 * does nothing.
	 * 
	 * @param monitor monitor for the presentation request that failed
	 * @param status status of update
	 */
	protected void handlePresentationFailure(IAsynchronousRequestMonitor monitor, IStatus status) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#preservingSelection(java.lang.Runnable)
	 */
	protected synchronized void preservingSelection(Runnable updateCode) {
		if (fPendingSelection == null || fPendingSelection.isEmpty()) {
			ISelection oldSelection = null;
			try {
				// preserve selection
				oldSelection = fCurrentSelection;
				// perform the update
				updateCode.run();
			} finally {
				// restore selection
				if (oldSelection == null) {
					oldSelection = new StructuredSelection();
				}
				if (getControl().getDisplay().getThread() == Thread.currentThread()) {
					restoreSelection(oldSelection);
				} else {
					final ISelection tempSelection = oldSelection;
					WorkbenchJob job = new WorkbenchJob("attemptSelection") { //$NON-NLS-1$
						public IStatus runInUIThread(IProgressMonitor monitor) {
                            if (!getControl().isDisposed()) {
    							restoreSelection(tempSelection);
                            }
                            return Status.OK_STATUS;
						}
						
					};
					job.setSystem(true);
					job.schedule();
				}							
			}
		} else {
			updateCode.run();
		}
	}
	
	protected synchronized void restoreSelection(ISelection oldSelection) {
		doAttemptSelectionToWidget(oldSelection, false);
		// send out notification if old and new differ
		fCurrentSelection = newSelectionFromWidget();
		if (!fCurrentSelection.equals(oldSelection))
			handleInvalidSelection(oldSelection, fCurrentSelection);		
	}
    
	/**
	 * Sets the color attributes of the given widget.
	 * 
	 * @param widget the widget to update
	 * @param foreground foreground color of the widget or <code>null</code> if default
	 * @param background background color of the widget or <code>null</code> if default
	 */
	abstract void setColors(Widget widget, RGB foreground[], RGB background[]);
	
	/**
	 * Sets the label attributes of the given widget.
	 * 
	 * @param widget the widget to update
	 * @param text label text
	 * @param image label image or <code>null</code>
	 */
	abstract void setLabels(Widget widget, String[] text, ImageDescriptor[] image);
	
	/**
	 * Sets the font attributes of the given widget.
	 * 
	 * @param widget widget to update
	 * @param font font of the widget or <code>null</code> if default.
	 */
	abstract void setFonts(Widget widget, FontData[] font);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#updateSelection(org.eclipse.jface.viewers.ISelection)
	 */
	protected synchronized void updateSelection(ISelection selection) {
		fCurrentSelection = selection;
		super.updateSelection(selection);
	}
	

	
	/**
	 * Notification the given model proxy has been added to this viewer's model.
	 * 
	 * @param proxy
	 */
	protected void modelProxyAdded(IModelProxy proxy) {
		if (fUpdatePolicy instanceof IModelChangedListener) {
			proxy.addModelChangedListener((IModelChangedListener)fUpdatePolicy);
		}		
	}
	
	/**
	 * Notification the given model proxy has been removed from this viewer's model.
	 * 
	 * @param proxy
	 */
	protected void modelProxyRemoved(IModelProxy proxy) {
		if (fUpdatePolicy instanceof IModelChangedListener) {
			proxy.removeModelChangedListener((IModelChangedListener)fUpdatePolicy);
		}		
	}	
	
	/**
	 * Returns this viewer's model
	 * 
	 * @return model
	 */
	protected AsynchronousModel getModel() {
		return fModel;
	}	
    
    /**
     * A node has been disposed from the model.
     * 
     * @param node
     */
    protected synchronized void nodeDisposed(ModelNode node) {
       Widget widget = node.getWidget();
       if (widget instanceof Item) {
           widget.dispose();
       } else if (widget == getControl()) {
    	   // root control
    	   ModelNode[] nodes = node.getChildrenNodes();
    	   if (nodes != null) {
    		   for (int i = 0; i < nodes.length; i++) {
    			   nodeDisposed(nodes[i]);
    		   }
    	   }
       }
    }    
    
    /**
     * A node in the model has been updated
     * 
     * @param node
     */
    protected synchronized void nodeChanged(ModelNode node) {
       Widget widget = node.getWidget();
       if (widget != null) {
           internalRefresh(node.getElement(), widget);
       }
    }
    
	/**
	 * @return if there are any more pending updates in the viewer
	 */
	protected synchronized boolean hasPendingUpdates() {
        return getModel().hasPendingUpdates();
	}

	/**
	 * Notification from the model that the update for the given request
	 * has completed.
	 * 
	 * @param monitor
	 */
	protected void updateComplete(IAsynchronousRequestMonitor monitor) {
	}
	
    /**
     * Called when nodes are set in the model. The children may not have been retrieved
     * yet when the tree got the call to "set data".
     * 
     * @param parent
     * @param children
     */
    protected abstract void nodeChildrenSet(ModelNode parent, ModelNode[] children);
    
    /**
     * The children of a node have changed.
     * 
     * @param parent
     */
    protected abstract void nodeChildrenChanged(ModelNode parentNode);    
	
}
