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

package org.eclipse.ui.texteditor;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Formerly thrown by <code>AbstractDocumentProvider.doValidateState(Object, Object)</code>.
 * @deprecated
 */
public class ValidateStateException extends CoreException {

	/*
	 * @see org.eclipse.core.runtime.CoreException#CoreException(IStatus)
	 */
	 public ValidateStateException(IStatus status) {
		super(status);
	}

}
