/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.annotations.Inject;

/**
 *
 */
public class ObjectWithAnnotations extends ObjectBasic {
	
	// Check that no extra calls are made
	@Inject(optional=true)
	public Object diMissing = null;
	public Object myMissing = null;
	public int setMissingCalled;
	// tests incompatible types
	@Inject(optional=true)
	public ObjectBasic diBoolean = null;
	public ObjectBasic myBoolean = null;
	public int setBooleanCalled;

	public ObjectWithAnnotations() {
		setMissingCalled = 0;
		setBooleanCalled = 0;
	}
	
	@Inject(optional=true)
	public void setMissingViaMethod(Object object) {
		myMissing = object;
		setMissingCalled++;
	}

	@Inject(optional=true)
	public void setBooleanViaMethod(ObjectBasic injector) {
		myBoolean = injector;
		setBooleanCalled++;
	}

}
