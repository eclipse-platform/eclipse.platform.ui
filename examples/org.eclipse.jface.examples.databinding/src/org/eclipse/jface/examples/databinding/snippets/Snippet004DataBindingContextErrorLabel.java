/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.jface.databinding.BindSpec;
import org.eclipse.jface.databinding.DataBindingContext;
import org.eclipse.jface.databinding.observable.Realm;
import org.eclipse.jface.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet that displays how to bind the validation error of the
 * {@link DataBindingContext} to a label.
 * 
 * @since 3.2
 */
public class Snippet004DataBindingContextErrorLabel {
    public static void main(String[] args) {
        Shell shell = new Shell();
        Display display = shell.getDisplay();
        Realm.setDefault(SWTObservables.getRealm(display));
        shell.setLayout(new GridLayout(2, false));

        new Label(shell, SWT.NONE).setText("Enter '5' to be valid:");

        Text text = new Text(shell, SWT.BORDER);
        WritableValue value = new WritableValue(String.class);
        new Label(shell, SWT.NONE).setText("Error:");

        Label errorLabel = new Label(shell, SWT.BORDER);
        errorLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
        GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).applyTo(errorLabel);

        DataBindingContext dbc = new DataBindingContext();

        // Bind the text to the value.
        dbc.bindValue(SWTObservables.getText(text, SWT.Modify),
                value,
                new BindSpec().setDomainValidator(new FiveValidator()));

        // Bind the error label to the validation error on the dbc.
        dbc.bindValue(SWTObservables.getText(errorLabel), dbc.getValidationError(), null);

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    /**
     * Validator that returns validation errors for any value other than 5.
     * 
     * @since 3.2
     */
    private static class FiveValidator implements IDomainValidator {
        public ValidationError isValid(Object value) {
            return ("5".equals(value)) ? null : ValidationError.error("the value was '" + value + "', not '5'");
        }
    }
}
