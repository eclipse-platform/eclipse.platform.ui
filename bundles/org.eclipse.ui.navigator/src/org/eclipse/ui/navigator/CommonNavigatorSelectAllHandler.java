/*******************************************************************************
 * Copyright (c) 2003, 2026 IBM Corporation and others.
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
package org.eclipse.ui.navigator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * CommonNavigatorSelectAllHandler is the handler for the select all action.
 *
 * @since 3.14
 *
 */
public class CommonNavigatorSelectAllHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof CommonNavigator navigator) {
			navigator.getCommonViewer().getTree().selectAll();
		}
		return null;
	}
}
