/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
 *     Erik Chou <ekchou@ymail.com> - Bug 425962
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445664, 442278, 472654
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 388476
 *     Patrik Suzzi - <psuzzi@gmail.com> - Bug 515265
 *     Kit Lo - <kitlo@us.ibm.com> - Bug 572444
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import static org.eclipse.jface.viewers.LabelProvider.createTextProvider;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_COLOR_AND_FONT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_OS_VERSION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_THEME_ASSOCIATION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_THEME_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.UserScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.renderers.swt.CTabRendering;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.themes.IThemeDescriptor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.themes.IThemeManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The ViewsPreferencePage is the page used to set preferences for the
 * appearance of the workbench. Originally this applied only to views but now
 * applies to the overall appearance, hence the name.
 */
public class ViewsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String E4_THEME_EXTENSION_POINT = "org.eclipse.e4.ui.css.swt.theme"; //$NON-NLS-1$

	/** The workbench theme engine; may be {@code null} if no engine */
	private IThemeEngine engine;
	private ComboViewer themeIdCombo;
	private ControlDecoration themeComboDecorator;
	private ITheme currentTheme;
	private String defaultTheme;
	private Button enableMru;
	private Button useColoredLabels;

	private ComboViewer colorsAndFontsThemeCombo;
	private ControlDecoration colorFontsDecorator;
	private ColorsAndFontsTheme currentColorsAndFontsTheme;
	private Map<String, String> themeAssociations;
	private boolean highContrastMode;

	private Button themingEnabled;
	private Button rescaleAtRuntime;

	private Button hideIconsForViewTabs;
	private Button showFullTextForViewTabs;

	private Button showDirtyIndicatorForTabs;

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.VIEWS_PREFERENCE_PAGE);

		Composite comp = new Composite(parent, SWT.NONE);

		highContrastMode = parent.getDisplay().getHighContrast();
		// Deactivate theming in high contrast mode
		themingEnabled = createCheckButton(comp, WorkbenchMessages.ThemingEnabled, isThemingPossible());
		themingEnabled.setEnabled(!highContrastMode);

		// if started with "-cssTheme none" or if high contrast mode is active,
		// CSS settings should be disabled but other appearance settings should be *not*
		// disabled
		if (!isThemingPossible()) {
			GridLayout layout = new GridLayout(1, false);
			layout.horizontalSpacing = 10;
			comp.setLayout(layout);
			createThemeIndependentComposits(comp);
			createRescaleAtRuntimeCheckButton(comp);
			return comp;
		}

		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		comp.setLayout(layout);

		new Label(comp, SWT.NONE).setText(WorkbenchMessages.ViewsPreferencePage_Theme);

		Composite themeComposite = new Composite(comp, SWT.NONE);
		GridLayout themeLayout = new GridLayout(2, false);
		themeLayout.marginWidth = 0;
		themeLayout.marginHeight = 0;
		themeComposite.setLayout(themeLayout);
		themeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		themeIdCombo = new ComboViewer(themeComposite, SWT.READ_ONLY);
		themeIdCombo.setLabelProvider(createTextProvider(element -> ((ITheme) element).getLabel()));
		themeIdCombo.setContentProvider(ArrayContentProvider.getInstance());
		themeIdCombo.setInput(engine.getThemes());
		themeIdCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.currentTheme = engine.getActiveTheme();
		if (this.currentTheme != null) {
			themeIdCombo.setSelection(new StructuredSelection(currentTheme));
		}
		themeComboDecorator = new ControlDecoration(themeIdCombo.getCombo(), SWT.TOP | SWT.LEFT);

		Button manageDefaultButton = new Button(themeComposite, SWT.PUSH);
		manageDefaultButton.setText(WorkbenchMessages.ThemeDefault_manageButton);
		manageDefaultButton.addSelectionListener(widgetSelectedAdapter(e -> openManageDefaultThemeDialog()));

		themeIdCombo.addSelectionChangedListener(event -> {
			ITheme selection = getSelectedTheme();
			if (!selection.equals(currentTheme)) {
				themeComboDecorator.setDescriptionText(WorkbenchMessages.ThemeChangeWarningText);
				Image decorationImage = FieldDecorationRegistry.getDefault()
						.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage();
				themeComboDecorator.setImage(decorationImage);
				themeComboDecorator.show();
			} else {
				themeComboDecorator.hide();
			}
			selectColorsAndFontsTheme(getColorAndFontThemeIdByThemeId(selection.getId()));
		});

		currentColorsAndFontsTheme = getCurrentColorsAndFontsTheme();
		createColorsAndFontsThemeCombo(comp);

		createThemeIndependentComposits(comp);

		// Theme dependent controls for Tab icons and titles in view areas
		createShowFullTextForViewTabs(comp);
		createHideIconsForViewTabs(comp);
		createDependency(showFullTextForViewTabs, hideIconsForViewTabs);

		createShowDirtyIndicatorForTabs(comp);

		createRescaleAtRuntimeCheckButton(comp);

		if (currentTheme != null) {
			String colorsAndFontsThemeId = getColorAndFontThemeIdByThemeId(currentTheme.getId());
			if (colorsAndFontsThemeId != null && !currentColorsAndFontsTheme.getId().equals(colorsAndFontsThemeId)) {
				colorsAndFontsThemeId = currentColorsAndFontsTheme.getId();
			}
			selectColorsAndFontsTheme(colorsAndFontsThemeId);
		}

		Dialog.applyDialogFont(comp);
		return comp;
	}

	/**
	 * @return <code>true</code> if there is a theme engine set (<i>i.e.</i> if the
	 *         workbench started with the checkbox "enable theming" set to
	 *         <code>true</code>) and the <i>high contrast mode</i> is
	 *         <strong>disabled on the OS.
	 *
	 * @implNote Currently only Windows is able to tell if <i>high contrast mode</i>
	 *           is active. Linux and Mac lack this functionality (they always say
	 *           it is <b>disabled</b>).
	 *
	 */
	private boolean isThemingPossible() {
		return engine != null && !highContrastMode;
	}

	@SuppressWarnings("restriction")
	private void createRescaleAtRuntimeCheckButton(Composite parent) {
		if (!OS.isWindows()) {
			return;
		}
		createLabel(parent, ""); //$NON-NLS-1$

		boolean initialStateRescaleAtRuntime = ConfigurationScope.INSTANCE.getNode(WorkbenchPlugin.PI_WORKBENCH)
				.getBoolean(IWorkbenchPreferenceConstants.RESCALING_AT_RUNTIME, true);
		rescaleAtRuntime = createCheckButton(parent, WorkbenchMessages.RescaleAtRuntimeEnabled,
				initialStateRescaleAtRuntime);
		if (!DPIUtil.isSetupCompatibleToMonitorSpecificScaling()) {
			rescaleAtRuntime.setEnabled(false);
			Font font = parent.getFont();
			Composite note = createNoteComposite(font, parent, WorkbenchMessages.Preference_note,
					WorkbenchMessages.RescaleAtRuntimeDisabledDescription);
			note.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
		} else {
			rescaleAtRuntime.setToolTipText(WorkbenchMessages.RescaleAtRuntimeDescription);
		}
	}

	private void createThemeIndependentComposits(Composite comp) {
		createColoredLabelsPref(comp);
		createEnableMruPref(comp);
	}

	protected void createShowFullTextForViewTabs(Composite composite) {
		boolean actualValue = getSwtRendererPreference(CTabRendering.SHOW_FULL_TEXT_FOR_VIEW_TABS,
				CTabRendering.SHOW_FULL_TEXT_FOR_VIEW_TABS_DEFAULT);
		createLabel(composite, ""); //$NON-NLS-1$
		createLabel(composite, WorkbenchMessages.ViewsPreference_viewTabs_icons_and_titles_label);
		showFullTextForViewTabs = createCheckButton(composite,
				WorkbenchMessages.ViewsPreference_showFullTextForViewTabs, actualValue);
	}

	protected void createHideIconsForViewTabs(Composite composite) {
		boolean actualValue = getSwtRendererPreference(CTabRendering.HIDE_ICONS_FOR_VIEW_TABS,
				CTabRendering.HIDE_ICONS_FOR_VIEW_TABS_DEFAULT);
		hideIconsForViewTabs = createCheckButton(composite, WorkbenchMessages.ViewsPreference_hideIconsForViewTabs,
				actualValue);
	}

	protected void createShowDirtyIndicatorForTabs(Composite composite) {
		boolean actualValue = getSwtRendererPreference(CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS,
				CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS_DEFAULT);
		createLabel(composite, ""); //$NON-NLS-1$
		createLabel(composite, WorkbenchMessages.ViewsPreference_viewTabs_dirty_indicator_label);
		showDirtyIndicatorForTabs = createCheckButton(composite,
				WorkbenchMessages.ViewsPreference_showDirtyIndicatorForTabs, actualValue);
	}

	private boolean getSwtRendererPreference(String prefName, boolean defaultValue) {
		return Platform.getPreferencesService().getBoolean(CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT,
				prefName, defaultValue, null);
	}

	/**
	 * @param showFullTextForViewTabs
	 * @param hideIconsForViewTabs
	 */
	private void createDependency(Button parent, Button dependent) {
		GridData gridData = new GridData();
		gridData.horizontalIndent = 20;
		dependent.setLayoutData(gridData);
		boolean parentState = parent.getSelection();
		dependent.setEnabled(parentState);

		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state = parent.getSelection();
				dependent.setEnabled(state);
				if (!state) {
					dependent.setSelection(state);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		parent.addSelectionListener(listener);
	}

	private void createColoredLabelsPref(Composite composite) {
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		useColoredLabels = createCheckButton(composite, WorkbenchMessages.ViewsPreference_useColoredLabels,
				apiStore.getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS));
	}

	private Button createCheckButton(Composite composite, String text, boolean selection) {
		Button button = new Button(composite, SWT.CHECK);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1);
		button.setLayoutData(data);
		button.setText(text);
		button.setSelection(selection);
		return button;
	}

	private Label createLabel(Composite composite, String text) {
		Label label = new Label(composite, SWT.NONE);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1);
		label.setLayoutData(data);
		label.setText(text);
		return label;
	}

	protected void createEnableMruPref(Composite composite) {
		createLabel(composite, ""); //$NON-NLS-1$
		createLabel(composite, WorkbenchMessages.ViewsPreference_visibleTabs_description);
		boolean defaultValue = getSwtRendererPreference(StackRenderer.MRU_KEY_DEFAULT, StackRenderer.MRU_DEFAULT);
		boolean actualValue = getSwtRendererPreference(StackRenderer.MRU_KEY, defaultValue);
		enableMru = createCheckButton(composite, WorkbenchMessages.ViewsPreference_enableMRU, actualValue);
	}

	/** @return the currently selected theme or null if there are no themes */
	private ITheme getSelectedTheme() {
		return (ITheme) (themeIdCombo.getStructuredSelection().getFirstElement());
	}

	private void openManageDefaultThemeDialog() {
		String productOrAppId = getProductOrApplicationId();
		IEclipsePreferences baseNode = UserScope.INSTANCE.getNode(E4_THEME_EXTENSION_POINT);
		IEclipsePreferences scopedNode = productOrAppId != null ? (IEclipsePreferences) baseNode.node(productOrAppId)
				: baseNode;
		String currentDefaultId = scopedNode.get("themeid", null); //$NON-NLS-1$

		String currentDefaultLabel = null;
		if (currentDefaultId != null) {
			for (ITheme t : engine.getThemes()) {
				if (t.getId().equals(currentDefaultId)) {
					currentDefaultLabel = t.getLabel();
					break;
				}
			}
			if (currentDefaultLabel == null) {
				currentDefaultLabel = currentDefaultId;
			}
		}

		String productDisplayName = getProductDisplayName();

		String message;
		if (currentDefaultLabel != null) {
			if (productOrAppId != null) {
				String displayName = productDisplayName != null ? productDisplayName : productOrAppId;
				message = NLS.bind(WorkbenchMessages.ThemeDefault_currentDefault, new Object[] { currentDefaultLabel,
						displayName, productOrAppId });
			} else {
				message = NLS.bind(WorkbenchMessages.ThemeDefault_currentDefaultUnscoped, currentDefaultLabel);
			}
		} else {
			if (productOrAppId != null) {
				String displayName = productDisplayName != null ? productDisplayName : productOrAppId;
				message = NLS.bind(WorkbenchMessages.ThemeDefault_noDefault, displayName, productOrAppId);
			} else {
				message = WorkbenchMessages.ThemeDefault_noDefaultUnscoped;
			}
		}
		message = WorkbenchMessages.ThemeDefault_description + "\n\n" + message; //$NON-NLS-1$

		ITheme selectedTheme = getSelectedTheme();
		List<String> buttonLabels = new ArrayList<>();
		buttonLabels.add(WorkbenchMessages.ThemeDefault_setDefault);
		if (currentDefaultId != null) {
			buttonLabels.add(WorkbenchMessages.ThemeDefault_removeDefault);
		}
		buttonLabels.add(IDialogConstants.CLOSE_LABEL);

		MessageDialog dialog = new MessageDialog(getShell(), WorkbenchMessages.ThemeDefault_dialogTitle, null, message,
				MessageDialog.INFORMATION, 0, buttonLabels.toArray(new String[0]));

		int result = dialog.open();
		if (result == 0 && selectedTheme != null) {
			// Set as default
			scopedNode.put("themeid", selectedTheme.getId()); //$NON-NLS-1$
			try {
				scopedNode.flush();
			} catch (BackingStoreException e) {
				WorkbenchPlugin.log("Failed to set default theme in user scope", e); //$NON-NLS-1$
			}
		} else if (currentDefaultId != null && result == 1) {
			// Remove default
			scopedNode.remove("themeid"); //$NON-NLS-1$
			try {
				scopedNode.flush();
			} catch (BackingStoreException e) {
				WorkbenchPlugin.log("Failed to remove default theme from user scope", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Returns the product ID if a product is configured, otherwise falls back to
	 * the application ID from the system property. Returns {@code null} if
	 * neither is available.
	 */
	private static String getProductOrApplicationId() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			return product.getId();
		}
		return System.getProperty("eclipse.application"); //$NON-NLS-1$
	}

	/**
	 * Returns the product name if a product is configured, or {@code null}
	 * otherwise.
	 */
	private static String getProductDisplayName() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			return product.getName();
		}
		return null;
	}

	@Override
	public void init(IWorkbench workbench) {
		MApplication application = workbench.getService(MApplication.class);
		IEclipseContext context = application.getContext();
		defaultTheme = (String) context.get(E4Application.THEME_ID);
		engine = context.get(IThemeEngine.class);
	}

	@Override
	public boolean performOk() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode(CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		if (isThemingPossible()) {
			ITheme theme = getSelectedTheme();
			if (theme != null) {
				engine.setTheme(getSelectedTheme(), true);
			}
			prefs.putBoolean(CTabRendering.HIDE_ICONS_FOR_VIEW_TABS, hideIconsForViewTabs.getSelection());
			prefs.putBoolean(CTabRendering.SHOW_FULL_TEXT_FOR_VIEW_TABS, showFullTextForViewTabs.getSelection());
			prefs.putBoolean(CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS, showDirtyIndicatorForTabs.getSelection());
		}

		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
		apiStore.setValue(IWorkbenchPreferenceConstants.USE_COLORED_LABELS, useColoredLabels.getSelection());

		prefs.putBoolean(StackRenderer.MRU_KEY, enableMru.getSelection());
		boolean themingEnabledChanged = false;
		// Only if the setting is modifiable by the user does checking for it (and
		// storing it) make sense
		if (themingEnabled.isEnabled()) {
			themingEnabledChanged = prefs.getBoolean(PartRenderingEngine.ENABLED_THEME_KEY, true) != themingEnabled
					.getSelection();
			prefs.putBoolean(PartRenderingEngine.ENABLED_THEME_KEY, themingEnabled.getSelection());
		}

		boolean isRescaleAtRuntimeChanged = false;
		if (rescaleAtRuntime != null) {
			IEclipsePreferences configurationScopeNode = ConfigurationScope.INSTANCE
					.getNode(WorkbenchPlugin.PI_WORKBENCH);
			boolean initialStateRescaleAtRuntime = configurationScopeNode
					.getBoolean(IWorkbenchPreferenceConstants.RESCALING_AT_RUNTIME, true);
			isRescaleAtRuntimeChanged = initialStateRescaleAtRuntime != rescaleAtRuntime.getSelection();
			configurationScopeNode.putBoolean(IWorkbenchPreferenceConstants.RESCALING_AT_RUNTIME,
					rescaleAtRuntime.getSelection());
			try {
				configurationScopeNode.flush();
			} catch (BackingStoreException e) {
			}
		}

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			WorkbenchPlugin.log("Failed to set SWT renderer preferences", e); //$NON-NLS-1$
		}

		boolean showRestartDialog = false;
		String restartDialogTitle = null;
		String restartDialogMessage = null;

		if (isThemingPossible()) {
			ITheme theme = getSelectedTheme();
			boolean themeChanged = theme != null && !theme.equals(currentTheme);
			boolean colorsAndFontsThemeChanged = !PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getId()
					.equals(currentColorsAndFontsTheme.getId());

			if (theme != null) {
				currentTheme = theme;
			}

			ColorsAndFontsTheme colorsAndFontsTheme = getSelectedColorsAndFontsTheme();
			if (colorsAndFontsTheme != null) {
				currentColorsAndFontsTheme = colorsAndFontsTheme;
			}

			themeComboDecorator.hide();
			colorFontsDecorator.hide();

			if (themeChanged || colorsAndFontsThemeChanged) {
				showRestartDialog = true;
				restartDialogTitle = WorkbenchMessages.ThemeChangeWarningTitle;
				restartDialogMessage = WorkbenchMessages.ThemeChangeWarningText;
			}
		}
		if (themingEnabledChanged) {
			showRestartDialog = true;
			restartDialogTitle = WorkbenchMessages.ThemeChangeWarningTitle;
			restartDialogMessage = WorkbenchMessages.ThemeChangeWarningText;
		}
		if (isRescaleAtRuntimeChanged) {
			showRestartDialog = true;
			restartDialogTitle = WorkbenchMessages.RescaleAtRuntimeSettingChangeWarningTitle;
			restartDialogMessage = WorkbenchMessages.RescaleAtRuntimeSettingChangeWarningText;
		}

		if (showRestartDialog) {
			String themeId = null;
			if (isThemingPossible()) {
				ITheme theme = getSelectedTheme();
				if (theme != null) {
					themeId = theme.getId();
				}
			}
			showRestartDialog(restartDialogTitle, restartDialogMessage, themeId);
		}

		return super.performOk();
	}

	private void showRestartDialog(String title, String warningText, String themeId) {
		boolean[] useAsDefault = { true };
		MessageDialog dialog = new MessageDialog(null, title, null, warningText, MessageDialog.NONE, 2,
				WorkbenchMessages.Workbench_RestartButton, WorkbenchMessages.Workbench_DontRestartButton) {
			@Override
			protected Control createCustomArea(Composite parent) {
				if (themeId == null) {
					return null;
				}
				Button checkbox = new Button(parent, SWT.CHECK);
				checkbox.setText(WorkbenchMessages.ThemeChange_useAsDefault);
				checkbox.setSelection(useAsDefault[0]);
				checkbox.addSelectionListener(widgetSelectedAdapter(e -> useAsDefault[0] = checkbox.getSelection()));
				return checkbox;
			}
		};
		int result = dialog.open();
		if (result == 0 || result == 1) { // 0: Restart, 1: Don't Restart
			if (themeId != null && useAsDefault[0]) {
				IEclipsePreferences baseNode = UserScope.INSTANCE
						.getNode(E4_THEME_EXTENSION_POINT);
				String productOrAppId = getProductOrApplicationId();
				IEclipsePreferences scopedNode = productOrAppId != null
						? (IEclipsePreferences) baseNode.node(productOrAppId)
						: baseNode;
				scopedNode.put("themeid", themeId); //$NON-NLS-1$
				try {
					scopedNode.flush();
				} catch (BackingStoreException e) {
					WorkbenchPlugin.log("Failed to set default theme in user scope", e); //$NON-NLS-1$
				}
			}
		}
		if (result == 0) {
			Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().restart());
		}
	}

	private void setColorsAndFontsTheme(ColorsAndFontsTheme theme) {
		org.eclipse.ui.themes.ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		if (theme != null && !currentTheme.getId().equals(theme.getId())) {
			PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(theme.getId());
		}
	}

	@Override
	protected void performDefaults() {
		IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE
				.getNode(CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		if (isThemingPossible()) {
			setColorsAndFontsTheme(currentColorsAndFontsTheme);

			engine.setTheme(defaultTheme, true);
			if (engine.getActiveTheme() != null) {
				themeIdCombo.setSelection(new StructuredSelection(engine.getActiveTheme()));
			}
			hideIconsForViewTabs.setSelection(defaultPrefs.getBoolean(CTabRendering.HIDE_ICONS_FOR_VIEW_TABS,
					CTabRendering.HIDE_ICONS_FOR_VIEW_TABS_DEFAULT));
			showFullTextForViewTabs.setSelection(defaultPrefs.getBoolean(CTabRendering.SHOW_FULL_TEXT_FOR_VIEW_TABS,
					CTabRendering.SHOW_FULL_TEXT_FOR_VIEW_TABS_DEFAULT));
			showFullTextForViewTabs.notifyListeners(SWT.Selection, null);
			showDirtyIndicatorForTabs.setSelection(defaultPrefs.getBoolean(CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS,
					CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS_DEFAULT));
		}
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
		useColoredLabels.setSelection(apiStore.getDefaultBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS));

		enableMru.setSelection(defaultPrefs.getBoolean(StackRenderer.MRU_KEY_DEFAULT, StackRenderer.MRU_DEFAULT));
		super.performDefaults();
	}

	@Override
	public boolean performCancel() {
		if (isThemingPossible()) {
			setColorsAndFontsTheme(currentColorsAndFontsTheme);

			if (currentTheme != null && !currentTheme.equals(engine.getActiveTheme())) {
				engine.setTheme(currentTheme, false);
			}
		}

		return super.performCancel();
	}

	private void createColorsAndFontsThemeCombo(Composite composite) {
		new Label(composite, SWT.NONE).setText(WorkbenchMessages.ViewsPreference_currentTheme);
		colorsAndFontsThemeCombo = new ComboViewer(composite, SWT.READ_ONLY);
		colorsAndFontsThemeCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		colorsAndFontsThemeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ColorsAndFontsTheme) element).getLabel();
			}
		});
		colorFontsDecorator = new ControlDecoration(colorsAndFontsThemeCombo.getCombo(), SWT.TOP | SWT.LEFT);
		colorsAndFontsThemeCombo.setContentProvider(ArrayContentProvider.getInstance());
		colorsAndFontsThemeCombo.setInput(getColorsAndFontsThemes());
		colorsAndFontsThemeCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		colorsAndFontsThemeCombo.addSelectionChangedListener(event -> {
			ColorsAndFontsTheme colorsAndFontsTheme = getSelectedColorsAndFontsTheme();
			if (!colorsAndFontsTheme.equals(currentColorsAndFontsTheme)) {
				Image decorationImage = FieldDecorationRegistry.getDefault()
						.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage();
				colorFontsDecorator.setImage(decorationImage);
				colorFontsDecorator.setDescriptionText(WorkbenchMessages.ThemeChangeWarningText);
				colorFontsDecorator.show();
			} else {
				colorFontsDecorator.hide();
			}
			setColorsAndFontsTheme(colorsAndFontsTheme);
		});
	}

	private void selectColorsAndFontsTheme(String colorAndFontThemeId) {
		if (colorAndFontThemeId == null) {
			colorAndFontThemeId = currentColorsAndFontsTheme.getId();
		}

		for (ColorsAndFontsTheme theme : (List<ColorsAndFontsTheme>) colorsAndFontsThemeCombo.getInput()) {
			if (theme.getId().equals(colorAndFontThemeId)) {
				ISelection selection = new StructuredSelection(theme);
				colorsAndFontsThemeCombo.setSelection(selection);
				break;
			}
		}
	}

	private String getColorAndFontThemeIdByThemeId(String themeId) {
		if (themeAssociations == null) {
			themeAssociations = createThemeAssociations();
		}

		// first get by exact matching (together with os_version)
		String result = themeAssociations.get(themeId);

		if (result == null) {
			for (Map.Entry<String, String> entry : themeAssociations.entrySet()) {
				if (themeId.startsWith(entry.getKey())) {
					return entry.getValue();
				}
			}
		}

		return result;
	}

	private Map<String, String> createThemeAssociations() {
		Map<String, String> result = new HashMap<>();
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint(E4_THEME_EXTENSION_POINT);

		for (IExtension e : extPoint.getExtensions()) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				if (ce.getName().equals(ATT_THEME_ASSOCIATION)) {
					String themeId = ce.getAttribute(ATT_THEME_ID);
					String osVersion = ce.getAttribute(ATT_OS_VERSION);
					result.put(osVersion != null ? themeId + osVersion : themeId,
							ce.getAttribute(ATT_COLOR_AND_FONT_ID));
				}
			}
		}
		return result;
	}

	private List<ColorsAndFontsTheme> getColorsAndFontsThemes() {
		List<ColorsAndFontsTheme> result = new ArrayList<>();
		org.eclipse.ui.themes.ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();

		IThemeDescriptor[] descs = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes();
		String defaultThemeString = PlatformUI.getWorkbench().getThemeManager().getTheme(IThemeManager.DEFAULT_THEME)
				.getLabel();
		if (currentTheme.getId().equals(IThemeManager.DEFAULT_THEME)) {
			defaultThemeString = NLS.bind(WorkbenchMessages.ViewsPreference_currentThemeFormat,
					new Object[] { defaultThemeString });
		}
		result.add(new ColorsAndFontsTheme(IThemeManager.DEFAULT_THEME, defaultThemeString));

		String themeString;
		for (IThemeDescriptor themeDescriptor : descs) {
			themeString = themeDescriptor.getName();
			if (themeDescriptor.getId().equals(currentTheme.getId())) {
				themeString = NLS.bind(WorkbenchMessages.ViewsPreference_currentThemeFormat,
						new Object[] { themeString });
			}
			result.add(new ColorsAndFontsTheme(themeDescriptor.getId(), themeString));
		}
		return result;
	}

	private ColorsAndFontsTheme getSelectedColorsAndFontsTheme() {
		return (ColorsAndFontsTheme) colorsAndFontsThemeCombo.getStructuredSelection().getFirstElement();
	}

	private ColorsAndFontsTheme getCurrentColorsAndFontsTheme() {
		org.eclipse.ui.themes.ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();

		return new ColorsAndFontsTheme(theme.getId(), theme.getLabel());
	}

	private static class ColorsAndFontsTheme {
		private final String label;
		private final String id;

		public ColorsAndFontsTheme(String id, String label) {
			this.id = id;
			this.label = label;
		}

		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ColorsAndFontsTheme other = (ColorsAndFontsTheme) obj;
			return Objects.equals(id, other.id);
		}

	}

}
