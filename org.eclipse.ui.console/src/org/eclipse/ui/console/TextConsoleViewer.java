/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.console.ConsoleDocumentAdapter;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;

/**
 * Viewer used to display a TextConsole
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.1
 */
public class TextConsoleViewer extends TextViewer implements LineStyleListener, LineBackgroundListener, MouseTrackListener, MouseMoveListener, MouseListener, PaintListener {
    /**
     * Adapts document to the text widget.
     */
    private ConsoleDocumentAdapter documentAdapter;

    private IHyperlink hyperlink;

    private Cursor handCursor;

    private Cursor textCursor;

    private int consoleWidth = -1;

    private TextConsole console;
    
    private IPropertyChangeListener propertyChangeListener;

    public TextConsoleViewer(Composite parent, TextConsole console) {
        super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        this.console = console;
        setDocument(console.getDocument());

        StyledText styledText = getTextWidget();
        styledText.setDoubleClickEnabled(true);
        styledText.addLineStyleListener(this);
        styledText.addLineBackgroundListener(this);
        styledText.setEditable(true);
        setFont(console.getFont());
        styledText.addMouseTrackListener(this);
        styledText.addPaintListener(this);
        
        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        propertyChangeListener = new HyperlinkColorChangeListener();
        colorRegistry.addListener(propertyChangeListener);
    }

    public void setTabWidth(int tabWidth) {
        StyledText styledText = getTextWidget();
        styledText.setTabs(tabWidth);
        styledText.redraw();
    }

    public void setFont(Font font) {
        StyledText styledText = getTextWidget();
        styledText.setFont(font);
        styledText.redraw();
    }

    protected void revealEndOfDocument() {
        StyledText text = getTextWidget();
        if (text != null) {
            int charCount = text.getCharCount();
            text.setCaretOffset(charCount);
            text.showSelection();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
     */
    public void lineGetStyle(LineStyleEvent event) {
        IDocument document = getDocument();
        if (document != null && document.getLength() > 0) {
            ArrayList ranges = new ArrayList();
            int offset = event.lineOffset;
            int length = event.lineText.length();
            
            StyleRange[] partitionerStyles = ((IConsoleDocumentPartitioner) document.getDocumentPartitioner()).getStyleRanges(event.lineOffset, event.lineText.length());
            if (partitionerStyles != null) {
                for (int i = 0; i < partitionerStyles.length; i++) {
                    ranges.add(partitionerStyles[i]);
                }
            }

            try {
                Display display = ConsolePlugin.getStandardDisplay();
                Color hyperlinkText = JFaceColors.getHyperlinkText(display);
                Position[] positions = getDocument().getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
                Position[] overlap = findPosition(offset, length, positions);
                if (overlap != null) {
	                for (int i = 0; i < overlap.length; i++) {
	                    weave(ranges, new StyleRange(overlap[i].offset, overlap[i].length, hyperlinkText, null));
	                }
                }
            } catch (BadPositionCategoryException e) {
            }
            
            if (ranges.size() > 0) {
                event.styles = (StyleRange[]) ranges.toArray(new StyleRange[ranges.size()]);
            }
        }
    }
    
    /**
     * Weaves the given style range into the given list of style ranges. The given 
     * range may overlap ranges in the list of ranges, and must be split into
     * non-overlapping ranges and inserted into the list to maintain order.
     * 
     * @param ranges
     * @param styleRange
     */
    private void weave(List ranges, StyleRange styleRange) {
        if (ranges.isEmpty()) {
            ranges.add(styleRange);
            return;
        }
        int start = styleRange.start;
        int end = start + styleRange.length;
        for (int i = 0; i < ranges.size(); i++) {
            StyleRange r = (StyleRange) ranges.get(i);
            int rEnd = r.start + r.length;
            if (start < r.start) {
                if (end >= r.start) {
                    ranges.add(i, new StyleRange(start, r.start - start, styleRange.foreground, styleRange.background));
                    if (end > rEnd) {
                        start = rEnd + 1;
                    } else {
                        return;
                    }
                } else {
                    
                }
            } else if (start < rEnd) {
                if (end > rEnd) {
                    start = rEnd + 1;
                } else {
                    return;
                }
            }
        }
        if (start < end) {
            ranges.add(new StyleRange(start, end - start, styleRange.foreground, styleRange.background));
        }
    }
    
	/**
	 * Binary search for the positions overlapping the given range
	 *
	 * @param offset the offset of the range
	 * @param length the length of the range
	 * @param positions the positions to search
	 * @return the positions overlapping the given range, or <code>null</code>
	 */
	private Position[] findPosition(int offset, int length, Position[] positions) {
		
		if (positions.length == 0)
			return null;
			
		int rangeEnd = offset + length;
		int left= 0;
		int right= positions.length - 1;
		int mid= 0;
		Position position= null;
		
		while (left < right) {
			
			mid= (left + right) / 2;
				
			position= positions[mid];
			if (rangeEnd < position.getOffset()) {
				if (left == mid)
					right= left;
				else
					right= mid -1;
			} else if (offset > (position.getOffset() + position.getLength() - 1)) {
				if (right == mid)
					left= right;
				else
					left= mid  +1;
			} else {
				left= right= mid;
			}
		}
		
		
		List list = new ArrayList();
		int index = left - 1;
		if (index >= 0) {
			position= positions[index];
			while (index >= 0 && (position.getOffset() + position.getLength()) > offset) {
				index--;
				if (index > 0) {
					position= positions[index];
				}
			}
		}
		index++;
		position= positions[index];
		while (index < positions.length && (position.getOffset() < rangeEnd)) {
			list.add(position);
			index++;
			if (index < positions.length) {
				position= positions[index];
			}
		}
		
		if (list.isEmpty()) {
			return null;
		}
		return (Position[])list.toArray(new Position[list.size()]);
	}    

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.custom.LineBackgroundListener#lineGetBackground(org.eclipse.swt.custom.LineBackgroundEvent)
     */
    public void lineGetBackground(LineBackgroundEvent event) {
        event.lineBackground = null;
    }

    /**
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e) {
        if (hyperlink != null) {
            IDocument doc = getDocument();
            StyledText text = getTextWidget();

            if (doc == null || text == null) {
                return;
            }

            IConsoleDocumentPartitioner partitioner = (IConsoleDocumentPartitioner) doc.getDocumentPartitioner();
            if (partitioner == null) {
                return;
            }

            IRegion linkRegion = console.getRegion(hyperlink);
            if (linkRegion != null) {
                int start = linkRegion.getOffset();
                int end = start + linkRegion.getLength();

                Color fontColor = JFaceColors.getActiveHyperlinkText(Display.getCurrent());
                Color color = e.gc.getForeground();
                e.gc.setForeground(fontColor);
                
                FontMetrics metrics = e.gc.getFontMetrics();
                int height = metrics.getHeight();
                int width = metrics.getAverageCharWidth();

                int startLine = text.getLineAtOffset(start);
                int endLine = text.getLineAtOffset(end);
                int baseLineBias = text.getBaseline() - (metrics.getAscent() + metrics.getLeading());

                for (int i = startLine; i <= endLine; i++) {
                    int styleStart = i == startLine ? start : text.getOffsetAtLine(i);
                    int styleEnd = i == endLine ? end : text.getOffsetAtLine(i + 1);
                    Point p1 = text.getLocationAtOffset(styleStart);
                    Point p2 = text.getLocationAtOffset(styleEnd - 1);
                    e.gc.drawLine(p1.x, p1.y + height + baseLineBias, p2.x + width, p2.y + height + baseLineBias);
                    
                    String hyperlinkText = text.getText(styleStart, styleEnd-1);
                    e.gc.drawString(hyperlinkText, p1.x, p1.y + baseLineBias);
                }
                e.gc.setForeground(color);
            }
        }
    }

    protected Cursor getHandCursor() {
        if (handCursor == null) {
            handCursor = new Cursor(ConsolePlugin.getStandardDisplay(), SWT.CURSOR_HAND);
        }
        return handCursor;
    }

    protected Cursor getTextCursor() {
        if (textCursor == null) {
            textCursor = new Cursor(ConsolePlugin.getStandardDisplay(), SWT.CURSOR_IBEAM);
        }
        return textCursor;
    }

    protected void linkEntered(IHyperlink link) {
        Control control = getTextWidget();
        control.setRedraw(false);
        if (hyperlink != null) {
            linkExited(hyperlink);
        }
        hyperlink = link;
        hyperlink.linkEntered();
        control.setCursor(getHandCursor());
        control.setRedraw(true);
        control.redraw();
        control.addMouseListener(this);
    }

    protected void linkExited(IHyperlink link) {
        link.linkExited();
        hyperlink = null;
        Control control = getTextWidget();
        control.setCursor(getTextCursor());
        control.redraw();
        control.removeMouseListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseEnter(MouseEvent e) {
        getTextWidget().addMouseMoveListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseExit(MouseEvent e) {
        getTextWidget().removeMouseMoveListener(this);
        if (hyperlink != null) {
            linkExited(hyperlink);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseHover(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
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

    /**
     * The cursor has just be moved to the given offset, the mouse has hovered
     * over the given offset. Update link rendering.
     * 
     * @param offset
     */
    protected void updateLinks(int offset) {
        if (offset >= 0) {
            IHyperlink link = getHyperlink(offset);
            if (link != null) {
                if (link.equals(hyperlink)) {
                    return;
                }
                linkEntered(link);
                return;
            }
        }
        if (hyperlink != null) {
            linkExited(hyperlink);
        }
    }

    /**
     * Returns the current value of <code>hyperlink</code> field
     * 
     * @return The current value of <code>hyperlink</code> field
     */
    public IHyperlink getHyperlink() {
        return hyperlink;
    }

    public IHyperlink getHyperlink(int offset) {
        if (offset >= 0 && console != null) {
            return console.getHyperlink(offset);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseDoubleClick(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseDown(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseUp(MouseEvent e) {
        if (hyperlink != null) {
            String selection = getTextWidget().getSelectionText();
            if (selection.length() <= 0) {
                if (e.button == 1) {
                    hyperlink.linkActivated();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.TextViewer#createDocumentAdapter()
     */
    protected IDocumentAdapter createDocumentAdapter() {
        if (documentAdapter == null) {
            documentAdapter = new ConsoleDocumentAdapter(consoleWidth = -1);
        }
        return documentAdapter;
    }

    /**
     * @param consoleWidth
     */
    public void setConsoleWidth(int width) {
        consoleWidth = width;
        if (documentAdapter != null) {
            documentAdapter.setWidth(consoleWidth);

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.TextViewer#handleDispose()
     */
    protected void handleDispose() {
        super.handleDispose();

        StyledText styledText = getTextWidget();
        styledText.removeLineStyleListener(this);
        styledText.removeLineBackgroundListener(this);
        styledText.removeMouseTrackListener(this);
        styledText.removePaintListener(this);
        
        handCursor = null;
        textCursor = null;
        hyperlink = null;
        console = null;
        
        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        colorRegistry.removeListener(propertyChangeListener);
    }
    
    class HyperlinkColorChangeListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(JFacePreferences.ACTIVE_HYPERLINK_COLOR) || event.getProperty().equals(JFacePreferences.HYPERLINK_COLOR)) {
				getTextWidget().redraw();
			}
		}
    	
    }
}
