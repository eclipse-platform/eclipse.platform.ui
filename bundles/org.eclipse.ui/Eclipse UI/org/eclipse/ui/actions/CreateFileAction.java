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
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import java.util.Iterator;

/**
 * Standard action for creating a file resource within the currently
 * selected folder or project.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CreateFileAction extends SelectionListenerAction {
	
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CreateFileAction";
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;
/**
 * Creates a new action for creating a file resource.
 *
 * @param shell the shell for any dialogs
 */
public CreateFileAction(Shell shell) {
	super("New &File");
	Assert.isNotNull(shell);
	this.shell = shell;
	setToolTipText("Create a new file");
	setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
	setId(ID);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.CREATE_FILE_ACTION});
}
/**
 * The <code>CreateFileAction</code> implementation of this
 * <code>IAction</code> method opens a <code>BasicNewFileResourceWizard</code>
 * in a wizard dialog under the shell passed to the constructor.
 */
public void run() {
	BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
	wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
	wizard.setNeedsProgressMonitor(true);
	WizardDialog dialog = new WizardDialog(shell, wizard);
	dialog.create();
	dialog.getShell().setText("New");
	dialog.open();
}
/**
 * The <code>CreateFileAction</code> implementation of this
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
