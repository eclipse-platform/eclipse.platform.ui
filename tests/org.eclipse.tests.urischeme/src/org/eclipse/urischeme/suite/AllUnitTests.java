/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.suite;

import org.eclipse.urischeme.TestUnitAutoRegisterSchemeHandlersJob;
import org.eclipse.urischeme.internal.UriSchemeProcessorUnitTest;
import org.eclipse.urischeme.internal.registration.TestUnitDesktopFileWriter;
import org.eclipse.urischeme.internal.registration.TestUnitPlistFileWriter;
import org.eclipse.urischeme.internal.registration.TestUnitRegistrationLinux;
import org.eclipse.urischeme.internal.registration.TestUnitRegistrationMacOsX;
import org.eclipse.urischeme.internal.registration.TestUnitRegistrationWindows;
import org.eclipse.urischeme.internal.registration.TestUnitRegistryWriter;
import org.eclipse.urischeme.internal.registration.TestUnitWinRegistry;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

@Suite
@SelectClasses({ UriSchemeProcessorUnitTest.class, //
		TestUnitPlistFileWriter.class, //
		TestUnitDesktopFileWriter.class, //
		TestUnitRegistrationMacOsX.class, //
		TestUnitRegistrationLinux.class, //
		TestUnitRegistrationWindows.class, //
		TestUnitRegistryWriter.class, //
		TestUnitWinRegistry.class, //
		TestUnitAutoRegisterSchemeHandlersJob.class })
public class AllUnitTests {
}
