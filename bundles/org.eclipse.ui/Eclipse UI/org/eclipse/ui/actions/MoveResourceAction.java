package org.eclipse.ui.actions;


/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
	public static final String ID = PlatformUI.PLUGIN_ID + ".MoveResourceAction";
	
	/**
	* Keep a list of destinations so that any required update can be done after the
	* move.
	*/
	protected List destinations;

	private static final String CHECK_MOVE_TITLE = "Check Move";
	private static final String CHECK_MOVE_MESSAGE = " is read only. Do you still wish to move it?";
	private static final String MOVING_MESSAGE = "Moving";
	
	
/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public MoveResourceAction(Shell shell) {
	super(shell, "Mo&ve");
	setToolTipText("Move the resource");
	setId(MoveResourceAction.ID);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.MOVE_RESOURCE_ACTION});
}
/**
 * The <code>MoveResourceAction</code> implementation of this 
 * <code>CopyResourceAction</code> method returns <code>false</code>.
 */
boolean canPerformAutoRename() {
	return false;
}
/**
 * Returns the destination resources for the resources that have been moved so far.
 *
 * @return list of destination <code>IResource</code>s
 */
protected List getDestinations() {
	return this.destinations;
}
/** (non-Javadoc)
 * Method declared on CopyResourceAction.
 */
String getProblemsMessage() {
	return "Problems occurred moving the selected resources.";
}
/** (non-Javadoc)
 * Method declared on CopyResourceAction.
 */
String getProblemsTitle() {
	return "Move Problems";
}
/**
 * Return an array of resources from the provided list.
 */
protected IResource[] getResources(List resourceList) {

	ReadOnlyStateChecker checker =
		new ReadOnlyStateChecker(getShell(), CHECK_MOVE_TITLE, CHECK_MOVE_MESSAGE);

	return checker.checkReadOnlyResources(super.getResources(resourceList));
}
/**
 * The <code>MoveResourceAction</code> implementation of this 
 * <code>CopyResourceAction</code> method does a move instead of a copy.
 */
boolean performCopy(
	final IResource[] resources,
	final IPath destination,
	IProgressMonitor monitor) {

	try {
		monitor.subTask(MOVING_MESSAGE);
		ContainerGenerator generator = new ContainerGenerator(destination);
		generator.generateContainer(new SubProgressMonitor(monitor, 500));
		
		//We have 75 units to work with
		resources[0].getWorkspace().move(
			resources,
			destination,
			false,
			new SubProgressMonitor(monitor, 75));
		for (int i = 0; i < resources.length; i++) {
			getDestinations().add(destination.append(resources[i].getName()));
		}
	} catch (CoreException e) {
		recordError(e); // log error
		return false;
	}

	return true;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	//Initialize the destinations
	this.destinations = new ArrayList();
	super.run();
}
}
