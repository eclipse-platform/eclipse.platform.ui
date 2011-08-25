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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;

/**
 * The abstract superclass for save actions that depend on the active editor.
 */
public abstract class BaseSaveAction extends ActiveEditorAction {
    /*
     * The view-related code below was added to track the view with focus
     * in order to support save actions from a view (see bug 10234). 
     */
	
    /**
     * List of parts (element type: <code>IWorkbenchPart</code>)
     * against which this class has outstanding property listeners registered.
     */
    private List partsWithListeners = new ArrayList(1);

    private final IPropertyListener propListener = new IPropertyListener() {
        public void propertyChanged(Object source, int propId) {
            if (source == getActiveEditor()) {
                if (propId == IEditorPart.PROP_DIRTY) {
					updateState();
				}
            }
        }
    };

    /**
     * Creates a new action with the given text.
     *
     * @param text the string used as the text for the action, 
     *   or <code>null</code> if there is no text
     * @param window the workbench window this action is
     *   registered with.
     */
    protected BaseSaveAction(String text, IWorkbenchWindow window) {
        super(text, window);
    }

    /* (non-Javadoc)
     * Method declared on ActiveEditorAction.
     */
    protected void editorActivated(IEditorPart part) {
        if (part != null) {
            part.addPropertyListener(propListener);
            partsWithListeners.add(part);
        }
    }

    /* (non-Javadoc)
     * Method declared on ActiveEditorAction.
     */
    protected void editorDeactivated(IEditorPart part) {
        if (part != null) {
            part.removePropertyListener(propListener);
            partsWithListeners.remove(part);
        }
    }

    private IViewPart activeView;

    private final IPropertyListener propListener2 = new IPropertyListener() {
        public void propertyChanged(Object source, int propId) {
            if (source == activeView) {
                if (propId == IEditorPart.PROP_DIRTY) {
					updateState();
				}
            }
        }
    };

    /** the active saveable part is tracked in order to listen to its dirty events */ 
    private ISaveablePart activeSaveablePart;
    
    private final IPropertyListener propListener3 = new IPropertyListener() {
    	public void propertyChanged(Object source, int propId) {
    		if (source == activeSaveablePart) {
    			if (propId == IEditorPart.PROP_DIRTY) {
    				updateState();
    			}
    		}
    	}
    };
    
    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageActivated(IWorkbenchPage page) {
        super.pageActivated(page);
        updateActiveView();
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageClosed(IWorkbenchPage page) {
        super.pageClosed(page);
        updateActiveView();
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partActivated(IWorkbenchPart part) {
        super.partActivated(part);
        if (part instanceof IViewPart) {
            updateActiveView();
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partClosed(IWorkbenchPart part) {
        super.partClosed(part);
        if (part instanceof IViewPart) {
            updateActiveView();
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partDeactivated(IWorkbenchPart part) {
        super.partDeactivated(part);
        if (part instanceof IViewPart) {
            updateActiveView();
            updateState();
        }
    }

    /**
     * Update the active view based on the current
     * active page.
     */
    private void updateActiveView() {
        if (getActivePage() == null) {
			setActiveView(null);
		} else {
			setActiveView(getActivePage().getActivePart());
		}
    }

    /**
	 * 
	 */
	private void updateActiveSaveablePart() {
		if (activeSaveablePart instanceof IWorkbenchPart) {
			((IWorkbenchPart)activeSaveablePart).removePropertyListener(propListener3);
			partsWithListeners.remove(activeSaveablePart);
		}
		activeSaveablePart = getSaveableView();
		if (activeSaveablePart == activeView) {
			// no need to listen to the same part twice
			activeSaveablePart = null;
		}
		if (activeSaveablePart instanceof IWorkbenchPart) {
			((IWorkbenchPart)activeSaveablePart).addPropertyListener(propListener3);
			partsWithListeners.add(activeSaveablePart);
		}
	}

	/**
     * Set the active editor
     */
    private void setActiveView(IWorkbenchPart part) {
        if (activeView == part) {
            return;
        }
        if (activeView != null) {
            activeView.removePropertyListener(propListener2);
            partsWithListeners.remove(activeView);
        }
        if (part instanceof IViewPart) {
            activeView = (IViewPart) part;
        } else {
            activeView = null;
        }
        if (activeView != null) {
            activeView.addPropertyListener(propListener2);
            partsWithListeners.add(activeView);
        }
        updateActiveSaveablePart();
    }

    protected final ISaveablePart getSaveableView() {
        if (activeView == null) {
            return null;
        }

        return (ISaveablePart) Util.getAdapter(activeView, ISaveablePart.class);
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void dispose() {
        super.dispose();
        for (Iterator it = partsWithListeners.iterator(); it.hasNext();) {
            IWorkbenchPart part = (IWorkbenchPart) it.next();
            part.removePropertyListener(propListener);
            part.removePropertyListener(propListener2);
            part.removePropertyListener(propListener3);
        }
        partsWithListeners.clear();
    }
}
