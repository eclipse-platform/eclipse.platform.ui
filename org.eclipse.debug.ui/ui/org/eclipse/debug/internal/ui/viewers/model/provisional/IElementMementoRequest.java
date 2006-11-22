/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.ui.IMemento;

/**
 * Request to store a memento for an element in a specific context.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 3.3
 */
public interface IElementMementoRequest extends IViewerUpdate {
	
	/**
	 * Returns the memento used to persist the element.
	 * 
	 * @return memento
	 */
	public IMemento getMemento();
}
