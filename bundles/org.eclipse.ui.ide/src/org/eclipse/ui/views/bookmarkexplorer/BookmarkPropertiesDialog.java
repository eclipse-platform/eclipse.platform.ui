/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Shows the properties of a new or existing bookmark
 */
class BookmarkPropertiesDialog extends Dialog {

    /**
     * The bookmark being shown, or <code>null</code> for a new bookmark
     */
    private IMarker marker = null;

    /**
     * The resource on which to create a new bookmark
     */
    private IResource resource = null;

    /**
     * The initial attributes to use when creating a new bookmark
     */
    private Map initialAttributes = null;

    /**
     * The text control for the Description field.
     */
    private Text descriptionText;

    /**
     * The control for the Creation Time field.
     */
    private Label creationTime;

    /**
     * The text control for the Resource field.
     */
    private Text resourceText;

    /**
     * The text control for the Folder field.
     */
    private Text folderText;

    /**
     * The text control for the Location field.
     */
    private Text locationText;

    /**
     * Dirty flag.  True if any changes have been made.
     */
    private boolean dirty;

    private String title;

    /**
     * Creates the dialog.  By default this dialog creates a new bookmark.
     * To set the resource and initial attributes for the new bookmark, 
     * use <code>setResource</code> and <code>setInitialAttributes</code>.
     * To show or modify an existing bookmark, use <code>setMarker</code>.
     * 
     * @param shell the parent shell
     */
    public BookmarkPropertiesDialog(Shell parentShell) {
        super(parentShell);
    }

    public BookmarkPropertiesDialog(Shell parentShell, String title) {
        super(parentShell);
        this.title = title;
    }

    public void create() {
        super.create();
    }

    /**
     * Sets the marker to show or modify.
     * 
     * @param marker the marker, or <code>null</code> to create a new marker
     */
    public void setMarker(IMarker marker) {
        this.marker = marker;
    }

    /**
     * Returns the marker being created or modified.
     * For a new marker, this returns <code>null</code> until
     * the dialog returns, but is non-null after.
     */
    public IMarker getMarker() {
        return marker;
    }

    /**
     * Sets the resource to use when creating a new bookmark.
     * If not set, the new bookmark is created on the workspace root.
     */
    public void setResource(IResource resource) {
        this.resource = resource;
    }

    /**
     * Returns the resource to use when creating a new bookmark,
     * or <code>null</code> if none has been set.
     * If not set, the new bookmark is created on the workspace root.
     */
    public IResource getResource() {
        return resource;
    }

    /**
     * Sets initial attributes to use when creating a new bookmark.
     * If not set, the new bookmark is created with default attributes.
     */
    public void setInitialAttributes(Map initialAttributes) {
        this.initialAttributes = initialAttributes;
    }

    /**
     * Returns the initial attributes to use when creating a new bookmark,
     * or <code>null</code> if not set.
     * If not set, the new bookmark is created with default attributes.
     */
    public Map getInitialAttributes() {
        return initialAttributes;
    }

    /* (non-Javadoc)
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (title == null)
            newShell.setText(BookmarkMessages
                    .getString("PropertiesDialogTitle.text")); //$NON-NLS-1$
        else
            newShell.setText(title);
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        //initialize resources/properties
        if (marker != null) {
            resource = marker.getResource();
            try {
                initialAttributes = marker.getAttributes();
            } catch (CoreException e) {
            }
        } else if (initialAttributes == null && resource == null) {
            resource = (IResource) ResourcesPlugin.getWorkspace().getRoot();
        }

        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);
        createDescriptionArea(composite);
        if (marker != null)
            createCreationTimeArea(composite);
        if (resource != null && resource.getType() != IResource.ROOT)
            createResourceArea(composite);
        updateDialogFromMarker();
        return composite;
    }

    /**
     * Method createCreationTimeArea.
     * @param composite
     */
    private void createCreationTimeArea(Composite parent) {
        String creation = BookmarkMessages.getString("MarkerCreationTime.text");//$NON-NLS-1$

        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText(creation);
        label.setFont(font);

        creationTime = new Label(composite, SWT.NONE);
        creationTime.setFont(font);
    }

    /**
     * Creates the OK and Cancel buttons.
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Creates the area for the Description field.
     */
    private void createDescriptionArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        Label label = new Label(composite, SWT.NONE);
        label.setText(BookmarkMessages
                .getString("ColumnDescription.dialogText")); //$NON-NLS-1$
        label.setFont(font);
        int style = SWT.SINGLE | SWT.BORDER;
        descriptionText = new Text(composite, style);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = convertHorizontalDLUsToPixels(400);
        descriptionText.setLayoutData(gridData);
        descriptionText.setFont(font);

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                markDirty();
            }
        });
    }

    /**
     * Creates the area for the Resource field.
     */
    private void createResourceArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gridData);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        Label resourceLabel = new Label(composite, SWT.NONE);
        resourceLabel.setText(BookmarkMessages
                .getString("ColumnResource.dialogText")); //$NON-NLS-1$
        resourceLabel.setFont(font);
        resourceText = new Text(composite, SWT.SINGLE | SWT.WRAP
                | SWT.READ_ONLY | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        resourceText.setLayoutData(gridData);
        resourceText.setFont(font);

        Label folderLabel = new Label(composite, SWT.NONE);
        folderLabel.setText(BookmarkMessages
                .getString("ColumnFolder.dialogText")); //$NON-NLS-1$
        folderLabel.setFont(font);
        folderText = new Text(composite, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY
                | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        folderText.setLayoutData(gridData);
        folderText.setFont(font);

        Label locationLabel = new Label(composite, SWT.NONE);
        locationLabel.setText(BookmarkMessages
                .getString("ColumnLocation.dialogText")); //$NON-NLS-1$
        locationLabel.setFont(font);
        locationText = new Text(composite, SWT.SINGLE | SWT.WRAP
                | SWT.READ_ONLY | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        locationText.setLayoutData(gridData);
        locationText.setFont(font);
    }

    /**
     * Updates the dialog from the marker state.
     */
    private void updateDialogFromMarker() {
        if (marker == null) {
            updateDialogForNewMarker();
            return;
        }
        descriptionText.setText(MarkerUtil.getMessage(marker));
        if (creationTime != null)
            creationTime.setText(MarkerUtil.getCreationTime(marker));
        if (resourceText != null)
            resourceText.setText(MarkerUtil.getResourceName(marker));
        if (folderText != null)
            folderText.setText(MarkerUtil.getContainerName(marker));
        if (locationText != null) {
            int line = MarkerUtil.getLineNumber(marker);
            if (line < 0)
                locationText.setText(""); //$NON-NLS-1$
            else
                locationText
                        .setText(BookmarkMessages
                                .format(
                                        "LineIndicator.text", new String[] { String.valueOf(line) })); //$NON-NLS-1$
        }

        descriptionText.selectAll();
    }

    void updateDialogForNewMarker() {
        if (resource != null) {
            resourceText.setText(resource.getName());

            IPath path = resource.getFullPath();
            int n = path.segmentCount() - 1; // n is the number of segments in container, not path
            if (n > 0) {
                int len = 0;
                for (int i = 0; i < n; ++i)
                    len += path.segment(i).length();
                // account for /'s
                if (n > 1)
                    len += n - 1;
                StringBuffer sb = new StringBuffer(len);
                for (int i = 0; i < n; ++i) {
                    if (i != 0)
                        sb.append('/');
                    sb.append(path.segment(i));
                }
                folderText.setText(sb.toString());
            }
        }

        if (initialAttributes != null) {
            Object description = initialAttributes.get(IMarker.MESSAGE);
            if (description != null && description instanceof String)
                descriptionText.setText((String) description);
            descriptionText.selectAll();

            Object line = initialAttributes.get(IMarker.LINE_NUMBER);
            if (line != null && line instanceof Integer)
                locationText
                        .setText(BookmarkMessages
                                .format(
                                        "LineIndicator.text", new String[] { line.toString() })); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * Method declared on Dialog
     */
    protected void okPressed() {
        saveChanges();
        super.okPressed();
    }

    private void markDirty() {
        dirty = true;
    }

    private boolean isDirty() {
        return dirty;
    }

    /**
     * Saves the changes made in the dialog if needed.
     * Creates a new bookmark if needed.
     * Updates the existing bookmark only if there have been changes.
     */
    private void saveChanges() {
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    if (marker == null)
                        createMarker();
                    else if (isDirty())
                        updateMarker();
                }
            }, null);
        } catch (CoreException e) {
            ErrorDialog.openError(getShell(), BookmarkMessages
                    .getString("Error"), null, e.getStatus()); //$NON-NLS-1$
            return;
        }
    }

    /**
     * Creates or updates the marker.  Must be called within a workspace runnable.
     */
    private void updateMarker() throws CoreException {
        // Set the marker attributes from the current dialog field values.
        // Do not use setAttributes(Map) as that overwrites any attributes
        // not covered by the dialog.
        Map attrs = getMarkerAttributesFromDialog();
        for (Iterator i = attrs.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            Object val = attrs.get(key);
            marker.setAttribute(key, val);
        }
    }

    /**
     * Returns the marker attributes to save back to the marker, 
     * based on the current dialog fields.
     */
    private Map getMarkerAttributesFromDialog() {
        Map attribs = new HashMap();
        attribs.put(IMarker.MESSAGE, descriptionText.getText());
        return attribs;
    }

    private void createMarker() {
        if (resource == null || !(resource instanceof IFile))
            return;

        IFile file = (IFile) resource;
        try {
            IMarker newMarker = file.createMarker(IMarker.BOOKMARK);
            if (initialAttributes != null)
                newMarker.setAttributes(initialAttributes);
            String message = descriptionText.getText();
            newMarker.setAttribute(IMarker.MESSAGE, message);
        } catch (CoreException e) {
        }
    }

}