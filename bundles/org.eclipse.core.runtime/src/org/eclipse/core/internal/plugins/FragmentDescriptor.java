/********************************************************************** * Copyright (c) 2000,2002 IBM Corporation and others. * All rights reserved.   This program and the accompanying materials * are made available under the terms of the Common Public License v0.5 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/cpl-v05.html *  * Contributors:  * IBM - Initial API and implementation **********************************************************************/package org.eclipse.core.internal.plugins;import java.net.MalformedURLException;import java.net.URL;import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.runtime.PlatformURLFragmentConnection;

public class FragmentDescriptor extends PluginFragmentModel {

	// constants
	static final String FRAGMENT_URL = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PlatformURLFragmentConnection.FRAGMENT + "/"; //$NON-NLS-1$ //$NON-NLS-2$

public String toString() {
	return getId() + PluginDescriptor.VERSION_SEPARATOR + getVersion();
}
public URL getInstallURL() {	try {		return new URL(FRAGMENT_URL + toString() + "/"); //$NON-NLS-1$	} catch (MalformedURLException e) {		throw new IllegalStateException(); // unchecked	}}}
