/*******************************************************************************
 * Copyright (c) 2022 Mat Booth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Demo of the JFace {@link ColorSelector} which is compound widget comprising a
 * color swatch button and the native color chooser dialog.
 */
public class Snippet082ColorSelectDialog {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));
		shell.setText("Color Select Dialog Demo");
		shell.setSize(320, 240);
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		Label bgLabel = new Label(shell, SWT.NONE);
		bgLabel.setText("Background Color:");
		ColorSelector bgColorSelector = new ColorSelector(shell);
		bgColorSelector.setColorValue(shell.getBackground().getRGB());
		Button bgColorButton = bgColorSelector.getButton();

		Label fgLabel = new Label(shell, SWT.NONE);
		fgLabel.setText("Foreground Color:");
		ColorSelector fgColorSelector = new ColorSelector(shell);
		fgColorSelector.setColorValue(shell.getForeground().getRGB());
		Button fgColorButton = fgColorSelector.getButton();

		bgColorButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			shell.setBackground(new Color(bgColorSelector.getColorValue()));
		}));

		fgColorButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			Color color = new Color(fgColorSelector.getColorValue());
			shell.setForeground(color);
			// On Windows 10, foreground color is not automatically inherited by default
			bgLabel.setForeground(color);
			fgLabel.setForeground(color);
		}));

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}
}
