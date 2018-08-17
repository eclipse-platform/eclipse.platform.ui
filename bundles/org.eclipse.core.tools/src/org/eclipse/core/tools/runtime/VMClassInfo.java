/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.runtime;

public class VMClassInfo {
	String name;
	int ramSize = 0;
	int romSize = 0;
	int jitSize = 0;

	public VMClassInfo(String name) {
		this.name = name.replace('/', '.');
	}

	public int getJitSize() {
		return jitSize;
	}

	public String getName() {
		return name;
	}

	public int getRAMSize() {
		return ramSize;
	}

	public int getROMSize() {
		return romSize;
	}

	public void setJITSize(int value) {
		jitSize = value;
	}

	public void setRAMSize(int value) {
		ramSize = value;
	}

	public void setROMSize(int value) {
		romSize = value;
	}
}
