package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ConsoleViewer extends TextViewer implements IPropertyChangeListener, MouseTrackListener, MouseMoveListener, MouseListener, PaintListener {

	/**
	 * Font used in the underlying text widget
	 */
	protected Font fFont;
	
	/**
	 * Hand cursor
	 */
	private Cursor fHandCursor;
	
	/**
	 * Text cursor
	 */
	private Cursor fTextCursor;
	
	/**
	 * The active hyperlink, or <code>null</code>
	 */
	private IConsoleHyperlink fHyperLink = null;
	
	/**
	 * Whether this viewer is visible. The console viewer only paints in
	 * response to document changes when it is visible.
	 */
	private boolean fVisible = false;
	
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
			if (isVisible()) {
				revealEndOfDocument();
				paintDocument();
			}
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
		IPreferenceStore pstore= DebugUIPlugin.getDefault().getPreferenceStore();
		FontData data= PreferenceConverter.getFontData(pstore, IDebugPreferenceConstants.CONSOLE_FONT);
		fFont= new Font(getControl().getDisplay(), data);
		getTextWidget().setFont(fFont);
		getTextWidget().addMouseTrackListener(this);
		getTextWidget().addPaintListener(this);
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
		IPreferenceStore pstore= DebugUIPlugin.getDefault().getPreferenceStore();
		FontData fontData= PreferenceConverter.getFontData(pstore, IDebugPreferenceConstants.CONSOLE_FONT);
		Font temp= fFont;
		fFont= new Font(getControl().getDisplay(), fontData);
		getTextWidget().setFont(fFont);
		temp.dispose();
	}
	
	/**
	 * Dispose this viewer and resources
	 */
	protected void dispose() {
		Control control = getTextWidget();
		if (control != null) {
			control.removeMouseTrackListener(this);
			control.removePaintListener(this);
		}
		if (fHandCursor != null) {
			fHandCursor.dispose();
		}
		if (fTextCursor != null) {
			fTextCursor.dispose();
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
		IDocument document = getDocument();
		if (document != null) {
			final ConsoleDocumentPartitioner partitioner = (ConsoleDocumentPartitioner)document.getDocumentPartitioner();
			if (partitioner != null) {
				Runnable r = new Runnable() {
					public void run() {
						IConsoleColorProvider contentProvider = partitioner.getContentProvider();
						// bug 28227
						IDocument doc = getDocument();
						if (doc != null) {
							ITypedRegion[] regions = partitioner.computePartitioning(0, getDocument().getLength());
							StyleRange[] styles = new StyleRange[regions.length];
							for (int i = 0; i < regions.length; i++) {
								StreamPartition partition = (StreamPartition)regions[i];
								Color color = contentProvider.getColor(partition.getStreamIdentifier());
								styles[i] = new StyleRange(partition.getOffset(), partition.getLength(), color, null);
							}	
							try {
								getTextWidget().setStyleRanges(styles);
							} catch (IllegalArgumentException e) {
								// style ranges are out of bounds - likely an old/changed document.
							}
						}
					}
				};
				getControl().getDisplay().asyncExec(r);
			}
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
		if (fHyperLink != null) {
			linkExited(fHyperLink);
		}
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
			Point p = new Point(e.x, e.y);
			offset = getTextWidget().getOffsetAtLocation(p);
		} catch (IllegalArgumentException ex) {
			// out of the document range
		}
		if (offset >= 0) {
			IConsoleHyperlink link = getHyperlink(offset);
			if (link != null) {
				if (link.equals(fHyperLink)) {
					return;
				} else {
					linkEntered(link);
					return;
				}
			}
		}
		if (fHyperLink != null) {
			linkExited(fHyperLink);
		}		
	}

	public IConsoleHyperlink getHyperlink(int offset) {
		if (offset >= 0 && getDocument() != null) {
			Position[] positions = null;
			try {
				positions = getDocument().getPositions(HyperlinkPosition.HYPER_LINK_CATEGORY);
			} catch (BadPositionCategoryException ex) {
				// no links have been added
				return null;
			}
			for (int i = 0; i < positions.length; i++) {
				Position position = positions[i];
				if (offset >= position.getOffset() && offset <= (position.getOffset() + position.getLength())) {
					return ((HyperlinkPosition)position).getHyperLink();
				}
			}
		}
		return null;
	}

	protected void linkEntered(IConsoleHyperlink link) {
		Control control = getTextWidget();
		control.setRedraw(false);
		if (fHyperLink != null) {
			linkExited(fHyperLink);
		}
		fHyperLink = link;
		fHyperLink.linkEntered();
		control.setCursor(getHandCursor());
		control.setRedraw(true);
		control.redraw();
		control.addMouseListener(this);
	}
	
	protected void linkExited(IConsoleHyperlink link) {
		link.linkExited();
		fHyperLink = null;
		Control control = getTextWidget();
		control.setCursor(getTextCursor());
		control.redraw();
		control.removeMouseListener(this);
	}
	/**
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent e) {
		if (fHyperLink != null) {
			IDocument doc = getDocument();
			ConsoleDocumentPartitioner partitioner = (ConsoleDocumentPartitioner)getDocument().getDocumentPartitioner();
			IRegion linkRegion = partitioner.getRegion(fHyperLink);
			if (linkRegion != null) {
				int start = linkRegion.getOffset();
				int end = start + linkRegion.getLength();
				IConsoleColorProvider contentProvider = partitioner.getContentProvider();
				try {
					ITypedRegion partition = doc.getPartition(start);
					Color fontColor = e.gc.getForeground();
					if (partition instanceof StreamPartition) {
						StreamPartition streamPartition = (StreamPartition)partition;
						fontColor = contentProvider.getColor(streamPartition.getStreamIdentifier());
					}
					int startLine = doc.getLineOfOffset(start);
					int endLine = doc.getLineOfOffset(end);
					for (int i = startLine; i <= endLine; i++) {
						IRegion lineRegion = doc.getLineInformation(i);
						int lineStart = lineRegion.getOffset();
						int lineEnd = lineStart + lineRegion.getLength();
						Color color = e.gc.getForeground();
						e.gc.setForeground(fontColor);
						if (lineStart < end) {
							lineStart = Math.max(start, lineStart);
							lineEnd = Math.min(end, lineEnd);
							Point p1 = getTextWidget().getLocationAtOffset(lineStart);
							Point p2 = getTextWidget().getLocationAtOffset(lineEnd);
							FontMetrics metrics = e.gc.getFontMetrics();
							int height = metrics.getHeight();
							e.gc.drawLine(p1.x, p1.y + height, p2.x, p2.y + height);
						}
						e.gc.setForeground(color);
					}
				} catch (BadLocationException ex) {
				}
			}
		}
	}
	
	protected Cursor getHandCursor() {
		if (fHandCursor == null) {
			fHandCursor = new Cursor(DebugUIPlugin.getStandardDisplay(), SWT.CURSOR_HAND);
		}
		return fHandCursor;
	}
	
	protected Cursor getTextCursor() {
		if (fTextCursor == null) {
			fTextCursor = new Cursor(DebugUIPlugin.getStandardDisplay(), SWT.CURSOR_IBEAM);
		}
		return fTextCursor;
	}	

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		if (fHyperLink != null) {
			if (e.button == 1) {
				fHyperLink.linkActivated();
			}
		}
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
	}

	protected void setVisible(boolean visible) {
		fVisible = visible;
		if (visible) {
			paintDocument();
		}
	}
	
	protected boolean isVisible() {
		return fVisible;
	}
}

