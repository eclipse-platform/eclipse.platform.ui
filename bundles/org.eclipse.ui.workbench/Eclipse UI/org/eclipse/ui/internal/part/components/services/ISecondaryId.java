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
package org.eclipse.ui.internal.part.components.services;

/**
 * Service that provides a part with access to its secondary ID.
 * 
 * @since 3.1
 */
public interface ISecondaryId {
    /**
     * Returns the secondary ID for the part, or null if none
     *
     * @return the secondary ID for the part, or null if none
     */
	public String getSecondaryId();
}
