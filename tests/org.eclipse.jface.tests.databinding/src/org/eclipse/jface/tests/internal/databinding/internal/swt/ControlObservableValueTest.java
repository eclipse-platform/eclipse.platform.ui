/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 170848
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.swt.ControlObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public class ControlObservableValueTest extends TestCase {
	private Shell shell;

	protected void setUp() throws Exception {
		shell = new Shell();

		Realm.setDefault(SWTObservables.getRealm(shell.getDisplay()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
			shell = null;
		}

		Realm.setDefault(null);
	}

	public void testSetValueEnabled() throws Exception {
		ControlObservableValue observableValue = new ControlObservableValue(
				shell, SWTProperties.ENABLED);
		Boolean value = Boolean.FALSE;
		observableValue.setValue(value);
		assertFalse(shell.isEnabled());
	}

	public void testGetValueEnabled() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.ENABLED);
		shell.setEnabled(false);
		assertEquals(Boolean.FALSE, value.getValue());
	}

	public void testGetValueTypeEnabled() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.ENABLED);
		assertEquals(boolean.class, value.getValueType());
	}

	public void testSetValueVisible() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.VISIBLE);
		value.setValue(Boolean.FALSE);
		assertFalse(shell.isVisible());
	}

	public void testGetValueVisible() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.VISIBLE);
		shell.setVisible(false);
		assertEquals(Boolean.FALSE, value.getValue());
	}

	public void testGetValueTypeVisible() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.VISIBLE);
		assertEquals(Boolean.TYPE, value.getValueType());
	}

	public void testSetValueForeground() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.FOREGROUND);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);

		value.setValue(color);
		assertEquals(color, shell.getForeground());
	}

	public void testGetValueForeground() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.FOREGROUND);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		shell.setForeground(color);
		assertEquals(color, value.getValue());
	}

	public void testGetValueTypeForgroundColor() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.FOREGROUND);
		assertEquals(Color.class, value.getValueType());
	}

	public void testGetValueBackground() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.BACKGROUND);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		shell.setBackground(color);
		assertEquals(color, value.getValue());
	}

	public void testSetValueBackground() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.BACKGROUND);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);

		value.setValue(color);
		assertEquals(color, shell.getBackground());
	}

	public void testGetValueTypeBackgroundColor() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.BACKGROUND);
		assertEquals(Color.class, value.getValueType());
	}

	public void testGetValueTypeTooltip() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.TOOLTIP_TEXT);
		assertEquals(String.class, value.getValueType());
	}

	public void testSetValueFont() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.FONT);

		Font font = JFaceResources.getDialogFont();

		value.setValue(font);
		assertEquals(font, shell.getFont());
	}

	public void testGetValueFont() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.FONT);

		Font font = JFaceResources.getDialogFont();
		shell.setFont(font);
		assertEquals(font, value.getValue());
	}

	public void testGetValueTypeFont() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.FONT);
		assertEquals(Font.class, value.getValueType());
	}

	public void testSetValueTooltipText() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.TOOLTIP_TEXT);
		String text = "text";
		value.setValue(text);
		assertEquals(text, shell.getToolTipText());
	}

	public void testGetValueTooltipText() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.TOOLTIP_TEXT);
		String text = "text";
		shell.setToolTipText(text);
		assertEquals(text, value.getValue());
	}

	public void testGetValueTypeTooltipText() throws Exception {
		ControlObservableValue value = new ControlObservableValue(shell,
				SWTProperties.TOOLTIP_TEXT);
		assertEquals(String.class, value.getValueType());
	}
}
