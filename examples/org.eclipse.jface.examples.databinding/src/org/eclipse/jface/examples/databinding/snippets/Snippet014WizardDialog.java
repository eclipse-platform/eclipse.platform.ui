/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 260329
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
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
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
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

	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			IWizard wizard = new SampleWizard();
			WizardDialog dialog = new WizardDialog(null, wizard);
			dialog.open();
		});
	}

	static class FirstWizardPage extends WizardPage {
		private static final class SingleDigitValidator implements IValidator<Integer> {
			@Override
			public IStatus validate(Integer value) {
				if (value == null) {
					return ValidationStatus.info("Please enter a value.");
				}
				if (value < 0 || value > 9) {
					return ValidationStatus.error("Value must be between 0 and 9.");
				}
				return ValidationStatus.ok();
			}
		}

		protected FirstWizardPage() {
			super("First", "First Page", ImageDescriptor.createFromImage(new Image(Display.getDefault(), 16, 16)));
		}

		@Override
		public void createControl(Composite parent) {
			DataBindingContext bindingContext = new DataBindingContext();
			WizardPageSupport.create(this, bindingContext);
			Composite composite = new Composite(parent, SWT.NONE);
			Label label = new Label(composite, SWT.NONE);
			label.setText("Enter a number between 0 and 9:");
			Text text = new Text(composite, SWT.BORDER);

			bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(text),
					((SampleWizard) getWizard()).getModel().intValue,
					new UpdateValueStrategy<String, Integer>().setAfterConvertValidator(new SingleDigitValidator()),
					null);

			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);
			setControl(composite);
		}
	}

	static class SecondWizardPage extends WizardPage {
		protected SecondWizardPage() {
			super("Second", "Second Page", ImageDescriptor.createFromImage(new Image(Display.getDefault(), 16, 16)));
		}

		@Override
		public void createControl(Composite parent) {
			DataBindingContext bindingContext = new DataBindingContext();
			WizardPageSupport.create(this, bindingContext);
			Composite composite = new Composite(parent, SWT.NONE);
			Label label = new Label(composite, SWT.NONE);
			label.setText("Enter a date:");
			Text text = new Text(composite, SWT.BORDER);

			bindingContext.bindValue(WidgetProperties.text().observe(text),
					((SampleWizard) getWizard()).getModel().dateValue);

			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);
			setControl(composite);
		}
	}

	static class SampleWizardModel {
		IObservableValue<Integer> intValue = new WritableValue<>(null, Integer.class);
		IObservableValue<Integer> dateValue = new WritableValue<>(null, Date.class);
	}

	static class SampleWizard extends Wizard {

		private final SampleWizardModel model = new SampleWizardModel();

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
}
