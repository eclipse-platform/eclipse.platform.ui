/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.indexing;

import junit.textui.TestRunner;

public class StandAloneFieldTest {

	public static void main(String[] args) {
		TestRunner.run(BasicFieldTest.suite(new StandAloneTestEnvironment()));
	}

}
