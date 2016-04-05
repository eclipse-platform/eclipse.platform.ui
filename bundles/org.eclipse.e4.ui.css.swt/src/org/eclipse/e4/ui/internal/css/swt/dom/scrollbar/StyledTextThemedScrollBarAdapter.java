/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt.dom.scrollbar;

import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.osgi.service.log.LogService;

/**
 * An implementation which covers showing themed scrollbars for a StyledText.
 */
public class StyledTextThemedScrollBarAdapter extends AbstractThemedScrollBarAdapter {

	private final int fInitialRightMargin;

	private final int fInitialBottomMargin;

	public StyledTextThemedScrollBarAdapter(StyledText styledText) {
		this(styledText, new ScrollBarSettings());
	}

	private StyledTextThemedScrollBarAdapter(StyledText styledText, IScrollBarSettings scrollBarSettings) {
		super(styledText, new StyledTextHorizontalScrollHandler(styledText, scrollBarSettings),
				new StyledTextVerticalScrollHandler(styledText, scrollBarSettings), scrollBarSettings);

		fInitialRightMargin = styledText.getRightMargin();
		fInitialBottomMargin = styledText.getBottomMargin();
	}

	@Override
	protected IScrollBarPainter createPaintListener() {
		return new StyledTextPaintListener(fHorizontalScrollHandler, fVerticalScrollHandler, fScrollBarSettings,
				fInitialBottomMargin, fInitialRightMargin);
	}

	@Override
	protected Point computeHorizontalAndTopPixel() {
		StyledText styledText = (StyledText) fScrollable;
		return new Point(styledText.getHorizontalPixel(), styledText.getTopPixel());
	}

	/**
	 * Ideally this whole class wouldn't be needed (i.e.: if we knew when the
	 * scroll max/selection changed), but unfortunately, these notifications
	 * aren't reliable, so, this class is used to poll such a change when the
	 * text on the StyledText changes.
	 */
	static abstract class AbstractStyledTextScrollHandler extends AbstractScrollHandler
	implements ModifyListener, TextChangeListener {

		private final StyledText fStyledText;
		private AbstractThemedScrollBarAdapter fAbstractThemedScrollBarAdapter;
		private StyledTextContent fTextContent;
		private int fLastMax;
		private int fLastSelection;
		private int fCheckedTimes;

		protected AbstractStyledTextScrollHandler(StyledText styledText, ScrollBar scrollBar,
				IScrollBarSettings scrollBarSettings) {
			super(scrollBar, scrollBarSettings);
			this.fStyledText = styledText;
			this.fStyledText.setAlwaysShowScrollBars(true);
		}

		@Override
		protected void checkScrollbarInvisible() {
			if (this.fScrollBar == null || this.fScrollBar.isDisposed()
					|| !this.fScrollBarSettings.getScrollBarThemed()) {
				return;
			}
			if (this.fScrollBar.isVisible()) {
				if (fCheckedTimes > 20) {
					// If some client continually tries to make it visible,
					// we'll skip trying to put it off... Note that this
					// will only be fixed when SWT provides an API which
					// allows us to actually override the scrollbar (so that
					// visibility changes affect the themed scrollbar and
					// not the native one).
					return;
				}
				fCheckedTimes++;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (fStyledText.isDisposed()) {
							return;
						}
						if (!fStyledText.getAlwaysShowScrollBars()) {
							// We conflict with the setting to make it
							// visible or invisible
							fStyledText.setAlwaysShowScrollBars(true);
						}
						if (fScrollBar != null && !fScrollBar.isDisposed()) {
							fScrollBar.setVisible(false);
						}
					}
				});
			}
		}

		@Override
		public void install(AbstractThemedScrollBarAdapter abstractThemedScrollBarAdapter) {
			super.install(abstractThemedScrollBarAdapter);
			fStyledText.addModifyListener(this);
			this.fAbstractThemedScrollBarAdapter = abstractThemedScrollBarAdapter;
			fTextContent = fStyledText.getContent();
			fTextContent.addTextChangeListener(this);
			if(fScrollBar != null){
				fLastMax = fScrollBar.getMaximum();
				fLastSelection = fScrollBar.getSelection();
			}
		}

		@Override
		public void uninstall(AbstractThemedScrollBarAdapter abstractThemedScrollBarAdapter, boolean disposing) {
			super.uninstall(abstractThemedScrollBarAdapter, disposing);
			fStyledText.removeModifyListener(this);
			if (fTextContent != null) {
				fTextContent.removeTextChangeListener(this);
				fTextContent = null;
			}
			this.fAbstractThemedScrollBarAdapter = null;
		}

		private void checkNeedUpdate() {
			if(fScrollBar != null){
				if (fLastMax != fScrollBar.getMaximum() || fLastSelection != fScrollBar.getSelection()) {
					this.fAbstractThemedScrollBarAdapter.fPainter.redrawScrollBars();
				}
			}
		}

		@Override
		public void modifyText(ModifyEvent e) {
			checkNeedUpdate();
		}

		@Override
		public void textSet(TextChangedEvent event) {
			checkNeedUpdate();
		}

		@Override
		public void textChanged(TextChangedEvent event) {
			checkNeedUpdate();
		}

		@Override
		public void textChanging(TextChangingEvent event) {

		}

		@Override
		public void paintControl(GC gc, Rectangle currClientArea, Scrollable scrollable) {
			// At each paint, check if the content changed and keep the last
			// max/selection (unfortunately, it doesn't provide a reliable way
			// to listen such changes, so, we must poll it).
			if(fScrollBar != null){
				fLastMax = fScrollBar.getMaximum();
				fLastSelection = fScrollBar.getSelection();
			}

			if (fTextContent != null && fStyledText.getContent() != fTextContent) {
				fTextContent.removeTextChangeListener(this);
				fTextContent = fStyledText.getContent();
				fTextContent.addTextChangeListener(this);
			}
			super.paintControl(gc, currClientArea, scrollable);
		}
	}

	/**
	 * Handles the scroll vertically.
	 */
	static class StyledTextVerticalScrollHandler extends AbstractStyledTextScrollHandler {

		public StyledTextVerticalScrollHandler(StyledText styledText, IScrollBarSettings scrollBarSettings) {
			super(styledText, styledText.getVerticalBar(), scrollBarSettings);
		}

		@Override
		public void setPixel(Scrollable scrollable, int pixel) {
			StyledText styledText = (StyledText) scrollable;
			styledText.setTopPixel(pixel);
		}

		@Override
		protected Rectangle getFullBackgroundRect(Scrollable scrollable, Rectangle currClientArea,
				boolean considerMargins) {
			StyledText styledText = (StyledText) scrollable;
			int lineWidth = getCurrentScrollBarWidth();
			int w = currClientArea.width;
			int h = currClientArea.height;
			if (considerMargins) {
				h -= (styledText.getTopMargin() + styledText.getBottomMargin());
			}
			Rectangle rect = new Rectangle(w - lineWidth, considerMargins ? styledText.getTopMargin() : 0, lineWidth,
					h);
			return rect;
		}

		@Override
		public Rectangle computeProximityRect(Rectangle currClientArea) {
			if (this.fScrollBar == null || !this.getVisible()) {
				return null;
			}
			int lineWidth = getMouseNearScrollScrollBarWidth();
			int w = currClientArea.width;
			int h = currClientArea.height;
			Rectangle rect = new Rectangle(w - lineWidth, 0, lineWidth, h);
			rect.width += 30;
			rect.x -= 15;
			return rect;
		}

		@Override
		protected int getRelevantPositionFromPos(Point styledTextPos) {
			return styledTextPos.y;
		}

		@Override
		public boolean computePositions(Rectangle currClientArea, Scrollable scrollable) {
			fHandleDrawnRect = null;
			if (this.fScrollBar == null || this.fScrollBar.getMaximum() - this.fScrollBar.getMinimum() <= 1
					|| !getVisible() || !this.fScrollBarSettings.getScrollBarThemed()) {
				return false;
			}
			StyledText styledText = (StyledText) scrollable;
			int lineWidth = getCurrentScrollBarWidth();
			int w = currClientArea.width;
			int h = currClientArea.height - (styledText.getTopMargin() + styledText.getBottomMargin());

			this.fScrollBarPositions = new ScrollBarPositions.ScrollBarPositionsVertical(this.fScrollBar.getMinimum(),
					this.fScrollBar.getMaximum(), styledText.getTopPixel(), h, w);
			fHandleDrawnRect = fScrollBarPositions.getHandleDrawRect(lineWidth);
			if (fHandleDrawnRect == null || h <= fHandleDrawnRect.height) {
				return false;
			}
			return true;
		}

		@Override
		public void doPaintControl(GC gc, Rectangle currClientArea, Scrollable scrollable) {
			if (fHandleDrawnRect != null) {
				StyledText styledText = (StyledText) scrollable;
				int lineWidth = getCurrentScrollBarWidth();
				int w = currClientArea.width;
				int h = currClientArea.height - (styledText.getTopMargin() + styledText.getBottomMargin());
				int borderRadius = Math.min(fScrollBarSettings.getScrollBarBorderRadius(), lineWidth);
				// Fill the background (same thing as the
				// getFullBackgroundRect).
				gc.fillRoundRectangle(w - lineWidth, styledText.getTopMargin(), lineWidth, h, borderRadius,
						borderRadius);

				// Fill the foreground.
				Color foreground = gc.getForeground();
				Color background = gc.getBackground();
				gc.setBackground(foreground);
				gc.fillRoundRectangle(fHandleDrawnRect.x, fHandleDrawnRect.y, fHandleDrawnRect.width,
						fHandleDrawnRect.height, borderRadius, borderRadius);
				gc.setBackground(background);
			}
		}
	}

	/**
	 * Handles the scroll horizontally.
	 */
	/* default */ static class StyledTextHorizontalScrollHandler extends AbstractStyledTextScrollHandler {

		public StyledTextHorizontalScrollHandler(StyledText styledText, IScrollBarSettings scrollBarSettings) {
			super(styledText, styledText.getHorizontalBar(), scrollBarSettings);
		}

		@Override
		public void setPixel(Scrollable scrollable, int pixel) {
			StyledText styledText = (StyledText) scrollable;
			styledText.setHorizontalPixel(pixel);
		}

		@Override
		protected Rectangle getFullBackgroundRect(Scrollable scrollable, Rectangle currClientArea,
				boolean considerMargins) {
			StyledText styledText = (StyledText) scrollable;
			int lineWidth = getCurrentScrollBarWidth();
			int w = currClientArea.width;
			int h = currClientArea.height;
			if (considerMargins) {
				w -= (styledText.getLeftMargin() + styledText.getRightMargin());
			}
			Rectangle rect = new Rectangle(considerMargins ? styledText.getLeftMargin() : 0, h - lineWidth, w,
					lineWidth);
			return rect;
		}

		@Override
		public Rectangle computeProximityRect(Rectangle currClientArea) {
			if (this.fScrollBar == null || !this.getVisible()) {
				return null;
			}
			int lineWidth = getMouseNearScrollScrollBarWidth();
			int w = currClientArea.width;
			int h = currClientArea.height;
			Rectangle rect = new Rectangle(0, h - lineWidth, w, lineWidth);
			rect.height += 30;
			rect.y -= 15;
			return rect;
		}

		@Override
		protected int getRelevantPositionFromPos(Point styledTextPos) {
			return styledTextPos.x;
		}

		@Override
		public boolean computePositions(Rectangle currClientArea, Scrollable scrollable) {
			fHandleDrawnRect = null;

			if (this.fScrollBar == null || this.fScrollBar.getMaximum() - this.fScrollBar.getMinimum() <= 1
					|| !getVisible() || !this.fScrollBarSettings.getScrollBarThemed()) {
				return false;
			}
			StyledText styledText = (StyledText) scrollable;
			int lineWidth = getCurrentScrollBarWidth();
			int w = currClientArea.width - (styledText.getLeftMargin() + styledText.getRightMargin());
			int h = currClientArea.height;

			fScrollBarPositions = new ScrollBarPositions.ScrollBarPositionsHorizontal(this.fScrollBar.getMinimum(),
					this.fScrollBar.getMaximum(), styledText.getHorizontalPixel(), h, w);
			fHandleDrawnRect = fScrollBarPositions.getHandleDrawRect(lineWidth);
			if (fHandleDrawnRect == null || w <= fHandleDrawnRect.width) {
				return false;
			}
			return true;
		}

		@Override
		public void doPaintControl(GC gc, Rectangle currClientArea, Scrollable scrollable) {
			if (fHandleDrawnRect != null) {
				StyledText styledText = (StyledText) scrollable;
				int lineWidth = getCurrentScrollBarWidth();
				int w = currClientArea.width - (styledText.getLeftMargin() + styledText.getRightMargin());
				int h = currClientArea.height;
				int borderRadius = Math.min(fScrollBarSettings.getScrollBarBorderRadius(), lineWidth);

				// Fill the background (same thing as the
				// getFullBackgroundRect).
				gc.fillRoundRectangle(styledText.getLeftMargin(), h - lineWidth, w, lineWidth, borderRadius,
						borderRadius);

				// Fill the foreground.
				Color foreground = gc.getForeground();
				Color background = gc.getBackground();
				gc.setBackground(foreground);
				gc.fillRoundRectangle(fHandleDrawnRect.x, fHandleDrawnRect.y, fHandleDrawnRect.width,
						fHandleDrawnRect.height, borderRadius, borderRadius);
				gc.setBackground(background);
			}
		}
	}

	/**
	 * Paints the scrolls as needed (using internal handlers).
	 */
	public static class StyledTextPaintListener implements IScrollBarPainter {

		private final int fInitialBottomMargin;
		private final int fInitialRightMargin;
		private StyledText fStyledText;
		private boolean fInDraw;
		private Rectangle fCurrClientArea;
		private AbstractScrollHandler fHorizontalScrollHandler;
		private AbstractScrollHandler fVerticalScrollHandler;
		private IScrollBarSettings fScrollBarSettings;
		private Rectangle fLastHorizontalHandleRect;
		private Rectangle fLastVerticalHandleRect;

		public StyledTextPaintListener(AbstractScrollHandler horizontalScrollHandler,
				AbstractScrollHandler verticalScrollHandler, IScrollBarSettings colorProvider, int initialBottomMargin,
				int initialRightMargin) {
			this.fHorizontalScrollHandler = horizontalScrollHandler;
			this.fVerticalScrollHandler = verticalScrollHandler;
			this.fScrollBarSettings = colorProvider;
			this.fInitialBottomMargin = initialBottomMargin;
			this.fInitialRightMargin = initialRightMargin;
		}

		@Override
		public void install(Scrollable scrollable) {
			fStyledText = (StyledText) scrollable;
		}

		@Override
		public void uninstall() {
			fStyledText = null;
			fCurrClientArea = null;
		}

		@SuppressWarnings("unused")
		@Override
		public void paintControl(PaintEvent e) {
			if (fInDraw || fStyledText == null || fStyledText.isDisposed()) {
				return;
			}
			try {
				fInDraw = true;

				boolean clientAreaChanged = clientAreaChangedFromLastCall();
				int charCount = fStyledText.getCharCount();
				if (charCount <= 1) {
					return;
				}
				if (fCurrClientArea == null
						|| fCurrClientArea.width < fVerticalScrollHandler.getMouseNearScrollScrollBarWidth()
						|| fCurrClientArea.height < fHorizontalScrollHandler.getMouseNearScrollScrollBarWidth()) {
					return;
				}

				boolean drawHorizontal = fHorizontalScrollHandler.computePositions(fCurrClientArea, fStyledText);
				boolean drawVertical = fVerticalScrollHandler.computePositions(fCurrClientArea, fStyledText);

				if (!drawHorizontal && !drawVertical) {
					return;
				}
				fixMargins(drawHorizontal, drawVertical);

				try (AutoCloseable temp = configGC(e.gc)) {
					Rectangle clipping = e.gc.getClipping();
					boolean redrawAsync = false;

					if (drawHorizontal) {
						Rectangle handleRect = fHorizontalScrollHandler.getHandleRect();
						if (!handleRect.equals(fLastHorizontalHandleRect)) {
							if (clipping.intersection(handleRect).height != handleRect.height) {
								// Note: fixing the clipping area does not work.
								// We have to ask for a redraw of the component!
								redrawAsync = true;
							}
						}

						fLastHorizontalHandleRect = handleRect;
						fHorizontalScrollHandler.paintControl(e.gc, fCurrClientArea, fStyledText);
					}
					if (drawVertical) {
						Rectangle handleRect = fVerticalScrollHandler.getHandleRect();
						if (!handleRect.equals(fLastVerticalHandleRect)) {
							if (clipping.intersection(handleRect).width != handleRect.width) {
								// Note: fixing the clipping area does not work.
								// We have to ask for a redraw of the component!
								redrawAsync = true;
							}
						}
						fLastVerticalHandleRect = handleRect;
						fVerticalScrollHandler.paintControl(e.gc, fCurrClientArea, fStyledText);
					}
					if (redrawAsync) {
						redrawAsync();
					}
				}
			} catch (Exception e1) {
				CSSActivator.getDefault().log(LogService.LOG_ERROR, "Error painting scrollbar", e1);
			} finally {
				fInDraw = false;
			}
		}

		/**
		 * Asynchronously asks for a redraw of the whole StyledText.
		 */
		private void redrawAsync() {
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (fStyledText != null && !fStyledText.isDisposed()) {
						fStyledText.redraw();
					}
				}
			});
		}

		/**
		 * Fixes the editor margins. The scrollbars always have to be drawn in
		 * the editor margins (to avoid having the cursor over the scrollbar).
		 *
		 * @param drawHorizontal
		 *            Whether the horizontal scrollbar will be drawn.
		 * @param drawVertical
		 *            Whether the vertical scrollbar will be drawn.
		 * @return true if the margins have to be fixed before the actual
		 *         drawing and false otherwise.
		 */
		private boolean fixMargins(boolean drawHorizontal, boolean drawVertical) {
			int rightMargin;
			if (!drawVertical) {
				rightMargin = fInitialRightMargin;
			} else {
				int verticalLineWidth = fVerticalScrollHandler.getCurrentScrollBarWidth();
				rightMargin = fInitialRightMargin < verticalLineWidth ? verticalLineWidth : fInitialRightMargin;
				if (fVerticalScrollHandler.fScrollBar == null || !fVerticalScrollHandler.getVisible()) {
					rightMargin = fStyledText.getRightMargin();
				}
			}

			int bottomMargin;
			if (!drawHorizontal) {
				bottomMargin = fInitialBottomMargin;
			} else {
				int horizontalLineWidth = fHorizontalScrollHandler.getCurrentScrollBarWidth();
				bottomMargin = fInitialBottomMargin < horizontalLineWidth ? horizontalLineWidth : fInitialBottomMargin;
				if (fHorizontalScrollHandler.fScrollBar == null || !fHorizontalScrollHandler.getVisible()) {
					bottomMargin = fStyledText.getBottomMargin();
				}
			}

			if (fStyledText.getRightMargin() != rightMargin || fStyledText.getBottomMargin() != bottomMargin) {
				final int applyRightMargin = rightMargin;
				final int applyBottomMargin = bottomMargin;
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (fStyledText != null && !fStyledText.isDisposed()) {
							fStyledText.setMargins(fStyledText.getLeftMargin(), fStyledText.getTopMargin(),
									applyRightMargin, applyBottomMargin);
						}
					}
				});
				return true;
			}
			return false;
		}

		/**
		 * Configures the GC with the parameters needed for drawing.
		 *
		 * @param gc
		 * @return an AutoCloseable (to be used in a try() statement) which will
		 *         restore the GC with the previous values.
		 */
		private AutoCloseable configGC(final GC gc) {
			final int oldLineStyle = gc.getLineStyle();
			final int oldAlpha = gc.getAlpha();

			final Color oldForeground = gc.getForeground();
			final Color oldBackground = gc.getBackground();
			final int oldLineWidth = gc.getLineWidth();
			final int oldAntialias = gc.getAntialias();

			Color foreground = fScrollBarSettings.getForegroundColor();
			if (foreground != null) {
				gc.setForeground(foreground);
			}
			Color background = fScrollBarSettings.getBackgroundColor();
			if (background != null) {
				gc.setBackground(background);
			}
			gc.setLineStyle(SWT.LINE_SOLID);
			gc.setAntialias(SWT.ON);
			gc.setLineWidth(1);
			return new AutoCloseable() {

				@Override
				public void close() throws Exception {
					gc.setForeground(oldForeground);
					gc.setBackground(oldBackground);
					gc.setAlpha(oldAlpha);
					gc.setLineStyle(oldLineStyle);
					gc.setLineWidth(oldLineWidth);
					gc.setAntialias(oldAntialias);
				}
			};
		}

		/**
		 * @return true if the client are changed from the last time this method
		 *         was called (and false otherwise). Has a side effect of
		 *         updating fCurrClientArea.
		 */
		private boolean clientAreaChangedFromLastCall() {
			Rectangle clientArea = fStyledText.getClientArea();
			if (fCurrClientArea == null || !fCurrClientArea.equals(clientArea)) {
				fCurrClientArea = clientArea;
				return true;
			}
			return false;
		}

		@Override
		public void redrawScrollBars() {
			if (Display.getCurrent() != null && fStyledText != null) {
				if (!fStyledText.isDisposed() && fStyledText.isVisible()) {
					Rectangle clientArea = fStyledText.getClientArea();
					fStyledText.redraw(clientArea.x, clientArea.y, clientArea.width, clientArea.height, false);
				}
			}
		}
	}

	/**
	 * May return null if a StyledTextThemedScrollBarAdapter is already set in
	 * the data.
	 *
	 * @param styledText
	 *            the StyledText for which the adapter is being requested.
	 * @return the adapter or null if the data is already set with a
	 *         non-compatible instance.
	 */
	public static StyledTextThemedScrollBarAdapter getScrollbarAdapter(StyledText styledText) {
		if (styledText.getData("StyledTextThemedScrollBarAdapter") == null) { //$NON-NLS-1$
			StyledTextThemedScrollBarAdapter scrollbarOnlyWhenNeeded = new StyledTextThemedScrollBarAdapter(styledText);
			styledText.setData("StyledTextThemedScrollBarAdapter", scrollbarOnlyWhenNeeded); //$NON-NLS-1$
			return scrollbarOnlyWhenNeeded;
		}
		Object data = styledText.getData("StyledTextThemedScrollBarAdapter"); //$NON-NLS-1$
		if (data instanceof StyledTextThemedScrollBarAdapter) {
			return (StyledTextThemedScrollBarAdapter) data;
		}
		return null;
	}

}