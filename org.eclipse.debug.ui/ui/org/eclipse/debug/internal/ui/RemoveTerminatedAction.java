package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;
 
/**
 * Removes all terminated/detached launches from the
 * active debug view.
 */
public class RemoveTerminatedAction extends Action implements IUpdate {

	/**
	 * The part this action is installed in
	 */
	private IViewPart fPart;
	
	public RemoveTerminatedAction(IViewPart part) {
		super(DebugUIMessages.getString("RemoveTerminatedAction.Remove_&All_Terminated_1")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RemoveTerminatedAction.Remove_All_Terminated_Launches_2")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_TERMINATED));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_TERMINATED));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_TERMINATED));
		setEnabled(false);
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ACTION });
		setPart(part);
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
		IDebugViewAdapter view = (IDebugViewAdapter)getPart().getAdapter(IDebugViewAdapter.class);
		if (view != null) {
			Viewer viewer = view.getViewer();
			if (viewer instanceof StructuredViewer) {
				IStructuredContentProvider cp = (IStructuredContentProvider)((StructuredViewer)viewer).getContentProvider();
				Object input = viewer.getInput();
				return cp.getElements(input);
			}
		}
		return null;
	}
	
	/**
	 * Sets the part this action is installed in
	 * 
	 * @param part view part
	 */
	private void setPart(IViewPart part) {
		fPart = part;
	}
	
	/**
	 * Returns the part this action is installed in
	 * 
	 * @return view part
	 */
	protected IViewPart getPart() {
		return fPart;
	}
}

