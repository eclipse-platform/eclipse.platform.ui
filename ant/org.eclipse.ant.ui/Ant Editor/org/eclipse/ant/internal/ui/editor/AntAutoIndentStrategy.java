/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ant.internal.ui.editor;
import org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormatter;
import org.eclipse.ant.internal.ui.editor.model.AntElementNode;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultAutoIndentStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

/**
 * Auto indent strategy for Ant build files
 */
public class AntAutoIndentStrategy extends DefaultAutoIndentStrategy {
	
	private AntModel fModel;
	private int fAccumulatedChange= 0;
	
	public AntAutoIndentStrategy(AntModel model) {
		fModel= model;
	}
	
	/**
	 * Sets the indentation based on the Ant element node that contains the offset
	 * of the document command.
	 *
	 * @param d the document to work on
	 * @param c the command to deal with
	 */
	private synchronized void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {
		
		if (c.offset == -1 || d.getLength() == 0 || fModel.getProjectNode(false) == null) {
			return;
		}
		
		int position= (c.offset == d.getLength() ? c.offset  - 1 : c.offset);
		AntElementNode node= fModel.getProjectNode(false).getNode(position - fAccumulatedChange);
		if (node == null) {
			return;
		}
		StringBuffer buf= new StringBuffer(c.text);
		buf.append(XmlDocumentFormatter.getLeadingWhitespace(node.getOffset(), d));
		if (!nextNodeIsEndTag(c.offset, d)) {
			buf.append(XmlDocumentFormatter.createIndent());
		}
		
		fAccumulatedChange+= buf.length();
		c.text= buf.toString();
	}
	
	private boolean nextNodeIsEndTag(int offset, IDocument document) {
		if (offset + 1 > document.getLength()) {
			return false;
		}
		try {
			String nextChars= document.get(offset, 2);
			if ("</".equals(nextChars) || "/>".equals(nextChars)) { //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}
		} catch (BadLocationException e) {
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
			autoIndentAfterNewLine(d, c);
		}
	}
	
	public synchronized void reconciled() {
		fAccumulatedChange= 0;
	}
}