/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.breakpoints.provisional.OtherBreakpointCategory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Common function for breakpoint organizer delegates.
 * <p>
 * Clients implementing <code>IBreakpointOrganizerDelegate</code> must subclass this class.
 * </p>
 * @since 3.1
 */
public abstract class AbstractBreakpointOrganizerDelegate implements IBreakpointOrganizerDelegate {
    
    // property change listeners
    private ListenerList fListeners = new ListenerList();

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        // do noting, not supported by default
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        fListeners.add(listener);
    }
    
    /* (non-Javadoc)
     * 
     * Subclasses that override should return super.canAdd(...) when they are not able to add
     * the breakpoint.
     * 
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canAdd(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canAdd(IBreakpoint breakpoint, IAdaptable category) {
        return category instanceof OtherBreakpointCategory;
    }
    
    /* (non-Javadoc)
     * 
     * Subclasses that override should return super.canRemove(...) when they are not able to remove
     * the breakpoint.
     * 
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canRemove(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canRemove(IBreakpoint breakpoint, IAdaptable category) {
        return category instanceof OtherBreakpointCategory;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#dispose()
     */
    public void dispose() {
        fListeners = new ListenerList();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removeBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        // do nothing, not supported by default
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        fListeners.remove(listener);
    }
    
    /**
     * Fires a property change notification for the given category.
     * 
     * @param category category that has changed
     */
    protected void fireCategoryChanged(IAdaptable category) {
        if (fListeners.isEmpty()) {
            return;
        }
        final PropertyChangeEvent event = new PropertyChangeEvent(this, P_CATEGORY_CHANGED, category, null);
        Object[] listeners = fListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
            ISafeRunnable runnable = new ISafeRunnable() {
                public void handleException(Throwable exception) {
                    DebugUIPlugin.log(exception);
                }
                public void run() throws Exception {
                    listener.propertyChange(event);
                }
            };
            SafeRunner.run(runnable);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories()
     */
    public IAdaptable[] getCategories() {
        return null;
    }
}
