/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 132479 - [FieldAssist] Field assist example improvements
 *******************************************************************************/
package org.eclipse.ui.examples.fieldassist;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldAssistColors;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.examples.fieldassist.preferences.PreferenceConstants;

/**
 * Example dialog that shows different field assist capabilities, using
 * ControlDecorator to draw field-level decorations. The visual design in this
 * version of the dialog calls for required field emphasis to be shown with the
 * label, while content assist, error, and warning decorations are shown in a
 * shared slot to the left and center of the field.
 * <p>
 * Note that clients do not worry about aligning decorated and non-decorated
 * fields when using ControlDecorator, although it is up to the client to ensure
 * there is enough blank space for the decorator to paint properly.
 */
public class ExampleDialog2 extends StatusDialog {

	class SpinnerContentAdapter implements IControlContentAdapter {
		// We are only implementing this for our internal use, not for
		// content assist, so many of the methods are ignored.
		public String getControlContents(Control control) {
			return new Integer(((Spinner) control).getSelection()).toString();
		}

		public void setControlContents(Control control, String text,
				int cursorPosition) {
			// ignore
		}

		public void insertControlContents(Control control, String text,
				int cursorPosition) {
			// ignore
		}

		public int getCursorPosition(Control control) {
			// ignore
			return 0;
		}

		public Rectangle getInsertionBounds(Control control) {
			return control.getBounds();
		}

		public void setCursorPosition(Control control, int index) {
			// ignore
		}
	}

	abstract class SmartField {
		ControlDecoration controlDecoration;

		IControlContentAdapter contentAdapter;

		FieldDecoration errorDecoration, warningDecoration;

		SmartField(ControlDecoration decoration, IControlContentAdapter adapter) {
			this.controlDecoration = decoration;
			this.contentAdapter = adapter;
		}

		String getContents() {
			return contentAdapter.getControlContents(controlDecoration
					.getControl());
		}

		boolean isRequiredField() {
			return true;
		}

		FieldDecoration getErrorDecoration() {
			if (errorDecoration == null) {
				FieldDecoration standardError = FieldDecorationRegistry
						.getDefault().getFieldDecoration(
								FieldDecorationRegistry.DEC_ERROR);
				if (getErrorMessage() == null) {
					errorDecoration = standardError;
				} else {
					errorDecoration = new FieldDecoration(standardError
							.getImage(), getErrorMessage());
				}
			}
			return errorDecoration;

		}

		FieldDecoration getWarningDecoration() {
			if (warningDecoration == null) {
				FieldDecoration standardWarning = FieldDecorationRegistry
						.getDefault().getFieldDecoration(
								FieldDecorationRegistry.DEC_WARNING);
				if (getWarningMessage() == null) {
					warningDecoration = standardWarning;
				} else {
					warningDecoration = new FieldDecoration(standardWarning
							.getImage(), getWarningMessage());
				}
			}
			return warningDecoration;

		}

		abstract boolean isValid();

		abstract boolean isWarning();

		boolean hasContentAssist() {
			return false;
		}

		String getErrorMessage() {
			return null;
		}

		String getWarningMessage() {
			return null;
		}

	}

	class UserField extends SmartField {

		UserField(ControlDecoration decoration, IControlContentAdapter adapter) {
			super(decoration, adapter);
		}

		boolean isValid() {
			String contents = getContents();
			for (int i = 0; i < contents.length(); i++) {
				if (!Character.isLetter(contents.charAt(i))) {
					return false;
				}
			}
			return true;
		}

		String getErrorMessage() {
			return TaskAssistExampleMessages.ExampleDialog_UserError;
		}

		boolean isWarning() {
			return getContents().equals(
					TaskAssistExampleMessages.ExampleDialog_WarningName);
		}

		String getWarningMessage() {
			return TaskAssistExampleMessages.ExampleDialog_UserWarning;
		}

		boolean hasContentAssist() {
			return true;
		}

	}

	class AgeField extends SmartField {

		AgeField(ControlDecoration decoration, IControlContentAdapter adapter) {
			super(decoration, adapter);
		}

		boolean isValid() {
			// We seed the spinner with valid values always.
			return true;
		}

		boolean isWarning() {
			Spinner spinner = (Spinner) controlDecoration.getControl();
			return spinner.getSelection() > 65;
		}

		String getWarningMessage() {
			return TaskAssistExampleMessages.ExampleDialog_AgeWarning;
		}
	}

	private String[] validUsers = { "tom", "dick", "harry", "ferdinand", "tim", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"teresa", "tori", "daniela" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private String triggerKey;

	private String username;

	private boolean showErrorDecoration, showErrorMessage, showErrorColor,
			showWarningDecoration, showRequiredFieldColor,
			showRequiredFieldDecoration, showSecondaryPopup;

	private Color defaultTextColor, errorColor;

	/**
	 * Open the exapmle dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param username
	 *            the default username
	 */
	public ExampleDialog2(Shell parent, String username) {
		super(parent);
		setTitle(TaskAssistExampleMessages.ExampleDialog_Title);
		this.username = username;
		getPreferenceValues();
	}

	protected Control createDialogArea(Composite parent) {

		Composite outer = (Composite) super.createDialogArea(parent);

		initializeDialogUnits(outer);

		Group main = new Group(outer, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setText(TaskAssistExampleMessages.ExampleDialog_SecurityGroup);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		main.setLayout(layout);

		Label label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_UserName);

		// Create a field representing a user name
		Text text = new Text(main, SWT.BORDER);
		ControlDecoration dec = new ControlDecoration(text, getCueDecoration(),
				SWT.LEFT | SWT.CENTER);
		dec.setShowOnlyOnFocus(true);
		final SmartField textField = new UserField(dec,
				new TextContentAdapter());
		if (showRequiredFieldDecoration && textField.isRequiredField()) {
			addRequiredFieldIndicator(label);
		}
		defaultTextColor = text.getBackground();
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleModify(textField);
			}
		});
		text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
				handleFocusGained(textField);
			}

			public void focusLost(FocusEvent event) {
				handleFocusLost(textField);
			}

		});

		text.setText(username);
		installContentProposalAdapter(text, new TextContentAdapter());
		text.setLayoutData(getFieldGridData());
		// prime the required field color by calling the focus lost handler.
		handleFocusLost(textField);

		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_ComboUserName);

		// Create a combo field representing a user name
		Combo combo = new Combo(main, SWT.BORDER | SWT.DROP_DOWN);
		dec = new ControlDecoration(combo, getCueDecoration(), SWT.LEFT
				| SWT.CENTER);
		dec.setShowOnlyOnFocus(true);

		final SmartField comboField = new UserField(dec,
				new ComboContentAdapter());
		if (showRequiredFieldDecoration) {
			addRequiredFieldIndicator(label);
		}
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleModify(comboField);
			}
		});
		combo.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
				handleFocusGained(comboField);
			}

			public void focusLost(FocusEvent event) {
				handleFocusLost(comboField);
			}

		});
		combo.setText(username);
		combo.setItems(validUsers);
		combo.setLayoutData(getFieldGridData());
		installContentProposalAdapter(combo, new ComboContentAdapter());
		// prime the required field color by calling the focus lost handler.
		handleFocusLost(comboField);

		// Create a spinner representing a user age
		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_Age);

		Spinner spinner = new Spinner(main, SWT.BORDER);
		dec = new ControlDecoration(spinner, getWarningDecoration(), SWT.LEFT
				| SWT.CENTER);
		dec.hide();

		if (showRequiredFieldDecoration) {
			addRequiredFieldIndicator(label);
		}
		final SmartField spinnerField = new AgeField(dec,
				new SpinnerContentAdapter());
		spinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleModify(spinnerField);
			}
		});
		combo.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
				handleFocusGained(spinnerField);
			}

			public void focusLost(FocusEvent event) {
				handleFocusLost(spinnerField);
			}

		});
		spinner.setSelection(40);
		spinner.setLayoutData(getFieldGridData());

		// prime the required field color by calling the focus lost handler.
		handleFocusLost(spinnerField);

		// This field has no decorator
		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_Password);
		text = new Text(main, SWT.BORDER);
		text.setText("******"); //$NON-NLS-1$
		text.setLayoutData(getFieldGridData());
		if (showRequiredFieldDecoration) {
			addRequiredFieldIndicator(label);
		}

		Dialog.applyDialogFont(outer);

		return main;
	}

	private void getPreferenceValues() {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		showErrorMessage = store
				.getBoolean(PreferenceConstants.PREF_SHOWERRORMESSAGE);
		showErrorDecoration = store
				.getBoolean(PreferenceConstants.PREF_SHOWERRORDECORATION);
		showErrorColor = store
				.getBoolean(PreferenceConstants.PREF_SHOWERRORCOLOR);
		showWarningDecoration = store
				.getBoolean(PreferenceConstants.PREF_SHOWWARNINGDECORATION);
		showRequiredFieldColor = store
				.getBoolean(PreferenceConstants.PREF_SHOWREQUIREDFIELDCOLOR);
		showRequiredFieldDecoration = store
				.getBoolean(PreferenceConstants.PREF_SHOWREQUIREDFIELDDECORATION);
		showSecondaryPopup = store
				.getBoolean(PreferenceConstants.PREF_SHOWSECONDARYPOPUP);
		triggerKey = store.getString(PreferenceConstants.PREF_CONTENTASSISTKEY);

	}

	private FieldDecoration getCueDecoration() {
		// We use our own decoration which is based on the JFace version.
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		FieldDecoration dec = registry
				.getFieldDecoration(FieldAssistPlugin.DEC_CONTENTASSIST);
		if (dec == null) {
			// Get the standard one. We use its image and our own customized
			// text.
			FieldDecoration standardDecoration = registry
					.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
			registry.registerFieldDecoration(
					FieldAssistPlugin.DEC_CONTENTASSIST, NLS.bind(
							TaskAssistExampleMessages.Decorator_ContentAssist,
							triggerKey), standardDecoration.getImage());
			dec = registry
					.getFieldDecoration(FieldAssistPlugin.DEC_CONTENTASSIST);
		}
		return dec;
	}

	private FieldDecoration getWarningDecoration() {
		return FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_WARNING);
	}

	private void handleModify(SmartField smartField) {
		// Error indicator supercedes all others
		if (!smartField.isValid()) {
			showError(smartField);
		} else {
			hideError(smartField);
			if (smartField.isWarning()) {
				showWarning(smartField);
			} else {
				hideWarning(smartField);
				if (smartField.hasContentAssist()
						&& FieldAssistPlugin
								.getDefault()
								.getPreferenceStore()
								.getBoolean(
										PreferenceConstants.PREF_SHOWCONTENTPROPOSALCUE)) {
					showContentAssist(smartField);
				}
			}
		}
	}

	private void handleFocusGained(SmartField smartField) {
		// only set color if error color not already showing
		if (showErrorColor && !smartField.isValid())
			return;
		if (showRequiredFieldColor && smartField.isRequiredField()) {
			smartField.controlDecoration.getControl().setBackground(
					defaultTextColor);
		}
	}

	private void handleFocusLost(SmartField smartField) {
		// only set color if error color not showing
		if (showErrorColor && !smartField.isValid())
			return;
		if (showRequiredFieldColor && smartField.isRequiredField()
				&& smartField.getContents().length() == 0) {
			smartField.controlDecoration
					.getControl()
					.setBackground(
							FieldAssistColors
									.getRequiredFieldBackgroundColor(smartField.controlDecoration
											.getControl()));
		}
	}

	private void showError(SmartField smartField) {
		FieldDecoration dec = smartField.getErrorDecoration();
		if (showErrorMessage) {
			updateStatus(new Status(IStatus.ERROR,
					"org.eclipse.examples.contentassist", 0, dec //$NON-NLS-1$
							.getDescription(), null));
		}
		if (showErrorDecoration) {
			smartField.controlDecoration.setDecoration(dec);
			smartField.controlDecoration.setShowOnlyOnFocus(false);
			smartField.controlDecoration.show();
		}
		if (showErrorColor) {
			smartField.controlDecoration.getControl().setBackground(
					getErrorColor(smartField.controlDecoration.getControl()));
		}
	}

	private void hideError(SmartField smartField) {
		if (showErrorMessage) {
			this.updateStatus(Status.OK_STATUS);
		}
		if (showErrorDecoration) {
			smartField.controlDecoration.hide();
		}
		if (showErrorColor) {
			smartField.controlDecoration.getControl().setBackground(
					defaultTextColor);
		}
	}

	private void showWarning(SmartField smartField) {
		if (showWarningDecoration) {
			FieldDecoration dec = smartField.getWarningDecoration();
			smartField.controlDecoration.setDecoration(dec);
			smartField.controlDecoration.setShowOnlyOnFocus(false);
			smartField.controlDecoration.show();
		}
	}

	private void hideWarning(SmartField smartField) {
		if (showWarningDecoration) {
			smartField.controlDecoration.hide();
		}
	}

	private void showContentAssist(SmartField smartField) {
		FieldDecoration dec = getCueDecoration();
		smartField.controlDecoration.setDecoration(dec);
		smartField.controlDecoration.setShowOnlyOnFocus(true);
		smartField.controlDecoration.show();
	}

	private void installContentProposalAdapter(Control control,
			IControlContentAdapter contentAdapter) {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		boolean propagate = store
				.getBoolean(PreferenceConstants.PREF_CONTENTASSISTKEY_PROPAGATE);
		KeyStroke keyStroke;
		char[] autoActivationCharacters = null;
		int autoActivationDelay = store
				.getInt(PreferenceConstants.PREF_CONTENTASSISTDELAY);

		if (triggerKey.equals(PreferenceConstants.PREF_CONTENTASSISTKEYAUTO)) {
			// null means automatically assist when character typed
			keyStroke = null;
		} else if (triggerKey
				.equals(PreferenceConstants.PREF_CONTENTASSISTKEYAUTOSUBSET)) {
			keyStroke = null;
			autoActivationCharacters = new char[] { 't', 'd' };
		} else {
			try {
				keyStroke = KeyStroke.getInstance(triggerKey);
			} catch (ParseException e) {
				keyStroke = KeyStroke.getInstance(SWT.F10);
			}
		}

		ContentProposalAdapter adapter = new ContentProposalAdapter(control,
				contentAdapter, getContentProposalProvider(), keyStroke,
				autoActivationCharacters);
		adapter.setAutoActivationDelay(autoActivationDelay);
		adapter.setPropagateKeys(propagate);
		adapter.setFilterStyle(getContentAssistFilterStyle());
		adapter.setProposalAcceptanceStyle(getContentAssistAcceptance());
	}

	private IContentProposalProvider getContentProposalProvider() {
		return new IContentProposalProvider() {
			public IContentProposal[] getProposals(String contents, int position) {
				IContentProposal[] proposals = new IContentProposal[validUsers.length];
				for (int i = 0; i < validUsers.length; i++) {
					final String user = validUsers[i];
					proposals[i] = new IContentProposal() {
						public String getContent() {
							return user;
						}

						public String getLabel() {
							return user;
						}

						public String getDescription() {
							if (showSecondaryPopup)
								return MessageFormat
										.format(
												TaskAssistExampleMessages.ExampleDialog_ProposalDescription,
												new String[] { user });
							return null;
						}

						public int getCursorPosition() {
							return user.length();
						}
					};
				}
				return proposals;
			}
		};
	}

	private int getContentAssistAcceptance() {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		String acceptanceStyle = store
				.getString(PreferenceConstants.PREF_CONTENTASSISTRESULT);
		if (acceptanceStyle
				.equals(PreferenceConstants.PREF_CONTENTASSISTRESULT_INSERT))
			return ContentProposalAdapter.PROPOSAL_INSERT;
		if (acceptanceStyle
				.equals(PreferenceConstants.PREF_CONTENTASSISTRESULT_REPLACE))
			return ContentProposalAdapter.PROPOSAL_REPLACE;
		return ContentProposalAdapter.PROPOSAL_IGNORE;
	}

	private int getContentAssistFilterStyle() {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		String acceptanceStyle = store
				.getString(PreferenceConstants.PREF_CONTENTASSISTFILTER);
		if (acceptanceStyle
				.equals(PreferenceConstants.PREF_CONTENTASSISTFILTER_CHAR))
			return ContentProposalAdapter.FILTER_CHARACTER;
		if (acceptanceStyle
				.equals(PreferenceConstants.PREF_CONTENTASSISTFILTER_CUMULATIVE))
			return ContentProposalAdapter.FILTER_CUMULATIVE;
		return ContentProposalAdapter.FILTER_NONE;
	}

	private GridData getFieldGridData() {
		int margin = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + margin;
		data.horizontalIndent = margin;
		data.grabExcessHorizontalSpace = true;
		return data;

	}

	private Color getErrorColor(Control control) {
		if (errorColor == null) {
			RGB rgb = FieldAssistColors.computeErrorFieldBackgroundRGB(control);
			errorColor = new Color(control.getDisplay(), rgb);
		}
		return errorColor;
	}

	public boolean close() {
		if (errorColor != null) {
			errorColor.dispose();
		}
		return super.close();
	}

	private void addRequiredFieldIndicator(Label label) {
		String text = label.getText();
		// This concatenation could be done by a field assist helper
		// API, or else done automatically inside a smarter,
		// "extended field."
		text = text.concat("*");
		label.setText(text);
	}

}
