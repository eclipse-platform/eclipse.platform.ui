package org.eclipse.team.internal.ccvs.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * A wizard for creating a patch file.
 */
public class GenerateDiffFileWizard extends Wizard {
	private WizardNewFileCreationPage mainPage;
	private IStructuredSelection selection;
	private IResource[] resources;

	/**
	 * Page to select a patch file. Overriding validatePage was necessary to allow
	 * entering a file name that already exists.
	 */
	private class PatchFileSelectionPage extends WizardNewFileCreationPage {
		PatchFileSelectionPage(String pageName, IStructuredSelection selection) {
			super(pageName, selection);
		}
		protected boolean validatePage() {
			boolean valid = true;
			String filename = getFileName();
			IPath container = getContainerFullPath();

			if (container == null || filename == null || filename.length() == 0) {
				valid = false;
			}

			// Avoid draw flicker by clearing error message
			// if all is valid.
			if (valid) {
				setMessage(null);
				setErrorMessage(null);
			}
			return valid;
		}
	}

	public GenerateDiffFileWizard(IStructuredSelection selection, IResource[] resources) {
		super();
		this.selection = selection;
		this.resources = resources;
		setWindowTitle(Policy.bind("GenerateCVSDiff.title"));
		initializeDefaultPageImageDescriptor();
	}

	public void addPages() {
		String pageTitle = Policy.bind("GenerateCVSDiff.pageTitle");
		String pageDescription = Policy.bind("GenerateCVSDiff.pageDescription");
		mainPage = new PatchFileSelectionPage(pageTitle, selection);
		mainPage.setDescription(pageDescription);
		mainPage.setTitle(pageTitle);
		addPage(mainPage);
	}
	/**
	 * Initializes this creation wizard using the passed workbench and
	 * object selection.
	 *
	 * @param workbench the current workbench
	 * @param selection the current object selection
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	/**
	 * Declares the wizard banner iamge descriptor
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath;
		if (Display.getCurrent().getIconDepth() > 4) {
			iconPath = "icons/full/"; //$NON-NLS-1$
		} else {
			iconPath = "icons/basic/"; //$NON-NLS-1$
		}
		try {
			URL installURL = CVSUIPlugin.getPlugin().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + "wizban/newconnect_wizban.gif");
			//$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		} catch (MalformedURLException e) {
			// Should not happen.  Ignore.
		}
	}
	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean needsProgressMonitor() {
		return true;
	}
	/**
	 * Completes processing of the wizard. If this method returns <code>
	 * true</code>, the wizard will close; otherwise, it will stay active.
	 */
	public boolean performFinish() {
		String fileName = mainPage.getFileName();
		IPath container = mainPage.getContainerFullPath();

		IContainer parent;
		if (container.segmentCount() == 1) {
			parent = ResourcesPlugin.getWorkspace().getRoot().getProject(container.toString());
		} else {
			parent = ResourcesPlugin.getWorkspace().getRoot().getFolder(container);
		}
		IFile file = parent.getFile(new Path(fileName));

		try {
			if (file.exists()) {
				// prompt then delete
				String title = Policy.bind("GenerateCVSDiff.overwriteTitle");
				String msg = Policy.bind("GenerateCVSDiff.overwriteMsg");
				final MessageDialog dialog = new MessageDialog(getShell(), title, null, msg, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 0);

				dialog.open();

				if (dialog.getReturnCode() != 0) {
					// cancel
					return false;
				}
			}

			getContainer().run(false, true, new GenerateDiffFileOperation(resources, file, getShell()));
			return true;
		} catch (InterruptedException e1) {
			return true;
		} catch (InvocationTargetException e2) {
			if (e2.getTargetException() instanceof CoreException) {
				CoreException e = (CoreException) e2.getTargetException();
				ErrorDialog.openError(getShell(), Policy.bind("GenerateCVSDiff.error"), null, e.getStatus());
				return false;
			} else {
				Throwable target = e2.getTargetException();
				if (target instanceof RuntimeException) {
					throw (RuntimeException) target;
				}
				if (target instanceof Error) {
					throw (Error) target;
				}
			}
			return true;
		}
	}
}