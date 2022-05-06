/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_COLOR_AND_FONT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_OS_VERSION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_THEME_ASSOCIATION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_THEME_ID;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.renderers.swt.CTabRendering;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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
	private Button useRoundTabs;
	private Button enableMru;
	private Button useColoredLabels;

	private ComboViewer colorsAndFontsThemeCombo;
	private ControlDecoration colorFontsDecorator;
	private ColorsAndFontsTheme currentColorsAndFontsTheme;
	private Map<String, String> themeAssociations;
	private boolean highContrastMode;

	private Button themingEnabled;

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite comp = new Composite(parent, SWT.NONE);

		themingEnabled = createCheckButton(comp, WorkbenchMessages.ThemingEnabled, engine != null);

		// if started with "-cssTheme none", CSS settings should be disabled
		// but other appearance settings should be *not* disabled
		if (engine == null) {
			GridLayout layout = new GridLayout(1, false);
			layout.horizontalSpacing = 10;
			comp.setLayout(layout);
			createThemeIndependentComposits(comp);
			return comp;
		}

		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		comp.setLayout(layout);

		new Label(comp, SWT.NONE).setText(WorkbenchMessages.ViewsPreferencePage_Theme);
		highContrastMode = parent.getDisplay().getHighContrast();

		themeIdCombo = new ComboViewer(comp, SWT.READ_ONLY);
		themeIdCombo.setLabelProvider(createTextProvider(element -> ((ITheme) element).getLabel()));
		themeIdCombo.setContentProvider(ArrayContentProvider.getInstance());
		themeIdCombo.setInput(getCSSThemes(highContrastMode));
		themeIdCombo.getCombo().setEnabled(!highContrastMode);
		themeIdCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.currentTheme = engine.getActiveTheme();
		if (this.currentTheme != null) {
			themeIdCombo.setSelection(new StructuredSelection(currentTheme));
		}
		themeComboDecorator = new ControlDecoration(themeIdCombo.getCombo(), SWT.TOP | SWT.LEFT);
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

	private void createThemeIndependentComposits(Composite comp) {
		createUseRoundTabs(comp);
		createColoredLabelsPref(comp);
		createEnableMruPref(comp);
	}

	private List<ITheme> getCSSThemes(boolean highContrastMode) {
		ArrayList<ITheme> themes = new ArrayList<>();
		for (ITheme theme : engine.getThemes()) {
			/*
			 * When we have Win32 OS - when the high contrast mode is enabled on the
			 * platform, we display the 'high-contrast' special theme only. If not, we don't
			 * want to mess the themes combo with the theme since it is the special
			 * variation of the 'classic' one
			 *
			 * When we have GTK - we have to display the entire list of the themes since we
			 * are not able to figure out if the high contrast mode is enabled on the
			 * platform. The user has to manually select the theme if they need it
			 */
			if (!highContrastMode && !Util.isGtk() && theme.getId().equals(E4Application.HIGH_CONTRAST_THEME_ID)) {
				continue;
			}
			themes.add(theme);
		}
		Collator collator = Collator.getInstance(Locale.getDefault());
		themes.sort((ITheme t1, ITheme t2) -> collator.compare(t1.getLabel(), t2.getLabel()));
		return themes;
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

	protected void createUseRoundTabs(Composite composite) {
		IEclipsePreferences prefs = getSwtRendererPreferences();
		boolean enabled = prefs.getBoolean(CTabRendering.USE_ROUND_TABS, CTabRendering.USE_ROUND_TABS_DEFAULT);
		useRoundTabs = createCheckButton(composite, WorkbenchMessages.ViewsPreference_useRoundTabs, enabled);
	}

	protected void createEnableMruPref(Composite composite) {
		createLabel(composite, ""); //$NON-NLS-1$
		createLabel(composite, WorkbenchMessages.ViewsPreference_visibleTabs_description);
		IEclipsePreferences prefs = getSwtRendererPreferences();
		boolean defaultValue = getDefaultMRUValue();
		boolean actualValue = prefs.getBoolean(StackRenderer.MRU_KEY, defaultValue);
		enableMru = createCheckButton(composite, WorkbenchMessages.ViewsPreference_enableMRU, actualValue);
	}

	/** @return the currently selected theme or null if there are no themes */
	private ITheme getSelectedTheme() {
		return (ITheme) (themeIdCombo.getStructuredSelection().getFirstElement());
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
		if (engine != null) {
			ITheme theme = getSelectedTheme();
			if (theme != null) {
				engine.setTheme(getSelectedTheme(), !highContrastMode);
			}
		}

		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
		apiStore.setValue(IWorkbenchPreferenceConstants.USE_COLORED_LABELS, useColoredLabels.getSelection());

		IEclipsePreferences prefs = getSwtRendererPreferences();
		prefs.putBoolean(StackRenderer.MRU_KEY, enableMru.getSelection());
		boolean themingEnabledChanged = prefs.getBoolean(PartRenderingEngine.ENABLED_THEME_KEY, true) != themingEnabled
				.getSelection();
		prefs.putBoolean(PartRenderingEngine.ENABLED_THEME_KEY, themingEnabled.getSelection());

		prefs.putBoolean(CTabRendering.USE_ROUND_TABS, useRoundTabs.getSelection());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			WorkbenchPlugin.log("Failed to set SWT renderer preferences", e); //$NON-NLS-1$
		}

		if (engine != null) {
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
				showRestartDialog();
			}
		}
		if (themingEnabledChanged) {
			showRestartDialog();
		}

		return super.performOk();
	}

	/**
	 *
	 */
	private void showRestartDialog() {
		if (new MessageDialog(null, WorkbenchMessages.ThemeChangeWarningTitle, null,
				WorkbenchMessages.ThemeChangeWarningText, MessageDialog.QUESTION, 2,
				WorkbenchMessages.Workbench_RestartButton, WorkbenchMessages.Workbench_DontRestartButton)
						.open() == Window.OK) {
			Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().restart());
		}
	}

	private IEclipsePreferences getSwtRendererPreferences() {
		return InstanceScope.INSTANCE.getNode("org.eclipse.e4.ui.workbench.renderers.swt"); //$NON-NLS-1$
	}

	private boolean getDefaultMRUValue() {
		return getSwtRendererPreferences().getBoolean(StackRenderer.MRU_KEY_DEFAULT, StackRenderer.MRU_DEFAULT);
	}

	private void setColorsAndFontsTheme(ColorsAndFontsTheme theme) {
		org.eclipse.ui.themes.ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		if (theme != null && !currentTheme.getId().equals(theme.getId())) {
			PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(theme.getId());
		}
	}

	@Override
	protected void performDefaults() {
		if (engine != null) {
			setColorsAndFontsTheme(currentColorsAndFontsTheme);

			engine.setTheme(defaultTheme, true);
			if (engine.getActiveTheme() != null) {
				themeIdCombo.setSelection(new StructuredSelection(engine.getActiveTheme()));
			}
		}
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
		useColoredLabels.setSelection(apiStore.getDefaultBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS));

		useRoundTabs.setSelection(CTabRendering.USE_ROUND_TABS_DEFAULT);
		enableMru.setSelection(getDefaultMRUValue());
		super.performDefaults();
	}

	@Override
	public boolean performCancel() {
		if (engine != null) {
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
			} else
				colorFontsDecorator.hide();
			setColorsAndFontsTheme(colorsAndFontsTheme);
		});
	}

	@SuppressWarnings("unchecked")
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
		private String label;
		private String id;

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
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColorsAndFontsTheme other = (ColorsAndFontsTheme) obj;
			return Objects.equals(id, other.id);
		}

	}

}
