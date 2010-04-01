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

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;

/**
 * Test class to check injection mechanism
 */
public class ObjectBasic implements IDisposable {

	// Injected directly
	public String inject__String;
	private Integer inject__Integer;

	// Injected indirectly
	public Double d;
	public Float f;
	public Character c;
	protected IEclipseContext context;

	// Test status
	public boolean finalized = false;
	public boolean disposed = false;
	public int setMethodCalled = 0;
	public int setMethodCalled2 = 0;

	public ObjectBasic() {
		// placeholder
	}

	public void inject__ObjectViaMethod(Double d) {
		setMethodCalled++;
		this.d = d;
	}

	public void inject__Arguments(Float f, Character c) {
		setMethodCalled2++;
		this.f = f;
		this.c = c;
	}

	public void inject__contextSet(IEclipseContext context) {
		this.context = context;
		finalized = true;
	}

	public void dispose() {
		this.context = null;
		disposed = true;
	}

	public Integer getInt() {
		return inject__Integer;
	}

}
