/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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

package org.eclipse.ui.examples.adapterservice.snippets.adapter;

import javax.inject.Inject;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.ui.examples.adapterservice.snippets.SnippetSetup;

/**
 * This snippet demonstrates user of the Adapter service.
 *
 */
@SuppressWarnings("restriction")
public class AdapterSnippet implements IApplication{
	@Inject
	Adapter adapter;

	@Inject
	IAdapterManager adapterManager;

	public AdapterSnippet() {
	}

	/*
	 * This method shows how a typical client would use the adapter service
	 * to get the type that it wants.
	 */
	void tryAdapters(Object someObject) {
		System.out.println("The object to be adapted is " + someObject);

		MultiFacetedObject object = adapter.adapt(someObject, MultiFacetedObject.class);
		if (object != null) {
			System.out.println(object.identify());
		}

		Greeter greeter = adapter.adapt(someObject, Greeter.class);
		if (greeter != null) {
			System.out.println("This object can greet.");
			greeter.greet();
		}

		ThingWithId thingWithId = adapter.adapt(someObject, ThingWithId.class);
		if (thingWithId != null) {
			System.out.println("This object has an id, which is "
					+ thingWithId.getUniqueId());
		}

	}

	@Override
	public Object start(IApplicationContext context) throws Exception {
		// Inject services into this snippet
		SnippetSetup.setup(this);

		tryAdapters(new MultiFacetedObject("Fred"));
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
}
