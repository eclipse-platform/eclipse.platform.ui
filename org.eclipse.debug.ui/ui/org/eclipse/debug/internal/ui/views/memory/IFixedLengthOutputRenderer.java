/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;


/**
 * Renderer who knows how the number of characters a byte will
 * map to should implement this interface.
 * 
 * @since 3.0
 */
public interface IFixedLengthOutputRenderer {
	
	public int getNumCharPerByte();
}
