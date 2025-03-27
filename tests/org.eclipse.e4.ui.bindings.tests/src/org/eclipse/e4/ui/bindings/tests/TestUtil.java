/*******************************************************************************
 * Copyright (c) 2018 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.bindings.tests;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;

public class TestUtil {

	private static IEclipseContext appContext;

	public static IEclipseContext getGlobalContext() {
		if (appContext == null) {
			synchronized (TestUtil.class) {
				appContext = E4Application.createDefaultContext();
			}
		}

		return appContext;
	}

}
