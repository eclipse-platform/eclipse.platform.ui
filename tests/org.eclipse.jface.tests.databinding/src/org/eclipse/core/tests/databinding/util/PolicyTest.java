/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 258774, 260316
 ******************************************************************************/
package org.eclipse.core.tests.databinding.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.2
 *
 */
public class PolicyTest extends TestCase {

	public void testConstructor() {
		// cover the constructor too
		new Policy();
	}

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
			assertTrue("expecting severity", message.indexOf("CANCEL") != -1);
			assertTrue("expecting plugin id",
					message.indexOf("somePluginId") != -1);
			assertTrue("expecting message",
					message.indexOf("someMessage") != -1);
			assertTrue("expecting RuntimeException", message
					.indexOf("RuntimeException") != -1);
		} finally {
			Policy.setLog(oldLog);
			System.setErr(oldErr);
		}
	}

	public void testCustomLog() {
		ILogger oldLog = Policy.getLog();
		try {
			final IStatus[] statusHolder = new IStatus[1];
			Policy.setLog(new ILogger() {
				@Override
				public void log(IStatus status) {
					statusHolder[0] = status;
				}
			});
			IStatus status = new Status(IStatus.CANCEL, "somePluginId",
					"someMessage", new RuntimeException());
			Policy.getLog().log(status);
			assertEquals(status, statusHolder[0]);
		} finally {
			Policy.setLog(oldLog);
		}
	}
}