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
package org.eclipse.core.runtime.dynamicHelpers;

import org.eclipse.core.runtime.IExtension;

/**
 * A removal handler is notified of the removal of an extension if the removed extension has been registered
 * into the tracker with which the handler is being registered.
 * 
 * This API is EXPERIMENTAL and provided as early access.
 * @since 3.1
 */
public interface IExtensionRemovalHandler {

    /** 
     * This method is being called after the removal of the extension
     * @param extension the extension being removed
     * @param objects the objects that were associated for this object
     */
	public void removeInstance(IExtension extension, Object[] objects);
}
