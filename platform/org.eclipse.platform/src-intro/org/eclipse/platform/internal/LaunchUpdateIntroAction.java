/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.platform.internal;

import java.util.Properties;
import org.eclipse.core.commands.*;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.osgi.framework.Bundle;

public class LaunchUpdateIntroAction implements IIntroAction {

	private static final String COMMAND_P2 = "org.eclipse.equinox.p2.ui.sdk.update"; //$NON-NLS-1$
	private static final String COMMAND_UPDATE_MANAGER = "org.eclipse.ui.update.findAndInstallUpdates"; //$NON-NLS-1$

	public LaunchUpdateIntroAction() {
		//nothing to do
	}

	public void run(IIntroSite site, Properties params) {
		Runnable r = new Runnable() {
			public void run() {
				if (!executeUpdateCommand(COMMAND_P2))
					executeUpdateCommand(COMMAND_UPDATE_MANAGER);
			}
		};
		Shell currentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		currentShell.getDisplay().asyncExec(r);
	}

	boolean executeUpdateCommand(String command) {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		Command cmd;
		cmd = commandService.getCommand(command);
		ExecutionEvent executionEvent = handlerService.createExecutionEvent(cmd, null);
		try {
			cmd.executeWithChecks(executionEvent);
		} catch (ExecutionException e) {
			String bundleId = "org.eclipse.platform"; //$NON-NLS-1$
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle != null)
				Platform.getLog(bundle).log(new Status(IStatus.ERROR, bundleId, "Exception executing command: " + command, e)); //$NON-NLS-1$
		} catch (NotDefinedException e) {
			return false;
		} catch (NotEnabledException e) {
			return false;
		} catch (NotHandledException e) {
			return false;
		}
		return true;
	}
}
