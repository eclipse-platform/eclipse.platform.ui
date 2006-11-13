/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.nestedselection;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.examples.databinding.model.SimpleModel;
import org.eclipse.jface.examples.databinding.model.SimpleOrder;
import org.eclipse.jface.examples.databinding.model.SimplePerson;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.0
 * 
 */
public class TestMasterDetail {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestMasterDetail().run();
	}

	private Shell shell = null; // @jve:decl-index=0:visual-constraint="10,10"

	private Table personsTable = null;

	private Label label1 = null;

	private Text name = null;

	private Label label2 = null;

	private Text address = null;

	private Label label3 = null;

	private Text city = null;

	private Label label4 = null;

	private Text state = null;

	private Table ordersTable = null;

	/**
	 * This method initializes table
	 * 
	 */
	private void createTable() {
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		personsTable = new Table(shell, SWT.FULL_SELECTION);
		personsTable.setHeaderVisible(true);
		personsTable.setLayoutData(gridData);
		personsTable.setLinesVisible(true);
		TableColumn tableColumn = new TableColumn(personsTable, SWT.NONE);
		tableColumn.setWidth(60);
		tableColumn.setText("Name");
		TableColumn tableColumn1 = new TableColumn(personsTable, SWT.NONE);
		tableColumn1.setWidth(60);
		tableColumn1.setText("State");
	}

	/**
	 * This method initializes table1
	 * 
	 */
	private void createTable1() {
		GridData gridData5 = new org.eclipse.swt.layout.GridData();
		gridData5.horizontalSpan = 2;
		gridData5.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.grabExcessVerticalSpace = true;
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		ordersTable = new Table(shell, SWT.FULL_SELECTION);
		ordersTable.setHeaderVisible(true);
		ordersTable.setLayoutData(gridData5);
		ordersTable.setLinesVisible(true);
		TableColumn tableColumn2 = new TableColumn(ordersTable, SWT.NONE);
		tableColumn2.setWidth(60);
		tableColumn2.setText("Order No");
		TableColumn tableColumn3 = new TableColumn(ordersTable, SWT.NONE);
		tableColumn3.setWidth(60);
		tableColumn3.setText("Date");
	}

	/**
	 * This method initializes sShell
	 */
	private void createShell() {
		GridData gridData5 = new org.eclipse.swt.layout.GridData();
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData5.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData4 = new org.eclipse.swt.layout.GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData4.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData3 = new org.eclipse.swt.layout.GridData();
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData2 = new org.eclipse.swt.layout.GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData1 = new org.eclipse.swt.layout.GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		shell = new Shell();
		shell.setText("Shell");
		createTable();
		shell.setLayout(gridLayout);
		shell.setSize(new org.eclipse.swt.graphics.Point(495, 357));
		label1 = new Label(shell, SWT.NONE);
		label1.setText("Name");
		name = new Text(shell, SWT.BORDER);
		name.setLayoutData(gridData1);
		label2 = new Label(shell, SWT.NONE);
		label2.setText("Address");
		address = new Text(shell, SWT.BORDER);
		address.setLayoutData(gridData2);
		label3 = new Label(shell, SWT.NONE);
		label3.setText("City");
		city = new Text(shell, SWT.BORDER);
		city.setLayoutData(gridData4);
		label4 = new Label(shell, SWT.NONE);
		label4.setText("State");
		state = new Text(shell, SWT.BORDER);
		state.setLayoutData(gridData3);
		createTable1();
	}

	private void run() {
		Display display = new Display();

		createShell();
		bind(shell);

		shell.setSize(600, 600);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	SimpleModel model = new SimpleModel();

	private void bind(Control parent) {
		Realm realm = SWTObservables.getRealm(parent.getDisplay());

		TableViewer peopleViewer = new TableViewer(personsTable);
		ObservableListContentProvider peopleViewerContent = new ObservableListContentProvider();
		peopleViewer.setContentProvider(peopleViewerContent);
		IObservableMap[] attributeMaps = BeansObservables.observeMaps(
				peopleViewerContent.getKnownElements(), SimplePerson.class,
				new String[] { "name", "state" });
		peopleViewer.setLabelProvider(new ObservableMapLabelProvider(
				attributeMaps));
		peopleViewer.setInput(new WritableList(realm, model.getPersonList(),
				SimpleModel.class));

		IObservableValue selectedPerson = ViewersObservables
				.observeSingleSelection(peopleViewer);

		DataBindingContext dbc = new DataBindingContext(realm);
		dbc.bindValue(SWTObservables.observeText(name, SWT.Modify),
				BeansObservables.observeDetailValue(realm, selectedPerson,
						"name", String.class), null);

		dbc.bindValue(SWTObservables.observeText(address, SWT.Modify),
				BeansObservables.observeDetailValue(realm, selectedPerson,
						"address", String.class), null);

		dbc.bindValue(SWTObservables.observeText(city, SWT.Modify),
				BeansObservables.observeDetailValue(realm, selectedPerson,
						"city", String.class), null);

		dbc.bindValue(SWTObservables.observeText(state, SWT.Modify),
				BeansObservables.observeDetailValue(realm, selectedPerson,
						"state", String.class), null);

		TableViewer ordersViewer = new TableViewer(ordersTable);
		ObservableListContentProvider ordersViewerContent = new ObservableListContentProvider();
		ordersViewer.setContentProvider(ordersViewerContent);
		ordersViewer.setLabelProvider(new ObservableMapLabelProvider(
				BeansObservables.observeMaps(ordersViewerContent
						.getKnownElements(), SimpleOrder.class, new String[] {
						"orderNumber", "date" })));

		IObservableList orders = BeansObservables.observeDetailList(realm,
				selectedPerson, "orders", SimpleOrder.class);
		ordersViewer.setInput(orders);
	}

}
