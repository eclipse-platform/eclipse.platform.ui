package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Standard action for moving the currently selected resources elsewhere
 * in the workspace. All resources being moved as a group must be siblings.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class MoveResourceAction extends CopyResourceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".MoveResourceAction"; //$NON-NLS-1$

	/**
 	 * Keep a list of destinations so that any required update can be done after the
 	 * move.
	 */
	protected List destinations;

	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 */
	public MoveResourceAction(Shell shell) {
		super(shell, WorkbenchMessages.getString("MoveResourceAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("MoveResourceAction.toolTip")); //$NON-NLS-1$
		setId(MoveResourceAction.ID);
		WorkbenchHelp.setHelp(this, IHelpContextIds.MOVE_RESOURCE_ACTION);
	}
	/* (non-Javadoc)
	 * Overrides method in CopyResourceAction
	 */
	protected CopyFilesAndFoldersOperation createOperation() {
		return new MoveFilesAndFoldersOperation(getShell());
	}
	/**
	 * Returns the destination resources for the resources that have been moved so far.
	 *
	 * @return list of destination <code>IResource</code>s
	 */
	protected List getDestinations() {
		return destinations;
	}
	/* (non-Javadoc)
	 * Overrides method in CopyResourceAction
	 */
	protected IResource[] getResources(List resourceList) {
		ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
			getShell(), 
			WorkbenchMessages.getString("MoveResourceAction.title"),			//$NON-NLS-1$
			WorkbenchMessages.getString("MoveResourceAction.checkMoveMessage"));//$NON-NLS-1$	
		return checker.checkReadOnlyResources(super.getResources(resourceList));
	}
	/* (non-Javadoc)
	 * Overrides method in CopyResourceAction
	 */
	protected void runOperation(IResource[] resources, IContainer destination) {
		//Initialize the destinations
		destinations = new ArrayList();
		IResource[] copiedResources = operation.copyResources(resources, destination);

		for (int i = 0; i < copiedResources.length; i++) {
			destinations.add(destination.getFullPath().append(copiedResources[i].getName()));
		}
	}
}