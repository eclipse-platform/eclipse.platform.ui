package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.ConsolePreferencePage;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugPreferenceConstants;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;

public class ConsoleViewer extends TextViewer implements IPropertyChangeListener{

	/**
	 * Font used in the underlying text widget
	 */
	protected Font fFont;
	
	protected InternalDocumentListener fInternalDocumentListener= new InternalDocumentListener();
	/**
	 * Internal document listener.
	 */
	class InternalDocumentListener implements IDocumentListener {
		/**
		 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent e) {
		}
		
		/**
		 * @see IDocumentListener#documentChanged(DocumentEvent)
		 */
		public void documentChanged(DocumentEvent e) {
			ConsoleDocument doc= (ConsoleDocument) getDocument();
			if (doc == null || doc.isClosed()) {
				getTextWidget().setEditable(false);
				return;
			}
			revealEndOfDocument();
			if (doc.isReadOnly()) {
				getTextWidget().setEditable(false);
			}
			updateStyleRanges(doc);
		}
	}

	/**
	 * Creates a new console viewer and adds verification checking
	 * to only allow text modification if the text is being modified
	 * in the editable portion of the underlying document.
	 *
	 * @see org.eclipse.swt.events.VerifyListener
	 */	
	public ConsoleViewer(Composite parent) {
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		
		getTextWidget().setDoubleClickEnabled(true);
		
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		FontData data= ConsolePreferencePage.getConsoleFontData();
		fFont= new Font(getControl().getDisplay(), data);
		getTextWidget().setFont(fFont);
	}

	/**
	 * Reveals (makes visible) the end of the current document
	 */
	protected void revealEndOfDocument() {
		IDocument doc= getDocument();
		int docLength= doc.getLength();
		if (docLength > 0) {
			revealRange(docLength - 1, 1);
			StyledText widget= getTextWidget();
			widget.setCaretOffset(docLength);

		}
	}

	protected void updateStyleRanges(ConsoleDocument doc) {
		StyleRange[] ranges= doc.getStyleRanges();		
		int lastRangeIndex= ranges.length;
		if (lastRangeIndex > 0) {
			StyledText widget= getTextWidget();
			int storeLength= doc.getStore().getLength();
			StyleRange lastRange= ranges[lastRangeIndex - 1];
			if (!(storeLength - 1 < lastRange.start) &&
				lastRange.start + lastRange.length <= widget.getContent().getCharCount()) {
				widget.setStyleRanges(ranges);
			}
		}
	}

	/**
	 * Clears the contents of the current document.
	 */
	public void clearDocument() {
		IDocument doc= getDocument();
		if (doc != null) {
			((ConsoleDocument) doc).clearDocument();
		}
	}

	/**
	 * @see ITextViewer#setDocument(IDocument)
	 */
	public void setDocument(IDocument doc) {
		ConsoleDocument oldDoc= (ConsoleDocument) getDocument();
		ConsoleDocument document= (ConsoleDocument)doc;
		if (oldDoc == null && document == null) {
			return;
		}
		if (oldDoc != null) {
			oldDoc.removeDocumentListener(fInternalDocumentListener);
			oldDoc.setConsoleViewer(null);
			if (oldDoc.equals(document)) {
				document.addDocumentListener(fInternalDocumentListener);
				document.setConsoleViewer(this);
				return;
			}
		}

		if (document != null) {
			super.setDocument(document);
			getTextWidget().setEditable(!document.isReadOnly());
			updateStyleRanges(document);
			revealEndOfDocument();
			document.addDocumentListener(fInternalDocumentListener);
			document.setConsoleViewer(this);
		} else {
			getTextWidget().setEditable(false);
		}
	}
	
	/**
	 * @see IFindReplaceTarget#canPerformFind()
	 */
	protected boolean canPerformFind() {
		return (getTextWidget() != null && getVisibleDocument() != null && getVisibleDocument().getLength() > 0);
	}	
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (!propertyName.equals(IDebugPreferenceConstants.CONSOLE_FONT)) {
			return;
		}
		FontData data= ConsolePreferencePage.getConsoleFontData();
		Font temp= fFont;
		fFont= new Font(getControl().getDisplay(), data);
		getTextWidget().setFont(fFont);
		temp.dispose();
	}
	
	/**
	 * Dispose this viewer and resources
	 */
	protected void dispose() {
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		fFont.dispose();
	}
	
	/**
	 * @see ITextViewer#isEditable()
	 */
	public boolean isEditable() {
		StyledText widget= getTextWidget();
		int caretPos= widget.getCaretOffset();
		ConsoleDocument doc= (ConsoleDocument) getDocument();
		if (doc != null && doc.getStartOfEditableContent() > caretPos) {
			return false;
		}
		return true;
	}
	
	/**
	 * @see VerifyListener#verifyText(VerifyEvent)
	 */
	protected void handleVerifyEvent(VerifyEvent e) {
		ConsoleDocument doc= (ConsoleDocument) getDocument();
		if (doc != null && doc.getStartOfEditableContent() > e.start) {
			e.doit= false;
		}
	}
}

