/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Bruno Haible haible@ilog.fr - bug 228890
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.ExceptionHandler;

/**
 * Handles the cut command in both dialogs and windows. This handler is enabled
 * if the focus control supports the "cut", "copy" or "paste" methods.
 *
 * @since 3.0
 */
public class WidgetMethodHandler extends AbstractHandler implements IExecutableExtension {

	/**
	 * The parameters to pass to the method this handler invokes. This handler
	 * always passes no parameters.
	 */
	public WidgetMethodHandler() {
		display = Display.getCurrent();
		if (display != null) {
			focusListener = event -> updateEnablement();
			display.addFilter(SWT.FocusIn, focusListener);
		}
	}

	void updateEnablement() {
		boolean rc = isHandled();
		if (rc != isEnabled()) {
			setBaseEnabled(rc);
		}
	}

	/**
	 * The name of the method to be invoked by this handler. This value should never
	 * be <code>null</code>.
	 */
	protected String methodName;
	private Listener focusListener;
	private Display display;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Method methodToExecute = getMethodToExecute();
		if (methodToExecute != null) {
			try {
				final Control focusControl = Display.getCurrent().getFocusControl();
				if ((focusControl instanceof Composite)
						&& ((focusControl.getStyle() & SWT.EMBEDDED) != 0)) {
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

				} else {

					methodToExecute.invoke(focusControl);
				}

			} catch (IllegalAccessException e) {
				// The method is protected, so do nothing.

			} catch (InvocationTargetException e) {
				throw new ExecutionException("An exception occurred while executing " //$NON-NLS-1$
						+ methodToExecute.getName(), e.getTargetException());

			}
		}

		return null;
	}

	/**
	 * Invoke a runnable on the swing EDT.
	 */
	protected void swingInvokeLater(Runnable methodRunnable)
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final Class<?> swingUtilitiesClass = Class.forName("javax.swing.SwingUtilities"); //$NON-NLS-1$
		final Method swingUtilitiesInvokeLaterMethod = swingUtilitiesClass.getMethod("invokeLater", //$NON-NLS-1$
				Runnable.class);
		swingUtilitiesInvokeLaterMethod.invoke(swingUtilitiesClass, methodRunnable);
	}

	/**
	 * Find the swing focus component, if it is available.
	 *
	 * @return Hopefully, the swing focus component, but it can return
	 *         <code>null</code>.
	 */
	protected Object getFocusComponent()
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		/*
		 * Before JRE 1.4, one has to use
		 * javax.swing.FocusManager.getCurrentManager().getFocusOwner(). Since JRE 1.4,
		 * one has to use java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager
		 * ().getFocusOwner(); the use of the older API would install a
		 * LegacyGlueFocusTraversalPolicy which causes endless recursions in some
		 * situations.
		 */
		Class<?> keyboardFocusManagerClass = null;
		try {
			keyboardFocusManagerClass = Class.forName("java.awt.KeyboardFocusManager"); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			// switch to the old guy
		}
		if (keyboardFocusManagerClass != null) {
			// Use JRE 1.4 API
			final Method keyboardFocusManagerGetCurrentKeyboardFocusManagerMethod = keyboardFocusManagerClass
					.getMethod("getCurrentKeyboardFocusManager"); //$NON-NLS-1$
			final Object keyboardFocusManager = keyboardFocusManagerGetCurrentKeyboardFocusManagerMethod
					.invoke(keyboardFocusManagerClass);
			final Method keyboardFocusManagerGetFocusOwner = keyboardFocusManagerClass.getMethod("getFocusOwner"); //$NON-NLS-1$
			return keyboardFocusManagerGetFocusOwner.invoke(keyboardFocusManager);
		}
		// Use JRE 1.3 API
		final Class<?> focusManagerClass = Class.forName("javax.swing.FocusManager"); //$NON-NLS-1$
		final Method focusManagerGetCurrentManagerMethod = focusManagerClass.getMethod("getCurrentManager"); //$NON-NLS-1$
		final Object focusManager = focusManagerGetCurrentManagerMethod.invoke(focusManagerClass);
		final Method focusManagerGetFocusOwner = focusManagerClass.getMethod("getFocusOwner"); //$NON-NLS-1$
		return focusManagerGetFocusOwner.invoke(focusManager);

	}

	@Override
	public final boolean isHandled() {
		return getMethodToExecute() != null;
	}

	/**
	 * Looks up the method on the focus control.
	 *
	 * @return The method on the focus control; <code>null</code> if none.
	 */
	protected Method getMethodToExecute() {
		Display display = Display.getCurrent();
		if (display == null)
			return null;
		final Control focusControl = display.getFocusControl();
		Method method = null;

		if (focusControl != null) {
			final Class<?> clazz = focusControl.getClass();
			try {
				method = clazz.getMethod(methodName);
			} catch (NoSuchMethodException e) {
				// Fall through...
			}
		}

		if ((method == null) && (focusControl instanceof Composite)
				&& ((focusControl.getStyle() & SWT.EMBEDDED) != 0)) {
			/*
			 * We couldn't find the appropriate method on the current focus control. It is
			 * possible that the current focus control is an embedded SWT composite, which
			 * could be containing some Swing components. If this is the case, then we
			 * should try to pass through to the underlying Swing component hierarchy.
			 * Insha'allah, this will work.
			 */
			try {
				final Object focusComponent = getFocusComponent();
				if (focusComponent != null) {
					final Class<?> clazz = focusComponent.getClass();

					try {
						method = clazz.getMethod(methodName);
					} catch (NoSuchMethodException e) {
						// Do nothing.
					}
				}
			} catch (final ClassNotFoundException e) {
				// There is no Swing support, so do nothing.

			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				// The API has changed, which seems amazingly unlikely.
				throw new Error("Something is seriously wrong here"); //$NON-NLS-1$
			}
		}

		return method;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		// The data is really just a string (i.e., the method name).
		methodName = data.toString();
	}

	@Override
	public void dispose() {
		if (display != null && !display.isDisposed()) {
			display.removeFilter(SWT.FocusIn, focusListener);
		}
		display = null;
		focusListener = null;
	}
}
