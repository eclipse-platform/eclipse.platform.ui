/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Map;
import org.eclipse.e4.core.commands.internal.HandlerServiceHandler;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * @since 3.5
 *
 */
public class WorkbenchHandlerServiceHandler extends HandlerServiceHandler implements
		IElementUpdater {

	/**
	 * @param commandId
	 */
	public WorkbenchHandlerServiceHandler(String commandId) {
		super(commandId);
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		final IEclipseContext executionContext = getExecutionContext(null);
		if (executionContext == null) {
			return;
		}
		Object handler = HandlerServiceImpl.lookUpHandler(executionContext, commandId);
		if (handler instanceof IElementUpdater) {
			((IElementUpdater) handler).updateElement(element, parameters);
		}
	}

}
