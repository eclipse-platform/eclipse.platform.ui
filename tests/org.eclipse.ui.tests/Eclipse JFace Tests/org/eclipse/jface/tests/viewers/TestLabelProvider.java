/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TestLabelProvider extends LabelProvider {

	static Image fgImage = null;

	/**
	 *
	 */
	public static Image getImage() {
		if (fgImage == null) {
			fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class,
					"images/java.gif").createImage();
		}
		return fgImage;
	}

	@Override
	public Image getImage(Object element) {
		return getImage();
	}

	@Override
	public String getText(Object element) {
		String label = element.toString();
		return label + " <rendered>";
	}
}
