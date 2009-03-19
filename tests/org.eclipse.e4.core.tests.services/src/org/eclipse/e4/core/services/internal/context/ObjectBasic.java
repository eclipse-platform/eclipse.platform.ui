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
 * Test class to check injection mechanism
 */
public class ObjectBasic {
	
	public String di_String;
	private Integer di_Integer;
	protected IEclipseContext context;

	private String myString;
	private Object myObject;

	private boolean finalized;
	private boolean disposed;
	public int setStringCalled;
	public int setObjectCalled;

	public ObjectBasic() {
		setStringCalled = 0;
		setObjectCalled = 0;
		finalized = false;
		disposed = false;
	}

	public void setStringViaMethod(String string) {
		myString = string;
		setStringCalled++;
	}

	public void removeStringViaMethod(String string) {
		if (string != myString)
			throw new IllegalArgumentException("Unexpected string");
		myString = null;
	}

	public void setObjectViaMethod(Object object) {
		myObject = object;
		setObjectCalled++;
	}

	public void removeObjectViaMethod(Object object) {
		if (object != myObject)
			throw new IllegalArgumentException("Unexpected object");
		myObject = null;
	}

	public void contextSet(IEclipseContext context) {
		this.context = context;
		finalized = true;
	}

	public void contextDisposed(IEclipseContext context) {
		if (context != context)
			throw new IllegalArgumentException("Unexpected context");
		context = null;
		disposed = true;
	}

	public String getString() {
		return di_String;
	}

	public Integer getInteger() {
		return di_Integer;
	}

	public IEclipseContext getContext() {
		return context;
	}

	public String getStringViaMethod() {
		return myString;
	}

	public Object getObjectViaMethod() {
		return myObject;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public boolean isDisposed() {
		return disposed;
	}

}
