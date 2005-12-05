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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.mapping.*;

public class AdditionalMappingsDialog extends DetailsDialog {

    private ResourceMappingHierarchyArea selectedMappingsArea;
    private ResourceMappingHierarchyArea allMappingsArea;
	private final IResourceMappingScope scope;
	private final ISynchronizationContext context;

    public AdditionalMappingsDialog(Shell parentShell, String dialogTitle, IResourceMappingScope scope, ISynchronizationContext context) {
        super(parentShell, dialogTitle);
		this.scope = scope;
		this.context = context;
    }

	protected void createMainDialogArea(Composite parent) {
        createWrappingLabel(parent, "Additional elements must be included in the current operation due to the relationship between the selected elements and the files in which they are stored.");
        createSelectedMappingsArea(parent);
        createAllMappingsArea(parent);
    }
    
    /*
     * Create a list that allows the selection of mappings via checkbox
     */
    private void createSelectedMappingsArea(Composite parent) {
        Composite composite = createComposite(parent);
        selectedMappingsArea = ResourceMappingHierarchyArea.create(new ScopeGenerator().asInputScope(scope), null /* no context */);
        selectedMappingsArea.setDescription("These are the elements you selected");
        selectedMappingsArea.createArea(composite);
        // Create a separator between the two sets of buttons
        Label seperator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        seperator.setLayoutData(new GridData (GridData.FILL_HORIZONTAL));
    }
    
    /*
     * Create a list that allows the selection of mappings via checkbox
     */
    private void createAllMappingsArea(Composite parent) {
        Composite composite = createComposite(parent);
        allMappingsArea = ResourceMappingHierarchyArea.create(scope, context);
        allMappingsArea.setDescription("These are the elements that will be included in the operation");
        //allMappingsArea.addPropertyChangeListener(this);
        allMappingsArea.createArea(composite);
    }

    protected Composite createDropDownDialogArea(Composite parent) {
        // TODO Auto-generated method stub
        return null;
    }

    protected void updateEnablements() {
        // TODO Auto-generated method stub

    }
    
    protected boolean includeDetailsButton() {
        return false;
    }

}
