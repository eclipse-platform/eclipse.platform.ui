/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 260329
 *******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.util.Date;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Creates and opens a wizard dialog with two simple wizard pages.
 */
public class Snippet014WizardDialog {

	static class FirstWizardPage extends WizardPage {
		private final class SingleDigitValidator implements IValidator {
			@Override
			public IStatus validate(Object value) {
				Integer i = (Integer) value;
				if (i == null) {
					return ValidationStatus
							.info("Please enter a value.");
				}
				if (i.intValue() < 0 || i.intValue() > 9) {
					return ValidationStatus
							.error("Value must be between 0 and 9.");
				}
				return ValidationStatus.ok();
			}
		}

		protected FirstWizardPage() {
			super("First", "First Page", ImageDescriptor
					.createFromImage(new Image(Display.getDefault(), 16, 16)));
		}

		@Override
		public void createControl(Composite parent) {
			DataBindingContext dbc = new DataBindingContext();
			WizardPageSupport.create(this, dbc);
			Composite composite = new Composite(parent, SWT.NONE);
			Label label = new Label(composite, SWT.NONE);
			label.setText("Enter a number between 0 and 9:");
			Text text = new Text(composite, SWT.BORDER);
			
			dbc.bindValue(
							SWTObservables.observeText(text, SWT.Modify),
							((SampleWizard) getWizard()).getModel().intValue,
							new UpdateValueStrategy().setAfterConvertValidator(new SingleDigitValidator()),
							null);
			
			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(
					composite);
			setControl(composite);
		}
	}

	static class SecondWizardPage extends WizardPage {
		protected SecondWizardPage() {
			super("Second", "Second Page", ImageDescriptor
					.createFromImage(new Image(Display.getDefault(), 16, 16)));
		}

		@Override
		public void createControl(Composite parent) {
			DataBindingContext dbc = new DataBindingContext();
			WizardPageSupport.create(this, dbc);
			Composite composite = new Composite(parent, SWT.NONE);
			Label label = new Label(composite, SWT.NONE);
			label.setText("Enter a date:");
			Text text = new Text(composite, SWT.BORDER);
			
			dbc.bindValue(
							SWTObservables.observeText(text, SWT.Modify),
							((SampleWizard) getWizard()).getModel().dateValue);

			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(
					composite);
			setControl(composite);
		}
	}

	static class SampleWizardModel {
		IObservableValue intValue = new WritableValue(null, Integer.class);
		IObservableValue dateValue = new WritableValue(null, Date.class);
	}

	static class SampleWizard extends Wizard {

		private SampleWizardModel model = new SampleWizardModel();

		@Override
		public void addPages() {
			addPage(new FirstWizardPage());
			addPage(new SecondWizardPage());
		}

		public SampleWizardModel getModel() {
			return model;
		}
		
		@Override
		public String getWindowTitle() {
			return "Data Binding Snippet014";
		}

		@Override
		public boolean performFinish() {
			return true;
		}

	}

	public static void main(String[] args) {
		Display display = new Display();

		// note that the "runWithDefault" will be done for you if you are using
		// the
		// Workbench as opposed to just JFace/SWT.
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				IWizard wizard = new SampleWizard();
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
	}

}
