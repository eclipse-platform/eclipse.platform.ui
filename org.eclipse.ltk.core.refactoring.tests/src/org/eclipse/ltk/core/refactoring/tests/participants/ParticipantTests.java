/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ParticipantTests {

	public static Test suite() {
		TestSuite suite= new TestSuite(ParticipantTests.class.getName());
		suite.addTestSuite(FailingParticipantTests.class);
		suite.addTestSuite(SharedTextChangeTests.class);
		suite.addTestSuite(CancelingParticipantTests.class);
		return suite;
	}
}