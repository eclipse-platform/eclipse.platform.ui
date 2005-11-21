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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.elements.adapters.AsynchronousDebugLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.update.DefaultUpdatePolicy;
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
public abstract class AsynchronousViewer extends StructuredViewer {

	/**
	 * A map of elements to associated tree items or tree
	 */
	private Map fElementsToWidgets = new HashMap();

	/**
	 * Map of widgets to their data elements used to avoid requirement to access
	 * data in UI thread.
	 */
	private Map fWidgetsToElements = new HashMap();

	/**
	 * List of updates currently being performed.
	 */
	private List fPendingUpdates = new ArrayList();

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
	private IUpdatePolicy fUpdatePolicy;
	
	/**
	 * Cache of update policies keyed by element
	 */
	private Map fModelProxies = new HashMap();
	
	/**
	 * Creates a presentation adapter viewer 
	 */
	protected AsynchronousViewer() {
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
		
		disposeAllModelProxies();
		if (fUpdatePolicy != null) {
			fUpdatePolicy.dispose();
		}
		
		unmapAllElements();
		fPendingUpdates.clear();
	}

	/**
	 * Unintalls all update policies installed in this viewer
	 */
	private void disposeAllModelProxies() {
	    synchronized(fModelProxies) {
	        Iterator updatePolicies = fModelProxies.values().iterator();
	        while (updatePolicies.hasNext()) {
	            IModelProxy proxy = (IModelProxy)updatePolicies.next();
	            if (fUpdatePolicy instanceof IModelChangedListener) {
					proxy.removeModelChangedListener((IModelChangedListener)fUpdatePolicy);
				}	            
	            proxy.dispose();
	        }
	        
	        fModelProxies.clear();
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
			IAsynchronousLabelAdapter adapter = getLabelAdapter(element);
			if (adapter != null) {
				ILabelRequestMonitor labelUpdate = new LabelRequestMonitor(item, this);
				schedule(labelUpdate);
				adapter.retrieveLabel(element, getPresentationContext(), labelUpdate);
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
	 * Returns the label adapter for the given element or <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapter for
	 * @return presentation adapter or <code>null</code>
	 */
	protected IAsynchronousLabelAdapter getLabelAdapter(Object element) {
		IAsynchronousLabelAdapter adapter = null;
		if (element instanceof IAsynchronousLabelAdapter) {
			adapter = (IAsynchronousLabelAdapter) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IAsynchronousLabelAdapter) adaptable.getAdapter(IAsynchronousLabelAdapter.class);
		}
		// if no adapter, use default (i.e. model presentation)
		if (adapter == null) {
			return new AsynchronousDebugLabelAdapter();
		}
		return adapter;
	}	
	
	/**
	 * Returns the model proxy factory for the given element of <code>null</code> if none.
	 * 
	 * @param element element to retrieve adapters for
	 * @return model proxy factory adapter or <code>null</code>
	 */
	protected IModelProxyFactory getModelProxyFactoryAdapter(Object element) {
		IModelProxyFactory adapter = null;
		if (element instanceof IModelProxyFactory) {
			adapter = (IModelProxyFactory) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = (IModelProxyFactory) adaptable.getAdapter(IModelProxyFactory.class);
		}
		return adapter;
	}
	/**
	 * Cancels any conflicting updates for children of the given item, and
	 * schedules the new update.
	 * 
	 * @param update the update to schedule
	 */
	protected void schedule(IAsynchronousRequestMonitor update) {
		AsynchronousRequestMonitor absUpdate = (AsynchronousRequestMonitor) update;
		synchronized (fPendingUpdates) {
			Iterator updates = fPendingUpdates.listIterator();
			while (updates.hasNext()) {
				AsynchronousRequestMonitor pendingUpdate = (AsynchronousRequestMonitor) updates.next();
				if (absUpdate.contains(pendingUpdate)) {
					pendingUpdate.setCanceled(true);
					updates.remove();
				}
			}
			fPendingUpdates.add(update);
		}
	}

	/**
	 * Returns the widgets associated with the given element or
	 * <code>null</code>.
	 * 
	 * @param element element to retrieve widgets for
	 * @return widgets or <code>null</code> if none
	 */
	protected synchronized Widget[] getWidgets(Object element) {
		if (element == null) {
			return null;
		}
		return (Widget[]) fElementsToWidgets.get(element);
	}
	
	/**
	 * Returns the element associated with the given widget or
	 * <code>null</code>.
	 * 
	 * @param widget widget to retrieve element for
	 * @return element or <code>null</code> if none
	 */
	protected synchronized Object getElement(Widget widget) {
		return fWidgetsToElements.get(widget);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#unmapAllElements()
	 */
	protected synchronized void unmapAllElements() {
		Iterator iterator = fElementsToWidgets.keySet().iterator();
		while (iterator.hasNext()) {
			Object element = iterator.next();
			Widget[] widgets = getWidgets(element);
			if (widgets != null) {
				for (int i = 0; i < widgets.length; i++) {
					Widget widget = widgets[i];
					if (widget instanceof Item) {
						Item item = (Item) widget;
						item.dispose();
					}
				}
			}
		}
		fElementsToWidgets.clear();
		fWidgetsToElements.clear();
		disposeAllModelProxies();
	}

	/**
	 * Cancels all pending update requests.
	 */
	protected synchronized void cancelPendingUpdates() {
		Iterator updates = fPendingUpdates.iterator();
		while (updates.hasNext()) {
			IAsynchronousRequestMonitor update = (IAsynchronousRequestMonitor) updates.next();
			update.setCanceled(true);
		}
		fPendingUpdates.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	protected void inputChanged(Object input, Object oldInput) {
		if (fUpdatePolicy == null) {
			fUpdatePolicy = createUpdatePolicy();
		}
		cancelPendingUpdates();
	}

	/**
	 * Maps the given element to the given item. Installs the elememt's
	 * update policy if not already installed.
	 * 
	 * @param element model element
	 * @param item TreeItem or Tree
	 */
	protected void map(Object element, Widget item) {
		item.setData(element);
		Widget[] widgets = getWidgets(element);
		fWidgetsToElements.put(item, element);
		if (widgets == null) {
			fElementsToWidgets.put(element, new Widget[] { item });
		} else {
			Widget[] old = widgets;
			Widget[] items = new Widget[old.length + 1];
			System.arraycopy(old, 0, items, 0, old.length);
			items[old.length] = item;
			fElementsToWidgets.put(element, items);
		}
		installModelProxy(element);
	}
	
	/**
	 * Updates the cached information that maps the given element to the
	 * specified widget. This can be useful when an element is being remapped
	 * to an equal (but not identical) object.
	 * 
	 * @param element
	 * @param item
	 */
	protected void remap(Object element, Widget item) {
		item.setData(element);
		fWidgetsToElements.put(item, element);
		Object widgets = fElementsToWidgets.remove(element);
		fElementsToWidgets.put(element, widgets);
	}

	public IUpdatePolicy createUpdatePolicy() {
		DefaultUpdatePolicy policy = new DefaultUpdatePolicy();
		policy.init(this);
		return policy;
	}
	
	/**
	 * Installs the model proxy for the given element into this viewer
	 * if not already installed.
	 * 
	 * @param element element to install an update policy for
	 */
	public void installModelProxy(Object element) {
		synchronized (fModelProxies) {
			if (!fModelProxies.containsKey(element)) {
				IModelProxyFactory modelProxyFactory = getModelProxyFactoryAdapter(element);
				if (modelProxyFactory != null) {
					IModelProxy proxy = modelProxyFactory.createModelProxy(element, getPresentationContext());
					if (proxy != null) {
						proxy.init(getPresentationContext());
						fModelProxies.put(element, proxy);
						
						if (fUpdatePolicy instanceof IModelChangedListener) {
							proxy.addModelChangedListener((IModelChangedListener)fUpdatePolicy);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Uninstalls the model proxy installed for the given element, if any.
	 * 
	 * @param element
	 */
	protected void disposeModelProxy(Object element) {
		synchronized (fModelProxies) {
			IModelProxy proxy = (IModelProxy) fModelProxies.remove(element);
			if (proxy != null) {
				if (fUpdatePolicy instanceof IModelChangedListener) {
					proxy.removeModelChangedListener((IModelChangedListener)fUpdatePolicy);
				}
				proxy.dispose();
			}
		}
	}

	/**
	 * Removes the update from the pending updates list.
	 * 
	 * @param update
	 */
	protected void updateComplete(IAsynchronousRequestMonitor update) {
		synchronized (fPendingUpdates) {
			fPendingUpdates.remove(update);
		}
	}

	/**
	 * Unmaps the given item. Does not dispose of the given item,
	 * such that it can be reused.
	 * 
	 * @param kid
	 * @param widget
	 */
	protected synchronized void unmap(Object kid, Widget widget) {
		if (kid == null) {
			// when unmapping a dummy item
			return;
		}
		Widget[] widgets = getWidgets(kid);
		fWidgetsToElements.remove(widget);
		if (widgets != null) {
			for (int i = 0; i < widgets.length; i++) {
				Widget item = widgets[i];
				if (item == widget) {
					if (widgets.length == 1) {
						fElementsToWidgets.remove(kid);
						// uninstall its update policy, if element no longer in viewer
						disposeModelProxy(kid);
					} else {
						Widget[] newItems = new Widget[widgets.length - 1];
						System.arraycopy(widgets, 0, newItems, 0, i);
						if (i < newItems.length) {
							System.arraycopy(widgets, i + 1, newItems, i, newItems.length - i);
						}
						fElementsToWidgets.put(kid, newItems);
					}
				}
			}
		}
	}

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
	public synchronized void setSelection(ISelection selection, final boolean reveal) {
		Control control = getControl();
		if (control == null || control.isDisposed()) {
			return;
		}
		if (!acceptsSelection(selection)) {
			selection = getEmptySelection();
		}
		if (!overrideSelection(fCurrentSelection, selection)) {
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
				return (IModelSelectionPolicy) adaptable.getAdapter(IModelSelectionPolicy.class);
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
	protected synchronized void attemptSelection(boolean reveal) {
		if (fPendingSelection != null) {
			fPendingSelection = doAttemptSelectionToWidget(fPendingSelection, reveal);
			if (fPendingSelection.isEmpty()) {
				fPendingSelection = null;
			}
			ISelection currentSelection = newSelectionFromWidget();
			if (isSuppressEqualSelections() && currentSelection.equals(fCurrentSelection)) {
				return;
			}
			updateSelection(currentSelection);
		}
	}
	
	/**
	 * Controls whether selection change notification is sent even when
	 * successive selections are equal.
	 * 
	 * TODO: what we really want is to fire selection change on ACTIVATE
	 * model change, even when selection is the same.
	 * 
	 * @return whether to suppress change notification for equal successive
	 *  selections
	 */
	protected boolean isSuppressEqualSelections() {
		return true;
	}
	
	/**
	 * Attemtps to selection the specified selection and returns a selection
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
		if (fPendingSelection == null) {
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
							restoreSelection(tempSelection);
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
	
	/**
	 * Returns the parent widget of the give widget or <code>null</code>
	 * if none. This method can be called in a non-UI thread.
	 * 
	 * @param widget widget
	 * @return parent widget or <code>null</code>
	 */
	protected abstract Widget getParent(Widget widget);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#updateSelection(org.eclipse.jface.viewers.ISelection)
	 */
	protected synchronized void updateSelection(ISelection selection) {
		fCurrentSelection = selection;
		super.updateSelection(selection);
	}

	

}
