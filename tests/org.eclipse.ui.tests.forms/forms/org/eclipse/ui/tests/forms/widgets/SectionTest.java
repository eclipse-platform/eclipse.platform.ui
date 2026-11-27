/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     vogella GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SectionTest {

	private static final Color BODY = new Color(255, 255, 255);
	private static final Color LIGHT_TITLE = new Color(234, 234, 234);
	private static final Color DARK_TITLE = new Color(79, 83, 85);

	private Shell shell;
	private Section section;

	@BeforeEach
	public void createSection() {
		shell = new Shell(Display.getDefault());
		section = new Section(shell, ExpandableComposite.TITLE_BAR);
		section.setText("Title");
		section.setSize(200, 100);
		section.setBackground(BODY);
		section.setTitleBarBackground(LIGHT_TITLE);
		section.setTitleBarGradientBackground(LIGHT_TITLE);
	}

	@AfterEach
	public void disposeShell() {
		shell.dispose();
	}

	@Test
	public void testTitleBarColorChangeRebuildsHeaderImage() {
		Image before = paintedHeaderImage();

		section.setTitleBarBackground(DARK_TITLE);

		assertNull(section.getBackgroundImage());
		assertNotSame(before, paintedHeaderImage());
	}

	@Test
	public void testUnchangedTitleBarColorKeepsHeaderImage() {
		Image before = paintedHeaderImage();

		section.setTitleBarBackground(new Color(LIGHT_TITLE.getRGB()));
		section.setBackground(new Color(BODY.getRGB()));

		assertSame(before, section.getBackgroundImage());
	}

	@Test
	public void testBackgroundChangeDropsHeaderImage() {
		paintedHeaderImage();

		section.setBackground(DARK_TITLE);

		assertNull(section.getBackgroundImage());
	}

	private Image paintedHeaderImage() {
		Image target = new Image(section.getDisplay(), 200, 100);
		GC gc = new GC(target);
		try {
			Event event = new Event();
			event.gc = gc;
			section.notifyListeners(SWT.Paint, event);
		} finally {
			gc.dispose();
			target.dispose();
		}
		Image headerImage = section.getBackgroundImage();
		assertNotNull(headerImage);
		return headerImage;
	}
}
