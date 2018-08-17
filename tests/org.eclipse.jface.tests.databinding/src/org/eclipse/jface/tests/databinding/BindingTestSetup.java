/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import static org.junit.Assert.fail;

import java.util.Locale;

import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @since 3.2
 *
 */
public class BindingTestSetup extends TestWatcher {

	private Locale oldLocale;
	private ILogger oldLogger;
	private org.eclipse.jface.util.ILogger oldJFaceLogger;

	@Override
	protected void starting(Description description) {
		oldLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
		oldLogger = Policy.getLog();
		Policy.setLog(this::log);
		oldJFaceLogger = org.eclipse.jface.util.Policy.getLog();
		org.eclipse.jface.util.Policy.setLog(this::log);
	}

	@Override
	protected void finished(Description description) {
		Locale.setDefault(oldLocale);
		Policy.setLog(oldLogger);
		org.eclipse.jface.util.Policy.setLog(oldJFaceLogger);
	}

	private void log(IStatus status) {
		// we are not expecting anything in the log while we test.
		if (status.getException() != null) {
			throw new RuntimeException(status.getException());
		}
		fail();
	}
}
