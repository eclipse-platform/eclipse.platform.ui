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
package org.eclipse.ui.internal.part.multiplexer;

import org.eclipse.ui.internal.part.Part;

/**
 * Nested component implementation. Components implementing this interface
 * are capable of delegating to a shared component.
 *  
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface INestedComponent {
    
    /**
     * Copies the component's current state to the shared instance,
     * and start delegating to the shared component.  
     */
	public void activate(Part partBeingActivated);
    
    /**
     * Stops delegating to the shared component. Remove anything
     * that was added to the shared component in the activate() method,
     * if necessary, so that another component can start using
     * the shared instance.
     * 
     * @param newActive component of the same type that is about to
     * be activated as a replacement for this component, or null if none.
     */
	public void deactivate(Object newActive);
}
