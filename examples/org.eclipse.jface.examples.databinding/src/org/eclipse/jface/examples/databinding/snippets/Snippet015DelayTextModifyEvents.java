/************************************************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet015DelayTextModifyEvents {

	private static void createControls(Shell shell) {
		final Label field1 = createLabel(shell, SWT.NONE, "Field 1 ");

		Text text1 = new Text(shell, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).hint(200, SWT.DEFAULT)
				.applyTo(text1);
		createLabel(shell, SWT.NONE, "200ms delay");

		final Label field2 = createLabel(shell, SWT.NONE, "Field 2 ");

		Text text2 = new Text(shell, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).hint(200, SWT.DEFAULT)
				.applyTo(text2);

		createLabel(shell, SWT.NONE, "1000ms delay");

		final ISWTObservableValue delayed1 = WidgetProperties.text(SWT.Modify)
				.observeDelayed(200, text1);
		final ISWTObservableValue delayed2 = WidgetProperties.text(SWT.Modify)
				.observeDelayed(1000, text2);

		// (In a real application,you would want to dispose the resource manager
		// when you are done with it)
		ResourceManager resourceManager = new LocalResourceManager(
				JFaceResources.getResources());
		final Font shellFont = shell.getFont();
		final Font italicFont = resourceManager.createFont(FontDescriptor
				.createFrom(shellFont).setStyle(SWT.ITALIC));

		final IObservableValue stale1 = Observables.observeStale(delayed1);
		new ControlUpdater(field2) {
			@Override
			protected void updateControl() {
				boolean stale = ((Boolean) stale1.getValue()).booleanValue();
				field2.setFont(stale ? italicFont : shellFont);
			}
		};

		final IObservableValue stale2 = Observables.observeStale(delayed2);
		new ControlUpdater(field1) {
			@Override
			protected void updateControl() {
				boolean stale = ((Boolean) stale2.getValue()).booleanValue();
				field1.setFont(stale ? italicFont : shellFont);
			}
		};

		String info = "Pending changes are applied immediately if the observed control loses focus";
		GridDataFactory.fillDefaults().span(3, 1).applyTo(
				createLabel(shell, SWT.WRAP, info));

		DataBindingContext dbc = new DataBindingContext();

		dbc.bindValue(delayed1, delayed2);
	}

	private static Label createLabel(Composite parent, int style, String text) {
		Label label = new Label(parent, style);
		label.setText(text);
		return label;
	}

	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell();
				shell.setLayout(new GridLayout(3, false));

				createControls(shell);

				shell.pack();
				shell.open();
				while (!shell.isDisposed())
					if (!display.readAndDispatch())
						display.sleep();
			}

		});

		display.dispose();
	}

}
