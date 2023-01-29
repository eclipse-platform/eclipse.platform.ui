/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
package org.eclipse.ui.tests.views.properties.tabbed.text;

import java.util.StringTokenizer;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for the title bar for the tabbed property view.
 *
 * @author Anthony Hunter
 */
public class TextTestsLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object obj) {
		return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FILE);
	}

	@Override
	public String getText(Object obj) {
		if (obj instanceof ITextSelection textSelection) {
			if (textSelection.getLength() != 0) {
				StringTokenizer tokenizer = new StringTokenizer(textSelection
						.getText());
				int size = 0;
				while (tokenizer.hasMoreTokens()) {
					size++;
					tokenizer.nextToken();
				}
				if (size == 1) {
					return textSelection.getText();
				}
				return size + " words selected";//$NON-NLS-1$
			}
		}
		return null;
	}
}