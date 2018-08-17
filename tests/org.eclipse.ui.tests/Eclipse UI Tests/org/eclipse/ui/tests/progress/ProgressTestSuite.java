/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
 *     Red Hat Inc. - Bug 474132
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for the Progress View and related API
 *
 * @since 3.6
 * @author Prakash G.R. (grprakash@in.ibm.com)
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ProgressContantsTest.class,
	ProgressViewTests.class,
	JobInfoTest.class,
	JobInfoTestOrdering.class,
	ProgressAnimationItemTest.class,
	AccumulatingProgressMonitorTest.class
})
public class ProgressTestSuite {

}
