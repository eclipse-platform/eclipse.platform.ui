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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ComboControlCreator;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

/**
 * Example dialog that shows different field assist capabilities.
 */
public class DecoratedFieldTestDialog extends FieldAssistTestDialog {

	/**
	 * Open the exapmle dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param username
	 *            the default username
	 */
	public DecoratedFieldTestDialog(Shell parent, String username) {
		super(parent, username);
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
		DecoratedField field = new DecoratedField(main, SWT.BORDER,
				new TextControlCreator());
		final SmartField textField = new UserField(field, field.getControl(),
				new TextContentAdapter());
		field.addFieldDecoration(getCueDecoration(),
				getDecorationLocationBits(), true);
		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
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
		installContentProposalAdapter(text, new TextContentAdapter());
		field.getLayoutControl().setLayoutData(getDecoratedFieldGridData());
		// prime the required field color by calling the focus lost handler.
		handleFocusLost(textField);

		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_ComboUserName);

		// Create a combo field representing a user name
		field = new DecoratedField(main, SWT.BORDER | SWT.DROP_DOWN,
				new ComboControlCreator());
		final SmartField comboField = new UserField(field, field.getControl(),
				new ComboContentAdapter());
		field.addFieldDecoration(getCueDecoration(),
				getDecorationLocationBits(), true);
		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
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
		installContentProposalAdapter(combo, new ComboContentAdapter());
		// prime the required field color by calling the focus lost handler.
		handleFocusLost(comboField);

		// Create a spinner representing a user age
		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_Age);

		field = new DecoratedField(main, SWT.BORDER, new IControlCreator() {
			public Control createControl(Composite parent, int style) {
				return new Spinner(parent, style);
			}
		});
		field.addFieldDecoration(getRequiredFieldDecoration(),
				getDecorationLocationBits(), false);
		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
		}
		final SmartField spinnerField = new AgeField(field, field.getControl(),
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

		// prime the required field color by calling the focus lost handler.
		handleFocusLost(spinnerField);

		// This field is not managed by a decorated field
		label = new Label(main, SWT.LEFT);
		label.setText(TaskAssistExampleMessages.ExampleDialog_Password);
		// We need to indent the field by the size of the decoration.
		text = new Text(main, SWT.BORDER);
		text.setText("******"); //$NON-NLS-1$

		// If the decorators are on the left, then we need to allocate
		// margin space so that this field lines up
		if ((getDecorationLocationBits() & SWT.LEFT) == SWT.LEFT) {
			text.setLayoutData(getNonDecoratedFieldGridData());
		} else {
			text.setLayoutData(getDecoratedFieldGridData());
		}
		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
		}
	}

	void showErrorDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = smartField.getErrorDecoration();
		DecoratedField field = (DecoratedField) smartField.decImpl;
		if (show) {
			field.addFieldDecoration(dec, getDecorationLocationBits(), false);
			field.showDecoration(dec);
		} else {
			field.hideDecoration(dec);
		}
	}

	void showWarningDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = smartField.getWarningDecoration();
		DecoratedField field = (DecoratedField) smartField.decImpl;
		if (show) {
			field.addFieldDecoration(dec, getDecorationLocationBits(), false);
			field.showDecoration(dec);
		} else {
			field.hideDecoration(dec);
		}
	}

	void showContentAssistDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = getCueDecoration();
		DecoratedField field = (DecoratedField) smartField.decImpl;
		if (show) {
			field.addFieldDecoration(dec, getDecorationLocationBits(), true);
			field.showDecoration(dec);
		} else {
			field.hideDecoration(dec);
		}
	}

	void showRequiredFieldDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = getRequiredFieldDecoration();
		DecoratedField field = (DecoratedField) smartField.decImpl;
		if (show) {
			field.addFieldDecoration(dec, getDecorationLocationBits(), false);
			field.showDecoration(dec);
		} else {
			field.hideDecoration(dec);
		}
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

	public boolean close() {
		if (errorColor != null) {
			errorColor.dispose();
		}
		return super.close();
	}

}
