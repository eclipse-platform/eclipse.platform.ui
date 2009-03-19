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

	private String di_String;
	protected IEclipseContext context;
	private String myString;

	public int setStringCalled;
	public int setFinalizedCalled;

	public ObjectSuperClass() {
		setStringCalled = 0;
		setFinalizedCalled = 0;
	}

	public void setStringViaMethod(String string) {
		myString = string;
		setStringCalled++;
	}

	public void contextSet(IEclipseContext context) {
		this.context = context;
		setFinalizedCalled++;
	}

	public String getString() {
		return di_String;
	}

	public IEclipseContext getContext() {
		return context;
	}

	public String getStringViaMethod() {
		return myString;
	}

	public int getFinalizedCalled() {
		return setFinalizedCalled;
	}
}
