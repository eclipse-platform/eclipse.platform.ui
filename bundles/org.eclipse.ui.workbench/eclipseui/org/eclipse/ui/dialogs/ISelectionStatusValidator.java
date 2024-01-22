/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IStatus;

/**
 * Used in selection dialogs to validate selections
 *
 * @since 2.0
 */
public interface ISelectionStatusValidator {

	/**
	 * Validates an array of elements and returns the resulting status.
	 *
	 * @param selection The elements to validate
	 * @return The resulting status
	 */
	IStatus validate(Object[] selection);

}
