/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.context.IEclipseContext;

/**
 * Test class to check injection mechanism into classes with inheritance
 */
public class ObjectSuperClass {

	protected IEclipseContext context;
	private String inject__String;
	private String myString;

	public int postConstructSetStringCalled;
	public int setFinalizedCalled = 0;
	public int setStringCalled = 0;
	public int superPostConstructCount;
	public int superPreDestroyCount;
	public int setOverriddenCalled = 0;

	public ObjectSuperClass() {
		// placeholder
	}

	public void contextSet(IEclipseContext context) {
		this.context = context;
		setFinalizedCalled++;
	}

	public IEclipseContext getContext() {
		return context;
	}

	public int getFinalizedCalled() {
		return setFinalizedCalled;
	}

	public String getString() {
		return inject__String;
	}

	public String getStringViaMethod() {
		return myString;
	}

	public void inject__OverriddenMethod(Float f) {
		setOverriddenCalled++;
	}

	public void inject__StringViaMethod(String string) {
		myString = string;
		setStringCalled++;
	}

}
