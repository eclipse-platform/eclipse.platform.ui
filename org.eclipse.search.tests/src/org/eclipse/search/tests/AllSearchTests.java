/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.search.tests.filesearch.AllFileSearchTests;
import org.eclipse.search.core.tests.AllSearchModelTests;

public class AllSearchTests extends TestSuite {

	public static Test suite() {
		return new AllSearchTests();
	}

	public AllSearchTests() {
		addTest(AllFileSearchTests.suite());
		addTest(AllSearchModelTests.suite());
	}

}
