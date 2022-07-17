/*******************************************************************************
 * Copyright (c) 2014 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.adapterservice.snippets.adapter;

import org.eclipse.core.runtime.IAdaptable;

class MultiFacetedObject implements IAdaptable, Greeter {

	String name;

	MultiFacetedObject(String name) {
		this.name = name;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(Greeter.class)) {
			return (T) this;
		}
		return null;
	}

	@Override
	public void greet() {
		System.out.println("Hello, my name is " + name);
	}

	public String identify() {
		return "I am the MultiFacetedObject named " + name;
	}

}