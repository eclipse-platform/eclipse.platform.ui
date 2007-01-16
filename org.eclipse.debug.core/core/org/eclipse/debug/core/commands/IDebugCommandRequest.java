/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.commands;

import org.eclipse.debug.core.IRequest;

/**
 * A request to execute a command on specific elements. A debug command request is 
 * passed to a {@link IDebugCommandHandler} when a command is invoked.
 * <p>
 * Clients that invoke command handlers may implement this interface.
 * </p>
 * @since 3.3
 */
public interface IDebugCommandRequest extends IRequest {

	/**
	 * Returns the elements to execute a command on.
	 * 
	 * @return elements to execute a command on
	 */
	public Object[] getElements();
}
