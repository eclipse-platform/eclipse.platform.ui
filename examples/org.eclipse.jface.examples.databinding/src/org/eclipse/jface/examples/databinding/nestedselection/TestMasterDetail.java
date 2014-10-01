/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bugs 260329, 260337
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 *******************************************************************************/

package org.eclipse.jface.examples.databinding.nestedselection;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.conversion.ObjectToStringConverter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
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
	 * @since 3.2
	 * 
	 */
	private static final class CustomUpdateValueStrategy extends
			UpdateValueStrategy {
		@Override
		protected IStatus doSet(IObservableValue observableValue, Object value) {
			IStatus result = super.doSet(observableValue, value);
			if (result.isOK()) {
				Object changed = observableValue;
				if (changed instanceof IObserving) {
					changed = ((IObserving) changed).getObserved();
				}
				System.out.println("changed: " + changed);
			}
			return result;
		}
	}

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

	private Text validationStatus;

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
		validationStatus = new Text(shell, SWT.READ_ONLY | SWT.BORDER);
	}

	private void run() {
		final Display display = new Display();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				createShell();
				bind(shell);

				shell.setSize(600, 600);
				shell.open();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
		display.dispose();
	}

	SimpleModel model = new SimpleModel();

	private void bind(Control parent) {
		Realm realm = SWTObservables.getRealm(parent.getDisplay());

		TableViewer peopleViewer = new TableViewer(personsTable);
		ViewerSupport.bind(peopleViewer, new WritableList(realm, model
				.getPersonList(), SimpleModel.class), BeanProperties.values(
				SimplePerson.class, new String[] { "name", "state" }));

		IObservableValue selectedPerson = ViewersObservables
				.observeSingleSelection(peopleViewer);

		DataBindingContext dbc = new DataBindingContext(realm) {
			@Override
			protected UpdateValueStrategy createTargetToModelUpdateValueStrategy(
					IObservableValue fromValue, IObservableValue toValue) {
				return new CustomUpdateValueStrategy();
			}
		};
		IConverter upperCaseConverter = new IConverter() {
			@Override
			public Object convert(Object fromObject) {
				return ((String) fromObject).toUpperCase();
			}

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object getToType() {
				return String.class;
			}
		};
		IValidator vowelValidator = new IValidator() {
			@Override
			public IStatus validate(Object value) {
				String s = (String) value;
				if (!s.matches("[aeiouAEIOU]*")) {
					return ValidationStatus.error("only vowels allowed");
				}
				return Status.OK_STATUS;
			}
		};
		Binding b = dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(name),
				BeanProperties.value((Class) selectedPerson.getValueType(), "name", String.class).observeDetail(
								selectedPerson), new CustomUpdateValueStrategy()
						.setConverter(upperCaseConverter).setAfterGetValidator(
								vowelValidator), null);

		// AggregateValidationStatus status = new AggregateValidationStatus(dbc
		// .getBindings(), AggregateValidationStatus.MAX_SEVERITY);
		dbc.bindValue(WidgetProperties.text().observe(validationStatus), b
				.getValidationStatus(), null, new UpdateValueStrategy()
				.setConverter(new ObjectToStringConverter()));

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(address),
				BeanProperties.value((Class) selectedPerson.getValueType(), "address", String.class).observeDetail(
						selectedPerson));

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(city),
				BeanProperties.value((Class) selectedPerson.getValueType(), "city", String.class).observeDetail(
						selectedPerson));

		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(state),
				BeanProperties.value((Class) selectedPerson.getValueType(), "state", String.class).observeDetail(
				selectedPerson));

		TableViewer ordersViewer = new TableViewer(ordersTable);
		ViewerSupport.bind(ordersViewer, BeanProperties
				.list((Class) selectedPerson.getValueType(),
						"orders", SimpleOrder.class).observeDetail(selectedPerson),
				BeanProperties
				.values(SimpleOrder.class,
						new String[] { "orderNumber", "date" }));
	}
}
