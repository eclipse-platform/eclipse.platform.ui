/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.console;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

public class MessageConsoleViewer extends TextViewer implements LineStyleListener {
	
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
			revealEndOfDocument();
		}
	}
	
	/**
	 * Creates a new console viewer and adds verification checking
	 * to only allow text modification if the text is being modified
	 * in the editable portion of the underlying document.
	 *
	 * @see org.eclipse.swt.events.VerifyListener
	 */	
	public MessageConsoleViewer(Composite parent) {
		super(parent, getSWTStyles());
		getTextWidget().setDoubleClickEnabled(true);
		getTextWidget().setFont(parent.getFont());
		getTextWidget().addLineStyleListener(this);
		getTextWidget().setEditable(false);
	}
	
	/**
	 * Returns the SWT style flags used when instantiating this viewer
	 */
	private static int getSWTStyles() {
		int styles= SWT.H_SCROLL | SWT.V_SCROLL;
		return styles;
	}

	/**
	 * Reveals (makes visible) the end of the current document
	 */
	protected void revealEndOfDocument() {
		IDocument doc = getDocument();
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
	}

	/**
	 * @see ITextViewer#setDocument(IDocument)
	 */
	public void setDocument(IDocument doc) {
		IDocument oldDoc= getDocument();
		IDocument document= doc;
		if (oldDoc == null && document == null) {
			return;
		}
		if (oldDoc != null) {
			oldDoc.removeDocumentListener(fInternalDocumentListener);
			if (oldDoc.equals(document)) {
				document.addDocumentListener(fInternalDocumentListener);
				return;
			}
		}

		super.setDocument(document);
		if (document != null) {
			revealEndOfDocument();
			document.addDocumentListener(fInternalDocumentListener);
		}
	}
	
	/**
	 * @see IFindReplaceTarget#canPerformFind()
	 */
	protected boolean canPerformFind() {
		return (getTextWidget() != null && getVisibleDocument() != null && getVisibleDocument().getLength() > 0);
	}	
		
	/**
	 * Dispose this viewer and resources
	 */
	public void dispose() {
	}
	
	/**
	 * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
	 */
	public void lineGetStyle(LineStyleEvent event) {
		IDocument document = getDocument();
		if (document != null) {
			MessageConsolePartitioner partitioner = (MessageConsolePartitioner)document.getDocumentPartitioner();
			if (partitioner != null) {
				ITypedRegion[] regions = partitioner.computePartitioning(event.lineOffset, event.lineOffset + event.lineText.length());
				StyleRange[] styles = new StyleRange[regions.length];
				for (int i = 0; i < regions.length; i++) {
					MessageConsolePartition partition = (MessageConsolePartition)regions[i];
					Color color = partition.getStream().getColor();
					styles[i] = new StyleRange(partition.getOffset(), partition.getLength(), color, null);
				}	
				event.styles = styles;
			}
		}
	}
	
}

