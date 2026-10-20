/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.model.test.util;

public class E4UITestUtils {

	public static final String JENKINS_JOB_NAME_ENV_VAR = "JOB_NAME";

	/**
	 * Return {@code true} if we are probably running on Jenkins, i.e. if
	 * {@link #JENKINS_JOB_NAME_ENV_VAR} is set. Jenkins jobs are:
	 * <ul>
	 * <li>I-builds</li>
	 * <li>GitHub PR test job, excluding the Windows/Linux/MacOS test jobs ran with
	 * Tycho</li>
	 * </ul>
	 */
	public static boolean isRunningOnJenkins() {
		return System.getenv(JENKINS_JOB_NAME_ENV_VAR) != null;
	}
}
