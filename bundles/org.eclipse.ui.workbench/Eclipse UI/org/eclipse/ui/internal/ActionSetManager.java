/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Maintains a reference counted set of action sets, with a visibility mask.
 * This is used to determine the visibility of actions in a workbench page. In a
 * workbench page, there may be may be many conditions that can cause an action
 * set to become visible (such as the active part, the active editor, the
 * default visibility of the action, the properties of the perspective, etc.)
 * The user can also explicitly mask off particular action sets in each
 * perspective.
 * <p>
 * The reference count indicates how many conditions have requested that the
 * actions be active and the mask indicates whether or not the set was disabled
 * by the user.
 * </p>
 * 
 * @since 3.1
 */
public class ActionSetManager {

    private static class ActionSetRec {
        int showCount;

        int maskCount;
        
        public boolean isVisible() {
            return maskCount == 0 && showCount > 0;
        }
        
        public boolean isEmpty() {
            return maskCount == 0 && showCount == 0;
        }
    }

    private HashMap actionSets = new HashMap();
    private HashSet visibleItems = new HashSet();
    
    public static final int PROP_VISIBLE = 0;
    public static final int PROP_HIDDEN = 1;
    public static final int CHANGE_MASK = 0;
    public static final int CHANGE_UNMASK = 1;
    public static final int CHANGE_SHOW = 2;
    public static final int CHANGE_HIDE = 3;
    
    private ListenerList listeners = new ListenerList();
	private IPropertyListener contextListener;
	private Map activationsById = new HashMap();
	private IContextService contextService;
    
    public ActionSetManager(IServiceLocator locator) {
    	contextService = (IContextService) locator.getService(IContextService.class);
		addListener(getContextListener());
    }
    
    /**
	 * @return
	 */
	private IPropertyListener getContextListener() {
		if (contextListener == null) {
			contextListener = new IPropertyListener() {
				public void propertyChanged(Object source, int propId) {
					if (source instanceof IActionSetDescriptor) {
						IActionSetDescriptor desc = (IActionSetDescriptor) source;
						String id = desc.getId();
						if (propId == PROP_VISIBLE) {
							activationsById.put(id, contextService
									.activateContext(id));
						} else if (propId == PROP_HIDDEN) {
							IContextActivation act = (IContextActivation) activationsById
									.remove(id);
							if (act != null) {
								contextService.deactivateContext(act);
							}
						}
					}
				}
			};
		}
		return contextListener;
	}

	public void addListener(IPropertyListener l) {
        listeners.add(l);
    }

    public void removeListener(IPropertyListener l) {
        listeners.remove(l);
    }
    
    private void firePropertyChange(IActionSetDescriptor descriptor, int id) {
    	Object[] l = listeners.getListeners();
        for (int i=0; i<l.length; i++) {
            IPropertyListener listener = (IPropertyListener) l[i];
            listener.propertyChanged(descriptor, id);
        }
    }        
    
    private ActionSetRec getRec(IActionSetDescriptor descriptor) {
        ActionSetRec rec = (ActionSetRec)actionSets.get(descriptor);
        
        if (rec == null) {
            rec = new ActionSetRec();
            actionSets.put(descriptor, rec);
        }
        
        return rec;
    }
    
    public void showAction(IActionSetDescriptor descriptor) {
        ActionSetRec rec = getRec(descriptor);
        
        boolean wasVisible = rec.isVisible();
        rec.showCount++;
        if (!wasVisible && rec.isVisible()) {
            visibleItems.add(descriptor);
            firePropertyChange(descriptor, PROP_VISIBLE);
            if (rec.isEmpty()) {
                actionSets.remove(descriptor);
            }
        }
    }
    
    public void hideAction(IActionSetDescriptor descriptor) {
        ActionSetRec rec = getRec(descriptor);
        
        boolean wasVisible = rec.isVisible();
        rec.showCount--;
        if (wasVisible && !rec.isVisible()) {
            visibleItems.remove(descriptor);
            firePropertyChange(descriptor, PROP_HIDDEN);
            if (rec.isEmpty()) {
                actionSets.remove(descriptor);
            }
        }
    }
    
    public void maskAction(IActionSetDescriptor descriptor) {
        ActionSetRec rec = getRec(descriptor);
        
        boolean wasVisible = rec.isVisible();
        rec.maskCount++;
        if (wasVisible && !rec.isVisible()) {
            visibleItems.remove(descriptor);
            firePropertyChange(descriptor, PROP_HIDDEN);
            if (rec.isEmpty()) {
                actionSets.remove(descriptor);
            }
        }
    }
    
    public void unmaskAction(IActionSetDescriptor descriptor) {
        ActionSetRec rec = getRec(descriptor);
        
        boolean wasVisible = rec.isVisible();
        rec.maskCount--;
        if (!wasVisible && rec.isVisible()) {
            visibleItems.add(descriptor);
            firePropertyChange(descriptor, PROP_VISIBLE);
            if (rec.isEmpty()) {
                actionSets.remove(descriptor);
            }
        }
    }
    
    public Collection getVisibleItems() {
        return visibleItems;
    }
    
    public void change(IActionSetDescriptor descriptor, int changeType) {
        switch(changeType) {
        case CHANGE_SHOW:
            showAction(descriptor); break;
        case CHANGE_HIDE:
            hideAction(descriptor); break;
        case CHANGE_MASK:
            maskAction(descriptor); break;
        case CHANGE_UNMASK:
            unmaskAction(descriptor); break;
        }
    }
}
