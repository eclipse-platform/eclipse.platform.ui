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
import org.eclipse.ant.internal.ui.editor.model.AntElementNode;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultAutoIndentStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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
		buf.append(getLeadingWhitespace(node.getOffset(), d));
		if (!nextNodeIsEndTag(c.offset, d)) {
			buf.append(createIndent());
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
	
	/**
	 * Creates a string that represents one indent (can be
	 * spaces or tabs..)
	 * 
	 * @return one indentation
	 */
	private StringBuffer createIndent() {
		StringBuffer oneIndent= new StringBuffer();
		IPreferenceStore prefs= AntUIPlugin.getDefault().getPreferenceStore();
		prefs.getBoolean(AntEditorPreferenceConstants.FORMATTER_TAB_CHAR);
		
		if (!prefs.getBoolean(AntEditorPreferenceConstants.FORMATTER_TAB_CHAR)) {
			int tabLen= prefs.getInt(AntEditorPreferenceConstants.FORMATTER_TAB_SIZE);
			for (int i= 0; i < tabLen; i++) {
				oneIndent.append(' ');
			}
		} else {
			oneIndent.append('\t'); // default
		}
		
		return oneIndent;
	}
	
	/**
	 * Returns the indentation of the line at <code>offset</code> as a
	 * <code>StringBuffer</code>. If the offset is not valid, the empty string
	 * is returned.
	 * 
	 * @param offset the offset in the document
	 * @return the indentation (leading whitespace) of the line in which
	 * 		   <code>offset</code> is located
	 */
	private StringBuffer getLeadingWhitespace(int offset, IDocument document) {
		StringBuffer indent= new StringBuffer();
		try {
			IRegion line= document.getLineInformationOfOffset(offset);
			int lineOffset= line.getOffset();
			int nonWS= findEndOfWhiteSpace(document, lineOffset, lineOffset + line.getLength());
			indent.append(document.get(lineOffset, nonWS - lineOffset));
			return indent;
		} catch (BadLocationException e) {
			return indent;
		}
	}
	
	public synchronized void reconciled() {
		fAccumulatedChange= 0;
	}
}