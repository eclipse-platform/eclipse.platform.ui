/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
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
 *     Matthew Hall - bug 258774, 260316
 ******************************************************************************/
package org.eclipse.core.tests.databinding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class PolicyTest {

	@Test
	public void testConstructor() {
		// cover the constructor too
		new Policy();
	}

	@Test
	public void testDummyLog() {
		ILogger oldLog = Policy.getLog();
		PrintStream oldErr = System.err;
		try {
			// this should reset to using dummy log
			Policy.setLog(null);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			System.setErr(ps);
			IStatus status = new Status(IStatus.CANCEL, "somePluginId",
					"someMessage", new RuntimeException());
			Policy.getLog().log(status);
			ps.flush();
			String message = new String(os.toByteArray());
			System.out.println("testDummyLog message: " + message);
			assertTrue("expecting severity", message.contains("CANCEL"));
			assertTrue("expecting plugin id", message.contains("somePluginId"));
			assertTrue("expecting message", message.contains("someMessage"));
			assertTrue("expecting RuntimeException", message.contains("RuntimeException"));
		} finally {
			Policy.setLog(oldLog);
			System.setErr(oldErr);
		}
	}

	@Test
	public void testCustomLog() {
		ILogger oldLog = Policy.getLog();
		try {
			final IStatus[] statusHolder = new IStatus[1];
			Policy.setLog(status -> statusHolder[0] = status);
			IStatus status = new Status(IStatus.CANCEL, "somePluginId",
					"someMessage", new RuntimeException());
			Policy.getLog().log(status);
			assertEquals(status, statusHolder[0]);
		} finally {
			Policy.setLog(oldLog);
		}
	}
}