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
package org.eclipse.team.internal.ui.dialogs;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * Dialog that will display any mappings that contain resources whose 
 * sync state match the provided filter.
 */
public abstract class MappingSelectionDialog extends DetailsDialog implements IPropertyChangeListener {

    private final ResourceMapping[] mappings;
    private ResourceMapping[] checkedMappings;
    private ResourceMappingSelectionArea mappingArea;
    private ResourceMappingResourceDisplayArea resourceArea;
    private final IResourceMappingResourceFilter filter;

    protected MappingSelectionDialog(Shell parentShell, String dialogTitle, ResourceMapping[] mappings, IResourceMappingResourceFilter filter) {
        super(parentShell, dialogTitle);
        this.mappings = mappings;
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createMainDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected void createMainDialogArea(Composite parent) {     
        if (mappings.length == 1) {
            // There is only one mapping so just ask for a yes/no on including it
            createWrappingLabel(parent, getSingleMappingMessage(mappings[0]));
        } else {
            // Allow the user to choose which mappings to include
            createMappingSelectionArea(parent);
        } 
    }

    /*
     * Create a list that allows the selection of mappings via checkbox
     */
    private void createMappingSelectionArea(Composite parent) {
        Composite composite = createComposite(parent);
        mappingArea = new ResourceMappingSelectionArea(mappings);
        mappingArea.setDescription(getMultipleMappingsMessage());
        mappingArea.addPropertyChangeListener(this);
        mappingArea.createArea(composite);
        // Create a separator between the two sets of buttons
        Label seperator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        seperator.setLayoutData(new GridData (GridData.FILL_HORIZONTAL));
        
        checkedMappings = mappingArea.getCheckedMappings();
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createDropDownDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createDropDownDialogArea(Composite parent) {
        if (resourceArea == null) {
            ResourceMapping selectedMapping = getSelectedMapping();
            resourceArea = new ResourceMappingResourceDisplayArea(selectedMapping, getResourceListMessage(selectedMapping), filter);
        }
        Composite c = createComposite(parent);
        resourceArea.createArea(c);
        return c;
    }

    private ResourceMapping getSelectedMapping() {
        if (mappingArea != null)
            return mappingArea.getSelectedMapping();
        if (mappings.length == 1)
            return mappings[0];
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#updateEnablements()
     */
    protected void updateEnablements() {
        // Can always finish
        setPageComplete(true);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeErrorMessage()
     */
    protected boolean includeErrorMessage() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeOkButton()
     */
    protected boolean includeOkButton() {
        return mappings.length != 1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        if (mappings.length == 1) {
            createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
            createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, false);
        }
        super.createButtonsForButtonBar(parent);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#buttonPressed(int)
     */
    protected void buttonPressed(int id) {
        if (IDialogConstants.YES_ID == id) {
            checkedMappings = mappings;
            super.buttonPressed(IDialogConstants.OK_ID);
        } else if (IDialogConstants.NO_ID == id) {
            checkedMappings = new ResourceMapping[0];
            super.buttonPressed(IDialogConstants.OK_ID);
        } else {
            super.buttonPressed(id);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(ResourceMappingSelectionArea.SELECTED_MAPPING)) {
            if (resourceArea != null) {
                ResourceMapping selectedMapping = getSelectedMapping();
                resourceArea.setMapping(selectedMapping, getResourceListMessage(selectedMapping));
            }
        } else if (event.getProperty().equals(ResourceMappingSelectionArea.CHECKED_MAPPINGS)) {
            checkedMappings = mappingArea.getCheckedMappings();
            updateEnablements();
        }
    }
    
    /**
     * Provide the message that is displayed if there is only a single mapping to be selected.
     * @param mapping the mapping
     * @return the display string
     */
    protected abstract String getSingleMappingMessage(ResourceMapping mapping);
    
    /**
     * Provide the message that is displayed if there are multiple nappings to choose from.
     * @return the diusplay string
     */
    protected abstract String getMultipleMappingsMessage();
    
    /**
     * Return the label to be used in the details area for the list that
     * displays the resources contained in the mapping
     * @param mapping the resource mapping
     * @return the list label
     */
    protected abstract String getResourceListMessage(ResourceMapping mapping);

    /**
     * Return the <code>ResourceMappings</code> that are being displayed 
     * by the dialog.
     * @return the <code>ResourceMappings</code> that are being displayed 
     * by the dialog
     */
    public final ResourceMapping[] getMappings() {
        return mappings;
    }
    
    /**
     * Return the <code>ResourceMappings</code> that were checked
     * by the user.
     * @return the <code>ResourceMappings</code> that were checked
     */
    protected final ResourceMapping[] getCheckedMappings() {
        return checkedMappings;
    }
}
