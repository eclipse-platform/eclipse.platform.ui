/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * A small class to manage the about dialog information for a single bundle.
 * @since 3.0
 */
public class AboutBundleData extends AboutData {

    private int state;

    public AboutBundleData(Bundle bundle) {
        super(getResourceString(bundle, Constants.BUNDLE_VENDOR),
                getResourceString(bundle, Constants.BUNDLE_NAME),
                getResourceString(bundle, Constants.BUNDLE_VERSION), bundle
                        .getSymbolicName());

        state = bundle.getState();
    }

    public int getState() {
        return state;
    }

    /**
     * Return a string representation of the arugment state. Does not return
     * null.
     */
    public String getStateName() {
        switch (state) {
        case Bundle.INSTALLED:
            return WorkbenchMessages
                    .getString("AboutPluginsDialog.state.installed"); //$NON-NLS-1$
        case Bundle.RESOLVED:
            return WorkbenchMessages
                    .getString("AboutPluginsDialog.state.resolved"); //$NON-NLS-1$
        case Bundle.STARTING:
            return WorkbenchMessages
                    .getString("AboutPluginsDialog.state.starting"); //$NON-NLS-1$
        case Bundle.STOPPING:
            return WorkbenchMessages
                    .getString("AboutPluginsDialog.state.stopping"); //$NON-NLS-1$
        case Bundle.UNINSTALLED:
            return WorkbenchMessages
                    .getString("AboutPluginsDialog.state.uninstalled"); //$NON-NLS-1$
        case Bundle.ACTIVE:
            return WorkbenchMessages
                    .getString("AboutPluginsDialog.state.active"); //$NON-NLS-1$
        default:
            return WorkbenchMessages
                    .getString("AboutPluginsDialog.state.unknown"); //$NON-NLS-1$
        }
    }

    /**
     * A function to translate the resource tags that may be embedded in a
     * string associated with some bundle.
     * 
     * @param headerName
     *            the used to retrieve the correct string
     * @return the string or null if the string cannot be found
     */
    private static String getResourceString(Bundle bundle, String headerName) {
        String value = (String) bundle.getHeaders().get(headerName);
        return value == null ? null : Platform.getResourceString(bundle, value);
    }
}