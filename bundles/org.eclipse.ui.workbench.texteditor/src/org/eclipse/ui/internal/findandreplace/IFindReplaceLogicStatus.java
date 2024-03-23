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
package org.eclipse.ui.internal.findandreplace;

/**
 * Marker interface for all Status-objects that a Find/Replace-Operation could
 * return
 */
public interface IFindReplaceLogicStatus {
	public <T> T visit(IFindReplaceLogicStatusVisitor<T> visitor);
}
