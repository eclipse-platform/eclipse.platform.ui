/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.session;

/**
 * TODO It should live in the UI tests instead.
 */
public class UIPerformanceSessionTestSuite extends PerformanceSessionTestSuite {

	public UIPerformanceSessionTestSuite(String pluginId, int timesToRun) {
		super(pluginId, timesToRun);
		setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);
	}

	public UIPerformanceSessionTestSuite(String pluginId, int timesToRun, Class theClass) {
		super(pluginId, timesToRun, theClass);
		setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);
	}

	public UIPerformanceSessionTestSuite(String pluginId, int timesToRun, Class theClass, String name) {
		super(pluginId, timesToRun, theClass, name);
		setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);
	}

	public UIPerformanceSessionTestSuite(String pluginId, int timesToRun, String name) {
		super(pluginId, timesToRun, name);
		setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);
	}

}
