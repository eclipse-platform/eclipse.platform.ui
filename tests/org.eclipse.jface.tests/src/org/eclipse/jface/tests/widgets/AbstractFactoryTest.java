/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 ******************************************************************************/
package org.eclipse.jface.tests.widgets;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageGcDrawer;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class AbstractFactoryTest {
	protected static Shell shell;
	protected static Image image;

	@BeforeAll
	public static void classSetup() {
		final ImageGcDrawer noOp = (gc, width, height) -> {};
		image = new Image(null, noOp, 1, 1);
	}

	@BeforeEach
	public void setup() {
		shell = new Shell();
	}

	@AfterEach
	public void tearDown() {
		shell.dispose();
	}

	@AfterAll
	public static void classTearDown() {
		image.dispose();
		shell.dispose();
	}

}
