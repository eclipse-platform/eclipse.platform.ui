/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.internal.commands.ws.HandlerProxy;

final class Persistence {

    final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$

    final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$	

    final static String PACKAGE_FULL = PACKAGE_PREFIX + '.' + PACKAGE_BASE;

    final static String TAG_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$

    final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	

    final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$

    final static String TAG_COMMAND = "command"; //$NON-NLS-1$	

    final static String TAG_COMMAND_ID = "commandId"; //$NON-NLS-1$	

    final static String TAG_CONTEXT_ID = "contextId"; //$NON-NLS-1$	

    final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

    final static String TAG_HANDLER = "handlerSubmission"; //$NON-NLS-1$

    final static String TAG_ID = "id"; //$NON-NLS-1$

    final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$	

    final static String TAG_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$	

    final static String TAG_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$	

    final static String TAG_KEY_SEQUENCE_BINDING = "keyBinding"; //$NON-NLS-1$

    final static String TAG_LOCALE = "locale"; //$NON-NLS-1$

    final static String TAG_NAME = "name"; //$NON-NLS-1$	

    final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$

    final static String TAG_PLATFORM = "platform"; //$NON-NLS-1$	

    final static String TAG_SOURCE_ID = "sourceId"; //$NON-NLS-1$

    /**
     * Reads the handler from XML, and creates a proxy to contain it. The proxy
     * will only instantiate the handler when the handler is first asked for
     * information.
     * 
     * @param configurationElement
     *            The configuration element to read; must not be
     *            <code>null</code>.
     * @return The handler proxy for the given definition; never
     *         <code>null</code>.
     */
    static IHandler readHandlerSubmissionDefinition(
            IConfigurationElement configurationElement) {
        final String commandId = configurationElement
                .getAttribute(TAG_COMMAND_ID);

        return new HandlerProxy(commandId, configurationElement);
    }

    private Persistence() {
    }
}
