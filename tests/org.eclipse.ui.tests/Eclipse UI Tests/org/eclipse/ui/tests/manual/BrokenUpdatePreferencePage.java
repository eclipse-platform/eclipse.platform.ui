/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.manual;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * This is a test preference page designed to
 * generate errors on update
 */
public class BrokenUpdatePreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private final IPropertyChangeListener badListener = event -> {
		//Intentionally generate an error
		String[] strings = new String[1];
		System.out.println(strings[2]);
	};

	FontData[] data;

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		data = parent.getFont().getFontData();

		Composite buttonComposite = new Composite(parent, SWT.NULL);
		buttonComposite.setLayout(new GridLayout());
		buttonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Button fontButton = new Button(buttonComposite, SWT.PUSH);
		fontButton.setText("Update Font");
		fontButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeFont();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				changeFont();
			}
		});

		Button preferencePluginButton = new Button(buttonComposite, SWT.PUSH);
		preferencePluginButton.setText("Update Plugin Preferences");
		preferencePluginButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				changePluginPreference();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				changePluginPreference();
			}
		});

		Button preferenceButton = new Button(buttonComposite, SWT.PUSH);
		preferenceButton.setText("Update Dialog Preferences");
		preferenceButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				changePluginPreference();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				changePluginPreference();
			}
		});

		return buttonComposite;

	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {

		JFaceResources.getFontRegistry().addListener(badListener);

		PrefUtil.getInternalPreferenceStore()
				.addPropertyChangeListener(badListener);

	}

	/**
	 * see@DialogPage.dispose();
	 */
	@Override
	public void dispose() {
		super.dispose();
		JFaceResources.getFontRegistry().removeListener(badListener);

		PrefUtil.getInternalPreferenceStore()
				.removePropertyChangeListener(badListener);
	}

	public void changeFont() {
		JFaceResources.getFontRegistry().put("FAKO", data);
	}

	public void changePluginPreference() {
		PrefUtil.getInternalPreferenceStore().firePropertyChangeEvent(
				"FAKO", "Old", "New");
	}

	public void changePreference() {
		getPreferenceStore().firePropertyChangeEvent("FAKO", "Old", "New");
	}
}
