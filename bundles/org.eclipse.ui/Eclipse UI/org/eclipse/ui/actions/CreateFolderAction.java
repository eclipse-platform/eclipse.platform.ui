package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import java.util.Iterator;

/**
 * Standard action for creating a folder resource within the currently
 * selected folder or project.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CreateFolderAction extends SelectionListenerAction {
	
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CreateFolderAction";
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;
/**
 * Creates a new action for creating a folder resource.
 *
 * @param shell the shell for any dialogs
 */
public CreateFolderAction(Shell shell) {
	super("New F&older");
	Assert.isNotNull(shell);
	this.shell = shell;
	setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
	setToolTipText("Create a new folder");
	setId(ID);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.CREATE_FOLDER_ACTION});
}
/**
 * The <code>CreateFolderAction</code> implementation of this
 * <code>IAction</code> method opens a <code>BasicNewFolderResourceWizard</code>
 * in a wizard dialog under the shell passed to the constructor.
 */
public void run() {
	BasicNewFolderResourceWizard wizard = new BasicNewFolderResourceWizard();
	wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
	wizard.setNeedsProgressMonitor(true);
	WizardDialog dialog = new WizardDialog(shell, wizard);
	dialog.create();
	dialog.getShell().setText("New");
	dialog.open();

}
/**
 * The <code>CreateFolderAction</code> implementation of this
 * <code>SelectionListenerAction</code> method enables the action only
 * if the selection contains folders and open projects.
 */
protected boolean updateSelection(IStructuredSelection s) {
	if (!super.updateSelection(s)) {
		return false;
	}
	Iterator resources = getSelectedResources().iterator();
	while (resources.hasNext()) {
		IResource resource = (IResource)resources.next();
		if (!resourceIsType(resource, IResource.PROJECT | IResource.FOLDER) || !resource.isAccessible()) {
			return false;
		}
	}
	return true;
}
}
