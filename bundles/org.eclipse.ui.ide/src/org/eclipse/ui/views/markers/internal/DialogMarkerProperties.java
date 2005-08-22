/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;

/**
 * Shows the properties of a new or existing marker
 */
class DialogMarkerProperties extends Dialog {

    /**
     * The marker being shown, or <code>null</code> for a new marker
     */
    private IMarker marker = null;

    /**
     * The resource on which to create a new marker
     */
    private IResource resource = null;

    /**
     * The type of marker to be created
     */
    private String type = IMarker.MARKER;

    /**
     * The initial attributes to use when creating a new marker
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
     * Creates the dialog.  By default this dialog creates a new marker.
     * To set the resource and initial attributes for the new marker, 
     * use <code>setResource</code> and <code>setInitialAttributes</code>.
     * To show or modify an existing marker, use <code>setMarker</code>.
     * 
     * @param shell the parent shell
     */
    DialogMarkerProperties(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Creates the dialog.  By default this dialog creates a new marker.
     * To set the resource and initial attributes for the new marker, 
     * use <code>setResource</code> and <code>setInitialAttributes</code>.
     * To show or modify an existing marker, use <code>setMarker</code>.
     * 
     * @param shell the parent shell
     * @param title the title of the dialog
     */
    DialogMarkerProperties(Shell parentShell, String title) {
        super(parentShell);
        this.title = title;
    }

    /**
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create() {
        super.create();
    }

    /**
     * Sets the marker to show or modify.
     * 
     * @param marker the marker, or <code>null</code> to create a new marker
     */
    void setMarker(IMarker marker) {
        this.marker = marker;
        if (marker != null) {
            try {
                type = marker.getType();
            } catch (CoreException e) {
            }
        }
    }

    /**
     * Returns the marker being created or modified.
     * For a new marker, this returns <code>null</code> until
     * the dialog returns, but is non-null after.
     */
    IMarker getMarker() {
        return marker;
    }

    /**
     * Sets the resource to use when creating a new marker.
     * If not set, the new marker is created on the workspace root.
     */
    public void setResource(IResource resource) {
        this.resource = resource;
    }

    /**
     * Returns the resource to use when creating a new marker,
     * or <code>null</code> if none has been set.
     * If not set, the new marker is created on the workspace root.
     */
    IResource getResource() {
        return resource;
    }

    /**
     * Sets initial attributes to use when creating a new marker.
     * If not set, the new marker is created with default attributes.
     */
    void setInitialAttributes(Map initialAttributes) {
        this.initialAttributes = initialAttributes;
    }

    /**
     * Returns the initial attributes to use when creating a new marker,
     * or <code>null</code> if not set.
     * If not set, the new marker is created with default attributes.
     */
    Map getInitialAttributes() {
        if (initialAttributes == null) {
            initialAttributes = new HashMap();
        }
        return initialAttributes;
    }

    /**
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (title == null)
            newShell.setText(MarkerMessages.propertiesDialog_title);
        else
            newShell.setText(title);
    }

    /**
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
        } else if (resource == null) {
            resource = ResourcesPlugin.getWorkspace().getRoot();
        }

        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);
        createDescriptionArea(composite);
        if (marker != null) {
            createCreationTimeArea(composite);
        }
        createAttributesArea(composite);
        if (resource != null)
            createResourceArea(composite);
        updateDialogFromMarker();
        updateEnablement();
        return composite;
    }

    /**
     * Method createCreationTimeArea.
     * @param parent
     */
    private void createCreationTimeArea(Composite parent) {
        String creation = MarkerMessages
                .propertiesDialog_creationTime_text;

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
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gridData);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        
        Label label = new Label(composite, SWT.NONE);
        label.setText(MarkerMessages.propertiesDialog_description_text);
        label.setFont(font);
        int style = SWT.SINGLE | SWT.BORDER;
        descriptionText = new Text(composite, style);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint =  convertHorizontalDLUsToPixels(400);
        descriptionText.setLayoutData(gridData);
        descriptionText.setFont(font);

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                markDirty();
            }
        });
    }

    /**
     * This method is intended to be overridden by subclasses. The attributes area is created between
     * the creation time area and the resource area.
     * 
     * @param parent the parent composite
     */
    protected void createAttributesArea(Composite parent) {
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
        resourceLabel.setText(MarkerMessages.propertiesDialog_resource_text);
        resourceLabel.setFont(font);
        resourceText = new Text(composite, SWT.SINGLE | SWT.WRAP
                | SWT.READ_ONLY | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        resourceText.setLayoutData(gridData);
        resourceText.setFont(font);

        Label folderLabel = new Label(composite, SWT.NONE);
        folderLabel.setText(MarkerMessages.propertiesDialog_folder_text);
        folderLabel.setFont(font);
        folderText = new Text(composite, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY
                | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        folderText.setLayoutData(gridData);
        folderText.setFont(font);

        Label locationLabel = new Label(composite, SWT.NONE);
        locationLabel.setText(MarkerMessages.propertiesDialog_location_text);
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
    protected void updateDialogFromMarker() {
        if (marker == null) {
            updateDialogForNewMarker();
            return;
        }
        descriptionText.setText(Util.getProperty(IMarker.MESSAGE, marker));
        if (creationTime != null)
            creationTime.setText(Util.getCreationTime(marker));
        if (resourceText != null)
            resourceText.setText(Util.getResourceName(marker));
        if (folderText != null)
            folderText.setText(Util.getContainerName(marker));
        if (locationText != null) {
            String line = Util.getProperty(IMarker.LINE_NUMBER, marker);
            if (line.equals("")) //$NON-NLS-1$
                locationText.setText(""); //$NON-NLS-1$
            else
                locationText.setText(NLS.bind(MarkerMessages.label_lineNumber, line));
        }

        descriptionText.selectAll();
    }

    /**
     * Updates the dialog from the predefined attributes.
     */
    protected void updateDialogForNewMarker() {
        if (resource != null && resourceText != null && folderText != null) {
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
            if (line != null && line instanceof Integer && locationText != null)
                locationText.setText(
                    NLS.bind(MarkerMessages.label_lineNumber, line));
        }
    }

    /**
     * Method declared on Dialog
     */
    protected void okPressed() {
        if (marker == null || Util.isEditable(marker)) {
            saveChanges();
        }
        super.okPressed();
    }

    /**
     * Sets the dialog's dirty flag to <code>true</code>
     */
    protected void markDirty() {
        dirty = true;
    }

    /**
     * @return
     * <ul>
     * <li><code>true</code> if the dirty flag has been set to true.</li>
     * <li><code>false</code> otherwise.</li>
     * </ul>
     */
    protected boolean isDirty() {
        return dirty;
    }

    /**
     * Saves the changes made in the dialog if needed.
     * Creates a new marker if needed.
     * Updates the existing marker only if there have been changes.
     */
    private void saveChanges() {

        final CoreException[] coreExceptions = new CoreException[1];

        try {
            final Map attrs = getMarkerAttributes();

            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
                    new IRunnableWithProgress() {
                        /* (non-Javadoc)
                         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
                         */
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            try {

                                monitor.beginTask("", 100);//$NON-NLS-1$
                                ResourcesPlugin.getWorkspace().run(
                                        new IWorkspaceRunnable() {
                                            public void run(
                                                    IProgressMonitor monitor)
                                                    throws CoreException {
                                                if (marker == null)
                                                    createMarker(monitor);
                                                if (isDirty())
                                                    updateMarker(monitor, attrs);
                                            }
                                        }, monitor);
                                monitor.done();
                            } catch (CoreException e) {
                                coreExceptions[0] = e;
                            }
                        }

                    });
        } catch (InvocationTargetException e) {
            IDEWorkbenchPlugin.log(e.getMessage(), StatusUtil.newStatus(
                    IStatus.ERROR, e.getMessage(), e));
            return;
        }

        catch (InterruptedException e) {
        }

        if (coreExceptions[0] != null)
            ErrorDialog
                    .openError(
                            getShell(),
                            MarkerMessages.Error, null, coreExceptions[0].getStatus()); 

    }

    /**
     * Creates or updates the marker.  Must be called within a workspace runnable.
     * @param monitor the monitor we report to. 
     * @param attrs the attributes from the dialog
     */
    private void updateMarker(IProgressMonitor monitor, Map attrs)
            throws CoreException {
        // Set the marker attributes from the current dialog field values.
        // Do not use setAttributes(Map) as that overwrites any attributes
        // not covered by the dialog.

        int increment = 50 / attrs.size();

        for (Iterator i = attrs.keySet().iterator(); i.hasNext();) {
            monitor.worked(increment);
            String key = (String) i.next();
            Object val = attrs.get(key);
            marker.setAttribute(key, val);
        }
    }

    /**
     * Returns the marker attributes to save back to the marker, 
     * based on the current dialog fields.
     */
    protected Map getMarkerAttributes() {
        Map attrs;
        if (initialAttributes == null) {
            attrs = initialAttributes;
        } else {
            attrs = new HashMap();
        }
        attrs.put(IMarker.MESSAGE, descriptionText.getText());
        return attrs;
    }

    /**
     * Create the marker and report progress
     * to the monitor.
     * @param monitor
     * @throws a CoreException
     */
    private void createMarker(IProgressMonitor monitor) throws CoreException {
        if (resource == null)
            return;

        monitor.worked(10);
        marker = resource.createMarker(type);
        monitor.worked(40);
    }

    /**
     * Updates widget enablement for the dialog. Should be overridden by subclasses. 
     */
    protected void updateEnablement() {
        descriptionText.setEditable(isEditable());
    }

    /**
     * @return
     * <ul>
     * <li><code>true</code> if the marker is editable or the dialog is creating a new marker.</li>
     * <li><code>false</code> if the marker is not editable.</li>
     * </ul>
     */
    protected boolean isEditable() {
        if (marker == null) {
            return true;
        }
        return Util.isEditable(marker);
    }

    /**
     * Sets the marker type when creating a new marker.
     * 
     * @param type the marker type
     */
    void setType(String type) {
        this.type = type;
    }
}
