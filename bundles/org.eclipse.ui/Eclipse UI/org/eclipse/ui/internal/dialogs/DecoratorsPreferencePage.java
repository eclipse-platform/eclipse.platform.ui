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

		Composite mainComposite = new Composite(parent, SWT.NULL);

		GridData data =
			new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		mainComposite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		Label topLabel = new Label(mainComposite, SWT.NULL);
		topLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.explanation"));
		GridData topData = new GridData();
		topData.horizontalSpan = 2;
		topLabel.setLayoutData(topData);

		//Create an intermediate composite to facilitate tab traversal
		Composite definitionsComposite = new Composite(mainComposite, SWT.NULL);
		definitionsComposite.setLayout(new GridLayout());
		GridData definitionsData =
			new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING
					| GridData.FILL_VERTICAL
					| GridData.GRAB_VERTICAL);
		definitionsComposite.setLayoutData(definitionsData);

		Label decoratorsLabel = new Label(definitionsComposite, SWT.NULL);
		decoratorsLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.decoratorsLabel"));

		// Checkbox tree viewer of capabilities in selected categories
		checkboxViewer =
			CheckboxTableViewer.newCheckList(
				definitionsComposite,
				SWT.SINGLE | SWT.TOP | SWT.BORDER);
		checkboxViewer.getTable().setLayoutData(
			new GridData(GridData.FILL_BOTH | GridData.FILL_VERTICAL));
		checkboxViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				return null;
			}
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
				return getDefinitions();
			}

		});

		checkboxViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					DecoratorDefinition definition =
						(DecoratorDefinition) ((IStructuredSelection) event.getSelection())
							.getFirstElement();
					if (definition == null)
						clearDescription();
					else
						showDescription(definition);
				}
			}
		});

		checkboxViewer.setInput(new ArrayList());
		DecoratorDefinition[] definitions = getDefinitions();
		for (int i = 0; i < definitions.length; i++) {
			checkboxViewer.setChecked(definitions[i], definitions[i].isEnabled());
		}

		Composite textComposite = new Composite(mainComposite, SWT.NULL);

		GridData textData =
			new GridData(
				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		textComposite.setLayoutData(textData);
		textComposite.setLayout(new GridLayout());

		Label descriptionLabel = new Label(textComposite, SWT.NULL);
		descriptionLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.description"));

		descriptionText = new Text(textComposite, SWT.READ_ONLY | SWT.BORDER);
		textData =
			new GridData(
				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		descriptionText.setLayoutData(textData);

		return mainComposite;
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(DecoratorDefinition definition) {
		String text = definition.getDescription();
		if (text.length() == 0)
			descriptionText.setText(
				WorkbenchMessages.format(
					"DecoratorsPreferencePage.noDescription",
					new String[] { definition.getName()}));
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

	/*
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

	/*
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