package org.eclipse.ui.wizards.newresource;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import java.net.*;
import java.util.*;

/**
 * Abstract base implementation of the standard workbench wizards
 * that create new resources in the workspace.
 * <p>
 * This class is not intended to be subclassed outside this package.
 * </p>
 */
public abstract class BasicNewResourceWizard extends Wizard implements INewWizard {

	/**
	 * The workbench.
	 */
	private IWorkbench workbench;

	/**
	 * The current selection.
	 */
	protected IStructuredSelection selection;
/**
 * Creates an empty wizard for creating a new resource in the workspace.
 */
protected BasicNewResourceWizard() {
	super();
}
/**
 * Returns the selection which was passed to <code>init</code>.
 *
 * @return the selection
 */
public IStructuredSelection getSelection() {
	return selection;
}
/**
 * Returns the workbench which was passed to <code>init</code>.
 *
 * @return the workbench
 */
public IWorkbench getWorkbench() {
	return workbench;
}
/**
 * The <code>BasicNewResourceWizard</code> implementation of this 
 * <code>IWorkbenchWizard</code> method records the given workbench and
 * selection, and initializes the default banner image for the pages
 * by calling <code>initializeDefaultPageImageDescriptor</code>. 
 * Subclasses may extend.
 */
public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
	this.workbench = workbench;
	this.selection = currentSelection;
	
	initializeDefaultPageImageDescriptor();
}
/**
 * Initializes the default page image descriptor to an appropriate banner.
 * By calling <code>setDefaultPageImageDescriptor</code>.
 * The default implementation of this method uses a generic new wizard image.
 * <p>
 * Subclasses may reimplement.
 * </p>
 */
protected void initializeDefaultPageImageDescriptor() {
	String iconPath = "icons/full/";//$NON-NLS-1$
	try {
		URL installURL = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + "wizban/new_wiz.gif");//$NON-NLS-1$
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		setDefaultPageImageDescriptor(desc);
	}
	catch (MalformedURLException e) {
		// Should not happen.  Ignore.
	}
}
/**
 * Selects and reveals the newly added resource in all parts
 * of the active workbench window's active page.
 *
 * @see ISetSelectionTarget
 */
protected void selectAndReveal(IResource newResource) {
	selectAndReveal(newResource, getWorkbench().getActiveWorkbenchWindow());
}
/**
 * Attempts to select and reveal the specified resource in all
 * parts within the supplied workbench window's active page.
 * <p>
 * Checks all parts in the active page to see if they implement <code>ISetSelectionTarget</code>,
 * either directly or as an adapter. If so, tells the part to select and reveal the
 * specified resource.
 * </p>
 *
 * @param resource the resource to be selected and revealed
 * @param window the workbench window to select and reveal the resource
 * 
 * @see ISetSelectionTarget
 */
public static void selectAndReveal(IResource resource, IWorkbenchWindow window) {
	// validate the input
	if (window == null || resource == null)
		return;
	IWorkbenchPage page = window.getActivePage();
	if (page == null)
		return;

	// get all the view and editor parts
	List parts = new ArrayList();
	parts.addAll(Arrays.asList(page.getViews()));
	parts.addAll(Arrays.asList(page.getEditors()));
	
	final ISelection selection = new StructuredSelection(resource);
	Iterator enum = parts.iterator();
	while (enum.hasNext()) {
		IWorkbenchPart part = (IWorkbenchPart) enum.next();
		
		// get the part's ISetSelectionTarget implementation
		ISetSelectionTarget target = null;
		if (part instanceof ISetSelectionTarget)
			target = (ISetSelectionTarget) part;
		else
			target = (ISetSelectionTarget) part.getAdapter(ISetSelectionTarget.class);
			
		if (target != null) {
			// select and reveal resource
			final ISetSelectionTarget finalTarget = target;
			window.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					finalTarget.selectReveal(selection);
				}
			});
		}
	}
}
}
