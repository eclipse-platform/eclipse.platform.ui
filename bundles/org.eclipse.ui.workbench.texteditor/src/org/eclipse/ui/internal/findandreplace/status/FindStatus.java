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

public class FindStatus implements IFindReplaceStatus {

	public enum StatusCode {
		NO_MATCH,
		WRAPPED, READONLY,
	}

	private final StatusCode messageCode;

	public FindStatus(StatusCode errorCode) {
		this.messageCode = errorCode;
	}

	public StatusCode getMessageCode() {
		return messageCode;
	}

	@Override
	public <T> T accept(IFindReplaceStatusVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isInputValid() {
		return messageCode != StatusCode.READONLY;
	}

	@Override
	public boolean wasSuccessful() {
		return messageCode == StatusCode.WRAPPED;
	}

}
