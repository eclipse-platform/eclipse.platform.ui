/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.Properties;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.activities.ActivitiesPreferencePage;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.NotDefinedException;

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

	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			Object element = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			try {
				if (element instanceof ICategory) {
					descriptionText.setText(((ICategory) element)
							.getDescription());
				} else if (element instanceof IActivity) {
					descriptionText.setText(((IActivity) element)
							.getDescription());
				}
			} catch (NotDefinedException e) {
				descriptionText.setText(""); //$NON-NLS-1$
			}
		}
	};

	/**
	 * Listener that manages the grey/check state of categories.
	 */
	private ICheckStateListener checkListener = new ICheckStateListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		public void checkStateChanged(CheckStateChangedEvent event) {
			Set checked = new HashSet(Arrays.asList(dualViewer
					.getCheckedElements()));
			Object element = event.getElement();
			if (element instanceof ICategory) {
				// clicking on a category should enable/disable all activities
				// within it
				dualViewer.setSubtreeChecked(element, event.getChecked());
				// the state of the category is always absolute after clicking
				// on it. Never gray.
				dualViewer.setGrayed(element, false);
				Object categoryActivities[] = provider.getChildren(element);
				// Update the category's activities for multiplicity in other
				// categories
				for (int index = 0; index < categoryActivities.length; index++) {
					handleDuplicateActivities(event.getChecked(),
							categoryActivities[index]);
				}

			} else {
				// Activity checked
				handleActivityCheck(checked, element);
				handleDuplicateActivities(event.getChecked(), element);
			}
		}

		/**
		 * Handle duplicate activities.
		 * 
		 * @param checkedState
		 *            Checked state of the element.
		 * @param element
		 *            The checked element.
		 */
		private void handleDuplicateActivities(boolean checkedState,
				Object element) {
			// Retrieve duplicate activities from the other categories
			Object[] duplicateActivities = provider
					.getDuplicateCategoryActivities((CategorizedActivity) element);
			CategorizedActivity activity = null;
			for (int index = 0; index < duplicateActivities.length; index++) {
				activity = (CategorizedActivity) duplicateActivities[index];
				// Update the duplicate activity with the same state as the
				// original
				dualViewer.setChecked(activity, checkedState);
				// handle the activity check to potentially update its
				// category's enablement
				handleActivityCheck(new HashSet(Arrays.asList(dualViewer
						.getCheckedElements())), activity);
			}
		}

		/**
		 * Handle the checking of an activity and update its category's checked
		 * state.
		 * 
		 * @param checked
		 *            The set of checked elements in the viewer.
		 * @param element
		 *            The checked element.
		 */
		private void handleActivityCheck(Set checked, Object element) {
			// clicking on an activity can potentially change the check/gray
			// state of its category.
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
			// Check child required activities and uncheck parent required activities
			// if needed
			handleRequiredActivities(checked, element);
		}

		/**
		 * Handle the activity's required activities (parent and child).
		 * 
		 * @param checked
		 *            The set of checked elements in the viewer.
		 * @param element
		 *            The checked element.
		 *  
		 */
		private void handleRequiredActivities(Set checked, Object element) {
			Object[] requiredActivities = null;
			// An element has been checked - we want to check its child required
			// activities
			if (checked.contains(element)) {
				requiredActivities = provider
						.getChildRequiredActivities(((CategorizedActivity) element)
								.getId());
				for (int index = 0; index < requiredActivities.length; index++) {
					// We want to check the element if it is unchecked
					if (!checked.contains(requiredActivities[index])) {
						dualViewer.setChecked(requiredActivities[index], true);
						handleActivityCheck(new HashSet(Arrays
								.asList(dualViewer.getCheckedElements())),
								requiredActivities[index]);
					}
				}
			}
			// An element has been unchecked - we want to uncheck its parent
			// required activities
			else {
				requiredActivities = provider
						.getParentRequiredActivities(((CategorizedActivity) element)
								.getId());
				for (int index = 0; index < requiredActivities.length; index++) {
					// We want to uncheck the element if it is checked
					if (checked.contains(requiredActivities[index])) {
						dualViewer.setChecked(requiredActivities[index], false);
						handleActivityCheck(new HashSet(Arrays
								.asList(dualViewer.getCheckedElements())),
								requiredActivities[index]);
					}
				}
			}
		}

	};

	protected CheckboxTreeViewer dualViewer;

	/**
	 * The Set of activities that belong to at least one category.
	 */
	private Set managedActivities = new HashSet(7);

	/**
	 * The content provider.
	 */
	protected ActivityCategoryContentProvider provider = new ActivityCategoryContentProvider();

	/**
	 * The descriptive text.
	 */
	protected Text descriptionText;

	private Properties strings;

    private IMutableActivityManager activitySupport;

	/**
	 * Create a new instance.
	 * 
	 * @param activitySupport
	 *            the <code>IMutableActivityMananger</code> to use.
	 * @param strings
	 *            map of strings to use. See the constants on
	 *            {@link org.eclipse.ui.activities.ActivitiesPreferencePage} for
	 *            details.
	 */
	public ActivityEnabler(IMutableActivityManager activitySupport, Properties strings) {
		this.activitySupport = activitySupport;
		this.strings = strings;
	}

	/**
	 * Create the controls.
	 * 
	 * @param parent
	 *            the parent in which to create the controls.
	 * @return the composite in which the controls exist.
	 */
	public Control createControl(Composite parent) {
        GC gc = new GC(parent);
        gc.setFont(JFaceResources.getDialogFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayoutWithoutMargins(1, fontMetrics));

		new Label(composite, SWT.NONE).setText(strings.getProperty(ActivitiesPreferencePage.ACTIVITY_NAME, ActivityMessages.ActivityEnabler_activities) + ':');

		dualViewer = new CheckboxTreeViewer(composite);
		dualViewer.setComparator(new ViewerComparator());
		dualViewer.setLabelProvider(new ActivityCategoryLabelProvider());
		dualViewer.setContentProvider(provider);
		dualViewer.setInput(activitySupport);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		dualViewer.getControl().setLayoutData(data);

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(createGridLayoutWithoutMargins(2, fontMetrics));

		Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
		selectAllButton.setText(ActivityMessages.ActivityEnabler_selectAll);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				toggleTreeEnablement(true);
			}
		});
		setButtonLayoutData(selectAllButton, fontMetrics);

		Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		deselectAllButton.setText(ActivityMessages.ActivityEnabler_deselectAll); 
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				toggleTreeEnablement(false);
			}
		});
		setButtonLayoutData(deselectAllButton, fontMetrics);

		new Label(composite, SWT.NONE).setText(ActivityMessages.ActivityEnabler_description);

		descriptionText = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER
				| SWT.V_SCROLL);
		data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 5);
		descriptionText.setLayoutData(data);
		setInitialStates();

		dualViewer.addCheckStateListener(checkListener);
		dualViewer.addSelectionChangedListener(selectionListener);

		dualViewer.setSelection(new StructuredSelection());

        Dialog.applyDialogFont(composite);
        
		return composite;
	}

	private GridLayout createGridLayoutWithoutMargins(int nColumns, FontMetrics fontMetrics) {
		GridLayout layout = new GridLayout(nColumns, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_SPACING);
		return layout;
	}

    private GridData setButtonLayoutData(Button button, FontMetrics fontMetrics) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
        return data;
    }
    
	/**
	 * @param categoryId
	 *            the id to fetch.
	 * @return return all ids for activities that are in the given in the
	 *         category.
	 */
	private Collection getCategoryActivityIds(String categoryId) {
		ICategory category = activitySupport.getCategory(
				categoryId);
		Set activityBindings = category.getCategoryActivityBindings();
		List categoryActivities = new ArrayList(activityBindings.size());
		for (Iterator i = activityBindings.iterator(); i.hasNext();) {
			ICategoryActivityBinding binding = (ICategoryActivityBinding) i
					.next();
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
		Set enabledActivities = activitySupport
				.getEnabledActivityIds();
		setEnabledStates(enabledActivities);
	}

	private void setEnabledStates(Set enabledActivities) {
		Set categories = activitySupport
				.getDefinedCategoryIds();
		List checked = new ArrayList(10), grayed = new ArrayList(10);
		for (Iterator i = categories.iterator(); i.hasNext();) {
			String categoryId = (String) i.next();
			ICategory category = activitySupport
					.getCategory(categoryId);

			int state = NONE;
			Collection activities = getCategoryActivityIds(categoryId);
			int foundCount = 0;
			for (Iterator j = activities.iterator(); j.hasNext();) {
				String activityId = (String) j.next();
				managedActivities.add(activityId);
				if (enabledActivities.contains(activityId)) {
					IActivity activity = activitySupport
							.getActivity(activityId);
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
		Set enabledActivities = new HashSet(activitySupport
                .getEnabledActivityIds());

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

	/**
	 * Restore the default activity states.
	 */
	public void restoreDefaults() {
	    Set defaultEnabled = new HashSet();
	    Set activityIds = activitySupport.getDefinedActivityIds();
	    for (Iterator i = activityIds.iterator(); i.hasNext();) {
            String activityId = (String) i.next();
            IActivity activity = activitySupport.getActivity(activityId);
            try {
                if (activity.isDefaultEnabled()) {
                    defaultEnabled.add(activityId);
                }
            } catch (NotDefinedException e) {
                // this can't happen - we're iterating over defined activities.
            }
        }
	    
		setEnabledStates(defaultEnabled);
	}

	/**
	 * Toggles the enablement state of all activities.
	 * 
	 * @param enabled
	 *            whether the tree should be enabled
	 */
	protected void toggleTreeEnablement(boolean enabled) {
		Object[] elements = provider.getElements(activitySupport);
		
		//reset grey state to null
		dualViewer.setGrayedElements(new Object[0]);
		
		//enable all categories
		for (int i = 0; i < elements.length; i++) {
			dualViewer
					.expandToLevel(elements[i], AbstractTreeViewer.ALL_LEVELS);
			dualViewer.setSubtreeChecked(elements[i], enabled);
		}
	}
}
