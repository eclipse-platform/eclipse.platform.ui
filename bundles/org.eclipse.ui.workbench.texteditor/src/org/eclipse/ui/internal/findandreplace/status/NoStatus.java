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
 * Represents an "everything worked fine, nothing to display"-status.
 */
public class NoStatus implements IFindReplaceStatus {

	public NoStatus() {
		// do nothing
	}

	@Override
	public <T> T accept(IFindReplaceStatusVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isInputValid() {
		return true;
	}

}
