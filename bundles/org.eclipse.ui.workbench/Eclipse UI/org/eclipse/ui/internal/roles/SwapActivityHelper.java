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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Utility class that will create controls (two lists and swap buttons) for 
 * managing Activities as well as methods to update the state of currently
 * enabled Activities based on the contents of the lists.
 * 
 * 
 * TODO: this currently keys on Roles and not activities.
 * @since 3.0
 */
public class SwapActivityHelper {

    private class SwapSelectionListener implements SelectionListener {

        private ListViewer sourceViewer, destinationViewer;

        /**
         * @param sourceViewer
         * @param destinationViewer
         * @since 3.0
         */
        public SwapSelectionListener(ListViewer sourceViewer, ListViewer destinationViewer) {
            this.sourceViewer = sourceViewer;
            this.destinationViewer = destinationViewer; 
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetDefaultSelected(SelectionEvent e) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e) {
            List selection = ((IStructuredSelection)sourceViewer.getSelection()).toList();
            if (selection.size() > 0) {
                Collection sourceInput = (Collection)sourceViewer.getInput();
                Collection destInput = (Collection)destinationViewer.getInput();
                sourceInput.removeAll(selection);
                destInput.addAll(selection);
                sourceViewer.refresh();
                destinationViewer.refresh();
            }
        }
    }
    
    private ListViewer activeViewer, potentialViewer;
    private Composite mainComposite;
    private RoleSystemEnablementHelper roleHelper;
    
    /**
     * Create a swap button.
     *  
     * @param parent
     * @param source the ListViewer to copy selections from
     * @param destination the ListViewer to copy selections to
     * @param leftToRight whether the arrow on the Button should face to the right
     * @since 3.0
     */
    private void createButton(Composite parent, ListViewer source, ListViewer destination, boolean leftToRight) {
        Button button = new Button(parent, SWT.ARROW | (leftToRight ? SWT.RIGHT : SWT.LEFT));
        button.setLayoutData(new GridData());
        button.addSelectionListener(new SwapSelectionListener(source, destination));        
    }

    /**
     * Create the List controls and the Buttons.
     * 
     * @param parent
     * @since 3.0
     */
    public void createControl(Composite parent) {
        mainComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        mainComposite.setLayout(layout);
        
        roleHelper = new RoleSystemEnablementHelper();
        roleHelper.createControl(mainComposite);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        roleHelper.getControl().setLayoutData(data);
        
        //Collection activities = RoleManager.getInstance().getActivities();
        Role [] roles = RoleManager.getInstance().getRoles();
        
        potentialViewer = createViewer(RoleMessages.getString("SwapActivityHelper.disabled_activities")); //$NON-NLS-1$
        
        Composite swapComposite = new Composite(mainComposite, SWT.NONE);
        
        activeViewer = createViewer(RoleMessages.getString("SwapActivityHelper.enabled_activities")); //$NON-NLS-1$
        
        createSwapButtons(swapComposite);
        
        List active = new ArrayList(), potential = new ArrayList();
        //for (Iterator i = activities.iterator(); i.hasNext();) {
        for (int i = 0; i < roles.length; i++) {
            Role role = roles[i];
            if (role.allEnabled()) {
                active.add(role);
            }
            else {
                potential.add(role);
            }
//            Activity activity = (Activity) i.next();
//            if (activity.isEnabled()) {
//                active.add(activity);
//            }
//            else {
//                potential.add(activity);
//            }
        }
        
        potentialViewer.setInput(potential);
        activeViewer.setInput(active);
    }

    /**
     * @param parent the middle section of the main composite area.  
     * 
     * @since 3.0
     */
    private void createSwapButtons(Composite parent) {
        GridLayout layout = new GridLayout();
        parent.setLayout(layout);

        // create the left->right button        
        createButton(parent, potentialViewer, activeViewer, true);
        
        // create the right->left button
        createButton(parent, activeViewer, potentialViewer, false);
        
        parent.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
    }
    
    /**
     * Create a ListViewer with the given Label (as provided by a Group box).
     * 
     * @param parent
     * @param label
     * @return
     * @since 3.0
     */
    private ListViewer createViewer(String label) {
        Group group = new Group(mainComposite, SWT.NONE);
        group.setText(label);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 200;
        group.setLayoutData(data);        
        group.setLayout(new FillLayout());
        ListViewer viewer = new ListViewer(group);        
//        viewer.setLabelProvider(new ActivityLabelProvider());
//        viewer.setContentProvider(new ActivityContentProvider());
        viewer.setLabelProvider(new RoleLabelProvider());
        viewer.setContentProvider(new RoleContentProvider());
        viewer.setSorter(new ViewerSorter());
        return viewer;
    }

    /**
     * @return the Composite containing the Lists and Buttons
     * 
     * @since 3.0
     */
    public Composite getControl() {
        return mainComposite;
    }

    /**
     * Updates the Activity enablement states based on the contents of the 
     * Lists.
     * 
     * @since 3.0
     */
    public void updateActivityStates() {
        updateFromListViewer(activeViewer, true);
        updateFromListViewer(potentialViewer, false);
        roleHelper.updateRoleState();        
    }
    
    /**
     * Update all Activity objects in the viewer with the given enablement 
     * state.
     * 
     * @param viewer
     * @param enabled
     * @since 3.0
     */
    private void updateFromListViewer(ListViewer viewer, boolean enabled) {
        Collection roles = (Collection)viewer.getInput();
        for (Iterator i = roles.iterator(); i.hasNext();) {
            Role role = (Role) i.next();
            role.setEnabled(enabled);    
        }
        
//        Collection activities = (Collection)viewer.getInput();
//        RoleManager.getInstance().setEnabled((Activity []) activities.toArray(new Activity[activities.size()]), enabled);                
    }
}
