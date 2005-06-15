/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

/**
 * A handler that is intended to be defined in XML. These handlers support the
 * concept of executable extensions, defined by Platform Core. It is okay for
 * subclasses to never be used as executable extension. This default
 * implementation of <code>setInitializationData</code> is only intended as a
 * convenience for developers.
 * 
 * @since 3.1
 */
public abstract class ExecutableExtensionHandler extends AbstractHandler
        implements IExecutableExtension {

    /**
     * Initializes this handler with data provided from XML. By default, an
     * <code>ExecutableExtensionHandler</code> will do nothing with this
     * information. Subclasses should override if they expect parameters from
     * XML.
     * 
     * @param config
     *            the configuration element used to trigger this execution. It
     *            can be queried by the executable extension for specific
     *            configuration properties
     * @param propertyName
     *            the name of an attribute of the configuration element used on
     *            the <code>createExecutableExtension(String)</code> call.
     *            This argument can be used in the cases where a single
     *            configuration element is used to define multiple executable
     *            extensions.
     * @param data
     *            adapter data in the form of a <code>String</code>, a
     *            <code>Hashtable</code>, or <code>null</code>.
     * @throws CoreException
     *             if error(s) detected during initialization processing
     * 
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
     *      java.lang.String, java.lang.Object)
     */
    public void setInitializationData(final IConfigurationElement config,
            final String propertyName, final Object data) throws CoreException {
        // Do nothing, by default
    }
}
