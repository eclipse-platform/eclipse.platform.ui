/*******************************************************************************
* Copyright (c) 2025 SAP SE
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
******************************************************************************/
package org.eclipse.jface.text.codemining;

import java.util.function.Consumer;

import org.eclipse.swt.events.MouseEvent;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * A code mining rendered at the start of the line, located at the very end of the document.
 *
 * @since 3.27
 */
public class DocumentFooterCodeMining extends LineHeaderCodeMining {

	public DocumentFooterCodeMining(IDocument document, ICodeMiningProvider provider, Consumer<MouseEvent> action) throws BadLocationException {
		super(new Position(document.getLength(), 0), provider, action);
	}
}
