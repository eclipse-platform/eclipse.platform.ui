package org.eclipse.ui.examples.fieldassist;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceColors;
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

public class ExampleDialog extends StatusDialog {

	class SpinnerContentAdapter implements IControlContentAdapter {
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
			return 0;
		}

		public Rectangle getInsertionBounds(Control control) {
			return control.getBounds();
		}
	}

	abstract class SmartField {
		DecoratedField field;

		IControlContentAdapter contentAdapter;

		FieldDecoration errorDecoration, warningDecoration;

		SmartField(DecoratedField field, IControlContentAdapter adapter) {
			this.field = field;
			this.contentAdapter = adapter;
		}

		String getContents() {
			return contentAdapter.getControlContents(field.getControl());
		}

		boolean isRequiredField() {
			return true;
		}

		FieldDecoration getErrorDecoration() {
			if (errorDecoration == null) {
				FieldDecoration standardError = FieldDecorationRegistry
						.getDefault().getFieldDecoration(
								FieldAssistPlugin.DEC_ERROR);
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
								FieldAssistPlugin.DEC_WARNING);
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

		String getErrorMessage() {
			return null;
		}

		String getWarningMessage() {
			return null;
		}

	}

	class UserField extends SmartField {

		UserField(DecoratedField field, IControlContentAdapter adapter) {
			super(field, adapter);
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
			return "User name must only contain letters";
		}

		boolean isWarning() {
			return getContents().equals("bob");
		}

		String getWarningMessage() {
			return "Bob is entirely too casual a name.";
		}
	}

	class AgeField extends SmartField {

		AgeField(DecoratedField field, IControlContentAdapter adapter) {
			super(field, adapter);
		}

		boolean isValid() {
			// We seed the spinner with valid values always.
			return true;
		}

		boolean isWarning() {
			Spinner spinner = (Spinner) field.getControl();
			return spinner.getSelection() > 65;
		}

		String getWarningMessage() {
			return "May be eligible for senior citizen user status";
		}
	}

	private String[] validUsers = { "tom", "dick", "harry", "ferdinand", "tim",
			"teresa", "tori", "daniela" };

	private String triggerKey;

	private String username;

	private boolean showErrorDecoration, showErrorMessage, showErrorColor,
			showWarningDecoration, showRequiredFieldColor,
			showRequiredFieldDecoration, showSecondaryPopup;

	private Color defaultTextColor, errorColor;

	public ExampleDialog(Shell parent, String username) {
		super(parent);
		setTitle("Field Assist Example");
		this.username = username;
		getPreferenceValues();
	}

	protected Control createDialogArea(Composite parent) {
		Group main = new Group(parent, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setText("&Security info");
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		main.setLayout(layout);
		applyDialogFont(main);

		Label label = new Label(main, SWT.LEFT);
		label.setText("&User name:");

		IPreferenceStore store = FieldAssistPlugin.getDefault()
				.getPreferenceStore();

		// Create a field representing a user name
		DecoratedField field = new DecoratedField(main, SWT.BORDER,
				new TextControlCreator());
		final SmartField textField = new UserField(field,
				new TextContentAdapter());
		if (store.getBoolean(PreferenceConstants.PREF_SHOWCONTENTPROPOSALCUE)) {
			field.addFieldDecoration(getCueDecoration(), SWT.TOP | SWT.LEFT,
					true);
		}

		if (showRequiredFieldDecoration && textField.isRequiredField()) {
			field.addFieldDecoration(getRequiredFieldDecoration(), SWT.BOTTOM
					| SWT.LEFT, false);
		}

		Text text = (Text) field.getControl();
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
		installContentProposalAdapter(text);
		field.getLayoutControl().setLayoutData(getDecoratedFieldGridData());

		label = new Label(main, SWT.LEFT);
		label.setText("&Combo user name:");

		// Create a combo field representing a user name
		field = new DecoratedField(main, SWT.BORDER | SWT.DROP_DOWN,
				new IControlCreator() {
					public Control createControl(Composite parent, int style) {
						return new Combo(parent, style);
					}
				});
		final SmartField comboField = new UserField(field,
				new ComboContentAdapter());
		if (showRequiredFieldDecoration) {
			field.addFieldDecoration(getRequiredFieldDecoration(), SWT.BOTTOM
					| SWT.LEFT, false);
		}

		Combo combo = (Combo) field.getControl();
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
		field.getLayoutControl().setLayoutData(getDecoratedFieldGridData());

		// Create a spinner representing a user age
		label = new Label(main, SWT.LEFT);
		label.setText("&Age:");

		field = new DecoratedField(main, SWT.BORDER, new IControlCreator() {
			public Control createControl(Composite parent, int style) {
				return new Spinner(parent, style);
			}
		});
		if (showRequiredFieldDecoration) {
			field.addFieldDecoration(getRequiredFieldDecoration(), SWT.BOTTOM
					| SWT.LEFT, false);
		}
		final SmartField spinnerField = new AgeField(field,
				new SpinnerContentAdapter());

		Spinner spinner = (Spinner) field.getControl();
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
		field.getLayoutControl().setLayoutData(getDecoratedFieldGridData());

		// This field is not managed by a decorated field
		label = new Label(main, SWT.LEFT);
		label.setText("&Password:");
		// We need to indent the field by the size of the decoration.
		text = new Text(main, SWT.BORDER);
		text.setText("******");
		text.setLayoutData(getNonDecoratedFieldGridData());

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
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		FieldDecoration dec = registry
				.getFieldDecoration(FieldAssistPlugin.DEC_CONTENTASSIST);
		if (dec == null) {
			registry.registerFieldDecoration(
					FieldAssistPlugin.DEC_CONTENTASSIST, NLS.bind(
							TaskAssistExampleMessages.Decorator_ContentAssist,
							triggerKey), FieldAssistPlugin.DEC_CONTENTASSIST);
			dec = registry
					.getFieldDecoration(FieldAssistPlugin.DEC_CONTENTASSIST);
		}
		return dec;
	}

	private FieldDecoration getWarningDecoration() {
		return FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldAssistPlugin.DEC_WARNING);
	}

	private FieldDecoration getRequiredFieldDecoration() {
		return FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldAssistPlugin.DEC_REQUIRED);
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
				if (smartField.isRequiredField()) {
					smartField.field.addFieldDecoration(
							getRequiredFieldDecoration(),
							SWT.BOTTOM | SWT.LEFT, false);
				}
			}
		}
	}

	private void handleFocusGained(SmartField smartField) {
		// only set color if error color not already showing
		if (showErrorColor && !smartField.isValid())
			return;
		if (showRequiredFieldColor && smartField.isRequiredField()) {
			smartField.field.getControl().setBackground(defaultTextColor);
		}
	}

	private void handleFocusLost(SmartField smartField) {
		// only set color if error color not showing
		if (showErrorColor && !smartField.isValid())
			return;
		if (showRequiredFieldColor && smartField.isRequiredField()
				&& smartField.getContents().length() == 0) {
			smartField.field.getControl().setBackground(
					smartField.field.getControl().getDisplay().getSystemColor(
							SWT.COLOR_INFO_BACKGROUND));
		}
	}

	private void showError(SmartField smartField) {
		FieldDecoration dec = smartField.getErrorDecoration();
		if (showErrorMessage) {
			updateStatus(new Status(Status.ERROR,
					"org.eclipse.examples.contentassist", 0, dec
							.getDescription(), null));
		}
		if (showErrorDecoration) {
			smartField.field.addFieldDecoration(dec, SWT.BOTTOM | SWT.LEFT, false);
		}
		if (showErrorColor) {
			smartField.field.getControl().setBackground(
					getErrorColor(smartField.field.getControl()));
		}
	}

	private void hideError(SmartField smartField) {
		FieldDecoration dec = smartField.getErrorDecoration();
		if (showErrorMessage) {
			this.updateStatus(Status.OK_STATUS);
		}
		if (showErrorDecoration) {
			smartField.field.hideDecoration(dec);
		}
		if (showErrorColor) {
			smartField.field.getControl().setBackground(defaultTextColor);
		}
	}

	private void showWarning(SmartField smartField) {
		if (showWarningDecoration) {
			FieldDecoration dec = smartField.getWarningDecoration();
			smartField.field.addFieldDecoration(dec, SWT.BOTTOM | SWT.LEFT,
					false);
		}
	}

	private void hideWarning(SmartField smartField) {
		if (showWarningDecoration) {
			smartField.field.hideDecoration(getWarningDecoration());
		}
	}

	private void installContentProposalAdapter(Text text) {
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

		ContentProposalAdapter adapter = new ContentProposalAdapter(text,
				new TextContentAdapter(), getContentProposalProvider(),
				keyStroke, autoActivationCharacters);
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
												"{0} is a wonderful choice and you should seriously consider it.",
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

	private GridData getDecoratedFieldGridData() {
		return new GridData(IDialogConstants.ENTRY_FIELD_WIDTH, SWT.DEFAULT);

	}

	private GridData getNonDecoratedFieldGridData() {
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.horizontalIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		return data;
	}

	private RGB computeErrorColor(Control control) {
		// Takes into account both the magnitude of each color component
		// and significant variation from the average magnitude.
		Color background = control.getBackground();
		Color error = JFaceColors.getErrorText(control.getDisplay());
		int average = (error.getRed() + error.getBlue() + error.getGreen()) / 3;
		int rMore = (int) (0.08 * error.getRed())
				+ (int) (0.05 * (error.getRed() - average));
		int gMore = (int) (0.08 * error.getGreen())
				+ (int) (0.05 * (error.getGreen() - average));
		int bMore = (int) (0.08 * error.getBlue())
				+ (int) (0.05 * (error.getBlue() - average));
		int r = background.getRed();
		int g = background.getGreen();
		int b = background.getBlue();
		if (r <= 255 - rMore) {
			r += rMore;
		} else {
			g -= rMore;
			b -= rMore;
		}
		if (g <= 255 - gMore) {
			g += gMore;
		} else {
			r -= gMore;
			b -= gMore;
		}
		if (b <= 255 - bMore) {
			b += bMore;
		} else {
			r -= bMore;
			g -= bMore;
		}
		r = Math.max(0, Math.min(255, r));
		g = Math.max(0, Math.min(255, g));
		b = Math.max(0, Math.min(255, b));
		return new RGB(r, g, b);
	}

	private Color getErrorColor(Control control) {
		if (errorColor == null) {
			RGB rgb = computeErrorColor(control);
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

}
