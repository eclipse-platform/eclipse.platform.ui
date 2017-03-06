/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.internal.activities.InternalActivityHelper;

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

		@Override
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

		@Override
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
				// Update the category's activities for multiplicity in other
				// categories
				for (Object categoryActivity : provider.getChildren(element)) {
					handleDuplicateActivities(event.getChecked(),
							categoryActivity);
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
			Object[] duplicateActivities = provider.getDuplicateCategoryActivities((CategorizedActivity) element);
			for (Object activity : duplicateActivities) {
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
			for (Object child : children) {
				if (checked.contains(child)) {
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
				requiredActivities = provider.getParentRequiredActivities(((CategorizedActivity) element).getId());
				for (Object requiredActivity : requiredActivities) {
					// We want to uncheck the element if it is checked
					if (checked.contains(requiredActivity)) {
						dualViewer.setChecked(requiredActivity, false);
						handleActivityCheck(new HashSet(Arrays
								.asList(dualViewer.getCheckedElements())),
								requiredActivity);
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

	private TableViewer dependantViewer;

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

		composite.setLayout(new GridLayout(2, true));

		new Label(composite, SWT.NONE).setText(strings.getProperty(ActivitiesPreferencePage.ACTIVITY_NAME,
				ActivityMessages.ActivityEnabler_activities));
		new Label(composite, SWT.NONE).setText(ActivityMessages.ActivityEnabler_description);

		dualViewer = new CheckboxTreeViewer(composite);
		dualViewer.setComparator(new ViewerComparator());
		dualViewer.setLabelProvider(new ActivityCategoryLabelProvider());
		dualViewer.setContentProvider(provider);
		dualViewer.setInput(activitySupport);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		dualViewer.getControl().setLayoutData(data);

		Composite detailsComp = new Composite(composite, SWT.NONE);
		detailsComp.setLayout(new GridLayout());
		detailsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		descriptionText = new Text(detailsComp, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 5);
		descriptionText.setLayoutData(data);
		setInitialStates();

		new Label(detailsComp, SWT.NONE).setText(ActivityMessages.ActivitiesPreferencePage_requirements);
		dependantViewer = new TableViewer(detailsComp, SWT.BORDER);
		dependantViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		dependantViewer.setContentProvider(new ActivityCategoryContentProvider());
		dependantViewer.setLabelProvider(new ActivityCategoryLabelProvider());
		dependantViewer.setInput(Collections.EMPTY_SET);
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(createGridLayoutWithoutMargins(2, fontMetrics));

		Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
		selectAllButton.setText(ActivityMessages.ActivityEnabler_selectAll);
		selectAllButton.addSelectionListener(widgetSelectedAdapter(e -> toggleTreeEnablement(true)));
		setButtonLayoutData(selectAllButton, fontMetrics);

		Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		deselectAllButton.setText(ActivityMessages.ActivityEnabler_deselectAll);
		deselectAllButton.addSelectionListener(widgetSelectedAdapter(e -> toggleTreeEnablement(false)));
		setButtonLayoutData(deselectAllButton, fontMetrics);


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

			Collection activities = InternalActivityHelper
					.getActivityIdsForCategory(activitySupport, category);
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
		Set enabledActivities = new HashSet(activitySupport.getEnabledActivityIds());

		// remove all but the unmanaged activities (if any).
		enabledActivities.removeAll(managedActivities);

		for (Object element : dualViewer.getCheckedElements()) {
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
		for (Object element : elements) {
			dualViewer
					.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
			dualViewer.setSubtreeChecked(element, enabled);
		}
	}
}
