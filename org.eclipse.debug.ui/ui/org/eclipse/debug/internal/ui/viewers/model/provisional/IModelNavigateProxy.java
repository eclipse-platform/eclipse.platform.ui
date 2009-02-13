/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

/** 
 * Model proxy extension that allows the viewer to navigate elements in the model.
 * 
 * @since 3.5
 */
public interface IModelNavigateProxy extends IModelProxy {
    
    /**
     * Update to retrieve the next element in the model to navigate to.
     * 
     * @param update Update to complete.
     */
    public void update(IModelNavigateUpdate update);
}
