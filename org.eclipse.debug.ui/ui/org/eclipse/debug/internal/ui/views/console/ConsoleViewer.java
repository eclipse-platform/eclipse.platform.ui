package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.ConsolePreferencePage;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class ConsoleViewer extends TextViewer implements IPropertyChangeListener, MouseTrackListener, MouseMoveListener, PaintListener {

	/**
	 * Font used in the underlying text widget
	 */
	protected Font fFont;
	
	/**
	 * The active hyperlink, or <code>null</code>
	 */
	private IConsoleHyperLink fHyperLink = null;
	
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
			ConsoleDocument doc= (ConsoleDocument)getDocument();
			if (doc == null) {
				getTextWidget().setEditable(false);
				return;
			}
			getTextWidget().setEditable(!doc.isReadOnly());
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
		getTextWidget().addMouseTrackListener(this);
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
			doc.set(""); //$NON-NLS-1$
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
		if (propertyName.equals(IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB) ||
			propertyName.equals(IDebugPreferenceConstants.CONSOLE_SYS_OUT_RGB) ||
			propertyName.equals(IDebugPreferenceConstants.CONSOLE_SYS_ERR_RGB)) {
				paintDocument();
			}
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
		if (getTextWidget() != null) {
			getTextWidget().removeMouseTrackListener(this);
		}
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		fFont.dispose();
	}
	
	/**
	 * Only allow text to be typed at the end of the document.
	 * 
	 * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
	 */
	protected void handleVerifyEvent(VerifyEvent e) {
		ConsoleDocument doc= (ConsoleDocument)getDocument();
		if (doc != null) {
			if (doc.isReadOnly()) {
				e.doit = false;
				return;
			}
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
		final ConsoleDocumentPartitioner partitioner = (ConsoleDocumentPartitioner)getDocument().getDocumentPartitioner();
		if (partitioner != null) {
			Runnable r = new Runnable() {
				public void run() {
					IConsoleDocumentContentProvider contentProvider = partitioner.getContentProvider();
					ITypedRegion[] regions = partitioner.computePartitioning(0, getDocument().getLength());
					StyleRange[] styles = new StyleRange[regions.length];
					for (int i = 0; i < regions.length; i++) {
						StreamPartition partition = (StreamPartition)regions[i];
						Color color = contentProvider.getColor(partition.getStreamIdentifier());
						//System.out.println(partition.getType() + " : " + partition.getOffset() + " : " + partition.getLength());
						styles[i] = new StyleRange(partition.getOffset(), partition.getLength(), color, null);
					}	
					//System.out.println();
					getTextWidget().setStyleRanges(styles);
				}
			};
			getControl().getDisplay().asyncExec(r);
		}
	}

	/**
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseEnter(MouseEvent e) {
		getTextWidget().addMouseMoveListener(this);
	}

	/**
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseExit(MouseEvent e) {
		getTextWidget().removeMouseMoveListener(this);
	}

	/**
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseHover(MouseEvent e) {
	}

	/**
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent e) {
		int offset = -1;
		try {
			offset = getTextWidget().getOffsetAtLocation(new Point(e.x, e.y));
		} catch (IllegalArgumentException ex) {
			return;
		}
		if (offset >= 0) {
			Position[] positions = null;
			try {
				positions = getDocument().getPositions(HyperLinkPosition.HYPER_LINK_CATEGORY);
			} catch (BadPositionCategoryException ex) {
				// internal error
				DebugUIPlugin.log(ex);
				return;
			}
			for (int i = 0; i < positions.length; i++) {
				Position position = positions[i];
				if (offset >= position.getOffset() && position.getOffset() <= (position.getOffset() + position.getLength())) {
					IConsoleHyperLink link = ((HyperLinkPosition)position).getHyperLink();
					System.out.println(position.getOffset() + ":" + position.getLength());
					if (!link.equals(fHyperLink)) {
						linkEntered(link);
						return;
					}
				}
			}
			if (fHyperLink != null) {
				linkExited(fHyperLink);
			}
		}
	}


	protected void linkEntered(IConsoleHyperLink link) {
		if (fHyperLink != null) {
			linkExited(fHyperLink);
		}
		fHyperLink = link;
		fHyperLink.linkEntered();
	}
	
	protected void linkExited(IConsoleHyperLink link) {
		link.linkExited();
		fHyperLink = null;
	}
	/**
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent e) {
		if (fHyperLink != null) {
			int start = fHyperLink.getOffset();
			int end = start + fHyperLink.getLength();
			Point p1 = getTextWidget().getLocationAtOffset(start);
			Point p2 = getTextWidget().getLocationAtOffset(end);
			e.gc.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

}

