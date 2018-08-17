/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Handler for the <code>org.eclipse.ui.tests.commands.subtractInteger</code>
 * command.
 */
public class SubtractIntegerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Integer minuend = (Integer) event
				.getObjectParameterForExecution(CommandParameterTypeTest.MINUEND);
		Integer subtrahend = (Integer) event
				.getObjectParameterForExecution(CommandParameterTypeTest.SUBTRAHEND);
		return Integer.valueOf(minuend.intValue() - subtrahend.intValue());
	}
}
