/*******************************************************************************
 * Copyright (c) 2006, 2020 IBM Corporation and others.
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	FailingParticipantTests.class,
	SharedTextChangeTests.class,
	CancelingParticipantTests.class,
	MoveRefactoringWithRefUpdateTest.class
})
public class ParticipantTests {
}
