package org.eclipse.ui.internal.console;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.ConsolePlugin;

/**
 * This class is new and experimental. It will likely be subject to significant change before
 * it is finalized.
 * 
 * @since 3.1
 *
 */
public class IOConsoleViewer extends TextViewer implements LineStyleListener {
    private boolean autoScroll = true;
    
    public IOConsoleViewer(Composite parent, IDocument document) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        setDocument(document);
        StyledText text = getTextWidget();
        text.setDoubleClickEnabled(true);
        text.setFont(parent.getFont());
        text.addLineStyleListener(this);
        text.setEditable(true);
        
        document.addDocumentListener(new IDocumentListener() {
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
            public void documentChanged(DocumentEvent event) {
                revealEndOfDocument();
            }
            
        });
    }
    
    public boolean isAutoScroll() {
        return autoScroll;
    }	
    
    public void setAutoScroll(boolean scroll) {
        autoScroll = scroll;
    }
    
    public void setTabWidth(int tabWidth) {
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.TextViewer#handleVerifyEvent(org.eclipse.swt.events.VerifyEvent)
     */
    protected void handleVerifyEvent(VerifyEvent e) {
        IDocument doc = getDocument();        
        String[] legalLineDelimiters = doc.getLegalLineDelimiters();
        String text = e.text;
        
        try {
            IOConsolePartition partition = (IOConsolePartition) doc.getPartition(e.start);
            if (!partition.isReadOnly()) {
                boolean isCarriageReturn = false;
                for (int i = 0; i < legalLineDelimiters.length; i++) {
                    if(e.text.equals(legalLineDelimiters[i])) {
                        isCarriageReturn = true;
                        break;
                    }
                }
                
                if (!isCarriageReturn) {
                    super.handleVerifyEvent(e);
                    return;
                } 
            }
            
        } catch (BadLocationException e1) {
        }
        
        int length = doc.getLength();
        if (e.start == length) {
            super.handleVerifyEvent(e);
        } else {
            try {
                doc.replace(length, 0, text);
            } catch (BadLocationException e1) {
            }
            getTextWidget().setCaretOffset(doc.getLength());
            e.doit = false;
        }
    }
    
    protected void revealEndOfDocument() {
        if (autoScroll) {
            IDocument doc = getDocument();
            if (doc == null) {
                return;
            }
            int lines = doc.getNumberOfLines();
            try {
                // lines are 0-based
                int lineStartOffset = doc.getLineOffset(lines - 1);
                StyledText widget= getTextWidget();
                if (lineStartOffset > 0) {
                    widget.setCaretOffset(lineStartOffset);
                    widget.showSelection();
                }
                int lineEndOffset = lineStartOffset + doc.getLineLength(lines - 1);
                if (lineEndOffset > 0) {
                    widget.setCaretOffset(lineEndOffset);
                }
            } catch (BadLocationException e) {
            }
            getTextWidget().setCaretOffset(doc.getLength());
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
     */
    public void lineGetStyle(LineStyleEvent event) {
        IDocument document = getDocument();
        if (document.getLength() > 0){
            IOConsolePartition[] partitions = (IOConsolePartition[]) document.getDocumentPartitioner().computePartitioning(event.lineOffset, event.lineText.length());
            StyleRange[] styles = new StyleRange[partitions.length];        
            for (int i = 0; i < partitions.length; i++) {                
                int rangeStart = Math.max(partitions[i].getOffset(), event.lineOffset);
//                int rangeLength = Math.max(partitions[i].getLength(), event.lineText.length());
                int rangeLength = partitions[i].getLength();
                styles[i] = partitions[i].getStyleRange(rangeStart, rangeLength);
            }
            event.styles = styles;
        }
    }

    public void setWordWrap(final boolean wordWrap) {
        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                getTextWidget().setWordWrap(wordWrap);
                getTextWidget().redraw();       
            }
        });
    }
    
}
