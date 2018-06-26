/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.uirscheme.suite;

import org.eclipse.urischeme.internal.UriSchemeProcessorUnitTest;
import org.eclipse.urischeme.internal.registration.TestUnitDesktopFileWriter;
import org.eclipse.urischeme.internal.registration.TestUnitPlistFileWriter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		UriSchemeProcessorUnitTest.class, //
		TestUnitPlistFileWriter.class, //
		TestUnitDesktopFileWriter.class
})
public class AllUnitTests {
}
