/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
	private IThemeEngine engine;
	private ComboViewer themeIdCombo;
	private ITheme currentTheme;
	private String defaultTheme;
	private Button enableAnimations;
	private Button useColoredLabels;
	
	private Text themeDescriptionText;
	private Combo themeCombo;

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		new Label(comp, SWT.NONE).setText(WorkbenchMessages.ViewsPreferencePage_Theme);

		themeIdCombo = new ComboViewer(comp, SWT.READ_ONLY);
		themeIdCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ITheme) element).getLabel();
			}
		});
		themeIdCombo.setContentProvider(new ArrayContentProvider());
		themeIdCombo.setInput(engine.getThemes());
		themeIdCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.currentTheme = engine.getActiveTheme();
		if (this.currentTheme != null) {
			themeIdCombo.setSelection(new StructuredSelection(currentTheme));
		}
		themeIdCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ITheme selection = getSelection();
				engine.setTheme(selection, false);
				((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY))
						.setSelection(selection);
			}
		});

		createThemeCombo(comp);
		createThemeDescriptionText(comp);
		createEnableAnimationsPref(comp);
		createColoredLabelsPref(comp);

		((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY))
				.setSelection(currentTheme);
		((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY)).createContents(comp);


		return comp;
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
	private ITheme getSelection() {
		return (ITheme) ((IStructuredSelection) themeIdCombo.getSelection()).getFirstElement();
	}

	public void init(IWorkbench workbench) {
		MApplication application = (MApplication) workbench.getService(MApplication.class);
		IEclipseContext context = application.getContext();
		defaultTheme = (String) context.get(E4Application.THEME_ID);
		engine = context.get(IThemeEngine.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (getSelection() != null) {
			if (!getSelection().equals(currentTheme)) {
				MessageDialog.openWarning(getShell(), WorkbenchMessages.ThemeChangeWarningTitle,
						WorkbenchMessages.ThemeChangeWarningText);
			}
			engine.setTheme(getSelection(), true);
		}
		IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
		int idx = themeCombo.getSelectionIndex();
		if (idx <= 0) {
			PlatformUI.getWorkbench().getThemeManager()
					.setCurrentTheme(IThemeManager.DEFAULT_THEME);
			refreshThemeCombo(IThemeManager.DEFAULT_THEME);
		} else {
			IThemeDescriptor applyTheme = WorkbenchPlugin.getDefault().getThemeRegistry()
					.getThemes()[idx - 1];
			PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(applyTheme.getId());
			refreshThemeCombo(applyTheme.getId());
		}
		refreshThemeDescriptionText();
		apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
				enableAnimations.getSelection());
		apiStore.setValue(IWorkbenchPreferenceConstants.USE_COLORED_LABELS,
				useColoredLabels.getSelection());
		((PreferencePageEnhancer) Tweaklets.get(PreferencePageEnhancer.KEY)).performOK();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
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
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		if (currentTheme != null) {
			engine.setTheme(currentTheme, false);
		}
		return super.performCancel();
	}

	private void createThemeCombo(Composite composite) {
		new Label(composite, SWT.NONE).setText(WorkbenchMessages.ViewsPreference_currentTheme);
		themeCombo = new Combo(composite, SWT.READ_ONLY);
		themeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		themeCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refreshThemeDescriptionText();
			}
		});
		refreshThemeCombo(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getId());
	}

	/**
	 * Create the text box that will contain the current theme description text
	 * (if any).
	 * 
	 * @param parent
	 *            the parent <code>Composite</code>.
	 */
	private void createThemeDescriptionText(Composite parent) {
		new Label(parent, SWT.NONE)
				.setText(WorkbenchMessages.ViewsPreference_currentThemeDescription);

		themeDescriptionText = new Text(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY
				| SWT.BORDER | SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// give a height hint that'll show at least two lines (and let the
		// scroll bars draw nicely if necessary)
		GC gc = new GC(parent);
		layoutData.heightHint = Dialog.convertHeightInCharsToPixels(gc.getFontMetrics(), 2);
		gc.dispose();
		themeDescriptionText.setLayoutData(layoutData);

		refreshThemeDescriptionText();
	}

	private void refreshThemeDescriptionText() {
		String description = null;
		int idx = themeCombo.getSelectionIndex();
		// idx == 0 is "Default" which has no description
		if (idx > 0) {
			IThemeDescriptor theme = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes()[idx - 1];
			description = theme.getDescription();
		}
		if (description == null) {
			description = ""; //$NON-NLS-1$
		}
		themeDescriptionText.setText(description);
	}

	/**
	 * @param themeToSelect
	 *            the id of the theme to be selected
	 */
	private void refreshThemeCombo(String themeToSelect) {
		themeCombo.removeAll();
		org.eclipse.ui.themes.ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme();

		IThemeDescriptor[] descs = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes();
		String defaultThemeString = PlatformUI.getWorkbench().getThemeManager()
				.getTheme(IThemeManager.DEFAULT_THEME).getLabel();
		if (currentTheme.getId().equals(IThemeManager.DEFAULT_THEME)) {
			defaultThemeString = NLS.bind(WorkbenchMessages.ViewsPreference_currentThemeFormat,
					new Object[] { defaultThemeString });
		}
		themeCombo.add(defaultThemeString);

		String themeString;
		int selection = 0;
		for (int i = 0; i < descs.length; i++) {
			themeString = descs[i].getName();
			if (descs[i].getId().equals(currentTheme.getId())) {
				themeString = NLS.bind(WorkbenchMessages.ViewsPreference_currentThemeFormat,
						new Object[] { themeString });
			}
			if (themeToSelect.equals(descs[i].getId())) {
				selection = i + 1;
			}
			themeCombo.add(themeString);
		}
		themeCombo.select(selection);
	}
}
