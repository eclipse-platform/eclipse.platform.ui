/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ControlDecorationTests extends AbstractFieldAssistTestCase {

	private Text anotherControl;

	@Test
	public void testDecorationIsVisible() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.open();
		ControlDecoration decoration = new ControlDecoration(getFieldAssistWindow().getFieldAssistControl(), SWT.RIGHT);
		decoration.setImage(FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
		decoration.setDescriptionText("foo");
		window.open();
		assertTrue(decoration.isVisible(), "1.0");
		decoration.hide();
		assertFalse(decoration.isVisible(), "1.1");
		decoration.show();
		assertTrue(decoration.isVisible(), "1.2");
		window.getFieldAssistControl().setVisible(false);
		assertFalse(decoration.isVisible(), "1.3");
		window.getFieldAssistControl().setVisible(true);
		assertTrue(decoration.isVisible(), "1.4");

		// focus related tests.  Comment out for now.
		// see bug 275393
		decoration.setShowOnlyOnFocus(true);
		anotherControl.setFocus();
		spinEventLoop();

		/*
		assertFalse("1.5", decoration.isVisible());
		window.getFieldAssistControl().setFocus();
		spinEventLoop();
		assertTrue("1.6", decoration.isVisible());
		decoration.setShowOnlyOnFocus(false);
		*/

	}

	@Test
	public void testHoverVisibility() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.open();
		ControlDecoration decoration = new ControlDecoration(window.getFieldAssistControl(), SWT.RIGHT);
		decoration.setImage(FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
		decoration.setDescriptionText("foo");
		assertTrue(decoration.isVisible(), "1.0");
		assertOneShellUp();
		decoration.hide();
		decoration.showHoverText("Show me");
		assertOneShellUp(); // because the decoration is hidden;
		decoration.show();
		getFieldAssistWindow().getFieldAssistControl().setVisible(false);
		decoration.showHoverText("Show me");
		assertOneShellUp(); // because the control is hidden, bug 295386
		getFieldAssistWindow().getFieldAssistControl().setVisible(true);
		decoration.showHoverText("Show me");
		assertTwoShellsUp();
	}

	// focus related tests
	@Test
	@Disabled("Disabled see Bug 418420 and bug 275393")
	public void testBug418420() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.open();
		ControlDecoration decoration = new ControlDecoration(window.getFieldAssistControl(), SWT.RIGHT);
		decoration.setImage(FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
		decoration.setDescriptionText("foo");
		decoration.setShowOnlyOnFocus(true);
		anotherControl.forceFocus();
		decoration.showHoverText("Show me");
		assertOneShellUp();
	}

	@Override
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		return new TextFieldAssistWindow() {
				@Override
				protected void createExtraControls(Composite parent) {
					anotherControl = new Text(parent, SWT.DEFAULT);
				}
			};
	}
}
