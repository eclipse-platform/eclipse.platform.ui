/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;

import org.eclipse.ui.internal.texteditor.LineNumberColumn;

import org.eclipse.ui.texteditor.stickyscroll.IStickyLine;

/**
 * This class builds a control that is rendered on top of the given source viewer. The controls
 * shows the sticky lines that are set via {@link #setStickyLines(List)} on top of the source
 * viewer. The {@link IStickyLine#getLineNumber()} is linked to to corresponding line number in the
 * given source viewer, with index starting at 0.
 * 
 * As part of its responsibilities, the class handles layout arrangement and styling of the sticky
 * lines along with navigation to the respective sticky line.
 *
 * Note: The dispose method should be called to clean up system resources associated with this
 * object when it's no longer needed.
 */
public class StickyScrollingControl {

	/**
	 * This threshold represents the minimum number of source lines that must remain visible in the
	 * editor. If the StickyScrollingControl's size would result in fewer than this number of lines
	 * being visible, the height of the StickyScrollingControl will be reduced to ensure visibility
	 * of at least this many lines. Thus, it guarantees a minimum visibility threshold for the
	 * source content in the editor underneath the StickyScrollingControl.
	 */
	private final static int MIN_VISIBLE_EDITOR_LINES_THRESHOLD= 3;

	private static final String DISABLE_CSS= "org.eclipse.e4.ui.css.disabled"; //$NON-NLS-1$

	private List<IStickyLine> stickyLines;

	private ISourceViewer sourceViewer;

	private IVerticalRuler verticalRuler;

	private StickyScrollingControlSettings settings;

	private Canvas stickyLinesCanvas;

	private StyledText stickyLineNumber;

	private StyledText stickyLineText;

	private ITextPresentationListener textPresentationListener;

	private ControlListener controlListener;

	private StickyScollingCaretListener caretListener;

	private Composite bottomSeparator;

	private StickyScrollingHandler stickyScrollingHandler;

	private int maximumVisibleStickyLines= Integer.MAX_VALUE;

	public StickyScrollingControl(ISourceViewer sourceViewer, StickyScrollingControlSettings settings) {
		this(sourceViewer, null, settings, null);
	}

	public StickyScrollingControl(ISourceViewer sourceViewer, IVerticalRuler verticalRuler,
			StickyScrollingControlSettings settings, StickyScrollingHandler stickyScrollingHandler) {
		this.stickyScrollingHandler= stickyScrollingHandler;
		this.stickyLines= new ArrayList<>();
		this.sourceViewer= sourceViewer;
		this.verticalRuler= verticalRuler;
		this.settings= settings;

		createControls();
		addSourceViewerListeners();
	}

	/**
	 * Sets the sticky lines to show. The line numbers are linked to the line number in the
	 * corresponding source viewer, starting with index 0.
	 * 
	 * @param stickyLines The sticky lines to show
	 */
	public void setStickyLines(List<IStickyLine> stickyLines) {
		if (!stickyLines.equals(this.stickyLines)) {
			this.stickyLines= stickyLines;
			updateStickyScrollingControls();
		}
	}

	public void applySettings(StickyScrollingControlSettings newSettings) {
		this.settings= newSettings;

		stickyLineNumber.setBackground(newSettings.stickyLineBackgroundColor());
		stickyLineText.setBackground(newSettings.stickyLineBackgroundColor());
		bottomSeparator.setBackground(settings.stickyLinesSeparatorColor());

		updateStickyScrollingControls();
		styleStickyLines();
		layoutStickyLines();
	}

	public void dispose() {
		if (sourceViewer instanceof ITextViewerExtension4 extension) {
			extension.removeTextPresentationListener(textPresentationListener);
		}
		if (sourceViewer.getTextWidget() != null) {
			sourceViewer.getTextWidget().removeControlListener(controlListener);
			sourceViewer.getTextWidget().removeKeyListener(caretListener);
			sourceViewer.getTextWidget().removeCaretListener(caretListener);
		}
		this.stickyLinesCanvas.dispose();
	}

	private void createControls() {
		Composite sourceViewerComposite= null;
		if (sourceViewer instanceof ITextViewerExtension extension) {
			sourceViewerComposite= (Composite) extension.getControl();
		} else {
			sourceViewerComposite= sourceViewer.getTextWidget().getParent();
		}

		stickyLinesCanvas= new Canvas(sourceViewerComposite, SWT.NONE);
		addMouseListeners(stickyLinesCanvas);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).applyTo(stickyLinesCanvas);

		stickyLineNumber= new StyledText(stickyLinesCanvas, SWT.LEFT | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(false, true).exclude(verticalRuler == null).applyTo(stickyLineNumber);
		stickyLineNumber.setVisible(verticalRuler != null);
		stickyLineNumber.setEnabled(false);
		stickyLineNumber.setBackground(settings.stickyLineBackgroundColor());

		stickyLineText= new StyledText(stickyLinesCanvas, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(stickyLineText);
		stickyLineText.setEnabled(false);
		stickyLineText.setBackground(settings.stickyLineBackgroundColor());

		bottomSeparator= new Composite(stickyLinesCanvas, SWT.NONE);
		GridDataFactory.fillDefaults().hint(0, 3).grab(true, false).span(2, 1).applyTo(bottomSeparator);
		bottomSeparator.setEnabled(false);
		bottomSeparator.setData(DISABLE_CSS, Boolean.TRUE);
		bottomSeparator.setBackground(settings.stickyLinesSeparatorColor());

		layoutLineNumbers();
		limitVisibleStickyLinesToTextWidgetHeight(sourceViewer.getTextWidget());

		stickyLinesCanvas.pack();
		stickyLinesCanvas.moveAbove(null);
	}

	private void updateStickyScrollingControls() {
		StringJoiner stickyLineTextJoiner= new StringJoiner(System.lineSeparator());
		StringJoiner stickyLineNumberJoiner= new StringJoiner(System.lineSeparator());
		for (int i= 0; i < getNumberStickyLines(); i++) {
			IStickyLine stickyLine= stickyLines.get(i);
			stickyLineTextJoiner.add(stickyLine.getText());
			int lineNumber= stickyLine.getLineNumber();
			stickyLineNumberJoiner.add(fillLineNumberWithLeadingSpaces(lineNumber + 1));
		}

		String newStickyLineText= stickyLineTextJoiner.toString();
		String newStickyLineNumber= stickyLineNumberJoiner.toString();

		if (!newStickyLineText.equals(stickyLineText.getText())
				|| !newStickyLineNumber.equals(stickyLineNumber.getText())) {
			stickyLineText.setText(stickyLineTextJoiner.toString());
			stickyLineNumber.setText(stickyLineNumberJoiner.toString());
			styleStickyLines();
			layoutStickyLines();
		}
	}

	private String fillLineNumberWithLeadingSpaces(int lineNumber) {
		int lineCount= sourceViewer.getDocument().getNumberOfLines();
		int lineNumberLength= String.valueOf(lineCount).length();
		String formatString= "%" + lineNumberLength + "d"; //$NON-NLS-1$ //$NON-NLS-2$
		return String.format(formatString, lineNumber);
	}

	private void styleStickyLines() {
		StyledText textWidget= sourceViewer.getTextWidget();
		if (textWidget == null || textWidget.isDisposed()) {
			return;
		}

		List<StyleRange> stickyLinesStyleRanges= new ArrayList<>();
		int stickyLineTextOffset= 0;
		int stickyLinesCount = getNumberStickyLines();
		for (int i = 0; i < stickyLinesCount; i++) {
			IStickyLine stickyLine= stickyLines.get(i);
			StyleRange[] ranges= stickyLine.getStyleRanges();
			if (ranges != null) {
				for (StyleRange styleRange : ranges) {
					styleRange.start+= stickyLineTextOffset;
					stickyLinesStyleRanges.add(styleRange);
				}
			}
			stickyLineTextOffset+= stickyLine.getText().length() + System.lineSeparator().length();
		}
		stickyLineText.setStyleRanges(stickyLinesStyleRanges.toArray(StyleRange[]::new));

		stickyLineNumber.setFont(textWidget.getFont());
		stickyLineNumber.setStyleRange(new StyleRange(0, stickyLineNumber.getText().length(), settings.lineNumberColor(), null));
		stickyLineNumber.setLineSpacing(textWidget.getLineSpacing());

		stickyLineText.setFont(textWidget.getFont());
		stickyLineText.setForeground(textWidget.getForeground());
		stickyLineText.setLineSpacing(textWidget.getLineSpacing());
		stickyLineText.setLeftMargin(textWidget.getLeftMargin());
	}

	private void layoutStickyLines() {
		if (getNumberStickyLines() == 0) {
			stickyLinesCanvas.setVisible(false);
			return;
		}

		layoutLineNumbers();

		stickyLinesCanvas.setVisible(true);
		calculateAndSetStickyLinesCanvasBounds();
	}

	/**
	 * The line numbers layout is calculated based on the given {@link #verticalRuler}.
	 *
	 * If the vertical ruler is an instance of {@link CompositeRuler}, it is tried to align the
	 * layout with the layout of the {@link LineNumberColumn}.
	 *
	 * If the vertical ruler is from another instance, the lines number are align in the center of
	 * the vertical ruler space.
	 */
	private void layoutLineNumbers() {
		if (verticalRuler == null) {
			return;
		}

		LineNumberColumn lineNumberColumn= getLineNumberColumn(verticalRuler);
		if (!settings.showLineNumbers()) {
			stickyLineNumber.setRightMargin(verticalRuler.getWidth());
			((GridData) stickyLineNumber.getLayoutData()).widthHint= 0;
			stickyLineNumber.setLeftMargin(0);
		} else if (lineNumberColumn == null) {
			((GridData) stickyLineNumber.getLayoutData()).widthHint= verticalRuler.getWidth();
			GC gc= new GC(stickyLinesCanvas);
			gc.setFont(sourceViewer.getTextWidget().getFont());
			String lastLineNumber= String.valueOf(sourceViewer.getTextWidget().getLineCount());
			Point p= gc.textExtent(lastLineNumber);
			int textWidth= p.x;
			gc.dispose();
			int width= verticalRuler.getWidth();
			int left= (width - textWidth) / 2;

			stickyLineNumber.setLeftMargin(left);
			((GridData) stickyLineNumber.getLayoutData()).widthHint= textWidth;
			stickyLineNumber.setRightMargin(width - left - textWidth);
		} else {
			Rectangle lineNumberBounds= lineNumberColumn.getControl().getBounds();
			stickyLineNumber.setLeftMargin(lineNumberBounds.x);
			((GridData) stickyLineNumber.getLayoutData()).widthHint= lineNumberBounds.width;
			stickyLineNumber.setRightMargin(verticalRuler.getWidth() - lineNumberBounds.x - lineNumberBounds.width);
		}
		stickyLinesCanvas.layout();
	}

	private LineNumberColumn getLineNumberColumn(IVerticalRuler ruler) {
		if (ruler instanceof CompositeRuler compositeRuler) {
			Iterator<IVerticalRulerColumn> decoratorIterator= compositeRuler.getDecoratorIterator();
			while (decoratorIterator.hasNext()) {
				IVerticalRulerColumn colum= decoratorIterator.next();
				if (colum instanceof LineNumberColumn lineNumberColumn) {
					return lineNumberColumn;
				}
			}
		}
		return null;
	}

	private void calculateAndSetStickyLinesCanvasBounds() {
		StyledText textWidget= sourceViewer.getTextWidget();

		int numberStickyLines= getNumberStickyLines();
		int lineHeight= stickyLineText.getLineHeight() * numberStickyLines;
		int spacingHeight= stickyLineText.getLineSpacing() * (numberStickyLines - 1);
		int separatorHeight= bottomSeparator.getBounds().height;

		int rulerWidth= verticalRuler != null ? verticalRuler.getWidth() : 0;
		int textWidth= textWidget.getClientArea().width + 1;

		Rectangle bounds= new Rectangle(0, 0, 0, 0);
		bounds.height= lineHeight + spacingHeight + separatorHeight;
		bounds.width= rulerWidth + textWidth;
		stickyLinesCanvas.setBounds(bounds);
	}

	private void navigateToClickedLine(MouseEvent event) {
		int clickedStickyLineIndex= stickyLineText.getLineIndex(event.y);
		IStickyLine clickedStickyLine= stickyLines.get(clickedStickyLineIndex);

		try {
			int offset= sourceViewer.getDocument().getLineOffset(clickedStickyLine.getLineNumber());
			sourceViewer.setSelectedRange(offset, 0);
			ensureSourceViewerLineVisible(clickedStickyLine.getLineNumber());
		} catch (BadLocationException e) {
			//Do not navigate
		}
	}

	private void ensureSourceViewerLineVisible(int line) {
		if (sourceViewer instanceof ITextViewerExtension5 extension) {
			//operate on text widget since source viewer can hide lines via code folding, etc.
			line= extension.modelLine2WidgetLine(line);
		}
		StyledText textWidget= sourceViewer.getTextWidget();
		int bottomIndex= JFaceTextUtil.getBottomIndex(textWidget);
		if (line < textWidget.getTopIndex() + settings.maxCountStickyLines() ||
				line > bottomIndex) {
			int jumpTo= Math.max(0, line - settings.maxCountStickyLines());
			if (sourceViewer instanceof ITextViewerExtension5 extension) {
				jumpTo= extension.widgetLine2ModelLine(jumpTo);
			}
			sourceViewer.setTopIndex(jumpTo);
		}
	}

	private int getNumberStickyLines() {
		int numberStickyLines= Math.min(settings.maxCountStickyLines(), this.stickyLines.size());
		numberStickyLines= Math.min(maximumVisibleStickyLines, numberStickyLines);
		return numberStickyLines;
	}

	/**
	 * Add several listeners to the source viewer.<br>
	 *
	 * textPresentationListener in order to style the sticky lines when the source viewer styling
	 * has changed.<br>
	 * <br>
	 * keyListener in order to scroll the source viewer so that the affected line is visible under
	 * the sticky lines.<br>
	 * controlListener in order to layout the sticky lines when the source viewer is
	 * resized/moved.<br>
	 */
	private void addSourceViewerListeners() {
		StyledText textWidget= sourceViewer.getTextWidget();

		if (sourceViewer instanceof ITextViewerExtension4 extension) {
			textPresentationListener = e -> {
				Display.getDefault().asyncExec(() -> {
					if (textWidget.isDisposed() || areStickyLinesOutDated(textWidget)) {
						return;
					}
					styleStickyLines();
				});
			};
			extension.addTextPresentationListener(textPresentationListener);
		}

		caretListener= new StickyScollingCaretListener();
		textWidget.addCaretListener(caretListener);
		textWidget.addKeyListener(caretListener);

		controlListener= new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				if (areStickyLinesOutDated(textWidget)) {
					return;
				}
				limitVisibleStickyLinesToTextWidgetHeight(textWidget);
				layoutStickyLines();
				if (stickyScrollingHandler != null) {
					stickyScrollingHandler.viewportChanged(textWidget.getTopPixel());
				}
			}

			@Override
			public void controlMoved(ControlEvent e) {
				layoutStickyLines();
			}
		};
		textWidget.addControlListener(controlListener);
	}

	/**
	 * Checks if the sticky lines are out dated. Specifically, it verifies that the
	 * line number of the last sticky line does not exceed the total line count of
	 * the source viewer.
	 * 
	 * This situation can occur, for example, when an editor is opened via the
	 * search view and "reuse editor" is enabled. In such cases, the text in the
	 * source viewer is replaced, but the out dated sticky lines associated with the
	 * previous source code remain in the first call.
	 */
	private boolean areStickyLinesOutDated(StyledText textWidget) {
		if (stickyLines.size() > 0) {
			int lastStickyLineNumber = stickyLines.get(stickyLines.size() - 1).getLineNumber();
			return lastStickyLineNumber >= textWidget.getLineCount();
		}
		return false;
	}

	private void limitVisibleStickyLinesToTextWidgetHeight(StyledText textWidget) {
		int lineHeight= textWidget.getLineHeight() + textWidget.getLineSpacing();
		int textWidgetHeight= textWidget.getBounds().height;

		int visibleLinesInTextWidget= textWidgetHeight / lineHeight;

		maximumVisibleStickyLines= Math.max(0, visibleLinesInTextWidget - MIN_VISIBLE_EDITOR_LINES_THRESHOLD);
		updateStickyScrollingControls();
	}

	/**
	 * Sets the cursor on the canvas to {@link SWT#CURSOR_HAND} and adds several mouse listeners to
	 * the canvas.<br>
	 * <br>
	 * mouseListener in order to navigate to the clicked sticky line<br>
	 * mouseMoveListener in order to highlight the affected sticky line<br>
	 * mouseTrackListener in order to remove the highlighting when the control is exit<br>
	 */
	private void addMouseListeners(Canvas canvas) {
		canvas.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				navigateToClickedLine(e);
			}
		});

		canvas.addMouseMoveListener(e -> {
			int affectedStickyLineIndex= stickyLineText.getLineIndex(e.y);
			stickyLineText.setLineBackground(0, getNumberStickyLines(), null);
			stickyLineText.setLineBackground(affectedStickyLineIndex, 1, settings.stickyLineHoverColor());
		});

		canvas.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				stickyLineText.setLineBackground(0, getNumberStickyLines(), null);
			}
		});

		ScrollingDispatchingListener scrollingDispatchingListener= new ScrollingDispatchingListener();
		canvas.addListener(SWT.MouseHorizontalWheel, scrollingDispatchingListener);
		canvas.addListener(SWT.MouseVerticalWheel, scrollingDispatchingListener);
	}

	/**
	 * A listener that ensures that the caret is visible below the sticky lines control whenever it
	 * is moved using the keyboard. This listener ignores actions that move the caret, unless those
	 * actions are initiated from the keyboard.
	 */
	class StickyScollingCaretListener implements CaretListener, KeyListener {

		private boolean enableCaretListener;

		@Override
		public void keyPressed(KeyEvent e) {
			enableCaretListener= true;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			enableCaretListener= false;
		}

		@Override
		public void caretMoved(CaretEvent event) {
			int offsetEndPosition= sourceViewer.getTextWidget().getCharCount();
			if (event.caretOffset == 0 || event.caretOffset == offsetEndPosition) {
				return;
			}
			Display.getDefault().asyncExec(() -> {
				StyledText textWidget= sourceViewer.getTextWidget();
				if (!enableCaretListener || event.caretOffset > textWidget.getCharCount()) {
					return;
				}
				int line= textWidget.getLineAtOffset(event.caretOffset);
				if (sourceViewer instanceof ITextViewerExtension5 extension) {
					line= extension.widgetLine2ModelLine(line);
				}
				ensureSourceViewerLineVisible(line);
			});
		}
	}

	/**
	 * A mouse wheel listener that is dispatching the scrolling on the sticky lines canvas to the
	 * source viewer. The calculation of the scrolling steps is copied from
	 * {@link Composite}#scrollWheel.
	 */
	class ScrollingDispatchingListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			StyledText textWidget= sourceViewer.getTextWidget();

			ScrollBar bar= event.type == SWT.MouseHorizontalWheel ? textWidget.getHorizontalBar() : textWidget.getVerticalBar();
			if (bar == null) {
				return;
			}

			int deltaY= event.count;
			if (-1 < deltaY && deltaY < 0) {
				deltaY= -1;
			}
			if (0 < deltaY && deltaY < 1) {
				deltaY= 1;
			}

			int pixel= Math.max(0, (int) (0.5f + bar.getSelection() - bar.getIncrement() * deltaY));

			if (event.type == SWT.MouseHorizontalWheel) {
				sourceViewer.getTextWidget().setHorizontalPixel(pixel);
			} else {
				sourceViewer.getTextWidget().setTopPixel(pixel);
				if (stickyScrollingHandler != null) {
					stickyScrollingHandler.viewportChanged(pixel);
				}
			}
		}
	}
}
