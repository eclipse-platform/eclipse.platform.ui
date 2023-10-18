/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.actions.LTKLauncher;

/**
 * Standard action for renaming the selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RenameResourceAction extends WorkspaceAction {

	private static final String WORKBENCH_PLUGIN_ID = "org.eclipse.ui.workbench"; //$NON-NLS-1$

	/*
	 * The tree editing widgets. If treeEditor is null then edit using the
	 * dialog. We keep the editorText around so that we can close it if a new
	 * selection is made.
	 */
	private TreeEditor treeEditor;

	private Tree navigatorTree;

	private Text textEditor;

	private Composite textEditorParent;

	private TextActionHandler textActionHandler;

	// The resource being edited if this is being done inline
	private IResource inlinedResource;

	private boolean saving = false;

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID
			+ ".RenameResourceAction";//$NON-NLS-1$

	/**
	 * The new path.
	 */
	private IPath newPath;

	private String[] modelProviderIds;

	private static final String CHECK_RENAME_TITLE = IDEWorkbenchMessages.RenameResourceAction_checkTitle;

	private static final String CHECK_RENAME_MESSAGE = IDEWorkbenchMessages.RenameResourceAction_readOnlyCheck;

	private static String RESOURCE_EXISTS_TITLE = IDEWorkbenchMessages.RenameResourceAction_resourceExists;

	private static String RESOURCE_EXISTS_MESSAGE = IDEWorkbenchMessages.RenameResourceAction_overwriteQuestion;

	private static String PROJECT_EXISTS_MESSAGE = IDEWorkbenchMessages.RenameResourceAction_overwriteProjectQuestion;

	private static String PROJECT_EXISTS_TITLE = IDEWorkbenchMessages.RenameResourceAction_projectExists;

	/**
	 * Creates a new action. Using this constructor directly will rename using a
	 * dialog rather than the inline editor of a ResourceNavigator.
	 *
	 * @param shell
	 *            the shell for any dialogs
	 * @deprecated see {@link #RenameResourceAction(IShellProvider)}
	 */
	@Deprecated
	public RenameResourceAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.RenameResourceAction_text);
		initAction();
	}

	/**
	 * Creates a new action. Using this constructor directly will rename using a
	 * dialog rather than the inline editor of a ResourceNavigator.
	 *
	 * @param provider
	 *            the IShellProvider for any dialogs
	 * @since 3.4
	 */
	public RenameResourceAction(IShellProvider provider){
		super(provider, IDEWorkbenchMessages.RenameResourceAction_text);
		initAction();
	}

	private void initAction(){
		setToolTipText(IDEWorkbenchMessages.RenameResourceAction_toolTip);
		setId(ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.RENAME_RESOURCE_ACTION);
	}
	/**
	 * Creates a new action.
	 *
	 * @param shell
	 *            the shell for any dialogs
	 * @param tree
	 *            the tree
	 * @deprecated see {@link #RenameResourceAction(IShellProvider, Tree)}
	 */
	@Deprecated
	public RenameResourceAction(Shell shell, Tree tree) {
		this(shell);
		this.navigatorTree = tree;
		this.treeEditor = new TreeEditor(tree);
	}

	/**
	 * Creates a new action.
	 *
	 * @param provider
	 *            the shell provider for any dialogs
	 * @param tree
	 *            the tree
	 * @since 3.4
	 */
	public RenameResourceAction(IShellProvider provider, Tree tree) {
		this(provider);
		this.navigatorTree = tree;
		this.treeEditor = new TreeEditor(tree);
	}

	/**
	 * Check if the user wishes to overwrite the supplied resource
	 *
	 * @param shell
	 *            the shell to create the dialog in
	 * @param destination -
	 *            the resource to be overwritten
	 * @return true if there is no collision or delete was successful
	 */
	private boolean checkOverwrite(final Shell shell,
			final IResource destination) {

		final boolean[] result = new boolean[1];

		// Run it inside of a runnable to make sure we get to parent off of the
		// shell as we are not in the UI thread.

		Runnable query = () -> {
			String pathName = destination.getFullPath().makeRelative()
					.toString();
			String message = RESOURCE_EXISTS_MESSAGE;
			String title = RESOURCE_EXISTS_TITLE;
			if (destination.getType() == IResource.PROJECT) {
				message = PROJECT_EXISTS_MESSAGE;
				title = PROJECT_EXISTS_TITLE;
			}
			result[0] = MessageDialog.openQuestion(shell,
					title, MessageFormat.format(message, pathName));
		};

		shell.getDisplay().syncExec(query);
		return result[0];
	}

	/**
	 * Check if the supplied resource is read only or null. If it is then ask
	 * the user if they want to continue. Return true if the resource is not
	 * read only or if the user has given permission.
	 *
	 * @return boolean
	 */
	private boolean checkReadOnlyAndNull(IResource currentResource) {
		// Do a quick read only and null check
		if (currentResource == null) {
			return false;
		}

		// Do a quick read only check
		final ResourceAttributes attributes = currentResource
				.getResourceAttributes();
		if (attributes != null && attributes.isReadOnly()) {
			return MessageDialog.openQuestion(getShell(), CHECK_RENAME_TITLE,
					MessageFormat.format(CHECK_RENAME_MESSAGE, currentResource.getName()));
		}

		return true;
	}

	Composite createParent() {
		Tree tree = getTree();
		Composite result = new Composite(tree, SWT.NONE);
		TreeItem[] selectedItems = tree.getSelection();
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.grabHorizontal = true;
		if (selectedItems.length != 0) {
			treeEditor.setEditor(result, selectedItems[0]);
		}
		return result;
	}

	/**
	 * Get the inset used for cell editors
	 * @param c the Control
	 * @return int
	 */
	private static int getCellEditorInset(Control c) {
		return 1; // one pixel wide black border
	}

	/**
	 * Create the text editor widget.
	 *
	 * @param resource
	 *            the resource to rename
	 */
	private void createTextEditor(final IResource resource) {
		// Create text editor parent. This draws a nice bounding rect.
		textEditorParent = createParent();
		textEditorParent.setVisible(false);
		final int inset = getCellEditorInset(textEditorParent);
		if (inset > 0) {
			textEditorParent.addListener(SWT.Paint, e -> {
				Point textSize = textEditor.getSize();
				Point parentSize = textEditorParent.getSize();
				e.gc.drawRectangle(0, 0, Math.min(textSize.x + 4,
						parentSize.x - 1), parentSize.y - 1);
			});
		}
		// Create inner text editor.
		textEditor = new Text(textEditorParent, SWT.NONE);
		textEditor.setFont(navigatorTree.getFont());
		textEditorParent.setBackground(textEditor.getBackground());
		textEditor.addListener(SWT.Modify, e -> {
			Point textSize = textEditor.computeSize(SWT.DEFAULT,
					SWT.DEFAULT);
			textSize.x += textSize.y; // Add extra space for new
			// characters.
			Point parentSize = textEditorParent.getSize();
			textEditor.setBounds(2, inset, Math.min(textSize.x,
					parentSize.x - 4), parentSize.y - 2 * inset);
			textEditorParent.redraw();
		});
		textEditor.addListener(SWT.Traverse, event -> {

			// Workaround for Bug 20214 due to extra
			// traverse events
			switch (event.detail) {
			case SWT.TRAVERSE_ESCAPE:
				// Do nothing in this case
				disposeTextWidget();
				event.doit = true;
				event.detail = SWT.TRAVERSE_NONE;
				break;
			case SWT.TRAVERSE_RETURN:
				saveChangesAndDispose(resource);
				event.doit = true;
				event.detail = SWT.TRAVERSE_NONE;
				break;
			}
		});
		textEditor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent fe) {
				saveChangesAndDispose(resource);
			}
		});

		if (textActionHandler != null) {
			textActionHandler.addText(textEditor);
		}
	}

	/**
	 * Close the text widget and reset the editorText field.
	 */
	private void disposeTextWidget() {
		if (textActionHandler != null) {
			textActionHandler.removeText(textEditor);
		}

		if (textEditorParent != null) {
			textEditorParent.dispose();
			textEditorParent = null;
			textEditor = null;
			treeEditor.setEditor(null, null);
		}
	}

	/**
	 * Returns the elements that the action is to be performed on. Return the
	 * resource cached by the action as we cannot rely on the selection being
	 * correct for inlined text.
	 *
	 * @return list of resource elements (element type: <code>IResource</code>)
	 */
	@Override
	protected List<? extends IResource> getActionResources() {
		if (inlinedResource == null) {
			return super.getActionResources();
		}

		List<IResource> actionResources = new ArrayList<>();
		actionResources.add(inlinedResource);
		return actionResources;
	}

	@Override
	protected String getOperationMessage() {
		return IDEWorkbenchMessages.RenameResourceAction_progress;
	}

	@Override
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.RenameResourceAction_problemMessage;
	}

	@Override
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.RenameResourceAction_problemTitle;
	}

	/**
	 * Get the Tree being edited.
	 *
	 * @return Tree
	 */
	private Tree getTree() {
		return this.navigatorTree;
	}

	/**
	 * Return the new name to be given to the target resource.
	 *
	 * @return java.lang.String
	 * @param resource
	 *            the resource to query status on
	 */
	protected String queryNewResourceName(final IResource resource) {
		final IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		final IPath prefix = resource.getFullPath().removeLastSegments(1);
		IInputValidator validator = string -> {
			if (resource.getName().equals(string)) {
				return IDEWorkbenchMessages.RenameResourceAction_nameMustBeDifferent;
			}
			IStatus status = workspace.validateName(string, resource
					.getType());
			if (!status.isOK()) {
				return status.getMessage();
			}
			if (workspace.getRoot().exists(prefix.append(string))) {
				return IDEWorkbenchMessages.RenameResourceAction_nameExists;
			}
			return null;
		};

		InputDialog dialog = new InputDialog(getShell(),
				IDEWorkbenchMessages.RenameResourceAction_inputDialogTitle,
				IDEWorkbenchMessages.RenameResourceAction_inputDialogMessage,
				resource.getName(), validator);
		dialog.setBlockOnOpen(true);
		int result = dialog.open();
		if (result == Window.OK)
			return dialog.getValue();
		return null;
	}

	/**
	 * Return the new name to be given to the target resource or <code>null</code>
	 * if the query was canceled. Rename the currently selected resource using the
	 * table editor. Continue the action when the user is done.
	 *
	 * @param resource the resource to rename
	 */
	private void queryNewResourceNameInline(final IResource resource) {
		// Make sure text editor is created only once. Simply reset text
		// editor when action is executed more than once. Fixes bug 22269.
		if (textEditorParent == null) {
			createTextEditor(resource);
		}
		textEditor.setText(resource.getName());

		// Open text editor with initial size.
		textEditorParent.setVisible(true);
		Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		textSize.x += textSize.y; // Add extra space for new characters.
		Point parentSize = textEditorParent.getSize();
		int inset = getCellEditorInset(textEditorParent);
		textEditor.setBounds(2, inset, Math.min(textSize.x, parentSize.x - 4),
				parentSize.y - 2 * inset);
		textEditorParent.redraw();
		int startOfFileExtension = resource.getName().lastIndexOf('.'); // $NON-NLS-1$
		if (startOfFileExtension == -1) {
			textEditor.selectAll();
		} else {
			textEditor.setSelection(0, startOfFileExtension);
		}
		textEditor.setFocus();
	}

	@Override
	public void run() {
		IResource currentResource = getCurrentResource();
		if (currentResource == null || !currentResource.exists()) {
			return;
		}

		String defaultValue = ""; //$NON-NLS-1$
		IPreferencesService preferences = Platform.getPreferencesService();
		String renameMode = preferences.getString(WORKBENCH_PLUGIN_ID,
				IWorkbenchPreferenceConstants.RESOURCE_RENAME_MODE, defaultValue,
				null);
		boolean dialogMode = IWorkbenchPreferenceConstants.RESOURCE_RENAME_MODE_DIALOG.equals(renameMode);

		if (this.navigatorTree == null || dialogMode || LTKLauncher.isCompositeRename(getStructuredSelection())) {
			if (!LTKLauncher.openRenameWizard(getStructuredSelection())) {
				// LTK Launcher couldn't rename the resource
				if (!checkReadOnlyAndNull(currentResource)) {
					return;
				}
				String newName = queryNewResourceName(currentResource);
				if (newName == null || newName.isEmpty()) {
					return;
				}
				newPath = currentResource.getFullPath().removeLastSegments(1).append(newName);
				super.run();
			}
		} else {
			runWithInlineEditor();
		}
	}

	/*
	 * Run the receiver using an inline editor from the supplied navigator. The
	 * navigator will tell the action when the path is ready to run.
	 */
	private void runWithInlineEditor() {
		IResource currentResource = getCurrentResource();
		if (!checkReadOnlyAndNull(currentResource)) {
			return;
		}
		queryNewResourceNameInline(currentResource);
	}

	/**
	 * Return the currently selected resource. Only return an IResouce if there
	 * is one and only one resource selected.
	 *
	 * @return IResource or <code>null</code> if there is zero or more than
	 *         one resources selected.
	 */
	private IResource getCurrentResource() {
		List<? extends IResource> resources = getSelectedResources();
		if (resources.size() == 1) {
			return resources.get(0);
		}
		return null;

	}

	/**
	 * @param path
	 *            the path
	 * @param resource
	 *            the resource
	 */
	protected void runWithNewPath(IPath path, IResource resource) {
		this.newPath = path;
		super.run();
	}

	/**
	 * Save the changes and dispose of the text widget.
	 *
	 * @param resource -
	 *            the resource to move.
	 */
	private void saveChangesAndDispose(IResource resource) {
		if (saving == true) {
			return;
		}

		saving = true;
		// Cache the resource to avoid selection loss since a selection of
		// another item can trigger this method
		inlinedResource = resource;
		final String newName = textEditor.getText();
		// Run this in an async to make sure that the operation that triggered
		// this action is completed. Otherwise this leads to problems when the
		// icon of the item being renamed is clicked (i.e., which causes the
		// rename
		// text widget to lose focus and trigger this method).
		Runnable query = () -> {
			try {
				if (!newName.equals(inlinedResource.getName())) {
					IWorkspace workspace = IDEWorkbenchPlugin
							.getPluginWorkspace();
					IStatus status = workspace.validateName(newName,
							inlinedResource.getType());
					if (!status.isOK()) {
						displayError(status.getMessage());
					} else if (!LTKLauncher.renameResource(newName, new StructuredSelection(inlinedResource))) {
						// LTK Launcher couldn't rename the resource
						IPath newPath = inlinedResource.getFullPath().removeLastSegments(1).append(newName);
						runWithNewPath(newPath, inlinedResource);
					}
				}
				inlinedResource = null;
				// Dispose the text widget regardless
				disposeTextWidget();
				// Ensure the Navigator tree has focus, which it may not if
				// the
				// text widget previously had focus.
				if (navigatorTree != null && !navigatorTree.isDisposed()) {
					navigatorTree.setFocus();
				}
			} finally {
				saving = false;
			}
		};
		getTree().getShell().getDisplay().asyncExec(query);
	}

	/**
	 * The <code>RenameResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * disabled if any of the selections are not resources or resources that are
	 * not local.
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		disposeTextWidget();

		if (selection.size() > 1) {
			return false;
		}
		if (!super.updateSelection(selection)) {
			return false;
		}

		IResource currentResource = getCurrentResource();
		if (currentResource == null || !currentResource.exists()) {
			return false;
		}

		return true;
	}

	/**
	 * Set the text action handler.
	 *
	 * @param actionHandler
	 *            the action handler
	 */
	public void setTextActionHandler(TextActionHandler actionHandler) {
		textActionHandler = actionHandler;
	}

	/**
	 * Returns the model provider ids that are known to the client that
	 * instantiated this operation.
	 *
	 * @return the model provider ids that are known to the client that
	 *         instantiated this operation.
	 * @since 3.2
	 */
	public String[] getModelProviderIds() {
		return modelProviderIds;
	}

	/**
	 * Sets the model provider ids that are known to the client that
	 * instantiated this operation. Any potential side effects reported by these
	 * models during validation will be ignored.
	 *
	 * @param modelProviderIds
	 *            the model providers known to the client who is using this
	 *            operation.
	 * @since 3.2
	 */
	public void setModelProviderIds(String[] modelProviderIds) {
		this.modelProviderIds = modelProviderIds;
	}

	/*
	 * Overridden to create and execute an undoable operation that performs the
	 * rename.
	 */
	@Override
	protected IRunnableWithProgress createOperation(final IStatus[] errorStatus) {
		return monitor -> {
			IResource[] resources = getActionResources()
					.toArray(new IResource[getActionResources().size()]);
			// Rename is only valid for a single resource. This has already
			// been validated.
			if (resources.length == 1) {
				// check for overwrite
				IWorkspaceRoot workspaceRoot = resources[0].getWorkspace()
						.getRoot();
				IResource newResource = workspaceRoot.findMember(newPath);
				boolean go = true;
				if (newResource != null) {
					go = checkOverwrite(getShell(), newResource);
				}
				if (go) {
					MoveResourcesOperation op = new MoveResourcesOperation(
							resources[0],
							newPath,
							IDEWorkbenchMessages.RenameResourceAction_operationTitle);
					op.setModelProviderIds(getModelProviderIds());
					try {
						PlatformUI
								.getWorkbench()
								.getOperationSupport()
								.getOperationHistory()
								.execute(
										op,
										monitor,
										WorkspaceUndoUtil
												.getUIInfoAdapter(getShell()));
					} catch (ExecutionException e) {
						IDEWorkbenchPlugin.log(e.toString());
						if (e.getCause() instanceof CoreException) {
							errorStatus[0] = ((CoreException) e.getCause())
									.getStatus();
						} else {
							errorStatus[0] = new Status(IStatus.ERROR,
									PlatformUI.PLUGIN_ID,
									getProblemsMessage(), e);
						}
					}
				}
			}
		};
	}
}
