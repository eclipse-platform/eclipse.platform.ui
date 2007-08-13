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

	public BindingTestSetup(Test test) {
		super(test);
	}

	protected void setUp() throws Exception {
		super.setUp();
		oldLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
		Policy.setLog(new ILogger() {
			public void log(IStatus status) {
				fail();
			}
		});
	}

	protected void tearDown() throws Exception {
		Locale.setDefault(oldLocale);
		super.tearDown();
	}
}
