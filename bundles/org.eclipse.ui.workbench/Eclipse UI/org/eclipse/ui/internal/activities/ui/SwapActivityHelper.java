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
package org.eclipse.ui.internal.activities.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.roles.IActivityBinding;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleManager;

/**
 * Utility class that will create controls (two lists and swap buttons) for
 * managing IActivity objects in the system manager as well as methods to
 * update the state of currently enabled IActivity objects based on the
 * contents of the lists. This control will only display IActivity objects that
 * are bound to some IRole object in the system manager.
 * 
 * @since 3.0
 */
public class SwapActivityHelper {

    private class SwapSelectionListener implements SelectionListener {

        private ListViewer sourceViewer, destinationViewer;

        /**
		 * @param sourceViewer
		 *            the source viewer.
		 * @param destinationViewer
		 *            the destination viewer.
		 * @since 3.0
		 */
        public SwapSelectionListener(ListViewer sourceViewer, ListViewer destinationViewer) {
            this.sourceViewer = sourceViewer;
            this.destinationViewer = destinationViewer;
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
        public void widgetDefaultSelected(SelectionEvent e) {
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
        public void widgetSelected(SelectionEvent e) {
            List selection = ((IStructuredSelection) sourceViewer.getSelection()).toList();
            if (selection.size() > 0) {
                Collection sourceInput = (Collection) sourceViewer.getInput();
                Collection destInput = (Collection) destinationViewer.getInput();
                sourceInput.removeAll(selection);
                destInput.addAll(selection);
                sourceViewer.refresh();
                destinationViewer.refresh();
            }
        }
    }

    private ListViewer activeViewer, potentialViewer;
    private Composite mainComposite;

    /**
	 * Create a swap button.
	 * 
	 * @param parent
	 *            the parent control.
	 * @param source
	 *            the ListViewer to copy selections from.
	 * @param destination
	 *            the ListViewer to copy selections to.
	 * @param leftToRight
	 *            whether the arrow on the Button should face to the right.
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
	 *            the parent control.
	 * @since 3.0
	 */
    public void createControl(Composite parent) {
        mainComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        mainComposite.setLayout(layout);

        potentialViewer = createViewer(ActivityMessages.getString("SwapActivityHelper.disabled_activities")); //$NON-NLS-1$

        Composite swapComposite = new Composite(mainComposite, SWT.NONE);

        activeViewer = createViewer(ActivityMessages.getString("SwapActivityHelper.enabled_activities")); //$NON-NLS-1$

        createSwapButtons(swapComposite);

        IActivityManager activityManager = PlatformUI.getWorkbench().getActivityManager();
        Set activityIds = activityManager.getDefinedActivityIds();

        List active = new ArrayList(), potential = new ArrayList();
        for (Iterator i = activityIds.iterator(); i.hasNext();) {
            IActivity activity = activityManager.getActivity((String) i.next());
            if (belongsToARole(activity.getId())) {
                if (activity.isEnabled()) {
                    active.add(activity);
                }
                else {
                    potential.add(activity);
                }
            }
        }

        potentialViewer.setInput(potential);
        activeViewer.setInput(active);
    }

    /**
	 * Answers whether the given activity id is bound to a role.
	 * 
	 * @param activityId
	 *            the activity id to test.
	 * @return whether the given activity is bound to a role.
	 * @since 3.0
	 */
    private boolean belongsToARole(String activityId) {
        IRoleManager roleManager = PlatformUI.getWorkbench().getRoleManager();
        for (Iterator roleItr = roleManager.getDefinedRoleIds().iterator(); roleItr.hasNext();) {
            IRole role = roleManager.getRole((String) roleItr.next());
            for (Iterator bindingItr = role.getActivityBindings().iterator(); bindingItr.hasNext();) {
                IActivityBinding binding = (IActivityBinding) bindingItr.next();
                if (binding.getActivityId().equals(activityId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
	 * @param parent
	 *            the middle section of the main composite area.
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
	 * @param label
	 *            the label to give to the viewer.
	 * @return @since 3.0
	 */
    private ListViewer createViewer(String label) {
        Group group = new Group(mainComposite, SWT.NONE);
        group.setText(label);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 200;
        group.setLayoutData(data);
        group.setLayout(new FillLayout());
        ListViewer viewer = new ListViewer(group);
        viewer.setLabelProvider(new ActivityLabelProvider());
        viewer.setContentProvider(new ActivityContentProvider());
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
    	// TODO cast
    	IMutableActivityManager activityManager = (IMutableActivityManager) PlatformUI.getWorkbench().getActivityManager();
    	
        Set finalState = new HashSet(activityManager.getEnabledActivityIds());

        Collection disabledActivities = (Collection) potentialViewer.getInput();
        for (Iterator i = disabledActivities.iterator(); i.hasNext();) {
            finalState.remove(((IActivity) i.next()).getId());
        }

        Collection enabledActivities = (Collection) activeViewer.getInput();
        for (Iterator i = enabledActivities.iterator(); i.hasNext();) {
            finalState.add(((IActivity) i.next()).getId());
        }

        activityManager.setEnabledActivityIds(finalState);
    }
}
