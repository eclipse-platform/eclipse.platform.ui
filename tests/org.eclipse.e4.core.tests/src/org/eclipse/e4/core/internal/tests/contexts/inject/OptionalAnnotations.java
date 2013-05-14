/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;

public class OptionalAnnotations {

	@Inject @Optional
	public Float f = null;
	
	public Double d;
	public String s = new String("ouch");
	public Integer i;
	
	public int methodOptionalCalled = 0;
	public int methodRequiredCalled = 0;
	
	public OptionalAnnotations() {
		// placehodler
	}

	@Inject @Optional
	public void methodOptional(Double d) {
		this.d = d;
		methodOptionalCalled++;
	}

	@Inject
	public void methodRequired(@Optional String s, Integer i) {
		this.s = s;
		this.i = i;
		methodRequiredCalled++;
	}

}
