/*******************************************************************************
 * Copyright (c) 2022 Avaloq Group AG (http://www.avaloq.com).
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Andrew Lamb (Avaloq Group AG) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

public class WideDoubleClickStrategy implements ITextDoubleClickStrategy {

	@Override
	public void doubleClicked(ITextViewer viewer) {
		int offset = viewer.getSelectedRange().x;
		IDocument document = viewer.getDocument();
		try {
			IRegion region = document.getLineInformationOfOffset(offset);
			viewer.setSelectedRange(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			// do nothing
		}
	}

}
