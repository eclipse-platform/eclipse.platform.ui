/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import java.lang.reflect.Method;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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
	private static final Class<?>[] METHOD_PARAMETERS = { int.class };

	@Override
	public final Object execute(final ExecutionEvent event) {
		Control focusControl = Display.getCurrent().getFocusControl();
		if (focusControl != null) {
			int traversal = "next".equals(methodName) ? SWT.TRAVERSE_PAGE_NEXT : SWT.TRAVERSE_PAGE_PREVIOUS; //$NON-NLS-1$
			Control control = focusControl;
			do {
				if (control.traverse(traversal))
					return null;
				if (control instanceof Shell)
					return null;
				control = control.getParent();
			} while (control != null);
		}

		return null;
	}

	/**
	 * Looks up the traverse(int) method on the given focus control.
	 *
	 * @return The method on the focus control; <code>null</code> if none.
	 */
	@Override
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
