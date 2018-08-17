/*******************************************************************************
 * Copyright (c) 2017, Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

public class CheckInvokedHandler extends AbstractHandler {

	public static boolean invoked = false;

	@Override
	public Object execute(ExecutionEvent event) {
		invoked = true;
		return Boolean.TRUE;
	}

}
