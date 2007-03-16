/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A proxy for a handler that has been defined in XML. This delays the class
 * loading until the handler is really asked for information (besides the
 * priority or the command identifier). Asking a proxy for anything but the
 * attributes defined publicly in this class will cause the proxy to instantiate
 * the proxied handler.
 * </p>
 * 
 * @since 3.0
 */
public final class LegacyHandlerProxy extends AbstractHandler {

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real handler.
	 */
	private static final String HANDLER_ATTRIBUTE_NAME = "handler"; //$NON-NLS-1$

	/**
	 * The configuration element from which the handler can be created. This
	 * value will exist until the element is converted into a real class -- at
	 * which point this value will be set to <code>null</code>.
	 */
	private IConfigurationElement configurationElement;

	/**
	 * The real handler. This value is <code>null</code> until the proxy is
	 * forced to load the real handler. At this point, the configuration element
	 * is converted, nulled out, and this handler gains a reference.
	 */
	private IHandler handler;

	/**
	 * Constructs a new instance of <code>HandlerProxy</code> with all the
	 * information it needs to try to avoid loading until it is needed.
	 * 
	 * @param newConfigurationElement
	 *            The configuration element from which the real class can be
	 *            loaded at run-time.
	 */
	public LegacyHandlerProxy(
			final IConfigurationElement newConfigurationElement) {
		configurationElement = newConfigurationElement;
		handler = null;
	}

	/**
	 * Passes the dipose on to the proxied handler, if it has been loaded.
	 */
	public void dispose() {
		if (handler != null) {
			handler.dispose();
		}
	}

	/**
	 * @see IHandler#execute(Map)
	 */
	public Object execute(Map parameters) throws ExecutionException {
		if (loadHandler()) {
			return handler.execute(parameters);
		}

		return null;
	}

	/**
	 * @see IHandler#getAttributeValuesByName()
	 */
	public Map getAttributeValuesByName() {
		if (loadHandler()) {
			return handler.getAttributeValuesByName();
		} else {
			return Collections.EMPTY_MAP;
		}
	}

	/**
	 * Loads the handler, if possible. If the handler is loaded, then the member
	 * variables are updated accordingly.
	 * 
	 * @return <code>true</code> if the handler is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadHandler() {
		if (handler == null) {
			// Load the handler.
			try {
				handler = (IHandler) configurationElement
						.createExecutableExtension(HANDLER_ATTRIBUTE_NAME);
				configurationElement = null;
				return true;
			} catch (final CoreException e) {
				/*
				 * TODO If it can't be instantiated, should future attempts to
				 * instantiate be blocked?
				 */
				final String message = "The proxied handler for '" + configurationElement.getAttribute(HANDLER_ATTRIBUTE_NAME) //$NON-NLS-1$
						+ "' could not be loaded"; //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;
			}
		}

		return true;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();

		buffer.append("LegacyProxy("); //$NON-NLS-1$
		if (handler == null) {
			final String className = configurationElement
					.getAttribute(HANDLER_ATTRIBUTE_NAME);
			buffer.append(className);
		} else {
			buffer.append(handler);
		}
		buffer.append(')');

		return buffer.toString();
	}
}
