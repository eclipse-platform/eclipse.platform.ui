/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445484, 457132
 *******************************************************************************/
package org.eclipse.ui.internal;

import com.ibm.icu.text.MessageFormat;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
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
		if (configElements.length == 0) {
			missingStartupElementMessage("The org.eclipse.ui.IStartup extension from '" + //$NON-NLS-1$
						extension.getNamespaceIdentifier() + "' does not provide a valid '" //$NON-NLS-1$
					+ IWorkbenchConstants.TAG_STARTUP + "' element."); //$NON-NLS-1$
		}
        // look for the startup tag in each element and run the extension
        for (IConfigurationElement element : configElements) {
            if (element != null&& element.getName().equals(IWorkbenchConstants.TAG_STARTUP)) {
                runEarlyStartup(getExecutableExtension(element));
            }
        }
    }

	private void missingStartupElementMessage(String message) {
		IStatus status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null);
		WorkbenchPlugin.log(status);
	}

    @Override
	public void handleException(Throwable exception) {
		IStatus status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
				"Unable to execute early startup code for the org.eclipse.ui.IStartup extension contributed by the '" //$NON-NLS-1$
						+ extension.getNamespaceIdentifier() + "' plug-in.", //$NON-NLS-1$
                exception);
		WorkbenchPlugin.log(status);
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
			String message = executableExtension == null ?
					"The org.eclipse.ui.IStartup extension from '" + extension.getNamespaceIdentifier() //$NON-NLS-1$
					+ "' does not provide a valid class attribute." : //$NON-NLS-1$
					MessageFormat.format("Startup class {0} must implement org.eclipse.ui.IStartup", //$NON-NLS-1$
							executableExtension.getClass().getName());
			IStatus status =
					new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null);
			WorkbenchPlugin.log(status);
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
		return WorkbenchPlugin.createExtension(element, IWorkbenchConstants.TAG_CLASS);
    }
}
