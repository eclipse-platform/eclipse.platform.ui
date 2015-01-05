/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import java.util.Locale;

import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;

import junit.extensions.TestSetup;
import junit.framework.Test;

/**
 * @since 3.2
 *
 */
public class BindingTestSetup extends TestSetup {

	private Locale oldLocale;
	private ILogger oldLogger;
	private org.eclipse.jface.util.ILogger oldJFaceLogger;

	public BindingTestSetup(Test test) {
		super(test);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		oldLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
		oldLogger = Policy.getLog();
		Policy.setLog(new ILogger() {
			@Override
			public void log(IStatus status) {
				// we are not expecting anything in the log while we test.
				if (status.getException() != null) {
					throw new RuntimeException(status.getException());
				}
				fail();
			}
		});
		oldJFaceLogger = org.eclipse.jface.util.Policy.getLog();
		org.eclipse.jface.util.Policy.setLog(new org.eclipse.jface.util.ILogger(){
			@Override
			public void log(IStatus status) {
				// we are not expecting anything in the log while we test.
				if (status.getException() != null) {
					throw new RuntimeException(status.getException());
				}
				fail();
			}
		});
	}

	@Override
	protected void tearDown() throws Exception {
		Locale.setDefault(oldLocale);
		Policy.setLog(oldLogger);
		org.eclipse.jface.util.Policy.setLog(oldJFaceLogger);
		super.tearDown();
	}
}
