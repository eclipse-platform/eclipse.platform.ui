/*******************************************************************************
 * Copyright (c) 2018 Angelo ZERR.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - [minimap] Initialize minimap view - Bug 535450
 * Arne Deutsch <arne.deutsch@itemis.de> - Correct view height - Bug 536207
 *******************************************************************************/
package org.eclipse.ui.internal.views.minimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

/**
 * Minimap widget which displays scaled content of the given text editor.
 *
 */
public class MinimapWidget {

	private static final float SCALE = 0.2f;

	private final ITextViewer fEditorViewer;

	private final StyledText fMinimapTextWidget;

	/**
	 * Editor tracker used to track text changed and styles changes of the
	 * editor content.
	 */
	class EditorTracker implements TextChangeListener, ControlListener, ITextPresentationListener, ITextInputListener,
			IViewportListener {

		private Map<Font, Font> fScaledFonts;

		@Override
		public void textSet(TextChangedEvent event) {
			synchText();
		}

		@Override
		public void textChanging(TextChangingEvent event) {
			try {
				fMinimapTracker.replaceTextRange(event);
			} catch (Exception e) {
				IStatus status = new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK,
						"Minimap text content synchronization failed", e); //$NON-NLS-1$
				TextEditorPlugin.getDefault().getLog().log(status);
				synchText();
			}
		}

		@Override
		public void textChanged(TextChangedEvent event) {
			updateMinimapAfterTextChange();
		}

		@Override
		public void applyTextPresentation(TextPresentation presentation) {
			try {
				addPresentation(presentation);
			} catch (Exception e) {
				synchTextAndStyles();
				IStatus status = new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK,
						"Minimap styles synchronization failed", e); //$NON-NLS-1$
				TextEditorPlugin.getDefault().getLog().log(status);
			}
		}

		private StyleRange modelStyleRange2WidgetStyleRange(StyleRange range) {
			IRegion region = modelRange2WidgetRange(new Region(range.start, range.length));
			if (region != null) {
				StyleRange result = (StyleRange) range.clone();
				result.start = region.getOffset();
				result.length = region.getLength();
				return result;
			}
			return null;
		}

		private IRegion modelRange2WidgetRange(IRegion region) {
			if (fEditorViewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension = (ITextViewerExtension5) fEditorViewer;
				return extension.modelRange2WidgetRange(region);
			}
			if (fEditorViewer instanceof TextViewer) {
				return ((TextViewer) fEditorViewer).modelRange2WidgetRange(region);
			}
			IRegion visibleRegion = fEditorViewer.getVisibleRegion();
			int start = region.getOffset() - visibleRegion.getOffset();
			int end = start + region.getLength();
			if (end > visibleRegion.getLength())
				end = visibleRegion.getLength();

			return new Region(start, end - start);
		}

		private void addPresentation(TextPresentation presentation) {

			StyleRange range = presentation.getDefaultStyleRange();
			if (range != null) {

				range = modelStyleRange2WidgetStyleRange(range);
				if (range != null) {
					updateStyle(range);
					fMinimapTextWidget.setStyleRange(range);
				}
				List<StyleRange> ranges = new ArrayList<>(presentation.getDenumerableRanges());
				Iterator<StyleRange> e = presentation.getNonDefaultStyleRangeIterator();
				while (e.hasNext()) {
					range = e.next();
					range = modelStyleRange2WidgetStyleRange(range);
					if (range != null) {
						updateStyle(range);
						ranges.add(range);
					}
				}

				if (!ranges.isEmpty()) {
					fMinimapTextWidget.replaceStyleRanges(0, 0, ranges.toArray(new StyleRange[ranges.size()]));
				}

			} else {
				IRegion region = modelRange2WidgetRange(presentation.getCoverage());
				if (region == null)
					return;

				List<StyleRange> list = new ArrayList<>(presentation.getDenumerableRanges());
				Iterator<StyleRange> e = presentation.getAllStyleRangeIterator();
				while (e.hasNext()) {
					range = e.next();
					range = modelStyleRange2WidgetStyleRange(range);
					if (range != null) {
						updateStyle(range);
						list.add(range);
					}
				}

				if (!list.isEmpty()) {
					StyleRange[] ranges = new StyleRange[list.size()];
					list.toArray(ranges);
					fMinimapTextWidget.replaceStyleRanges(region.getOffset(), region.getLength(), ranges);
				}
			}
		}

		void updateStyle(StyleRange range) {
			// scale the font
			range.font = getScaledFont(range.font);
			// remove GlyphMetrics (created by code mining)
			range.metrics = null;
		}

		Font getScaledFont(Font editorFont) {
			if (editorFont == null) {
				return null;
			}
			Font scaledFont = fScaledFonts.get(editorFont);
			if (scaledFont != null) {
				return scaledFont;
			}
			FontData[] fontData = editorFont.getFontData();
			FontData fontDatum = fontData[0];

			scaledFont = new Font(editorFont.getDevice(), fontDatum.getName(),
					(int) Math.ceil(fontDatum.getHeight() * getScale()), fontDatum.getStyle());
			fScaledFonts.put(editorFont, scaledFont);
			return scaledFont;
		}

		@Override
		public void controlMoved(ControlEvent e) {
			// Do nothing
		}

		@Override
		public void controlResized(ControlEvent e) {
			updateMinimapAfterResize();
		}

		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			if (oldInput != newInput) {
				synchTextAndStyles();
			}
		}

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			// Do nothing
		}

		@Override
		public void viewportChanged(int verticalOffset) {
			fMinimapTextWidget.getDisplay().asyncExec(() -> {
				if (!fMinimapTextWidget.isDisposed()) {
					updateMinimapAfterResize();
				}
			});
		}

		void updateMinimapAfterResize() {
			updateMinimap(false);
		}

		void updateMinimapAfterTextChange() {
			updateMinimap(true);
		}

		void updateMinimap(boolean textChanged) {
			StyledText editorTextWidget = fEditorViewer.getTextWidget();
			int editorTopIndex = JFaceTextUtil.getPartialTopIndex(editorTextWidget);
			int editorBottomIndex = JFaceTextUtil.getPartialBottomIndex(editorTextWidget);
			int maximalLines = fEditorViewer.getTextWidget().getClientArea().height
					/ fEditorViewer.getTextWidget().getLineHeight();
			fMinimapTracker.updateMinimap(editorTopIndex, editorBottomIndex, maximalLines, textChanged);
		}

		void install() {
			StyledText editorTextWidget = fEditorViewer.getTextWidget();
			fScaledFonts = new HashMap<>();
			// Compute scaled font
			Font scaledFont = getScaledFont(editorTextWidget.getFont());
			fMinimapTextWidget.setFont(scaledFont);
			// track changed content of styled text of the editor
			editorTextWidget.getContent().addTextChangeListener(this);
			// track changed styles of styled text of the editor
			fMinimapTextWidget.setBackground(editorTextWidget.getBackground());
			fMinimapTextWidget.setForeground(editorTextWidget.getForeground());
			if (fEditorViewer instanceof ITextViewerExtension4) {
				((ITextViewerExtension4) fEditorViewer).addTextPresentationListener(this);
			}
			fEditorViewer.addTextInputListener(this);
			// track changed of vertical bar scroll to update highlight
			// Viewport.
			fEditorViewer.addViewportListener(this);
			editorTextWidget.addControlListener(this);
			synchTextAndStyles();
		}

		void synchTextAndStyles() {
			synchText();
			synchStyles();
		}

		private void synchStyles() {
			StyledText editorTextWidget = fEditorViewer.getTextWidget();
			StyleRange[] ranges = editorTextWidget.getStyleRanges();
			if (ranges != null) {
				for (StyleRange range : ranges) {
					updateStyle(range);
				}
			}
			fMinimapTextWidget.setStyleRanges(ranges);
		}

		private void synchText() {
			StyledText editorTextWidget = fEditorViewer.getTextWidget();
			fMinimapTextWidget.setText(editorTextWidget.getText());
		}

		void uninstall() {
			StyledText editorTextWidget = fEditorViewer.getTextWidget();
			// untrack changed content of styled text of the editor
			if (editorTextWidget.getContent() != null) {
				editorTextWidget.getContent().removeTextChangeListener(this);
			}
			// untrack changed styles of styled text of the editor
			if (fEditorViewer instanceof ITextViewerExtension4) {
				((ITextViewerExtension4) fEditorViewer).removeTextPresentationListener(this);
			}
			fEditorViewer.removeTextInputListener(this);
			// track changed of vertical bar scroll to update highlight
			// Viewport.
			fEditorViewer.removeViewportListener(this);
			editorTextWidget.removeControlListener(this);
			fScaledFonts.values().forEach(Font::dispose);
		}
	}

	/**
	 * Minimap tracker.
	 *
	 */
	class MinimapTracker implements CaretListener, SelectionListener, PaintListener, MouseWheelListener {

		private static final int NB_LINES_SCROLL = 10;

		private int fEditorTopIndex;

		private int fEditorBottomIndex;

		private int fTopIndexY;

		private int fBottomIndexY;

		private int fMinimalHeight;

		private int fMaximalLines;

		private boolean fTextChanging;

		@Override
		public void caretMoved(CaretEvent event) {
			if (fTextChanging) {
				// When editor content changed, it updates the minimap styled
				// text content which update caret.
				// In this case, ignore it to avoid set the top index of the
				// editor viewer.
				return;
			}
			updateEditorTopIndex();
		}

		private void updateEditorTopIndex() {
			int caretOffset = fMinimapTextWidget.getCaretOffset();
			int lineAtOffset = fMinimapTextWidget.getLineAtOffset(caretOffset);
			int newTopIndex = lineAtOffset;
			if (fEditorViewer instanceof ITextViewerExtension5) {
				// adjust offset according folded content
				newTopIndex = ((ITextViewerExtension5) fEditorViewer).widgetLine2ModelLine(lineAtOffset);
			}
			fEditorViewer.setTopIndex(newTopIndex);
		}

		void updateMinimap(int editorTopIndex, int editorBottomIndex, int maximalLines, boolean textChanged) {
			if (editorTopIndex != fEditorTopIndex || editorBottomIndex != fEditorBottomIndex
					|| maximalLines != fMaximalLines || textChanged) {
				fEditorTopIndex = editorTopIndex;
				fEditorBottomIndex = editorBottomIndex;
				fMaximalLines = maximalLines;
				// Update the position of minimap styled text
				fMinimapTextWidget.setRedraw(false);
				int newMinimapTopIndex = editorTopIndex;
				if (editorTopIndex != 0
						&& editorBottomIndex != fMinimapTextWidget.getLineAtOffset(fMinimapTextWidget.getCharCount())) {
					// center the draw of square of editor client area
					int minimapTopIndex = JFaceTextUtil.getPartialTopIndex(fMinimapTextWidget);
					int minimapBottomIndex = JFaceTextUtil.getPartialBottomIndex(fMinimapTextWidget);
					int minimapVisibleLineCount = minimapBottomIndex - minimapTopIndex;
					int editorVisibleLineCount = editorBottomIndex - editorTopIndex;
					newMinimapTopIndex = Math.max(0,
							editorTopIndex + editorVisibleLineCount - minimapVisibleLineCount / 2);
				}
				fMinimapTextWidget.setTopIndex(newMinimapTopIndex);
				fTopIndexY = fMinimapTextWidget.getLinePixel(fEditorTopIndex);
				// "fEditorBottomIndex + 1" because fEditorBottomIndex is START
				// of last line ... we want to include it
				fBottomIndexY = fMinimapTextWidget.getLinePixel(fEditorBottomIndex + 1);
				// to avoid that the highlight viewport shrinks to the text
				// height in case all content is visible in the editor,
				// calculate a minimal height based on the number of lines
				// maximal shown and the line height in the minimap
				fMinimalHeight = maximalLines > fMinimapTextWidget.getLineCount()
						? maximalLines * fMinimapTextWidget.getLineHeight()
						: 0;
				fMinimapTextWidget.setRedraw(true);
			}
		}

		void replaceTextRange(TextChangingEvent event) {
			fTextChanging = true;
			int start = event.start;
			int length = event.replaceCharCount;
			String text = event.newText;
			if (event.newLineCount > 0 || event.replaceLineCount > 0) {
				Rectangle clientArea = fMinimapTextWidget.getClientArea();
				fMinimapTextWidget.setRedraw(false);
				fMinimapTextWidget.replaceTextRange(start, length, text);
				fMinimapTextWidget.redraw(0, fTopIndexY, clientArea.width, clientArea.height, false);
				fMinimapTextWidget.setRedraw(true);
			} else {
				fMinimapTextWidget.replaceTextRange(start, length, text);
			}
			fTextChanging = false;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Do nothing
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// hack to avoid drawing selection of styled text while mouse move
			// of highlight viewport
			fMinimapTextWidget.setSelection(fMinimapTextWidget.getCaretOffset());
		}

		@Override
		public void paintControl(PaintEvent event) {
			GC gc = event.gc;
			int clientAreaWidth = fMinimapTextWidget.getClientArea().width;
			gc.setBackground(fMinimapTextWidget.getSelectionBackground());
			Rectangle rect = new Rectangle(0, fTopIndexY, clientAreaWidth,
					Math.max(fBottomIndexY - fTopIndexY, fMinimalHeight));
			gc.drawRectangle(rect.x, rect.y, Math.max(1, rect.width - 1), Math.max(1, rect.height - 1));
			gc.setAdvanced(true);
			if (gc.getAdvanced()) {
				gc.setAlpha(20);
				gc.fillRectangle(rect);
				gc.setAdvanced(false);
			}
		}

		@Override
		public void mouseScrolled(MouseEvent e) {
			int caretOffset = fMinimapTextWidget.getOffsetAtPoint(new Point(0, fTopIndexY));
			int lineIndex = fMinimapTextWidget.getLineAtOffset(caretOffset);
			if (e.count > 0) {
				lineIndex = Math.max(0, lineIndex - NB_LINES_SCROLL);
			} else {
				lineIndex = Math.min(fMinimapTextWidget.getCharCount(), lineIndex + NB_LINES_SCROLL);
			}
			caretOffset = fMinimapTextWidget.getOffsetAtLine(lineIndex);
			fMinimapTextWidget.setCaretOffset(caretOffset);
		}

		void install() {
			fMinimapTextWidget.addCaretListener(this);
			fMinimapTextWidget.addSelectionListener(this);
			fMinimapTextWidget.addPaintListener(this);
			fMinimapTextWidget.addMouseWheelListener(this);
		}

		void uninstall() {
			if (!fMinimapTextWidget.isDisposed()) {
				fMinimapTextWidget.removeCaretListener(this);
				fMinimapTextWidget.removeSelectionListener(this);
				fMinimapTextWidget.removePaintListener(this);
				fMinimapTextWidget.removeMouseWheelListener(this);
			}
		}

	}

	private final EditorTracker fEditorTracker;

	private final MinimapTracker fMinimapTracker;

	public MinimapWidget(Composite parent, ITextViewer viewer) {
		fEditorViewer = viewer;

		// Create minimap styled text
		fMinimapTextWidget = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY);
		fMinimapTextWidget.setEditable(false);
		fMinimapTextWidget.setCursor(fMinimapTextWidget.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));

		// Initialize trackers
		fEditorTracker = new EditorTracker();
		fMinimapTracker = new MinimapTracker();
	}

	/**
	 * Install minimap widget.
	 */
	public void install() {
		fEditorTracker.install();
		fMinimapTracker.install();
	}

	public void uninstall() {
		fEditorTracker.uninstall();
		fMinimapTracker.uninstall();
	}

	public Control getControl() {
		return fMinimapTextWidget;
	}

	float getScale() {
		return SCALE;
	}
}
