/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.ui.internal.part.components.interfaces.IFocusable;

/**
 * Default implementation of the IFocusable adapter. If a part doesn't explicitly
 * provide an adapter for IFocusable, this implementation will be used.
 * 
 * @since 3.1
 */
public class NullFocusable implements IFocusable {
	
	/**
	 * Creates the default implementation of IFocusable
	 */
	public NullFocusable() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.part.components.interfaces.IFocusable#setFocus()
	 */
	public boolean setFocus() {        
        return false;
	}
}
