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
package org.eclipse.ui.internal.console;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;

/**
 * Adapts an IOConsole's document to the viewer StyledText widget. Allows proper line
 * wrapping of fixed width consoles without having to add line delimeters to the StyledText.
 * 
 * By using this adapter, the offset of any character is the same in both the widget and the
 * document.
 * 
 * @since 3.1
 */
public class IOConsoleDocumentAdapter implements IDocumentAdapter, IDocumentListener {
    private static final String DELIMITER = System.getProperty("line.separator"); //$NON-NLS-1$;
    
    private int consoleWidth = -1;
    private ArrayList textChangeListeners;
    private IDocument document;
    /*
     * A list of Strings, every line in the Text Widget represents one String in this list.
     */
    private ArrayList lines;
    
    public IOConsoleDocumentAdapter(IDocument doc, int width) {
        textChangeListeners = new ArrayList();
        consoleWidth = width;
        document = doc;
        document.addDocumentListener(this);
        lines = new ArrayList();
        repairLines(0);
    }
    
    /*
     * repairs lines list from the beginning of the line containing the offset of any 
     * DocumentEvent, to the end of the Document.
     */
    private void repairLines(int eventOffset) {
        try {
            int docLine = document.getLineOfOffset(eventOffset);
            int docLineStart = document.getLineOffset(docLine);
            int textLine = getLineAtOffset(docLineStart);
            
            for (int i=lines.size()-1; i>=textLine; i--) {
                lines.remove(i);
            }
            
            int numLinesInDoc = document.getNumberOfLines();
            String line = null;
            for (int i = docLine; i<numLinesInDoc; i++) {
                int offset = document.getLineOffset(i);
                int length = document.getLineLength(i);
                
                if (length == 0) {
                    line = ""; //$NON-NLS-1$
                    lines.add(line);
                } else {
                    while (length > 0) {
                        int wrappedLength = consoleWidth > 0 ? Math.min(consoleWidth, length) : length;
                        line = document.get(offset, wrappedLength);
                        lines.add(line);
                        offset += wrappedLength;
                        length -= wrappedLength;
                    }
                }
            }
            if (line != null && line.endsWith(DELIMITER)) {
                lines.add(""); //$NON-NLS-1$
            }
            
        } catch (BadLocationException e) {
        }
        
        if (lines.size() == 0) {
            lines.add(""); //$NON-NLS-1$
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentAdapter#setDocument(org.eclipse.jface.text.IDocument)
     */
    public void setDocument(IDocument doc) {
        document = doc;
        if (document != null) {
            repairLines(0);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#addTextChangeListener(org.eclipse.swt.custom.TextChangeListener)
     */
    public synchronized void addTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		if (!textChangeListeners.contains(listener)) {
			textChangeListeners.add(listener);
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#removeTextChangeListener(org.eclipse.swt.custom.TextChangeListener)
     */
    public synchronized void removeTextChangeListener(TextChangeListener listener) {
        if(textChangeListeners != null) {
            Assert.isNotNull(listener);
            textChangeListeners.remove(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getCharCount()
     */
    public int getCharCount() {
        return document.getLength();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLine(int)
     */
    public String getLine(int lineIndex) {
        return (String) lines.get(lineIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLineAtOffset(int)
     */
    public int getLineAtOffset(int offset) {
        if (offset == 0) {
            return 0;
        }
        //offset can be greater than length when user is deleting.
        if (offset >= document.getLength()) {
            int size = lines.size();
            return size > 0 ? size-1 : 0;
        }
        
        int len = 0;
        int line = 0;
        for (Iterator i = lines.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            len += s.length();
            if (len > offset) {
                return line;
            }
            line++;
        }
        return lines.size() - 1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLineCount()
     */
    public int getLineCount() {
        return lines.size();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getLineDelimiter()
     */
    public String getLineDelimiter() {
        return DELIMITER;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getOffsetAtLine(int)
     */
    public int getOffsetAtLine(int lineIndex) {
        int offset = 0;
        for (int i = 0; i< lineIndex; i++) {
            String s = (String) lines.get(i);
            offset += s.length();
        }
        return offset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#getTextRange(int, int)
     */
    public String getTextRange(int start, int length) {
        try {
            return document.get(start, length);
        } catch (BadLocationException e) {
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#replaceTextRange(int, int, java.lang.String)
     */
    public void replaceTextRange(int start, int replaceLength, String text) {
        try {
            document.replace(start, replaceLength, text);
        } catch (BadLocationException e) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.StyledTextContent#setText(java.lang.String)
     */
    public synchronized void setText(String text) {
        TextChangedEvent changeEvent = new TextChangedEvent(this);
        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textSet(changeEvent);
        }    
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public synchronized void documentAboutToBeChanged(DocumentEvent event) {
        TextChangingEvent changeEvent = new TextChangingEvent(this);
        changeEvent.start = event.fOffset;
        changeEvent.newText = (event.fText == null ? "" : event.fText); //$NON-NLS-1$
        changeEvent.replaceCharCount = event.fLength;
        changeEvent.newCharCount = (event.fText == null ? 0 : event.fText.length());
        
        int first = getLineAtOffset(event.fOffset);
        int last = getLineAtOffset(event.fOffset + event.fLength);
        changeEvent.replaceLineCount= last - first;
        
        int numLines = lines.size();
        String lastLine = (String)lines.get(numLines-1);
        changeEvent.newLineCount = countLines(lastLine + event.fText);
        
        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textChanging(changeEvent);
        }
    }

    /**
     * Counts the number of lines the viewer's text widget will need to use to 
     * display the String
     * @return The number of lines necessary to display the string in the viewer.
     */
    private int countLines(String string) {
        if (string.equals(DELIMITER)) {
            return 1;
        }
        int delimLength = DELIMITER.length();
        int index = string.indexOf(DELIMITER);
        if (index == -1) {
            return consoleWidth > 0 ? string.length() / consoleWidth : 1;
        } else if (index == string.length()-delimLength) {
            int count = 0;
            String firstLine = string.substring(0, index);
            String rest = string.substring(index);
            count += countLines(firstLine);
            count += countLines(rest);
            return count;
        }else {
            int count = 0;
            String firstLine = string.substring(0, index + delimLength);
            String rest = string.substring(index + delimLength);
            count += countLines(firstLine);
            count += countLines(rest);
            return count;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public synchronized void documentChanged(DocumentEvent event) {
        repairLines(event.fOffset);
        
        TextChangedEvent changeEvent = new TextChangedEvent(this);

        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textChanged(changeEvent);
        }
    }

    /**
     * sets consoleWidth, repairs line information, then fires event to the viewer text widget.
     * @param width The console's width
     */
    public void setWidth(int width) {
        consoleWidth = width;
        repairLines(0);
        TextChangedEvent changeEvent = new TextChangedEvent(this);
        for (Iterator iter = textChangeListeners.iterator(); iter.hasNext();) {
            TextChangeListener element = (TextChangeListener) iter.next();
            element.textSet(changeEvent);
        }
    }


    public void dispose() {
        textChangeListeners = null;
        document = null;
        lines = null;
    }
}
