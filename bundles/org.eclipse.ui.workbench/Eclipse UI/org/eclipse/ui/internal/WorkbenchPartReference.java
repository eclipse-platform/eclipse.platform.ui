/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM; Chris Torrence, ITT Visual Information Solutions - bug 51580
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.UIListenerLogging;
import org.eclipse.ui.internal.util.Util;

/**
 * 
 */
public abstract class WorkbenchPartReference implements IWorkbenchPartReference, ISizeProvider {

    /**
     * Internal property ID: Indicates that the underlying part was created
     */
    public static final int INTERNAL_PROPERTY_OPENED = 0x211;
    
    /**
     * Internal property ID: Indicates that the underlying part was destroyed
     */
    public static final int INTERNAL_PROPERTY_CLOSED = 0x212;

    /**
     * Internal property ID: Indicates that the result of IEditorReference.isPinned()
     */
    public static final int INTERNAL_PROPERTY_PINNED = 0x213;

    /**
     * Internal property ID: Indicates that the result of getVisible() has changed
     */
    public static final int INTERNAL_PROPERTY_VISIBLE = 0x214;

    /**
     * Internal property ID: Indicates that the result of isZoomed() has changed
     */
    public static final int INTERNAL_PROPERTY_ZOOMED = 0x215;

    /**
     * Internal property ID: Indicates that the part has an active child and the
     * active child has changed. (fired by PartStack)
     */
    public static final int INTERNAL_PROPERTY_ACTIVE_CHILD_CHANGED = 0x216;

    /**
     * Internal property ID: Indicates that changed in the min / max
     * state has changed
     */
    public static final int INTERNAL_PROPERTY_MAXIMIZED = 0x217;

    // State constants //////////////////////////////
    
    /**
     * State constant indicating that the part is not created yet
     */
    public static int STATE_LAZY = 0;
     
    /**
     * State constant indicating that the part is in the process of being created
     */
    public static int STATE_CREATION_IN_PROGRESS = 1;
    
    /**
     * State constant indicating that the part has been created
     */
    public static int STATE_CREATED = 2;
    
    /**
     * State constant indicating that the reference has been disposed (the reference shouldn't be
     * used anymore)
     */
    public static int STATE_DISPOSED = 3;
  
    /**
     * Current state of the reference. Used to detect recursive creation errors, disposed
     * references, etc. 
     */
    private int state = STATE_LAZY;
   
    protected IWorkbenchPart part;

    private String id;

    protected PartPane pane;

    private boolean pinned = false;
    
    private String title;

    private String tooltip;

    /**
     * Stores the current Image for this part reference. Lazily created. Null if not allocated.
     */
    private Image image = null;
    
    private ImageDescriptor defaultImageDescriptor;
    
    /**
     * Stores the current image descriptor for the part. 
     */
    private ImageDescriptor imageDescriptor;

    /**
     * API listener list
     */
    private ListenerList propChangeListeners = new ListenerList();

    /**
     * Internal listener list. Listens to the INTERNAL_PROPERTY_* property change events that are not yet API.
     * TODO: Make these properties API in 3.2
     */
    private ListenerList internalPropChangeListeners = new ListenerList();
    
    private ListenerList partChangeListeners = new ListenerList();
    
    private String partName;

    private String contentDescription;
    
    protected Map propertyCache = new HashMap();
    
    /**
     * Used to remember which events have been queued.
     */
    private BitSet queuedEvents = new BitSet();

    private boolean queueEvents = false;

    private static DisposeListener prematureDisposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
            WorkbenchPlugin.log(new RuntimeException("Widget disposed too early!")); //$NON-NLS-1$
        }    
    };
    
    private IPropertyListener propertyChangeListener = new IPropertyListener() {
        /* (non-Javadoc)
         * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
         */
        public void propertyChanged(Object source, int propId) {
            partPropertyChanged(source, propId);
        }
    };
    
    private IPropertyChangeListener partPropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			partPropertyChanged(event);
		}
    };
    
    public WorkbenchPartReference() {
        //no-op
    }
    
    public boolean isDisposed() {
        return state == STATE_DISPOSED;
    }
    
    protected void checkReference() {
        if (state == STATE_DISPOSED) {
            throw new RuntimeException("Error: IWorkbenchPartReference disposed"); //$NON-NLS-1$
        }
    }
    
    /**
     * Calling this with deferEvents(true) will queue all property change events until a subsequent
     * call to deferEvents(false). This should be used at the beginning of a batch of related changes
     * to prevent duplicate property change events from being sent.
     * 
     * @param shouldQueue
     */
    private void deferEvents(boolean shouldQueue) {
        queueEvents = shouldQueue;

        if (queueEvents == false) {
        	// do not use nextSetBit, to allow compilation against JCL Foundation (bug 80053)
        	for (int i = 0, n = queuedEvents.size(); i < n; ++i) {
        		if (queuedEvents.get(i)) {
        			firePropertyChange(i);
        			queuedEvents.clear(i);
        		}
            }
        }
    }

    protected void setTitle(String newTitle) {
        if (Util.equals(title, newTitle)) {
            return;
        }

        title = newTitle;
        firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
    }

    protected void setPartName(String newPartName) {
        if (Util.equals(partName, newPartName)) {
            return;
        }

        partName = newPartName;
        firePropertyChange(IWorkbenchPartConstants.PROP_PART_NAME);
    }

    protected void setContentDescription(String newContentDescription) {
        if (Util.equals(contentDescription, newContentDescription)) {
            return;
        }

        contentDescription = newContentDescription;
        firePropertyChange(IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION);
    }

    protected void setImageDescriptor(ImageDescriptor descriptor) {
        if (Util.equals(imageDescriptor, descriptor)) {
            return;
        }

        Image oldImage = image;
        ImageDescriptor oldDescriptor = imageDescriptor;
        image = null;
        imageDescriptor = descriptor;
        
        // Don't queue events triggered by image changes. We'll dispose the image
        // immediately after firing the event, so we need to fire it right away.
        immediateFirePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
        if (queueEvents) {
            // If there's a PROP_TITLE event queued, remove it from the queue because
            // we've just fired it.
            queuedEvents.clear(IWorkbenchPartConstants.PROP_TITLE);
        }
        
        // If we had allocated the old image, deallocate it now (AFTER we fire the property change 
        // -- listeners may need to clean up references to the old image)
        if (oldImage != null) {
            JFaceResources.getResources().destroy(oldDescriptor);
        }
    }
    
    protected void setToolTip(String newToolTip) {
        if (Util.equals(tooltip, newToolTip)) {
            return;
        }

        tooltip = newToolTip;
        firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
    }

    protected void partPropertyChanged(Object source, int propId) {

        // We handle these properties directly (some of them may be transformed
        // before firing events to workbench listeners)
        if (propId == IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION
                || propId == IWorkbenchPartConstants.PROP_PART_NAME
                || propId == IWorkbenchPartConstants.PROP_TITLE) {

            refreshFromPart();
        } else {
            // Any other properties are just reported to listeners verbatim
            firePropertyChange(propId);
        }
        
        // Let the model manager know as well
        if (propId == IWorkbenchPartConstants.PROP_DIRTY) {
        	IWorkbenchPart actualPart = getPart(false);
        	if (actualPart != null) {
				SaveablesList modelManager = (SaveablesList) actualPart.getSite().getService(ISaveablesLifecycleListener.class);
	        	modelManager.dirtyChanged(actualPart);
        	}
        }
    }
    
    protected void partPropertyChanged(PropertyChangeEvent event) {
    	firePartPropertyChange(event);
    }

    /**
     * Refreshes all cached values with the values from the real part 
     */
    protected void refreshFromPart() {
        deferEvents(true);

        setPartName(computePartName());
        setTitle(computeTitle());
        setContentDescription(computeContentDescription());
        setToolTip(getRawToolTip());
        setImageDescriptor(computeImageDescriptor());

        deferEvents(false);
    }
    
    protected ImageDescriptor computeImageDescriptor() {
        if (part != null) {
            return ImageDescriptor.createFromImage(part.getTitleImage(), Display.getCurrent());
        }
        return defaultImageDescriptor;
    }

    public void init(String id, String title, String tooltip,
            ImageDescriptor desc, String paneName, String contentDescription) {
        Assert.isNotNull(id);
        Assert.isNotNull(title);
        Assert.isNotNull(tooltip);
        Assert.isNotNull(desc);
        Assert.isNotNull(paneName);
        Assert.isNotNull(contentDescription);
        
        this.id = id;
        this.title = title;
        this.tooltip = tooltip;
        this.partName = paneName;
        this.contentDescription = contentDescription;
        this.defaultImageDescriptor = desc;
        this.imageDescriptor = computeImageDescriptor();
    }

    /**
     * Releases any references maintained by this part reference
     * when its actual part becomes known (not called when it is disposed).
     */
    protected void releaseReferences() {

    }

    /* package */ void addInternalPropertyListener(IPropertyListener listener) {
        internalPropChangeListeners.add(listener);
    }
    
    /* package */ void removeInternalPropertyListener(IPropertyListener listener) {
        internalPropChangeListeners.remove(listener);
    }

    protected void fireInternalPropertyChange(int id) {
        Object listeners[] = internalPropChangeListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyListener) listeners[i]).propertyChanged(this, id);
        }
    }
    
    /**
     * @see IWorkbenchPart
     */
    public void addPropertyListener(IPropertyListener listener) {
        // The properties of a disposed reference will never change, so don't
        // add listeners
        if (isDisposed()) {
            return;
        }
        
        propChangeListeners.add(listener);
    }

    /**
     * @see IWorkbenchPart
     */
    public void removePropertyListener(IPropertyListener listener) {
        // Currently I'm not calling checkReference here for fear of breaking things late in 3.1, but it may
        // make sense to do so later. For now we just turn it into a NOP if the reference is disposed.
        if (isDisposed()) {
            return;
        }
        propChangeListeners.remove(listener);
    }

    public final String getId() {
        if (part != null) {
            IWorkbenchPartSite site = part.getSite();
            if (site != null) {
				return site.getId();
			}
        }
        return Util.safeString(id);
    }

    public String getTitleToolTip() {
        return Util.safeString(tooltip);
    }

    protected final String getRawToolTip() {
        return Util.safeString(part.getTitleToolTip());
    }

    /**
     * Returns the pane name for the part
     * 
     * @return the pane name for the part
     */
    public String getPartName() {
        return Util.safeString(partName);
    }
    
    /**
     * Gets the part name directly from the associated workbench part,
     * or the empty string if none.
     * 
     * @return
     */
    protected final String getRawPartName() {
        String result = ""; //$NON-NLS-1$

        if (part instanceof IWorkbenchPart2) {
            IWorkbenchPart2 part2 = (IWorkbenchPart2) part;

            result = Util.safeString(part2.getPartName());
        }

        return result;
    }

    protected String computePartName() {
        return getRawPartName();
    }

    /**
     * Returns the content description for this part.
     * 
     * @return the pane name for the part
     */
    public String getContentDescription() {
        return Util.safeString(contentDescription);
    }

    /**
     * Computes a new content description for the part. Subclasses may override to change the
     * default behavior
     * 
     * @return the new content description for the part
     */
    protected String computeContentDescription() {
        return getRawContentDescription();
    }

    /**
     * Returns the content description as set directly by the part, or the empty string if none
     * 
     * @return the unmodified content description from the part (or the empty string if none)
     */
    protected final String getRawContentDescription() {
        if (part instanceof IWorkbenchPart2) {
            IWorkbenchPart2 part2 = (IWorkbenchPart2) part;

            return part2.getContentDescription();
        }

        return ""; //$NON-NLS-1$				
    }

    public boolean isDirty() {
        if (!(part instanceof ISaveablePart)) {
			return false;
		}
        return ((ISaveablePart) part).isDirty();
    }

    public String getTitle() {
        return Util.safeString(title);
    }

    /**
     * Computes a new title for the part. Subclasses may override to change the default behavior.
     * 
     * @return the title for the part
     */
    protected String computeTitle() {
        return getRawTitle();
    }

    /**
     * Returns the unmodified title for the part, or the empty string if none
     * 
     * @return the unmodified title, as set by the IWorkbenchPart. Returns the empty string if none.
     */
    protected final String getRawTitle() {
        return Util.safeString(part.getTitle());
    }

    public final Image getTitleImage() {
        if (isDisposed()) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
        }
        
        if (image == null) {        
            image = JFaceResources.getResources().createImageWithDefault(imageDescriptor);
        }
        return image;
    }
    
    public ImageDescriptor getTitleImageDescriptor() {
        if (isDisposed()) {
            return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW);
        }
        
        return imageDescriptor;
    }
    
    /* package */ void fireVisibilityChange() {
        fireInternalPropertyChange(INTERNAL_PROPERTY_VISIBLE);
    }

    /* package */ void fireZoomChange() {
        fireInternalPropertyChange(INTERNAL_PROPERTY_ZOOMED);
    }
    
    public boolean getVisible() {
        if (isDisposed()) {
            return false;
        }
        return getPane().getVisible();
    }
    
    public void setVisible(boolean isVisible) {
        if (isDisposed()) {
            return;
        }
        getPane().setVisible(isVisible);
    }
    
    protected void firePropertyChange(int id) {

        if (queueEvents) {
            queuedEvents.set(id);
            return;
        }
        
        immediateFirePropertyChange(id);
    }
    
    private void immediateFirePropertyChange(int id) {
        UIListenerLogging.logPartReferencePropertyChange(this, id);
        Object listeners[] = propChangeListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyListener) listeners[i]).propertyChanged(part, id);
        }
        
        fireInternalPropertyChange(id);
    }

    public final IWorkbenchPart getPart(boolean restore) {
        if (isDisposed()) {
            return null;
        }
        
        if (part == null && restore) {
            
            if (state == STATE_CREATION_IN_PROGRESS) {
                IStatus result = WorkbenchPlugin.getStatus(
                        new PartInitException(NLS.bind("Warning: Detected recursive attempt by part {0} to create itself (this is probably, but not necessarily, a bug)",  //$NON-NLS-1$
                                getId())));
                WorkbenchPlugin.log(result);
                return null;
            }
            
            try {
                state = STATE_CREATION_IN_PROGRESS;
                
                IWorkbenchPart newPart = createPart();
                if (newPart != null) {
                    part = newPart;
                    // Add a dispose listener to the part. This dispose listener does nothing but log an exception
                    // if the part's widgets get disposed unexpectedly. The workbench part reference is the only
                    // object that should dispose this control, and it will remove the listener before it does so.
                    getPane().getControl().addDisposeListener(prematureDisposeListener);
                    part.addPropertyListener(propertyChangeListener);
                    if (part instanceof IWorkbenchPart3) {
                    	((IWorkbenchPart3)part).addPartPropertyListener(partPropertyChangeListener);
                    }

                    refreshFromPart();
                    releaseReferences();
                    
                    fireInternalPropertyChange(INTERNAL_PROPERTY_OPENED);
                    
                    ISizeProvider sizeProvider = (ISizeProvider) Util.getAdapter(part, ISizeProvider.class);
                    if (sizeProvider != null) {
                        // If this part has a preferred size, indicate that the preferred size may have changed at this point
                        if (sizeProvider.getSizeFlags(true) != 0 || sizeProvider.getSizeFlags(false) != 0) {
                            fireInternalPropertyChange(IWorkbenchPartConstants.PROP_PREFERRED_SIZE);
                        }
                    }
                }
            } finally {
                state = STATE_CREATED;
            }
        }        
        
        return part;
    }
    
    protected abstract IWorkbenchPart createPart();
        
    protected abstract PartPane createPane();
    
    /**
     * Returns the part pane for this part reference. Does not return null. Should not be called
     * if the reference has been disposed.
     * 
     * TODO: clean up all code that has any possibility of calling this on a disposed reference
     * and make this method throw an exception if anyone else attempts to do so.
     * 
     * @return
     */
    public final PartPane getPane() {
        
        // Note: we should never call this if the reference has already been disposed, since it
        // may cause a PartPane to be created and leaked.
        if (pane == null) {
            pane = createPane();
        }
        return pane;
    }

    public final void dispose() {
        
        if (isDisposed()) {
            return;
        }
        
        // Store the current title, tooltip, etc. so that anyone that they can be returned to
        // anyone that held on to the disposed reference.
        partName = getPartName();
        contentDescription = getContentDescription();
        tooltip = getTitleToolTip();
        title = getTitle();
        
        if (state == STATE_CREATION_IN_PROGRESS) {
            IStatus result = WorkbenchPlugin.getStatus(
                    new PartInitException(NLS.bind("Warning: Blocked recursive attempt by part {0} to dispose itself during creation",  //$NON-NLS-1$
                            getId())));
            WorkbenchPlugin.log(result);
            return;
        }
        
    	// Disposing the pane disposes the part's widgets. The part's widgets need to be disposed before the part itself.
        if (pane != null) {
            // Remove the dispose listener since this is the correct place for the widgets to get disposed
            Control targetControl = getPane().getControl(); 
            if (targetControl != null) {
                targetControl.removeDisposeListener(prematureDisposeListener);
            }
            pane.dispose();
        }
        
        doDisposePart();
   
        if (pane != null) {
        	pane.removeContributions();
        }
        
        clearListenerList(internalPropChangeListeners);
        clearListenerList(partChangeListeners);
        Image oldImage = image;
        ImageDescriptor oldDescriptor = imageDescriptor;
        image = null;
        
        state = STATE_DISPOSED;
        imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        defaultImageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        immediateFirePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
        clearListenerList(propChangeListeners);
        
        if (oldImage != null) {
            JFaceResources.getResources().destroy(oldDescriptor);
        }
    }

	/**
	 * Clears all of the listeners in a listener list. TODO Bug 117519 Remove
	 * this method when fixed.
	 * 
	 * @param list
	 *            The list to be clear; must not be <code>null</code>.
	 */
	private final void clearListenerList(final ListenerList list) {
		final Object[] listeners = list.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			list.remove(listeners[i]);
		}
	}

    /**
     * 
     */
    protected void doDisposePart() {
        if (part != null) {
            fireInternalPropertyChange(INTERNAL_PROPERTY_CLOSED);
            // Don't let exceptions in client code bring us down. Log them and continue.
            try {
                part.removePropertyListener(propertyChangeListener);
                if (part instanceof IWorkbenchPart3) {
                	((IWorkbenchPart3)part).removePartPropertyListener(partPropertyChangeListener);
                }
                part.dispose();
            } catch (Exception e) {
                WorkbenchPlugin.log(e);
            }
            part = null;
        }
    }

    public void setPinned(boolean newPinned) {
        if (isDisposed()) {
            return;
        }

        if (newPinned == pinned) {
            return;
        }
        
        pinned = newPinned;
        
        setImageDescriptor(computeImageDescriptor());
        
        fireInternalPropertyChange(INTERNAL_PROPERTY_PINNED);
    }
    
    public boolean isPinned() {
        return pinned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartReference#getPartProperty(java.lang.String)
     */
    public String getPartProperty(String key) {
		if (part != null) {
			if (part instanceof IWorkbenchPart3) {
				return ((IWorkbenchPart3) part).getPartProperty(key);
			}
		} else {
			return (String)propertyCache.get(key);
		}
		return null;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartReference#addPartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPartPropertyListener(IPropertyChangeListener listener) {
    	if (isDisposed()) {
    		return;
    	}
    	partChangeListeners.add(listener);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartReference#removePartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePartPropertyListener(IPropertyChangeListener listener) {
    	if (isDisposed()) {
    		return;
    	}
    	partChangeListeners.remove(listener);
    }
    
    protected void firePartPropertyChange(PropertyChangeEvent event) {
		Object[] l = partChangeListeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			((IPropertyChangeListener) l[i]).propertyChange(event);
		}
	}
    
    protected void createPartProperties(IWorkbenchPart3 workbenchPart) {
		Iterator i = propertyCache.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			workbenchPart.setPartProperty((String) e.getKey(), (String) e.getValue());
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.ISizeProvider#computePreferredSize(boolean, int, int, int)
     */
    public int computePreferredSize(boolean width, int availableParallel,
            int availablePerpendicular, int preferredResult) {

        ISizeProvider sizeProvider = (ISizeProvider) Util.getAdapter(part, ISizeProvider.class);
        if (sizeProvider != null) {
            return sizeProvider.computePreferredSize(width, availableParallel, availablePerpendicular, preferredResult);
        }

        return preferredResult;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISizeProvider#getSizeFlags(boolean)
     */
    public int getSizeFlags(boolean width) {
        ISizeProvider sizeProvider = (ISizeProvider) Util.getAdapter(part, ISizeProvider.class);
        if (sizeProvider != null) {
            return sizeProvider.getSizeFlags(width);
        }
        return 0;
    }
    
}
