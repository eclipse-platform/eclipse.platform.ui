/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;

/**
 * @since 3.3
 *
 */
abstract public class AbstractStatusManager {
	/**
	 * A style indicating that the status should not be acted on. This is used
	 * by objects such as log listeners that do not want to report a status twice.
	 */
	public static final int NONE = 0;

	/**
	 * A style indicating that the status should be logged only.
	 */
	public static final int LOG = 0x01;

	/**
	 * A style indicating that handlers should show a problem to an user without
	 * blocking the calling method while awaiting user response. This is generally 
	 * done using a non modal {@link Dialog}.
	 */
	public static final int SHOW = 0x02;
	
	/**
	 * A style indicating that the handling should block the calling method until the
	 * user has responded. This is generally done using a modal window such as a 
	 * {@link Dialog}.
	 */
	public static final int BLOCK = 0x04;
	
	/**
	 * Handles the given status due to the style.
	 * 
	 * @param status
	 *            status to handle
	 * @param style
	 *            style
	 */
	abstract public void handle(IStatus status, int style);

}
