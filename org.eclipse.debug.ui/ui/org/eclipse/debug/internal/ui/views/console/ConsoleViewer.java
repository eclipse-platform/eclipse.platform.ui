package org.eclipse.debug.internal.ui.views.console;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.ConsolePreferencePage;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
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
			IDocument doc= getDocument();
			if (doc == null) {
				getTextWidget().setEditable(false);
				return;
			}
			revealEndOfDocument();
			paintDocument();
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
		super(parent, getSWTStyles());
		
		getTextWidget().setDoubleClickEnabled(true);
		
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		FontData data= ConsolePreferencePage.getConsoleFontData();
		fFont= new Font(getControl().getDisplay(), data);
		getTextWidget().setFont(fFont);
	}
	
	/**
	 * Returns the SWT style flags used when instantiating this viewer
	 */
	private static int getSWTStyles() {
		int styles= SWT.H_SCROLL | SWT.V_SCROLL;
		if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP)) {
			styles= styles | SWT.WRAP;
		}
		return styles;
	}

	/**
	 * Reveals (makes visible) the end of the current document
	 */
	protected void revealEndOfDocument() {
		IDocument doc= getDocument();
		int docLength= doc.getLength();
		if (docLength > 0) {
			StyledText widget= getTextWidget();
			widget.setCaretOffset(docLength);
			widget.showSelection();
		}
	}

	/**
	 * Clears the contents of the current document.
	 */
	public void clearDocument() {
		IDocument doc= getDocument();
		if (doc != null) {
			doc.set("");
		}
		selectionChanged(0, 0);
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
			paintDocument();
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
	 * Only allow text to be typed at the end of the document.
	 * 
	 * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	protected void handleVerifyEvent(VerifyEvent e) {
		IDocument doc= getDocument();
		if (doc != null) {
			IDocumentPartitioner partitioner = doc.getDocumentPartitioner();
			if (partitioner != null) {
				int length = doc.getLength();
				ITypedRegion[] partitions = partitioner.computePartitioning(length, 0);
				if (partitions.length == 0) {
				} else {
					ITypedRegion partition = partitions[partitions.length - 1];
					if (partition.getType().equals(InputPartition.INPUT_PARTITION_TYPE)) {
						// > 1 char in the input buffer
						e.doit = (e.start >= partition.getOffset()) && (e.end <= (partition.getLength() + partition.getOffset()));
					} else {
						// first character in the input buffer
						e.doit = length == e.start;
					}
				}
			}
		}
	}
	
	protected void paintDocument() {
		final IDocumentPartitioner partitioner = getDocument().getDocumentPartitioner();
		if (partitioner != null) {
			Runnable r = new Runnable() {
				public void run() {
					ITypedRegion[] regions = partitioner.computePartitioning(0, getDocument().getLength());
					StyleRange[] styles = new StyleRange[regions.length];
					for (int i = 0; i < regions.length; i++) {
						ColorPartition partition = (ColorPartition)regions[i];
						//System.out.println(partition.getType() + " : " + partition.getOffset() + " : " + partition.getLength());
						styles[i] = new StyleRange(partition.getOffset(), partition.getLength(), partition.getColor(), null);
					}	
					//System.out.println();
					getTextWidget().setStyleRanges(styles);
				}
			};
			getControl().getDisplay().asyncExec(r);
		}
	}
}

