/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import junit.framework.TestCase;
import org.eclipse.core.internal.jobs.JobManager;

/**
 * Base class for tests using IJobManager
 */
public class AbstractJobManagerTest extends TestCase {
	protected JobManager manager;
	private FussyProgressProvider progressProvider;

	public AbstractJobManagerTest() {
		super();
	}
	public AbstractJobManagerTest(String name) {
		super(name);
	}
	protected void setUp() throws Exception {
		super.setUp();
		manager = JobManager.getInstance();
		progressProvider = new FussyProgressProvider();
		manager.setProgressProvider(progressProvider);
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		progressProvider.sanityCheck();
		manager.setProgressProvider(null);
	}
}
