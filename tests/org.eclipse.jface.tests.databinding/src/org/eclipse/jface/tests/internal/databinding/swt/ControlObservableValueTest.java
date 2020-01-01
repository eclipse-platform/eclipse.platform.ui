/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 170848
 *     Matthew Hall - bug 194734
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class ControlObservableValueTest extends AbstractDefaultRealmTestCase {
	private Shell shell;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		shell = new Shell();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
			shell = null;
		}
	}

	@Test
	public void testSetValueEnabled() throws Exception {
		IObservableValue<Boolean> observableValue = WidgetProperties.enabled().observe(shell);
		observableValue.setValue(false);
		assertFalse(shell.isEnabled());
	}

	@Test
	public void testGetValueEnabled() throws Exception {
		IObservableValue<Boolean> value = WidgetProperties.enabled().observe(shell);
		shell.setEnabled(false);
		assertFalse(value.getValue());
	}

	@Test
	public void testGetValueTypeEnabled() throws Exception {
		IObservableValue<Boolean> value = WidgetProperties.enabled().observe(shell);
		assertEquals(boolean.class, value.getValueType());
	}

	@Test
	public void testSetValueVisible() throws Exception {
		IObservableValue<Boolean> value = WidgetProperties.visible().observe(shell);
		value.setValue(false);
		assertFalse(shell.isVisible());
	}

	@Test
	public void testGetValueVisible() throws Exception {
		IObservableValue<Boolean> value = WidgetProperties.visible().observe(shell);
		shell.setVisible(false);
		assertFalse(value.getValue());
	}

	@Test
	public void testGetValueTypeVisible() throws Exception {
		IObservableValue<Boolean> value = WidgetProperties.visible().observe(shell);
		assertEquals(boolean.class, value.getValueType());
	}

	@Test
	public void testSetValueForeground() throws Exception {
		IObservableValue<Color> value = WidgetProperties.foreground().observe(shell);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);

		value.setValue(color);
		assertEquals(color, shell.getForeground());
	}

	@Test
	public void testGetValueForeground() throws Exception {
		IObservableValue<Color> value = WidgetProperties.foreground().observe(shell);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		shell.setForeground(color);
		assertEquals(color, value.getValue());
	}

	@Test
	public void testGetValueTypeForgroundColor() throws Exception {
		IObservableValue<Color> value = WidgetProperties.foreground().observe(shell);
		assertEquals(Color.class, value.getValueType());
	}

	@Test
	public void testGetValueBackground() throws Exception {
		IObservableValue<Color> value = WidgetProperties.background().observe(shell);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		shell.setBackground(color);
		assertEquals(color, value.getValue());
	}

	@Test
	public void testSetValueBackground() throws Exception {
		IObservableValue<Color> value = WidgetProperties.background().observe(shell);

		Color color = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);

		value.setValue(color);
		assertEquals(color, shell.getBackground());
	}

	@Test
	public void testGetValueTypeBackgroundColor() throws Exception {
		IObservableValue<Color> value = WidgetProperties.background().observe(shell);
		assertEquals(Color.class, value.getValueType());
	}

	@Test
	public void testGetValueTypeTooltip() throws Exception {
		IObservableValue<String> value = WidgetProperties.tooltipText().observe(shell);
		assertEquals(String.class, value.getValueType());
	}

	@Test
	public void testSetValueFont() throws Exception {
		IObservableValue<Font> value = WidgetProperties.font().observe(shell);

		Font font = JFaceResources.getDialogFont();

		value.setValue(font);
		assertEquals(font, shell.getFont());
	}

	@Test
	public void testGetValueFont() throws Exception {
		IObservableValue<Font> value = WidgetProperties.font().observe(shell);

		Font font = JFaceResources.getDialogFont();
		shell.setFont(font);
		assertEquals(font, value.getValue());
	}

	@Test
	public void testGetValueTypeFont() throws Exception {
		IObservableValue<Font> value = WidgetProperties.font().observe(shell);
		assertEquals(Font.class, value.getValueType());
	}

	@Test
	public void testSetValueTooltipText() throws Exception {
		IObservableValue<String> value = WidgetProperties.tooltipText().observe(shell);
		value.setValue("text");
		assertEquals("text", shell.getToolTipText());
	}

	@Test
	public void testGetValueTooltipText() throws Exception {
		IObservableValue<String> value = WidgetProperties.tooltipText().observe(shell);
		shell.setToolTipText("text");
		assertEquals("text", value.getValue());
	}

	@Test
	public void testGetValueTypeTooltipText() throws Exception {
		IObservableValue<String> value = WidgetProperties.tooltipText().observe(shell);
		assertEquals(String.class, value.getValueType());
	}

	@Test
	public void testObserveFocus() {
		System.out.println("ControlObservableValueTest.testObserveFocus() start active shell: "
				+ shell.getDisplay().getActiveShell());
		shell.setLayout(new FillLayout());
		Text c1 = new Text(shell, SWT.NONE);
		c1.setText("1");
		Text c2 = new Text(shell, SWT.NONE);
		c2.setText("2");
		shell.pack();
		shell.setVisible(true);

		processDisplayQueue();
		System.out.println("active shell (2): " + shell.getDisplay().getActiveShell());

		shell.forceActive();
		System.out.println("active shell (3): " + shell.getDisplay().getActiveShell());

		assertTrue(c1.setFocus());
		Control focus = shell.getDisplay().getFocusControl();
		System.out.println("focus control (1): " + focus + ", c2? " + (focus == c2));
		System.out.println("active shell (4): " + shell.getDisplay().getActiveShell());

		IObservableValue<Boolean> value = WidgetProperties.focused().observe(c2);
		ValueChangeEventTracker<Boolean> tracker = ValueChangeEventTracker.observe(value);

		assertTrue(c2.setFocus());

		processDisplayQueue();
		focus = shell.getDisplay().getFocusControl();
		System.out.println("focus control (2): " + focus + ", c2? " + (focus == c2));
		System.out.println("active shell (5): " + shell.getDisplay().getActiveShell());

		System.out.println("Value (should be true): " + value.getValue());
		Screenshots.takeScreenshot(getClass(), getClass().getSimpleName(), System.out);

		assertTrue(value.getValue());

		assertEquals(1, tracker.count);
		assertFalse(tracker.event.diff.getOldValue());
		assertTrue(tracker.event.diff.getNewValue());
	}

	private void processDisplayQueue() {
		while (Display.getCurrent().readAndDispatch()) {
		}
	}
}
