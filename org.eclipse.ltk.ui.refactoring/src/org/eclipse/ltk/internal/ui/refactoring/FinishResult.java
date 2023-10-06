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
package org.eclipse.ltk.internal.ui.refactoring;

/**
 * Enumeration representing the finish result
 *
 * @since 3.0
 */
public class FinishResult {
	private int fValue;

	private FinishResult(int value) {
		fValue= value;
	}

	public static FinishResult createException() {
		return new FinishResult(0);
	}

	public boolean isException() {
		return fValue == 0;
	}

	public static FinishResult createInterrupted() {
		return new FinishResult(1);
	}

	public boolean isInterrupted() {
		return fValue == 1;
	}

	public static FinishResult createOK() {
		return new FinishResult(2);
	}

	public boolean isOK() {
		return fValue == 2;
	}
}
