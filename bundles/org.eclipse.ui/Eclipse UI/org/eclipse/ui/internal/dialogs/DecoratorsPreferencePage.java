package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;

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

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 10;
		mainComposite.setLayout(layout);

		Label topLabel = new Label(mainComposite, SWT.NONE);
		topLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.explanation")); //$NON-NLS-1$
		
		createDecoratorsArea(mainComposite);
		createDescriptionArea(mainComposite);
		populateDecorators();

		return mainComposite;
	}

	/** 
	 * Creates the widgets for the list of decorators.
	 */
	private void createDecoratorsArea(Composite mainComposite) {
		Composite decoratorsComposite = new Composite(mainComposite, SWT.NONE);
		decoratorsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout decoratorsLayout = new GridLayout();
		decoratorsLayout.marginWidth = 0;
		decoratorsLayout.marginHeight = 0;
		decoratorsComposite.setLayout(decoratorsLayout);
		
		Label decoratorsLabel = new Label(decoratorsComposite, SWT.NONE);
		decoratorsLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.decoratorsLabel")); //$NON-NLS-1$
		
		// Checkbox table viewer of decorators
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
	}

	/** 
	 * Creates the widgets for the description.
	 */
	private void createDescriptionArea(Composite mainComposite) {
		Composite textComposite = new Composite(mainComposite, SWT.NONE);
		textComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout textLayout = new GridLayout();
		textLayout.marginWidth = 0;
		textLayout.marginHeight = 0;
		textComposite.setLayout(textLayout);
		
		Label descriptionLabel = new Label(textComposite, SWT.NONE);
		descriptionLabel.setText(
			WorkbenchMessages.getString("DecoratorsPreferencePage.description")); //$NON-NLS-1$
		
		descriptionText =
			new Text(textComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		descriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));	}

	/**
	 * Populates the list of decorators.
	 */
	private void populateDecorators() {
		DecoratorDefinition[] definitions = getDefinitions();
		checkboxViewer.setInput(definitions);
		for (int i = 0; i < definitions.length; i++) {
			checkboxViewer.setChecked(definitions[i], definitions[i].isEnabled());
		}
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(DecoratorDefinition definition) {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		String text = definition.getDescription();
		if (text == null || text.length() == 0)
			descriptionText.setText(
				WorkbenchMessages.getString(
					"DecoratorsPreferencePage.noDescription")); //$NON-NLS-1$
		else
			descriptionText.setText(text);
	}

	/**
	 * Clear the selected description in the text.
	 */
	private void clearDescription() {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		descriptionText.setText(""); //$NON-NLS-1$
	}

	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		DecoratorManager manager = (DecoratorManager) WorkbenchPlugin.getDefault().getDecoratorManager();
		DecoratorDefinition[] definitions = manager.getDecoratorDefinitions();
		for (int i = 0; i < definitions.length; i++) {
			checkboxViewer.setChecked(definitions[i],definitions[i].getDefaultValue());
		}
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (super.performOk()) {
			DecoratorManager manager = getDecoratorManager();
			DecoratorDefinition[] definitions = manager.getDecoratorDefinitions();
			for (int i = 0; i < definitions.length; i++) {
				definitions[i].setEnabledWithErrorHandling(checkboxViewer.getChecked(definitions[i]));
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
		return getDecoratorManager().getDecoratorDefinitions();
	}
	
	/**
	 * Get the DecoratorManager being used for this
	 */
	
	private DecoratorManager getDecoratorManager(){
		return (DecoratorManager) WorkbenchPlugin
			.getDefault()
			.getDecoratorManager();
	}

}