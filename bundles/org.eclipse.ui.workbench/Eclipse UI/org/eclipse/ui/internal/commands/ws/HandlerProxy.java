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
 * A proxy for a handler that has been defined in XML. This delays the class
 * loading until the handler is really asked for information (besides the
 * priority or the command identifier). Asking a proxy for anything but the
 * attributes defined publicly in this class will cause the proxy to instantiate
 * the proxied handler.
 * 
 * @since 3.0
 */
public final class HandlerProxy extends AbstractHandler {

    /**
     * The name of the configuration element attribute which contains the
     * information necessary to instantiate the real handler.
     */
    private static final String HANDLER_ATTRIBUTE_NAME = "handler"; //$NON-NLS-1$

    /**
     * The identifier for the command to which this proxy should be associated.
     * This value should never be <code>null</code>.
     */
    private final String commandId;

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
     * @param newCommandId
     *            The identifier for the command to which this proxy should be
     *            associated; must not be <code>null</code>.
     * @param newConfigurationElement
     *            The configuration element from which the real class can be
     *            loaded at run-time.
     */
    public HandlerProxy(final String newCommandId,
            final IConfigurationElement newConfigurationElement) {
        commandId = newCommandId;
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
    public Object execute(Map parameterValuesByName) throws ExecutionException {
        if (loadHandler()) { return handler.execute(parameterValuesByName); }

        return null;
    }

    /**
     * An accessor for the identifier of the command to which the proxied
     * handler should be associated.
     * 
     * @return The command identifier; should never be <code>null</code>.
     */
    final String getCommandId() {
        return commandId;
    }

    /**
     * @see IHandler#getAttributeValuesByName()
     */
    public Map getAttributeValuesByName() {
        if (loadHandler())
            return handler.getAttributeValuesByName();
        else
            return Collections.EMPTY_MAP;
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
                final String message = "The proxied handler for '" + commandId //$NON-NLS-1$
                        + "' could not be loaded"; //$NON-NLS-1$
                IStatus status = new Status(IStatus.ERROR,
                        WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
                WorkbenchPlugin.log(message, status);
                return false;
            }
        }

        return true;
    }
}
