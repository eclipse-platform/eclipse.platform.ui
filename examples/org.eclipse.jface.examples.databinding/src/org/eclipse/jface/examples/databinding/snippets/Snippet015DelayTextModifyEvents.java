/************************************************************************************************************
 * Copyright (c) 2007, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Matthew Hall - initial API and implementation (bug 180746)
 * 		Boris Bokowski, IBM - initial API and implementation
 *      Matthew Hall - bugs 260329, 264286
 ***********************************************************************************************************/
package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet015DelayTextModifyEvents {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	private static Shell createShell() {
		Shell shell = new Shell();
		shell.setLayout(new GridLayout(3, false));

		final Label field1 = new Label(shell, SWT.NONE);
		field1.setText("Field 1 ");

		Text text1 = new Text(shell, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).hint(200, SWT.DEFAULT).applyTo(text1);
		new Label(shell, SWT.NONE).setText("200 ms delay");

		Label field2 = new Label(shell, SWT.NONE);
		field2.setText("Field 2 ");

		Text text2 = new Text(shell, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).hint(200, SWT.DEFAULT).applyTo(text2);

		new Label(shell, SWT.NONE).setText("2000 ms delay");

		IObservableValue<String> delayed1 = WidgetProperties.text(SWT.Modify).observeDelayed(200, text1);
		IObservableValue<String> delayed2 = WidgetProperties.text(SWT.Modify).observeDelayed(2000, text2);

		// (In a real application, you would want to dispose the resource manager when
		// you are done with it)
		ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
		final Font shellFont = shell.getFont();
		final Font italicFont = resourceManager.create(FontDescriptor.createFrom(shellFont).setStyle(SWT.ITALIC));

		IObservableValue<Boolean> stale1 = Observables.observeStale(delayed1);
		IObservableValue<Boolean> stale2 = Observables.observeStale(delayed2);

		Label info = new Label(shell, SWT.WRAP);
		info.setText(
				"Pending changes are applied immediately if the observed control loses focus, or enter is pressed.");
		GridDataFactory.fillDefaults().span(3, 1).hint(300, SWT.DEFAULT).applyTo(info);

		DataBindingContext bindingContext = new DataBindingContext();

		IValueProperty<Boolean, Font> fontProperty = Properties.convertedValue(stale -> stale ? italicFont : shellFont);

		bindingContext.bindValue(WidgetProperties.font().observe(field2), fontProperty.observeDetail(stale1));
		bindingContext.bindValue(WidgetProperties.font().observe(field1), fontProperty.observeDetail(stale2));

		bindingContext.bindValue(delayed1, delayed2);

		// Sometimes it is useful to manually flush the delayed observables. This can be
		// done in the following two ways.

		text2.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				// The DataBindingContext update methods flushes delayed observables in bulk
				bindingContext.updateTargets();
			}
		});

		text1.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				// Calling getValue on a delayed observable flushes its value
				delayed1.getValue();
			}
		});

		shell.pack();
		shell.open();

		return shell;
	}
}
