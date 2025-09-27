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

import java.util.regex.PatternSyntaxException;

/**
 * This class is used as glue to correctly map to the error messages generated
 * by RegEx-Errors which are directly displayed in plain text.
 */
public class InvalidRegExStatus implements IFindReplaceStatus {

	private final PatternSyntaxException regExException;

	public InvalidRegExStatus(PatternSyntaxException regExException) {
		this.regExException = regExException;
	}

	public String getMessage() {
		return regExException.getLocalizedMessage();
	}

	@Override
	public <T> T accept(IFindReplaceStatusVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isInputValid() {
		return false;
	}

	@Override
	public boolean wasSuccessful() {
		return false;
	}

}
