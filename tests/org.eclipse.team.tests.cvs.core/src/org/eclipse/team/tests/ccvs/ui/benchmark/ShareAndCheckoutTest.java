/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui.benchmark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;

/**
 * Benchmark test which shares and checks out a large project
 */
public class ShareAndCheckoutTest extends BenchmarkTest {

	public ShareAndCheckoutTest() {
		super();
	}

	public ShareAndCheckoutTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(ShareAndCheckoutTest.class);
	}
		
	public void testShareAndCheckout() throws TeamException, ZipException, CoreException, IOException, InterruptedException, InvocationTargetException {
		IProject project = createUniqueProject(BenchmarkTestSetup.BIG_ZIP_FILE);
		shareProject(project);
		checkoutCopy(project, "-copy"); //$NON-NLS-1$
	}
}
