/*******************************************************************************
 * Copyright (c) 2013 Remain BV, Industrial-TSI BV and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongmam <wim.jongman@remainsoftware.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.imp;

import java.util.ArrayList;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

public class ModelImportPage1 extends WizardPage {

	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IConfigurationElement) {
				IConfigurationElement config = (IConfigurationElement) element;
				return config.getAttribute(wizard.getMappingName());
			}
			return element.toString();
		}
	}

	private IExtensionRegistry registry;
	private ModelImportWizard wizard;
	protected Object[] checkedElements;

	private class TableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {

			if (!(inputElement instanceof RegistryStruct)) {
				return new String[] { "Wrong input" };
			}

			RegistryStruct input = (RegistryStruct) inputElement;

			return RegistryUtil.getExtensions(registry, input, wizard.isLiveModel());
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class ComboContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return RegistryUtil.getProvidingBundles(registry, wizard.getExtensionPoint(), wizard.isLiveModel());
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Create the wizard.
	 */
	public ModelImportPage1() {
		super("wizardPage");
		setTitle("Import ");
		setDescription("Select plug-in and elements to import");
		wizard = (ModelImportWizard) getWizard();
		setPageComplete(false);
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));

		ComboViewer comboViewer = new ComboViewer(container, SWT.NONE);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		final CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		TableColumn column = tableViewerColumn.getColumn();
		column.setResizable(false);
		tcl_composite.setColumnData(column, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
		column.setText("Description");
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.setContentProvider(new TableContentProvider());
		comboViewer.setContentProvider(new ComboContentProvider());

		comboViewer.setInput("go");

		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				String bundle = ((IStructuredSelection) event.getSelection()).getFirstElement().toString();
				RegistryStruct struct = RegistryUtil.getStruct(wizard.getApplicationElement(), wizard.getHint());
				struct.setBundle(bundle);
				checkboxTableViewer.setInput(struct);

			}
		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkedElements = checkboxTableViewer.getCheckedElements();
				if (checkedElements.length > 0) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
	}

	@Override
	public void setWizard(IWizard newWizard) {
		this.wizard = (ModelImportWizard) newWizard;
		setTitle("Import " + wizard.getApplicationElement().getSimpleName());

		String hint = wizard.getHint();
		if (hint != null && hint.length() > 0) {
			setDescription("Select plug-in and elements (" + hint + ") to import");
			// else already set
		}

		super.setWizard(newWizard);
	}

	public IConfigurationElement[] getConfigurationElements() {
		ArrayList<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
		for (Object element : checkedElements) {
			if (element instanceof IConfigurationElement) {
				result.add((IConfigurationElement) element);
			}
		}
		return result.toArray(new IConfigurationElement[0]);
	}
}
