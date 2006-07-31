/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable.test;

import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.jface.examples.databinding.compositetable.binding.CompositeTableObservableLazyDataRequestor;
import org.eclipse.jface.examples.databinding.compositetable.binding.IRowBinder;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.DataBindingFactory1;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor.NewObject;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 *
 */
public class CompositeTableTestBinding {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private CompositeTable table = null;
	private Header header = null;
	private Row row = null;
	
	private List personList = new WritableList();
	
	/**
	 * 
	 */
	public CompositeTableTestBinding() {
		personList.add(new Person("John", "1234", "Wheaton", "IL"));
		personList.add(new Person("Jane", "1234", "Wheaton", "IL"));
		personList.add(new Person("Frank", "1234", "Wheaton", "IL"));
		personList.add(new Person("Joe", "1234", "Wheaton", "IL"));
		personList.add(new Person("Chet", "1234", "Wheaton", "IL"));
		personList.add(new Person("Jane", "1234", "Wheaton", "IL"));
		personList.add(new Person("Frank", "1234", "Wheaton", "IL"));
		personList.add(new Person("Joe", "1234", "Wheaton", "IL"));
		personList.add(new Person("Chet", "1234", "Wheaton", "IL"));
		personList.add(new Person("Jane", "1234", "Wheaton", "IL"));
		personList.add(new Person("Frank", "1234", "Wheaton", "IL"));
		personList.add(new Person("Joe", "1234", "Wheaton", "IL"));
		personList.add(new Person("Chet", "1234", "Wheaton", "IL"));
		personList.add(new Person("Jane", "1234", "Wheaton", "IL"));
		personList.add(new Person("Frank", "1234", "Wheaton", "IL"));
		personList.add(new Person("Joe", "1234", "Wheaton", "IL"));
		personList.add(new Person("Chet", "1234", "Wheaton", "IL"));
		personList.add(new Person("Wilbur", "1234", "Wheaton", "IL"));
		personList.add(new Person("Elmo", "1234", "Wheaton", "IL"));
	}

	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Shell");
		sShell.setLayout(new FillLayout());
		sShell.setSize(new Point(445,243));
		table = new CompositeTable(sShell, SWT.NONE);
		table.setRunTime(true);
		table.setWeights(new int[] {35, 35, 20, 10});
		table.setTraverseOnTabsEnabled(true);
		header = new Header(table, SWT.NONE);
		row = new Row(table, SWT.NONE);
		bindGUI(sShell);
	}

	private void bindGUI(Shell shell) {
		DataBindingContext bindingContext = new DataBindingFactory1().createContext(shell);
		
		LazyInsertDeleteProvider insertDeleteProvider = new LazyInsertDeleteProvider() {
			public boolean canDeleteElementAt(LazyDeleteEvent e) {
				return true;
			}
			
			public void deleteElementAt(LazyDeleteEvent e) {
				personList.remove(e.position);
			}
			
			public NewObject insertElementAt(LazyInsertEvent e) {
				Person newPerson = new Person();
				personList.add(e.positionHint, newPerson);
				return new NewObject(e.positionHint, newPerson);
//				int newPosition = (int)(Math.random() * (personList.size()+1));
//				personList.add(newPosition, newPerson);
//				return newPosition;
			}
		};
		
		IRowBinder rowBinder = new IRowBinder() {
			public void bindRow(DataBindingContext context, Control row, Object object) {
				Row rowObj = (Row) row;
				context.bind(rowObj.name, new Property(object, "name"), null);
				context.bind(rowObj.address, new Property(object, "address"), null);
				context.bind(rowObj.city, new Property(object, "city"), null);
				context.bind(rowObj.state, new Property(object, "state"), null);
			}
		};
		CompositeTableObservableLazyDataRequestor tableObservable = 
			new CompositeTableObservableLazyDataRequestor(bindingContext, table, rowBinder);
		
		bindingContext.bind(tableObservable, (IObservable)personList, 
				new BindSpec().setLazyInsertDeleteProvider(insertDeleteProvider));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		CompositeTableTestBinding thisClass = new CompositeTableTestBinding();
		thisClass.createSShell();
		thisClass.sShell.open();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
