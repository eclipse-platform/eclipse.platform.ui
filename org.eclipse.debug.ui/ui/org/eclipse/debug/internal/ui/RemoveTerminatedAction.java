package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;
 
/**
 * Removes all terminated/detached launches from the
 * active debug view.
 */
public class RemoveTerminatedAction extends Action implements IUpdate {

	
	public RemoveTerminatedAction() {
		super(DebugUIMessages.getString("RemoveTerminatedAction.Remove_&All_Terminated_1")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RemoveTerminatedAction.Remove_All_Terminated_Launches_2")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_TERMINATED));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_TERMINATED));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_TERMINATED));
		setEnabled(false);
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ACTION });
	}

	/**
	 * Removes all of the terminated launches relevant to the
	 * active debug view.
	 * 
	 * @see IAction
	 */
	public void run() {
		Object[] elements = getElements();
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof ILaunch) {
				ILaunch launch = (ILaunch)elements[i];
				if (launch.isTerminated()) {
					manager.deregisterLaunch(launch);
				}
			}
		}
	}

	/** 
	 * Updates the enabled state of this action to enabled if at
	 * least one launch is terminated and relative to the current perspective.
	 */
	public void update() {
		Object[] elements = getElements();
		if (elements != null) {
			for (int i= 0; i < elements.length; i++) {
				if (elements[i] instanceof ILaunch) {
					ILaunch launch= (ILaunch)elements[i];
					if (launch.isTerminated()) {
						setEnabled(true);
						return;
					}
				}
			}
		}
		setEnabled(false);
	}

	/**
	 * Returns the top level elements in the active debug
	 * view, or <code>null</code> if none.
	 * 
	 * @return array of object
	 */
	protected Object[] getElements() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart part = page.getActivePart();
				if (part != null) {
					IDebugViewAdapter view = (IDebugViewAdapter)part.getAdapter(IDebugViewAdapter.class);
					if (view != null) {
						StructuredViewer viewer = view.getViewer();
						if (viewer != null) {
							IStructuredContentProvider cp = (IStructuredContentProvider)viewer.getContentProvider();
							Object input = viewer.getInput();
							return cp.getElements(input);
						}
					}
				}
			}
		}
		return null;
	}
}

