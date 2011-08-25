/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.examples.services.snippets.adapter;

import javax.inject.Inject;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.e4.examples.services.snippets.SnippetSetup;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * This snippet demonstrates user of the Adapter service.
 * 
 */
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

	public Object start(IApplicationContext context) throws Exception {
		// Inject services into this snippet
		SnippetSetup.setup(this);
		
		tryAdapters(new MultiFacetedObject("Fred"));
		return IApplication.EXIT_OK;
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}
}
