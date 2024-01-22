/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.ExceptionHandler;

/**
 * This handler is an adaptation of the widget method handler allowing select
 * all to work even in some cases where the "selectAll" method does not exist.
 * This handler attempts to use "getTextLimit" and "setSelection" to do select
 * all. If this doesn't work, then it finally fails.
 *
 * @since 3.0
 */
public class SelectAllHandler extends WidgetMethodHandler {

	/**
	 * The parameters for a single point select all.
	 */
	private static final Class<?>[] METHOD_PARAMETERS = { Point.class };

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		final Method methodToExecute = getMethodToExecute();
		if (methodToExecute != null) {
			try {
				final Control focusControl = Display.getCurrent().getFocusControl();

				final int numParams = methodToExecute.getParameterTypes().length;

				if ((focusControl instanceof Composite)
						&& ((((Composite) focusControl).getStyle() & SWT.EMBEDDED) != 0)) {

					// we only support selectAll for swing components
					if (numParams != 0) {
						return null;
					}

					/*
					 * Okay. Have a seat. Relax a while. This is going to be a bumpy ride. If it is
					 * an embedded widget, then it *might* be a Swing widget. At the point where
					 * this handler is executing, the key event is already bound to be swallowed. If
					 * I don't do something, then the key will be gone for good. So, I will try to
					 * forward the event to the Swing widget. Unfortunately, we can't even count on
					 * the Swing libraries existing, so I need to use reflection everywhere. And, to
					 * top it off, I need to dispatch the event on the Swing event queue, which
					 * means that it will be carried out asynchronously to the SWT event queue.
					 */
					try {
						final Object focusComponent = getFocusComponent();
						if (focusComponent != null) {
							Runnable methodRunnable = () -> {
								try {
									methodToExecute.invoke(focusComponent);
									// and back to the UI thread :-)
									focusControl.getDisplay().asyncExec(() -> {
										if (!focusControl.isDisposed()) {
											focusControl.notifyListeners(SWT.Selection, null);
										}
									});
								} catch (final IllegalAccessException e1) {
									// The method is protected, so do
									// nothing.
								} catch (final InvocationTargetException e2) {
									/*
									 * I would like to log this exception -- and possibly show a dialog to the user
									 * -- but I have to go back to the SWT event loop to do this. So, back we go....
									 */
									focusControl.getDisplay()
											.asyncExec(() -> ExceptionHandler.getInstance()
													.handleException(new ExecutionException(
															"An exception occurred while executing " //$NON-NLS-1$
																	+ methodToExecute.getName(),
															e2.getTargetException())));
								}
							};

							swingInvokeLater(methodRunnable);
						}
					} catch (final ClassNotFoundException e) {
						// There is no Swing support, so do nothing.

					} catch (final NoSuchMethodException e) {
						// The API has changed, which seems amazingly unlikely.
						throw new Error("Something is seriously wrong here"); //$NON-NLS-1$
					}
				} else if (numParams == 0) {
					// This is a no-argument selectAll method.
					methodToExecute.invoke(focusControl);
					focusControl.notifyListeners(SWT.Selection, null);

				} else if (numParams == 1) {
					// This is a single-point selection method.
					final Method textLimitAccessor = focusControl.getClass().getMethod("getTextLimit"); //$NON-NLS-1$
					final Integer textLimit = (Integer) textLimitAccessor.invoke(focusControl);
					final Object[] parameters = { new Point(0, textLimit.intValue()) };
					methodToExecute.invoke(focusControl, parameters);
					if (!(focusControl instanceof Combo)) {
						focusControl.notifyListeners(SWT.Selection, null);
					}

				} else {
					/*
					 * This means that getMethodToExecute() has been changed, while this method
					 * hasn't.
					 */
					throw new ExecutionException("Too many parameters on select all", new Exception()); //$NON-NLS-1$

				}

			} catch (InvocationTargetException e) {
				throw new ExecutionException("An exception occurred while executing " //$NON-NLS-1$
						+ getMethodToExecute(), e.getTargetException());

			} catch (IllegalAccessException | NoSuchMethodException e) {
				// I can't get the text limit. Do nothing.

			}
		}

		return null;
	}

	/**
	 * Looks up the select all method on the given focus control.
	 *
	 * @return The method on the focus control; <code>null</code> if none.
	 */
	@Override
	protected Method getMethodToExecute() {
		Method method = super.getMethodToExecute();

		// Let's see if we have a control that supports point-based selection.
		if (method == null) {
			final Control focusControl = Display.getCurrent().getFocusControl();
			if (focusControl != null) {
				try {
					method = focusControl.getClass().getMethod("setSelection", //$NON-NLS-1$
							METHOD_PARAMETERS);
				} catch (NoSuchMethodException e) {
					// Do nothing.
				}
			}
		}

		return method;
	}

	/**
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		// The name is always "selectAll".
		methodName = "selectAll"; //$NON-NLS-1$
	}
}
