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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.util.PrefUtil;

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
}
