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
package org.eclipse.debug.internal.ui.viewers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelSelectionPolicyFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
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
public abstract class AsynchronousViewer extends StructuredViewer implements Listener {
	
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
	private AbstractUpdatePolicy fUpdatePolicy;

	protected static final String OLD_LABEL = "old_label"; //$NON-NLS-1$
	protected static final String OLD_IMAGE = "old_image"; //$NON-NLS-1$

    /**
     * Map of parent nodes for which children were needed to "set data"
     * in the virtual widget. A parent is added to this map when we try go
     * get children but they aren't there yet. The children are retrieved
     * asynchronously, and later put back into the widgetry.
     * The value is an array of ints of the indicies of the children that 
     * were requested. 
     */	
	private Map fParentsPendingChildren = new HashMap();
	
	/**
	 * Creates a new viewer 
	 */
	protected AsynchronousViewer() {
		setContentProvider(new NullContentProvider());
		setUseHashlookup(true);
	}

	/**
	 * Hash lookup is required, don't let subclasses change behavior.
	 */
	public final void setUseHashlookup(boolean enable) {
		Assert.isTrue(enable);
		super.setUseHashlookup(enable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#hookControl(org.eclipse.swt.widgets.Control)
	 */
	protected void hookControl(Control control) {
		super.hookControl(control);
		control.addListener(SWT.SetData, this);
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
		if (fContext != null) {
			((PresentationContext)fContext).dispose();
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
		ModelNode[] nodes = getModel().getNodes(element);
		if (nodes != null) {
			for (int i = 0; i < nodes.length; i++) {
				ModelNode node = nodes[i];
				updateLabel(node);
			}
		}
	}
	
	/**
	 * Updates the label for a specific element (node) in the model.
	 * 
	 * @param node node to update
	 * @param item its associated item
	 */
	protected void updateLabel(ModelNode node) {
		if (!node.getElement().equals(getInput())) {
			getModel().updateLabel(node);
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#unmapAllElements()
	 */
	protected synchronized void unmapAllElements() {
		super.unmapAllElements();
		AsynchronousModel model = getModel();
		if (model != null) {
			model.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	protected synchronized void inputChanged(Object input, Object oldInput) {
		fParentsPendingChildren.clear();
		if (fUpdatePolicy == null) {
			fUpdatePolicy = createUpdatePolicy();
            fUpdatePolicy.init(this);
		}
		if (fModel != null) {
			fModel.dispose();
		}
		fModel = createModel();
		fModel.init(input);
		if (input != null) {
			mapElement(fModel.getRootNode(), getControl());
			getControl().setData(fModel.getRootNode().getElement());
		} else {
			unmapAllElements();
		}
        refresh();		
	}
	
	/**
	 * Creaets a new emptyu model for this viewer that
	 * is *not* initialized.
	 * 
	 * @return a new model
	 */
	protected abstract AsynchronousModel createModel();

	/**
	 * Creates and returns this viewers update policy.
	 * @return update policy
	 */
	public abstract AbstractUpdatePolicy createUpdatePolicy();

	Image[] getImages(ImageDescriptor[] descriptors) {
        if (descriptors == null || descriptors.length == 0) {
            String[] columns = getPresentationContext().getColumns();
            if (columns == null) {
                return new Image[1];
            } else {
                return new Image[columns.length];
            }
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
	protected Image getImage(ImageDescriptor descriptor) {
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

	protected Font[] getFonts(FontData[] fontDatas) {
		if (fontDatas == null || fontDatas.length == 0) {
            String[] columns = getPresentationContext().getColumns();
            if (columns == null) {
                return new Font[1];
            } else {
                return new Font[columns.length];
            }
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
	protected Font getFont(FontData fontData) {
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
	
	protected Color[] getColors(RGB[] rgb) {
        if (rgb == null || rgb.length == 0) {
            String[] columns = getPresentationContext().getColumns();
            if (columns == null) {
                return new Color[1];
            } else {
                return new Color[columns.length];
            }
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
	protected Color getColor(RGB rgb) {
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
		AsynchronousModel model = getModel();
		if (model != null) {
			if (element.equals(model.getRootNode())) {
				return doFindInputItem(element);
			}
			Widget[] widgets = findItems(element);
			if (widgets.length > 0) {
				return widgets[0];
			}
		}
		return null;
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
     */
    protected Widget doFindInputItem(Object element) {
    	if (element instanceof ModelNode) {
			ModelNode node = (ModelNode) element;
			if (node.getElement().equals(getInput())) {
				return getControl();
			}
		}
        return null;
    }	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget, java.lang.Object, boolean)
	 */
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	protected void internalRefresh(Object element) {
		// get the nodes in the model
		AsynchronousModel model = getModel();
		if (model != null) {
			ModelNode[] nodes = model.getNodes(element);
			if (nodes != null) {
				for (int i = 0; i < nodes.length; i++) {
					ModelNode node = nodes[i];
					// get the widget for the node
					Widget item = findItem(node);
					if (item != null) {
						internalRefresh(node);
					}
				}
			}
		}
	}
	
	/**
	 * Refreshes a specific occurrence of an element (a node).
	 * 
	 * @param node node to update
	 * 
	 * Subclasses should override and call super
	 */
	protected void internalRefresh(ModelNode node) {
		updateLabel(node);
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
		IModelSelectionPolicy selectionPolicy = getSelectionPolicy(current);
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
	protected IModelSelectionPolicy getSelectionPolicy(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				IModelSelectionPolicyFactoryAdapter factory =  (IModelSelectionPolicyFactoryAdapter) adaptable.getAdapter(IModelSelectionPolicyFactoryAdapter.class);
				if (factory != null) {
					return factory.createModelSelectionPolicyAdapter(adaptable, getPresentationContext());
				}
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
			firePostSelectionChanged(new SelectionChangedEvent(this, currentSelection));
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
					if (!oldSelection.equals(newSelectionFromWidget())) {
						restoreSelection(oldSelection);
					}
				} else {
					final ISelection tempSelection = oldSelection;
					WorkbenchJob job = new WorkbenchJob("attemptSelection") { //$NON-NLS-1$
						public IStatus runInUIThread(IProgressMonitor monitor) {
                            if (!getControl().isDisposed()) {
                				if (!tempSelection.equals(newSelectionFromWidget())) {
                					restoreSelection(tempSelection);
                				}
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
	protected abstract void setColors(Widget widget, RGB foreground[], RGB background[]);
	
	/**
	 * Sets the label attributes of the given widget.
	 * 
	 * @param widget the widget to update
	 * @param text label text
	 * @param image label image or <code>null</code>
	 */
	protected abstract void setLabels(Widget widget, String[] text, ImageDescriptor[] image);
	
	/**
	 * Sets the font attributes of the given widget.
	 * 
	 * @param widget widget to update
	 * @param font font of the widget or <code>null</code> if default.
	 */
	protected abstract void setFonts(Widget widget, FontData[] font);

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
    protected void nodeDisposed(ModelNode node) {
    	Widget widget = findItem(node);
    	if (widget != null) {
    		unmapElement(node);
    		widget.dispose();
    		ModelNode[] childrenNodes = node.getChildrenNodes();
    		if (childrenNodes != null) {
    			for (int i = 0; i < childrenNodes.length; i++) {
					nodeDisposed(childrenNodes[i]);
				}
			}
		}
	}

	/**
	 * A node in the model has been updated
	 * 
	 * @param node
	 */
	public void nodeChanged(ModelNode node) {
		Widget widget = findItem(node);
		if (widget != null) {
			widget.setData(node.getElement());
			internalRefresh(node);
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
	 * Called when nodes are set in the model. The children may not have been
	 * retrieved yet when the tree got the call to "set data".
	 * 
	 * @param parent
	 * @param children
	 */
	protected void nodeChildrenSet(ModelNode parent, ModelNode[] children) {
		int[] indicies = removePendingChildren(parent);
		Widget widget = findItem(parent);
		if (widget != null && !widget.isDisposed()) {
			if (indicies != null) {
				for (int i = 0; i < indicies.length; i++) {
					int index = indicies[i];
					Widget item = getChildWidget(widget, index);
					if (item != null) {
						if (index < children.length) {
							ModelNode childNode = children[index];
							mapElement(childNode, item);
							item.setData(childNode.getElement());
							internalRefresh(childNode);
						}
					}
				}
				setItemCount(widget, children.length);
			} else {
				setItemCount(widget, children.length);
			}
		}
		attemptPendingUpdates();
	}

    protected abstract void clear(Widget item);

	/**
	 * Returns the child widet at the given index for the given parent or
	 * <code>null</code>
	 * 
	 * @param parent
	 * @param index
	 * @return
	 */
	protected abstract Widget getChildWidget(Widget parent, int index);

	/**
	 * Sets the item count for a parent widget
	 * 
	 * @param parent
	 * @param itemCount
	 */
	protected abstract void setItemCount(Widget parent, int itemCount);

    /**
     * Attempt pending udpates. Subclasses may override but should call super.
     */
    protected void attemptPendingUpdates() {
    	attemptSelection(false);
    }

	/**
	 * The children of a node have changed.
	 * 
	 * @param parent
	 */
	protected void nodeChildrenChanged(ModelNode parentNode) {
		ModelNode[] childrenNodes = parentNode.getChildrenNodes();
		if (childrenNodes != null) {
			nodeChildrenSet(parentNode, childrenNodes);
		} else {
			Widget widget = findItem(parentNode);
			if (widget != null && !widget.isDisposed()) {
				int childCount = parentNode.getChildCount();
				setItemCount(widget, childCount);
				attemptPendingUpdates();
			}
		}
	}

    /**
     * Returns the node corresponding to the given widget or <code>null</code>
     * @param widget widget for which a node is requested
     * @return node or <code>null</code>
     */
    protected ModelNode findNode(Widget widget) {
        ModelNode[] nodes = getModel().getNodes(widget.getData());
        if (nodes != null) {
        	for (int i = 0; i < nodes.length; i++) {
				ModelNode node = nodes[i];
				Widget item = findItem(node);
				if (widget == item) {
					return node;
				}
			}
        }    	
        return null;
    }
    
    /**
     * Returns the item for the node or <code>null</code>
     * @param node
     * @return
     */
    protected Widget findItem(ModelNode node) {
    	return findItem((Object)node);
    }

    /**
     * Note that the child at the specified index was requested by a widget
     * when revealed but that the data was not in the model yet. When the data
     * becomes available, map it to its widget.
     * 
     * @param parent
     * @param index
     */
	protected synchronized void addPendingChildIndex(ModelNode parent, int index) {
	    int[] indicies = (int[]) fParentsPendingChildren.get(parent);
	    if (indicies == null) {
	        indicies = new int[]{index};
	    } else {
	        int[] next = new int[indicies.length + 1];
	        System.arraycopy(indicies, 0, next, 0, indicies.length);
	        next[indicies.length] = index;
	        indicies = next;
	    }
	    fParentsPendingChildren.put(parent, indicies);    	
	}
	
	/**
	 * Removes and returns and children indicies that were pending for the given
	 * parent node. May return <code>null</code>.
	 * 
	 * @param parent
	 * @return indicies of children that data were requested for or <code>null</code>
	 */
	protected int[] removePendingChildren(ModelNode parent) {
		return (int[]) fParentsPendingChildren.remove(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * A virtual item has been exposed in the control, map its data.
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(final Event event) {		
		Item item = (Item) event.item;
		restoreLabels(item);

		Widget parentItem = getParentWidget(event.item);
		int index = event.index;
       
		ModelNode[] nodes = getModel().getNodes(parentItem.getData());
		if (nodes != null) {
			for (int i = 0; i < nodes.length; i++) {
				ModelNode node = nodes[i];
				Widget widget = findItem(node);
				if (widget == parentItem) {
		        	ModelNode[] childrenNodes = node.getChildrenNodes();
		        	if (childrenNodes != null && index < childrenNodes.length) {
		        		final ModelNode child = childrenNodes[index];
		        		mapElement(child, event.item);
		        		event.item.setData(child.getElement());
		        		preservingSelection(new Runnable() {
		        		    public void run() {
		        		        internalRefresh(child);
		        		    }
		        		});
		        	} else {
		        		addPendingChildIndex(node, index);
		            }
		        	return;
				}
			}
        }
    }	

	protected abstract void restoreLabels(Item item);

	/**
	 * Returns the parent widget for the given widget or <code>null</code>
	 * 
	 * @param widget
	 * @return parent widget or <code>null</code>
	 */
	protected abstract Widget getParentWidget(Widget widget);

	/**
	 * Updates the children of the given node.
	 * 
	 * @param parent
	 *            node of which to update children
	 */
	protected void updateChildren(ModelNode parent) {
		getModel().updateChildren(parent);
	}
}
