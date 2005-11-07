/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.contexts.actions.RemoveAllTerminatedAction;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * ConsoleRemoveAllTerminatedAction
 */
public class ConsoleRemoveAllTerminatedAction extends Action implements IUpdate, IDebugEventSetListener, ILaunchesListener {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			Object source = event.getSource();
			if (event.getKind() == DebugEvent.TERMINATE && (source instanceof IDebugTarget || source instanceof IProcess)) {
				update();
			}
		}
		
	}

	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			if (launch.isTerminated()) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		RemoveAllTerminatedAction.removeTerminatedLaunches(launches);
	}
	
	public ConsoleRemoveAllTerminatedAction() {
		super(ConsoleMessages.ConsoleRemoveAllTerminatedAction_0); 
		        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CONSOLE_REMOVE_ALL_TERMINATED);
		setToolTipText(ConsoleMessages.ConsoleRemoveAllTerminatedAction_1); 
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_ALL));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_ALL));
		DebugPlugin.getDefault().addDebugEventListener(this);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		update();
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
     */
    public void launchesRemoved(ILaunch[] launches) {
       if (isEnabled()) {
           update();
       }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
     */
    public void launchesAdded(ILaunch[] launches) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
     */
    public void launchesChanged(ILaunch[] launches) {
    }	
}
