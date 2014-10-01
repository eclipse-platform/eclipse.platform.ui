/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.Iterator;
import java.util.Random;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.MappedSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewerLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests UpdatableSetContentProvider, ComputableValue, ControlUpdator,
 * UpdatableFunction, and ConvertingSet.
 *
 * <p>
 * This test displays a dialog with user-editable list of Doubles. It allows the
 * user to select a math function to apply to the set, and displays the result
 * in a new list. A line of text along the bottom of the dialog displays the sum
 * of the elements from the transformed set. Although this dialog is rather
 * silly, it is a good example of a dialog where a lot of things can change from
 * many directions.
 * </p>
 *
 * <p>
 * An UpdatableSetContentProvider is used to supply the contents each
 * ListViewer. ControlUpdators
 *
 * </p>
 *
 * @since 1.0
 */
public class StructuredContentProviderTest {

	private static Realm realm;

	/**
	 * Top-level shell for the dialog
	 */
	private Shell shell;

	/**
	 * Random number stream. Used for the "add" button.
	 */
	protected Random random = new Random();

	// Data model ////////////////////////////////////////////////////////

	/**
	 * inputSet stores a set of Doubles. The user is allowed to add and remove
	 * Doubles from this set.
	 */
	private WritableSet inputSet;

	/**
	 * currentFunction is an Integer, set to one of the SomeMathFunction.OP_*
	 * constants. It identifies which function will be applied to inputSet.
	 */
	private WritableValue currentFunction;

	/**
	 * mathFunction is the transformation. It can multiply by 2, round down to
	 * the nearest integer, or do nothing (identity)
	 */
	private SomeMathFunction mathFunction;

	/**
	 * Set of Doubles. Holds the result of applying mathFunction to the
	 * inputSet.
	 */
	private MappedSet outputSet;

	/**
	 * A Double. Stores the sum of the Doubles in outputSet
	 */
	private IObservableValue sumOfOutputSet;

	/**
	 * Creates the test dialog as a top-level shell.
	 */
	public StructuredContentProviderTest() {

		// Initialize the data model
		createDataModel();

		shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
		{ // Initialize shell
			final Label someDoubles = new Label(shell, SWT.NONE);
			someDoubles.setText("A list of random Doubles"); //$NON-NLS-1$
			someDoubles.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_FILL
							| GridData.VERTICAL_ALIGN_FILL));

			Control addRemoveComposite = createInputControl(shell, inputSet);

			GridData addRemoveData = new GridData(GridData.FILL_BOTH);
			addRemoveData.minimumHeight = 1;
			addRemoveData.minimumWidth = 1;

			addRemoveComposite.setLayoutData(addRemoveData);

			Group operation = new Group(shell, SWT.NONE);
			{ // Initialize operation group
				operation.setText("Select transformation"); //$NON-NLS-1$

				createRadioButton(operation, currentFunction, "f(x) = x", //$NON-NLS-1$
						new Integer(SomeMathFunction.OP_IDENTITY));
				createRadioButton(operation, currentFunction, "f(x) = 2 * x", //$NON-NLS-1$
						new Integer(SomeMathFunction.OP_MULTIPLY));
				createRadioButton(operation, currentFunction,
						"f(x) = floor(x)", new Integer( //$NON-NLS-1$
								SomeMathFunction.OP_ROUND));

				GridLayout layout = new GridLayout();
				layout.numColumns = 1;
				operation.setLayout(layout);
			}
			operation.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_FILL));

			Control outputControl = createOutputComposite(shell);
			GridData outputData = new GridData(GridData.FILL_BOTH);
			outputData.minimumHeight = 1;
			outputData.minimumWidth = 1;
			outputData.widthHint = 300;
			outputData.heightHint = 150;

			outputControl.setLayoutData(outputData);

			final Label sumLabel = new Label(shell, SWT.NONE);
			new ControlUpdater(sumLabel) {
				@Override
				protected void updateControl() {
					double sum = ((Double) sumOfOutputSet.getValue())
							.doubleValue();
					int size = outputSet.size();

					sumLabel.setText("The sum of the above " + size //$NON-NLS-1$
							+ " doubles is " + sum); //$NON-NLS-1$
				}
			};
			sumLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_FILL));

			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			shell.setLayout(layout);
		}

	}

	/**
	 * Create the updatables for this dialog
	 */
	private void createDataModel() {
		// Initialize data model. We will create a user-editable set of Doubles.
		// The user can run
		// a transformation on this set and view the result in a list viewer.

		// inputSet will be a writable set of doubles. The user will add and
		// remove entries from this set
		// through the UI.
		inputSet = new WritableSet(realm);

		// currentFunction holds the ID currently selected function to apply to
		// elements in the inputSet.
		// We will allow the user to change the current function through a set
		// of radio buttons
		currentFunction = new WritableValue(realm, new Integer(
				SomeMathFunction.OP_MULTIPLY), null);

		// mathFunction implements the selected function
		mathFunction = new SomeMathFunction(inputSet);
		currentFunction.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				mathFunction
						.setOperation(((Integer) currentFunction.getValue())
								.intValue());
			}
		});
		mathFunction.setOperation(((Integer) currentFunction.getValue())
				.intValue());

		// outputSet holds the result. It displays the result of applying the
		// currently-selected
		// function on all the elements in the input set.
		outputSet = new MappedSet(inputSet, mathFunction);

		// sumOfOutputSet stores the current sum of the the Doubles in the
		// output set
		sumOfOutputSet = new ComputedValue(realm) {
			@Override
			protected Object calculate() {
				double sum = 0.0;
				for (Iterator iter = outputSet.iterator(); iter.hasNext();) {
					Double next = (Double) iter.next();

					sum += next.doubleValue();
				}
				return new Double(sum);
			}
		};
	}

	/**
	 * Creates a radio button in the given parent composite. When selected, the
	 * button will change the given SettableValue to the given value.
	 *
	 * @param parent
	 *            parent composite
	 * @param model
	 *            SettableValue that will hold the value of the
	 *            currently-selected radio button
	 * @param string
	 *            text to appear in the radio button
	 * @param value
	 *            value of this radio button (SettableValue will hold this value
	 *            when the radio button is selected)
	 */
	private void createRadioButton(Composite parent, final WritableValue model,
			String string, final Object value) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setText(string);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.setValue(value);
				super.widgetSelected(e);
			}
		});
		new ControlUpdater(button) {
			@Override
			protected void updateControl() {
				button.setSelection(model.getValue().equals(value));
			}
		};
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL));
	}

	private Control createOutputComposite(Composite parent) {
		ListViewer listOfInts = new ListViewer(parent, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);

		listOfInts.setContentProvider(new ObservableSetContentProvider());
		listOfInts.setLabelProvider(new ViewerLabelProvider());
		listOfInts.setInput(outputSet);
		return listOfInts.getControl();
	}

	/**
	 * Creates and returns a control that will allow the user to add and remove
	 * Doubles from the given input set.
	 *
	 * @param parent
	 *            parent control
	 * @param inputSet
	 *            input set
	 * @return a newly created SWT control that displays Doubles from the input
	 *         set and allows the user to add and remove entries
	 */
	private Control createInputControl(Composite parent,
			final WritableSet inputSet) {
		Composite addRemoveComposite = new Composite(parent, SWT.NONE);
		{ // Initialize addRemoveComposite
			ListViewer listOfInts = new ListViewer(addRemoveComposite,
					SWT.BORDER);

			listOfInts.setContentProvider(new ObservableSetContentProvider());
			listOfInts.setLabelProvider(new ViewerLabelProvider());
			listOfInts.setInput(inputSet);

			final IObservableValue selectedInt = ViewersObservables.observeSingleSelection(listOfInts);

			GridData listData = new GridData(GridData.FILL_BOTH);
			listData.minimumHeight = 1;
			listData.minimumWidth = 1;
			listData.widthHint = 150;
			listData.heightHint = 150;
			listOfInts.getControl().setLayoutData(listData);

			Composite buttonBar = new Composite(addRemoveComposite, SWT.NONE);
			{ // Initialize button bar

				Button add = new Button(buttonBar, SWT.PUSH);
				add.setText("Add"); //$NON-NLS-1$
				add.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						inputSet.add(new Double(random.nextDouble() * 100.0));
						super.widgetSelected(e);
					}
				});
				add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_FILL));

				final Button remove = new Button(buttonBar, SWT.PUSH);
				remove.setText("Remove"); //$NON-NLS-1$
				// Enable the Remove button if and only if there is currently an
				// element selected.
				new ControlUpdater(remove) {
					@Override
					protected void updateControl() {
						// This block demonstrates auto-listening.
						// When updateControl is running, the framework
						// remembers each
						// updatable that gets touched. Since we're calling
						// selectedInt.getValue()
						// here, this updator will be flagged as dependant on
						// selectedInt. This
						// means that whenever selectedInt changes, this block
						// of code will re-run
						// itself.

						// The result is that the remove button will recompute
						// its enablement
						// whenever the selection in the listbox changes, and it
						// was not necessary
						// to attach any listeners.
						remove.setEnabled(selectedInt.getValue() != null);
					}
				};

				remove.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						inputSet.remove(selectedInt.getValue());
						super.widgetSelected(e);
					}
				});
				remove.setLayoutData(new GridData(
						GridData.HORIZONTAL_ALIGN_FILL
								| GridData.VERTICAL_ALIGN_FILL));

				GridLayout buttonLayout = new GridLayout();
				buttonLayout.numColumns = 1;
				buttonBar.setLayout(buttonLayout);

			} // End button bar
			buttonBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING));

			GridLayout addRemoveLayout = new GridLayout();
			addRemoveLayout.numColumns = 2;
			addRemoveComposite.setLayout(addRemoveLayout);
		}
		return addRemoveComposite;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		realm = SWTObservables.getRealm(display);
		StructuredContentProviderTest test = new StructuredContentProviderTest();
		Shell s = test.getShell();
		s.pack();
		s.setVisible(true);

		while (!s.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private Shell getShell() {
		return shell;
	}

}
