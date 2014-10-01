/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Erik Chou <ekchou@ymail.com> - Bug 425962
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445664, 442278
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_COLOR_AND_FONT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_OS_VERSION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_THEME_ASSOCIATION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_THEME_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.themes.IThemeDescriptor;
import org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.themes.IThemeManager;

/**
 * The ViewsPreferencePage is the page used to set preferences for the
 * appearance of the workbench. Originally this applied only to views but now
 * applies to the overall appearance, hence the name.
 */
public class ViewsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private static final String E4_THEME_EXTENSION_POINT = "org.eclipse.e4.ui.css.swt.theme"; //$NON-NLS-1$

	/** The workbench theme engine; may be {@code null} if no engine */
	private IThemeEngine engine;
	private ComboViewer themeIdCombo;
	private ControlDecoration themeComboDecorator;
	private ITheme currentTheme;
	private String defaultTheme;
	private Button enableAnimations;
	private Button useColoredLabels;
	
	private Text colorsAndFontsThemeDescriptionText;
	private ComboViewer colorsAndFontsThemeCombo;
	private ControlDecoration colorFontsDecorator;
	private ColorsAndFontsTheme currentColorsAndFontsTheme;
	private Map<String, String> themeAssociations;
	private boolean highContrastMode;

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		comp.setLayout(layout);
		if (engine == null) {
			new Label(comp, SWT.NONE).setText(WorkbenchMessages.ThemingDisabled);
			return comp;
		}
		new Label(comp, SWT.NONE).setText(WorkbenchMessages.ViewsPreferencePage_Theme);
		highContrastMode = parent.getDisplay().getHighContrast();

		themeIdCombo = new ComboViewer(comp, SWT.READ_ONLY);
		themeIdCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ITheme) element).getLabel();
			}
		});
		themeIdCombo.setContentProvider(new ArrayContentProvider());
		themeIdCombo.setInput(getCSSThemes(highContrastMode));
		themeIdCombo.getCombo().setEnabled(!highContrastMode);
		themeIdCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.currentTheme = engine.getActiveTheme();
		if (this.currentTheme != null) {
			themeIdCombo.setSelection(new StructuredSelection(currentTheme));
		}
		themeComboDecorator = new ControlDecoration(themeIdCombo.getCombo(), SWT.TOP | SWT.LEFT);
		themeIdCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
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
				try {
					((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY))
							.setSelection(selection);
				} catch (SWTException e) {
					WorkbenchPlugin.log("Failed to set CSS preferences", e); //$NON-NLS-1$
				}
				selectColorsAndFontsTheme(getColorAndFontThemeIdByThemeId(selection.getId()));
			}
		});

		currentColorsAndFontsTheme = getCurrentColorsAndFontsTheme();
		createColorsAndFontsThemeCombo(comp);
		createColorsAndFontsThemeDescriptionText(comp);
		createEnableAnimationsPref(comp);
		createColoredLabelsPref(comp);

		((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY))
				.setSelection(currentTheme);
		((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY)).createContents(comp);

		if (currentTheme != null) {
			String colorsAndFontsThemeId = getColorAndFontThemeIdByThemeId(currentTheme
					.getId());
			if (colorsAndFontsThemeId != null
					&& !currentColorsAndFontsTheme.getId().equals(colorsAndFontsThemeId)) {
				colorsAndFontsThemeId = currentColorsAndFontsTheme.getId();
			}
			selectColorsAndFontsTheme(colorsAndFontsThemeId);
		}

		return comp;
	}

	private List<ITheme> getCSSThemes(boolean highContrastMode) {
		List<ITheme> themes = new ArrayList<ITheme>();
		for (ITheme theme : engine.getThemes()) {
			/*
			 * When we have Win32 OS - when the high contrast mode is enabled on
			 * the platform, we display the 'high-contrast' special theme only.
			 * If not, we don't want to mess the themes combo with the theme
			 * since it is the special variation of the 'classic' one
			 * 
			 * When we have GTK - we have to display the entire list of the
			 * themes since we are not able to figure out if the high contrast
			 * mode is enabled on the platform. The user has to manually select
			 * the theme if they need it
			 */
			if (!highContrastMode && !Util.isGtk()
					&& theme.getId().equals(E4Application.HIGH_CONTRAST_THEME_ID)) {
				continue;
			}
			themes.add(theme);
		}
		return themes;
	}

	private void createColoredLabelsPref(Composite composite) {
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		useColoredLabels = createCheckButton(composite,
				WorkbenchMessages.ViewsPreference_useColoredLabels,
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

	protected void createEnableAnimationsPref(Composite composite) {
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();

		enableAnimations = createCheckButton(composite,
				WorkbenchMessages.ViewsPreference_enableAnimations,
				apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS));
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

			IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
			apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
					enableAnimations.getSelection());
			apiStore.setValue(IWorkbenchPreferenceConstants.USE_COLORED_LABELS,
					useColoredLabels.getSelection());
			((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY)).performOK();
		}
		return super.performOk();
	}

	private void setColorsAndFontsTheme(ColorsAndFontsTheme theme) {
		org.eclipse.ui.themes.ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme();
		if (theme != null && !currentTheme.getId().equals(theme.getId())) {
			PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(theme.getId());
		}
	}

	@Override
	protected void performDefaults() {
		if (engine != null) {
			setColorsAndFontsTheme(currentColorsAndFontsTheme);

			((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY)).performDefaults();
			engine.setTheme(defaultTheme, true);
			if (engine.getActiveTheme() != null) {
				themeIdCombo.setSelection(new StructuredSelection(engine.getActiveTheme()));
			}
			IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
			enableAnimations.setSelection(apiStore
					.getDefaultBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS));
			useColoredLabels.setSelection(apiStore
					.getDefaultBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS));
		}
		super.performDefaults();
	}

	@Override
	public boolean performCancel() {
		if (engine != null) {
			setColorsAndFontsTheme(currentColorsAndFontsTheme);

			if (currentTheme != null) {
				engine.setTheme(currentTheme, false);
			}
		}

		return super.performCancel();
	}

	@Override
	protected void performApply() {
		super.performApply();
		if (engine != null) {
			ITheme theme = getSelectedTheme();
			if (theme != null) {
				currentTheme = theme;
			}

			ColorsAndFontsTheme colorsAndFontsTheme = getSelectedColorsAndFontsTheme();
			if (colorsAndFontsTheme != null) {
				currentColorsAndFontsTheme = colorsAndFontsTheme;
			}

			themeComboDecorator.hide();
			colorFontsDecorator.hide();
		}
	}

	private void createColorsAndFontsThemeCombo(Composite composite) {
		new Label(composite, SWT.NONE).setText(WorkbenchMessages.ViewsPreference_currentTheme);
		colorsAndFontsThemeCombo = new ComboViewer(composite, SWT.READ_ONLY);
		colorsAndFontsThemeCombo.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false));
		colorsAndFontsThemeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ColorsAndFontsTheme) element).getLabel();
			}
		});
		colorFontsDecorator = new ControlDecoration(colorsAndFontsThemeCombo.getCombo(), SWT.TOP
				| SWT.LEFT);
		colorsAndFontsThemeCombo.setContentProvider(new ArrayContentProvider());
		colorsAndFontsThemeCombo.setInput(getColorsAndFontsThemes());
		colorsAndFontsThemeCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		colorsAndFontsThemeCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ColorsAndFontsTheme colorsAndFontsTheme = getSelectedColorsAndFontsTheme();
				if (!colorsAndFontsTheme.equals(currentColorsAndFontsTheme)) {
					Image decorationImage = FieldDecorationRegistry.getDefault()
							.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage();
					colorFontsDecorator.setImage(decorationImage);
					colorFontsDecorator
							.setDescriptionText(WorkbenchMessages.ThemeChangeWarningText);
					colorFontsDecorator.show();
				} else
					colorFontsDecorator.hide();
				refreshColorsAndFontsThemeDescriptionText(colorsAndFontsTheme);
				setColorsAndFontsTheme(colorsAndFontsTheme);
			}
		});		
	}

	/**
	 * Create the text box that will contain the current theme description text
	 * (if any).
	 * 
	 * @param parent
	 *            the parent <code>Composite</code>.
	 */
	private void createColorsAndFontsThemeDescriptionText(Composite parent) {
		new Label(parent, SWT.NONE)
				.setText(WorkbenchMessages.ViewsPreference_currentThemeDescription);

		colorsAndFontsThemeDescriptionText = new Text(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.READ_ONLY
				| SWT.BORDER | SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// give a height hint that'll show at least two lines (and let the
		// scroll bars draw nicely if necessary)
		GC gc = new GC(parent);
		layoutData.heightHint = Dialog.convertHeightInCharsToPixels(gc.getFontMetrics(), 2);
		gc.dispose();
		colorsAndFontsThemeDescriptionText.setLayoutData(layoutData);
	}

	@SuppressWarnings("unchecked")
	private void selectColorsAndFontsTheme(String colorAndFontThemeId) {
		if (colorAndFontThemeId == null) {
			colorAndFontThemeId = currentColorsAndFontsTheme.getId();
		}

		List<ColorsAndFontsTheme> colorsAndFontsThemes = (List<ColorsAndFontsTheme>) colorsAndFontsThemeCombo
				.getInput();

		for (int i = 0; i < colorsAndFontsThemes.size(); i++) {
			if (colorsAndFontsThemes.get(i).getId().equals(colorAndFontThemeId)) {
				ISelection selection = new StructuredSelection(colorsAndFontsThemes.get(i));
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
		Map<String, String> result = new HashMap<String, String>();
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
		List<ColorsAndFontsTheme> result = new ArrayList<ColorsAndFontsTheme>();
		org.eclipse.ui.themes.ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme();

		IThemeDescriptor[] descs = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes();
		String defaultThemeString = PlatformUI.getWorkbench().getThemeManager()
				.getTheme(IThemeManager.DEFAULT_THEME).getLabel();
		if (currentTheme.getId().equals(IThemeManager.DEFAULT_THEME)) {
			defaultThemeString = NLS.bind(WorkbenchMessages.ViewsPreference_currentThemeFormat,
					new Object[] { defaultThemeString });
		}
		result.add(new ColorsAndFontsTheme(IThemeManager.DEFAULT_THEME, defaultThemeString));

		String themeString;
		for (int i = 0; i < descs.length; i++) {
			themeString = descs[i].getName();
			if (descs[i].getId().equals(currentTheme.getId())) {
				themeString = NLS.bind(WorkbenchMessages.ViewsPreference_currentThemeFormat,
						new Object[] { themeString });
			}
			result.add(new ColorsAndFontsTheme(descs[i].getId(), themeString));
		}
		return result;
	}

	private void refreshColorsAndFontsThemeDescriptionText(ColorsAndFontsTheme theme) {
		String description = ""; //$NON-NLS-1$
		IThemeDescriptor[] descs = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes();
		
		for (int i = 0; theme != null && description == null && i < descs.length; i++) {
			if (descs[i].getId().equals(theme.getId())) {
				description = descs[i].getDescription();
			}
		}
		colorsAndFontsThemeDescriptionText.setText(description);
	}

	private ColorsAndFontsTheme getSelectedColorsAndFontsTheme() {
		return (ColorsAndFontsTheme) colorsAndFontsThemeCombo.getStructuredSelection().getFirstElement();
	}

	private ColorsAndFontsTheme getCurrentColorsAndFontsTheme() {
		org.eclipse.ui.themes.ITheme theme = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme();

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
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
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
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

	}
}
