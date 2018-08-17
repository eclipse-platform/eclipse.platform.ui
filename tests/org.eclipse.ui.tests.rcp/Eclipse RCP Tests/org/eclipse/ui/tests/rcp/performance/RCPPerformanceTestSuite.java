/*******************************************************************************
 * Copyright (c) 2004, 2006, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
