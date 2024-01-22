/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;

/**
 * The DecoratorsPreferencePage is the preference page for enabling and
 * disabling the decorators in the image and for giving the user a description
 * of the decorator.
 */
public class DecoratorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text descriptionText;

	private CheckboxTableViewer checkboxViewer;

	/**
	 * Create decorators preference page with description header.
	 */
	public DecoratorsPreferencePage() {
		setDescription(WorkbenchMessages.DecoratorsPreferencePage_explanation);
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		Font font = parent.getFont();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.DECORATORS_PREFERENCE_PAGE);

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mainComposite.setFont(font);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 10;
		mainComposite.setLayout(layout);

		createDecoratorsArea(mainComposite);
		createDescriptionArea(mainComposite);
		populateDecorators();

		return mainComposite;
	}

	/**
	 * Creates the widgets for the list of decorators.
	 */
	private void createDecoratorsArea(Composite mainComposite) {

		Font mainFont = mainComposite.getFont();
		Composite decoratorsComposite = new Composite(mainComposite, SWT.NONE);
		decoratorsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout decoratorsLayout = new GridLayout();
		decoratorsLayout.marginWidth = 0;
		decoratorsLayout.marginHeight = 0;
		decoratorsComposite.setLayout(decoratorsLayout);
		decoratorsComposite.setFont(mainFont);

		Label decoratorsLabel = new Label(decoratorsComposite, SWT.NONE);
		decoratorsLabel.setText(WorkbenchMessages.DecoratorsPreferencePage_decoratorsLabel);
		decoratorsLabel.setFont(mainFont);

		// Checkbox table viewer of decorators
		checkboxViewer = CheckboxTableViewer.newCheckList(decoratorsComposite, SWT.SINGLE | SWT.TOP | SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 300;
		checkboxViewer.getTable().setLayoutData(layoutData);
		checkboxViewer.getTable().setFont(decoratorsComposite.getFont());
		checkboxViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DecoratorDefinition) element).getName();
			}
		});
		checkboxViewer.getTable().setFont(mainFont);

		checkboxViewer.setContentProvider(new IStructuredContentProvider() {
			private final Comparator comparer = new Comparator() {
				private Collator collator = Collator.getInstance();

				@Override
				public int compare(Object arg0, Object arg1) {
					String s1 = ((DecoratorDefinition) arg0).getName();
					String s2 = ((DecoratorDefinition) arg1).getName();
					return collator.compare(s1, s2);
				}
			};

			@Override
			public void dispose() {
				// Nothing to do on dispose
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				// Make an entry for each decorator definition
				Object[] elements = (Object[]) inputElement;
				Object[] results = new Object[elements.length];
				System.arraycopy(elements, 0, results, 0, elements.length);
				Arrays.asList(results).sort(comparer);
				return results;
			}

		});

		checkboxViewer.addSelectionChangedListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection sel = event.getStructuredSelection();
				DecoratorDefinition definition = (DecoratorDefinition) sel.getFirstElement();
				if (definition == null) {
					clearDescription();
				} else {
					showDescription(definition);
				}
			}
		});

		checkboxViewer.addCheckStateListener(
				event -> checkboxViewer.setSelection(new StructuredSelection(event.getElement())));
	}

	/**
	 * Creates the widgets for the description.
	 */
	private void createDescriptionArea(Composite mainComposite) {

		Font mainFont = mainComposite.getFont();
		Composite textComposite = new Composite(mainComposite, SWT.NONE);
		textComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout textLayout = new GridLayout();
		textLayout.marginWidth = 0;
		textLayout.marginHeight = 0;
		textComposite.setLayout(textLayout);
		textComposite.setFont(mainFont);

		Label descriptionLabel = new Label(textComposite, SWT.NONE);
		descriptionLabel.setText(WorkbenchMessages.DecoratorsPreferencePage_description);
		descriptionLabel.setFont(mainFont);

		descriptionText = new Text(textComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 50;
		descriptionText.setLayoutData(layoutData);
		descriptionText.setFont(mainFont);
	}

	/**
	 * Populates the list of decorators.
	 */
	private void populateDecorators() {
		DecoratorDefinition[] definitions = getAllDefinitions();
		checkboxViewer.setInput(definitions);
		for (DecoratorDefinition definition : definitions) {
			checkboxViewer.setChecked(definition, definition.isEnabled());
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
		if (text == null || text.isEmpty()) {
			descriptionText.setText(WorkbenchMessages.PreferencePage_noDescription);
		} else {
			descriptionText.setText(text);
		}
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
	@Override
	protected void performDefaults() {
		super.performDefaults();
		DecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
		for (DecoratorDefinition definition : manager.getAllDecoratorDefinitions()) {
			checkboxViewer.setChecked(definition, definition.getDefaultValue());
		}
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (super.performOk()) {
			DecoratorManager manager = getDecoratorManager();
			// Clear the caches first to avoid unneccessary updates
			manager.clearCaches();
			for (DecoratorDefinition definition : manager.getAllDecoratorDefinitions()) {
				boolean checked = checkboxViewer.getChecked(definition);
				definition.setEnabled(checked);

			}
			// Have the manager clear again as there may have been
			// extra updates fired by the enablement changes.
			manager.clearCaches();
			manager.updateForEnablementChange();
			return true;
		}
		return false;
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/**
	 * Get the decorator definitions for the workbench.
	 */
	private DecoratorDefinition[] getAllDefinitions() {
		return getDecoratorManager().getAllDecoratorDefinitions();
	}

	/**
	 * Get the DecoratorManager being used for this page.
	 *
	 * @return the decorator manager
	 */
	private DecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

}
