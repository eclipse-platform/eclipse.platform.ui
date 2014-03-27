/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM; Chris Torrence, ITT Visual Information Solutions - bug 51580
 *     Nikolay Botev - bug 240651
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.misc.UIListenerLogging;
import org.eclipse.ui.internal.util.Util;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

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

	static String MEMENTO_KEY = "memento"; //$NON-NLS-1$
  
    /**
     * Current state of the reference. Used to detect recursive creation errors, disposed
     * references, etc. 
     */
    private int state = STATE_LAZY;
   
	protected IWorkbenchPart legacyPart;
    private boolean pinned = false;
    
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
    
    protected Map propertyCache = new HashMap();
    
    private IPropertyListener propertyChangeListener = new IPropertyListener() {
        /* (non-Javadoc)
         * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
         */
        @Override
		public void propertyChanged(Object source, int propId) {
            partPropertyChanged(source, propId);
        }
    };
    
    private IPropertyChangeListener partPropertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			partPropertyChanged(event);
		}
    };

	private IWorkbenchPage page;

	private MPart part;

	private IEclipseContext windowContext;

	private EventHandler contextEventHandler;
    
    public WorkbenchPartReference(IEclipseContext windowContext, IWorkbenchPage page, MPart part) {
    	this.windowContext = windowContext;
		this.page = page;
		this.part = part;

		// cache the reference in the MPart's transientData
		if (part != null) {
			part.getTransientData().put(IWorkbenchPartReference.class.getName(), this);
		}
	}

	private EventHandler createContextEventHandler() {
		if (contextEventHandler == null) {
			contextEventHandler = new EventHandler() {
				@Override
				public void handleEvent(Event event) {
					Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
					MPart part = getModel();
					if (element == part) {
						if (part.getContext() != null) {
							part.getContext().set(getClass().getName(), this);
							unsubscribe();
						}
					}
				}
			};
		}
		return contextEventHandler;
	}

	public void subscribe() {
		IEventBroker broker = windowContext.get(IEventBroker.class);
		broker.subscribe(UIEvents.Context.TOPIC_CONTEXT,
				createContextEventHandler());
	}

	public void unsubscribe() {
		if (contextEventHandler != null) {
			IEventBroker broker = windowContext.get(IEventBroker.class);
			broker.unsubscribe(contextEventHandler);
			contextEventHandler = null;
		}
	}
    
    public boolean isDisposed() {
        return state == STATE_DISPOSED;
    }
    
    protected void checkReference() {
        if (state == STATE_DISPOSED) {
            throw new RuntimeException("Error: IWorkbenchPartReference disposed"); //$NON-NLS-1$
        }
    }
    
	public MPart getModel() {
		return part;
	}


    protected void partPropertyChanged(Object source, int propId) {
		firePropertyChange(propId);
        
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
    @Override
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
    @Override
	public void removePropertyListener(IPropertyListener listener) {
        // Currently I'm not calling checkReference here for fear of breaking things late in 3.1, but it may
        // make sense to do so later. For now we just turn it into a NOP if the reference is disposed.
        if (isDisposed()) {
            return;
        }
        propChangeListeners.remove(listener);
    }


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPartReference#getTitle()
	 */
	@Override
	public String getTitle() {
		String title = legacyPart == null ? part.getLocalizedLabel() : legacyPart.getTitle();
		return Util.safeString(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPartReference#getTitleToolTip()
	 */
	@Override
	public String getTitleToolTip() {
		String toolTip = (String) part.getTransientData().get(
				IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY);
		if (toolTip == null || toolTip.length() == 0)
			toolTip = part.getLocalizedTooltip();
		return Util.safeString(toolTip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPartReference#getId()
	 */
	@Override
	public String getId() {
		String id = part.getElementId();

		// Only return the descriptor id
		int colonIndex = id.indexOf(':');
		return colonIndex == -1 ? id : id.substring(0, colonIndex);
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
		return Util.safeString(legacyPart.getTitle());
    }

	@Override
	public final Image getTitleImage() {
		if (isDisposed()) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
		}

		WorkbenchWindow wbw = (WorkbenchWindow) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (part != null && wbw.getModel().getRenderer() instanceof SWTPartRenderer) {
			SWTPartRenderer r = (SWTPartRenderer) wbw.getModel().getRenderer();
			return r.getImage(part);
		}

		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
	}
    
    /* package */ void fireVisibilityChange() {
        fireInternalPropertyChange(INTERNAL_PROPERTY_VISIBLE);
    }

    /* package */ void fireZoomChange() {
        fireInternalPropertyChange(INTERNAL_PROPERTY_ZOOMED);
    }

	protected void firePropertyChange(int id) {
        immediateFirePropertyChange(id);
    }
    
    private void immediateFirePropertyChange(int id) {
        UIListenerLogging.logPartReferencePropertyChange(this, id);
        Object listeners[] = propChangeListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
			((IPropertyListener) listeners[i]).propertyChanged(legacyPart, id);
        }
        
        fireInternalPropertyChange(id);
    }

	public abstract PartSite getSite();

	public abstract void initialize(IWorkbenchPart part) throws PartInitException;

	void addPropertyListeners() {
		IWorkbenchPart workbenchPart = getPart(false);
		if (workbenchPart != null) {
			workbenchPart.addPropertyListener(propertyChangeListener);

			if (workbenchPart instanceof IWorkbenchPart3) {
				((IWorkbenchPart3) workbenchPart)
						.addPartPropertyListener(partPropertyChangeListener);

			}
		}
	}

    @Override
	public final IWorkbenchPart getPart(boolean restore) {
        if (isDisposed()) {
            return null;
        }
        
        if (legacyPart == null) {
			if (restore && part.getWidget() == null) {
				// create the underlying client object backed by the part model
				// with the rendering engine
				EPartService partService = windowContext.get(EPartService.class);
				partService.showPart(part, PartState.CREATE);
			}

			// check if we were actually created, it is insufficient to check
			// whether the 'object' feature is valid or not because it is one of
			// the last things to be unset during the teardown process, this
			// means we may return a valid workbench part even if it is actually
			// in the process of being destroyed, see bug 328944
			if (part.getObject() instanceof CompatibilityPart) {
				CompatibilityPart compatibilityPart = (CompatibilityPart) part.getObject();
				if (compatibilityPart != null) {
					legacyPart = compatibilityPart.getPart();
				}
			} else if (part.getObject() != null) {
        		if (part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
        		  return (IWorkbenchPart) part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY);
				}
        	}
		}

		return legacyPart;
    }
    
	public abstract IWorkbenchPart createPart() throws PartInitException;

	abstract IWorkbenchPart createErrorPart();

	public abstract IWorkbenchPart createErrorPart(IStatus status);

	protected void doDisposeNestedParts() {
		// To be implemented by subclasses
	}

    /**
     * 
     */
	private void doDisposePart() {
		if (legacyPart != null) {
            fireInternalPropertyChange(INTERNAL_PROPERTY_CLOSED);
            // Don't let exceptions in client code bring us down. Log them and continue.
            try {
				legacyPart.removePropertyListener(propertyChangeListener);
				if (legacyPart instanceof IWorkbenchPart3) {
					((IWorkbenchPart3) legacyPart)
							.removePartPropertyListener(partPropertyChangeListener);
                }
            } catch (Exception e) {
                WorkbenchPlugin.log(e);
            }
			legacyPart = null;
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

		immediateFirePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
        if (pinned)
        	part.getTags().add(IPresentationEngine.ADORNMENT_PIN);
        else
        	part.getTags().remove(IPresentationEngine.ADORNMENT_PIN);

        fireInternalPropertyChange(INTERNAL_PROPERTY_PINNED);
    }
    
    public boolean isPinned() {
        return pinned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartReference#getPartProperty(java.lang.String)
     */
    @Override
	public String getPartProperty(String key) {
		if (legacyPart != null) {
			if (legacyPart instanceof IWorkbenchPart3) {
				return ((IWorkbenchPart3) legacyPart).getPartProperty(key);
			}
		} else {
			return (String)propertyCache.get(key);
		}
		return null;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartReference#addPartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    @Override
	public void addPartPropertyListener(IPropertyChangeListener listener) {
    	if (isDisposed()) {
    		return;
    	}
    	partChangeListeners.add(listener);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartReference#removePartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    @Override
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
    @Override
	public int computePreferredSize(boolean width, int availableParallel,
            int availablePerpendicular, int preferredResult) {

		ISizeProvider sizeProvider = (ISizeProvider) Util.getAdapter(legacyPart,
				ISizeProvider.class);
        if (sizeProvider != null) {
            return sizeProvider.computePreferredSize(width, availableParallel, availablePerpendicular, preferredResult);
        }

        return preferredResult;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISizeProvider#getSizeFlags(boolean)
     */
    @Override
	public int getSizeFlags(boolean width) {
		ISizeProvider sizeProvider = (ISizeProvider) Util.getAdapter(legacyPart,
				ISizeProvider.class);
        if (sizeProvider != null) {
            return sizeProvider.getSizeFlags(width);
        }
        return 0;
    }
    
	@Override
	public IWorkbenchPage getPage() {
		return page;
	}

	public void setPage(IWorkbenchPage newPage) {
		page = newPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPartReference#getPartName()
	 */
	@Override
	public String getPartName() {
		return part.getLocalizedLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPartReference#getContentDescription()
	 */
	@Override
	public String getContentDescription() {
		IWorkbenchPart workbenchPart = getPart(false);
		if (workbenchPart instanceof IWorkbenchPart2) {
			return ((IWorkbenchPart2) workbenchPart).getContentDescription();
		}
		return workbenchPart.getTitle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPartReference#isDirty()
	 */
	@Override
	public boolean isDirty() {
		IWorkbenchPart part = getPart(false);
		if (part instanceof ISaveablePart) {
			return ((ISaveablePart) part).isDirty();
		}
		return false;
	}

	public void invalidate() {
		doDisposePart();
	}

	public final PartPane getPane() {
		return new PartPane() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.WorkbenchPartReference.PartPane#getControl
			 * ()
			 */
			@Override
			public Control getControl() {
				return part == null ? null : (Control) part.getWidget();
			}
		};
	}
}
