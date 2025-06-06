/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.activities;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.InternalActivityHelper;
import org.eclipse.ui.internal.activities.ws.ActivityEnabler;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;

/**
 * Activities preference page that primarily shows categories and can optionally
 * show an advanced dialog that allows fine-tune adjustmenet of activities. This
 * page may be used by product developers to provide basic ability to tweak the
 * enabled activity set. You may provide certain strings to this class via
 * method #2 of {@link org.eclipse.core.runtime.IExecutableExtension}.
 *
 * @see #ACTIVITY_NAME
 * @see #ALLOW_ADVANCED
 * @see #CAPTION_MESSAGE
 * @see #CATEGORY_NAME
 * @see #ACTIVITY_PROMPT_BUTTON
 * @see #ACTIVITY_PROMPT_BUTTON_TOOLTIP
 *
 * @since 3.1
 */
public final class ActivityCategoryPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage, IExecutableExtension {

	/**
	 * The name to use for the activities. Ie: "Capabilities".
	 */
	public static final String ACTIVITY_NAME = "activityName"; //$NON-NLS-1$

	/**
	 * The parameter to use if you want the page to show the allow button. Must be
	 * true or false.
	 */
	public static final String ALLOW_ADVANCED = "allowAdvanced"; //$NON-NLS-1$

	/**
	 * The string to use for the message at the top of the preference page.
	 */
	public static final String CAPTION_MESSAGE = "captionMessage"; //$NON-NLS-1$

	/**
	 * The name to use for the activity categories. Ie: "Roles".
	 */
	public static final String CATEGORY_NAME = "categoryName"; //$NON-NLS-1$

	/**
	 * The label to be used for the prompt button. Ie: "&amp;Prompt when enabling
	 * capabilities".
	 */
	public static final String ACTIVITY_PROMPT_BUTTON = "activityPromptButton"; //$NON-NLS-1$

	/**
	 * The tooltip to be used for the prompt button. Ie: "Prompt when a feature is
	 * first used that requires enablement of capabilities".
	 */
	public static final String ACTIVITY_PROMPT_BUTTON_TOOLTIP = "activityPromptButtonTooltip"; //$NON-NLS-1$

	private class CategoryLabelProvider extends LabelProvider implements ITableLabelProvider, IActivityManagerListener {

		private LocalResourceManager manager = new LocalResourceManager(JFaceResources.getResources());

		private Optional<ImageDescriptor> lockDescriptor;

		private boolean decorate;

		/**
		 * @param decorate true if the label image may be decorated
		 */
		public CategoryLabelProvider(boolean decorate) {
			this.decorate = decorate;
			lockDescriptor = ResourceLocator.imageDescriptorFromBundle(PlatformUI.PLUGIN_ID,
					"icons/full/ovr16/lock_ovr.svg"); //$NON-NLS-1$
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			ICategory category = (ICategory) element;
			ImageDescriptor descriptor = PlatformUI.getWorkbench().getActivitySupport().getImageDescriptor(category);
			if (descriptor != null) {
				try {
					if (decorate && isLocked(category) && lockDescriptor.isPresent()) {
						descriptor = new DecorationOverlayIcon(descriptor, lockDescriptor.get(), IDecoration.TOP_RIGHT);
					}
					return manager.create(descriptor);
				} catch (DeviceResourceException e) {
					WorkbenchPlugin.log(e);
				}
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			String name = null;
			ICategory category = (ICategory) element;
			try {
				name = category.getName();
			} catch (NotDefinedException e) {
				name = category.getId();
			}
			if (decorate && isLocked(category)) {
				name = NLS.bind(ActivityMessages.ActivitiesPreferencePage_lockedMessage, name);
			}
			return name;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return getText(element);
		}

		@Override
		public void dispose() {
			super.dispose();
			manager.dispose();
		}

		@Override
		public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
			if (activityManagerEvent.haveEnabledActivityIdsChanged()) {
				updateCategoryCheckState();
				fireLabelProviderChanged(new LabelProviderChangedEvent(this));
			}
		}
	}

	private class CategoryContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			// convert to category objects
			return WorkbenchActivityHelper.resolveCategories(workingCopy, (Set<String>) inputElement);
		}
	}

	private class EmptyCategoryFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			ICategory category = (ICategory) element;
			if (InternalActivityHelper.getActivityIdsForCategory(workingCopy, category).isEmpty()) {
				return false;
			}
			return true;
		}
	}

	/**
	 * The {@link IWorkbench}
	 */
	protected IWorkbench workbench;

	private CheckboxTableViewer categoryViewer;

	private TableViewer dependantViewer;

	private Text descriptionText;

	private IMutableActivityManager workingCopy;

	private Button activityPromptButton;

	private boolean allowAdvanced = false;

	private Properties strings = new Properties();

	private ActivityEnabler enabler;

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(strings.getProperty(CAPTION_MESSAGE, ActivityMessages.ActivitiesPreferencePage_captionMessage));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 400;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label = new Label(composite, SWT.NONE); // spacer
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		createPromptButton(composite);

		workbench.getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.CAPABILITY_PREFERENCE_PAGE);

		if (allowAdvanced) {
			enabler = new ActivityEnabler(workingCopy, strings);
			Control enablerControl = enabler.createControl(composite);
			enablerControl.setLayoutData(new GridData(GridData.FILL_BOTH));

			Dialog.applyDialogFont(composite);
			return composite;
		}

		createCategoryArea(composite);
		createDetailsArea(composite);
		createButtons(composite);

		Dialog.applyDialogFont(composite);

		return composite;
	}

	private void createPromptButton(Composite composite) {
		activityPromptButton = new Button(composite, SWT.CHECK);
		activityPromptButton
				.setText(strings.getProperty(ACTIVITY_PROMPT_BUTTON, ActivityMessages.activityPromptButton));
		activityPromptButton.setToolTipText(
				strings.getProperty(ACTIVITY_PROMPT_BUTTON_TOOLTIP, ActivityMessages.activityPromptToolTip));
		GridData data = new GridData();
		data.horizontalSpan = 2;
		activityPromptButton.setLayoutData(data);
		activityPromptButton
				.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT));
	}

	private void createButtons(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);

		Button enableAll = new Button(composite, SWT.PUSH);
		enableAll.addSelectionListener(
				widgetSelectedAdapter(e -> workingCopy.setEnabledActivityIds(workingCopy.getDefinedActivityIds())));
		enableAll.setText(ActivityMessages.ActivityEnabler_selectAll);
		setButtonLayoutData(enableAll);

		Button disableAll = new Button(composite, SWT.PUSH);
		disableAll.addSelectionListener(
				widgetSelectedAdapter(e -> workingCopy.setEnabledActivityIds(Collections.emptySet())));
		disableAll.setText(ActivityMessages.ActivityEnabler_deselectAll);
		setButtonLayoutData(disableAll);

	}

	private void createDetailsArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(composite, SWT.NONE).setText(ActivityMessages.ActivityEnabler_description);
		descriptionText = new Text(composite, SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 100;
		data.widthHint = 200;
		descriptionText.setLayoutData(data);

		new Label(composite, SWT.NONE).setText(ActivityMessages.ActivitiesPreferencePage_requirements);
		dependantViewer = new TableViewer(composite, SWT.BORDER);
		dependantViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		dependantViewer.setContentProvider(new CategoryContentProvider());
		dependantViewer.addFilter(new EmptyCategoryFilter());
		dependantViewer.setLabelProvider(new CategoryLabelProvider(false));
		dependantViewer.setInput(Collections.EMPTY_SET);
	}

	private void createCategoryArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 200;
		composite.setLayoutData(data);
		Label label = new Label(composite, SWT.NONE);
		label.setText(strings.getProperty(CATEGORY_NAME, ActivityMessages.ActivityEnabler_categories));
		Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.SINGLE);
		table.addSelectionListener(widgetSelectedAdapter(e -> {
			if (e.detail == SWT.CHECK) {
				TableItem tableItem = (TableItem) e.item;

				ICategory category = (ICategory) tableItem.getData();
				if (isLocked(category)) {
					tableItem.setChecked(true);
					e.doit = false; // veto the check
					return;
				}
				Set<String> activitySet = WorkbenchActivityHelper.getActivityIdsForCategory(category);
				if (tableItem.getChecked()) {
					activitySet.addAll(workingCopy.getEnabledActivityIds());
				} else {
					HashSet<String> newSet = new HashSet<>(workingCopy.getEnabledActivityIds());
					newSet.removeAll(activitySet);
					activitySet = newSet;
				}

				workingCopy.setEnabledActivityIds(activitySet);
				updateCategoryCheckState(); // even though we're reacting to
				// a check change we may need to
				// refresh a greying change.
				// Just process the whole thing.
			}
		}));
		categoryViewer = new CheckboxTableViewer(table);
		categoryViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		categoryViewer.setContentProvider(new CategoryContentProvider());
		CategoryLabelProvider categoryLabelProvider = new CategoryLabelProvider(true);
		workingCopy.addActivityManagerListener(categoryLabelProvider);
		categoryViewer.setLabelProvider(categoryLabelProvider);
		categoryViewer.setComparator(new ViewerComparator());
		categoryViewer.addFilter(new EmptyCategoryFilter());

		categoryViewer.addSelectionChangedListener(event -> {
			ICategory element = (ICategory) event.getStructuredSelection().getFirstElement();
			setDetails(element);
		});
		categoryViewer.setInput(workingCopy.getDefinedCategoryIds());

		updateCategoryCheckState();
	}

	/**
	 * Updates the check and grey state of the categories in the category viewer.
	 *
	 * @since 3.2
	 */
	private void updateCategoryCheckState() {
		ICategory[] enabledCategories = getEnabledCategories();
		ICategory[] partiallyEnabledCategories = getPartialCategories();
		Object[] allChecked = new Object[enabledCategories.length + partiallyEnabledCategories.length];
		System.arraycopy(enabledCategories, 0, allChecked, 0, enabledCategories.length);
		System.arraycopy(partiallyEnabledCategories, 0, allChecked, enabledCategories.length,
				partiallyEnabledCategories.length);
		categoryViewer.setCheckedElements(allChecked);
		categoryViewer.setGrayedElements((Object[]) partiallyEnabledCategories);
	}

	private ICategory[] getPartialCategories() {
		return WorkbenchActivityHelper.resolveCategories(workingCopy,
				InternalActivityHelper.getPartiallyEnabledCategories(workingCopy));
	}

	private ICategory[] getEnabledCategories() {
		return WorkbenchActivityHelper.resolveCategories(workingCopy,
				InternalActivityHelper.getEnabledCategories(workingCopy));
	}

	/**
	 * @param category the {@link ICategory} to get the details from
	 */
	protected void setDetails(ICategory category) {
		if (category == null) {
			clearDetails();
			return;
		}
		Set<String> categories = null;
		if (WorkbenchActivityHelper.isEnabled(workingCopy, category.getId())) {
			categories = WorkbenchActivityHelper.getDisabledCategories(workingCopy, category.getId());

		} else {
			categories = WorkbenchActivityHelper.getEnabledCategories(workingCopy, category.getId());
		}

		categories = WorkbenchActivityHelper.getContainedCategories(workingCopy, category.getId());
		dependantViewer.setInput(categories);
		try {
			descriptionText.setText(category.getDescription());
		} catch (NotDefinedException e) {
			descriptionText.setText(""); //$NON-NLS-1$
		}
	}

	/**
	 * Clear the details area.
	 */
	protected void clearDetails() {
		dependantViewer.setInput(Collections.EMPTY_SET);
		descriptionText.setText(""); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		workingCopy = workbench.getActivitySupport().createWorkingCopy();
		setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Return whether the category is locked.
	 *
	 * @param category the category to test
	 * @return whether the category is locked
	 */
	protected boolean isLocked(ICategory category) {
		return !WorkbenchActivityHelper.getDisabledCategories(workingCopy, category.getId()).isEmpty();
	}

	@Override
	public boolean performOk() {
		if (allowAdvanced) {
			enabler.updateActivityStates();
		}

		workbench.getActivitySupport().setEnabledActivityIds(workingCopy.getEnabledActivityIds());
		getPreferenceStore().setValue(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT,
				activityPromptButton.getSelection());
		return true;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		activityPromptButton.setSelection(
				getPreferenceStore().getDefaultBoolean(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT));
		if (allowAdvanced) {
			enabler.restoreDefaults();
			return;
		}

		Set<String> defaultEnabled = new HashSet<>();
		Set<String> activityIds = workingCopy.getDefinedActivityIds();
		for (String activityId : activityIds) {
			IActivity activity = workingCopy.getActivity(activityId);
			try {
				if (activity.isDefaultEnabled()) {
					defaultEnabled.add(activityId);
				}
			} catch (NotDefinedException e) {
				// this can't happen - we're iterating over defined activities.
			}
		}

		workingCopy.setEnabledActivityIds(defaultEnabled);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		if (data instanceof Hashtable) {
			Hashtable<?, ?> table = (Hashtable<?, ?>) data;
			allowAdvanced = Boolean.parseBoolean((String) table.remove(ALLOW_ADVANCED));
			strings.putAll(table);
		}
	}

	@Override
	public void dispose() {
		if ((workingCopy != null) && (!allowAdvanced)) {
			workingCopy.removeActivityManagerListener((CategoryLabelProvider) categoryViewer.getLabelProvider());
		}
		super.dispose();
	}
}
