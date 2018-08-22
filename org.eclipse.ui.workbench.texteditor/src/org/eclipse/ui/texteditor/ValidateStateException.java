/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Formerly thrown by <code>AbstractDocumentProvider.doValidateState(Object, Object)</code>.
 * <p>
 * This class is not intended to be serialized.
 * </p>
 *
 * @deprecated No longer used, create a {@link CoreException} instead
 * @since 2.1
 */
@Deprecated
public class ValidateStateException extends CoreException {

	/**
	 * Serial version UID for this class.
	 * <p>
	 * Note: This class is not intended to be serialized.
	 * </p>
	 * @since 3.1
	 */
	private static final long serialVersionUID= 3834309544406233910L;

	/*
	 * @see CoreException#CoreException(org.eclipse.core.runtime.IStatus)
	 */
	 public ValidateStateException(IStatus status) {
		super(status);
	}

}
