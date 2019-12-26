/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Snippet023ConditionalVisibility {
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
		shell.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(shell, SWT.NONE);
		Group radioGroup = new Group(composite, SWT.NONE);
		radioGroup.setText("Type");
		Button textButton = new Button(radioGroup, SWT.RADIO);
		textButton.setText("Text");
		Button rangeButton = new Button(radioGroup, SWT.RADIO);
		rangeButton.setText("Range");
		GridLayoutFactory.swtDefaults().generateLayout(radioGroup);

		final Composite oneOfTwo = new Composite(composite, SWT.NONE);
		final StackLayout stackLayout = new StackLayout();
		oneOfTwo.setLayout(stackLayout);

		final Group rangeGroup = new Group(oneOfTwo, SWT.NONE);
		rangeGroup.setText("Range");
		Label fromLabel = new Label(rangeGroup, SWT.NONE);
		fromLabel.setText("From:");
		new Text(rangeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);

		Label toLabel = new Label(rangeGroup, SWT.NONE);
		toLabel.setText("To:");
		new Text(rangeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(rangeGroup);

		final Group textGroup = new Group(oneOfTwo, SWT.NONE);
		textGroup.setText("Text");
		Label label = new Label(textGroup, SWT.NONE);
		label.setText("Text:");
		new Text(textGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(textGroup);

		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);

		final IObservableValue<Boolean> rangeSelected = WidgetProperties.buttonSelection().observe(rangeButton);
		final IObservableValue<Boolean> textSelected = WidgetProperties.buttonSelection().observe(textButton);

		ISideEffect.create(() -> {
			// Get both values up front so that the observable tracker records them
			boolean isRange = rangeSelected.getValue();
			boolean isText = textSelected.getValue();
			if (isRange) {
				stackLayout.topControl = rangeGroup;
				oneOfTwo.layout();
			} else if (isText) {
				stackLayout.topControl = textGroup;
				oneOfTwo.layout();
			}
		});

		shell.pack();
		shell.open();
		return shell;
	}
}
