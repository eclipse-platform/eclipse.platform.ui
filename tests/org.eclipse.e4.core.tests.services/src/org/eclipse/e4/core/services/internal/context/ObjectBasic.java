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
	
	public String diString;
	private Integer diInteger;
	protected IEclipseContext diContext;

	// tests incompatible types
	public ObjectBasic diBoolean = null;
	public ObjectBasic myBoolean = null;
	public int setBooleanCalled;

	private String myString;
	private Object myObject;

	private boolean finalized;
	private boolean disposed;
	public int setStringCalled;
	public int setObjectCalled;

	// Check that no extra calls are made
	public Object diMissing = null;
	public Object myMissing = null;
	public int setMissingCalled;

	public ObjectBasic() {
		setStringCalled = 0;
		setObjectCalled = 0;
		setMissingCalled = 0;
		setBooleanCalled = 0;
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

	public void setMissingViaMethod(Object object) {
		myMissing = object;
		setMissingCalled++;
	}

	public void setBooleanViaMethod(ObjectBasic injector) {
		myBoolean = injector;
		setBooleanCalled++;
	}

	public void contextSet(IEclipseContext context) {
		diContext = context;
		finalized = true;
	}

	public void contextDisposed(IEclipseContext context) {
		if (context != diContext)
			throw new IllegalArgumentException("Unexpected context");
		diContext = null;
		disposed = true;
	}

	public String getString() {
		return diString;
	}

	public Integer getInteger() {
		return diInteger;
	}

	public IEclipseContext getContext() {
		return diContext;
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
