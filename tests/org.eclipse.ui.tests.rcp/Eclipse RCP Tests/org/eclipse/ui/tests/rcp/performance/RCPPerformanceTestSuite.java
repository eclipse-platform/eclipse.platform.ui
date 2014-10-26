/*******************************************************************************
 * Copyright (c) 2004, 2006, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 436344 
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.performance;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @since 3.1
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({PlatformUIPerfTest.class , EmptyWorkbenchPerfTest.class})
public class RCPPerformanceTestSuite {

}
