/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands.ws;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.FocusManager;
import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.internal.ExceptionHandler;

/**
 * Handles the cut command in both dialogs and windows. This handler is enabled
 * if the focus control supports the "cut" method.
 * 
 * @since 3.0
 */
public class WidgetMethodHandler extends AbstractHandler implements
		IExecutableExtension {

	/**
	 * The name of the attribute controlling the enabled state.
	 */
	private static final String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$

	/**
	 * The name of the attribute for the identifier.
	 */
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The parameters to pass to the method this handler invokes. This handler
	 * always passes no parameters.
	 */
	protected static final Class[] NO_PARAMETERS = new Class[0];

	/**
	 * The name of the method to be invoked by this handler. This value should
	 * never be <code>null</code>.
	 */
	protected String methodName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IHandler#execute(java.lang.Object)
	 */
	public Object execute(Map parameterValuesByName) throws ExecutionException {
		final Method methodToExecute = getMethodToExecute();
		if (methodToExecute != null) {
			try {
				final Control focusControl = Display.getCurrent()
						.getFocusControl();
				if ((focusControl instanceof Composite)
						&& ((((Composite) focusControl).getStyle() & SWT.EMBEDDED) != 0)) {
					final FocusManager focusManager = FocusManager
							.getCurrentManager();
					final Component focusComponent = focusManager
							.getFocusOwner();
					if (focusComponent != null) {
						Runnable methodRunnable = new Runnable() {
							public void run() {
								try {
									methodToExecute
											.invoke(focusComponent, null);
								} catch (final IllegalAccessException e) {
									// The method is protected, so do nothing.
								} catch (final InvocationTargetException e) {
									/*
									 * I would like to log this exception -- and
									 * possibly show a dialog to the user -- but
									 * I have to go back to the SWT event loop
									 * to do this. So, back we go....
									 */
									focusControl.getDisplay().asyncExec(
											new Runnable() {
												public void run() {
													ExceptionHandler
															.getInstance()
															.handleException(
																	new ExecutionException(
																			"An exception occurred while executing " //$NON-NLS-1$
																					+ methodToExecute
																							.getName(),
																			e
																					.getTargetException()));
												}
											});
								}
							}
						};
						SwingUtilities.invokeLater(methodRunnable);
					}

				} else {

					methodToExecute.invoke(focusControl, null);
				}

			} catch (IllegalAccessException e) {
				// The method is protected, so do nothing.

			} catch (InvocationTargetException e) {
				throw new ExecutionException(
						"An exception occurred while executing " //$NON-NLS-1$
								+ methodToExecute.getName(), e
								.getTargetException());

			}
		}

		return null;
	}

	public Map getAttributeValuesByName() {
		Map attributeValuesByName = new HashMap();
		attributeValuesByName.put(ATTRIBUTE_ENABLED,
				getMethodToExecute() == null ? Boolean.FALSE : Boolean.TRUE);
		attributeValuesByName.put(ATTRIBUTE_ID, null);
		return Collections.unmodifiableMap(attributeValuesByName);
	}

	/**
	 * Looks up the method on the focus control.
	 * 
	 * @return The method on the focus control; <code>null</code> if none.
	 */
	protected Method getMethodToExecute() {
		final Control focusControl = Display.getCurrent().getFocusControl();
		Method method = null;

		if (focusControl != null) {
			final Class clazz = focusControl.getClass();
			try {
				method = clazz.getMethod(methodName, NO_PARAMETERS);
			} catch (NoSuchMethodException e) {
				// Fall through...
			}
		}

		if ((method == null)
				&& (focusControl instanceof Composite)
				&& ((((Composite) focusControl).getStyle() & SWT.EMBEDDED) != 0)) {
			/*
			 * We couldn't find the appropriate method on the current focus
			 * control. It is possible that the current focus control is an
			 * embedded SWT composite, which could be containing some Swing
			 * components. If this is the case, then we should try to pass
			 * through to the underlying Swing component hierarchy. Insha'allah,
			 * this will work.
			 */
			final FocusManager focusManager = FocusManager.getCurrentManager();
			final Component focusComponent = focusManager.getFocusOwner();
			final Class clazz = focusComponent.getClass();

			try {
				method = clazz.getMethod(methodName, NO_PARAMETERS);
			} catch (NoSuchMethodException e) {
				// Do nothing.
			}
		}

		return method;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		// The data is really just a string (i.e., the method name).
		methodName = data.toString();
	}
}