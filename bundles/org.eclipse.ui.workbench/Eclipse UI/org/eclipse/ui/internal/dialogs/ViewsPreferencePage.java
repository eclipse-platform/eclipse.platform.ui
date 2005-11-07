/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.text.Collator;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.themes.IThemeDescriptor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * The ViewsPreferencePage is the page used to set preferences for the look of
 * the views in the workbench.
 */
public class ViewsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Button showTextOnPerspectiveBar;

	/*
	 * change the tab style of the workbench
	 */
	private Button showTraditionalStyleTabs;

	private Button enableAnimations;

	private Button editorTopButton;

	private Button editorBottomButton;

	private Button viewTopButton;

	private Button viewBottomButton;

	private Button perspLeftButton;

	private Button perspTopLeftButton;

	private Button perspTopRightButton;

	static final String EDITORS_TITLE = WorkbenchMessages.ViewsPreference_editors;

	private static final String EDITORS_TOP_TITLE = WorkbenchMessages.ViewsPreference_editors_top;

	private static final String EDITORS_BOTTOM_TITLE = WorkbenchMessages.ViewsPreference_editors_bottom;

	private static final String VIEWS_TITLE = WorkbenchMessages.ViewsPreference_views;

	private static final String VIEWS_TOP_TITLE = WorkbenchMessages.ViewsPreference_views_top;

	private static final String VIEWS_BOTTOM_TITLE = WorkbenchMessages.ViewsPreference_views_bottom;

	private static final String PERSP_TITLE = WorkbenchMessages.ViewsPreference_perspectiveBar;

	private static final String PERSP_LEFT_TITLE = WorkbenchMessages.ViewsPreference_perspectiveBar_left;

	private static final String PERSP_TOP_LEFT_TITLE = WorkbenchMessages.ViewsPreference_perspectiveBar_topLeft;

	private static final String PERSP_TOP_RIGHT_TITLE = WorkbenchMessages.ViewsPreference_perspectiveBar_topRight;

	// These constants aren't my favourite idea, but to get this preference done
	// for M9... A better solution might be to have the presentation factory set
	// its dependant preference defaults on startup. I've filed bug 63346 to do
	// something about this area.
	private static final String R21PRESENTATION_ID = "org.eclipse.ui.internal.r21presentationFactory"; //$NON-NLS-1$

	private static final String R30PRESENTATION_ID = "org.eclipse.ui.presentations.default"; //$NON-NLS-1$

	private static final String INITIAL_VAL = new String();

	private static final int INITIAL_LOC_INT = -1;

	// remembers whether the hidden fastview bar pref needs to be changed on
	// OK/Apply
	private String fastViewLoc = INITIAL_VAL;

	private String showTextOnPerspBar = INITIAL_VAL;

	private int editorAlignment;

	private boolean editorAlignmentChanged = false;

	private int viewAlignment;

	private boolean viewAlignmentChanged = false;

	private String perspBarLocation;

	private Combo themeCombo;

	private Combo presentationCombo;

	private IConfigurationElement[] presentationFactories;

	private String currentPresentationFactoryId;

	private Button overridePresButton;

	private IPropertyChangeListener overrideListener;

	private boolean restartPosted = false;

	/**
	 * Create a composite that for creating the tab toggle buttons.
	 * 
	 * @param composite  Composite
	 * @param title  String
	 */
	private Group createButtonGroup(Composite composite, String title) {
		Group buttonComposite = new Group(composite, SWT.NONE);
		buttonComposite.setText(title);
		buttonComposite.setFont(composite.getFont());
		FormLayout layout = new FormLayout();
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);

		return buttonComposite;

	}

	/**
	 * Creates and returns the SWT control for the customized body of this
	 * preference page under the given parent composite.
	 * <p>
	 * This framework method must be implemented by concrete subclasses.
	 * </p>
	 * 
	 * @param parent  the parent composite
	 * @return Control the new control
	 */
	protected Control createContents(Composite parent) {

		Font font = parent.getFont();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.VIEWS_PREFERENCE_PAGE);

		IPreferenceStore internalStore = PrefUtil.getInternalPreferenceStore();
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		editorAlignment = internalStore
				.getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
		viewAlignment = internalStore
				.getInt(IPreferenceConstants.VIEW_TAB_POSITION);
		perspBarLocation = apiStore
				.getString(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(font);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		// layout.verticalSpacing = 10;
		composite.setLayout(layout);

		createPresentationCombo(composite);
		createPresentationOverride(composite);
		createEditorTabButtonGroup(composite);
		createViewTabButtonGroup(composite);
		createPerspBarTabButtonGroup(composite);
		createShowTextOnPerspectiveBarPref(composite);
		hookOverrideListener();
		updateOverride();

		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;

		Label label = new Label(composite, SWT.NONE);
		label.setText(WorkbenchMessages.ViewsPreference_currentTheme);
		label.setFont(parent.getFont());
		label.setLayoutData(data);

		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;

		themeCombo = new Combo(composite, SWT.READ_ONLY);
		themeCombo.setLayoutData(data);
		themeCombo.setFont(parent.getFont());
		refreshThemeCombo();

		createShowTraditionalStyleTabsPref(composite);
		createEnableAnimationsPref(composite);

		return composite;
	}

	private void createPresentationOverride(Composite parent) {
		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;

		overridePresButton = new Button(parent, SWT.CHECK);
		overridePresButton.setText("O&verride presentation settings"); //$NON-NLS-1$
		overridePresButton.setFont(parent.getFont());
		overridePresButton.setLayoutData(data);

		IPreferenceStore store = getPreferenceStore();
		overridePresButton.setSelection(store
				.getBoolean(IPreferenceConstants.OVERRIDE_PRESENTATION));
		overridePresButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateOverrideState(overridePresButton.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				updateOverrideState(overridePresButton.getSelection());
			}
		});
	}

	private void updateOverrideState(boolean override) {
		IPreferenceStore store = getPreferenceStore();
		if (store.getBoolean(IPreferenceConstants.OVERRIDE_PRESENTATION) != override)
			store
					.setValue(IPreferenceConstants.OVERRIDE_PRESENTATION,
							override);
		// as we are no longer overriding the prefs should match the selected
		// presentation
		setPresentationPrefs(getSelectedPresentationID());
	}

	private void createPresentationCombo(Composite parent) {
		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;

		Label label = new Label(parent, SWT.NONE);
		label.setText(WorkbenchMessages.ViewsPreference_currentPresentation);
		label.setFont(parent.getFont());
		label.setLayoutData(data);

		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;

		presentationCombo = new Combo(parent, SWT.READ_ONLY);
		presentationCombo.setFont(parent.getFont());
		presentationCombo.setLayoutData(data);

		presentationCombo.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				updateSettings();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				updateSettings();
			}

			private void updateSettings() {
				if (!overridePresButton.getSelection())
					setPresentationPrefs(getSelectedPresentationID());
			}
		});

		refreshPresentationCombo();
		setPresentationSelection();
	}

	/**
	 * Set the two supplied controls to be beside each other.
	 */

	private void attachControls(Control leftControl, Control rightControl) {

		FormData leftData = new FormData();
		leftData.left = new FormAttachment(0, 0);

		FormData rightData = new FormData();
		rightData.left = new FormAttachment(leftControl, 5);

		leftControl.setLayoutData(leftData);
		rightControl.setLayoutData(rightData);
	}

	/**
	 * Create a composite that contains buttons for selecting tab position for
	 * the edit selection.
	 * 
	 * @param composite  Composite
	 */
	private void createEditorTabButtonGroup(Composite composite) {

		Font font = composite.getFont();

		Group buttonComposite = createButtonGroup(composite, EDITORS_TITLE);

		this.editorTopButton = new Button(buttonComposite, SWT.RADIO);
		this.editorTopButton.setText(EDITORS_TOP_TITLE);
		this.editorTopButton.setSelection(this.editorAlignment == SWT.TOP);
		this.editorTopButton.setFont(font);
		this.editorTopButton.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					public void getName(AccessibleEvent e) {
						e.result = EDITORS_TITLE;
					}
				});

		this.editorBottomButton = new Button(buttonComposite, SWT.RADIO);
		this.editorBottomButton.setText(EDITORS_BOTTOM_TITLE);
		this.editorBottomButton
				.setSelection(this.editorAlignment == SWT.BOTTOM);
		this.editorBottomButton.setFont(font);

		SelectionListener sel = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.equals(editorTopButton)) {
					if (editorAlignment != SWT.TOP) {
						editorAlignment = SWT.TOP;
						editorAlignmentChanged = true;
					}
				} else if (e.widget.equals(editorBottomButton)) {
					if (editorAlignment != SWT.BOTTOM) {
						editorAlignment = SWT.BOTTOM;
						editorAlignmentChanged = true;
					}
				}
			}
		};

		editorTopButton.addSelectionListener(sel);
		editorBottomButton.addSelectionListener(sel);

		attachControls(this.editorTopButton, this.editorBottomButton);

	}

	/**
	 * Create a composite that contains buttons for selecting tab position for
	 * the view selection.
	 * 
	 * @param composite  Composite
	 */
	private void createViewTabButtonGroup(Composite composite) {

		Font font = composite.getFont();

		Group buttonComposite = createButtonGroup(composite, VIEWS_TITLE);
		buttonComposite.setFont(font);

		this.viewTopButton = new Button(buttonComposite, SWT.RADIO);
		this.viewTopButton.setText(VIEWS_TOP_TITLE);
		this.viewTopButton.setSelection(this.viewAlignment == SWT.TOP);
		this.viewTopButton.setFont(font);

		this.viewBottomButton = new Button(buttonComposite, SWT.RADIO);
		this.viewBottomButton.setText(VIEWS_BOTTOM_TITLE);
		this.viewBottomButton.setSelection(this.viewAlignment == SWT.BOTTOM);
		this.viewBottomButton.setFont(font);

		SelectionListener sel = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.equals(viewTopButton)) {
					if (viewAlignment != SWT.TOP) {
						viewAlignment = SWT.TOP;
						viewAlignmentChanged = true;
					}
				} else if (e.widget.equals(viewBottomButton)) {
					if (viewAlignment != SWT.BOTTOM) {
						viewAlignment = SWT.BOTTOM;
						viewAlignmentChanged = true;
					}
				}
			}
		};

		viewTopButton.addSelectionListener(sel);
		viewBottomButton.addSelectionListener(sel);

		attachControls(this.viewTopButton, this.viewBottomButton);
	}

	/**
	 * Create a composite that contains buttons for selecting perspective
	 * switcher position.
	 * 
	 * @param composite Composite
	 */
	private void createPerspBarTabButtonGroup(Composite composite) {
		Font font = composite.getFont();

		Group buttonComposite = createButtonGroup(composite, PERSP_TITLE);
		buttonComposite.setFont(font);

		perspLeftButton = new Button(buttonComposite, SWT.RADIO);
		perspLeftButton.setText(PERSP_LEFT_TITLE);
		perspLeftButton.setSelection(IWorkbenchPreferenceConstants.LEFT
				.equals(perspBarLocation));
		perspLeftButton.setFont(font);
		perspLeftButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				perspBarLocation = IWorkbenchPreferenceConstants.LEFT;
			}
		});

		perspTopLeftButton = new Button(buttonComposite, SWT.RADIO);
		perspTopLeftButton.setText(PERSP_TOP_LEFT_TITLE);
		perspTopLeftButton.setSelection(IWorkbenchPreferenceConstants.TOP_LEFT
				.equals(perspBarLocation));
		perspTopLeftButton.setFont(font);
		perspTopLeftButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				perspBarLocation = IWorkbenchPreferenceConstants.TOP_LEFT;
			}
		});

		perspTopRightButton = new Button(buttonComposite, SWT.RADIO);
		perspTopRightButton.setText(PERSP_TOP_RIGHT_TITLE);
		perspTopRightButton
				.setSelection(IWorkbenchPreferenceConstants.TOP_RIGHT
						.equals(perspBarLocation));
		perspTopRightButton.setFont(font);
		perspTopRightButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				perspBarLocation = IWorkbenchPreferenceConstants.TOP_RIGHT;
			}
		});

		FormData leftData = new FormData();
		leftData.left = new FormAttachment(0, 5);

		FormData topLeftData = new FormData();
		topLeftData.left = new FormAttachment(perspLeftButton, 5);

		FormData topRightData = new FormData();
		topRightData.left = new FormAttachment(perspTopLeftButton, 0);

		perspLeftButton.setLayoutData(leftData);
		perspTopLeftButton.setLayoutData(topLeftData);
		perspTopRightButton.setLayoutData(topRightData);
	}

	/**
	 * Hook a listener to update the buttons based on an override preference. If
	 * the preference is false then do not allow editing of these options.
	 * 
	 */
	private void hookOverrideListener() {
		if (overrideListener != null)
			return;
		final IPreferenceStore store = getPreferenceStore();
		overrideListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(
						IPreferenceConstants.OVERRIDE_PRESENTATION)) {
					updateOverride();
				}
			}
		};
		store.addPropertyChangeListener(overrideListener);
	}

	/**
	 * Dispose resources created by the receiver.
	 */
	public void dispose() {
		super.dispose();
		if (overrideListener != null) {
			getPreferenceStore().removePropertyChangeListener(overrideListener);
			overrideListener = null;
		}
	}

	private void updateOverride() {
		boolean override = getPreferenceStore().getBoolean(
				IPreferenceConstants.OVERRIDE_PRESENTATION);
		editorTopButton.setEnabled(override);
		editorBottomButton.setEnabled(override);
		viewTopButton.setEnabled(override);
		viewBottomButton.setEnabled(override);
		perspTopLeftButton.setEnabled(override);
		perspLeftButton.setEnabled(override);
		perspTopRightButton.setEnabled(override);
		showTextOnPerspectiveBar.setEnabled(override);
	}

	private void refreshPresentationCombo() {

		// get the active presentation
		presentationCombo.removeAll();
		refreshPresentationFactories();

		for (int i = 0; i < presentationFactories.length; ++i) {
			IConfigurationElement el = presentationFactories[i];
			String name = el.getAttribute(IWorkbenchConstants.TAG_NAME);
			if (!currentPresentationFactoryId.equals(el
					.getAttribute(IWorkbenchConstants.TAG_ID)))
				presentationCombo.add(name);
			else {
				presentationCombo
					.add(NLS.bind(
						WorkbenchMessages.ViewsPreference_currentPresentationFormat,
						name));
			}
		}

	}

	private void setPresentationSelection() {
		for (int i = 0; i < presentationFactories.length; ++i) {
			if (currentPresentationFactoryId.equals(presentationFactories[i]
					.getAttribute(IWorkbenchConstants.TAG_ID))) {
				presentationCombo.select(i);
				break;
			}
		}
	}

	/**
	 * Update this page's list of presentation factories. This should only be
	 * used when the presentation combo is refreshed, as this array will be used
	 * to set the selection from the combo.
	 */
	private void refreshPresentationFactories() {
		// update the current selection (used to look for changes on apply)
		currentPresentationFactoryId = PrefUtil.getAPIPreferenceStore()
				.getString(
						IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID);
		// Workbench.getInstance().getPresentationId();

		// update the sorted list of factories
		presentationFactories = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PlatformUI.PLUGIN_ID,
						IWorkbenchConstants.PL_PRESENTATION_FACTORIES);

		// sort the array by name
		Arrays.sort(presentationFactories, new Comparator() {
			Collator collator = Collator.getInstance(Locale.getDefault());

			public int compare(Object a, Object b) {
				IConfigurationElement el1 = (IConfigurationElement) a;
				IConfigurationElement el2 = (IConfigurationElement) b;
				return collator.compare(el1
						.getAttribute(IWorkbenchConstants.TAG_NAME), el2
						.getAttribute(IWorkbenchConstants.TAG_NAME));
			}
		});
	}

	/**
	 * Update the preferences associated with the argument presentation factory.
	 * 
	 * @return boolean
	 *         <code>true<\code> of the presentation has changed and <code>false<\code> otherwise
	 */
	private boolean updatePresentationPreferences() {
		// There are some preference values associated with the R2.1
		// presentation that cannot be captured in the presentation
		// factory. Perhaps the extension point should contain these
		// (a list of attributes?), but for now it is done manually.

		if (presentationCombo == null) {
			return false;
		}

		String id = getSelectedPresentationID();

		// if it hasn't changed then there's nothing to do
		if (id.equals(currentPresentationFactoryId))
			return false;

		currentPresentationFactoryId = id;
		// apply 2.1 prefs if needed
		setPresentationPrefs(id);
		// set the new presentation factory id
		PrefUtil.getAPIPreferenceStore().putValue(
				IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID, id);
		// a restart is required to update the presentation
		return true;

	}

	private void setPresentationPrefs(String id) {
		if (R21PRESENTATION_ID.equals(id))
			setR21Preferences();
		else if (R30PRESENTATION_ID.equals(id))
			setR30Preferences();
	}

	private String getSelectedPresentationID() {
		int selection = presentationCombo.getSelectionIndex();
		IConfigurationElement element = presentationFactories[selection];
		String id = element.getAttribute(IWorkbenchConstants.TAG_ID);
		return id;
	}

	private void setR30Preferences() {
		IPreferenceStore internalStore = PrefUtil.getInternalPreferenceStore();
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		setEditorAlignDefault(internalStore);
		setViewAlignDefault(internalStore);

		// perspective switcher on the left
		perspBarLocation = apiStore
				.getDefaultString(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR);
		perspLeftButton.setSelection(perspBarLocation
				.equals(IWorkbenchPreferenceConstants.LEFT));
		perspTopLeftButton.setSelection(perspBarLocation
				.equals(IWorkbenchPreferenceConstants.TOP_LEFT));
		perspTopRightButton.setSelection(perspBarLocation
				.equals(IWorkbenchPreferenceConstants.TOP_RIGHT));

		// perspective bar should be set to default on OK or Apply
		perspBarLocation = INITIAL_VAL;

		// turn off text on persp bar
		showTextOnPerspectiveBar
				.setSelection(apiStore
						.getDefaultBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		// text on perspective bar should be set to default on OK or Apply
		showTextOnPerspBar = INITIAL_VAL;

		// fast view bar should be set to default on OK or Apply
		fastViewLoc = INITIAL_VAL;
	}

	private void setViewAlignDefault(IPreferenceStore internalStore) {
		int oldVal;
		// reset the preferences for 3.0 presentation
		oldVal = viewAlignment;
		viewAlignment = internalStore
				.getDefaultInt(IPreferenceConstants.VIEW_TAB_POSITION);
		viewTopButton.setSelection(viewAlignment == SWT.TOP);
		viewBottomButton.setSelection(viewAlignment == SWT.BOTTOM);

		// view tabs should be set to default on OK or Apply
		if (oldVal != viewAlignment)
			viewAlignmentChanged = true;
		viewAlignment = INITIAL_LOC_INT;
	}

	private void setEditorAlignDefault(IPreferenceStore internalStore) {
		// editor tabs on the bottom, really should associate this with the
		// presentation
		int oldVal = editorAlignment;
		editorAlignment = internalStore
				.getDefaultInt(IPreferenceConstants.EDITOR_TAB_POSITION);
		editorTopButton.setSelection(editorAlignment == SWT.TOP);
		editorBottomButton.setSelection(editorAlignment == SWT.BOTTOM);

		// editor tabs should be set to default on OK or Apply
		if (oldVal != editorAlignment)
			editorAlignmentChanged = true;
		editorAlignment = INITIAL_LOC_INT;
	}

	private void setR21Preferences() {
		// editor tabs on the bottom, really should associate this with the
		// presentation
		int oldVal = editorAlignment;
		editorAlignment = SWT.TOP;
		editorTopButton.setSelection(editorAlignment == SWT.TOP);
		editorBottomButton.setSelection(editorAlignment == SWT.BOTTOM);
		if (oldVal != editorAlignment)
			editorAlignmentChanged = true;

		// view tabs on the bottom, really should associate this with the
		// presentation
		oldVal = viewAlignment;
		viewAlignment = SWT.BOTTOM;
		viewTopButton.setSelection(viewAlignment == SWT.TOP);
		viewBottomButton.setSelection(viewAlignment == SWT.BOTTOM);
		if (oldVal != viewAlignment)
			viewAlignmentChanged = true;

		// perspective switcher on the left, really should associate this with
		// the presentation
		perspBarLocation = IWorkbenchPreferenceConstants.LEFT;
		perspLeftButton.setSelection(perspBarLocation
				.equals(IWorkbenchPreferenceConstants.LEFT));
		perspTopLeftButton.setSelection(perspBarLocation
				.equals(IWorkbenchPreferenceConstants.TOP_LEFT));
		perspTopRightButton.setSelection(perspBarLocation
				.equals(IWorkbenchPreferenceConstants.TOP_RIGHT));

		// turn off text on persp bar, really should associate this with the
		// presentation
		showTextOnPerspectiveBar.setSelection(false);
		showTextOnPerspBar = String.valueOf(false);

		// fast view bar on the left (hidden pref, set it directly)
		fastViewLoc = IWorkbenchPreferenceConstants.LEFT;
	}

	/**
	 * 
	 */
	private void refreshThemeCombo() {
		themeCombo.removeAll();
		ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme();

		IThemeDescriptor[] descs = WorkbenchPlugin.getDefault()
				.getThemeRegistry().getThemes();
		int selection = 0;
		String themeString = PlatformUI.getWorkbench().getThemeManager()
				.getTheme(IThemeManager.DEFAULT_THEME).getLabel();
		if (currentTheme.getId().equals(IThemeManager.DEFAULT_THEME)) {
			themeString = MessageFormat.format(
					WorkbenchMessages.ViewsPreference_currentThemeFormat,
					new Object[] { themeString });
		}
		themeCombo.add(themeString);

		for (int i = 0; i < descs.length; i++) {
			themeString = descs[i].getName();
			if (descs[i].getId().equals(currentTheme.getId())) {
				themeString = MessageFormat.format(
						WorkbenchMessages.ViewsPreference_currentThemeFormat,
						new Object[] { themeString });
				selection = i + 1;
			}
			themeCombo.add(themeString);
		}

		themeCombo.select(selection);
	}

	/**
	 * Create the button and text that support setting the preference for
	 * showing text labels on the perspective switching bar
	 */
	protected void createShowTextOnPerspectiveBarPref(Composite composite) {
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		showTextOnPerspectiveBar = new Button(composite, SWT.CHECK);
		showTextOnPerspectiveBar
				.setText(WorkbenchMessages.WorkbenchPreference_showTextOnPerspectiveBar);
		showTextOnPerspectiveBar.setFont(composite.getFont());
		showTextOnPerspectiveBar
				.setSelection(apiStore
						.getBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		setButtonLayoutData(showTextOnPerspectiveBar);
	}

	/**
	 * Create the button and text that support setting the preference for
	 * showing text labels on the perspective switching bar
	 */
	protected void createShowTraditionalStyleTabsPref(Composite composite) {
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		showTraditionalStyleTabs = new Button(composite, SWT.CHECK);
		showTraditionalStyleTabs
				.setText(WorkbenchMessages.ViewsPreference_traditionalTabs);
		showTraditionalStyleTabs.setFont(composite.getFont());
		showTraditionalStyleTabs
				.setSelection(apiStore
						.getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		setButtonLayoutData(showTraditionalStyleTabs);
	}

	protected void createEnableAnimationsPref(Composite composite) {
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		enableAnimations = new Button(composite, SWT.CHECK);
		enableAnimations
				.setText(WorkbenchMessages.ViewsPreference_enableAnimations);
		enableAnimations.setFont(composite.getFont());
		enableAnimations.setSelection(apiStore
				.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS));
		setButtonLayoutData(enableAnimations);
	}

	/**
	 * Returns preference store that belongs to the our plugin.
	 * 
	 * @return IPreferenceStore the preference store for this plugin
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Initializes this preference page for the given workbench.
	 * <p>
	 * This method is called automatically as the preference page is being
	 * created and initialized. Clients must not call this method.
	 * </p>
	 * 
	 * @param workbench  the workbench
	 */
	public void init(org.eclipse.ui.IWorkbench workbench) {
		currentPresentationFactoryId = PrefUtil.getAPIPreferenceStore()
				.getString(
						IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID);
	}

	/**
	 * The default button has been pressed.
	 */
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		showTextOnPerspectiveBar
				.setSelection(apiStore
						.getDefaultBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		showTraditionalStyleTabs
				.setSelection(apiStore
						.getDefaultBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		enableAnimations
				.setSelection(apiStore
						.getDefaultBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS));

		String presID = apiStore
				.getDefaultString(IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID);
		currentPresentationFactoryId = presID;
		setPresentationSelection();

		boolean overridePrefs = store
				.getDefaultBoolean(IPreferenceConstants.OVERRIDE_PRESENTATION);
		overridePresButton.setSelection(overridePrefs);

		setEditorAlignDefault(store);
		setViewAlignDefault(store);

		perspBarLocation = apiStore
				.getDefaultString(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR);
		perspLeftButton.setSelection(IWorkbenchPreferenceConstants.LEFT
				.equals(perspBarLocation));
		perspTopLeftButton.setSelection(IWorkbenchPreferenceConstants.TOP_LEFT
				.equals(perspBarLocation));
		perspTopRightButton
				.setSelection(IWorkbenchPreferenceConstants.TOP_RIGHT
						.equals(perspBarLocation));

		WorkbenchPlugin.getDefault().savePluginPreferences();
		super.performDefaults();
	}

	/**
	 * The user has pressed Ok. Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
		boolean override = store
				.getBoolean(IPreferenceConstants.OVERRIDE_PRESENTATION);

		// apply the presentation selection first since it might change some of
		// the other values
		boolean restart = updatePresentationPreferences();

		if (showTextOnPerspBar.equals(INITIAL_VAL) && !override) {
			apiStore
					.setToDefault(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR);
		} else {
			apiStore.setValue(
					IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR,
					showTextOnPerspectiveBar.getSelection());
		}

		if (editorAlignmentChanged) {
			if (editorAlignment == INITIAL_LOC_INT) {
				store.setToDefault(IPreferenceConstants.EDITOR_TAB_POSITION);
			} else if (!override) {
				// store the editor tab value to setting
				store.setValue(IPreferenceConstants.EDITOR_TAB_POSITION,
						editorAlignment);
			} else {
				// store the editor tab value to setting
				store.setValue(IPreferenceConstants.EDITOR_TAB_POSITION,
						editorAlignment);
			}
			restart = true;
		}

		if (viewAlignmentChanged) {
			if (viewAlignment == INITIAL_LOC_INT) {
				store.setToDefault(IPreferenceConstants.VIEW_TAB_POSITION);
			} else if (!override) {
				// store the view tab value to setting
				store.setValue(IPreferenceConstants.VIEW_TAB_POSITION,
						viewAlignment);
			} else {
				// store the view tab value to setting
				store.setValue(IPreferenceConstants.VIEW_TAB_POSITION,
						viewAlignment);
			}
			restart = true;
		}

		if (perspBarLocation.equals(INITIAL_VAL)) {
			apiStore
					.setToDefault(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR);
		} else if (!override) {
			// store the perspective bar text enabled setting
			apiStore.setValue(
					IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
					perspBarLocation);
		} else {
			// store the perspective bar text enabled setting
			apiStore.setValue(
					IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
					perspBarLocation);
		}

		if (fastViewLoc.equals(INITIAL_VAL)) {
			apiStore
					.setToDefault(IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION);
		} else {
			apiStore
					.setValue(
							IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION,
							fastViewLoc);
		}

		int idx = themeCombo.getSelectionIndex();
		if (idx == 0) {
			Workbench.getInstance().getThemeManager().setCurrentTheme(
					IThemeManager.DEFAULT_THEME);
		} else {
			Workbench.getInstance().getThemeManager()
					.setCurrentTheme(
							WorkbenchPlugin.getDefault().getThemeRegistry()
									.getThemes()[idx - 1].getId());
		}

		apiStore.setValue(
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				showTraditionalStyleTabs.getSelection());
		apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
				enableAnimations.getSelection());

		refreshThemeCombo();
		PrefUtil.savePrefs();

		// we can get here through performApply, in that case only post one
		// restart
		if (restart && !restartPosted) {
			if (getContainer() instanceof IWorkbenchPreferenceContainer) {
				IWorkbenchPreferenceContainer container = (IWorkbenchPreferenceContainer) getContainer();
				UIJob job = new UIJob("Restart Request") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						// make sure they really want to do this
						int really = new MessageDialog(
								null,
								WorkbenchMessages.ViewsPreference_presentationConfirm_title,
								null,
								WorkbenchMessages.ViewsPreference_presentationConfirm_message,
								MessageDialog.QUESTION,
								new String[] {
										WorkbenchMessages.ViewsPreference_presentationConfirm_yes,
										WorkbenchMessages.ViewsPreference_presentationConfirm_no },
								1).open();
						if (really == Window.OK)
							PlatformUI.getWorkbench().restart();
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				container.registerUpdateJob(job);
				restartPosted = true;
			}
		}
		return true;
	}
}
