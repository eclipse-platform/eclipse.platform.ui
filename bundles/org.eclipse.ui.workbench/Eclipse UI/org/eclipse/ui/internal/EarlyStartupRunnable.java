/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445484
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.misc.UIStats;

/**
 * A utility class used to call #earlyStartup on the proper instance for a given
 * configuration element.
 * 
 * @since 3.0
 */
public class EarlyStartupRunnable extends SafeRunnable {

    private IExtension extension;

    /**
     * @param extension
     *            must not be null
     */
    public EarlyStartupRunnable(IExtension extension) {
        this.extension = extension;
    }

    @Override
	public void run() throws Exception {
		IConfigurationElement[] configElements = extension.getConfigurationElements();

        // look for the startup tag in each element and run the extension
        for (IConfigurationElement element : configElements) {
            if (element != null&& element.getName().equals(IWorkbenchConstants.TAG_STARTUP)) {
                runEarlyStartup(getExecutableExtension(element));
            }
        }
    }

    @Override
	public void handleException(Throwable exception) {
		IStatus status = new Status(IStatus.ERROR, extension.getNamespaceIdentifier(), 0,
                "Unable to execute early startup code for an extension", //$NON-NLS-1$
                exception);
        WorkbenchPlugin.log("Unhandled Exception", status); //$NON-NLS-1$
    }

    private void runEarlyStartup(Object executableExtension) {
		if (executableExtension instanceof IStartup) {
			String methodName = executableExtension.getClass().getName() + ".earlyStartup"; //$NON-NLS-1$
			try {
				UIStats.start(UIStats.EARLY_STARTUP, methodName);
				((IStartup) executableExtension).earlyStartup();
			} finally {
				UIStats.end(UIStats.EARLY_STARTUP, executableExtension, methodName);
			}
		} else {
			IStatus status = new Status(IStatus.ERROR, extension.getNamespaceIdentifier(), 0,
                    "startup class must implement org.eclipse.ui.IStartup", //$NON-NLS-1$
                    null);
            WorkbenchPlugin.log("Bad extension specification", status); //$NON-NLS-1$
        }
    }

    /**
     * In 3.0 the class attribute is a mandatory element of the startup element.
     * 
     * @return an executable extension for this startup element or null if an
     *         extension (or plugin) could not be found
     */
    private Object getExecutableExtension(IConfigurationElement element)
            throws CoreException {
        return WorkbenchPlugin.createExtension(element,
                IWorkbenchConstants.TAG_CLASS);
    }

}
