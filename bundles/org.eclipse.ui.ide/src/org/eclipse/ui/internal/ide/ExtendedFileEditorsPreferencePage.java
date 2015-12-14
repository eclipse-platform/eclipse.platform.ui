/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.dialogs.FileEditorsPreferencePage;

/**
 * @since 3.12
 *
 */
public class ExtendedFileEditorsPreferencePage extends FileEditorsPreferencePage {

	private IPreferenceStore idePreferenceStore;

	@Override
	protected Composite createContents(Composite parent) {
		Composite res = (Composite)super.createContents(parent);

		Composite defaultStrategyComposite = new Composite(res, SWT.NONE);
		defaultStrategyComposite.setLayout(new GridLayout(2, false));
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		layoutData.verticalIndent = 20;
		defaultStrategyComposite.setLayoutData(layoutData);
		Label unknownTypeStrategyLabel = new Label(defaultStrategyComposite, SWT.NONE);
		unknownTypeStrategyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		unknownTypeStrategyLabel
				.setText(IDEWorkbenchMessages.ExtendedFileEditorsPreferencePage_strategyForUnknownFiles);
		ComboViewer viewer = new ComboViewer(defaultStrategyComposite);
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object o) {
				String id = (String) o;
				String label = UnknownEditorStrategyRegistry.getLabel(id);
				if (label != null) {
					return label;
				}
				IDEWorkbenchPlugin.log("Could not resolve unknownEditorStrategy '" + id + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				return NLS.bind(IDEWorkbenchMessages.ExtendedFileEditorsPreferencePage_labelNotResolved, id);
			}
		});
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(UnknownEditorStrategyRegistry.retrieveAllStrategies());
		this.idePreferenceStore = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		viewer.setSelection(
				new StructuredSelection(this.idePreferenceStore.getString(IDE.UNKNOWN_EDITOR_STRATEGY_PREFERENCE_KEY)));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				idePreferenceStore.setValue(IDE.UNKNOWN_EDITOR_STRATEGY_PREFERENCE_KEY,
						(String) ((IStructuredSelection) event.getSelection()).getFirstElement());
			}
		});

		return res;
	}

	@Override
	public boolean performOk() {
		if (idePreferenceStore != null && idePreferenceStore.needsSaving()
				&& idePreferenceStore instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) idePreferenceStore).save();
			} catch (IOException e) {
				String message = JFaceResources.format("PreferenceDialog.saveErrorMessage", getTitle(), //$NON-NLS-1$
						e.getMessage());
				Policy.getStatusHandler().show(new Status(IStatus.ERROR, Policy.JFACE, message, e),
						JFaceResources.getString("PreferenceDialog.saveErrorTitle")); //$NON-NLS-1$
			}
		}
		return super.performOk();
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		idePreferenceStore.setToDefault(IDE.UNKNOWN_EDITOR_STRATEGY_PREFERENCE_KEY);
	}

}
