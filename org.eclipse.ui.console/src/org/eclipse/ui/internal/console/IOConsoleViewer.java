/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsoleViewer;

/**
 * Viewer used to display an IOConsole
 * 
 * @since 3.1
 */
public class IOConsoleViewer extends TextConsoleViewer {
    /**
     * will always scroll with output if value is true.
     */
    private boolean fAutoScroll = true;

    private IDocumentListener fDocumentListener;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.TextViewer#handleDispose()
     */
    protected void handleDispose() {
        IDocument document = getDocument();
        if (document != null) {
            document.removeDocumentListener(fDocumentListener);
        }
        super.handleDispose();
    }

    public IOConsoleViewer(Composite parent, TextConsole console) {
        super(parent, console);
        fDocumentListener = new IDocumentListener() {
            public void documentAboutToBeChanged(DocumentEvent event) {
            }

            public void documentChanged(DocumentEvent event) {
                if (fAutoScroll) {
                    revealEndOfDocument();
                }
            }
        };
        getDocument().addDocumentListener(fDocumentListener);
    }

    public boolean isAutoScroll() {
        return fAutoScroll;
    }

    public void setAutoScroll(boolean scroll) {
        fAutoScroll = scroll;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.TextViewer#handleVerifyEvent(org.eclipse.swt.events.VerifyEvent)
     */
    protected void handleVerifyEvent(VerifyEvent e) {
        IDocument doc = getDocument();
        String[] legalLineDelimiters = doc.getLegalLineDelimiters();
        String eventString = e.text;

        IConsoleDocumentPartitioner partitioner = (IConsoleDocumentPartitioner) doc.getDocumentPartitioner();
        if (!partitioner.isReadOnly(e.start)) {
            boolean isCarriageReturn = false;
            for (int i = 0; i < legalLineDelimiters.length; i++) {
                if (e.text.equals(legalLineDelimiters[i])) {
                    isCarriageReturn = true;
                    break;
                }
            }

            if (!isCarriageReturn) {
                super.handleVerifyEvent(e);
                return;
            }
        }

        int length = doc.getLength();
        if (e.start == length) {
            super.handleVerifyEvent(e);
        } else {
            try {
                doc.replace(length, 0, eventString);
            } catch (BadLocationException e1) {
            }
            e.doit = false;
        }
    }

    /**
     * makes the associated text widget uneditable.
     */
    public void setReadOnly() {
        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                StyledText text = getTextWidget();
                if (text != null) {
                    text.setEditable(false);
                }
            }
        });
    }

    /**
     * @return false if text is editable
     */
    public boolean isReadOnly() {
        return !getTextWidget().getEditable();
    }
}
