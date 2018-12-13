/*******************************************************************************
 * Copyright (c) 2015, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.testing.dumps;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @since 0.15
 */
public class AwtScreenshot {

	public static void main(String[] args) {
		try {
			System.setProperty("java.awt.headless", "false");
			Robot robot= new Robot();
			Rectangle rect= new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage image= robot.createScreenCapture(rect);
			File file= new File(args[0]);
			ImageIO.write(image, "png", file);

			System.out.println("AWT screenshot saved to: " + file.getAbsolutePath());
		} catch (HeadlessException|AWTException|IOException e) {
			e.printStackTrace();
		}
	}

}
