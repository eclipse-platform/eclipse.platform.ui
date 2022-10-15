/*******************************************************************************
 * Copyright (c) 2004, 2020 IBM Corporation and others.
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
package org.eclipse.ui.tests.concurrency;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The suite of tests related to concurrency and deadlock.
 *
 * @since 3.1
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ModalContextCrashTest.class,
	NestedSyncExecDeadlockTest.class,
	SyncExecWhileUIThreadWaitsForRuleTest.class,
	SyncExecWhileUIThreadWaitsForLock.class,
	NoFreezeWhileWaitingForRuleTest.class,
	TestBug105491.class,
	TestBug108162.class,
	TestBug98621.class,
	TransferRuleTest.class,
	Bug_262032.class,
	TestBug269121.class,
	TestGitHubBug227.class
})
public final class ConcurrencyTestSuite {
}
