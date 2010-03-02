/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Hashtable;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Abstract selection service.
 */
public abstract class AbstractSelectionService implements ISelectionService {

    /** 
     * The list of selection listeners (not per-part).
     */
    private ListenerList listeners = new ListenerList();

    /** 
     * The list of post selection listeners (not per-part).
     */
    private ListenerList postListeners = new ListenerList();

    /**
     * The currently active part.
     */
    private IWorkbenchPart activePart;

    /**
     * The active part's selection provider, remembered in case the part 
     * replaces its selection provider after we hooked a listener.
     */
    private ISelectionProvider activeProvider;

    /**
     * Map from part id (String) to per-part tracker (AbstractPartSelectionTracker).
     */
    private Hashtable perPartTrackers;

    /**
     * The JFace selection listener to hook on the active part's selection provider.
     */
    private ISelectionChangedListener selListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            fireSelection(activePart, event.getSelection());
        }
    };

    /**
     * The JFace post selection listener to hook on the active part's selection provider.
     */
    private ISelectionChangedListener postSelListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            firePostSelection(activePart, event.getSelection());
        }
    };

    /**
     * Creates a new SelectionService.
     */
    protected AbstractSelectionService() {
    }

    /* (non-Javadoc)
     * Method declared on ISelectionService.
     */
    public void addSelectionListener(ISelectionListener l) {
        listeners.add(l);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionService.
     */
    public void addSelectionListener(String partId, ISelectionListener listener) {
        getPerPartTracker(partId).addSelectionListener(listener);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionService.
     */
    public void addPostSelectionListener(ISelectionListener l) {
        postListeners.add(l);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionService.
     */
    public void addPostSelectionListener(String partId,
            ISelectionListener listener) {
        getPerPartTracker(partId).addPostSelectionListener(listener);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionService.
     */
    public void removeSelectionListener(ISelectionListener l) {
        listeners.remove(l);
    }

    /*
     * (non-Javadoc)
     * Method declared on ISelectionListener.
     */
    public void removePostSelectionListener(String partId,
            ISelectionListener listener) {
        getPerPartTracker(partId).removePostSelectionListener(listener);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionService.
     */
    public void removePostSelectionListener(ISelectionListener l) {
        postListeners.remove(l);
    }

    /*
     * (non-Javadoc)
     * Method declared on ISelectionListener.
     */
    public void removeSelectionListener(String partId,
            ISelectionListener listener) {
        getPerPartTracker(partId).removeSelectionListener(listener);
    }

    /**
     * Fires a selection event to the given listeners.
     * 
     * @param part the part or <code>null</code> if no active part
     * @param sel the selection or <code>null</code> if no active selection
     */
    protected void fireSelection(final IWorkbenchPart part, final ISelection sel) {
        Object[] array = listeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final ISelectionListener l = (ISelectionListener) array[i];
            if ((part != null && sel != null)
                    || l instanceof INullSelectionListener) {
                
                try {
                    l.selectionChanged(part, sel);
                } catch (Exception e) {
                    WorkbenchPlugin.log(e);
                }
            }
        }
    }

    /**
     * Fires a selection event to the given listeners.
     * 
     * @param part the part or <code>null</code> if no active part
     * @param sel the selection or <code>null</code> if no active selection
     */
    protected void firePostSelection(final IWorkbenchPart part,
            final ISelection sel) {
        Object[] array = postListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final ISelectionListener l = (ISelectionListener) array[i];
            if ((part != null && sel != null)
                    || l instanceof INullSelectionListener) {
                
                try {
                    l.selectionChanged(part, sel);
                } catch (Exception e) {
                    WorkbenchPlugin.log(e);
                }
            }
        }
    }

    /**
     * Returns the per-part selection tracker for the given part id.
     * 
     * @param partId part identifier
     * @return per-part selection tracker
     */
    protected AbstractPartSelectionTracker getPerPartTracker(String partId) {
        if (perPartTrackers == null) {
            perPartTrackers = new Hashtable(4);
        }
        AbstractPartSelectionTracker tracker = (AbstractPartSelectionTracker) perPartTrackers
                .get(partId);
        if (tracker == null) {
            tracker = createPartTracker(partId);
            perPartTrackers.put(partId, tracker);
        }
        return tracker;
    }

    /**
     * Creates a new per-part selection tracker for the given part id.
     * 
     * @param partId part identifier
     * @return per-part selection tracker
     */
    protected abstract AbstractPartSelectionTracker createPartTracker(
            String partId);

    /**
     * Returns the selection.
     */
    public ISelection getSelection() {
        if (activeProvider != null) {
			return activeProvider.getSelection();
		} else {
			return null;
		}
    }

    /*
     * @see ISelectionService#getSelection(String)
     */
    public ISelection getSelection(String partId) {
        return getPerPartTracker(partId).getSelection();
    }

    /**
     * Sets the current-active part (or null if none)
     * 
     * @since 3.1 
     *
     * @param newPart the new active part (or null if none)
     */
    public void setActivePart(IWorkbenchPart newPart) {
        // Optimize.
        if (newPart == activePart) {
			return;
		}
        
        ISelectionProvider selectionProvider = null;
        
        if (newPart != null) {
            selectionProvider = newPart.getSite().getSelectionProvider();
            
            if (selectionProvider == null) {
                newPart = null;
            }
        }
        
        if (newPart == activePart) {
			return;
		}
        
        if (activePart != null) {
            if (activeProvider != null) {
                activeProvider.removeSelectionChangedListener(selListener);
                if (activeProvider instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) activeProvider)
                            .removePostSelectionChangedListener(postSelListener);
				} else {
					activeProvider
                            .removeSelectionChangedListener(postSelListener);
				}
                activeProvider = null;
            }
            activePart = null;
        }

        activePart = newPart;
        
        if (newPart != null) {
            activeProvider = selectionProvider;
            // Fire an event if there's an active provider
            activeProvider.addSelectionChangedListener(selListener);
            ISelection sel = activeProvider.getSelection();
            fireSelection(newPart, sel);
            if (activeProvider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) activeProvider)
                        .addPostSelectionChangedListener(postSelListener);
			} else {
				activeProvider.addSelectionChangedListener(postSelListener);
			}
            firePostSelection(newPart, sel);
        } else {
            fireSelection(null, null);
            firePostSelection(null, null);
        }
    }
    
//    /**
//     * Notifies the listener that a part has been activated.
//     */
//    public void partActivated(IWorkbenchPart newPart) {
//        // Optimize.
//        if (newPart == activePart)
//            return;
//
//        // Unhook selection from the old part.
//        reset();
//
//        // Update active part.
//        activePart = newPart;
//
//        // Hook selection on the new part.
//        if (activePart != null) {
//            activeProvider = activePart.getSite().getSelectionProvider();
//            if (activeProvider != null) {
//                // Fire an event if there's an active provider
//                activeProvider.addSelectionChangedListener(selListener);
//                ISelection sel = activeProvider.getSelection();
//                fireSelection(newPart, sel);
//                if (activeProvider instanceof IPostSelectionProvider)
//                    ((IPostSelectionProvider) activeProvider)
//                            .addPostSelectionChangedListener(postSelListener);
//                else
//                    activeProvider.addSelectionChangedListener(postSelListener);
//                firePostSelection(newPart, sel);
//            } else {
//                //Reset active part. activeProvider may not be null next time this method is called.
//                activePart = null;
//            }
//        }
//        // No need to fire an event if no active provider, since this was done in reset()
//    }
//
//    /**
//     * Notifies the listener that a part has been brought to the front.
//     */
//    public void partBroughtToTop(IWorkbenchPart newPart) {
//        // do nothing, the active part has not changed,
//        // so the selection is unaffected
//    }
//
//    /**
//     * Notifies the listener that a part has been closed
//     */
//    public void partClosed(IWorkbenchPart part) {
//        // Unhook selection from the part.
//        if (part == activePart) {
//            reset();
//        }
//    }
//
//    /**
//     * Notifies the listener that a part has been deactivated.
//     */
//    public void partDeactivated(IWorkbenchPart part) {
//        // Unhook selection from the part.
//        if (part == activePart) {
//            reset();
//        }
//    }
//
//    /**
//     * Notifies the listener that a part has been opened.
//     */
//    public void partOpened(IWorkbenchPart part) {
//        // Wait for activation.
//    }
//
//    /**
//     * Notifies the listener that a part has been opened.
//     */
//    public void partInputChanged(IWorkbenchPart part) {
//        // 36501 - only process if part is active
//        if (activePart == part) {
//            reset();
//            partActivated(part);
//        }
//    }
//
//    /**
//     * Resets the service.  The active part and selection provider are
//     * dereferenced.
//     */
//    public void reset() {
//        if (activePart != null) {
//            fireSelection(null, null);
//            firePostSelection(null, null);
//            if (activeProvider != null) {
//                activeProvider.removeSelectionChangedListener(selListener);
//                if (activeProvider instanceof IPostSelectionProvider)
//                    ((IPostSelectionProvider) activeProvider)
//                            .removePostSelectionChangedListener(postSelListener);
//                else
//                    activeProvider
//                            .removeSelectionChangedListener(postSelListener);
//                activeProvider = null;
//            }
//            activePart = null;
//        }
//    }

}
