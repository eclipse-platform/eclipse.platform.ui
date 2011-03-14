/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.testing.ContributionInfo;
import org.osgi.framework.Constants;

/**
 * The Startup preference page.
 */
public class StartupPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {
    private Table pluginsList;

    private Workbench workbench;

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.STARTUP_PREFERENCE_PAGE);

        Composite composite = createComposite(parent);

        createEarlyStartupSelection(composite);

        return composite;
    }

    protected Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH
                | GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        return composite;
    }

    protected void createEarlyStartupSelection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(WorkbenchMessages.StartupPreferencePage_label);
        label.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(data);
        pluginsList = new Table(parent, SWT.BORDER | SWT.CHECK | SWT.H_SCROLL
                | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        pluginsList.setFont(parent.getFont());
        pluginsList.setLayoutData(data);
		TableViewer viewer = new TableViewer(pluginsList);
		viewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return (String) Platform.getBundle(((ContributionInfo) element).getBundleId())
						.getHeaders().get(
						Constants.BUNDLE_NAME);
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(workbench.getEarlyActivatedPlugins());
		// Workbench plugin should not be disabled. Don't show it
		viewer.setFilters(new ViewerFilter[] { createPlatformUIFilter() });
		updateCheckState();
    }

	private ViewerFilter createPlatformUIFilter() {
		return new ViewerFilter() {

			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof ContributionInfo) {
					ContributionInfo info = (ContributionInfo) element;
					return !PlatformUI.PLUGIN_ID.equals(info.getBundleId());
				}
				return true;
			}
		};
	}

	private void updateCheckState() {
        HashSet disabledPlugins = new HashSet(Arrays.asList(workbench.getDisabledEarlyActivatedPlugins()));
		for (int i = 0; i < pluginsList.getItemCount(); i++) {
			TableItem item = pluginsList.getItem(i);
			String pluginId = ((ContributionInfo) item.getData()).getBundleId();
            item.setChecked(!disabledPlugins.contains(pluginId));
        }
    }

    /**
     * @see IWorkbenchPreferencePage
     */
    public void init(IWorkbench workbench) {
        this.workbench = (Workbench) workbench;
    }

    /**
     * @see PreferencePage
     */
    protected void performDefaults() {
		IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
		store.setToDefault(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);
		updateCheckState();
    }

    /**
     * @see PreferencePage
     */
    public boolean performOk() {
        StringBuffer preference = new StringBuffer();
        TableItem items[] = pluginsList.getItems();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].getChecked()) {
				preference.append(((ContributionInfo) items[i].getData()).getBundleId());
                preference.append(IPreferenceConstants.SEPARATOR);
            }
        }
        String pref = preference.toString();
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        store.putValue(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP,
                pref);
        PrefUtil.savePrefs();
        return true;
    }
}
