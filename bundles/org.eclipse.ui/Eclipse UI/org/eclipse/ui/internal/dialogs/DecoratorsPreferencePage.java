package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.win32.CREATESTRUCT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.*;

/**
 * The DecoratorsPreferencePage is the preference page for enabling and disabling
 * the decorators in the image and for giving the user a description of the decorator.
 */

public class DecoratorsPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Text descriptionText;
	private CheckboxTableViewer checkboxViewer;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mainComposite.setLayout(new GridLayout());

		Label topLabel = new Label(mainComposite, SWT.NONE);
		topLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.explanation"));

		Composite decoratorsComposite = new Composite(mainComposite, SWT.NONE);
		decoratorsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		decoratorsComposite.setLayout(new GridLayout());

		Label decoratorsLabel = new Label(decoratorsComposite, SWT.NONE);
		decoratorsLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.decoratorsLabel"));
		
		// Checkbox tree viewer of capabilities in selected categories
		checkboxViewer =
			CheckboxTableViewer.newCheckList(
				decoratorsComposite,
				SWT.SINGLE | SWT.TOP | SWT.BORDER);
		checkboxViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((DecoratorDefinition) element).getName();
			}
		});

		checkboxViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
				//Nothing to do on dispose
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public Object[] getElements(Object inputElement) {
				//Make an entry for each decorator definition
				return (DecoratorDefinition[]) inputElement;
			}

		});

		checkboxViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					DecoratorDefinition definition = 
						(DecoratorDefinition) sel.getFirstElement();
					if (definition == null)
						clearDescription();
					else
						showDescription(definition);
				}
			}
		});

		checkboxViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkboxViewer.setSelection(
					new StructuredSelection(event.getElement()));
			}
		});
		
		DecoratorDefinition[] definitions = getDefinitions();
		checkboxViewer.setInput(definitions);
		for (int i = 0; i < definitions.length; i++) {
			checkboxViewer.setChecked(definitions[i], definitions[i].isEnabled());
		}

		Composite textComposite = new Composite(mainComposite, SWT.NONE);
		textComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		textComposite.setLayout(new GridLayout());

		Label descriptionLabel = new Label(textComposite, SWT.NONE);
		descriptionLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.description"));

		descriptionText = new Text(textComposite, SWT.READ_ONLY | SWT.BORDER);
		descriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));

		return mainComposite;
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(DecoratorDefinition definition) {
		String text = definition.getDescription();
		if (text == null || text.length() == 0)
			descriptionText.setText(
				WorkbenchMessages.getString(
					"DecoratorsPreferencePage.noDescription"));
		else
			descriptionText.setText(text);
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
		checkboxViewer.setAllChecked(false);
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (super.performOk()) {
			DecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
			DecoratorDefinition[] definitions = manager.getDecoratorDefinitions();
			for (int i = 0; i < definitions.length; i++) {
				definitions[i].setEnabled(checkboxViewer.getChecked(definitions[i]));
			}
			manager.reset();
			return true;
		}
		return false;
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * Get the decorator definitions for the workbench.
	 */
	private DecoratorDefinition[] getDefinitions() {
		return WorkbenchPlugin
			.getDefault()
			.getDecoratorManager()
			.getDecoratorDefinitions();
	}

}