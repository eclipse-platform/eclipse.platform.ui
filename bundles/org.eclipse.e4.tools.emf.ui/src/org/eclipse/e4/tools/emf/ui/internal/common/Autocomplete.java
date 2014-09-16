/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;

/**
 *
 * @author Steven Spungin
 *
 */
public class Autocomplete {

	public static void installOn(ComboViewer comboViewer) {
		final Combo combo = comboViewer.getCombo();

		combo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				setClosestMatch(combo);
			}

			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.keyCode == SWT.BS) {
					Point pt = combo.getSelection();
					combo.setSelection(new Point(Math.max(0, pt.x - 1), pt.y));
				}
			}
		});

		combo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				combo.setSelection(new Point(0, combo.getText().length()));
			}

			@Override
			public void focusGained(FocusEvent e) {
				combo.setSelection(new Point(0, combo.getText().length()));
			}
		});

		combo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				combo.setSelection(new Point(0, combo.getText().length()));
			}
		});
	}

	private static void setClosestMatch(Combo combo) {
		String[] items = combo.getItems();
		if (items.length == 0) {
			return;
		}

		// TODO this would be faster using binary search if we could insure that
		// the items are sorted in natural order.

		String str = combo.getText();
		Pattern pattern = Pattern.compile(str + ".*", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

		int index = -1;
		int length = 0;
		for (int i = 0; i < items.length; i++) {
			Matcher m = pattern.matcher(items[i]);
			if (m.matches()) {
				if (index == -1 || items[i].length() < length) {
					length = items[i].length();
					index = i;
				}
			}
		}
		if (index == -1) {
			index = 0;
		}
		Point pt = combo.getSelection();
		String selectedText = items[index];
		combo.select(index);
		combo.setText(selectedText);
		combo.setSelection(new Point(pt.x, selectedText.length()));
	}
}
