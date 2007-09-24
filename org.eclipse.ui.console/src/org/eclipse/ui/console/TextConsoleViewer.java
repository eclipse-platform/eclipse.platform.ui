/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.console.ConsoleDocumentAdapter;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Default viewer used to display a <code>TextConsole</code>.
 * <p>
 * Clients may subclass this class.
 * </p>
 * 
 * @since 3.1
 */
public class TextConsoleViewer extends SourceViewer implements LineStyleListener, LineBackgroundListener, MouseTrackListener, MouseMoveListener, MouseListener {
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

    private IDocumentListener documentListener = new IDocumentListener() {
        public void documentAboutToBeChanged(DocumentEvent event) {
        }

        public void documentChanged(DocumentEvent event) {
            updateLinks(event.fOffset);
        }
    };
    // event listener used to send event to hyperlink for IHyperlink2
    private Listener mouseUpListener = new Listener() {
		public void handleEvent(Event event) {
	        if (hyperlink != null) {
	            String selection = getTextWidget().getSelectionText();
	            if (selection.length() <= 0) {
	                if (event.button == 1) {
	                	if (hyperlink instanceof IHyperlink2) {
							((IHyperlink2) hyperlink).linkActivated(event);
						} else {
							hyperlink.linkActivated();
						}
	                }
	            }
	        }
		}
	};

    WorkbenchJob revealJob = new WorkbenchJob("Reveal End of Document") {//$NON-NLS-1$
        public IStatus runInUIThread(IProgressMonitor monitor) {
            StyledText textWidget = getTextWidget();
            if (textWidget != null && !textWidget.isDisposed()) {
                int lineCount = textWidget.getLineCount();
                textWidget.setTopIndex(lineCount - 1);
            }
            return Status.OK_STATUS;
        }
    };
    
    private IPositionUpdater positionUpdater = new IPositionUpdater() {
        public void update(DocumentEvent event) {
            try {
                IDocument document = getDocument();
                if (document != null) {
                    Position[] positions = document.getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
                    for (int i = 0; i < positions.length; i++) {
                        Position position = positions[i];
                        if (position.offset == event.fOffset && position.length<=event.fLength) {
                            position.delete();
                        }
                        if (position.isDeleted) {
                            document.removePosition(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY, position);
                        }
                    }
                }
            } catch (BadPositionCategoryException e) {
            }
        }
    };

    /**
     * Constructs a new viewer in the given parent for the specified console.
     * 
     * @param parent
     *            containing widget
     * @param console
     *            text console
     */
    public TextConsoleViewer(Composite parent, TextConsole console) {
        super(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
        this.console = console;

        IDocument document = console.getDocument();
        setDocument(document);

        StyledText styledText = getTextWidget();
        styledText.setDoubleClickEnabled(true);
        styledText.addLineStyleListener(this);
        styledText.addLineBackgroundListener(this);
        styledText.setEditable(true);
        setFont(console.getFont());
        styledText.addMouseTrackListener(this);
        styledText.addListener(SWT.MouseUp, mouseUpListener);

        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        propertyChangeListener = new HyperlinkColorChangeListener();
        colorRegistry.addListener(propertyChangeListener);

        revealJob.setSystem(true);
        document.addDocumentListener(documentListener);
        document.addPositionUpdater(positionUpdater);
    }

    /**
     * Sets the tab width used by this viewer.
     * 
     * @param tabWidth
     *            the tab width used by this viewer
     */
    public void setTabWidth(int tabWidth) {
        StyledText styledText = getTextWidget();
        int oldWidth = styledText.getTabs();
        if (tabWidth != oldWidth) {
            styledText.setTabs(tabWidth);
        }
    }

    /**
     * Sets the font used by this viewer.
     * 
     * @param font
     *            the font used by this viewer
     */
    public void setFont(Font font) {
        StyledText styledText = getTextWidget();
        Font oldFont = styledText.getFont();
        if (oldFont == font) {
            return;
        }
        if (font == null || !(font.equals(oldFont))) {
            styledText.setFont(font);
        }
    }

    /**
     * Positions the cursor at the end of the document.
     */
    protected void revealEndOfDocument() {
        revealJob.schedule(50);
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
            } else {
                ranges.add(new StyleRange(offset, length, null, null));
            }

            try {
                Position[] positions = getDocument().getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
                Position[] overlap = findPosition(offset, length, positions);
                Color color = JFaceColors.getHyperlinkText(Display.getCurrent());
                if (overlap != null) {
                    for (int i = 0; i < overlap.length; i++) {
                        Position position = overlap[i];
                        StyleRange linkRange = new StyleRange(position.offset, position.length, color, null);
                        linkRange.underline = true;
                        override(ranges, linkRange);
                    }
                }
            } catch (BadPositionCategoryException e) {
            }

            if (ranges.size() > 0) {
                event.styles = (StyleRange[]) ranges.toArray(new StyleRange[ranges.size()]);
            }
        }
    }

    private void override(List ranges, StyleRange newRange) {
        if (ranges.isEmpty()) {
            ranges.add(newRange);
            return;
        }

        int start = newRange.start;
        int end = start + newRange.length;
        for (int i = 0; i < ranges.size(); i++) {
            StyleRange existingRange = (StyleRange) ranges.get(i);
            int rEnd = existingRange.start + existingRange.length;
            if (end <= existingRange.start || start >= rEnd) {
                continue;
            }

            if (start < existingRange.start && end > existingRange.start) {
                start = existingRange.start;
            }

            if (start >= existingRange.start && end <= rEnd) {
                existingRange.length = start - existingRange.start;
                ranges.add(++i, newRange);
                if (end != rEnd) {
                    ranges.add(++i, new StyleRange(end, rEnd - end - 1, existingRange.foreground, existingRange.background));
                }
                return;
            } else if (start >= existingRange.start && start < rEnd) {
                existingRange.length = start - existingRange.start;
                ranges.add(++i, newRange);
            } else if (end >= rEnd) {
                ranges.remove(i);
            } else {
                ranges.add(++i, new StyleRange(end + 1, rEnd - end + 1, existingRange.foreground, existingRange.background));
            }
        }
    }

    /**
     * Binary search for the positions overlapping the given range
     * 
     * @param offset
     *            the offset of the range
     * @param length
     *            the length of the range
     * @param positions
     *            the positions to search
     * @return the positions overlapping the given range, or <code>null</code>
     */
    private Position[] findPosition(int offset, int length, Position[] positions) {

        if (positions.length == 0)
            return null;

        int rangeEnd = offset + length;
        int left = 0;
        int right = positions.length - 1;
        int mid = 0;
        Position position = null;

        while (left < right) {

            mid = (left + right) / 2;

            position = positions[mid];
            if (rangeEnd < position.getOffset()) {
                if (left == mid)
                    right = left;
                else
                    right = mid - 1;
            } else if (offset > (position.getOffset() + position.getLength() - 1)) {
                if (right == mid)
                    left = right;
                else
                    left = mid + 1;
            } else {
                left = right = mid;
            }
        }

        List list = new ArrayList();
        int index = left - 1;
        if (index >= 0) {
            position = positions[index];
            while (index >= 0 && (position.getOffset() + position.getLength()) > offset) {
                index--;
                if (index > 0) {
                    position = positions[index];
                }
            }
        }
        index++;
        position = positions[index];
        while (index < positions.length && (position.getOffset() < rangeEnd)) {
            list.add(position);
            index++;
            if (index < positions.length) {
                position = positions[index];
            }
        }

        if (list.isEmpty()) {
            return null;
        }
        return (Position[]) list.toArray(new Position[list.size()]);
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
     * Returns the hand cursor.
     * 
     * @return the hand cursor
     */
    protected Cursor getHandCursor() {
        if (handCursor == null) {
            handCursor = new Cursor(ConsolePlugin.getStandardDisplay(), SWT.CURSOR_HAND);
        }
        return handCursor;
    }

    /**
     * Returns the text cursor.
     * 
     * @return the text cursor
     */
    protected Cursor getTextCursor() {
        if (textCursor == null) {
            textCursor = new Cursor(ConsolePlugin.getStandardDisplay(), SWT.CURSOR_IBEAM);
        }
        return textCursor;
    }

    /**
     * Notification a hyperlink has been entered.
     * 
     * @param link
     *            the link that was entered
     */
    protected void linkEntered(IHyperlink link) {
        Control control = getTextWidget();
        if (hyperlink != null) {
            linkExited(hyperlink);
        }
        hyperlink = link;
        hyperlink.linkEntered();
        control.setCursor(getHandCursor());
        control.redraw();
        control.addMouseListener(this);
    }

    /**
     * Notification a link was exited.
     * 
     * @param link
     *            the link that was exited
     */
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
     * Returns the currently active hyperlink or <code>null</code> if none.
     * 
     * @return the currently active hyperlink or <code>null</code> if none
     */
    public IHyperlink getHyperlink() {
        return hyperlink;
    }

    /**
     * Returns the hyperlink at the specified offset, or <code>null</code> if
     * none.
     * 
     * @param offset
     *            offset at which a hyperlink has been requested
     * @return hyperlink at the specified offset, or <code>null</code> if none
     */
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
     * Sets the console to have a fixed character width. Use -1 to indicate that
     * a fixed width should not be used.
     * 
     * @param width
     *            fixed character width of the console, or -1
     */
    public void setConsoleWidth(int width) {
        if (consoleWidth != width) {
            consoleWidth = width;
            ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (documentAdapter != null) {
                        documentAdapter.setWidth(consoleWidth);
                    }
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.TextViewer#handleDispose()
     */
    protected void handleDispose() {
        IDocument document = getDocument();
        if (document != null) {
            document.removeDocumentListener(documentListener);
            document.removePositionUpdater(positionUpdater);
        }

        StyledText styledText = getTextWidget();
        styledText.removeLineStyleListener(this);
        styledText.removeLineBackgroundListener(this);
        styledText.removeMouseTrackListener(this);

        if(handCursor != null) {
        	handCursor.dispose();
        }
        handCursor = null;
        if(textCursor != null) {
        	textCursor.dispose();
        }
        textCursor = null;
        hyperlink = null;
        console = null;

        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        colorRegistry.removeListener(propertyChangeListener);
        
        super.handleDispose();
    }

    class HyperlinkColorChangeListener implements IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(JFacePreferences.ACTIVE_HYPERLINK_COLOR) || event.getProperty().equals(JFacePreferences.HYPERLINK_COLOR)) {
                getTextWidget().redraw();
            }
        }

    }

    /*
     * work around to memory leak in TextViewer$WidgetCommand
     */
    protected void updateTextListeners(WidgetCommand cmd) {
        super.updateTextListeners(cmd);
        cmd.preservedText = null;
        cmd.event = null;
        cmd.text = null;
    }

    protected void internalRevealRange(int start, int end) {
        StyledText textWidget = getTextWidget();
        int startLine = documentAdapter.getLineAtOffset(start);
        int endLine = documentAdapter.getLineAtOffset(end);

        int top = textWidget.getTopIndex();
        if (top > -1) {
            // scroll vertically
            int lines = getVisibleLinesInViewport();
            int bottom = top + lines;

            // two lines at the top and the bottom should always be left
            // if window is smaller than 5 lines, always center position is
            // chosen
            int bufferZone = 2;
            if (startLine >= top + bufferZone && startLine <= bottom - bufferZone && endLine >= top + bufferZone && endLine <= bottom - bufferZone) {

                // do not scroll at all as it is already visible
            } else {
                int delta = Math.max(0, lines - (endLine - startLine));
                textWidget.setTopIndex(startLine - delta / 3);
                updateViewportListeners(INTERNAL);
            }

            // scroll horizontally
            if (endLine < startLine) {
                endLine += startLine;
                startLine = endLine - startLine;
                endLine -= startLine;
            }

            int startPixel = -1;
            int endPixel = -1;

            if (endLine > startLine) {
                // reveal the beginning of the range in the start line
                IRegion extent = getExtent(start, start);
                startPixel = extent.getOffset() + textWidget.getHorizontalPixel();
                endPixel = startPixel;
            } else {
                IRegion extent = getExtent(start, end);
                startPixel = extent.getOffset() + textWidget.getHorizontalPixel();
                endPixel = startPixel + extent.getLength();
            }

            int visibleStart = textWidget.getHorizontalPixel();
            int visibleEnd = visibleStart + textWidget.getClientArea().width;

            // scroll only if not yet visible
            if (startPixel < visibleStart || visibleEnd < endPixel) {
                // set buffer zone to 10 pixels
                bufferZone = 10;
                int newOffset = visibleStart;
                int visibleWidth = visibleEnd - visibleStart;
                int selectionPixelWidth = endPixel - startPixel;

                if (startPixel < visibleStart)
                    newOffset = startPixel;
                else if (selectionPixelWidth + bufferZone < visibleWidth)
                    newOffset = endPixel + bufferZone - visibleWidth;
                else
                    newOffset = startPixel;

                float index = ((float) newOffset) / ((float) getAverageCharWidth());

                textWidget.setHorizontalIndex(Math.round(index));
            }

        }
    }

}
