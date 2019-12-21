/*******************************************************************************
 * Copyright (c) 2020 SAP SE and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Marcus Hoepfner (SAP SE) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.SectionFactory;
import org.eclipse.ui.forms.widgets.Twistie;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SectionFactoryTest {
	protected static Shell shell;

	@Before
	public void setup() {
		shell = new Shell();
	}

	@After
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void createsSection() {
		Section section = SectionFactory.newSection(SWT.NONE).create(shell);
		assertEquals(shell, section.getParent());
	}

	@Test
	public void createsSectionWithText() {
		Section section = SectionFactory.newSection(SWT.NONE).title("test").create(shell);
		assertEquals("test", section.getText());
	}

	@Test
	public void createsSectionWithDescription() {
		Section section = SectionFactory.newSection(Section.DESCRIPTION).description("test").create(shell);
		assertEquals("test", section.getDescription());
	}

	@Test
	public void createsSectionWithDescriptionControl() {
		Section section = SectionFactory.newSection(SWT.NONE).description(parent -> new Label(parent, SWT.NONE))
				.create(shell);
		assertTrue(section.getDescriptionControl() instanceof Label);
	}

	@Test
	public void addsSectionExpandListeners() {
		final ExpansionEvent[] raisedEvents = new ExpansionEvent[1];
		Section section = SectionFactory.newSection(Section.TWISTIE).onExpanded(e -> raisedEvents[0] = e)
				.create(shell);
		Control twistie = section.getChildren()[0];
		assertTrue("Expected a twistie", twistie instanceof Twistie);
		click(twistie);

		assertNotNull(raisedEvents[0]);
	}

	@Test
	public void addsSectionExpandingListener() {
		final ExpansionEvent[] raisedEvents = new ExpansionEvent[1];
		Section section = SectionFactory.newSection(Section.TWISTIE).onExpanding(e -> raisedEvents[0] = e)
				.create(shell);
		Control twistie = section.getChildren()[0];
		assertTrue("Expected a twistie", twistie instanceof Twistie);
		click(twistie);

		assertNotNull(raisedEvents[0]);
	}

	private void click(Control twistie) {
		Event event = new Event();
		event.keyCode = SWT.ARROW_RIGHT;
		twistie.notifyListeners(SWT.KeyDown, event);
	}
}