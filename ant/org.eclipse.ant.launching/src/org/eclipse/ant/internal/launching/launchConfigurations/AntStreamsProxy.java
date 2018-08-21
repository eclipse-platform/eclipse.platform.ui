/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.launching.launchConfigurations;

import org.eclipse.ant.internal.launching.AntLaunching;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * 
 */
public class AntStreamsProxy implements IStreamsProxy {

	private AntStreamMonitor fErrorMonitor = new AntStreamMonitor();
	private AntStreamMonitor fOutputMonitor = new AntStreamMonitor();

	public static final String ANT_DEBUG_STREAM = AntLaunching.PLUGIN_ID + ".ANT_DEBUG_STREAM"; //$NON-NLS-1$
	public static final String ANT_VERBOSE_STREAM = AntLaunching.PLUGIN_ID + ".ANT_VERBOSE_STREAM"; //$NON-NLS-1$
	public static final String ANT_WARNING_STREAM = AntLaunching.PLUGIN_ID + ".ANT_WARNING_STREAM"; //$NON-NLS-1$

	private AntStreamMonitor fDebugMonitor = new AntStreamMonitor();
	private AntStreamMonitor fVerboseMonitor = new AntStreamMonitor();
	private AntStreamMonitor fWarningMonitor = new AntStreamMonitor();

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getErrorStreamMonitor()
	 */
	@Override
	public IStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getOutputStreamMonitor()
	 */
	@Override
	public IStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#write(java.lang.String)
	 */
	@Override
	public void write(String input) {
		// do nothing
	}

	public IStreamMonitor getWarningStreamMonitor() {
		return fWarningMonitor;
	}

	public IStreamMonitor getDebugStreamMonitor() {
		return fDebugMonitor;
	}

	public IStreamMonitor getVerboseStreamMonitor() {
		return fVerboseMonitor;
	}
}
