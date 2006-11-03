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
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.examples.fieldassist.preferences.PreferenceConstants;

/**
 * Example dialog that shows different field assist capabilities.
 */
public abstract class FieldAssistTestDialog extends StatusDialog {

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
		// Cast to different types by each subclass
		Object decImpl;

		Control control;

		IControlContentAdapter contentAdapter;

		FieldDecoration errorDecoration, warningDecoration;

		SmartField(Object decImpl, Control control,
				IControlContentAdapter adapter) {
			this.decImpl = decImpl;
			this.contentAdapter = adapter;
			this.control = control;
		}

		boolean isRequiredField() {
			return true;
		}

		boolean hasContentAssist() {
			return false;
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

		String getContents() {
			return contentAdapter.getControlContents(control);
		}

		abstract boolean isValid();

		abstract boolean isWarning();

		String getErrorMessage() {
			return null;
		}

		String getWarningMessage() {
			return null;
		}

	}

	class UserField extends SmartField {

		UserField(Object decImpl, Control control,
				IControlContentAdapter adapter) {
			super(decImpl, control, adapter);
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

		AgeField(Object decImpl, Control control, IControlContentAdapter adapter) {
			super(decImpl, control, adapter);
		}

		boolean isValid() {
			// We seed the spinner with valid values always.
			return true;
		}

		boolean isWarning() {
			Spinner spinner = (Spinner) control;
			return spinner.getSelection() > 65;
		}

		String getWarningMessage() {
			return TaskAssistExampleMessages.ExampleDialog_AgeWarning;
		}
	}

	String[] validUsers = { "tom", "dick", "harry", "ferdinand", "tim",
			"teresa", "tori", "daniela", "aaron", "kevin", "tod", "mike",
			"kim", "eric", "paul" };

	String triggerKey;

	String username;

	boolean showErrorDecoration, showErrorMessage, showErrorColor,
			showWarningDecoration, showRequiredFieldColor,
			showRequiredFieldDecoration, showRequiredFieldLabelIndicator,
			showSecondaryPopup, showContentAssist;

	int marginWidth;

	Color defaultTextColor, errorColor;

	/**
	 * Open the exapmle dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param username
	 *            the default username
	 */
	public FieldAssistTestDialog(Shell parent, String username) {
		super(parent);
		setTitle(TaskAssistExampleMessages.ExampleDialog_Title);
		this.username = username;
		getPreferenceValues();
	}

	protected Control createDialogArea(Composite parent) {

		Composite outer = (Composite) super.createDialogArea(parent);

		initializeDialogUnits(outer);
		createSecurityGroup(outer);

		// Create a simple field to show how field assist can be used for
		// autocomplete.
		Group autoComplete = new Group(outer, SWT.NONE);
		autoComplete.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		autoComplete.setLayout(layout);
		autoComplete
				.setText(TaskAssistExampleMessages.ExampleDialog_AutoCompleteGroup);

		Label label = new Label(autoComplete, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_UserName);

		// Create an auto-complete field representing a user name
		Text text = new Text(autoComplete, SWT.BORDER);
		text.setLayoutData(getFieldGridData());
		new AutoCompleteField(text, new TextContentAdapter(), validUsers);

		Dialog.applyDialogFont(outer);

		return outer;
	}

	abstract void createSecurityGroup(Composite parent);

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
		showRequiredFieldLabelIndicator = store
				.getBoolean(PreferenceConstants.PREF_SHOWREQUIREDFIELDLABELINDICATOR);
		showSecondaryPopup = store
				.getBoolean(PreferenceConstants.PREF_SHOWSECONDARYPOPUP);
		showContentAssist = store
				.getBoolean(PreferenceConstants.PREF_SHOWCONTENTPROPOSALCUE);
		triggerKey = store.getString(PreferenceConstants.PREF_CONTENTASSISTKEY);
		marginWidth = store
				.getInt(PreferenceConstants.PREF_DECORATOR_MARGINWIDTH);
	}

	FieldDecoration getCueDecoration() {
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

	FieldDecoration getWarningDecoration() {
		return FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_WARNING);
	}

	void handleModify(SmartField smartField) {
		// Error indicator supercedes all others
		if (!smartField.isValid()) {
			showError(smartField);
		} else {
			hideError(smartField);
			if (smartField.isWarning()) {
				showWarning(smartField);
			} else {
				hideWarning(smartField);
				if (showContentAssist && smartField.hasContentAssist()) {
					showContentAssistDecoration(smartField, true);
				} else {
					showContentAssistDecoration(smartField, false);
					showRequiredFieldDecoration(smartField,
							showRequiredFieldDecoration);
				}
			}
		}
	}

	GridData getFieldGridData() {
		int margin = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + margin;
		data.horizontalIndent = margin;
		data.grabExcessHorizontalSpace = true;
		return data;

	}

	abstract void showRequiredFieldDecoration(SmartField smartField,
			boolean show);

	abstract void showContentAssistDecoration(SmartField smartField,
			boolean show);

	void handleFocusGained(SmartField smartField) {
		// only set color if error color not already showing
		if (showErrorColor && !smartField.isValid())
			return;
		if (showRequiredFieldColor && smartField.isRequiredField()) {
			smartField.control.setBackground(defaultTextColor);
		}
	}

	void handleFocusLost(SmartField smartField) {
		// only set color if error color not showing
		if (showErrorColor && !smartField.isValid())
			return;
		if (showRequiredFieldColor && smartField.isRequiredField()
				&& smartField.getContents().length() == 0) {
			smartField.control.setBackground(FieldAssistColors
					.getRequiredFieldBackgroundColor(smartField.control));
		}
	}

	void showError(SmartField smartField) {
		FieldDecoration dec = smartField.getErrorDecoration();
		if (showErrorMessage) {
			updateStatus(new Status(IStatus.ERROR,
					"org.eclipse.examples.contentassist", 0, dec //$NON-NLS-1$
							.getDescription(), null));
		}
		if (showErrorDecoration) {
			showErrorDecoration(smartField, true);
		}
		if (showErrorColor) {
			smartField.control.setBackground(getErrorColor(smartField.control));
		}
	}

	abstract void showErrorDecoration(SmartField smartField, boolean show);

	void hideError(SmartField smartField) {
		if (showErrorMessage) {
			this.updateStatus(Status.OK_STATUS);
		}
		if (showErrorDecoration) {
			showErrorDecoration(smartField, false);
		}
		if (showErrorColor) {
			smartField.control.setBackground(defaultTextColor);
		}
	}

	void showWarning(SmartField smartField) {
		if (showWarningDecoration) {
			showWarningDecoration(smartField, true);
		}
	}

	void hideWarning(SmartField smartField) {
		if (showWarningDecoration) {
			showWarningDecoration(smartField, false);
		}
	}

	abstract void showWarningDecoration(SmartField smartField, boolean show);

	void installContentProposalAdapter(Control control,
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
				autoActivationCharacters) {
			public void closeProposalPopup() {
				closeProposalPopup();
			}
		};
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

	void addRequiredFieldIndicator(Label label) {
		String text = label.getText();
		// This concatenation could be done by a field assist helper.
		text = text.concat("*");
		label.setText(text);
	}

	FieldDecoration getRequiredFieldDecoration() {
		return FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_REQUIRED);
	}

	int getDecorationLocationBits() {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		int bits = 0;
		String vert = store
				.getString(PreferenceConstants.PREF_DECORATOR_VERTICALLOCATION);
		if (vert
				.equals(PreferenceConstants.PREF_DECORATOR_VERTICALLOCATION_BOTTOM)) {
			bits = SWT.BOTTOM;
		} else if (vert
				.equals(PreferenceConstants.PREF_DECORATOR_VERTICALLOCATION_CENTER)) {
			bits = SWT.CENTER;
		} else {
			bits = SWT.TOP;
		}

		String horz = store
				.getString(PreferenceConstants.PREF_DECORATOR_HORIZONTALLOCATION);
		if (horz
				.equals(PreferenceConstants.PREF_DECORATOR_HORIZONTALLOCATION_RIGHT)) {
			bits |= SWT.RIGHT;
		} else {
			bits |= SWT.LEFT;
		}
		return bits;
	}
}
