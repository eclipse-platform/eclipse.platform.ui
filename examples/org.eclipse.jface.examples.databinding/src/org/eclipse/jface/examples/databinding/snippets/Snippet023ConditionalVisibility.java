/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater;
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

/**
 * @since 3.2
 * 
 */
public class Snippet023ConditionalVisibility {
	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				new Snippet023ConditionalVisibility().createControls(shell);
			}
		});

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	Text text;
	Text toText;
	Text fromText;

	/**
	 * @param shell
	 */
	private void createControls(Shell shell) {
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
		fromText = new Text(rangeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);

		Label toLabel = new Label(rangeGroup, SWT.NONE);
		toLabel.setText("To:");
		toText = new Text(rangeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridLayoutFactory.swtDefaults().numColumns(2)
				.generateLayout(rangeGroup);

		final Group textGroup = new Group(oneOfTwo, SWT.NONE);
		textGroup.setText("Text");
		Label label = new Label(textGroup, SWT.NONE);
		label.setText("Text:");
		text = new Text(textGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(textGroup);

		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);

		final ISWTObservableValue rangeSelected = SWTObservables
				.observeSelection(rangeButton);
		final ISWTObservableValue textSelected = SWTObservables
				.observeSelection(textButton);

		// Note that ControlUpdater is not API.
		new ControlUpdater(oneOfTwo) {
			protected void updateControl() {
				if (((Boolean) rangeSelected.getValue()).booleanValue()) {
					stackLayout.topControl = rangeGroup;
					oneOfTwo.layout();
				} else if (((Boolean) textSelected.getValue()).booleanValue()) {
					stackLayout.topControl = textGroup;
					oneOfTwo.layout();
				}
			}
		};
	}
}
