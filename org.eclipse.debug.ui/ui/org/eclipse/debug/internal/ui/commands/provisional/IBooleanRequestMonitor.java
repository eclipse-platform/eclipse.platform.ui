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
package org.eclipse.debug.internal.ui.commands.provisional;

import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Request monitor for obtaining a boolean result asynchronously.
 * 
 * @since 3.3
 */
public interface IBooleanRequestMonitor extends IAsynchronousRequestMonitor {

	/**
	 * Sets the result of a boolean request.
	 * 
	 * @param result the result
	 */
	public void setResult(boolean result);
}
