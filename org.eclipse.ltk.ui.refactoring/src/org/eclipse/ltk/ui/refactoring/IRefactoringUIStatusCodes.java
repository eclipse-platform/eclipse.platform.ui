/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring;

/**
 * Status codes used by the refactoring UI plug-in.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.core.runtime.Status
 *
 * @since 3.0
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRefactoringUIStatusCodes {

	/**
	 * Status code (value 10000) indicating an internal error.
	 */
	public static final int INTERNAL_ERROR= 10000;

}
