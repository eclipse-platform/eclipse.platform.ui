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

/**
 * Provides context sensitive labels. Can be registered as an adapter for an element,
 * or implemented directly.
 * 
 * @since 3.3
 */
public interface IElementLabelProvider {
	
	/**
	 * Updates the specified labels.
	 * 
	 * @param updates each update specifies the element and context for which a label is requested and
	 *  stores label attributes
	 */
	public void update(ILabelUpdate[] updates);
}
