/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.statushandlers;

import org.eclipse.core.tests.session.Setup;
import org.eclipse.core.tests.session.SetupManager.SetupException;
import org.eclipse.ui.tests.session.WorkbenchSessionTest;

/**
 * @since 3.5
 *
 */
public class StatusHandlerConfigurationSuite extends WorkbenchSessionTest {

	public StatusHandlerConfigurationSuite(String dataLocation) {
		super(dataLocation);
		System.out.println("initalization1");
	}

	public StatusHandlerConfigurationSuite(String dataLocation, Class<?> clazz) {
		super(dataLocation, clazz);
		System.out.println("initalization2");
	}

	@Override
	protected Setup newSetup() throws SetupException {
		Setup base = super.newSetup();
		System.out.println(base);
		base.setEclipseArgument("statushandler",
				"org.eclipse.ui.tests.statushandlers.FreeStatusHandler");
		System.out.println(base);
		return base;
	}



}
