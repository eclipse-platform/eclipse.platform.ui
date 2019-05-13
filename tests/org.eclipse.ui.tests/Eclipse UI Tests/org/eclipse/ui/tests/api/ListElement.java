/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IActionFilter;

public class ListElement implements IAdaptable {

	private String name;

	private boolean flag;

	public ListElement(String name) {
		this(name, false);
	}

	public ListElement(String name, boolean flag) {
		this.name = name;
		this.flag = flag;
	}

	@Override
	public String toString() {
		return name + ':' + flag;
	}

	public String getName() {
		return name;
	}

	public boolean getFlag() {
		return flag;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IActionFilter.class) {
			return (T) ListElementActionFilter.getSingleton();
		}
		return null;
	}

}

