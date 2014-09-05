/*******************************************************************************
 * Copyright (c) 2008, 2014 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 *     Matthew Hall - bug 260329
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442278, 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 * 
 */
public class Snippet021MultiFieldValidation extends WizardPage {

	private List list_1;
	private List list;
	private Button addAddendButton;
	private Button removeAddendButton;
	private Text sumModelValue;
	private Text field2ModelValue;
	private Text field1ModelValue;
	private Text sumTarget;
	private Text field2Target;
	private Text field1Target;
	private ListViewer addendsTarget;
	private ListViewer addendsModelValue;

	/**
	 * Create the wizard
	 */
	public Snippet021MultiFieldValidation() {
		super("snippet021");
		setTitle("Snippet 021 - Multi-field Validators");
		setDescription("Enter values which satisfy the cross-field constraints");
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		//
		setControl(container);

		final Group bothEvenOrGroup = new Group(container, SWT.NONE);
		bothEvenOrGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false));
		bothEvenOrGroup.setText("Numbers must be both even or both odd");
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 3;
		bothEvenOrGroup.setLayout(gridLayout_1);
		new Label(bothEvenOrGroup, SWT.NONE);

		final Label targetLabel = new Label(bothEvenOrGroup, SWT.NONE);
		targetLabel.setText("Target");

		final Label modelLabel = new Label(bothEvenOrGroup, SWT.NONE);
		modelLabel.setText("Model");

		final Label field1Label = new Label(bothEvenOrGroup, SWT.NONE);
		field1Label.setText("Field 1");

		field1Target = new Text(bothEvenOrGroup, SWT.BORDER);
		final GridData gd_field1Target = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		field1Target.setLayoutData(gd_field1Target);

		field1ModelValue = new Text(bothEvenOrGroup, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_field1ModelValue = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		field1ModelValue.setLayoutData(gd_field1ModelValue);

		final Label field2Label = new Label(bothEvenOrGroup, SWT.NONE);
		field2Label.setText("Field 2");

		field2Target = new Text(bothEvenOrGroup, SWT.BORDER);
		final GridData gd_field2Target = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		field2Target.setLayoutData(gd_field2Target);

		field2ModelValue = new Text(bothEvenOrGroup, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_field2ModelValue = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		field2ModelValue.setLayoutData(gd_field2ModelValue);

		final Group sumOfAllGroup = new Group(container, SWT.NONE);
		sumOfAllGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				true));
		sumOfAllGroup.setText("Addends must add up to sum");
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 3;
		sumOfAllGroup.setLayout(gridLayout_2);
		new Label(sumOfAllGroup, SWT.NONE);

		final Label targetLabel_1 = new Label(sumOfAllGroup, SWT.NONE);
		targetLabel_1.setText("Target");

		final Label modelLabel_1 = new Label(sumOfAllGroup, SWT.NONE);
		modelLabel_1.setText("Model");

		final Label expectedSumLabel = new Label(sumOfAllGroup, SWT.NONE);
		expectedSumLabel.setText("Sum");

		sumTarget = new Text(sumOfAllGroup, SWT.BORDER);
		final GridData gd_sumTarget = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		sumTarget.setLayoutData(gd_sumTarget);

		sumModelValue = new Text(sumOfAllGroup, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_sumModelValue = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		sumModelValue.setLayoutData(gd_sumModelValue);

		final Label addendsLabel = new Label(sumOfAllGroup, SWT.NONE);
		addendsLabel.setText("Addends");

		addendsTarget = new ListViewer(sumOfAllGroup, SWT.V_SCROLL | SWT.BORDER);
		list_1 = addendsTarget.getList();
		list_1
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1,
						2));

		addendsModelValue = new ListViewer(sumOfAllGroup, SWT.V_SCROLL
				| SWT.BORDER);
		list = addendsModelValue.getList();
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 2));

		final Composite composite = new Composite(sumOfAllGroup, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.marginWidth = 0;
		gridLayout_3.marginHeight = 0;
		composite.setLayout(gridLayout_3);

		addAddendButton = new Button(composite, SWT.NONE);
		addAddendButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		addAddendButton.setText("Add");

		removeAddendButton = new Button(composite, SWT.NONE);
		removeAddendButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false));
		removeAddendButton.setText("Remove");

		bindUI();
	}

	private void bindUI() {
		DataBindingContext dbc = new DataBindingContext();

		bindEvensAndOddsGroup(dbc);
		bindSumAndAddendsGroup(dbc);

		WizardPageSupport.create(this, dbc);
	}

	private void bindEvensAndOddsGroup(DataBindingContext dbc) {
		IObservableValue targetField1 = WidgetProperties.text(SWT.Modify).observe(field1Target);
		final IObservableValue middleField1 = new WritableValue(null,
				Integer.TYPE);
		dbc.bindValue(targetField1, middleField1);

		IObservableValue targetField2 = WidgetProperties.text(SWT.Modify).observe(field2Target);
		final IObservableValue middleField2 = new WritableValue(null,
				Integer.TYPE);
		dbc.bindValue(targetField2, middleField2);

		MultiValidator validator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				Integer field1 = (Integer) middleField1.getValue();
				Integer field2 = (Integer) middleField2.getValue();
				if (Math.abs(field1.intValue()) % 2 != Math.abs(field2
						.intValue()) % 2)
					return ValidationStatus
							.error("Fields 1 and 2 must be both even or both odd");
				return null;
			}
		};
		dbc.addValidationStatusProvider(validator);

		IObservableValue modelField1 = new WritableValue(new Integer(1),
				Integer.TYPE);
		IObservableValue modelField2 = new WritableValue(new Integer(4),
				Integer.TYPE);
		dbc.bindValue(validator.observeValidatedValue(middleField1),
				modelField1);
		dbc.bindValue(validator.observeValidatedValue(middleField2),
				modelField2);

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(field1ModelValue),
				modelField1);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(field2ModelValue),
				modelField2);
	}

	private void bindSumAndAddendsGroup(DataBindingContext dbc) {
		IObservableValue targetSum = WidgetProperties.text(SWT.Modify).observe(sumTarget);
		final IObservableValue middleSum = new WritableValue(null, Integer.TYPE);
		dbc.bindValue(targetSum, middleSum);

		final IObservableList targetAddends = new WritableList(new ArrayList(),
				Integer.TYPE);
		addendsTarget.setContentProvider(new ObservableListContentProvider());
		addendsTarget.setInput(targetAddends);

		addAddendButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				InputDialog dialog = new InputDialog(getShell(),
						"Input addend", "Enter an integer addend", "0",
						new IInputValidator() {
							@Override
							public String isValid(String newText) {
								try {
									Integer.valueOf(newText);
									return null;
								} catch (NumberFormatException e) {
									return "Enter a number between "
											+ Integer.MIN_VALUE + " and "
											+ Integer.MAX_VALUE;
								}
							}
						});
				if (dialog.open() == Window.OK) {
					targetAddends.add(Integer.valueOf(dialog.getValue()));
				}
			}
		});

		removeAddendButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = addendsTarget.getStructuredSelection();
				if (!selection.isEmpty())
					targetAddends.remove(selection.getFirstElement());
			}
		});

		IObservableValue modelSum = new WritableValue(new Integer(5),
				Integer.TYPE);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(sumModelValue),
				modelSum);

		IObservableList modelAddends = new WritableList(new ArrayList(),
				Integer.TYPE);

		MultiValidator validator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				Integer sum = (Integer) middleSum.getValue();
				int actualSum = 0;
				for (Iterator iterator = targetAddends.iterator(); iterator
						.hasNext();) {
					actualSum += ((Integer) iterator.next()).intValue();
				}
				if (sum.intValue() != actualSum)
					return ValidationStatus.error("Sum of addends is "
							+ actualSum + ", expecting " + sum);
				return ValidationStatus.ok();
			}
		};
		dbc.addValidationStatusProvider(validator);

		addendsModelValue
				.setContentProvider(new ObservableListContentProvider());
		addendsModelValue.setInput(modelAddends);

		dbc.bindValue(validator.observeValidatedValue(middleSum), modelSum);
		dbc.bindList(validator.observeValidatedList(targetAddends),
				modelAddends);
	}

	static class MultiFieldValidationWizard extends Wizard {
		@Override
		public void addPages() {
			addPage(new Snippet021MultiFieldValidation());
		}

		@Override
		public String getWindowTitle() {
			return "Snippet 021 - Multi-field Validation";
		}

		@Override
		public boolean performFinish() {
			return true;
		}
	}

	public static void main(String[] args) {
		Display display = new Display();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				IWizard wizard = new MultiFieldValidationWizard();
				WizardDialog dialog = new WizardDialog(null, wizard);
				dialog.open();

				// The SWT event loop
				Display display = Display.getCurrent();
				while (dialog.getShell() != null
						&& !dialog.getShell().isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});

		display.dispose();
	}
}
