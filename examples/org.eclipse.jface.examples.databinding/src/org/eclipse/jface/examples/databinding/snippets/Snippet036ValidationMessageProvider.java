/*******************************************************************************
 * Copyright (c) 2009, 2018 Ovidio Mallo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 248877)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.IValidationMessageProvider;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationUpdater;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
 * Illustrates the use of the {@link IValidationMessageProvider} API.
 */
public class Snippet036ValidationMessageProvider {

	public static void main(String[] args) {
		Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			IWizard wizard = new MessageProviderWizard();
			WizardDialog wizardDialog = new WizardDialog(null, wizard);
			wizardDialog.open();

			Display display1 = Display.getCurrent();
			while (wizardDialog.getShell() != null && !wizardDialog.getShell().isDisposed()) {
				if (!display1.readAndDispatch()) {
					display1.sleep();
				}
			}
		});
	}

	private static final class MessageProviderWizard extends Wizard {

		@Override
		public void addPages() {
			addPage(new MessageProviderWizardPage());
		}

		@Override
		public String getWindowTitle() {
			return "Snippet 036 - IValidationMessageProvider";
		}

		@Override
		public boolean performFinish() {
			return true;
		}
	}

	private static final class MessageProviderWizardPage extends WizardPage {

		private DataBindingContext dbc;
		private Map<Binding, String> bindingMapName;

		protected MessageProviderWizardPage() {
			super(MessageProviderWizardPage.class.getName());
			setTitle("Snippet 036 - IValidationMessageProvider");
			setDescription("Please fill in the form.");
		}

		@Override
		public void createControl(Composite parent) {
			dbc = new DataBindingContext();
			bindingMapName = new HashMap<>();

			// Create the container composite.
			Composite container = new Composite(parent, SWT.NULL);
			GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5)
					.spacing(15, 5).applyTo(container);
			setControl(container);

			// Create the input fields.
			createTextLine(container, "Name", WritableValue
					.withValueType(String.class));
			createTextLine(container, "Age", WritableValue
					.withValueType(Integer.class));
			createTextLine(container, "Birthday", WritableValue
					.withValueType(Date.class));

			// Attach the DBC's validation to the wizard.
			WizardPageSupport wps = WizardPageSupport.create(this, dbc);

			// Use our CustomMessageProvider.
			wps.setValidationMessageProvider(new CustomMessageProvider(
					bindingMapName));
		}

		private <T> void createTextLine(Composite parent, String labelText, IObservableValue<T> modelValue) {
			// Create the Label.
			Label label = new Label(parent, SWT.LEFT);
			label.setText(labelText);
			GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);

			// Create the Text.
			final Text text = new Text(parent, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(text);

			// Create the Text observable.
			IObservableValue<String> textObservable = WidgetProperties.text(SWT.Modify).observe(text);

			// Bind the Text to the model and attach a RequiredValidator.
			Binding binding = dbc.bindValue(textObservable, modelValue,
					new UpdateValueStrategy<String, T>().setAfterConvertValidator(new RequiredValidator()),
					new UpdateValueStrategy<>());

			// Custom control decoration for "required" validation.
			ControlDecorationUpdater decorationUpdater = new ControlDecorationUpdater() {

				@Override
				protected Image getImage(IStatus status) {
					// For required validations, we do not want to display an
					// error icon since the user has not done anything wrong.
					if (text.getText().length() == 0) {
						// Display a "required" decoration (asterisk).
						FieldDecoration fieldDecoration = FieldDecorationRegistry
								.getDefault().getFieldDecoration(
										FieldDecorationRegistry.DEC_REQUIRED);
						return fieldDecoration.getImage();
					}
					return super.getImage(status);
				}
			};

			// Attach the control decoration.
			ControlDecorationSupport.create(binding, SWT.TOP, null,
					decorationUpdater);

			// Map the created binding to its name, i.e. the Label's text.
			bindingMapName.put(binding, labelText);
		}
	}

	/**
	 * Custom {@link IValidationMessageProvider} which does the following:
	 * <ul>
	 * <li>Every validation message of a binding is prefixed by the binding's
	 * name, if available.</li>
	 * <li>Validation errors due to empty, required fields are not displayed as
	 * errors but as simple text without any icon.</li>
	 * </ul>
	 */
	private static final class CustomMessageProvider extends
			ValidationMessageProvider {

		private final Map<Binding, String> bindingMapName;

		public CustomMessageProvider(Map<Binding, String> bindingMapName) {
			this.bindingMapName = bindingMapName;
		}

		@Override
		public String getMessage(ValidationStatusProvider statusProvider) {
			if (statusProvider != null) {
				String name = bindingMapName.get(statusProvider);
				if (name != null) {
					// For named bindings, we display "<name>: <message>"
					String message = super.getMessage(statusProvider);
					return name + ": " + message;
				}
			}
			return super.getMessage(statusProvider);
		}

		@Override
		public int getMessageType(ValidationStatusProvider statusProvider) {
			if (statusProvider instanceof Binding) {
				Binding binding = (Binding) statusProvider;
				IStatus status = binding.getValidationStatus().getValue();

				// For required validations, we do not want to display an error
				// icon since the user has not done anything wrong.
				if (status.matches(IStatus.ERROR)) {
					IObservableValue<?> target = (IObservableValue<?>) binding.getTarget();
					// If the input is empty, we do not display any error icon.
					if ("".equals(target.getValue())) {
						return IMessageProvider.NONE;
					}
				}
			}
			return super.getMessageType(statusProvider);
		}
	}

	private static final class RequiredValidator implements IValidator<Object> {

		@Override
		public IStatus validate(Object value) {
			if (value == null || "".equals(value)) {
				return ValidationStatus.error("Please specify a value.");
			}
			return ValidationStatus.ok();
		}
	}
}
