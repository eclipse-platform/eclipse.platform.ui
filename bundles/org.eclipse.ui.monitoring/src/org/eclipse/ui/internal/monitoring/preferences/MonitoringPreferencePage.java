/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import java.util.HashMap;
import java.util.Map;

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
public class MonitoringPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final int HOUR_IN_MS = 3600000;
	private static final IPreferenceStore preferences = MonitoringPlugin.getDefault().getPreferenceStore();
	private boolean pluginEnabled = preferences.getBoolean(PreferenceConstants.MONITORING_ENABLED);
	private IntegerEditor longEventThreshold;
	private IntegerEditor sampleInterval;
	private IntegerEditor initialSampleDelay;
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
			if (longEventThreshold.isValid() &&
					sampleInterval.checkValue() && initialSampleDelay.checkValue()) {
				deadlockThreshold.checkValue();
			}
		}

		@Override
		protected boolean checkState() {
			if (!super.checkState()) {
				return false;
			}

			String preferenceName = getPreferenceName();
			if (preferenceName.equals(PreferenceConstants.SAMPLE_INTERVAL_MILLIS)) {
				if (longEventThreshold.isValid() &&
						getIntValue() >= longEventThreshold.getIntValue()) {
					showMessage(Messages.MonitoringPreferencePage_sample_interval_too_high_error);
					return false;
				}
			} else if (preferenceName.equals(PreferenceConstants.INITIAL_SAMPLE_DELAY_MILLIS)) {
				if (longEventThreshold.isValid() &&
						getIntValue() >= longEventThreshold.getIntValue()) {
					showMessage(Messages.MonitoringPreferencePage_initial_sample_delay_too_high_error);
					return false;
				}
			} else if (preferenceName.equals(PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS)) {
				if (longEventThreshold.isValid() &&
						getIntValue() <= longEventThreshold.getIntValue()) {
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
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Composite groupContainer = new Composite(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout(1, false);
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupContainer.setLayout(groupLayout);
		groupContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite topGroup = new Composite(groupContainer, SWT.NONE);
		GridLayout innerGroupLayout = new GridLayout(2, false);
		innerGroupLayout.marginWidth = 0;
		innerGroupLayout.marginHeight = 0;
		topGroup.setLayout(innerGroupLayout);
		topGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createBooleanEditor(PreferenceConstants.MONITORING_ENABLED,
				Messages.MonitoringPreferencePage_enable_thread_label, topGroup);

		longEventThreshold = createIntegerEditor(
				PreferenceConstants.LONG_EVENT_THRESHOLD_MILLIS,
				Messages.MonitoringPreferencePage_long_event_threshold, topGroup, 3, HOUR_IN_MS);
		createIntegerEditor(
				PreferenceConstants.MAX_STACK_SAMPLES,
				Messages.MonitoringPreferencePage_max_stack_samples_label, topGroup, 1, 100);
		sampleInterval = createIntegerEditor(
				PreferenceConstants.SAMPLE_INTERVAL_MILLIS,
				Messages.MonitoringPreferencePage_sample_interval_label, topGroup, 2, HOUR_IN_MS);
		initialSampleDelay = createIntegerEditor(
				PreferenceConstants.INITIAL_SAMPLE_DELAY_MILLIS,
				Messages.MonitoringPreferencePage_initial_sample_delay_label, topGroup,
				2, HOUR_IN_MS);
		deadlockThreshold = createIntegerEditor(
				PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS,
				Messages.MonitoringPreferencePage_deadlock_threshold_label, topGroup,
				1000, 24 * HOUR_IN_MS);

		createBooleanEditor(PreferenceConstants.DUMP_ALL_THREADS,
				Messages.MonitoringPreferencePage_dump_all_threads_label, topGroup);
		topGroup.setLayout(innerGroupLayout);

		createBooleanEditor(PreferenceConstants.LOG_TO_ERROR_LOG,
				Messages.MonitoringPreferencePage_log_freeze_events_label, topGroup);
		topGroup.setLayout(innerGroupLayout);

		final Composite bottomGroup = new Composite(groupContainer, SWT.NONE);
		bottomGroup.setLayout(innerGroupLayout);
		bottomGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addField(new ListFieldEditor(PreferenceConstants.FILTER_TRACES,
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
	    			for (Map.Entry<FieldEditor, Composite> entry : editors.entrySet()) {
						FieldEditor editor = entry.getKey();
	    				if (!editor.getPreferenceName().equals(PreferenceConstants.MONITORING_ENABLED)) {
	    					editor.setEnabled(enabled, entry.getValue());
	    				}
	    			}
    			}
    		}
        }
		super.propertyChange(event);
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
			editor.setEnabled(pluginEnabled, parent);
		}
		return editor;
	}
}
