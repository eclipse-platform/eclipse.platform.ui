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

package org.eclipse.ui.internal.ide.dialogs;

import java.io.File;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ide.dialogs.PathVariableSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Widget group for specifying a linked file or folder target.
 * 
 * @since 2.1
 */
public class CreateLinkedResourceGroup {
    private Listener listener;

    private String linkTarget = ""; //$NON-NLS-1$

    private int type;

    private boolean createLink = false;

    // used to compute layout sizes
    private FontMetrics fontMetrics;

    // widgets
    private Composite groupComposite;

    private Text linkTargetField;

    private Button browseButton;

    private Button variablesButton;

    private Label resolvedPathLabelText;

    private Label resolvedPathLabelData;

    /**
     * Creates a link target group 
     *
     * @param type specifies the type of resource to link to. 
     * 	<code>IResource.FILE</code> or <code>IResource.FOLDER</code>
     * @param listener listener to notify when one of the widgets'
     * 	value is changed.
     */
    public CreateLinkedResourceGroup(int type, Listener listener) {
        this.type = type;
        this.listener = listener;
    }

    /**
     * Creates the widgets 
     * 
     * @param parent parent composite of the widget group
     * @return the widget group
     */
    public Composite createContents(Composite parent) {
        Font font = parent.getFont();
        initializeDialogUnits(parent);
        // top level group
        groupComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        groupComposite.setLayout(layout);
        groupComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.FILL_HORIZONTAL));
        groupComposite.setFont(font);

        final Button createLinkButton = new Button(groupComposite, SWT.CHECK);
        if (type == IResource.FILE)
            createLinkButton.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_linkFileButton);
        else
            createLinkButton.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_linkFolderButton);
        createLinkButton.setSelection(createLink);
        createLinkButton.setFont(font);
        SelectionListener selectionListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                createLink = createLinkButton.getSelection();
                browseButton.setEnabled(createLink);
                variablesButton.setEnabled(createLink);
                linkTargetField.setEnabled(createLink);
                if (listener != null)
                    listener.handleEvent(new Event());
            }
        };
        createLinkButton.addSelectionListener(selectionListener);

        createLinkLocationGroup(groupComposite, createLink);
        return groupComposite;
    }

    /**
     * Creates the link target location widgets.
     *
     * @param locationGroup the parent composite
     * @param enabled sets the initial enabled state of the widgets
     */
    private void createLinkLocationGroup(Composite locationGroup,
            boolean enabled) {
        Font font = locationGroup.getFont();
        Button button = new Button(locationGroup, SWT.CHECK);
        int indent = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

        button.dispose();

        // linkTargetGroup is necessary to decouple layout from 
        // resolvedPathGroup layout
        Composite linkTargetGroup = new Composite(locationGroup, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        linkTargetGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = indent;
        linkTargetGroup.setLayoutData(data);
        linkTargetGroup.setFont(font);

        // link target location entry field
        linkTargetField = new Text(linkTargetGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        linkTargetField.setLayoutData(data);
        linkTargetField.setFont(font);
        linkTargetField.setEnabled(enabled);
        linkTargetField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                linkTarget = linkTargetField.getText();
                resolveVariable();
                if (listener != null)
                    listener.handleEvent(new Event());
            }
        });
        // browse button
        browseButton = new Button(linkTargetGroup, SWT.PUSH);
        setButtonLayoutData(browseButton);
        browseButton.setFont(font);
        browseButton.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_browseButton);
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleLinkTargetBrowseButtonPressed();
            }
        });
        browseButton.setEnabled(enabled);

        // variables button
        variablesButton = new Button(linkTargetGroup, SWT.PUSH);
        setButtonLayoutData(variablesButton);
        variablesButton.setFont(font);
        variablesButton.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_variablesButton);
        variablesButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleVariablesButtonPressed();
            }
        });
        variablesButton.setEnabled(enabled);

        Composite resolvedPathGroup = new Composite(locationGroup, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        resolvedPathGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = indent;
        resolvedPathGroup.setLayoutData(data);
        resolvedPathGroup.setFont(font);

        resolvedPathLabelText = new Label(resolvedPathGroup, SWT.SINGLE);
        resolvedPathLabelText.setText(IDEWorkbenchMessages.CreateLinkedResourceGroup_resolvedPathLabel);
        resolvedPathLabelText.setVisible(false);
        resolvedPathLabelText.setFont(font);

        resolvedPathLabelData = new Label(resolvedPathGroup, SWT.SINGLE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        resolvedPathLabelData.setLayoutData(data);
        resolvedPathLabelData.setVisible(false);
        resolvedPathLabelData.setFont(font);

        if (linkTarget != null)
            linkTargetField.setText(linkTarget);
    }

    /**
     * Returns a new status object with the given severity and message.
     * 
     * @return a new status object with the given severity and message.
     */
    private IStatus createStatus(int severity, String message) {
        return new Status(severity, IDEWorkbenchPlugin.getDefault()
                .getDescriptor().getUniqueIdentifier(), severity, message, null);
    }

    /**
     * Disposes the group's widgets. 
     */
    public void dispose() {
        if (groupComposite != null && groupComposite.isDisposed() == false)
            groupComposite.dispose();
    }

    /**
     * Returns the link target location entered by the user. 
     *
     * @return the link target location entered by the user. null if the user
     * 	chose not to create a link.
     */
    public String getLinkTarget() {
        if (createLink)
            return linkTarget;

        return null;
    }

    /**
     * Opens a file or directory browser depending on the link type.
     */
    private void handleLinkTargetBrowseButtonPressed() {
        File file = null;
        String selection = null;

        if ("".equals(linkTarget) == false) { //$NON-NLS-1$
            file = new File(linkTarget);
            if (file.exists() == false)
                file = null;
        }
        if (type == IResource.FILE) {
            FileDialog dialog = new FileDialog(linkTargetField.getShell());
            if (file != null) {
                if (file.isFile())
                    dialog.setFileName(linkTarget);
                else
                    dialog.setFilterPath(linkTarget);
            }
            selection = dialog.open();
        } else {
            DirectoryDialog dialog = new DirectoryDialog(linkTargetField
                    .getShell());
            if (file != null) {
                String path = linkTarget;
                if (file.isFile())
                    path = file.getParent();
                if (path != null)
                    dialog.setFilterPath(path);
            }
            dialog
                    .setMessage(IDEWorkbenchMessages.CreateLinkedResourceGroup_targetSelectionLabel);
            selection = dialog.open();
        }
        if (selection != null)
            linkTargetField.setText(selection);
    }

    /**
     * Opens a path variable selection dialog
     */
    private void handleVariablesButtonPressed() {
        int variableTypes = IResource.FOLDER;

        // allow selecting file and folder variables when creating a 
        // linked file
        if (type == IResource.FILE)
            variableTypes |= IResource.FILE;

        PathVariableSelectionDialog dialog = new PathVariableSelectionDialog(
                linkTargetField.getShell(), variableTypes);
        if (dialog.open() == IDialogConstants.OK_ID) {
            String[] variableNames = (String[]) dialog.getResult();
            if (variableNames != null && variableNames.length == 1)
                linkTargetField.setText(variableNames[0]);
        }
    }

    /**
     * Initializes the computation of horizontal and vertical dialog units
     * based on the size of current font.
     * <p>
     * This method must be called before <code>setButtonLayoutData</code> 
     * is called.
     * </p>
     *
     * @param control a control from which to obtain the current font
     */
    protected void initializeDialogUnits(Control control) {
        // Compute and store a font metric
        GC gc = new GC(control);
        gc.setFont(control.getFont());
        fontMetrics = gc.getFontMetrics();
        gc.dispose();
    }

    /**
     * Tries to resolve the value entered in the link target field as 
     * a variable, if the value is a relative path.
     * Displays the resolved value if the entered value is a variable.
     */
    private void resolveVariable() {
        IPathVariableManager pathVariableManager = ResourcesPlugin
                .getWorkspace().getPathVariableManager();
        IPath path = new Path(linkTarget);
        IPath resolvedPath = pathVariableManager.resolvePath(path);

        if (path.equals(resolvedPath)) {
            resolvedPathLabelText.setVisible(false);
            resolvedPathLabelData.setVisible(false);
        } else {
            resolvedPathLabelText.setVisible(true);
            resolvedPathLabelData.setVisible(true);
        }
        resolvedPathLabelData.setText(resolvedPath.toOSString());
    }

    /**
     * Sets the <code>GridData</code> on the specified button to
     * be one that is spaced for the current dialog page units. The
     * method <code>initializeDialogUnits</code> must be called once
     * before calling this method for the first time.
     *
     * @param button the button to set the <code>GridData</code>
     * @return the <code>GridData</code> set on the specified button
     */
    private GridData setButtonLayoutData(Button button) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
                IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        return data;
    }

    /**
     * Sets the value of the link target field
     * 
     * @param target the value of the link target field
     */
    public void setLinkTarget(String target) {
        linkTarget = target;
        if (linkTargetField != null && linkTargetField.isDisposed() == false)
            linkTargetField.setText(target);
    }

    /**
     * Validates the type of the given file against the link type specified
     * in the constructor.
     * 
     * @param linkTargetFile file to validate
     * @return IStatus indicating the validation result. IStatus.OK if the 
     * 	given file is valid.
     */
    private IStatus validateFileType(File linkTargetFile) {
        if (type == IResource.FILE && linkTargetFile.isFile() == false) {
            return createStatus(IStatus.ERROR, IDEWorkbenchMessages.CreateLinkedResourceGroup_linkTargetNotFile);
        } else if (type == IResource.FOLDER
                && linkTargetFile.isDirectory() == false) {
            return createStatus(IStatus.ERROR, IDEWorkbenchMessages.CreateLinkedResourceGroup_linkTargetNotFolder);
        }
        return createStatus(IStatus.OK, ""); //$NON-NLS-1$
    }

    /**
     * Validates this page's controls.
     *
     * @return IStatus indicating the validation result. IStatus.OK if the 
     * 	specified link target is valid given the linkHandle.
     */
    public IStatus validateLinkLocation(IResource linkHandle) {
        if (linkTargetField == null || linkTargetField.isDisposed())
            return createStatus(IStatus.OK, ""); //$NON-NLS-1$

        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
        IPath path = new Path(linkTarget);

        if (createLink == false)
            return createStatus(IStatus.OK, ""); //$NON-NLS-1$

        IStatus locationStatus = workspace.validateLinkLocation(linkHandle,
                path);
        if (locationStatus.getSeverity() == IStatus.ERROR)
            return locationStatus;

        // use the resolved link target name
        String resolvedLinkTarget = resolvedPathLabelData.getText();
        path = new Path(resolvedLinkTarget);
        File linkTargetFile = new Path(resolvedLinkTarget).toFile();
        if (linkTargetFile.exists()) {
            IStatus fileTypeStatus = validateFileType(linkTargetFile);
            if (fileTypeStatus.isOK() == false)
                return fileTypeStatus;
        } else if (locationStatus.getSeverity() == IStatus.OK) {
            // locationStatus takes precedence over missing location warning.
            return createStatus(
                    IStatus.WARNING,
                    IDEWorkbenchMessages.CreateLinkedResourceGroup_linkTargetNonExistent);
        }
        return locationStatus;
    }
}
