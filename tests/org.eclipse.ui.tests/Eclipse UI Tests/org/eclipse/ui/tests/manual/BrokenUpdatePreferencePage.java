/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.manual;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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

    private IPropertyChangeListener badListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            //Intentionally generate an error
            String[] strings = new String[1];
            System.out.println(strings[2]);
        }
    };

    FontData[] data;

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {

        data = parent.getFont().getFontData();

        Composite buttonComposite = new Composite(parent, SWT.NULL);
        buttonComposite.setLayout(new GridLayout());
        buttonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Button fontButton = new Button(buttonComposite, SWT.PUSH);
        fontButton.setText("Update Font");
        fontButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                changeFont();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                changeFont();
            }
        });

        Button preferencePluginButton = new Button(buttonComposite, SWT.PUSH);
        preferencePluginButton.setText("Update Plugin Preferences");
        preferencePluginButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                changePluginPreference();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                changePluginPreference();
            }
        });

        Button preferenceButton = new Button(buttonComposite, SWT.PUSH);
        preferenceButton.setText("Update Dialog Preferences");
        preferenceButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                changePluginPreference();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                changePluginPreference();
            }
        });

        return buttonComposite;

    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench) {

        JFaceResources.getFontRegistry().addListener(badListener);

        PrefUtil.getInternalPreferenceStore()
                .addPropertyChangeListener(badListener);

    }

    /**	
     * see@DialogPage.dispose();
     */
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
