/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.ui.model.application.MPart;

public class HeadlessPhotoDemoTest extends HeadlessStartupTest {

	@Override
	protected String getAppURI() {
		return "org.eclipse.e4.ui.tests/xmi/photo.xmi";
	}

	@Override
	protected MPart getFirstPart() {
		return (MPart) findElement("ExifView");
	}

	@Override
	protected MPart getSecondPart() {
		return (MPart) findElement("ThumbnailsView");
	}

}
