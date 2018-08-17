/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.ui.examples.urischemehandler.commandHandlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * Command handler that creates an <code>SWT.OpenUlr</code> event to "simulate"
 * a user clicking on an URL.
 *
 */
public class OpenUrlEventCreationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent e) {
		Event event = new Event();
		event.text = "hello://demo.url"; //$NON-NLS-1$
		event.type = SWT.OpenUrl;

		invokeSendEvent(event);

		return null;
	}

	private void invokeSendEvent(Event event) {
		Display display = Display.getDefault();
		try {
			Method sendEventMethod = display.getClass().getDeclaredMethod("sendEvent", int.class, Event.class); //$NON-NLS-1$
			sendEventMethod.setAccessible(true);
			sendEventMethod.invoke(display, event.type, event);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}