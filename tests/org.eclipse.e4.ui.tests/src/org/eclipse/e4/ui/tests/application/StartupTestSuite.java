/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Manumitting Technologies Inc - conversion to JUnit4-style suite
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ EModelServiceTest.class, EModelServiceFindTest.class, EModelServicePerspectiveFindTest.class,
		EModelServiceInsertTest.class, EPartServiceTest.class, ESelectionServiceTest.class, EventBrokerTest.class,
		HeadlessContactsDemoTest.class, HeadlessPhotoDemoTest.class, UIEventsTest.class,
		// UIContactsDemoTest.class,
		// UIPhotoDemoTest.class,
})
public class StartupTestSuite {
}
