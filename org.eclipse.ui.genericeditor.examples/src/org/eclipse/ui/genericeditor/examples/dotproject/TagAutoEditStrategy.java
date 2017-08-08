/*******************************************************************************
 * Copyright (c) 2017 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class TagAutoEditStrategy implements IAutoEditStrategy {

	public TagAutoEditStrategy() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		if (!">".equals(command.text)) { //$NON-NLS-1$
			return;
		}
		try {
			IRegion region = document.getLineInformationOfOffset(command.offset);
			String line = document.get(region.getOffset(), command.offset - region.getOffset());
			int index = line.lastIndexOf('<');
			if (index != -1 && (index != line.length() - 1) && line.charAt(index + 1) != '/') {
				String tag = line.substring(index + 1);
				command.text += "</" + tag + command.text; //$NON-NLS-1$
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
