/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainerTypeDelegate;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;

/**
 * Common function for source container type delegates.
 * <p>
 * Clients implementing source container delegates should subclass this class.
 * </p>
 * @sicne 3.0
 */
public abstract class AbstractSourceContainerTypeDelegate implements ISourceContainerTypeDelegate {
	
	/**
	 * Throws an exception with the given message and underlying exception.
	 * 
	 * @param message error message
	 * @param exception underlying exception, or <code>null</code>
	 * @throws CoreException
	 */
	protected void abort(String message, Throwable exception) throws CoreException {
		SourceLookupUtils.abort(message, exception);
	}
}
