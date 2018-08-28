/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;

/**
 * @author Prakash G.R.
 * @since 3.7
 *
 */
public class DynamicHelpHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {

		BusyIndicator.showWhile(null, () -> PlatformUI.getWorkbench().getHelpSystem().displayDynamicHelp());
		return null;
	}

}
