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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.internal.roles.ui.RoleContentProvider;
import org.eclipse.ui.internal.roles.ui.RoleLabelProvider;
import org.eclipse.ui.roles.IActivityBinding;
import org.eclipse.ui.roles.IRole;
import org.eclipse.ui.roles.IRoleManager;

/**
 * A simple control provider that will allow the user to toggle on/off the
 * activities bound to roles.
 * 
 * @since 3.0
 */
public class ActivityEnabler {
	private ListViewer activitiesViewer;
	private IMutableActivityManager activityManager;

	private CheckboxTableViewer categoryViewer;
	private Set checkedInSession = new HashSet(7),
		uncheckedInSession = new HashSet(7);
	private IRoleManager roleManager;

	/**
	 * Create a new instance.
	 * 
	 * @param activityManager
	 *            the activity manager that will be used.
	 * @param roleManager
	 *            the role manager that will be used.
	 */
	public ActivityEnabler(
		IMutableActivityManager activityManager,
		IRoleManager roleManager) {
		this.activityManager = activityManager;
		this.roleManager = roleManager;
	}

	/**
	 * @param categoryId
	 *            the id to check.
	 * @return whether all activities in the category are enabled.
	 */
	private boolean categoryEnabled(String categoryId) {
		Collection roleActivities = getCategoryActivities(categoryId);
		Set enabledActivities = activityManager.getEnabledActivityIds();
		return enabledActivities.containsAll(roleActivities);
	}

	/**
	 * Create the controls.
	 * 
	 * @param parent
	 *            the parent in which to create the controls.
	 * @return the composite in which the controls exist.
	 */
	public Control createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, true));

		Label label = new Label(mainComposite, SWT.NONE);
		label.setText(ActivityMessages.getString("ActivityEnabler.categories")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(mainComposite, SWT.NONE);
		label.setText(ActivityMessages.getString("ActivityEnabler.activities")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		{
			categoryViewer = new CheckboxTableViewer(mainComposite);
			categoryViewer.getControl().setLayoutData(
				new GridData(GridData.FILL_BOTH));
			categoryViewer.setContentProvider(new RoleContentProvider());
			categoryViewer.setLabelProvider(new RoleLabelProvider(roleManager));
			categoryViewer.setSorter(new ViewerSorter());
			categoryViewer.setInput(roleManager);
			categoryViewer.setSelection(new StructuredSelection());
			setCategoryStates();
		}

		{
			activitiesViewer = new ListViewer(mainComposite);
			activitiesViewer.getControl().setLayoutData(
				new GridData(GridData.FILL_BOTH));
			activitiesViewer.setContentProvider(new ActivityContentProvider());
			activitiesViewer.setLabelProvider(
				new ActivityLabelProvider(activityManager));
			activitiesViewer.setSorter(new ViewerSorter());
			activitiesViewer.setInput(Collections.EMPTY_SET);
		}

		categoryViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection =
					(IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					String roleId = (String) selection.getFirstElement();
					activitiesViewer.setInput(getCategoryActivities(roleId));
				}
			}
		});

		categoryViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				if (event.getChecked()) {
					if (!uncheckedInSession.remove(element)) {
						checkedInSession.add(element);
					}
				} else {
					if (!checkedInSession.remove(element)) {
						uncheckedInSession.add(element);
					}
				}
			}
		});

		return mainComposite;
	}

	/**
	 * @param categoryId
	 *            the id to fetch.
	 * @return all activity ids in the category.
	 */
	private Collection getCategoryActivities(String categoryId) {
		IRole category = roleManager.getRole(categoryId);
		Set activityBindings = category.getActivityBindings();
		List roleActivities = new ArrayList(10);
		for (Iterator j = activityBindings.iterator(); j.hasNext();) {
			IActivityBinding binding = (IActivityBinding) j.next();
			String activityId = binding.getActivityId();
			roleActivities.add(activityId);
		}
		return roleActivities;
	}

	/**
	 * Set the enabled category states based on current activity enablement.
	 */
	private void setCategoryStates() {
		Set roles = roleManager.getDefinedRoleIds();
		List enabledRoles = new ArrayList(10);
		for (Iterator i = roles.iterator(); i.hasNext();) {
			String roleId = (String) i.next();
			if (categoryEnabled(roleId)) {
				enabledRoles.add(roleId);
			}
		}
		categoryViewer.setCheckedElements(enabledRoles.toArray());
	}

	/**
	 * Update activity enablement based on the check/uncheck actions of the
	 * user in this session. First, any activities that are bound to unchecked
	 * categories are applied and then those that were checked.
	 */
	public void updateActivityStates() {
		Set enabledActivities =
			new HashSet(activityManager.getEnabledActivityIds());

		for (Iterator i = uncheckedInSession.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			enabledActivities.removeAll(getCategoryActivities(categoryId));
		}

		for (Iterator i = checkedInSession.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			enabledActivities.addAll(getCategoryActivities(categoryId));
		}

		activityManager.setEnabledActivityIds(enabledActivities);
	}
}
