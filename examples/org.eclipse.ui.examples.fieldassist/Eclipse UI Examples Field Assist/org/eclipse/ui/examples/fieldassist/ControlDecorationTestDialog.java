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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

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
public class ControlDecorationTestDialog extends FieldAssistTestDialog {

	Menu menu;

	/**
	 * Open the exapmle dialog.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param username
	 *            the default username
	 */
	public ControlDecorationTestDialog(Shell parent, String username) {
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
		Text text = new Text(main, SWT.BORDER);
		ControlDecoration dec = new ControlDecoration(text,
				getDecorationLocationBits());
		dec.setMarginWidth(marginWidth);
		dec.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				MessageDialog.openInformation(getShell(),
						TaskAssistExampleMessages.ExampleDialog_SelectionTitle, 
						TaskAssistExampleMessages.ExampleDialog_SelectionMessage);
			}
		});

		final SmartField textField = new UserField(dec, text,
				new TextContentAdapter());
		if (showRequiredFieldLabelIndicator && textField.isRequiredField()) {
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
		dec = new ControlDecoration(combo, getDecorationLocationBits());
		dec.setMarginWidth(marginWidth);
		dec.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				Control control = (Control) event.widget;
				if (menu == null) {
					menu = new Menu(control);
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(TaskAssistExampleMessages.ExampleDialog_DecorationMenuItem);
					item.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent event) {
							MessageDialog.openInformation(getShell(),
									TaskAssistExampleMessages.ExampleDialog_DecorationMenuSelectedTitle,
									TaskAssistExampleMessages.ExampleDialog_DecorationMenuSelectedMessage);
						}

						public void widgetDefaultSelected(SelectionEvent event) {

						}
					});
				}
				menu.setLocation(event.x, event.y);
				menu.setVisible(true);
			}
		});
		dec.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event event) {
				MessageDialog.openInformation(getShell(),
						TaskAssistExampleMessages.ExampleDialog_DefaultSelectionTitle, 
						TaskAssistExampleMessages.ExampleDialog_DefaultSelectionMessage);
			}
		});

		final SmartField comboField = new UserField(dec, combo,
				new ComboContentAdapter());
		if (showRequiredFieldLabelIndicator) {
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
		if (showRequiredFieldLabelIndicator) {
			addRequiredFieldIndicator(label);
		}
	}

	void showErrorDecoration(SmartField smartField, boolean show) {
		FieldDecoration dec = smartField.getErrorDecoration();
		ControlDecoration cd = (ControlDecoration) smartField.decImpl;
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
		ControlDecoration cd = (ControlDecoration) smartField.decImpl;
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
		ControlDecoration cd = (ControlDecoration) smartField.decImpl;
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
		ControlDecoration cd = (ControlDecoration) smartField.decImpl;
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
		if (menu != null) {
			menu.dispose();
		}
		return super.close();
	}
}
