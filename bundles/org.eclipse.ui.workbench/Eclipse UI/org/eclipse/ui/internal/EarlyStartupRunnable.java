/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;

/**
 * A utility class used to call #earlyStartup on the proper instance for a given
 * configuration element. There are a few steps to the process in order to
 * ensure compatibility with pre-3.0 plugins.
 * 
 * @since 3.0
 */
public class EarlyStartupRunnable extends SafeRunnable {

    private static final String EXTENSION_CLASS = "org.eclipse.core.runtime.IExtension"; //$NON-NLS-1$

//    private static final String PLUGIN_DESC_CLASS = "org.eclipse.core.runtime.IPluginDescriptor"; //$NON-NLS-1$

    private static final String GET_PLUGIN_METHOD = "getPlugin"; //$NON-NLS-1$

    private static final String GET_DESC_METHOD = "getDeclaringPluginDescriptor"; //$NON-NLS-1$

    private static final String PI_RUNTIME_COMPATIBILITY = "org.eclipse.core.runtime.compatibility"; //$NON-NLS-1$ 

    private IExtension extension;

    /**
     * @param extension
     *            must not be null
     */
    public EarlyStartupRunnable(IExtension extension) {
        this.extension = extension;
    }

    public void run() throws Exception {
        IConfigurationElement[] configElements = extension
                .getConfigurationElements();

        // look for the startup tag in each element and run the extension
        boolean foundAtLeastOne = false;
        for (int i = 0; i < configElements.length; ++i) {
            IConfigurationElement element = configElements[i];
            if (element != null
                    && element.getName()
                            .equals(IWorkbenchConstants.TAG_STARTUP)) {
                runEarlyStartup(getExecutableExtension(element));
                foundAtLeastOne = true;
            }
        }

        // if no startup tags were found, then try the plugin object
        if (!foundAtLeastOne) {
			runEarlyStartup(getPluginForCompatibility());
		}
    }

    public void handleException(Throwable exception) {
        IStatus status = new Status(IStatus.ERROR, extension.getNamespace(), 0,
                "Unable to execute early startup code for an extension", //$NON-NLS-1$
                exception);
        WorkbenchPlugin.log("Unhandled Exception", status); //$NON-NLS-1$
    }

    private void runEarlyStartup(Object executableExtension) {
        if (executableExtension != null
                && executableExtension instanceof IStartup) {
			((IStartup) executableExtension).earlyStartup();
		} else {
            IStatus status = new Status(IStatus.ERROR,
                    extension.getNamespace(), 0,
                    "startup class must implement org.eclipse.ui.IStartup", //$NON-NLS-1$
                    null);
            WorkbenchPlugin.log("Bad extension specification", status); //$NON-NLS-1$
        }
    }

    /**
     * In 3.0 the class attribute is a mandatory element of the startup element.
     * However, 2.1 plugins should still be able to run if the compatibility
     * bundle is loaded.
     * 
     * @return an executable extension for this startup element or null if an
     *         extension (or plugin) could not be found
     */
    private Object getExecutableExtension(IConfigurationElement element)
            throws CoreException {

        String classname = element.getAttribute(IWorkbenchConstants.TAG_CLASS);

        // if class attribute is absent then try to use the compatibility
        // bundle to return the plugin object
        if (classname == null || classname.length() <= 0) {
			return getPluginForCompatibility();
		}

        // otherwise the 3.0 runtime should be able to do it
        return WorkbenchPlugin.createExtension(element,
                IWorkbenchConstants.TAG_CLASS);
    }

    /**
     * If the compatiblity bundle is loaded, then return the plugin object for
     * the extension on this runnable. Return null if the compatibility bundle
     * is not loaded or the plugin object cannot be created.
     */
    private Object getPluginForCompatibility() {
        // make sure the compatibility bundle is available
        Bundle compatBundle = Platform.getBundle(PI_RUNTIME_COMPATIBILITY);
        if (compatBundle == null) {
			return null;
		}

        // use reflection to try to access the plugin object
        try {
            // IPluginDescriptor pluginDesc =
            // 		extension.getDeclaringPluginDescriptor();
            Class extensionClass = compatBundle.loadClass(EXTENSION_CLASS);
            Method getDescMethod = extensionClass.getDeclaredMethod(
                    GET_DESC_METHOD, new Class[0]);
            Object pluginDesc = getDescMethod.invoke(extension, new Object[0]);
            if (pluginDesc == null) {
				return null;
			}

            // Plugin plugin = pluginDesc.getPlugin();
            Class pluginDescClass = pluginDesc.getClass();
            Method getPluginMethod = pluginDescClass.getDeclaredMethod(
                    GET_PLUGIN_METHOD, new Class[0]);
            return getPluginMethod.invoke(pluginDesc, new Object[0]);
        } catch (ClassNotFoundException e) {
            handleException(e);
        } catch (IllegalAccessException e) {
            handleException(e);
        } catch (InvocationTargetException e) {
            handleException(e);
        } catch (NoSuchMethodException e) {
            handleException(e);
        }

        return null;
    }
}
