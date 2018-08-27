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
package org.eclipse.core.tests.internal.watson;

/**
 * Used in conjunction with PluggableDeltaLogicTests
 */
public class PhantomElementData {
	String name;
	boolean isPhantom;

	/**
	 * Creates a new element info for either a phantom or real element
	 */
	public PhantomElementData(String name, boolean isPhantom) {
		this.name = name;
		this.isPhantom = isPhantom;
	}

	/**
	 * For debugging
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("ElementData(");
		buf.append(isPhantom ? "Phantom, " : "Real, ");
		buf.append(name);
		buf.append(')');
		return buf.toString();
	}
}
