/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.monitoring.MonitoringPlugin;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Preference page that allows user to toggle plug in settings from Eclipse preferences.
 */
public class MonitoringPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	private static final int HOUR_IN_MS = 3600000;
	private static final IPreferenceStore preferences =
			MonitoringPlugin.getDefault().getPreferenceStore();
	private BooleanFieldEditor monitoringEnabled;
	private IntegerEditor longEventWarningThreshold;
	private IntegerEditor longEventErrorThreshold;
	private IntegerEditor deadlockThreshold;
	private Map<FieldEditor, Composite> editors;

	private class IntegerEditor extends IntegerFieldEditor {
		public IntegerEditor(String name, String labelText, Composite parent, int min, int max) {
	    	super(name, labelText, parent);
	    	setValidRange(min, max);
		}

		@Override
		protected void valueChanged() {
			super.valueChanged();
			if (longEventWarningThreshold.isValid() && longEventErrorThreshold.checkValue()) {
				deadlockThreshold.checkValue();
			}
		}

		@Override
		protected boolean checkState() {
			if (!super.checkState()) {
				return false;
			}

			String preferenceName = getPreferenceName();
			if (preferenceName.equals(PreferenceConstants.LONG_EVENT_ERROR_THRESHOLD_MILLIS)) {
				if (longEventWarningThreshold.isValid() &&
						getIntValue() < longEventWarningThreshold.getIntValue()) {
					showMessage(Messages.MonitoringPreferencePage_error_threshold_too_low_error);
					return false;
				}
			} else if (preferenceName.equals(PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS)) {
				if (longEventWarningThreshold.isValid() &&
						getIntValue() <= longEventErrorThreshold.getIntValue()) {
					showMessage(Messages.MonitoringPreferencePage_deadlock_threshold_too_low_error);
					return false;
				}
			}
			return true;
		}

		private boolean checkValue() {
	        boolean oldState = isValid();
	        refreshValidState();

	        boolean isValid = isValid();
	        if (isValid != oldState) {
				fireStateChanged(IS_VALID, oldState, isValid);
			}
	        return isValid;
	    }
	}

	public MonitoringPreferencePage() {
		super(GRID);
		editors = new HashMap<FieldEditor, Composite>();
	}

	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
    	PixelConverter pixelConverter = new PixelConverter(parent);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Composite container = new Composite(parent, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = pixelConverter.convertHeightInCharsToPixels(1);
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topGroup = new Composite(container, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		topGroup.setLayout(layout);
		topGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		monitoringEnabled = createBooleanEditor(PreferenceConstants.MONITORING_ENABLED,
				Messages.MonitoringPreferencePage_enable_monitoring_label, topGroup);

		longEventWarningThreshold = createIntegerEditor(
				PreferenceConstants.LONG_EVENT_WARNING_THRESHOLD_MILLIS,
				Messages.MonitoringPreferencePage_warning_threshold_label, topGroup,
				3, HOUR_IN_MS);
		longEventErrorThreshold = createIntegerEditor(
				PreferenceConstants.LONG_EVENT_ERROR_THRESHOLD_MILLIS,
				Messages.MonitoringPreferencePage_error_threshold_label, topGroup,
				3, HOUR_IN_MS);
		deadlockThreshold = createIntegerEditor(
				PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS,
				Messages.MonitoringPreferencePage_deadlock_threshold_label, topGroup,
				1000, 24 * HOUR_IN_MS);
		createIntegerEditor(
				PreferenceConstants.MAX_STACK_SAMPLES,
				Messages.MonitoringPreferencePage_max_stack_samples_label, topGroup, 0, 100);

		topGroup.setLayout(layout);

		createBooleanEditor(PreferenceConstants.LOG_TO_ERROR_LOG,
				Messages.MonitoringPreferencePage_log_freeze_events_label, topGroup);
		topGroup.setLayout(layout);

		final Composite bottomGroup = new Composite(container, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		bottomGroup.setLayout(layout);
		bottomGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		addField(new FilterListEditor(PreferenceConstants.FILTER_TRACES,
				Messages.MonitoringPreferencePage_filter_label, bottomGroup), bottomGroup);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(preferences);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(FieldEditor.VALUE)) {
    		Object source = event.getSource();
    		if (source instanceof FieldEditor) {
    			String preferenceName = ((FieldEditor) source).getPreferenceName();
				if (preferenceName.equals(PreferenceConstants.MONITORING_ENABLED)) {
    				boolean enabled = Boolean.TRUE.equals(event.getNewValue());
	    			enableDependentFields(enabled);
    			}
    		}
        }
		super.propertyChange(event);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		enableDependentFields(monitoringEnabled.getBooleanValue());
	}

	private void enableDependentFields(boolean enable) {
		for (Map.Entry<FieldEditor, Composite> entry : editors.entrySet()) {
			FieldEditor editor = entry.getKey();
			if (!editor.getPreferenceName().equals(PreferenceConstants.MONITORING_ENABLED)) {
				editor.setEnabled(enable, entry.getValue());
			}
		}
	}

	private BooleanFieldEditor createBooleanEditor(String name, String labelText,
			Composite parent) {
		BooleanFieldEditor field = new BooleanFieldEditor(name, labelText, parent);
		return addField(field, parent);
	}

	private IntegerEditor createIntegerEditor(String name, String labelText, Composite parent,
			int min, int max) {
		IntegerEditor field = new IntegerEditor(name, labelText, parent, min, max);
		return addField(field, parent);
	}

	private <T extends FieldEditor> T addField(T editor, Composite parent) {
		super.addField(editor);
		editor.fillIntoGrid(parent, 2);
		editors.put(editor, parent);
		if (!editor.getPreferenceName().equals(PreferenceConstants.MONITORING_ENABLED)) {
			boolean enabled = preferences.getBoolean(PreferenceConstants.MONITORING_ENABLED);
			editor.setEnabled(enabled, parent);
		}
		return editor;
	}
}
