/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Utility class that will create a TableViewer for the purpose of 
 * managing Activities as well as methods to update the state of currently
 * enabled Activities based on the contents of the TableViewer.
 * 
 * @since 3.0
 */
public class CheckboxActivityHelper {

    private CheckboxTableViewer viewer;

    /**
     * Create the Table control.
     * 
     * @param parent
     * @since 3.0
     */
    public void createControl(Composite parent) {
        final RoleManager manager = RoleManager.getInstance();
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout(1, true));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalSpan = 2;
        viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
        viewer.setSorter(new ViewerSorter());
        viewer.setContentProvider(new ActivityContentProvider());
        viewer.setLabelProvider(new ActivityLabelProvider());
        viewer.setInput(manager);
        viewer.getControl().setLayoutData(gd);

        loadActivityStates();        
    }
    /**
     * @return the viewer Control
     * @since 3.0
     */
    public Control getControl() {
        return viewer.getControl();
    }
    
    /**
     * Sets the state of the viewer based on the currently enabled roles.
     * 
     * @since 3.0
     */
    private void loadActivityStates() {
        Collection activities = RoleManager.getInstance().getActivities();
        for (Iterator i = activities.iterator(); i.hasNext();) {
            Activity activity = (Activity) i.next();
            viewer.setChecked(activity, activity.isEnabled());
        }
    }    
    
    /**
     * Updates the Activity enablement states based on the contents of the 
     * viewer.
     * 
     * @since 3.0
     */
    public void updateActivityStates() {
        List toEnable = new ArrayList(), toDisable = new ArrayList();

        for (Iterator i = RoleManager.getInstance().getActivities().iterator(); i.hasNext(); ) {
            Activity activity = (Activity) i.next();
            if (viewer.getChecked(activity)) {
                toEnable.add(activity);
            }
            else {
                toDisable.add(activity);                            
            }
        }
                
        Activity [] toEnableArray = new Activity[toEnable.size()],
                    toDisableArray = new Activity[toDisable.size()];
                
        toEnable.toArray(toEnableArray);
        toDisable.toArray(toDisableArray);
        RoleManager.getInstance().setEnabled(toDisableArray, false);
        RoleManager.getInstance().setEnabled(toEnableArray, true);        
    }
}
