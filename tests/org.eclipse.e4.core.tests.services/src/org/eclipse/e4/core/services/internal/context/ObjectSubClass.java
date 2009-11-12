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
public class ObjectSubClass extends ObjectSuperClass {
	/* package */Integer inject__Integer;

	private Object myObject;

	public int setObjectCalled = 0;
	public int setSubFinalized = 0;
	public int postConstructSetObjectCalled;
	public int postConstructSetOverriddenCalled;
	public int subPostConstructCount;
	public int subPreDestroyCount;
	public int overriddenPreDestroyCount;

	public ObjectSubClass() {
		super();
	}

	public void inject__ObjectViaMethod(Float f) {
		myObject = f;
		setObjectCalled++;
	}

	public void inject__OverriddenMethod(Float f) {
		setOverriddenCalled++;
	}

	public void contextSet(IEclipseContext context) {
		super.contextSet(context);
		setSubFinalized++;
	}

	public Integer getInteger() {
		return inject__Integer;
	}

	public Object getObjectViaMethod() {
		return myObject;
	}

	public int getFinalizedCount() {
		return setSubFinalized;
	}

	public int getOverriddenCount() {
		return setOverriddenCalled;
	}
}
