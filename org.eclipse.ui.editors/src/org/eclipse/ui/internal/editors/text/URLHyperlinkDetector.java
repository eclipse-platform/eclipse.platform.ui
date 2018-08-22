/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.text;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;


/**
 * URL hyperlink detector.
 *
 * @since 3.1
 */
public class URLHyperlinkDetector extends org.eclipse.jface.text.hyperlink.URLHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		IHyperlink[] result= super.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
		if (result == null)
			return null;

		for (int i= 0; i < result.length; i++) {
			org.eclipse.jface.text.hyperlink.URLHyperlink hyperlink= (org.eclipse.jface.text.hyperlink.URLHyperlink)result[i];
			result[i]= new URLHyperlink(hyperlink.getHyperlinkRegion(), hyperlink.getURLString());
		}

		return result;
	}
}
