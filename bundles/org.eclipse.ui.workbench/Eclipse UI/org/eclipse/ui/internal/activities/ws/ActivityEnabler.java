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
package org.eclipse.ui.internal.activities.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * A simple control provider that will allow the user to toggle on/off the
 * activities bound to categories.
 * 
 * @since 3.0
 */
public class ActivityEnabler {
	private ListViewer activitiesViewer;
	private IWorkbenchActivitySupport activitySupport;

	private CheckboxTableViewer categoryViewer;
	private Set checkedInSession = new HashSet(7),
		uncheckedInSession = new HashSet(7);

	private String lastCategory = null;

	/**
	 * Create a new instance.
	 * 
	 * @param activityManager the activity manager that will be used.
	 */
	public ActivityEnabler(IWorkbenchActivitySupport activityManager) {
		this.activitySupport = activityManager;
	}

	/**
	 * @param categoryId the id to check.
	 * @return whether all activities in the category are enabled.
	 */
	private boolean categoryEnabled(String categoryId) {
		Collection categoryActivities = getCategoryActivities(categoryId);
		Set enabledActivities =
			activitySupport.getActivityManager().getEnabledActivityIds();
		return enabledActivities.containsAll(categoryActivities);
	}

	/**
	 * Create the controls.
	 * 
	 * @param parent the parent in which to create the controls.
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
			categoryViewer =
				CheckboxTableViewer.newCheckList(mainComposite, SWT.BORDER);
			categoryViewer.getControl().setLayoutData(
				new GridData(GridData.FILL_BOTH));
			categoryViewer.setContentProvider(new CategoryContentProvider());
			categoryViewer.setLabelProvider(
				new CategoryLabelProvider(
					activitySupport.getActivityManager()));
			categoryViewer.setSorter(new ViewerSorter());
			categoryViewer.setInput(activitySupport);
			categoryViewer.setSelection(new StructuredSelection());
			setCategoryStates();
		}

		{
			activitiesViewer = new ListViewer(mainComposite);
			activitiesViewer.getControl().setLayoutData(
				new GridData(GridData.FILL_BOTH));
			activitiesViewer.setContentProvider(new ActivityContentProvider());
			activitiesViewer.setLabelProvider(
				new ActivityLabelProvider(
					activitySupport.getActivityManager()));
			activitiesViewer.setSorter(new ViewerSorter());
			activitiesViewer.setInput(Collections.EMPTY_SET);
			activitiesViewer.getControl().setEnabled(false);
			// read only control
		}

		categoryViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection =
					(IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					String categoryId = (String) selection.getFirstElement();
					// don't reset the input unless we're a differnet category
					if (!categoryId.equals(lastCategory)) {
						lastCategory = categoryId;
						activitiesViewer.setInput(
							getCategoryActivities(categoryId));
					}
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
		// default select the first category so the right pane will not be
		// empty
		Object firstElement = categoryViewer.getElementAt(0);
		if (firstElement != null) {
			categoryViewer.setSelection(
				new StructuredSelection(firstElement),
				true);
		}

		return mainComposite;
	}

	/**
	 * @param categoryId the id to fetch.
	 * @return all activity ids in the category.
	 */
	private Collection getCategoryActivities(String categoryId) {
		ICategory category =
			activitySupport.getActivityManager().getCategory(categoryId);
		Set activityBindings = category.getCategoryActivityBindings();
		List categoryActivities = new ArrayList(10);
		for (Iterator j = activityBindings.iterator(); j.hasNext();) {
			ICategoryActivityBinding binding =
				(ICategoryActivityBinding) j.next();
			String activityId = binding.getActivityId();
			categoryActivities.add(activityId);
		}
		return categoryActivities;
	}

	/**
	 * Set the enabled category states based on current activity enablement.
	 */
	private void setCategoryStates() {
		Set categories =
			activitySupport.getActivityManager().getDefinedCategoryIds();
		List enabledCategories = new ArrayList(10);
		for (Iterator i = categories.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			if (categoryEnabled(categoryId)) {
				enabledCategories.add(categoryId);
			}
		}
		categoryViewer.setCheckedElements(enabledCategories.toArray());
	}

	/**
	 * Update activity enablement based on the check/uncheck actions of the
	 * user in this session. First, any activities that are bound to unchecked
	 * categories are applied and then those that were checked.
	 */
	public void updateActivityStates() {
		Set enabledActivities =
			new HashSet(
				activitySupport.getActivityManager().getEnabledActivityIds());

		for (Iterator i = uncheckedInSession.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			enabledActivities.removeAll(getCategoryActivities(categoryId));
		}

		for (Iterator i = checkedInSession.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			enabledActivities.addAll(getCategoryActivities(categoryId));
		}

		activitySupport.setEnabledActivityIds(enabledActivities);
	}
}
