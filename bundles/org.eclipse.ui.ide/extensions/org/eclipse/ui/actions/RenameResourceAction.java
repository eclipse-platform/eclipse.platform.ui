/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Standard action for renaming the selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class RenameResourceAction extends WorkspaceAction {

    /*The tree editing widgets. If treeEditor is null then edit using the
     dialog. We keep the editorText around so that we can close it if
     a new selection is made. */
    private TreeEditor treeEditor;

    private Tree navigatorTree;

    private Text textEditor;

    private Composite textEditorParent;

    private TextActionHandler textActionHandler;

    //The resource being edited if this is being done inline
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

    private static String RENAMING_MESSAGE = IDEWorkbenchMessages.RenameResourceAction_progressMessage;

    /**
     * Creates a new action. Using this constructor directly will rename using a
     * dialog rather than the inline editor of a ResourceNavigator.
     *
     * @param shell the shell for any dialogs
     */
    public RenameResourceAction(Shell shell) {
        super(shell, IDEWorkbenchMessages.RenameResourceAction_text);
        setToolTipText(IDEWorkbenchMessages.RenameResourceAction_toolTip);
        setId(ID);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.RENAME_RESOURCE_ACTION);
    }

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param tree the tree
     */
    public RenameResourceAction(Shell shell, Tree tree) {
        this(shell);
        this.navigatorTree = tree;
        this.treeEditor = new TreeEditor(tree);
    }

    /**
     * Check if the user wishes to overwrite the supplied resource
     * @returns true if there is no collision or delete was successful
     * @param shell the shell to create the dialog in 
     * @param destination - the resource to be overwritten
     */
    private boolean checkOverwrite(final Shell shell,
            final IResource destination) {

        final boolean[] result = new boolean[1];

        //Run it inside of a runnable to make sure we get to parent off of the shell as we are not
        //in the UI thread.

        Runnable query = new Runnable() {
            public void run() {
                String pathName = destination.getFullPath().makeRelative()
                        .toString();
                result[0] = MessageDialog.openQuestion(shell,
                        RESOURCE_EXISTS_TITLE, MessageFormat.format(
                                RESOURCE_EXISTS_MESSAGE,
                                new Object[] { pathName }));
            }

        };

        shell.getDisplay().syncExec(query);
        return result[0];
    }

    /**
     * Check if the supplied resource is read only or null. If it is then ask the user if they want
     * to continue. Return true if the resource is not read only or if the user has given
     * permission.
     * @return boolean
     */
    private boolean checkReadOnlyAndNull(IResource currentResource) {
        //Do a quick read only and null check
        if (currentResource == null)
            return false;

        //Do a quick read only check
        if (currentResource.getResourceAttributes().isReadOnly())
            return MessageDialog.openQuestion(getShell(), CHECK_RENAME_TITLE,
                    MessageFormat.format(CHECK_RENAME_MESSAGE,
                            new Object[] { currentResource.getName() }));
        
        return true;
    }

    Composite createParent() {
        Tree tree = getTree();
        Composite result = new Composite(tree, SWT.NONE);
        TreeItem[] selectedItems = tree.getSelection();
        treeEditor.horizontalAlignment = SWT.LEFT;
        treeEditor.grabHorizontal = true;
        treeEditor.setEditor(result, selectedItems[0]);
        return result;
    }

    /**
     * On Mac the text widget already provides a border when it has focus, so there is no need to draw another one.
     * The value of returned by this method is usd to control the inset we apply to the text field bound's in order to get space for drawing a border.
     * A value of 1 means a one-pixel wide border around the text field. A negative value supresses the border.
     * However, in M9 the system property "org.eclipse.swt.internal.carbon.noFocusRing" has been introduced
     * as a temporary workaround for bug #28842. The existence of the property turns the native focus ring off
     * if the widget is contained in a main window (not dialog).
     * The check for the property should be removed after a final fix for #28842 has been provided.
     */
    private static int getCellEditorInset(Control c) {
        if ("carbon".equals(SWT.getPlatform())) { // special case for MacOS X //$NON-NLS-1$
            if (System
                    .getProperty("org.eclipse.swt.internal.carbon.noFocusRing") == null || c.getShell().getParent() != null) //$NON-NLS-1$
                return -2; // native border
        }
        return 1; //  one pixel wide black border
    }

    /**
     * Create the text editor widget.
     * 
     * @param resource the resource to rename
     */
    private void createTextEditor(final IResource resource) {
        // Create text editor parent.  This draws a nice bounding rect.
        textEditorParent = createParent();
        textEditorParent.setVisible(false);
        final int inset = getCellEditorInset(textEditorParent);
        if (inset > 0) // only register for paint events if we have a border
            textEditorParent.addListener(SWT.Paint, new Listener() {
                public void handleEvent(Event e) {
                    Point textSize = textEditor.getSize();
                    Point parentSize = textEditorParent.getSize();
                    e.gc.drawRectangle(0, 0, Math.min(textSize.x + 4,
                            parentSize.x - 1), parentSize.y - 1);
                }
            });
        // Create inner text editor.
        textEditor = new Text(textEditorParent, SWT.NONE);
        textEditor.setFont(navigatorTree.getFont());
        textEditorParent.setBackground(textEditor.getBackground());
        textEditor.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event e) {
                Point textSize = textEditor.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT);
                textSize.x += textSize.y; // Add extra space for new characters.
                Point parentSize = textEditorParent.getSize();
                textEditor.setBounds(2, inset, Math.min(textSize.x,
                        parentSize.x - 4), parentSize.y - 2 * inset);
                textEditorParent.redraw();
            }
        });
        textEditor.addListener(SWT.Traverse, new Listener() {
            public void handleEvent(Event event) {

                //Workaround for Bug 20214 due to extra
                //traverse events
                switch (event.detail) {
                case SWT.TRAVERSE_ESCAPE:
                    //Do nothing in this case
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
            }
        });
        textEditor.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent fe) {
                saveChangesAndDispose(resource);
            }
        });

        if (textActionHandler != null)
            textActionHandler.addText(textEditor);
    }

    /**
     * Close the text widget and reset the editorText field.
     */
    private void disposeTextWidget() {
        if (textActionHandler != null)
            textActionHandler.removeText(textEditor);

        if (textEditorParent != null) {
            textEditorParent.dispose();
            textEditorParent = null;
            textEditor = null;
            treeEditor.setEditor(null, null);
        }
    }

    /**
     * Returns the elements that the action is to be performed on.
     * Return the resource cached by the action as we cannot rely
     * on the selection being correct for inlined text.
     *
     * @return list of resource elements (element type: <code>IResource</code>)
     */
    protected List getActionResources() {
        if (inlinedResource == null)
            return super.getActionResources();

        List actionResources = new ArrayList();
        actionResources.add(inlinedResource);
        return actionResources;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getOperationMessage() {
        return IDEWorkbenchMessages.RenameResourceAction_progress;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getProblemsMessage() {
        return IDEWorkbenchMessages.RenameResourceAction_problemMessage;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getProblemsTitle() {
        return IDEWorkbenchMessages.RenameResourceAction_problemTitle;
    }

    /**
     * Get the Tree being edited.
     * @returnTree
     */
    private Tree getTree() {
        return this.navigatorTree;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected void invokeOperation(IResource resource, IProgressMonitor monitor)
            throws CoreException {

    	if (!validateMove(resource, newPath)) {
    		return;
    	}
        monitor.beginTask(RENAMING_MESSAGE, 100);
        IWorkspaceRoot workspaceRoot = resource.getWorkspace().getRoot();

        IResource newResource = workspaceRoot.findMember(newPath);
        if (newResource != null) {
            if (checkOverwrite(getShell(), newResource)) {
                if (resource.getType() == IResource.FILE
                        && newResource.getType() == IResource.FILE) {
                    IFile file = (IFile) resource;
                    IFile newFile = (IFile) newResource;
                    if (validateEdit(file, newFile, getShell())) {
                        IProgressMonitor subMonitor = new SubProgressMonitor(
                                monitor, 50);
                        newFile.setContents(file.getContents(),
                                IResource.KEEP_HISTORY, subMonitor);
                        file.delete(IResource.KEEP_HISTORY, subMonitor);
                    }
                    monitor.worked(100);
                    return;
                } 
                newResource.delete(IResource.KEEP_HISTORY,
                        new SubProgressMonitor(monitor, 50));
            } else {
                monitor.worked(100);
                return;
            }
        }
        if (resource.getType() == IResource.PROJECT) {
            IProject project = (IProject) resource;
            IProjectDescription description = project.getDescription();
            description.setName(newPath.segment(0));
            project.move(description, IResource.FORCE | IResource.SHALLOW,
                    monitor);
        } else
            resource.move(newPath, IResource.KEEP_HISTORY | IResource.SHALLOW,
                    new SubProgressMonitor(monitor, 50));
    }

	/**
	 * Validates the operation against the model providers.
	 *
	 * @param resource the resource to move
	 * @param path the new path
	 * @return whether the operation should proceed
	 * @since 3.2
	 */
    private boolean validateMove(IResource resource, IPath path) {
    	IResourceChangeDescriptionFactory factory = ResourceChangeValidator.getValidator().createDeltaFactory();
    	factory.move(resource, path);
		return IDE.promptToConfirm(getShell(), IDEWorkbenchMessages.RenameResourceAction_confirm, NLS.bind(IDEWorkbenchMessages.RenameResourceAction_warning, resource.getName()), factory.getDelta(), modelProviderIds, true /* syncExec */);
	}

	/**
     * Return the new name to be given to the target resource.
     *
     * @return java.lang.String
     * @param resource the resource to query status on
     */
    protected String queryNewResourceName(final IResource resource) {
        final IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
        final IPath prefix = resource.getFullPath().removeLastSegments(1);
        IInputValidator validator = new IInputValidator() {
            public String isValid(String string) {
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
            }
        };

        InputDialog dialog = new InputDialog(getShell(), IDEWorkbenchMessages.RenameResourceAction_inputDialogTitle,
                IDEWorkbenchMessages.RenameResourceAction_inputDialogMessage,
                resource.getName(), validator);
        dialog.setBlockOnOpen(true);
        dialog.open();
        return dialog.getValue();
    }

    /**
     * Return the new name to be given to the target resource or <code>null<code>
     * if the query was canceled. Rename the currently selected resource using the table editor. 
     * Continue the action when the user is done.
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
        textEditor.selectAll();
        textEditor.setFocus();
    }

    /* (non-Javadoc)
     * Method declared on IAction; overrides method on WorkspaceAction.
     */
    public void run() {

        if (this.navigatorTree == null) {
            IResource currentResource = getCurrentResource();
            if (currentResource == null || !currentResource.exists())
                return;
            //Do a quick read only and null check
            if (!checkReadOnlyAndNull(currentResource))
                return;
            String newName = queryNewResourceName(currentResource);
            if (newName == null || newName.equals(""))//$NON-NLS-1$
                return;
            newPath = currentResource.getFullPath().removeLastSegments(1)
                    .append(newName);
            super.run();
        } else
            runWithInlineEditor();
    }

    /* 
     * Run the receiver using an inline editor from the supplied navigator. The
     * navigator will tell the action when the path is ready to run.
     */
    private void runWithInlineEditor() {
        IResource currentResource = getCurrentResource();
        if (!checkReadOnlyAndNull(currentResource))
            return;

        queryNewResourceNameInline(currentResource);

    }
    
    /**
     * Return the currently selected resource. Only return
     * an IResouce if there is one and only one resource selected.
     * @return IResource or <code>null</code> if there is zero
     * or more than one resources selected.
     */
    private IResource getCurrentResource(){
    	List resources = getSelectedResources();
    	if(resources.size() == 1)
    		return (IResource) resources.get(0);
    	return null;
    	
    }

    /**
     * @param path the path
     * @param resource the resource
     */
    protected void runWithNewPath(IPath path, IResource resource) {
        this.newPath = path;
        super.run();
    }

    /**
     * Save the changes and dispose of the text widget.
     * @param resource - the resource to move.
     */
    private void saveChangesAndDispose(IResource resource) {
        if (saving == true)
            return;

        saving = true;
        // Cache the resource to avoid selection loss since a selection of
        // another item can trigger this method
        inlinedResource = resource;
        final String newName = textEditor.getText();
        // Run this in an async to make sure that the operation that triggered
        // this action is completed.  Otherwise this leads to problems when the
        // icon of the item being renamed is clicked (i.e., which causes the rename
        // text widget to lose focus and trigger this method).
        Runnable query = new Runnable() {
            public void run() {
                try {
                    if (!newName.equals(inlinedResource.getName())) {
                        IWorkspace workspace = IDEWorkbenchPlugin
                                .getPluginWorkspace();
                        IStatus status = workspace.validateName(newName,
                                inlinedResource.getType());
                        if (!status.isOK()) {
                            displayError(status.getMessage());
                        } else {
                            IPath newPath = inlinedResource.getFullPath()
                                    .removeLastSegments(1).append(newName);
                            runWithNewPath(newPath, inlinedResource);
                        }
                    }
                    inlinedResource = null;
                    //Dispose the text widget regardless
                    disposeTextWidget();
                    // Ensure the Navigator tree has focus, which it may not if the
                    // text widget previously had focus.
                    if (navigatorTree != null && !navigatorTree.isDisposed()) {
                        navigatorTree.setFocus();
                    }
                } finally {
                    saving = false;
                }
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
    protected boolean updateSelection(IStructuredSelection selection) {
        disposeTextWidget();

        if (selection.size() > 1)
            return false;
        if (!super.updateSelection(selection))
            return false;

        IResource currentResource = getCurrentResource();
        if (currentResource == null || !currentResource.exists())
            return false;

        return true;
    }

    /**
     * Set the text action handler.
     * 
     * @param actionHandler the action handler
     */
    public void setTextActionHandler(TextActionHandler actionHandler) {
        textActionHandler = actionHandler;
    }

    /**
     * Validates the destination file if it is read-only and additionally 
     * the source file if both are read-only.
     * Returns true if both files could be made writeable.
     * 
     * @param source source file
     * @param destination destination file
     * @param shell ui context for the validation
     * @return boolean <code>true</code> both files could be made writeable.
     * 	<code>false</code> either one or both files were not made writeable  
     */
    boolean validateEdit(IFile source, IFile destination, Shell shell) {
        if (destination.isReadOnly()) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IStatus status;
            if (source.isReadOnly())
                status = workspace.validateEdit(new IFile[] { source,
                        destination }, shell);
            else
                status = workspace.validateEdit(new IFile[] { destination },
                        shell);
            return status.isOK();
        }
        return true;
    }
    
    /**
     * Returns the model provider ids that are known to the client
     * that instantiated this operation.
     * 
     * @return the model provider ids that are known to the client
     * that instantiated this operation.
     * @since 3.2
     */
	public String[] getModelProviderIds() {
		return modelProviderIds;
	}

	/**
     * Sets the model provider ids that are known to the client
     * that instantiated this operation. Any potential side effects
     * reported by these models during validation will be ignored.
     * 
	 * @param modelProviderIds the model providers known to the client
	 * who is using this operation.
	 * @since 3.2
	 */
	public void setModelProviderIds(String[] modelProviderIds) {
		this.modelProviderIds = modelProviderIds;
	}

}
