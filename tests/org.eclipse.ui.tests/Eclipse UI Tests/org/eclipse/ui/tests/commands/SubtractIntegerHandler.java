/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Integer minuend = (Integer) event
				.getObjectParameterForExecution(CommandParameterTypeTest.MINUEND);
		Integer subtrahend = (Integer) event
				.getObjectParameterForExecution(CommandParameterTypeTest.SUBTRAHEND);
		return new Integer(minuend.intValue() - subtrahend.intValue());
	}
}
