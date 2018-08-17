/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tips.ui.internal.util.ImageUtil;
import org.junit.Test;

public class ImageUtilTest {

	private static String fImageBase64 = "" //
			+ "iVBORw0KGgoAAAANSUhEUgAAACIAAAAkCAYAAADsHujfAAAAAXNSR0IArs4c6QAAAARnQU1BAACx"
			+ "jwv8YQUAAAAJcEhZcwAAJOgAACToAYJjBRwAAAEcSURBVFhH7dZBCsIwEIXh3MkbuPJubtwI0kOIK"
			+ "IJuddseo+eIPORBDZM0MyPaRQr/IiRtP9osEoa+j0uoQdKykPVtiLunPKdtcx/i9iHPMRECRDi+82K"
			+ "A4LNKGBHCG5kVM0UgjKV1SITgxdMHIC1Gg0DZPeLBaBEoC0EWjAWBihCkwVgRaBaCajAeBKqCoBLGi0"
			+ "DVECRhVtfPsQWBVBAkYZgVgdQQlPsy0traTJB0TzDuGUtqSA7BrBgVJEVgLP0mC6YaIiE49w1MFaSEYF"
			+ "7MLKQGwTyYIkSDYFZMFmJBMAtGhHgQTIsRIdObLQiWYnAWltYhEYJDrhfBiCkhUHGz/rIGSWuQtAZJC+M"
			+ "4xn93Ol9iiAu49oduKZAuvgC5R8NSTiN3qAAAAABJRU5ErkJggg==";

	private static String fImageBase64HTML = "data:image/png;base64," //
			+ "iVBORw0KGgoAAAANSUhEUgAAACIAAAAkCAYAAADsHujfAAAAAXNSR0IArs4c6QAAAARnQU1BAACx"
			+ "jwv8YQUAAAAJcEhZcwAAJOgAACToAYJjBRwAAAEcSURBVFhH7dZBCsIwEIXh3MkbuPJubtwI0kOIK"
			+ "IJuddseo+eIPORBDZM0MyPaRQr/IiRtP9osEoa+j0uoQdKykPVtiLunPKdtcx/i9iHPMRECRDi+82K"
			+ "A4LNKGBHCG5kVM0UgjKV1SITgxdMHIC1Gg0DZPeLBaBEoC0EWjAWBihCkwVgRaBaCajAeBKqCoBLGi0"
			+ "DVECRhVtfPsQWBVBAkYZgVgdQQlPsy0traTJB0TzDuGUtqSA7BrBgVJEVgLP0mC6YaIiE49w1MFaSEYF"
			+ "7MLKQGwTyYIkSDYFZMFmJBMAtGhHgQTIsRIdObLQiWYnAWltYhEYJDrhfBiCkhUHGz/rIGSWuQtAZJC+M"
			+ "4xn93Ol9iiAu49oduKZAuvgC5R8NSTiN3qAAAAABJRU5ErkJggg==";

	@Test
	public void decodeImageToBase64Test() throws IOException {
		Image image = new Image(null, ImageUtil.decodeToImage(fImageBase64));
		String base64Image = ImageUtil.decodeFromImage(image, SWT.IMAGE_PNG);
		image.dispose();
		image = new Image(null, ImageUtil.decodeToImage(base64Image));
		String base64Image2 = ImageUtil.decodeFromImage(image, SWT.IMAGE_PNG);
		assertTrue(base64Image, base64Image.equals(base64Image2));
		image.dispose();

		Image image2 = new Image(null, ImageUtil.decodeToImage(fImageBase64HTML));
		String base64Image3 = ImageUtil.decodeFromImage(image2, SWT.IMAGE_PNG);
		image2.dispose();
		image2 = new Image(null, ImageUtil.decodeToImage(base64Image3));
		String base64Image4 = ImageUtil.decodeFromImage(image2, SWT.IMAGE_PNG);
		assertTrue(base64Image3, base64Image3.equals(base64Image4));
		image2.dispose();
	}

	@Test
	public void testGetWidth() {
		assertTrue(ImageUtil.getWidth(1, 1000, 100) == 100);
		assertTrue(ImageUtil.getWidth(1, 100, 1000) == 100);
		assertTrue(ImageUtil.getWidth(1, 100, 99) == 99);
		assertTrue(ImageUtil.getWidth(1, 100, 98) == 98);
		assertTrue(ImageUtil.getWidth(1, 100, 100) == 100);
		assertTrue(ImageUtil.getWidth(1, 99, 100) == 99);
		assertTrue(ImageUtil.getWidth(1, 77, 77) == 77);
		assertTrue(ImageUtil.getWidth(1, 101, 100) == 100);
		assertTrue(ImageUtil.getWidth(1, 149, 100) == 100);
		assertTrue(ImageUtil.getWidth(1, 200, 300) == 200);
		assertTrue(ImageUtil.getWidth(1, 11, 10) == 10);

		assertTrue(ImageUtil.getWidth(0.5, 1000, 100) == 50);
		assertTrue(ImageUtil.getWidth(0.5, 100, 1000) == 100);
		assertTrue(ImageUtil.getWidth(0.5, 100, 99) == 49);
		assertTrue(ImageUtil.getWidth(0.5, 100, 98) == 49);
		assertTrue(ImageUtil.getWidth(0.5, 100, 100) == 50);
		assertTrue(ImageUtil.getWidth(0.5, 99, 100) == 50);
		assertTrue(ImageUtil.getWidth(0.5, 77, 77) == 38);
		assertTrue(ImageUtil.getWidth(0.5, 101, 100) == 50);
		assertTrue(ImageUtil.getWidth(0.5, 149, 100) == 50);
		assertTrue(ImageUtil.getWidth(0.5, 200, 300) == 150);
		assertTrue(ImageUtil.getWidth(0.5, 11, 10) == 5);

		assertTrue(ImageUtil.getWidth(2, 1000, 100) == 200);
		assertTrue(ImageUtil.getWidth(2, 100, 1000) == 100);
		assertTrue(ImageUtil.getWidth(2, 100, 99) == 100);
		assertTrue(ImageUtil.getWidth(2, 100, 50) == 100);
		assertTrue(ImageUtil.getWidth(2, 100, 49) == 98);
		assertTrue(ImageUtil.getWidth(2, 100, 98) == 100);
		assertTrue(ImageUtil.getWidth(2, 100, 100) == 100);
		assertTrue(ImageUtil.getWidth(2, 99, 100) == 99);
		assertTrue(ImageUtil.getWidth(2, 77, 77) == 77);
		assertTrue(ImageUtil.getWidth(2, 101, 100) == 101);
		assertTrue(ImageUtil.getWidth(2, 149, 100) == 149);
		assertTrue(ImageUtil.getWidth(2, 200, 300) == 200);
		assertTrue(ImageUtil.getWidth(2, 11, 10) == 11);
	}

	@Test
	public void testGetHeight() {
		assertTrue(ImageUtil.getHeight(1, 1000, 100) == 100);
		assertTrue(ImageUtil.getHeight(1, 100, 1000) == 100);
		assertTrue(ImageUtil.getHeight(1, 100, 99) == 99);
		assertTrue(ImageUtil.getHeight(1, 100, 98) == 98);
		assertTrue(ImageUtil.getHeight(1, 100, 100) == 100);
		assertTrue(ImageUtil.getHeight(1, 99, 100) == 99);
		assertTrue(ImageUtil.getHeight(1, 77, 77) == 77);
		assertTrue(ImageUtil.getHeight(1, 101, 100) == 100);
		assertTrue(ImageUtil.getHeight(1, 149, 100) == 100);
		assertTrue(ImageUtil.getHeight(1, 200, 300) == 200);
		assertTrue(ImageUtil.getHeight(1, 11, 10) == 10);

		assertTrue(ImageUtil.getHeight(0.5, 1000, 100) == 100);
		assertTrue(ImageUtil.getHeight(0.5, 100, 1000) == 200);
		assertTrue(ImageUtil.getHeight(0.5, 100, 99) == 99);
		assertTrue(ImageUtil.getHeight(0.5, 100, 98) == 98);
		assertTrue(ImageUtil.getHeight(0.5, 100, 100) == 100);
		assertTrue(ImageUtil.getHeight(0.5, 99, 100) == 100);
		assertTrue(ImageUtil.getHeight(0.5, 77, 77) == 77);
		assertTrue(ImageUtil.getHeight(0.5, 101, 100) == 100);
		assertTrue(ImageUtil.getHeight(0.5, 149, 100) == 100);
		assertTrue(ImageUtil.getHeight(0.5, 200, 300) == 300);
		assertTrue(ImageUtil.getHeight(0.5, 11, 10) == 10);

		assertTrue(ImageUtil.getHeight(2, 1000, 100) == 100);
		assertTrue(ImageUtil.getHeight(2, 100, 1000) == 50);
		assertTrue(ImageUtil.getHeight(2, 100, 99) == 50);
		assertTrue(ImageUtil.getHeight(2, 100, 50) == 50);
		assertTrue(ImageUtil.getHeight(2, 100, 49) == 49);
		assertTrue(ImageUtil.getHeight(2, 100, 98) == 50);
		assertTrue(ImageUtil.getHeight(2, 100, 100) == 50);
		assertTrue(ImageUtil.getHeight(2, 99, 100) == 49);
		assertTrue(ImageUtil.getHeight(2, 77, 77) == 38);
		assertTrue(ImageUtil.getHeight(2, 101, 100) == 50);
		assertTrue(ImageUtil.getHeight(2, 149, 100) == 74);
		assertTrue(ImageUtil.getHeight(2, 200, 300) == 100);
		assertTrue(ImageUtil.getHeight(2, 11, 10) == 5);
	}
}
