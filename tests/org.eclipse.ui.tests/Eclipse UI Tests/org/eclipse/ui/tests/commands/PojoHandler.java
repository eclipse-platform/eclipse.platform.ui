/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

import jakarta.inject.Inject;

/**
 * A handler contributed to <code>org.eclipse.ui.handlers</code> that implements
 * neither <code>IHandler</code> nor any other platform interface.
 */
public class PojoHandler {

	public static boolean executed;

	public static boolean canExecute = true;

	public static IEclipseContext injectedContext;

	@Inject
	void setContext(IEclipseContext context) {
		injectedContext = context;
	}

	@CanExecute
	public boolean canExecute() {
		return canExecute;
	}

	@Execute
	public Object execute() {
		executed = true;
		return "executed"; //$NON-NLS-1$
	}
}
