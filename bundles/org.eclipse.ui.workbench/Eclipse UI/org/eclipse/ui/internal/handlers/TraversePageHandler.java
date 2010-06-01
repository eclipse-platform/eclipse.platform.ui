/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import java.lang.reflect.Method;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * This handler is an adaptation of the widget method handler that implements
 * page traversal via {@link SWT#TRAVERSE_PAGE_NEXT} and
 * {@link SWT#TRAVERSE_PAGE_PREVIOUS} events.
 * 
 * @since 3.5
 */
public class TraversePageHandler extends WidgetMethodHandler {

	/**
	 * The parameters for traverse(int).
	 */
	private static final Class[] METHOD_PARAMETERS = { int.class };

	public final Object execute(final ExecutionEvent event) {
		Control focusControl = Display.getCurrent().getFocusControl();
		if (focusControl != null) {
			int traversal= "next".equals(methodName) ? SWT.TRAVERSE_PAGE_NEXT : SWT.TRAVERSE_PAGE_PREVIOUS; //$NON-NLS-1$
			focusControl.traverse(traversal, new Event());
		}

		return null;
	}

	/**
	 * Looks up the traverse(int) method on the given focus control.
	 * 
	 * @return The method on the focus control; <code>null</code> if none.
	 */
	protected Method getMethodToExecute() {
		final Control focusControl = Display.getCurrent().getFocusControl();
		if (focusControl != null) {
			try {
				return focusControl.getClass().getMethod("traverse", //$NON-NLS-1$
						METHOD_PARAMETERS);
			} catch (NoSuchMethodException e) {
				// Do nothing.
			}
		}
		return null;
	}

}
