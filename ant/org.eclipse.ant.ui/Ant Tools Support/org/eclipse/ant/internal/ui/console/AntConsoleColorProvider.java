/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.console;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.IAntUIPreferenceConstants;
import org.eclipse.ant.internal.ui.launchConfigurations.AntProcess;
import org.eclipse.ant.internal.ui.launchConfigurations.AntStreamsProxy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;


public class AntConsoleColorProvider extends ConsoleColorProvider implements IPropertyChangeListener {

	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleColorProvider#getColor(java.lang.String)
	 */
	public Color getColor(String streamIdentifer) {
		if (streamIdentifer.equals(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_INFO_COLOR);
		}
		if (streamIdentifer.equals(IDebugUIConstants.ID_STANDARD_ERROR_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_ERROR_COLOR);
		}				
		if (streamIdentifer.equals(AntStreamsProxy.ANT_DEBUG_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_DEBUG_COLOR);
		}
		if (streamIdentifer.equals(AntStreamsProxy.ANT_VERBOSE_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_VERBOSE_COLOR);
		}
		if (streamIdentifer.equals(AntStreamsProxy.ANT_WARNING_STREAM)) {
			return AntUIPlugin.getPreferenceColor(IAntUIPreferenceConstants.CONSOLE_WARNING_COLOR);
		}
		return super.getColor(streamIdentifer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleColorProvider#connect(org.eclipse.debug.core.model.IProcess, org.eclipse.debug.ui.console.IConsole)
	 */
	public void connect(IProcess process, IConsole console) {
		//Both remote and local Ant builds are guaranteed to have
		//an AntStreamsProxy. The remote Ant builds make use of the
		// org.eclipse.debug.core.processFactories extension point
		AntStreamsProxy proxy = (AntStreamsProxy)process.getStreamsProxy();
		if (process instanceof AntProcess) {
			((AntProcess)process).setConsole(console);
		}
		if (proxy != null) {
			console.connect(proxy.getDebugStreamMonitor(), AntStreamsProxy.ANT_DEBUG_STREAM);
			console.connect(proxy.getWarningStreamMonitor(), AntStreamsProxy.ANT_WARNING_STREAM);
			console.connect(proxy.getVerboseStreamMonitor(), AntStreamsProxy.ANT_VERBOSE_STREAM);
		}
		
		AntUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		super.connect(process, console);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleColorProvider#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (affectsAntConsole(event.getProperty())) {
			AntUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow() ;
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							IWorkbenchPart part = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
							if (part != null) {
								Widget textWidget = (Widget)part.getAdapter(Widget.class);
								if (textWidget instanceof StyledText) {
									((StyledText)textWidget).redraw();
								}
							}
						}
					}
				}
			});
		}
	}

	private boolean affectsAntConsole(String property) {
		if (IAntUIPreferenceConstants.CONSOLE_DEBUG_COLOR.equals(property)) {
			return true;
		} else if (IAntUIPreferenceConstants.CONSOLE_ERROR_COLOR.equals(property)) {
			return true;
		} else if (IAntUIPreferenceConstants.CONSOLE_INFO_COLOR.equals(property)) {
			return true;
		} else if (IAntUIPreferenceConstants.CONSOLE_VERBOSE_COLOR.equals(property)) {
			return true;
		} else if (IAntUIPreferenceConstants.CONSOLE_WARNING_COLOR.equals(property)) {
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleColorProvider#disconnect()
	 */
	public void disconnect() {
		AntUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		super.disconnect();
	}
}