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
package org.eclipse.debug.internal.ui.views.console;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.console.IConsole;
import org.eclipse.debug.internal.ui.console.IConsolePage;
import org.eclipse.debug.internal.ui.console.IConsoleView;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A console for a system process
 * <p>
 * Clients may instantiate this class. This class is not intended for
 * sub-classing.
 * </p>
 * @since 3.0
 */
public class ProcessConsole implements IConsole {
	
	private IProcess fProcess = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#createPage(org.eclipse.debug.internal.ui.console.IConsoleView)
	 */
	public IConsolePage createPage(IConsoleView view) {
		return new ProcessConsolePage(view, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		ILaunchConfiguration configuration = getProcess().getLaunch().getLaunchConfiguration();
		if (configuration != null) {
			ILaunchConfigurationType type;
			try {
				type = configuration.getType();
				return DebugPluginImages.getImageDescriptor(type.getIdentifier());
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#getName()
	 */
	public String getName() {	
		ILaunchConfiguration configuration = getProcess().getLaunch().getLaunchConfiguration(); 
		if (configuration != null) {
			if (getProcess().isTerminated()) {
				return MessageFormat.format(ConsoleMessages.getString("ProcessConsole.0"), new String[]{configuration.getName()}); //$NON-NLS-1$
			} else {
				return configuration.getName();
			}
		}
		if (getProcess().isTerminated()) {
			return MessageFormat.format(ConsoleMessages.getString("ProcessConsole.1"), new String[]{getProcess().getLabel()}); //$NON-NLS-1$
		} else {
			return getProcess().getLabel();
		}
	}

	/**
	 * Proxy to a console document
	 */
	public ProcessConsole(IProcess process) {
		fProcess = process;
	}
			
	/**
	 * Returns the process associated with this console.
	 * 
	 * @return the process associated with this console
	 */
	public IProcess getProcess() {
		return fProcess;
	}
	
}
