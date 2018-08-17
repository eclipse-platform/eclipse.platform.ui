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

import java.util.HashMap;
import java.util.Map;

public class VMClassloaderInfo {

	protected String id;
	protected ClassLoader loader;
	protected VMClassInfo[] classes;
	protected int allocRAM = 0;
	protected int allocROM = 0;
	protected int usedRAM = 0;
	protected int usedROM = 0;

	public static boolean hasNatives = false;
	protected static Map loaders = new HashMap(20);

	public static VMClassInfo[] getBaseClasses() {
		return new VMClassInfo[0];
	}

	public static VMClassloaderInfo getClassloader(String id) {
		return new VMClassloaderInfo(id, null);
	}

	public static void refreshInfos() {
		loaders = new HashMap(20);
	}

	protected VMClassloaderInfo(String id, ClassLoader loader) {
		this.id = id;
		if (loader != null)
			this.loader = loader;
	}

	public int getAllocRAM() {
		return allocRAM;
	}

	public int getAllocROM() {
		return allocROM;
	}

	public VMClassloaderInfo getClassloader() {
		return this;
	}

	public int getCount() {
		return getClasses().length;
	}

	public int getFreeRAM() {
		return getAllocRAM() - getUsedRAM();
	}

	public int getFreeROM() {
		return getAllocROM() - getUsedROM();
	}

	public String getName() {
		return id;
	}

	protected int getTotal(long type, int aspect) {
		return 0;
	}

	public int getUsedRAM() {
		return usedRAM;
	}

	public int getUsedROM() {
		return usedROM;
	}

	protected VMClassInfo[] getClasses() {
		if (classes == null)
			initializeClasses();
		return classes;
	}

	public VMClassInfo getClass(String name) {
		return new VMClassInfo(name);
	}

	public void refresh() {
		allocRAM = 0;
		allocROM = 0;
		usedRAM = 0;
		usedROM = 0;
		classes = null;
	}

	public void initializeClasses() {
		return;
	}
}
