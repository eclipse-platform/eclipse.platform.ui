/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.themes;

/**
 * Theme elements which may potentially be editted by the user should implement 
 * this interface.
 * 
 * @since 3.0
 */
public interface IEditable {

    /**
     * Returns whether this object is editable.
     * 
     * @return whether this object is editable.
     */
    public boolean isEditable();

}
