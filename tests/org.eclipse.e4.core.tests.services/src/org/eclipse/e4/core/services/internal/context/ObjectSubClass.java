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

import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.core.services.context.IEclipseContext;

/**
 * Test class to check injection mechanism into classes with inheritance
 */
public class ObjectSubClass extends ObjectSuperClass {
	/* package */Integer di_Integer;
	private Object myObject;

	public int setObjectCalled;
	public int setOverriddenCalled;
	public int setSubFinalized;
	public int postConstructSetObjectCalled;
	public int postConstructSetOverriddenCalled;
	public int subPostConstructCount;
	public int subPreDestroyCount;
	public int overriddenPreDestroyCount;

	public ObjectSubClass() {
		super();
		setObjectCalled = 0;
		setSubFinalized = 0;
	}

	public void setObjectViaMethod(Object object) {
		myObject = object;
		setObjectCalled++;
	}

	public void setOverriddenMethod(Object o) {
		setOverriddenCalled++;
	}

	public void contextSet(IEclipseContext context) {
		super.contextSet(context);
		setSubFinalized++;
	}

	public Integer getInteger() {
		return di_Integer;
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

	@PostConstruct
	public void subPostConstruct() {
		postConstructSetObjectCalled = setObjectCalled;
		postConstructSetStringCalled = setStringCalled;
		postConstructSetOverriddenCalled = setOverriddenCalled;
		subPostConstructCount++;
	}

	@PreDestroy
	public void subPreDestroy() {
		subPreDestroyCount++;

	}

	@PreDestroy()
	public void overriddenPreDestroy() {
		overriddenPreDestroyCount++;
	}

}
