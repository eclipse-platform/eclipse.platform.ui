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
package org.eclipse.ui.internal.components.framework;

import org.eclipse.ui.internal.components.NullDisposable;

/**
 * A handle to a component. Components are ordinary java objects that are created by an 
 * <code>ComponentFactory</code>. The handle knows how to clean up after the object when
 * it is no longer needed. 
 * 
 * Most handles point to a unique instance of a component, but it is also possible for 
 * many handles to point to the same singleton or reference-counted object.
 * 
 * <p>In most cases, factories return handles to components that either implement IDisposable 
 * or don't require explicit disposal. In this situation, the standard <code>ComponentHandle</code> 
 * class is sufficient. Clients only need to subclass this class if they need to specialize
 * the disposal behavior. For example, a subclass of <code>ComponentHandle</code> could
 * decrement a reference count rather than calling IDisposable.dispose() on the component.
 * </p>
 * 
 * <p>
 * Clients should use the following criteria when selecting what type of ComponentHandle
 * to create:
 * </p>
 * 
 * <ul>
 * <li>If the component is owned by the handle and implements IDisposable, use ComponentHandle</li>
 * <li>If the component does not implement IDisposable and does not require disposal, use ComponentHandle</li>
 * <li>If the component is a singleton or shared object that is being managed outside the framework, use NonDisposingHandle</li>
 * <li>If the component is a temporary object which should be GC'd as soon as possible, use NonDisposingHandle</li>
 * <li>If the component requires explicit disposal but does not implement IDisposable, use a custom subclass of ComponentHandle</li>
 * <li>If the component is reference-counted, use a custom subclass of ComponentHandle</li>
 * </ul>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public class ComponentHandle {
    
    private Object component;
    
    /**
     * Creates a handle for the given component
     * 
     * @param component
     */
    public ComponentHandle(Object component) {
        this.component = component;
    }
    
	/**
	 * Returns the component instance. 
	 * 
	 * @return the component instance. Never null.
	 */
    public final Object getInstance() {
        return component;
    }
    
    /**
     * Returns an interface that can be used to dispose the handle. The owner of the handle
     * must call getDisposable().dispose() when they are done with the component.
     *  
     * <p>Note that if requiresDisposal() returns false the owner of the handle may, at its option,
     * choose to ignore this method.</p>  
     *
     * @return an IDisposable interface that should be used to dispose the handle 
     * when no longer needed
     */
    public IDisposable getDisposable() {
        if (component instanceof IDisposable) {
            return (IDisposable)component;
        }
        
        return NullDisposable.instance;
    }
    
    /**
     * Hint to the owner of the handle. If this returns true, the owner of the handle must
     * call getDisposable().dispose() when they are done with the handle.
     *  
     * <p>
     * If this method returns false the owner of the handle may, at its option, choose to 
     * discard the handle and the component at any time without disposing it.
     * </p>  
     *
     * @return true iff the owner of the handle may discard the handle without explicit
     * disposal.
     */
    public boolean requiresDisposal() {
        return true;
    }
    
}
