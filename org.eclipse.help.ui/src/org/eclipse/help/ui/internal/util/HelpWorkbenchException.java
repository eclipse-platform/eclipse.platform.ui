/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.util;
// possibly use platform CoreException later. For now, this is
// handled by the Logger class in base.
public class HelpWorkbenchException extends Exception {
	public HelpWorkbenchException() {
		super();
	}
	public HelpWorkbenchException(String message) {
		super(message);
	}
}
