/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.watson;

import org.eclipse.core.internal.watson.IDeltaFilter;

public class TestDeltaFilter implements IDeltaFilter {
/**
 * TestDeltaFilter constructor comment.
 */
public TestDeltaFilter() {
	super();
}
/**
 * Accepts all flags
 */
public boolean includeElement(int flag) {
	return true;
}
}
