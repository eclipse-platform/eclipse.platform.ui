/*******************************************************************************
 * Copyright (c) 2017, Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
