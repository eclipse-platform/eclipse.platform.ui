/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;


import java.io.IOException;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
;

/**
 * 
 */
public class AntStreamsProxy implements IStreamsProxy {
	
	private AntStreamMonitor fErrorMonitor = new AntStreamMonitor();
	private AntStreamMonitor fOutputMonitor = new AntStreamMonitor();
	
	public static final String ANT_DEBUG_STREAM = IExternalToolConstants.PLUGIN_ID + ".ANT_DEBUG_STREAM"; //$NON-NLS-1$
	public static final String ANT_VERBOSE_STREAM = IExternalToolConstants.PLUGIN_ID + ".ANT_VERBOSE_STREAM"; //$NON-NLS-1$
	public static final String ANT_WARNING_STREAM = IExternalToolConstants.PLUGIN_ID + ".ANT_WARNING_STREAM"; //$NON-NLS-1$
	
	private AntStreamMonitor fDebugMonitor = new AntStreamMonitor();
	private AntStreamMonitor fVerboseMonitor = new AntStreamMonitor();
	private AntStreamMonitor fWarningMonitor = new AntStreamMonitor();

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getErrorStreamMonitor()
	 */
	public IStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#getOutputStreamMonitor()
	 */
	public IStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamsProxy#write(java.lang.String)
	 */
	public void write(String input) throws IOException {
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
