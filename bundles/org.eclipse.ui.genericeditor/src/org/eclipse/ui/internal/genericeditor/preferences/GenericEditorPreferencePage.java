/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.genericeditor.Messages;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class GenericEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private final ArrayList<SelectionListener> leadFollowerListeners = new ArrayList<>();
	private final Map<PreferenceMetadata<Boolean>, Button> buttons = new HashMap<>();
	private final Map<PreferenceMetadata<String>, Text> textFields = new HashMap<>();
	private static final int columns = 2;
	private final IPreferenceStore store;

	public GenericEditorPreferencePage() {
		this.store = GenericEditorPreferenceConstants.getPreferenceStore();
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing to do
	}

	@Override
	protected Control createContents(Composite parent) {
		var control = createAppearancePage(parent);
		Dialog.applyDialogFont(control);
		initialize();
		return control;
	}

	@Override
	public boolean performOk() {
		if (store instanceof ScopedPreferenceStore scopedStore) {
			buttons.entrySet().forEach(e -> scopedStore.setValue(e.getKey().identifer(), e.getValue().getSelection()));
			textFields.entrySet().forEach(e -> scopedStore.setValue(e.getKey().identifer(), e.getValue().getText()));
			try {
				scopedStore.save();
			} catch (IOException e) {
				Platform.getLog(getClass()).error("Cannot to save preferences.", e); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

	@Override
	protected void performDefaults() {
		buttons.entrySet().forEach(e -> e.getValue().setSelection(store.getDefaultBoolean(e.getKey().identifer())));
		textFields.entrySet().forEach(e -> e.getValue().setText(store.getDefaultString(e.getKey().identifer())));
		updateFollower();
		super.performDefaults();
	}

	private Control createAppearancePage(Composite parent) {

		Composite appearanceComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		final var contetAssistGroup = createGroup(appearanceComposite, Messages.ContentAssistant);

		final var autoActivationMetadata = new PreferenceMetadata<>(Boolean.class, //
				GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION, //
				GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_DEFAULT, //
				Messages.ContentAssistant_autoActivation, //
				Messages.ContentAssistant_autoActivation_Tooltip);
		final var autoActivation = createButton(autoActivationMetadata, contetAssistGroup, SWT.CHECK, 0);

		final var activationDelay = new PreferenceMetadata<>(String.class, //
				GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY, //
				Integer.toString(GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_DELAY_DEFAULT), //
				Messages.ContentAssistant_autoActivationDelay, //
				Messages.ContentAssistant_autoActivationDelay_Tooltip);
		final var activationDelayControl = createTextField(activationDelay, contetAssistGroup, 4, 20);

		final var autoActivationOnTypeMetada = new PreferenceMetadata<>(Boolean.class, //
				GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE, //
				GenericEditorPreferenceConstants.CONTENT_ASSISTANT_AUTO_ACTIVATION_ON_TYPE_DEFAULT, //
				Messages.ContentAssistant_autoActivationOnType, //
				Messages.ContentAssistant_autoActivationOnType_Tooltip); //
		final var autoActivationOnType = createButton(autoActivationOnTypeMetada, contetAssistGroup, SWT.CHECK, 20);
		List<Control> follower = new ArrayList<>(3);
		Collections.addAll(follower, activationDelayControl);
		follower.add(autoActivationOnType);

		createDependency(autoActivation, autoActivationMetadata.identifer(), follower.toArray(new Control[0]));

		appearanceComposite.setLayout(layout);
		return appearanceComposite;
	}

	private void initialize() {
		buttons.entrySet().forEach(e -> e.getValue().setSelection(store.getBoolean(e.getKey().identifer())));
		textFields.entrySet().forEach(e -> e.getValue().setText(store.getString(e.getKey().identifer())));
	}

	private static Group createGroup(Composite parent, String label) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(label);
		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return group;
	}

	private Button createButton(final PreferenceMetadata<Boolean> meta, Composite composite, int style,
			int horizontalIndent) {
		Button button = new Button(composite, style);
		button.setLayoutData(GridDataFactory.fillDefaults().span(columns, 1).indent(horizontalIndent, 0).create());
		button.setData(meta);
		button.setText(meta.name());
		button.setToolTipText(meta.description());
		buttons.put(meta, button);
		return button;
	}

	private Control[] createTextField(final PreferenceMetadata<String> meta, Composite composite, int textLimit,
			int horizontalIndent) {
		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(meta.name());
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = horizontalIndent;
		labelControl.setLayoutData(gd);

		final Text textControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		textControl.setToolTipText(meta.description());

		textControl.addModifyListener(e -> {
			updateStatus(validateDelay(textControl.getText()));
		});
		textFields.put(meta, textControl);
		return new Control[] { labelControl, textControl };
	}

	private static String validateDelay(String value) {
		if (value.isEmpty()) {
			return Messages.ContentAssistant_autoActivationDelay_EmptyInput;
		}
		try {
			int integer = parseInteger(value);
			if (integer < 0)
				return NLS.bind(Messages.ContentAssistant_autoActivationDelay_InvalidInput, integer);
		} catch (NumberFormatException e) {
			return NLS.bind(Messages.ContentAssistant_autoActivationDelay_InvalidInput, value);
		}
		return null;
	}

	private void updateStatus(String errorMessage) {
		setValid(errorMessage == null);
		var messageType = errorMessage == null ? IMessageProvider.NONE : IMessageProvider.ERROR;
		setMessage(errorMessage, messageType);
		setErrorMessage(errorMessage);
	}

	private static int parseInteger(Object value) throws NumberFormatException {
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		if (value instanceof String) {
			return Integer.parseInt((String) value);
		}
		throw new NumberFormatException(NLS.bind(Messages.ContentAssistant_autoActivationDelay_InvalidInput, value));
	}

	private void createDependency(final Button lead, String leadKey, final Control[] follower) {
		var leadState = store.getBoolean(leadKey);
		for (Control f : follower) {
			f.setEnabled(leadState);
		}

		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state = lead.getSelection();
				for (Control f : follower) {
					f.setEnabled(state);
				}
			}
		};
		leadFollowerListeners.add(listener);
		lead.addSelectionListener(listener);
	}

	private void updateFollower() {
		for (var listener : leadFollowerListeners) {
			listener.widgetSelected(null);
		}
	}
}
