package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleContentProvider;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;


public class AntConsoleContentProvider extends ConsoleContentProvider {
	
	/**
	 * Identifier of ant console content provider
	 */
	public static final String ID_ANT_CONSOLE_CONTNET_PROVIDER = "org.eclipse.ant.consoleContentProvider"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleContentProvider#getColor(java.lang.String)
	 */
	public Color getColor(String streamIdentifer) {
		if (streamIdentifer.equals(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM)) {
			return ExternalToolsPlugin.getPreferenceColor(IPreferenceConstants.CONSOLE_INFO_RGB);
		}
		if (streamIdentifer.equals(IDebugUIConstants.ID_STANDARD_ERROR_STREAM)) {
			return ExternalToolsPlugin.getPreferenceColor(IPreferenceConstants.CONSOLE_ERROR_RGB);
		}				
		if (streamIdentifer.equals(AntStreamsProxy.ANT_DEBUG_STREAM)) {
			return ExternalToolsPlugin.getPreferenceColor(IPreferenceConstants.CONSOLE_DEBUG_RGB);
		}
		if (streamIdentifer.equals(AntStreamsProxy.ANT_VERBOSE_STREAM)) {
			return ExternalToolsPlugin.getPreferenceColor(IPreferenceConstants.CONSOLE_VERBOSE_RGB);
		}
		if (streamIdentifer.equals(AntStreamsProxy.ANT_WARNING_STREAM)) {
			return ExternalToolsPlugin.getPreferenceColor(IPreferenceConstants.CONSOLE_WARNING_RGB);
		}
		return super.getColor(streamIdentifer);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleContentProvider#connect(org.eclipse.debug.core.model.IProcess, org.eclipse.debug.internal.ui.views.console.IConsole)
	 */
	public void connect(IProcess process, IConsole console) {
		AntStreamsProxy proxy = (AntStreamsProxy)process.getStreamsProxy();
		((AntProcess)process).setConsole(console);
		console.connect(proxy.getDebugStreamMonitor(), AntStreamsProxy.ANT_DEBUG_STREAM);
		console.connect(proxy.getWarningStreamMonitor(), AntStreamsProxy.ANT_WARNING_STREAM);
		console.connect(proxy.getVerboseStreamMonitor(), AntStreamsProxy.ANT_VERBOSE_STREAM);		
		super.connect(process, console);
	}

}

