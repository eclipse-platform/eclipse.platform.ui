/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

public class UIPhotoDemoTest extends UIStartupTest {

	@Override
	protected String getURI() {
		return "org.eclipse.e4.ui.tests/xmi/photo.e4xmi";
	}

	@Override
	protected MPart getFirstPart() {
		return (MPart) findElement("ThumbnailsView");
	}

	@Override
	protected MPart getSecondPart() {
		return (MPart) findElement("ExifView");
	}

}
