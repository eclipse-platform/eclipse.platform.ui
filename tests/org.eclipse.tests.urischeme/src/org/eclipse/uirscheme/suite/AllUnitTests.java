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
package org.eclipse.uirscheme.suite;

import org.eclipse.urischeme.internal.UriSchemeProcessorUnitTest;
import org.eclipse.urischeme.internal.registration.TestUnitDesktopFileWriter;
import org.eclipse.urischeme.internal.registration.TestUnitLsregisterParser;
import org.eclipse.urischeme.internal.registration.TestUnitPlistFileWriter;
import org.eclipse.urischeme.internal.registration.TestUnitRegistrationLinux;
import org.eclipse.urischeme.internal.registration.TestUnitRegistrationMacOsX;
import org.eclipse.urischeme.internal.registration.TestUnitRegistrationWindows;
import org.eclipse.urischeme.internal.registration.TestUnitRegistryWriter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		UriSchemeProcessorUnitTest.class, //
		TestUnitPlistFileWriter.class, //
		TestUnitDesktopFileWriter.class, //
		TestUnitLsregisterParser.class, //
		TestUnitRegistrationMacOsX.class, //
		TestUnitRegistrationLinux.class, //
		TestUnitRegistrationWindows.class, //
		TestUnitRegistryWriter.class
})
public class AllUnitTests {
}
