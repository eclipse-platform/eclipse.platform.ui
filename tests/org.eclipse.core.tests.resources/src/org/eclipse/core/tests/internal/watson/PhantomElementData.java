/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public String toString() {
		StringBuffer buf = new StringBuffer("ElementData(");
		buf.append(isPhantom ? "Phantom, " : "Real, ");
		buf.append(name);
		buf.append(')');
		return buf.toString();
	}
}
