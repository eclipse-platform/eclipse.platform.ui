/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CTabRenderingTest {

	private static final int WIDTH = 300;
	private static final int HEIGHT = 200;

	private Shell shell;
	private CTabFolder folder;
	private TestableCTabRendering renderer;
	private Color folderBackground;
	private Color selectedTabFill;

	@BeforeEach
	public void setUp() {
		Display display = Display.getDefault();
		folderBackground = display.getSystemColor(SWT.COLOR_BLUE);
		selectedTabFill = display.getSystemColor(SWT.COLOR_RED);

		shell = new Shell(display);
		folder = new CTabFolder(shell, SWT.NONE);
		folder.setBounds(0, 0, WIDTH, HEIGHT);
		folder.setBackground(folderBackground);
		renderer = new TestableCTabRendering(folder);
		folder.setRenderer(renderer);
		renderer.setSelectedTabFill(selectedTabFill);
		renderer.setDrawCustomTabContentBackground(true);
	}

	@AfterEach
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testFolderWithoutTabsKeepsFolderBackgroundInContentArea() {
		assertEquals(folderBackground.getRGB(), paintAndGetContentAreaColor());
	}

	@Test
	public void testFolderWithTabsButNoSelectedTabKeepsFolderBackgroundInContentArea() {
		new CTabItem(folder, SWT.NONE).setText("Tab");
		assertEquals(-1, folder.getSelectionIndex());

		assertEquals(folderBackground.getRGB(), paintAndGetContentAreaColor());
	}

	@Test
	public void testFolderWithSelectedTabPaintsSelectedTabFillInContentArea() {
		new CTabItem(folder, SWT.NONE).setText("Tab");
		folder.setSelection(0);

		assertEquals(selectedTabFill.getRGB(), paintAndGetContentAreaColor());
	}

	/**
	 * Paints the folder body and header the way {@link CTabFolder} does on a
	 * paint event and returns the color in the middle of the content area below
	 * the tab row.
	 */
	private RGB paintAndGetContentAreaColor() {
		Image image = new Image(folder.getDisplay(), WIDTH, HEIGHT);
		try {
			GC gc = new GC(image);
			try {
				gc.setBackground(folder.getBackground());
				Rectangle bodyBounds = new Rectangle(0, 0, WIDTH, HEIGHT);
				renderer.draw(CTabFolderRenderer.PART_BODY, SWT.BACKGROUND | SWT.FOREGROUND, bodyBounds, gc);
				renderer.draw(CTabFolderRenderer.PART_HEADER, SWT.BACKGROUND | SWT.FOREGROUND, bodyBounds, gc);
			} finally {
				gc.dispose();
			}
			ImageData imageData = image.getImageData();
			return imageData.palette.getRGB(imageData.getPixel(WIDTH / 2, HEIGHT * 3 / 4));
		} finally {
			image.dispose();
		}
	}

	private static class TestableCTabRendering extends CTabRendering {

		TestableCTabRendering(CTabFolder parent) {
			super(parent);
		}

		// Re-declared so that the test, loaded by a different bundle class loader
		// than CTabRendering, may call the protected draw method
		@Override
		protected void draw(int part, int state, Rectangle bounds, GC gc) {
			super.draw(part, state, bounds, gc);
		}
	}
}
