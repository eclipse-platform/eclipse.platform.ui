package org.eclipse.ui.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The ReadOnlyStateChecker is a helper class that takes a set of resource
 * some of which may be read only and queries the user as to whether or
 * not they wish to continue the operation on it.
 */
class ReadOnlyStateChecker {
	private Shell shell;
	private String titleMessage;
	private String mainMessage;
	private boolean yesToAllNotSelected = true;

	private String READ_ONLY_EXCEPTION_MESSAGE = "Read Only Checking Problems";
	
/**
 * Create a new checker that parents the dialog off of parent using the supplied
 * title and message.
 * @param parent the shell used for dialogs
 * @param title the title for dialogs
 * @param message the message for a dialog - this will be prefaced with the name of the resource.
 */
ReadOnlyStateChecker(Shell parent, String title, String message) {
	this.shell = parent;
	this.titleMessage = title;
	this.mainMessage = message;
}
/**
 * Check an individual resource to see if it passed the read only query. If it is a file
 * just add it, otherwise it is a container and the children need to be checked too.
 * Return true if all items are selected and false if any are skipped.
 */
private boolean checkAcceptedResource(
	IResource resourceToCheck,
	List selectedChildren)
	throws CoreException {

	if (resourceToCheck.getType() == IResource.FILE)
		selectedChildren.add(resourceToCheck);
	else {
		//Now check below
		int childCheck =
			checkReadOnlyResources(
				((IContainer) resourceToCheck).members(),
				selectedChildren);
		//Add in the resource only if nothing was left out
		if (childCheck == IDialogConstants.YES_TO_ALL_ID)
			selectedChildren.add(resourceToCheck);
		else //Something was left out - return false
			return false;
	}
	return true;

}
/**
 * Check the supplied resources to see if they are read only. If so then prompt
 * the user to see if they can be deleted.Return those that were accepted.
 * @return the resulting selected resources
 */
/*package*/
IResource[] checkReadOnlyResources(IResource[] itemsToCheck) {

	List selections = new ArrayList();
	int result = IDialogConstants.CANCEL_ID;
	try {
		result = checkReadOnlyResources(itemsToCheck, selections);
	} catch (CoreException exception) {
		ErrorDialog.openError(
			this.shell,
			READ_ONLY_EXCEPTION_MESSAGE,
			null,
			exception.getStatus());
	}

	if (result == IDialogConstants.CANCEL_ID)
		return new IResource[0];

	//All were selected so return the original items
	if (result == IDialogConstants.YES_TO_ALL_ID)
		return itemsToCheck;


	IResource[] returnValue = new IResource[selections.size()];
	selections.toArray(returnValue);
	return returnValue;
}
/**
 * Check the children of the container to see if they are read only.
 * @return int
 * one of
 * 	YES_TO_ALL_ID - all elements were selected
 * 	NO_ID - No was hit at some point
 * 	CANCEL_ID - cancel was hit
 * @param itemsToCheck IResource[]
 * @param allSelected the List of currently selected resources to add to.
 */
private int checkReadOnlyResources(IResource[] itemsToCheck, List allSelected)
	throws CoreException {

	//Shortcut. If the user has already selected yes to all then just return it
	if (!this.yesToAllNotSelected)
		return IDialogConstants.YES_TO_ALL_ID;

	boolean noneSkipped = true;
	List selectedChildren = new ArrayList();

	for (int i = 0; i < itemsToCheck.length; i++) {
		IResource resourceToCheck = itemsToCheck[i];
		if (this.yesToAllNotSelected && resourceToCheck.isReadOnly()) {
			int action = queryYesToAllNoCancel(resourceToCheck);
			if (action == IDialogConstants.YES_ID) {
				boolean childResult = checkAcceptedResource(resourceToCheck, selectedChildren);
				if (!childResult)
					noneSkipped = false;
			}
			if (action == IDialogConstants.NO_ID)
				noneSkipped = false;
			if (action == IDialogConstants.CANCEL_ID)
				return IDialogConstants.CANCEL_ID;
			if (action == IDialogConstants.YES_TO_ALL_ID) {
				this.yesToAllNotSelected = false;
				selectedChildren.add(resourceToCheck);
			}
		} else {
			boolean childResult = checkAcceptedResource(resourceToCheck, selectedChildren);
			if (!childResult)
				noneSkipped = false;
		}

	}

	if (noneSkipped)
		return IDialogConstants.YES_TO_ALL_ID;
	else {
		allSelected.addAll(selectedChildren);
		return IDialogConstants.NO_ID;
	}

}
/**
 * Open a message dialog with Yes No, Yes To All and Cancel buttons. Return the
 * code that indicates the selection.
 * @return int 
 *	one of
 *		YES_TO_ALL_ID
 *		YES_ID
 *		NO_ID
 *		CANCEL_ID
 * 		
 * @param resource - the resource being queried.
 */
private int queryYesToAllNoCancel(IResource resource) {

	MessageDialog dialog =
		new MessageDialog(
			this.shell,
			this.titleMessage,
			null,
			resource.getName() + this.mainMessage,
			MessageDialog.QUESTION,
			new String[] {
				IDialogConstants.YES_LABEL,
				IDialogConstants.YES_TO_ALL_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL },
			0);

	int result = dialog.open();
	if (result == 0)
		return IDialogConstants.YES_ID;
	if (result == 1)
		return IDialogConstants.YES_TO_ALL_ID;
	if (result == 2)
		return IDialogConstants.NO_ID;
	return IDialogConstants.CANCEL_ID;
}
}
