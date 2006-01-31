/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.dialogs.HockeyleagueSetDefaultsDialog;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.editor.HockeyleagueEditor;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * An abstract implementation of a section with a table field with add and
 * remove buttons.
 * 
 * @author Anthony Hunter
 */
public abstract class AbstractTablePropertySection
	extends AbstractHockeyleaguePropertySection {

	/**
	 * the table control for the section.
	 */
	protected Table table;

	/**
	 * the title columns for the section.
	 */
	protected List columns;

	/**
	 * the add button for the section.
	 */
	protected Button addButton;

	/**
	 * the remove button for the section.
	 */
	protected Button removeButton;

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
			.createFlatFormComposite(parent);
		FormData data;

		table = getWidgetFactory().createTable(composite,
			SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		List labels = getColumnLabelText();
		columns = new ArrayList();

		for (Iterator i = labels.iterator(); i.hasNext();) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText((String) i.next());
			columns.add(column);
		}

		Shell shell = new Shell();
		GC gc = new GC(shell);
		gc.setFont(shell.getFont());
		Point point = gc.textExtent("");//$NON-NLS-1$
		int buttonHeight = point.y + 11;
		gc.dispose();
		shell.dispose();

		addButton = getWidgetFactory().createButton(composite,
			MessageFormat.format("Add {0}...",//$NON-NLS-1$
				new String[] {getButtonLabelText()}), SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		data.top = new FormAttachment(100, -buttonHeight);
		addButton.setLayoutData(data);
		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				EditingDomain editingDomain = ((HockeyleagueEditor) getPart())
					.getEditingDomain();
				AddCommand addCommand = (AddCommand) AddCommand.create(
					editingDomain, eObject, getFeature(), getNewChild());
				HockeyleagueSetDefaultsDialog dialog = new HockeyleagueSetDefaultsDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell(), addCommand);
				dialog.open();
				if (dialog.getReturnCode() == Window.CANCEL) {
					return;
				}
				editingDomain.getCommandStack().execute(addCommand);
			}
		});

		removeButton = getWidgetFactory().createButton(composite,
			MessageFormat.format("Delete {0}",//$NON-NLS-1$
				new String[] {getButtonLabelText()}), SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(addButton,
			ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
		data.bottom = new FormAttachment(100, 0);
		data.top = new FormAttachment(100, -buttonHeight);
		removeButton.setLayoutData(data);
		removeButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				EditingDomain editingDomain = ((HockeyleagueEditor) getPart())
					.getEditingDomain();
				Object object = table.getSelection()[0].getData();
				editingDomain.getCommandStack().execute(
					RemoveCommand.create(editingDomain, object));
			}
		});

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.bottom = new FormAttachment(addButton,
			-ITabbedPropertyConstants.VSPACE);
		data.width = 400;
		table.setLayoutData(data);

		table.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				removeButton.setEnabled(true);
			}
		});
		table.addMouseListener(new MouseAdapter() {

			public void mouseDoubleClick(MouseEvent e) {
				Object object = table.getSelection()[0].getData();
				propertySheetPage.getEditor().getViewer().setSelection(
					new StructuredSelection(object), true);
				propertySheetPage.getEditor().setFocus();
			}
		});
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
	 */
	public boolean shouldUseExtraSpace() {
		return true;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
	 */
	public void refresh() {
		table.removeAll();
		removeButton.setEnabled(false);

		for (Iterator i = getOwnedRows().iterator(); i.hasNext();) {
			Object next = i.next();
			String key = getKeyForRow(next);

			// find index (for sorting purposes)
			int k = 0;
			int size = table.getItemCount();
			while (k < size) {
				String currentKey = table.getItem(k).getText();
				if (key.compareToIgnoreCase(currentKey) < 0) {
					break;
				}
				k++;
			}

			// create the table item
			TableItem item = new TableItem(table, SWT.NONE, k);
			String[] values = new String[columns.size()];
			List valuesForRow = getValuesForRow(next);
			for (int j = 0; j < columns.size(); j++) {
				values[j] = (String) valuesForRow.get(j);
			}
			item.setText(values);
			item.setData(next);
		}

		for (Iterator i = columns.iterator(); i.hasNext();) {
			((TableColumn) i.next()).pack();
		}
	}

	/**
	 * Get the text for the labels that will be used for the Add and Remove
	 * buttons.
	 * 
	 * @return the label text.
	 */
	protected abstract String getButtonLabelText();

	/**
	 * Get the row objects for the table.
	 * 
	 * @return the list of the row objects.
	 */
	protected abstract List getOwnedRows();

	/**
	 * Get the feature for the table field for the section.
	 * 
	 * @return the feature for the table.
	 */
	protected abstract EReference getFeature();

	/**
	 * Get the key for the table that is used for sorting. Usually the table is
	 * sorted by Name or some key string..
	 * 
	 * @param object
	 *            an object in the row of the table.
	 * @return the string for the key.
	 */
	protected abstract String getKeyForRow(Object object);

	/**
	 * Get the values for the row in the table.
	 * 
	 * @param object
	 *            an object in the row of the table.
	 * @return the list of string values for the row.
	 */
	protected abstract List getValuesForRow(Object object);

	/**
	 * Get the labels for the columns for the table.
	 * 
	 * @return the labels for the columns.
	 */
	protected abstract List getColumnLabelText();

	/**
	 * Get a new child instance for the result of clicking the add button.
	 * 
	 * @return a new child instance.
	 */
	protected abstract Object getNewChild();

}