/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.ui.internal.editor.support;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.eclipse.ant.ui.internal.editor.AntEditorCompletionProcessor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.w3c.dom.Element;

public class TestTextCompletionProcessor extends AntEditorCompletionProcessor {

	private File editedFile;

    public ICompletionProposal[] getAttributeProposals(String aTaskName, String aPrefix) {
    	cursorPosition= aTaskName.length();
        return super.getAttributeProposals(aTaskName, aPrefix);
    }

    public Element findChildElementNamedOf(Element anElement, String aChildElementName) {
        return super.findChildElementNamedOf(anElement, aChildElementName);
    }

    public ICompletionProposal[] getTaskProposals(String aWholeDocumentString, Element aParentTaskElement, String aPrefix) {
    	cursorPosition= Math.max(0, aWholeDocumentString.length() - 1);
        return super.getTaskProposals(aWholeDocumentString, aParentTaskElement, aPrefix);
    }

    public int determineProposalMode(String aWholeDocumentString, int aCursorPosition, String aPrefix) {
        return super.determineProposalMode(
            aWholeDocumentString,
            aCursorPosition,
            aPrefix);
    }

    public Element findParentElement(
        String aWholeDocumentString,
        int aLineNumber,
        int aColumnNumber) {
        return super.findParentElement(
            aWholeDocumentString,
            aLineNumber,
            aColumnNumber);
    }

    public String getPrefixFromDocument(
        String aDocumentText,
        int anOffset) {
        return super.getPrefixFromDocument(aDocumentText, anOffset);
    }

    public ICompletionProposal[] getPropertyProposals(
        String aDocumentText,
        String aPrefix, int aCursorPosition) {
        return super.getPropertyProposals(aDocumentText, aPrefix, aCursorPosition);
    }

    /**
     * Returns the edited File that org.eclipse.ant.ui.internal.editor.AntEditorCompletionProcessor sets or a temporary 
     * file, which only serves as a dummy.
     * @see org.eclipse.ant.ui.internal.editor.AntEditorCompletionProcessor#getEditedFile()
     */
	public File getEditedFile() {
		if (editedFile != null){
			return editedFile;
		}
		File tempFile = null;
        try {
            tempFile = File.createTempFile("test", null);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

	public void setLineNumber(int aLineNumber) {
    	lineNumber = aLineNumber;
    }

	public void setColumnNumber(int aColumnNumber) {
    	columnNumber = aColumnNumber;
    }
    
	public void setEditedFile(File aFile) {
		editedFile= aFile;
	}
}