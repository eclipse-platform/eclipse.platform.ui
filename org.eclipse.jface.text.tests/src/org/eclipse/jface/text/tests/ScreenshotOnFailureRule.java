/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.rules.TestWatcher;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

final class ScreenshotOnFailureRule extends TestWatcher {
	@Override
	protected void failed(Throwable e, org.junit.runner.Description description) {
		Robot robot;
		try {
			robot= new Robot();
			String format = "jpg";
			String fileName = Platform.getLogFileLocation().removeLastSegments(2).toString() + '/' + description.getClassName() + '.' + description.getMethodName() + ".failure." + format;
			Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
			ImageIO.write(screenFullImage, format, new File(fileName));
			super.failed(e, description);
		} catch (Exception ex) {
			Platform.getLog(Platform.getBundle("org.eclipse.jface.text.tests")).log(new Status(IStatus.ERROR, "org.eclipse.jface.text.tests", ex.getMessage(), ex));
		}
	}
}