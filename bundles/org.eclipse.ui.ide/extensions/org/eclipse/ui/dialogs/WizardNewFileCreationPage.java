/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Fedorov <Alexander.Fedorov@borland.com> - Bug 172000
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472784
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.CreateLinkedResourceGroup;
import org.eclipse.ui.internal.ide.misc.ResourceAndContainerGroup;

/**
 * Standard main page for a wizard that creates a file resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Subclasses may override
 * </p>
 * <ul>
 * <li><code>getInitialContents</code></li>
 * <li><code>getNewFileLabel</code></li>
 * </ul>
 * <p>
 * Subclasses may extend
 * </p>
 * <ul>
 * <li><code>handleEvent</code></li>
 * </ul>
 */
public class WizardNewFileCreationPage extends WizardPage implements Listener {
	private static final int SIZING_CONTAINER_GROUP_HEIGHT = 250;

	// the current resource selection
	private IStructuredSelection currentSelection;

	// cache of newly-created file
	private IFile newFile;

	private URI linkTargetPath;

	// widgets
	private ResourceAndContainerGroup resourceGroup;

	private Button advancedButton;

	private CreateLinkedResourceGroup linkedResourceGroup;

	private Composite linkedResourceParent;

	private Composite linkedResourceComposite;

	// initial value stores
	private String initialFileName;

	/**
	 * The file extension to use for this page's file name field when it does not
	 * exist yet.
	 *
	 * @see WizardNewFileCreationPage#setFileExtension(String)
	 * @since 3.3
	 */
	private String initialFileExtension;

	private IPath initialContainerFullPath;

	private boolean initialAllowExistingResources = false;

	/**
	 * Height of the "advanced" linked resource group. Set when the advanced group
	 * is first made visible.
	 */
	private int linkedResourceGroupHeight = -1;

	/**
	 * First time the advanced group is validated.
	 */
	private boolean firstLinkCheck = true;

	/**
	 * Creates a new file creation wizard page. If the initial resource selection
	 * contains exactly one container resource then it will be used as the default
	 * container resource.
	 *
	 * @param pageName  the name of the page
	 * @param selection the current resource selection
	 */
	public WizardNewFileCreationPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		setPageComplete(false);
		this.currentSelection = selection;
	}

	/**
	 * Creates the widget for advanced options.
	 *
	 * @param parent the parent composite
	 */
	protected void createAdvancedControls(Composite parent) {
		Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();

		if (preferences.getBoolean(ResourcesPlugin.PREF_DISABLE_LINKING) == false) {
			linkedResourceParent = new Composite(parent, SWT.NONE);
			linkedResourceParent.setFont(parent.getFont());
			linkedResourceParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			linkedResourceParent.setLayout(layout);

			advancedButton = new Button(linkedResourceParent, SWT.PUSH);
			advancedButton.setFont(linkedResourceParent.getFont());
			advancedButton.setText(IDEWorkbenchMessages.showAdvanced);
			GridData data = setButtonLayoutData(advancedButton);
			data.horizontalAlignment = GridData.BEGINNING;
			advancedButton.setLayoutData(data);
			advancedButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleAdvancedButtonSelect();
				}
			});
		}
		linkedResourceGroup = new CreateLinkedResourceGroup(IResource.FILE, e -> {
			setPageComplete(validatePage());
			firstLinkCheck = false;
		}, new CreateLinkedResourceGroup.IStringValue() {
			@Override
			public void setValue(String string) {
				resourceGroup.setResource(string);
			}

			@Override
			public String getValue() {
				return resourceGroup.getResource();
			}

			@Override
			public IResource getResource() {
				IPath path = resourceGroup.getContainerFullPath();
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource resource = root.findMember(path);
				if (resource != null && resource instanceof IContainer) {
					String resourceName = resourceGroup.getResource();
					if (resourceName.length() > 0) {
						try {
							return ((IContainer) resource).getFile(IPath.fromOSString(resourceName));
						} catch (IllegalArgumentException e) {
							// continue below.
						}
					}
					return resource;
				}
				return resource;
			}
		});
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		// top level group
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		topLevel.setFont(parent.getFont());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(topLevel, IIDEHelpContextIds.NEW_FILE_WIZARD_PAGE);

		// resource and container group
		resourceGroup = new ResourceAndContainerGroup(topLevel, this, getNewFileLabel(),
				IDEWorkbenchMessages.WizardNewFileCreationPage_file, false, SIZING_CONTAINER_GROUP_HEIGHT);
		resourceGroup.setAllowExistingResources(initialAllowExistingResources);
		initialPopulateContainerNameField();
		createAdvancedControls(topLevel);
		if (initialFileName != null) {
			resourceGroup.setResource(initialFileName);
		}
		if (initialFileExtension != null) {
			resourceGroup.setResourceExtension(initialFileExtension);
		}
		validatePage();
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(topLevel);
	}

	/**
	 * Creates a file resource given the file handle and contents.
	 *
	 * @param fileHandle the file handle to create a file resource with
	 * @param contents   the initial contents of the new file resource, or
	 *                   <code>null</code> if none (equivalent to an empty stream)
	 * @param monitor    the progress monitor to show visual progress with
	 * @exception CoreException              if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 *
	 * @deprecated As of 3.3, use or override {@link #createNewFile()} which uses
	 *             the undoable operation support. To supply customized file content
	 *             for a subclass, use {@link #getInitialContents()}.
	 */
	@Deprecated
	protected void createFile(IFile fileHandle, InputStream contents, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		if (contents == null) {
			contents = new ByteArrayInputStream(new byte[0]);
		}

		try {
			// Create a new file resource in the workspace
			if (linkTargetPath != null) {
				fileHandle.createLink(linkTargetPath, IResource.ALLOW_MISSING_LOCAL, subMonitor.split(100));
			} else {
				IPath path = fileHandle.getFullPath();
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				int numSegments = path.segmentCount();
				if (numSegments > 2 && !root.getFolder(path.removeLastSegments(1)).exists()) {
					// If the direct parent of the path doesn't exist, try to
					// create the necessary directories.
					SubMonitor loopMonitor = subMonitor.split(30);
					for (int i = numSegments - 2; i > 0; i--) {
						loopMonitor.setWorkRemaining(i);
						IFolder folder = root.getFolder(path.removeLastSegments(i));
						if (!folder.exists()) {
							folder.create(false, true, loopMonitor.split(1));
						}
					}
				}
				subMonitor.setWorkRemaining(100);
				fileHandle.create(contents, false, subMonitor.split(100));
			}
		} catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED) {
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			} else {
				throw e;
			}
		}
	}

	/**
	 * Creates a file resource handle for the file with the given workspace path.
	 * This method does not create the file resource; this is the responsibility of
	 * <code>createFile</code>.
	 *
	 * @param filePath the path of the file resource to create a handle for
	 * @return the new file resource handle
	 * @see #createFile
	 */
	protected IFile createFileHandle(IPath filePath) {
		return IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getFile(filePath);
	}

	/**
	 * Creates the link target path if a link target has been specified.
	 */
	protected void createLinkTarget() {
		linkTargetPath = linkedResourceGroup.getLinkTargetURI();
	}

	/**
	 * Creates a new file resource in the selected container and with the selected
	 * name. Creates any missing resource containers along the path; does nothing if
	 * the container resources already exist.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish on
	 * the wizard; the enablement of the Finish button implies that all controls on
	 * on this page currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this page caches the new file once it has been successfully
	 * created; subsequent invocations of this method will answer the same file
	 * resource without attempting to create it again.
	 * </p>
	 * <p>
	 * This method should be called within a workspace modify operation since it
	 * creates resources.
	 * </p>
	 *
	 * @return the created file resource, or <code>null</code> if the file was not
	 *         created
	 */
	public IFile createNewFile() {
		if (newFile != null) {
			return newFile;
		}

		// create the new file and cache it if successful

		final IPath containerPath = resourceGroup.getContainerFullPath();
		IPath newFilePath = containerPath.append(resourceGroup.getResource());
		final IFile newFileHandle = createFileHandle(newFilePath);
		final InputStream initialContents = getInitialContents();

		createLinkTarget();

		if (linkTargetPath != null) {
			URI resolvedPath = newFileHandle.getPathVariableManager().resolveURI(linkTargetPath);
			try {
				if (resolvedPath.getScheme() != null && resolvedPath.getSchemeSpecificPart() != null) {
					IFileStore store = EFS.getStore(resolvedPath);
					if (!store.fetchInfo().exists()) {
						MessageDialog dlg = new MessageDialog(getContainer().getShell(),
								IDEWorkbenchMessages.WizardNewFileCreationPage_createLinkLocationTitle, null,
								NLS.bind(IDEWorkbenchMessages.WizardNewFileCreationPage_createLinkLocationQuestion,
										linkTargetPath),
								MessageDialog.QUESTION_WITH_CANCEL, 0, IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL);
						int result = dlg.open();
						if (result == Window.OK) {
							store.getParent().mkdir(0, new NullProgressMonitor());
							OutputStream stream = store.openOutputStream(0, new NullProgressMonitor());
							stream.close();
						}
						if (result == 2)
							return null;
					}
				}
			} catch (CoreException | IOException e) {
				MessageDialog.open(MessageDialog.ERROR, getContainer().getShell(),
						IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorTitle,
						NLS.bind(IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorMessage, e.getMessage()),
						SWT.SHEET);

				return null;
			}
		}

		IRunnableWithProgress op = monitor -> {
			CreateFileOperation op1 = new CreateFileOperation(newFileHandle, linkTargetPath, initialContents,
					IDEWorkbenchMessages.WizardNewFileCreationPage_title);
			try {
				// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=219901
				// directly execute the operation so that the undo state is
				// not preserved. Making this undoable resulted in too many
				// accidental file deletions.
				op1.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
			} catch (final ExecutionException e) {
				getContainer().getShell().getDisplay().syncExec(() -> {
					if (e.getCause() instanceof CoreException) {
						ErrorDialog.openError(getContainer().getShell(), // Was
								// Utilities.getFocusShell()
								IDEWorkbenchMessages.WizardNewFileCreationPage_errorTitle, null, // no special
								// message
								((CoreException) e.getCause()).getStatus());
					} else {
						IDEWorkbenchPlugin.log(getClass(), "createNewFile()", e.getCause()); //$NON-NLS-1$
						MessageDialog.openError(getContainer().getShell(),
								IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorTitle,
								NLS.bind(IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorMessage,
										e.getCause().getMessage()));
					}
				});
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			// Execution Exceptions are handled above but we may still get
			// unexpected runtime errors.
			IDEWorkbenchPlugin.log(getClass(), "createNewFile()", e.getTargetException()); //$NON-NLS-1$
			MessageDialog.open(MessageDialog.ERROR, getContainer().getShell(),
					IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorTitle,
					NLS.bind(IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorMessage,
							e.getTargetException().getMessage()),
					SWT.SHEET);

			return null;
		}

		newFile = newFileHandle;

		return newFile;
	}

	/**
	 * Returns the scheduling rule to use when creating the resource at the given
	 * container path. The rule should be the creation rule for the top-most
	 * non-existing parent.
	 *
	 * @param resource The resource being created
	 * @return The scheduling rule for creating the given resource
	 * @since 3.1
	 * @deprecated As of 3.3, scheduling rules are provided by the undoable
	 *             operation that this page creates and executes.
	 */
	@Deprecated
	protected ISchedulingRule createRule(IResource resource) {
		IResource parent = resource.getParent();
		while (parent != null) {
			if (parent.exists()) {
				return resource.getWorkspace().getRuleFactory().createRule(resource);
			}
			resource = parent;
			parent = parent.getParent();
		}
		return resource.getWorkspace().getRoot();
	}

	/**
	 * Returns the current full path of the containing resource as entered or
	 * selected by the user, or its anticipated initial value.
	 *
	 * @return the container's full path, anticipated initial value, or
	 *         <code>null</code> if no path is known
	 */
	public IPath getContainerFullPath() {
		return resourceGroup.getContainerFullPath();
	}

	/**
	 * Returns the current file name as entered by the user, or its anticipated
	 * initial value. <br>
	 * <br>
	 * The current file name will include the file extension if the preconditions
	 * are met.
	 *
	 * @see WizardNewFileCreationPage#setFileExtension(String)
	 *
	 * @return the file name, its anticipated initial value, or <code>null</code> if
	 *         no file name is known
	 */
	public String getFileName() {
		if (resourceGroup == null) {
			return initialFileName;
		}

		return resourceGroup.getResource();
	}

	/**
	 * Returns the file extension to use when creating the new file.
	 *
	 * @return the file extension or <code>null</code>.
	 * @see WizardNewFileCreationPage#setFileExtension(String)
	 * @since 3.3
	 */
	public String getFileExtension() {
		if (resourceGroup == null) {
			return initialFileExtension;
		}
		return resourceGroup.getResourceExtension();
	}

	/**
	 * Returns a stream containing the initial contents to be given to new file
	 * resource instances. <b>Subclasses</b> may wish to override. This default
	 * implementation provides no initial contents.
	 *
	 * @return initial contents to be given to new file resource instances
	 */
	protected InputStream getInitialContents() {
		return null;
	}

	/**
	 * Returns the label to display in the file name specification visual component
	 * group.
	 * <p>
	 * Subclasses may reimplement.
	 * </p>
	 *
	 * @return the label to display in the file name specification visual component
	 *         group
	 */
	protected String getNewFileLabel() {
		return IDEWorkbenchMessages.WizardNewFileCreationPage_fileLabel;
	}

	/**
	 * Shows/hides the advanced option widgets.
	 */
	protected void handleAdvancedButtonSelect() {
		Shell shell = getShell();
		Point shellSize = shell.getSize();
		Composite composite = (Composite) getControl();

		if (linkedResourceComposite != null) {
			linkedResourceComposite.dispose();
			linkedResourceComposite = null;
			composite.layout();
			shell.setSize(shellSize.x, shellSize.y - linkedResourceGroupHeight);
			advancedButton.setText(IDEWorkbenchMessages.showAdvanced);
		} else {
			linkedResourceComposite = linkedResourceGroup.createContents(linkedResourceParent);
			setupLinkedResourceTarget();
			if (linkedResourceGroupHeight == -1) {
				Point groupSize = linkedResourceComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				linkedResourceGroupHeight = groupSize.y;
			}
			shell.setSize(shellSize.x, shellSize.y + linkedResourceGroupHeight);
			composite.layout();
			advancedButton.setText(IDEWorkbenchMessages.hideAdvanced);
		}
	}

	boolean setupLinkedResourceTargetRecursiveFlag = false;

	private void setupLinkedResourceTarget() {
		if (!setupLinkedResourceTargetRecursiveFlag) {
			setupLinkedResourceTargetRecursiveFlag = true;
			try {
				if (isFilteredByParent()) {
					URI existingLink = linkedResourceGroup.getLinkTargetURI();
					boolean setDefaultLinkValue = false;
					if (existingLink == null)
						setDefaultLinkValue = true;
					else {
						IPath path = URIUtil.toPath(existingLink);
						if (path != null)
							setDefaultLinkValue = path.toPortableString().length() > 0;
					}

					if (setDefaultLinkValue) {
						IPath containerPath = resourceGroup.getContainerFullPath();
						IPath newFilePath = containerPath.append(resourceGroup.getResource());
						IFile newFileHandle = createFileHandle(newFilePath);
						try {
							URI uri = newFileHandle.getPathVariableManager()
									.convertToRelative(newFileHandle.getLocationURI(), false, null);
							linkedResourceGroup.setLinkTarget(URIUtil.toPath(uri).toPortableString());
						} catch (CoreException e) {
							// nothing
						}
					}
				}
			} finally {
				setupLinkedResourceTargetRecursiveFlag = false;
			}
		}
	}

	/**
	 * The <code>WizardNewFileCreationPage</code> implementation of this
	 * <code>Listener</code> method handles all events and enablements for controls
	 * on this page. Subclasses may extend.
	 */
	@Override
	public void handleEvent(Event event) {
		setPageComplete(validatePage());
	}

	/**
	 * Sets the initial contents of the container name entry field, based upon
	 * either a previously-specified initial value or the ability to determine such
	 * a value.
	 */
	protected void initialPopulateContainerNameField() {
		if (initialContainerFullPath != null) {
			resourceGroup.setContainerFullPath(initialContainerFullPath);
		} else {
			Iterator it = currentSelection.iterator();
			if (it.hasNext()) {
				Object object = it.next();
				IResource selectedResource = Adapters.adapt(object, IResource.class);
				if (selectedResource != null) {
					if (selectedResource.getType() == IResource.FILE) {
						selectedResource = selectedResource.getParent();
					}
					if (selectedResource.isAccessible()) {
						resourceGroup.setContainerFullPath(selectedResource.getFullPath());
					}
				}
			}
		}
	}

	/**
	 * Sets the flag indicating whether existing resources are permitted to be
	 * specified on this page.
	 *
	 * @param value <code>true</code> if existing resources are permitted, and
	 *              <code>false</code> otherwise
	 * @since 3.4
	 */
	public void setAllowExistingResources(boolean value) {
		if (resourceGroup == null) {
			initialAllowExistingResources = value;
		} else {
			resourceGroup.setAllowExistingResources(value);
		}
	}

	/**
	 * Sets the value of this page's container name field, or stores it for future
	 * use if this page's controls do not exist yet.
	 *
	 * @param path the full path to the container
	 */
	public void setContainerFullPath(IPath path) {
		if (resourceGroup == null) {
			initialContainerFullPath = path;
		} else {
			resourceGroup.setContainerFullPath(path);
		}
	}

	/**
	 * Sets the value of this page's file name field, or stores it for future use if
	 * this page's controls do not exist yet.
	 *
	 * @param value new file name
	 */
	public void setFileName(String value) {
		if (resourceGroup == null) {
			initialFileName = value;
		} else {
			resourceGroup.setResource(value);
		}
	}

	/**
	 * Set the only file extension allowed for this page's file name field. If this
	 * page's controls do not exist yet, store it for future use. <br>
	 * <br>
	 * If a file extension is specified, then it will always be appended with a '.'
	 * to the text from the file name field for validation when the following
	 * conditions are met: <br>
	 * <br>
	 * (1) File extension length is greater than 0 <br>
	 * (2) File name field text length is greater than 0 <br>
	 * (3) File name field text does not already end with a '.' and the file
	 * extension specified (case sensitive) <br>
	 * <br>
	 * The file extension will not be reflected in the actual file name field until
	 * the file name field loses focus.
	 *
	 * @param value The file extension without the '.' prefix (e.g. 'java', 'xml')
	 * @since 3.3
	 */
	public void setFileExtension(String value) {
		if (resourceGroup == null) {
			initialFileExtension = value;
		} else {
			resourceGroup.setResourceExtension(value);
		}
	}

	/**
	 * Checks whether the linked resource target is valid. Sets the error message
	 * accordingly and returns the status.
	 *
	 * @return IStatus validation result from the CreateLinkedResourceGroup
	 */
	protected IStatus validateLinkedResource() {
		IPath containerPath = resourceGroup.getContainerFullPath();
		IPath newFilePath = containerPath.append(resourceGroup.getResource());
		IFile newFileHandle = createFileHandle(newFilePath);
		IStatus status = linkedResourceGroup.validateLinkLocation(newFileHandle);

		if (status.getSeverity() == IStatus.ERROR) {
			if (firstLinkCheck) {
				setMessage(status.getMessage());
				setErrorMessage(null);
			} else {
				setErrorMessage(status.getMessage());
			}
		} else if (status.getSeverity() == IStatus.WARNING) {
			setMessage(status.getMessage(), WARNING);
			setErrorMessage(null);
		}
		return status;
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 *
	 * @return <code>true</code> if all controls are valid, and <code>false</code>
	 *         if at least one is invalid
	 */
	protected boolean validatePage() {
		boolean valid = true;

		if (!resourceGroup.areAllValuesValid()) {
			// if blank name then fail silently
			if (resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_RESOURCE_EMPTY
					|| resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_CONTAINER_EMPTY) {
				setMessage(resourceGroup.getProblemMessage());
				setErrorMessage(null);
			} else {
				setErrorMessage(resourceGroup.getProblemMessage());
			}
			valid = false;
		}

		String resourceName = resourceGroup.getResource();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IStatus result = null;
		if (resourceName.isEmpty() || IPath.ROOT.isValidSegment(resourceName)) { // just a file (no subfolder(s))
			result = workspace.validateName(resourceName, IResource.FILE);
		} else {
			IPath containerPath = resourceGroup.getContainerFullPath();
			if (containerPath == null) {
				result = workspace.validatePath(resourceName, IResource.FILE);
			} else {
				result = workspace.validatePath(containerPath.toString() + IPath.SEPARATOR + resourceName,
						IResource.FILE);
			}
		}

		if (!result.isOK()) {
			setErrorMessage(result.getMessage());
			return false;
		}

		IStatus linkedResourceStatus = null;
		if (valid) {
			linkedResourceStatus = validateLinkedResource();
			if (linkedResourceStatus.getSeverity() == IStatus.ERROR) {
				valid = false;
			}
		}
		// validateLinkedResource sets messages itself
		if (valid && (linkedResourceStatus == null || linkedResourceStatus.isOK())) {
			setMessage(null);
			setErrorMessage(null);

			// perform "resource exists" check if it was skipped in
			// ResourceAndContainerGroup
			if (resourceGroup.getAllowExistingResources()) {
				String problemMessage = NLS.bind(IDEWorkbenchMessages.ResourceGroup_nameExists, getFileName());
				IPath resourcePath = getContainerFullPath().append(getFileName());
				if (workspace.getRoot().getFolder(resourcePath).exists()) {
					setErrorMessage(problemMessage);
					valid = false;
				}
				if (workspace.getRoot().getFile(resourcePath).exists()) {
					setMessage(problemMessage, IMessageProvider.WARNING);
				}
			}
		}
		if (isFilteredByParent()) {
			setMessage(IDEWorkbenchMessages.WizardNewFileCreationPage_resourceWillBeFilteredWarning,
					IMessageProvider.ERROR);
			setupLinkedResourceTarget();
			valid = false;
		}
		return valid;
	}

	private boolean isFilteredByParent() {
		if ((linkedResourceGroup == null) || linkedResourceGroup.isEnabled())
			return false;
		IPath containerPath = resourceGroup.getContainerFullPath();
		if (containerPath == null)
			return false;
		String resourceName = resourceGroup.getResource();
		if (resourceName == null)
			return false;
		if (resourceName.length() > 0) {
			IPath newFilePath = containerPath.append(resourceName);
			if (newFilePath.segmentCount() < 2)
				return false;
			IFile newFileHandle = createFileHandle(newFilePath);
			IWorkspace workspace = newFileHandle.getWorkspace();
			return !workspace.validateFiltered(newFileHandle).isOK();
		}
		return false;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			resourceGroup.setFocus();
		}
	}
}
