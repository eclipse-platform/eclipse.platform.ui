/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base.util;

/**
 * Utility interface for displaying an error message. Implementation may output
 * messages in different ways (console, pop up window).
 */
public interface IErrorUtil {
	public void displayError(String msg);

	public void displayError(String msg, Thread uiThread);
}
