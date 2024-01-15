/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.status;

/**
 * Interface for statuses that can occur while performing
 * Find/Replace-operations.
 */
public interface IFindReplaceStatus {
	public <T> T accept(IFindReplaceStatusVisitor<T> visitor);

	/**
	 * {@return whether the input is valid, e.g., that the find string is valid and
	 * that the target is writable on replace operations}
	 */
	public boolean isInputValid();
}
