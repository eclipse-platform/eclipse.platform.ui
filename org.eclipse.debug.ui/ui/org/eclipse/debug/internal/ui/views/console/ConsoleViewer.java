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
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
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
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.console.IConsoleConstants;

public class ConsoleViewer extends TextViewer implements IPropertyChangeListener, MouseTrackListener, MouseMoveListener, MouseListener, PaintListener, LineStyleListener, Listener {
	
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
		
	protected InternalDocumentListener fInternalDocumentListener= new InternalDocumentListener();
	
	/**
	 * Whether the console scrolls as output is appended.
	 */
	private boolean fAutoScroll = true;
	
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
		JFaceResources.getFontRegistry().addListener(this);

		getTextWidget().setFont(JFaceResources.getFont(IConsoleConstants.CONSOLE_FONT));
		getTextWidget().addMouseTrackListener(this);
		getTextWidget().addPaintListener(this);
		getTextWidget().addLineStyleListener(this);
		getTextWidget().addListener(SWT.KeyUp, this);
		getTextWidget().setTabs(DebugUIPlugin.getDefault().getPluginPreferences().getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH));
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
		if (isAutoScroll()) {
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
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR) ||
			propertyName.equals(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR) ||
			propertyName.equals(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR)) {
				getTextWidget().redraw();
		} else if (propertyName.equals(IConsoleConstants.CONSOLE_FONT)) {
			getTextWidget().setFont(JFaceResources.getFont(IConsoleConstants.CONSOLE_FONT));
		} else if (propertyName.equals(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH)) {
			getTextWidget().setTabs(DebugUIPlugin.getDefault().getPluginPreferences().getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH));
		} else if (propertyName.equals(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK)) {
			setAutoScroll(!DebugUIPlugin.getDefault().getPluginPreferences().getBoolean(IInternalDebugUIConstants.PREF_CONSOLE_SCROLL_LOCK));
		}
	}
	
	/**
	 * Dispose this viewer and resources
	 */
	public void dispose() {
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
		JFaceResources.getFontRegistry().removeListener(this);
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
		updateLinks(offset);	
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
			if (doc == null) {
				return;
			}
			ConsoleDocumentPartitioner partitioner = (ConsoleDocumentPartitioner)doc.getDocumentPartitioner();
			if (partitioner == null) {
				return;
			}
			IRegion linkRegion = partitioner.getRegion(fHyperLink);
			if (linkRegion != null) {
				int start = linkRegion.getOffset();
				int end = start + linkRegion.getLength();
				IConsoleColorProvider colorProvider = partitioner.getColorProvider();
				try {
					ITypedRegion partition = doc.getPartition(start);
					Color fontColor = e.gc.getForeground();
					if (partition instanceof StreamPartition) {
						StreamPartition streamPartition = (StreamPartition)partition;
						fontColor = colorProvider.getColor(streamPartition.getStreamIdentifier());
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
	}

	/**
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		if (fHyperLink != null) {
			String selection = getTextWidget().getSelectionText();
			if (selection.length() <= 0) {
				if (e.button == 1) {
					fHyperLink.linkActivated();
				}
			}
		}
	}

	/**
	 * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
	 */
	public void lineGetStyle(LineStyleEvent event) {
		IDocument document = getDocument();
		if (document != null) {
			ConsoleDocumentPartitioner partitioner = (ConsoleDocumentPartitioner)document.getDocumentPartitioner();
			if (partitioner != null) {
				IConsoleColorProvider colorProvider = partitioner.getColorProvider();
				ITypedRegion[] regions = partitioner.computePartitioning(event.lineOffset, event.lineOffset + event.lineText.length());
				StyleRange[] styles = new StyleRange[regions.length];
				for (int i = 0; i < regions.length; i++) {
					StreamPartition partition = (StreamPartition)regions[i];
					Color color = colorProvider.getColor(partition.getStreamIdentifier());
					styles[i] = new StyleRange(partition.getOffset(), partition.getLength(), color, null);
				}	
				event.styles = styles;
			}
		}
	}
	
	/**
	 * Sets whether this viewer should auto-scroll as output is appended to the
	 * document.
	 * 
	 * @param scroll
	 */
	public void setAutoScroll(boolean scroll) {
		fAutoScroll = scroll;
	}
	
	/**
	 * Returns whether this viewer should auto-scroll as output is appended to
	 * the document.
	 */
	public boolean isAutoScroll() {
		return fAutoScroll;
	}	

	/**
	 * On KeyUp events, see if we need to enter/exit a link.
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		int offset = getTextWidget().getCaretOffset();
		updateLinks(offset);
	}
	
	/**
	 * The cursor has just be moved to the given offset, the mouse has
	 * hovered over the given offset. Update link rendering.
	 * 
	 * @param offset
	 */
	protected void updateLinks(int offset) {
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

}

