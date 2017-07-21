/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.progress;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

public class CommandHandler extends AbstractHandler {


	public boolean executed;
	@Override
	public Object execute(ExecutionEvent event) {
		executed = true;
		return null;
	}

}
