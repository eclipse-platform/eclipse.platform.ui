/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.dialogs;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.model.*;

/**
 * Dialog area that displays a checkbox list of mappings.
 */
public class ResourceMappingSelectionArea extends DialogArea {

    /**
     * Property constant used to indicate that the selected mapping has changed.
     * The object associated with the property is a <code>ResourceMapping</code>.
     */
    public static final String SELECTED_MAPPING = "SelectedMapping"; //$NON-NLS-1$
    
    /**
     * Property constant used to indicate that the checked mappings have changed.
     * The object associated with the property is a <code>ResourceMapping[]</code>
     * (i.e. an array of mappings).
     */
    public static final String CHECKED_MAPPINGS = "CheckedMappings"; //$NON-NLS-1$
    
    private ResourceMapping[] mappings;
    private CheckboxTableViewer viewer;
    private ResourceMapping[] checkedMappings;
    private ResourceMapping selectedMapping;
    private String description;
    
    public ResourceMappingSelectionArea(ResourceMapping[] mappings) {
        this.mappings = mappings;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.dialogs.DialogArea#createArea(org.eclipse.swt.widgets.Composite)
     */
    public void createArea(Composite parent) {
        Composite composite = createComposite(parent, 1, true);
        
        if (description != null)
            createWrappingLabel(composite, description, 1);
        
        viewer = CheckboxTableViewer.newCheckList(composite, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 100;
        data.widthHint = 200;
        viewer.getControl().setLayoutData(data);
        viewer.setContentProvider(new BaseWorkbenchContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setInput(new AdaptableList(mappings));
        viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                ResourceMapping[] oldMappings = checkedMappings;
                checkedMappings = internalGetCheckedMappings();
                if (oldMappings != checkedMappings)
                    firePropertyChangeChange(CHECKED_MAPPINGS, oldMappings, checkedMappings);
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ResourceMapping oldSelection = selectedMapping;
                selectedMapping = internalGetSelectedMapping();
                if (oldSelection != selectedMapping)
                    firePropertyChangeChange(SELECTED_MAPPING, oldSelection, selectedMapping);
            }
        });
        viewer.setCheckedElements(mappings);
        checkedMappings = mappings;

        Composite buttons = createEmbeddedButtonComposite(composite);
        
        Button selectAll = new Button(buttons, SWT.PUSH);
        selectAll.setText(Policy.bind("ResourceMappingSelectionArea.0")); //$NON-NLS-1$
        selectAll.setLayoutData(new GridData(GridData.FILL_BOTH));
        selectAll.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
            }
        });
        
        Button deselectAll = new Button(buttons, SWT.PUSH);
        deselectAll.setText(Policy.bind("ResourceMappingSelectionArea.1")); //$NON-NLS-1$
        deselectAll.setLayoutData(new GridData(GridData.FILL_BOTH));
        deselectAll.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
            }
        });
    }

    /* private */ ResourceMapping[] internalGetCheckedMappings() {
        Object[] checked = viewer.getCheckedElements();
        ResourceMapping[] mappings = new ResourceMapping[checked.length];
        for (int i = 0; i < checked.length; i++) {
            Object object = checked[i];
            mappings[i] = (ResourceMapping)object;
        }
        return mappings;
    }
    
    private Composite createEmbeddedButtonComposite(Composite composite) {
        GridData data;
        Composite buttons = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        buttons.setLayout(layout);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END
                | GridData.VERTICAL_ALIGN_CENTER);
        buttons.setLayoutData(data);
        return buttons;
    }
    
    /* private */ ResourceMapping internalGetSelectedMapping() {
        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object firstElement = ss.getFirstElement();
            if (firstElement instanceof ResourceMapping)
                return (ResourceMapping)firstElement;
        }
        return null;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    public ResourceMapping[] getCheckedMappings() {
        return checkedMappings;
    }
    public ResourceMapping getSelectedMapping() {
        return selectedMapping;
    }
}
