/*******************************************************************************
 * Copyright (c) 2004, 2023 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 490700
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.junit.Test;

/**
 * @since 3.0
 */
public class ImageRegistryTest {

	@Test
	public void testGetNull() {
		ImageRegistry reg = JFaceResources.getImageRegistry();

		Image result = reg.get((String) null);
		assertNull("Registry should handle null", result);
	}

	@Test
	public void testGetString() {

		// note, we must touch the class to ensure the static initialer runs
		// so the image registry is up to date
		Dialog.getBlockedHandler();

		@SuppressWarnings("deprecation")
		String[] imageNames = new String[] { Dialog.DLG_IMG_ERROR, Dialog.DLG_IMG_INFO, Dialog.DLG_IMG_QUESTION,
				Dialog.DLG_IMG_WARNING, Dialog.DLG_IMG_MESSAGE_ERROR, Dialog.DLG_IMG_MESSAGE_INFO,
				Dialog.DLG_IMG_MESSAGE_WARNING };

		ImageRegistry reg = JFaceResources.getImageRegistry();

		for (String imageName : imageNames) {
			Image image1 = reg.get(imageName);
			assertNotNull("Returned null image", image1);
		}

	}

	/**
	 * check that we get non-null versions of the <code>IconAndMessageDialog</code>
	 * images so we know that the code using them can rely on them.
	 *
	 * Note that they can be <code>null</code> from SWT.
	 */
	@Test
	public void testGetIconMessageDialogImages() {

		IconAndMessageDialog iconDialog = new MessageDialog(null, "testGetDialogIcons", null, "Message", Window.CANCEL,
				0, "cancel");

		Image[] images = new Image[] { iconDialog.getErrorImage(), iconDialog.getInfoImage(),
				iconDialog.getQuestionImage(), iconDialog.getWarningImage() };

		for (Image image : images) {
			assertNotNull("Returned null image", image);
		}

	}
}
