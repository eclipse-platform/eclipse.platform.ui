/*******************************************************************************
 * Copyright (c) Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;

public class Activator implements BundleActivator, DebugOptionsListener {
	/**
	 * The identifier of the descriptor of this plugin in plugin.xml.
	 */
	public static final String PLUGIN_ID = "org.eclipse.text"; //$NON-NLS-1$

	private static final String DEBUG_FLAG = PLUGIN_ID + "/debug"; //$NON-NLS-1$

	private static DebugTrace debugTrace;

	public static boolean DEBUG = false;

	private ServiceRegistration<DebugOptionsListener> debugRegistration;

	@Override
	public void start(BundleContext context) throws Exception {
		Hashtable<String, String> props = new Hashtable<>(2);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
		debugRegistration = context.registerService(DebugOptionsListener.class, this, props);
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		debugTrace = options.newDebugTrace(PLUGIN_ID, Activator.class);
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (debugRegistration != null) {
			debugRegistration.unregister();
			debugRegistration = null;
		}
	}

	public static void trace(String message) {
		if (DEBUG) {
			debugTrace.trace(null, message, null);
		}
	}
}
