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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * A simple control provider that will allow the user to toggle on/off the
 * activities bound to categories.
 * 
 * @since 3.0
 */
public class ActivityEnabler {

	private static final int ALL = 2;
	private static final int NONE = 0;
	private static final int SOME = 1;

	private IWorkbenchActivitySupport activitySupport;

	/**
	 * Listener that manages the grey/check state of categories.
	 */
	private ICheckStateListener checkListener = new ICheckStateListener() {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		public void checkStateChanged(CheckStateChangedEvent event) {
			Set checked = new HashSet(Arrays.asList(dualViewer.getCheckedElements()));
			Object element = event.getElement();
			if (element instanceof ICategory) {
				// clicking on a category should enable/disable all activities within it
				dualViewer.setSubtreeChecked(element, event.getChecked());
				// the state of the category is alwas absolute after clicking on it.  Never gray.
				dualViewer.setGrayed(element, false);
			} else {
				// clicking on an activity can potential change the check/gray state of its category.
				CategorizedActivity proxy = (CategorizedActivity) element;
				Object[] children = provider.getChildren(proxy.getCategory());
				int state = NONE;
				int count = 0;
				for (int i = 0; i < children.length; i++) {
					if (checked.contains(children[i])) {
						count++;
					}
				}

				if (count == children.length) {
					state = ALL;
				} else if (count != 0) {
					state = SOME;
				}

				if (state == NONE) {
					checked.remove(proxy.getCategory());
				} else {
					checked.add(proxy.getCategory());
				}

				dualViewer.setGrayed(proxy.getCategory(), state == SOME);
				dualViewer.setCheckedElements(checked.toArray());
			}
		}
	};

	private CheckboxTreeViewer dualViewer;

	/**
	 * The Set of activities that belong to at least one category.
	 */
	private Set managedActivities = new HashSet(7);

	/**
	 * The content provider.
	 */
	private ActivityCategoryContentProvider provider = new ActivityCategoryContentProvider();

	/**
	 * Create a new instance.
	 * 
	 * @param activitySupport the <code>IWorkbenchActivitySupport</code> from 
	 * which to draw the <code>IActivityManager</code>.
	 */
	public ActivityEnabler(IWorkbenchActivitySupport activitySupport) {
		this.activitySupport = activitySupport;
	}

	/**
	 * Create the controls.
	 * 
	 * @param parent the parent in which to create the controls.
	 * @return the composite in which the controls exist.
	 */
	public Control createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, true));

		dualViewer = new CheckboxTreeViewer(mainComposite);
		dualViewer.setSorter(new ViewerSorter());
		dualViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		dualViewer.setLabelProvider(new ActivityCategoryLabelProvider());
		dualViewer.setContentProvider(provider);
		dualViewer.setInput(activitySupport.getActivityManager());
		dualViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		setInitialStates();

		dualViewer.addCheckStateListener(checkListener);

		return mainComposite;
	}

	/**
	 * @param categoryId the id to fetch.
	 * @return return all ids for activities that are in the given in the 
	 * category.
	 */
	private Collection getCategoryActivityIds(String categoryId) {
		ICategory category = activitySupport.getActivityManager().getCategory(categoryId);
		Set activityBindings = category.getCategoryActivityBindings();
		List categoryActivities = new ArrayList(activityBindings.size());
		for (Iterator i = activityBindings.iterator(); i.hasNext();) {
			ICategoryActivityBinding binding = (ICategoryActivityBinding) i.next();
			String activityId = binding.getActivityId();
			categoryActivities.add(activityId);
		}
		return categoryActivities;
	}

	/**
	 * Set the enabled category/activity check/grey states based on initial 
	 * activity enablement.
	 */
	private void setInitialStates() {
		Set enabledActivities = activitySupport.getActivityManager().getEnabledActivityIds();
		Set categories = activitySupport.getActivityManager().getDefinedCategoryIds();
		List checked = new ArrayList(10), grayed = new ArrayList(10);
		for (Iterator i = categories.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			ICategory category = activitySupport.getActivityManager().getCategory(categoryId);

			int state = NONE;
			Collection activities = getCategoryActivityIds(categoryId);
			int foundCount = 0;
			for (Iterator j = activities.iterator(); j.hasNext();) {
				String activityId = (String) j.next();
				managedActivities.add(activityId);
				if (enabledActivities.contains(activityId)) {
					IActivity activity =
						activitySupport.getActivityManager().getActivity(activityId);
					checked.add(new CategorizedActivity(category, activity));
					//add activity proxy
					foundCount++;
				}
			}

			if (foundCount == activities.size()) {
				state = ALL;
			} else if (foundCount > 0) {
				state = SOME;
			}

			if (state == NONE) {
				continue;
			}
			checked.add(category);

			if (state == SOME) {
				grayed.add(category);
			}
		}

		dualViewer.setCheckedElements(checked.toArray());
		dualViewer.setGrayedElements(grayed.toArray());
	}

	/**
	 * Update activity enablement based on the check states of activities in the
	 * tree. 
	 */
	public void updateActivityStates() {
		Set enabledActivities =
			new HashSet(activitySupport.getActivityManager().getEnabledActivityIds());

		// remove all but the unmanaged activities (if any).
		enabledActivities.removeAll(managedActivities);

		Object[] checked = dualViewer.getCheckedElements();
		for (int i = 0; i < checked.length; i++) {
			Object element = checked[i];
			if (element instanceof ICategory || dualViewer.getGrayed(element)) {
				continue;
			}
			enabledActivities.add(((IActivity) element).getId());
		}

		activitySupport.setEnabledActivityIds(enabledActivities);
	}
}
