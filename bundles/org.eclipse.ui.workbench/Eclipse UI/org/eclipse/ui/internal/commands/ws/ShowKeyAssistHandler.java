/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands.ws;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;

/**
 * A handler that displays the key assist dialog when executed.
 * 
 * @since 3.1
 */
public class ShowKeyAssistHandler extends AbstractHandler implements
        IExecutableExtension {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.commands.IHandler#execute(java.util.Map)
     */
    public Object execute(Map parameterValuesByName) throws ExecutionException {
        PlatformUI.getWorkbench().getContextSupport().openKeyAssistDialog();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
     *      java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        // There is no data to pass.
    }
}