/*******************************************************************************
 * Copyright (c) ETAS GmbH 2024, all rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ETAS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.Map;

import org.eclipse.swt.SWT;

import org.eclipse.jface.text.source.ISourceViewer;

/**
 * @since 3.27 This strategy supports surrounding the selected text with similar opening and closing
 *        brackets when the text is selected and an opening bracket is inserted.
 */
public class SurroundWithBracketsStrategy implements IAutoEditStrategy {

	private ISourceViewer sourceViewer;

	@SuppressWarnings("nls")
	private final Map<String, String> bracketsMap= Map.of("(", ")", "[", "]", "{", "}", "<", ">", "\"", "\"", "'", "'", "`", "`");

	public SurroundWithBracketsStrategy(ISourceViewer sourceViewer) {
		this.sourceViewer= sourceViewer;
	}

	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		if (command.text != null && bracketsMap.containsKey(command.text)) {
			try {
				ITextSelection selection= command.fSelection;
				if (selection != null && selection.getLength() > 0) {
					String selectedText= document.get(selection.getOffset(), selection.getLength());
					String closingBracket= bracketsMap.get(command.text);
					command.text= command.text + selectedText + closingBracket;
					command.offset= selection.getOffset();
					command.length= selection.getLength();

					// Set the caret offset after the opening bracket but before the closing bracket
					command.caretOffset= command.offset + command.text.length() - closingBracket.length();
					command.shiftsCaret= false;

					// Run this in a UI thread asynchronously to ensure the selection is updated correctly
					sourceViewer.getTextWidget().getDisplay().asyncExec(() -> sourceViewer.setSelectedRange(command.offset + 1, selectedText.length()));
				}
			} catch (BadLocationException e) {
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
		}
	}
}
