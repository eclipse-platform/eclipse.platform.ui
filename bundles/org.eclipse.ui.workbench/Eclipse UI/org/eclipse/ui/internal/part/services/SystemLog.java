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
package org.eclipse.ui.internal.part.services;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.part.components.services.IStatusFactory;
import org.eclipse.ui.internal.part.components.services.ISystemLog;
import org.osgi.framework.Bundle;

/**
 * @since 3.1
 */
public class SystemLog implements ISystemLog {
    
    private IStatusFactory factory;
    private Bundle pluginBundle;
    
    public SystemLog(Bundle pluginBundle, IStatusFactory factory) {
        this.pluginBundle = pluginBundle;
        this.factory = factory;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.component.services.IErrorContext#log(org.eclipse.core.runtime.IStatus)
     */
    public void log(IStatus toLog) {
        ILog log = Platform.getLog(pluginBundle);
        if (log != null) {
            log.log(toLog);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.component.services.IErrorContext#log(java.lang.Throwable)
     */
    public void log(Throwable t) {
        log(factory.newError(t));
    }

}
