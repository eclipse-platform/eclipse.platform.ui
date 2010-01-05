/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Terminates all launches.
 * 
 * @since 3.3
 */
public class TerminateAllAction extends DebugCommandAction implements ILaunchesListener2 {
		
	protected ISelection getContext() {
		return new StructuredSelection(getLaunchManager().getLaunches());
	}
	
	public void dispose() {
		getLaunchManager().removeLaunchListener(this);
		super.dispose();
	}

	public void init(IWorkbenchPart part) {
		super.init(part);
		ILaunchManager launchManager = getLaunchManager();
		launchManager.addLaunchListener(this);
		// heuristic... rather than updating all the time, just assume there's
		// something that's not terminated.
		setEnabled(launchManager.getLaunches().length > 0);
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public String getHelpContextId() {
		return "org.eclipse.debug.ui.terminate_all_action_context"; //$NON-NLS-1$
	}

	public String getId() {
		return "org.eclipse.debug.ui.debugview.popupMenu.terminateAll"; //$NON-NLS-1$
	}

	public String getText() {
		return ActionMessages.TerminateAllAction_2;
	}

	public String getToolTipText() {
		return ActionMessages.TerminateAllAction_3;
	}

	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugPluginImages
				.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_ALL);
	}

	public ImageDescriptor getHoverImageDescriptor() {
		return DebugPluginImages
				.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_ALL);
	}

	public ImageDescriptor getImageDescriptor() {
		return DebugPluginImages
				.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_ALL);
	}

	protected Class getCommandType() {
		return ITerminateHandler.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
		setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
		setEnabled(getLaunchManager().getLaunches().length > 0);
	}
}
