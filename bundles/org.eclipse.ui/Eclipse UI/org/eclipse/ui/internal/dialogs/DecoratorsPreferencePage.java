package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.DecoratorDefinition;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The DecoratorsPreferencePage is the preference page for enabling and disabling
 * the decorators in the image and for giving the user a description of the decorator.
 */

public class DecoratorsPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage{

	Text descriptionText;
	Button[] decoratorButtons;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite mainComposite = new Composite(parent, SWT.NULL);

		GridData data =
			new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		mainComposite.setLayoutData(data);
		mainComposite.setLayout(new GridLayout());

		//Make an entry for each decorator definition
		DecoratorDefinition[] definitions =
			WorkbenchPlugin.getDefault().getDecoratorManager().getDecoratorDefinitions();
		decoratorButtons = new Button[definitions.length];

		//Create an intermediate composite to facilitate tab traversal
		Composite definitionsComposite = new Composite(mainComposite, SWT.NULL);
		definitionsComposite.setLayout(new GridLayout());

		for (int i = 0; i < definitions.length; i++) {
			final DecoratorDefinition definition = definitions[i];
			Button decoratorButton = new Button(definitionsComposite, SWT.CHECK);
			decoratorButton.setText(definition.getName());
			decoratorButton.setSelection(definition.isEnabled());
			decoratorButton.setData(definition);
			decoratorButtons[i] = decoratorButton;

			decoratorButton.addFocusListener(new FocusListener() {

				//Show the description in the descriptionPane
				public void focusGained(FocusEvent e) {
					showDescription(definition);
				}

				public void focusLost(FocusEvent e) {
					clearDescription();
				}
			});

		}

		descriptionText = new Text(mainComposite, SWT.READ_ONLY | SWT.BORDER);
		GridData textData =
			new GridData(
				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		descriptionText.setLayoutData(textData);

		return mainComposite;
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(DecoratorDefinition definition) {
		descriptionText.setText(definition.getDescription());
	}

	/**
	 * Clear the selected description in the text.
	 */
	private void clearDescription() {
		descriptionText.setText("");
	}

	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		for (int i = 0; i < decoratorButtons.length; i++) {
			//Turn off all of the decorators by default
			decoratorButtons[i].setSelection(false);
		}
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (super.performOk()) {
			for (int i = 0; i < decoratorButtons.length; i++) {
				DecoratorDefinition definition =
					(DecoratorDefinition) decoratorButtons[i].getData();
				boolean enabled = decoratorButtons[i].getSelection();
				if (enabled != definition.isEnabled())
					definition.setEnabled(enabled);
			}
			WorkbenchPlugin.getDefault().getDecoratorManager().reset();
			return true;
		}
		return false;
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}