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
package org.eclipse.ant.ui.internal.launchConfigurations;

import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.IAntUIPreferenceConstants;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.RuntimeProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.swt.graphics.Color;


public class AntConsoleColorProvider extends ConsoleColorProvider {

	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleColorProvider#getColor(java.lang.String)
	 */
	public Color getColor(String streamIdentifer) {
		if (streamIdentifer.equals(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_INFO_RGB);
		}
		if (streamIdentifer.equals(IDebugUIConstants.ID_STANDARD_ERROR_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_ERROR_RGB);
		}				
		if (streamIdentifer.equals(AntStreamsProxy.ANT_DEBUG_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_DEBUG_RGB);
		}
		if (streamIdentifer.equals(AntStreamsProxy.ANT_VERBOSE_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_VERBOSE_RGB);
		}
		if (streamIdentifer.equals(AntStreamsProxy.ANT_WARNING_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_WARNING_RGB);
		}
		return super.getColor(streamIdentifer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleColorProvider#connect(org.eclipse.debug.core.model.IProcess, org.eclipse.debug.ui.console.IConsole)
	 */
	public void connect(IProcess process, IConsole console) {
		if (process instanceof AntProcess) {
			AntStreamsProxy proxy = (AntStreamsProxy)process.getStreamsProxy();
			((AntProcess)process).setConsole(console);
			console.connect(proxy.getDebugStreamMonitor(), AntStreamsProxy.ANT_DEBUG_STREAM);
			console.connect(proxy.getWarningStreamMonitor(), AntStreamsProxy.ANT_WARNING_STREAM);
			console.connect(proxy.getVerboseStreamMonitor(), AntStreamsProxy.ANT_VERBOSE_STREAM);
		} else if (process instanceof RuntimeProcess) {
			((RuntimeProcess)process).setStreamsProxy(new AntStreamsProxy());
			AntStreamsProxy proxy = (AntStreamsProxy)process.getStreamsProxy();
			console.connect(proxy.getDebugStreamMonitor(), AntStreamsProxy.ANT_DEBUG_STREAM);
			console.connect(proxy.getWarningStreamMonitor(), AntStreamsProxy.ANT_WARNING_STREAM);
			console.connect(proxy.getVerboseStreamMonitor(), AntStreamsProxy.ANT_VERBOSE_STREAM);
		}
		super.connect(process, console);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleColorProvider#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

}

