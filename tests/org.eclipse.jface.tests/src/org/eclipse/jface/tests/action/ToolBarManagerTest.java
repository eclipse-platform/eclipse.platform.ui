/*******************************************************************************
 * Copyright (c) 2013, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - add testcase for Bug #551587 and #567905
 ******************************************************************************/

package org.eclipse.jface.tests.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.Rule;
import org.junit.Test;

public class ToolBarManagerTest {

	private static final int DEFAULT_STYLE = SWT.WRAP | SWT.FLAT | SWT.RIGHT;

	@Rule
	public JFaceActionRule rule = new JFaceActionRule();

	public void testSetStyleWhenToolBarDoesNotExist() {
		Composite parent = createComposite();
		ToolBarManager manager = new ToolBarManager(DEFAULT_STYLE | SWT.HORIZONTAL);

		manager.setStyle(DEFAULT_STYLE | SWT.VERTICAL);
		ToolBar toolBar = manager.createControl(parent);
		assertFalse(toolBar.isDisposed());
		verifyOrientation(toolBar, SWT.VERTICAL);
	}

	@Test
	public void testSetStyleWhenToolBarExists() {
		Composite parent = createComposite();
		ToolBar toolBar = new ToolBar(parent, DEFAULT_STYLE | SWT.VERTICAL);
		ToolBarManager manager = new ToolBarManager(toolBar);

		manager.setStyle(DEFAULT_STYLE | SWT.HORIZONTAL);
		assertSame(toolBar, manager.createControl(parent));
		assertFalse(toolBar.isDisposed());

		toolBar.dispose();
		ToolBar newToolBar = manager.createControl(parent);
		assertNotSame(toolBar, newToolBar);
		assertFalse(newToolBar.isDisposed());
		verifyOrientation(newToolBar, SWT.HORIZONTAL);
	}

	@Test
	public void testCreateControlWhenParentNull() {
		Composite parent = createComposite();
		ToolBarManager manager = new ToolBarManager(DEFAULT_STYLE | SWT.VERTICAL);

		assertNull(manager.createControl(null));

		ToolBar toolBar = manager.createControl(parent);
		assertNotNull(toolBar);
		assertSame(toolBar, manager.createControl(null));
	}

	@Test
	public void testDispose() {
		Composite parent = createComposite();
		ToolBar toolBar = new ToolBar(parent, DEFAULT_STYLE | SWT.VERTICAL);
		ToolBarManager manager = new ToolBarManager(toolBar);

		manager.dispose();
		assertTrue(toolBar.isDisposed());
	}

	@Test
	public void testUpdate() {
		ToolBarManager manager = new ToolBarManager();
		ObservableControlContribution item = new ObservableControlContribution("i want to be updated!");
		manager.add(item);
		manager.createControl(createComposite());
		assertFalse("Update was called already", item.updateCalled);
		assertTrue("computeWidth was not called", item.computeWidthCalled);
		item.computeWidthCalled = false;
		manager.update(false);
		assertFalse("Item update should only be called when manager update is forced", item.updateCalled);
		assertFalse("computeWidth should only be called when manager update is forced", item.computeWidthCalled);
		manager.update(true);
		assertTrue("Update was not called", item.updateCalled);
		assertTrue("computeWidth was not called", item.computeWidthCalled);
	}

	@Test
	public void testControlContributionIsSet() {
		ToolBarManager manager = new ToolBarManager();
		manager.add(new ControlContribution("test") {

			@Override
			protected Control createControl(Composite parent) {

				return new Label(parent, SWT.NONE);
			}
		});
		ToolBar toolBar = manager.createControl(createComposite());
		for (ToolItem item : toolBar.getItems()) {
			if (!(item.getData() instanceof ControlContribution)) {
				fail("ToolItem data is not set to ControlContribution");
			}
		}
	}

	@Test
	public void testDefaultImageIsGray() {
		boolean oldState = ActionContributionItem.getUseColorIconsInToolbars();
		try {
			ActionContributionItem.setUseColorIconsInToolbars(false);
			ToolBarManager manager = new ToolBarManager();
			Action action = new Action("Button with Hover") {
			};
			ImageDescriptor descriptor = JFaceResources.getImageRegistry().getDescriptor(Dialog.DLG_IMG_MESSAGE_INFO);
			ImageDescriptor hoverDescriptor = JFaceResources.getImageRegistry()
					.getDescriptor(Dialog.DLG_IMG_MESSAGE_ERROR);
			ImageDescriptor disabledDescriptor = JFaceResources.getImageRegistry()
					.getDescriptor(Dialog.DLG_IMG_MESSAGE_WARNING);
			action.setImageDescriptor(descriptor);
			action.setHoverImageDescriptor(hoverDescriptor);
			action.setDisabledImageDescriptor(disabledDescriptor);
			manager.add(action);
			ToolBar toolBar = manager.createControl(createComposite());
			ToolItem[] items = toolBar.getItems();
			assertEquals(1, items.length);

			ToolItem item = items[0];
			assertImageEqualsDescriptor(hoverDescriptor, item.getHotImage());
			assertImageEqualsDescriptor(disabledDescriptor, item.getDisabledImage());
			assertImageEqualsDescriptor(ImageDescriptor.createWithFlags(descriptor, SWT.IMAGE_GRAY), item.getImage());
		} finally {
			ActionContributionItem.setUseColorIconsInToolbars(oldState);
		}
	}

	@Test
	public void testActionImagesAreSet() {
		boolean oldState = ActionContributionItem.getUseColorIconsInToolbars();
		try {
			ActionContributionItem.setUseColorIconsInToolbars(true);
			ToolBarManager manager = new ToolBarManager();
			Action action = new Action("Button with Hover") {
			};
			ImageDescriptor descriptor = JFaceResources.getImageRegistry().getDescriptor(Dialog.DLG_IMG_MESSAGE_INFO);
			ImageDescriptor hoverDescriptor = JFaceResources.getImageRegistry()
					.getDescriptor(Dialog.DLG_IMG_MESSAGE_ERROR);
			ImageDescriptor disabledDescriptor = JFaceResources.getImageRegistry()
					.getDescriptor(Dialog.DLG_IMG_MESSAGE_WARNING);
			action.setImageDescriptor(descriptor);
			action.setHoverImageDescriptor(hoverDescriptor);
			action.setDisabledImageDescriptor(disabledDescriptor);
			manager.add(action);
			ToolBar toolBar = manager.createControl(createComposite());
			ToolItem[] items = toolBar.getItems();
			assertEquals(1, items.length);

			ToolItem item = items[0];
			assertImageEqualsDescriptor(descriptor, item.getImage());
			assertImageEqualsDescriptor(hoverDescriptor, item.getHotImage());
			assertImageEqualsDescriptor(disabledDescriptor, item.getDisabledImage());
		} finally {
			ActionContributionItem.setUseColorIconsInToolbars(oldState);
		}
	}

	private static void assertImageEqualsDescriptor(ImageDescriptor descriptor, Image image) {
		Image createImage = descriptor.createImage();
		try {
			assertImageDataEquals(createImage.getImageData(), image.getImageData());
		} finally {
			createImage.dispose();
		}
	}

	private static void assertImageDataEquals(ImageData im1, ImageData im2) {
		assertNotNull("ImageData 1 is null", im1);
		assertNotNull("ImageData 2 is null", im2);
		assertEquals("ImageData width missmatch", im1.width, im2.width);
		assertEquals("ImageData height missmatch", im1.height, im2.height);
		for (int x = 0; x < im1.width; x++) {
			for (int y = 0; y < im1.height; y++) {
				assertEquals("pixel (" + x + "," + y + ") are not equal", im1.getPixel(x, y), im2.getPixel(x, y));
			}
		}
	}

	@Test
	public void testMissingIsSet() {
		boolean oldState = ActionContributionItem.getUseColorIconsInToolbars();
		try {
			ActionContributionItem.setUseColorIconsInToolbars(true);
			ToolBarManager manager = new ToolBarManager();
			Action action = new Action("Button with Missing") {
			};
			action.setDisabledImageDescriptor(
					JFaceResources.getImageRegistry().getDescriptor(Dialog.DLG_IMG_MESSAGE_WARNING));
			manager.add(action);
			ToolBar toolBar = manager.createControl(createComposite());
			ToolItem[] items = toolBar.getItems();
			assertEquals(1, items.length);

			ToolItem item = items[0];
			assertNotNull(item.getImage());
			Image img = ImageDescriptor.getMissingImageDescriptor().createImage();
			byte[] data = img.getImageData().data;
			img.dispose();
			assertTrue(Arrays.equals(data, item.getImage().getImageData(100).data));
			assertNull(item.getHotImage());
			assertNotNull(item.getDisabledImage());
		} finally {
			ActionContributionItem.setUseColorIconsInToolbars(oldState);
		}
	}

	private Composite createComposite() {
		return new Composite(rule.getShell(), SWT.DEFAULT);
	}

	private static void verifyOrientation(ToolBar toolBar, int expected) {
		assertTrue((toolBar.getStyle() & expected) != 0);

		int opposite = (expected & SWT.HORIZONTAL) != 0 ? SWT.VERTICAL : SWT.HORIZONTAL;
		assertEquals(0, (toolBar.getStyle() & opposite));
	}

	private final class ObservableControlContribution extends ControlContribution {
		private boolean updateCalled;
		private boolean computeWidthCalled;

		private ObservableControlContribution(String id) {
			super(id);
		}

		@Override
		protected Control createControl(Composite parent) {
			return new ComboViewer(parent).getControl();
		}

		@Override
		public void update() {
			super.update();
			updateCalled = true;
		}

		@Override
		protected int computeWidth(Control control) {
			int computeWidth = super.computeWidth(control);
			computeWidthCalled = true;
			return computeWidth;
		}

	}

}
