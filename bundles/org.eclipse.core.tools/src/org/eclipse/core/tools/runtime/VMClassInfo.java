/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
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