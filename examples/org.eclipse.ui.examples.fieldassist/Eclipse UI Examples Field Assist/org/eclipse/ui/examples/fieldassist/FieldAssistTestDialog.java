/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.examples.fieldassist.preferences.PreferenceConstants;

/**
 * Example dialog that shows different field assist capabilities.
 */
public class FieldAssistTestDialog extends StatusDialog {

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

		Control control;

		IControlContentAdapter contentAdapter;

		FieldDecoration errorDecoration, warningDecoration;

		SmartField(ControlDecoration dec, Control control,
				IControlContentAdapter adapter) {
			this.controlDecoration = dec;
			this.contentAdapter = adapter;
			this.control = control;
		}

		boolean isRequiredField() {
			return true;
		}

		boolean hasQuickFix() {
			return false;
		}

		void quickFix() {
			// do nothing
		}

		boolean hasContentAssist() {
			return false;
		}

		void dispose() {
			// do nothing
		}

		FieldDecoration getErrorDecoration() {
			if (errorDecoration == null) {
				FieldDecoration standardError;
				if (hasQuickFix()) {
					standardError = FieldDecorationRegistry.getDefault()
							.getFieldDecoration(
									FieldDecorationRegistry.DEC_ERROR_QUICKFIX);
				} else {
					standardError = FieldDecorationRegistry.getDefault()
							.getFieldDecoration(
									FieldDecorationRegistry.DEC_ERROR);
				}
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

		void setContents(String contents) {
			contentAdapter.setControlContents(control, contents, contents
					.length());
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
		Menu quickFixMenu;

		UserField(ControlDecoration dec, Control control,
				IControlContentAdapter adapter) {
			super(dec, control, adapter);
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

		boolean hasQuickFix() {
			return true;
		}

		void quickFix() {
			String contents = getContents();
			StringBuffer lettersOnly = new StringBuffer();
			int length = contents.length();
			for (int i = 0; i < length;) {
				char ch = contents.charAt(i++);
				if (Character.isLetter(ch)) {
					lettersOnly.append(ch);
				}
			}
			setContents(lettersOnly.toString());
		}

		void dispose() {
			if (quickFixMenu != null) {
				quickFixMenu.dispose();
				quickFixMenu = null;
			}
		}
	}

	class AgeField extends SmartField {

		AgeField(ControlDecoration dec, Control control,
				IControlContentAdapter adapter) {
			super(dec, control, adapter);
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

	String[] validUsers = { "tom", "dick", "harry", "ferdinand", "tim", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"teresa", "tori", "daniela", "aaron", "kevin", "tod", "mike", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"kim", "eric", "paul", "todd" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	String triggerKey;

	String username;

	boolean showErrorDecoration, showErrorMessage, showWarningDecoration,
			showRequiredFieldDecoration, showRequiredFieldLabelIndicator,
			showSecondaryPopup, showContentAssist;

	int marginWidth;

	UserField textField, comboField;

	/**
	 * Open the example dialog.
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

		// Another one to test combos
		label = new Label(autoComplete, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_ComboUserName);

		Combo combo = new Combo(autoComplete, SWT.BORDER | SWT.DROP_DOWN);
		combo.setText(username);
		combo.setItems(validUsers);
		combo.setLayoutData(getFieldGridData());
		new AutoCompleteField(combo, new ComboContentAdapter(), validUsers);

		Dialog.applyDialogFont(outer);

		return outer;
	}

	private void getPreferenceValues() {
		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();
		showErrorMessage = store
				.getBoolean(PreferenceConstants.PREF_SHOWERRORMESSAGE);
		showErrorDecoration = store
				.getBoolean(PreferenceConstants.PREF_SHOWERRORDECORATION);
		showWarningDecoration = store
				.getBoolean(PreferenceConstants.PREF_SHOWWARNINGDECORATION);
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
	
	GridData getMultiLineTextFieldGridData() {
		int margin = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + margin;
		data.heightHint = JFaceResources.getDialogFont().getFontData()[0].getHeight()*5;
		data.horizontalIndent = margin;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		return data;

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
	}

	void hideError(SmartField smartField) {
		if (showErrorMessage) {
			this.updateStatus(Status.OK_STATUS);
		}
		if (showErrorDecoration) {
			showErrorDecoration(smartField, false);
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
							if (showSecondaryPopup && !user.equals("tori"))  //$NON-NLS-1$
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

	void addRequiredFieldIndicator(Label label) {
		String text = label.getText();
		// This concatenation could be done by a field assist helper.
		text = text.concat("*"); //$NON-NLS-1$
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

	void createSecurityGroup(Composite parent) {

		Group main = new Group(parent, SWT.NONE);
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
		ControlDecoration dec = new ControlDecoration(text,
				getDecorationLocationBits());
		dec.setMarginWidth(marginWidth);
		dec.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				MessageDialog
						.openInformation(
								getShell(),
								TaskAssistExampleMessages.ExampleDialog_SelectionTitle,
								TaskAssistExampleMessages.ExampleDialog_SelectionMessage);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// Nothing on default select
			}
		});

		textField = new UserField(dec, text, new TextContentAdapter());
		dec.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent event) {
				// no quick fix if we aren't in error state.
				if (textField.isValid()) {
					return;
				}
				if (textField.quickFixMenu == null) {
					textField.quickFixMenu = createQuickFixMenu(textField);
				}
				textField.quickFixMenu.setLocation(event.x, event.y);
				textField.quickFixMenu.setVisible(true);
			}
		});
		if (showRequiredFieldLabelIndicator && textField.isRequiredField()) {
			addRequiredFieldIndicator(label);
		}
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleModify(textField);
			}
		});

		text.setText(username);
		installContentProposalAdapter(text, new TextContentAdapter());
		text.setLayoutData(getFieldGridData());

		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_ComboUserName);

		// Create a combo field representing a user name
		Combo combo = new Combo(main, SWT.BORDER | SWT.DROP_DOWN);
		dec = new ControlDecoration(combo, getDecorationLocationBits());
		dec.setMarginWidth(marginWidth);
		comboField = new UserField(dec, combo, new ComboContentAdapter());

		dec.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent event) {
				// no quick fix if we aren't in error state.
				if (comboField.isValid()) {
					return;
				}
				if (comboField.quickFixMenu == null) {
					comboField.quickFixMenu = createQuickFixMenu(comboField);
				}
				comboField.quickFixMenu.setLocation(event.x, event.y);
				comboField.quickFixMenu.setVisible(true);
			}
		});
		dec.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {
				MessageDialog
						.openInformation(
								getShell(),
								TaskAssistExampleMessages.ExampleDialog_DefaultSelectionTitle,
								TaskAssistExampleMessages.ExampleDialog_DefaultSelectionMessage);
			}

			public void widgetSelected(SelectionEvent e) {
				// Do nothing on selection
			}
		});

		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
		}
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleModify(comboField);
			}
		});

		combo.setText(username);
		combo.setItems(validUsers);
		combo.setLayoutData(getFieldGridData());
		installContentProposalAdapter(combo, new ComboContentAdapter());

		// Create a spinner representing a user age
		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_Age);

		Spinner spinner = new Spinner(main, SWT.BORDER);
		dec = new ControlDecoration(spinner, getDecorationLocationBits());
		dec.setMarginWidth(marginWidth);

		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
		}
		final SmartField spinnerField = new AgeField(dec, spinner,
				new SpinnerContentAdapter());
		spinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				handleModify(spinnerField);
			}
		});
		spinner.setSelection(40);
		spinner.setLayoutData(getFieldGridData());

		// This field has no decorator
		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_Password);
		text = new Text(main, SWT.BORDER | SWT.PASSWORD);
		text.setText("******"); //$NON-NLS-1$
		text.setLayoutData(getFieldGridData());
		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
		}
		
		// This tests multi-line text popup placement
		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.FieldAssistTestDialog_Comments);
		text = new Text(main, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		text.setText(TaskAssistExampleMessages.FieldAssistTestDialog_CommentsDefaultContent); 
		text.setLayoutData(getMultiLineTextFieldGridData());
		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
		}
		installContentProposalAdapter(text, new TextContentAdapter());

	}

	Menu createQuickFixMenu(final SmartField field) {
		Menu newMenu = new Menu(field.control);
		MenuItem item = new MenuItem(newMenu, SWT.PUSH);
		item
				.setText(TaskAssistExampleMessages.ExampleDialog_DecorationMenuItem);
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				field.quickFix();
			}

			public void widgetDefaultSelected(SelectionEvent event) {

			}
		});
		return newMenu;
	}

	void showErrorDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = smartField.getErrorDecoration();
		ControlDecoration cd = smartField.controlDecoration;
		if (show) {
			cd.setImage(dec.getImage());
			cd.setDescriptionText(dec.getDescription());
			cd.setShowOnlyOnFocus(false);
			cd.show();
		} else {
			cd.hide();
		}
	}

	void showWarningDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = smartField.getWarningDecoration();
		ControlDecoration cd = smartField.controlDecoration;
		if (show) {
			cd.setImage(dec.getImage());
			cd.setDescriptionText(dec.getDescription());
			cd.setShowOnlyOnFocus(false);
			cd.show();
		} else {
			cd.hide();
		}
	}

	void showRequiredFieldDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = getRequiredFieldDecoration();
		ControlDecoration cd = smartField.controlDecoration;
		if (show) {
			cd.setImage(dec.getImage());
			cd.setDescriptionText(dec.getDescription());
			cd.setShowOnlyOnFocus(false);
			cd.show();
		} else {
			cd.hide();
		}
	}

	void showContentAssistDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = getCueDecoration();
		ControlDecoration cd = smartField.controlDecoration;
		if (show) {
			cd.setImage(dec.getImage());
			cd.setDescriptionText(dec.getDescription());
			cd.setShowOnlyOnFocus(true);
			cd.show();
		} else {
			cd.hide();
		}
	}

	public boolean close() {
		textField.dispose();
		comboField.dispose();
		return super.close();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}
}
