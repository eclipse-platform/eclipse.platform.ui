/*******************************************************************************
 * Copyright (c) 2010, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fabio Zadrozny - Bug 465711
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 497586
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 506540
 *     Mike Marchand <mmarchand@cranksoftware.com> - Bug 538740
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.lang.reflect.Field;
import java.util.Objects;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.internal.css.swt.ICTabRendering;
import org.eclipse.e4.ui.internal.workbench.PartStackUtil;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

@SuppressWarnings("restriction")
public class CTabRendering extends CTabFolderRenderer implements ICTabRendering, IPreferenceChangeListener {

	/**
	 * The preference qualifier.
	 */
	private static final String PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT = "org.eclipse.e4.ui.workbench.renderers.swt"; //$NON-NLS-1$

	/**
	 * A named preference for setting CTabFolder's to be rendered with rounded
	 * corners
	 * <p>
	 * The default value for this preference is: <code>false</code> (render
	 * CTabFolder's with square corners)
	 * </p>
	 */
	public static final String USE_ROUND_TABS = "USE_ROUND_TABS"; //$NON-NLS-1$

	/**
	 * Default value for "use round tabs" preference
	 */
	public static final boolean USE_ROUND_TABS_DEFAULT = false;
	/**
	 * A named preference for setting CTabFolder's to be rendered without icons in view areas
	 * <p>
	 * The default value for this preference is: <code>false</code> (render
	 * CTabFolder's with icons)
	 * </p>
	 */
	public static final String HIDE_ICONS_FOR_VIEW_TABS = "HIDE_ICONS_FOR_VIEW_TABS"; //$NON-NLS-1$

	/**
	 * Default value for "hide icons" preference for view tabs
	 */
	public static final boolean HIDE_ICONS_FOR_VIEW_TABS_DEFAULT = false;
	/**
	 * A named preference for setting CTabFolder's to show full text in view areas
	 * <p>
	 * The default value for this preference is: <code>false</code> (render
	 * CTabFolder's without full text)
	 * </p>
	 */
	public static final String SHOW_FULL_TEXT_FOR_VIEW_TABS = "SHOW_FULL_TEXT_FOR_VIEW_TABS"; //$NON-NLS-1$

	/**
	 * Default value for "show full text" preference for view tabs
	 */
	public static final boolean SHOW_FULL_TEXT_FOR_VIEW_TABS_DEFAULT = false;

	private static int MIN_VIEW_CHARS = 1;
	private static int MAX_VIEW_CHARS = Integer.MAX_VALUE;

	// Constants for circle drawing
	static enum CirclePart {
		LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM;

		static CirclePart left(boolean onBottom) {
			if (onBottom) {
				return LEFT_BOTTOM;
			}
			return LEFT_TOP;
		}

		static CirclePart right(boolean onBottom) {
			if (onBottom) {
				return RIGHT_BOTTOM;
			}
			return RIGHT_TOP;
		}

		public boolean isLeft() {
			return this == LEFT_TOP || this == LEFT_BOTTOM;
		}

		public boolean isTop() {
			return this == LEFT_TOP || this == RIGHT_TOP;
		}
	}

	static final int SQUARE_CORNER = 0;

	// keylines
	static final int OUTER_KEYLINE_WIDTH = 1;
	static final int INNER_KEYLINE_WIDTH = 0;
	static final int TOP_KEYLINE_WIDTH = 0;

	// The tab has an outline, it contributes to the trim. See Bug 562183.
	static final int TAB_OUTLINE_WIDTH = 1;

	// Item Constants
	static final int ITEM_TOP_MARGIN = 2;
	static final int ITEM_BOTTOM_MARGIN = 6;
	static final int ITEM_LEFT_MARGIN = 4;
	static final int ITEM_RIGHT_MARGIN = 4;

	static final String E4_TOOLBAR_ACTIVE_IMAGE = "org.eclipse.e4.renderer.toolbar_background_active_image"; //$NON-NLS-1$
	static final String E4_TOOLBAR_INACTIVE_IMAGE = "org.eclipse.e4.renderer.toolbar_background_inactive_image"; //$NON-NLS-1$

	Rectangle rectShape;
	int[] shape;

	Image toolbarActiveImage, toolbarInactiveImage;

	int cornerSize = 0;

	Color outerKeylineColor, innerKeylineColor;
	boolean active;

	Color[] selectedTabFillColors;
	int[] selectedTabFillPercents;

	Color[] unselectedTabsColors;
	int[] unselectedTabsPercents;

	Color tabOutlineColor;

	int paddingLeft = 0, paddingRight = 0, paddingTop = 0, paddingBottom = 0;

	private CTabFolderWrapper parentWrapper;

	private Color hotUnselectedTabsColorBackground;
	private Color selectedTabHighlightColor;
	private boolean drawTabHighlightOnTop = true;


	private boolean drawCustomTabContentBackground;

	public CTabRendering(CTabFolder parent) {
		super(parent);
		parentWrapper = new CTabFolderWrapper(parent);

		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		preferences.addPreferenceChangeListener(this);
		parent.addDisposeListener(e -> preferences.removePreferenceChangeListener(this));

		cornerRadiusPreferenceChanged();
		showFullTextForViewTabsPreferenceChanged();
		hideIconsForViewTabsPreferenceChanged();
	}

	@Override
	public void setUnselectedHotTabsColorBackground(Color color) {
		this.hotUnselectedTabsColorBackground = color;
	}

	@Override
	protected Rectangle computeTrim(int part, int state, int x, int y, int width, int height) {
		boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;
		int borderTop = onBottom ? INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH : TOP_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH;
		int borderBottom = onBottom ? TOP_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH
				: INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH;
		int marginWidth = parent.marginWidth;
		int marginHeight = parent.marginHeight;
		int sideDropWidth = 0;

		// Trim is not affected by the corner size.
		switch (part) {
		case PART_BODY:
			if (state == SWT.FILL) {
				x = -1 - paddingLeft;
				int tabHeight = parent.getTabHeight() + 1;
				y = onBottom ? y - paddingTop - marginHeight - borderTop - TAB_OUTLINE_WIDTH
						: y - paddingTop - marginHeight - tabHeight - borderTop - TAB_OUTLINE_WIDTH;
				width = 2 + paddingLeft + paddingRight;
				height += paddingTop + paddingBottom + TAB_OUTLINE_WIDTH;
				height += tabHeight + borderBottom + borderTop;
			} else {
				x = x - marginWidth - OUTER_KEYLINE_WIDTH - INNER_KEYLINE_WIDTH - sideDropWidth;
				width = width + 2 * OUTER_KEYLINE_WIDTH + 2 * INNER_KEYLINE_WIDTH + 2 * marginWidth + 2 * sideDropWidth;
				int tabHeight = parent.getTabHeight() + 1; // TODO: Figure out
				// what
				// to do about the
				// +1
				// TODO: Fix
				if (parent.getMinimized()) {
					y = onBottom ? y - borderTop - 5 : y - tabHeight - borderTop - 5;
					height = borderTop + borderBottom + tabHeight;
				} else {
					// y = tabFolder.onBottom ? y - marginHeight -
					// highlight_margin
					// - borderTop: y - marginHeight - highlight_header -
					// tabHeight
					// - borderTop;
					y = onBottom ? y - marginHeight - borderTop
							: y - marginHeight - tabHeight - borderTop - TAB_OUTLINE_WIDTH;
					height = height + borderBottom + borderTop + 2 * marginHeight + tabHeight + TAB_OUTLINE_WIDTH;
				}
			}
			break;
		case PART_HEADER:
			x = x - (INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH) - sideDropWidth;
			width = width + 2 * (INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH + sideDropWidth);
			break;
		case PART_BORDER:
			x = x - INNER_KEYLINE_WIDTH - OUTER_KEYLINE_WIDTH - sideDropWidth - ITEM_LEFT_MARGIN;
			width = width + 2 * (INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH + sideDropWidth) + ITEM_RIGHT_MARGIN;
			height += borderTop + borderBottom;
			y -= borderTop;
			break;
		default:
			if (0 <= part && part < parent.getItemCount()) {
				x -= ITEM_LEFT_MARGIN;// - (CORNER_SIZE/2);
				width += ITEM_LEFT_MARGIN + ITEM_RIGHT_MARGIN + 1;
				y -= ITEM_TOP_MARGIN;
				height += ITEM_TOP_MARGIN + ITEM_BOTTOM_MARGIN;
			}
			break;
		}
		return new Rectangle(x, y, width, height);
	}

	@Override
	protected Point computeSize(int part, int state, GC gc, int wHint, int hHint) {
		wHint += paddingLeft + paddingRight;
		hHint += paddingTop + paddingBottom;
		if (0 <= part && part < parent.getItemCount()) {
			gc.setAdvanced(true);
			return super.computeSize(part, state, gc, wHint, hHint);
		}
		return super.computeSize(part, state, gc, wHint, hHint);
	}

	@Override
	protected void draw(int part, int state, Rectangle bounds, GC gc) {

		switch (part) {
		case PART_BACKGROUND:
			if (this.drawCustomTabContentBackground) {
				this.drawCustomBackground(gc, bounds, state);
			} else {
				super.draw(part, state, bounds, gc);
			}
			return;
		case PART_BODY:
			this.drawTabBody(gc, bounds);
			return;
		case PART_HEADER:
			this.drawTabHeader(gc, bounds, state);
			if (cornerSize != SQUARE_CORNER) {
				this.drawCorners(gc, bounds);
			}
			return;
		default:
			if (0 <= part && part < parent.getItemCount()) {
				// Sometimes the clipping is incorrect, see Bug 428697 and Bug 563345
				// Resetting it before draw the tabs prevents draw issues.
				gc.setClipping((Rectangle) null);
				gc.setAdvanced(true);
				if (bounds.width == 0 || bounds.height == 0)
					return;
				if ((state & SWT.SELECTED) != 0) {
					drawSelectedTab(part, gc, bounds);
					state &= ~SWT.BACKGROUND;
					super.draw(part, state, bounds, gc);
				} else {
					drawUnselectedTab(part, gc, bounds, state);
					if ((state & SWT.HOT) == 0 && !active) {
						gc.setAlpha(0x7f);
						state &= ~SWT.BACKGROUND;
						super.draw(part, state, bounds, gc);
						gc.setAlpha(0xff);
					} else {
						state &= ~SWT.BACKGROUND;
						super.draw(part, state, bounds, gc);
					}
				}
				return;
			}
		}
		super.draw(part, state, bounds, gc);
	}

	void drawCorners(GC gc, Rectangle bounds) {
		Color bg = gc.getBackground();
		Color fg = gc.getForeground();
		Color toFill = parent.getParent().getBackground();
		gc.setAlpha(255);
		gc.setBackground(toFill);
		gc.setForeground(toFill);
		int radius = cornerSize / 2 + 1;
		int leftX = bounds.x - 1;
		int topY = bounds.y - 1;
		int rightX = bounds.x + bounds.width;
		int bottomY = bounds.y + bounds.height;
		drawCutout(gc, leftX, topY, radius, CirclePart.LEFT_TOP);
		drawCutout(gc, rightX, topY, radius, CirclePart.RIGHT_TOP);
		drawCutout(gc, leftX, bottomY, radius, CirclePart.LEFT_BOTTOM);
		drawCutout(gc, rightX, bottomY, radius, CirclePart.RIGHT_BOTTOM);
		gc.setBackground(bg);
		gc.setForeground(fg);
	}

	private void drawCutout(GC gc, int x, int y, int radius, CirclePart side) {
		int centerX = x + (side.isLeft() ? radius : -radius);
		int centerY = y + (side.isTop() ? radius : -radius);

		int[] circle = drawCircle(centerX, centerY, radius, side);
		int[] result = new int[circle.length + 2];
		result[0] = x;
		result[1] = y;
		int count = circle.length / 2;
		for (int idx = 0; idx < count; idx++) {
			int destIdx = idx * 2 + 2;
			int srcIdx = (count - 1 - idx) * 2;
			result[destIdx] = circle[srcIdx];
			result[destIdx + 1] = circle[srcIdx + 1];
		}

		gc.fillPolygon(result);
	}

	void drawTabHeader(GC gc, Rectangle bounds, int state) {
		boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;
		// TODO: this needs to be added to computeTrim for HEADER
		int header = 1;
		Rectangle trim = computeTrim(PART_HEADER, state, 0, 0, 0, 0);
		trim.width = bounds.width - trim.width;

		// XXX: The magic numbers need to be cleaned up. See
		// https://bugs.eclipse.org/425777 for details.
		trim.height = (parent.getTabHeight() + (onBottom ? 7 : 4)) - trim.height;

		trim.x = -trim.x;
		trim.y = onBottom ? bounds.height - parent.getTabHeight() - 1 - header : -trim.y;
		draw(PART_BACKGROUND, SWT.NONE, trim, gc);

		if (outerKeylineColor == null)
			outerKeylineColor = gc.getDevice().getSystemColor(SWT.COLOR_BLACK);
		gc.setForeground(outerKeylineColor);

		if (cornerSize == SQUARE_CORNER) {
			gc.drawRectangle(rectShape);
		} else {
			gc.drawPolyline(shape);
		}
	}

	void drawTabBody(GC gc, Rectangle bounds) {
		int marginWidth = parent.marginWidth;
		int marginHeight = parent.marginHeight;
		int delta = INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH + 2 * marginWidth;
		int width = bounds.width - delta;
		int height = Math.max(
				parent.getTabHeight() + INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH,
				bounds.height - INNER_KEYLINE_WIDTH - OUTER_KEYLINE_WIDTH - 2 * marginHeight);

		// Remember for use in header drawing
		if (cornerSize == SQUARE_CORNER) {
			Rectangle rect = new Rectangle(bounds.x, bounds.y, width, height);
			gc.fillRectangle(rect);
			rectShape = rect;
		} else {
			int[] points = new int[1024];
			int index = 0;
			int radius = cornerSize / 2;

			int circX = bounds.x + delta / 2 + radius;
			int circY = bounds.y + radius;

			// Body
			int[] ltt = drawCircle(circX, circY, radius, CirclePart.LEFT_TOP);
			System.arraycopy(ltt, 0, points, index, ltt.length);
			index += ltt.length;

			int[] lbb = drawCircle(circX, circY + height - (radius * 2), radius, CirclePart.LEFT_BOTTOM);
			System.arraycopy(lbb, 0, points, index, lbb.length);
			index += lbb.length;

			int[] rb = drawCircle(circX + width - (radius * 2), circY + height - (radius * 2), radius,
					CirclePart.RIGHT_BOTTOM);
			System.arraycopy(rb, 0, points, index, rb.length);
			index += rb.length;

			int[] rt = drawCircle(circX + width - (radius * 2), circY, radius, CirclePart.RIGHT_TOP);
			System.arraycopy(rt, 0, points, index, rt.length);
			index += rt.length;
			points[index++] = circX;
			points[index++] = circY - radius;

			int[] tempPoints = new int[index];
			System.arraycopy(points, 0, tempPoints, 0, index);
			gc.fillPolygon(tempPoints);

			// Remember for use in header drawing
			shape = tempPoints;
		}

	}

	private int[] computeSquareTabOutline(boolean onBottom, int startX, int endX, int bottomY,
			Rectangle bounds, Point parentSize) {
		int index = 0;
		int outlineY = onBottom ? bottomY + bounds.height : bounds.y;
		int[] points = new int[20];

		int margin = (Objects.equals(outerKeylineColor, tabOutlineColor)
						|| Objects.equals(outerKeylineColor, parent.getBackground())
						? 0
						: 1);

		if (active) {
			points[index++] = margin;
			points[index++] = bottomY;
		}

		points[index++] = startX;
		points[index++] = bottomY;

		points[index++] = startX;
		points[index++] = outlineY;

		points[index++] = endX;
		points[index++] = outlineY;

		points[index++] = endX;
		points[index++] = bottomY;

		if (active) {
			points[index++] = parentSize.x - 1 - margin;
			points[index++] = bottomY;
		}

		int[] tmpPoints = new int[index];
		System.arraycopy(points, 0, tmpPoints, 0, index);

		return tmpPoints;
	}


	private int[] computeRoundTabOutline(int itemIndex, boolean onBottom, int bottomY, Rectangle bounds,
			Point parentSize) {
		int header = 0;
		int width = bounds.width;
		int[] points = new int[1024];
		int index = 0;
		int radius = cornerSize / 2;
		int circX = bounds.x + radius;
		int circY = onBottom ? bounds.y + bounds.height + 1 - header - radius : bounds.y - 1 + radius;
		if (itemIndex == 0 && bounds.x == -computeTrim(CTabFolderRenderer.PART_HEADER, SWT.NONE, 0, 0, 0, 0).x) {
			circX -= 1;
			points[index++] = circX - radius;
			points[index++] = bottomY;

			points[index++] = circX - radius;
			points[index++] = bottomY;
		} else {
			if (active) {
				points[index++] = INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH;
				points[index++] = bottomY;
			}
			points[index++] = bounds.x;
			points[index++] = bottomY;
		}

		int[] ltt = drawCircle(circX, circY, radius, CirclePart.left(onBottom));
		if (!onBottom) {
			mirrorCirclePoints(ltt);
		}
		System.arraycopy(ltt, 0, points, index, ltt.length);
		index += ltt.length;
		int[] rt = drawCircle(circX + width - (radius * 2), circY, radius, CirclePart.right(onBottom));
		if (!onBottom) {
			mirrorCirclePoints(rt);
		}
		System.arraycopy(rt, 0, points, index, rt.length);
		index += rt.length;

		points[index++] = bounds.width + circX - radius;
		points[index++] = bottomY;

		if (active) {
			points[index++] = parentSize.x + INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH;
			points[index++] = bottomY;
		}

		int[] tmpPoints = new int[index];
		System.arraycopy(points, 0, tmpPoints, 0, index);
		return tmpPoints;
	}

	void drawSelectedTab(int itemIndex, GC gc, Rectangle bounds) {
		if (parent.getSingle() && parent.getItem(itemIndex).isShowing())
			return;

		boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;
		int header = 0;
		int bottomY = onBottom ? bounds.y - header : bounds.y + bounds.height;
		int selectionX1, selectionY1, selectionX2, selectionY2;
		int startX, endX;
		int[] tabOutlinePoints = null;
		Point parentSize = parent.getSize();

		gc.setClipping(0, onBottom ? bounds.y - header : bounds.y,
				parentSize.x + INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH,
				bounds.y + bounds.height);// bounds.height

		Pattern backgroundPattern = null;
		if (selectedTabFillColors == null) {
			setSelectedTabFill(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
		}
		if (selectedTabFillColors.length == 1) {
			gc.setBackground(selectedTabFillColors[0]);
			gc.setForeground(selectedTabFillColors[0]);
		} else if (selectedTabFillColors.length == 2) {
			// for now we support the 2-colors gradient for selected tab
			if (!onBottom) {
				backgroundPattern = new Pattern(gc.getDevice(), 0, 0, 0, bounds.height + 1, selectedTabFillColors[0],
						selectedTabFillColors[1]);
			} else {
				backgroundPattern = new Pattern(gc.getDevice(), 0, 0, 0, bounds.height + 1, selectedTabFillColors[1],
						selectedTabFillColors[0]);
			}

			gc.setBackgroundPattern(backgroundPattern);
			gc.setForeground(selectedTabFillColors[1]);
		}

		startX = bounds.x - 1;
		endX = bounds.x + bounds.width;
		selectionX1 = startX + 1;
		selectionY1 = bottomY;
		selectionX2 = endX - 1;
		selectionY2 = bottomY;

		boolean superimposeKeylineOutline = Objects.equals(outerKeylineColor, tabOutlineColor);
		Rectangle outlineBoundsForOutline = new Rectangle( //
				superimposeKeylineOutline ? bounds.x - OUTER_KEYLINE_WIDTH : bounds.x,
				!onBottom && superimposeKeylineOutline ? bounds.y - OUTER_KEYLINE_WIDTH : bounds.y,
				superimposeKeylineOutline ? bounds.width + OUTER_KEYLINE_WIDTH : bounds.width, //
				bounds.height);
		if (cornerSize == SQUARE_CORNER) {
			tabOutlinePoints = computeSquareTabOutline(onBottom, startX, endX, bottomY, outlineBoundsForOutline,
					parentSize);
			outlineBoundsForOutline.height += TAB_OUTLINE_WIDTH; // increase area to fill by outline thickness
			gc.fillRectangle(outlineBoundsForOutline);
		} else {
			tabOutlinePoints = computeRoundTabOutline(itemIndex, onBottom, bottomY, outlineBoundsForOutline,
					parentSize);
			gc.fillPolygon(tabOutlinePoints);
		}

		gc.drawLine(selectionX1, selectionY1, selectionX2, selectionY2);

		if (tabOutlineColor == null)
			tabOutlineColor = gc.getDevice().getSystemColor(SWT.COLOR_BLACK);
		gc.setForeground(tabOutlineColor);

		Color gradientLineTop = null;
		Pattern foregroundPattern = null;
		if (!active && !onBottom) {
			RGB blendColor = gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW).getRGB();
			RGB topGradient = blend(blendColor, tabOutlineColor.getRGB(), 40);
			gradientLineTop = new Color(gc.getDevice(), topGradient);
			foregroundPattern = new Pattern(gc.getDevice(), 0, 0, 0, bounds.height + 1, gradientLineTop,
					gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
			gc.setForegroundPattern(foregroundPattern);
		}

		gc.setClipping((Rectangle) null);

		if (active) {
			if (outerKeylineColor == null)
				outerKeylineColor = gc.getDevice().getSystemColor(SWT.COLOR_RED);
			gc.setForeground(outerKeylineColor);
			if (cornerSize == SQUARE_CORNER) {
				gc.drawRectangle(rectShape);
			} else {
				gc.drawPolyline(shape);
			}
		} else if (!onBottom) {
			gc.drawLine(startX, 0, endX, 0);
		}

		if (selectedTabHighlightColor != null) {
			gc.setBackground(selectedTabHighlightColor);
			boolean highlightOnTop = drawTabHighlightOnTop;
			if (onBottom) {
				highlightOnTop = !highlightOnTop;
			}
			int verticalOffset = highlightOnTop ? 0 : bounds.height - 2;
			int horizontalOffset = itemIndex == 0 || cornerSize == SQUARE_CORNER ? 0 : 1;
			int widthAdjustment = cornerSize == SQUARE_CORNER ? 0 : 1;
			gc.fillRectangle(bounds.x + horizontalOffset, bounds.y + verticalOffset, bounds.width - widthAdjustment, 3);
		}

		if (backgroundPattern != null) {
			backgroundPattern.dispose();
		}
		if (foregroundPattern != null) {
			foregroundPattern.dispose();
		}

		gc.setForeground(tabOutlineColor);
		if (TAB_OUTLINE_WIDTH > 0) {
			gc.drawPolyline(tabOutlinePoints);
		}
	}

	void drawUnselectedTab(int itemIndex, GC gc, Rectangle bounds, int state) {
		if ((state & SWT.HOT) != 0) {
			int header = 0;
			int width = bounds.width;
			boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;
			int bottomY = onBottom ? bounds.y - header : bounds.y + bounds.height;

			// Remember for use in header drawing
			if (cornerSize == SQUARE_CORNER) {
				Color color = hotUnselectedTabsColorBackground;
				if (color == null) {
					// Fallback: if color was not set, use white for highlighting
					// hot tab.
					color = gc.getDevice().getSystemColor(SWT.COLOR_WHITE);
				}
				gc.setBackground(color);

				Rectangle rect = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
				gc.fillRectangle(rect);
			} else {
				int[] points = new int[1024];
				int[] inactive = new int[8];
				int index = 0, inactive_index = 0;
				int radius = cornerSize / 2;
				int circX = bounds.x + radius;
				int circY = onBottom ? bounds.y + bounds.height + 1 - header - radius : bounds.y - 1 + radius;

				int leftIndex = circX;
				if (itemIndex == 0) {
					if (parent.getSelectionIndex() != 0)
						leftIndex -= 1;
					points[index++] = leftIndex - radius;
					points[index++] = bottomY;
				} else {
					points[index++] = bounds.x;
					points[index++] = bottomY;
				}

				if (!active) {
					System.arraycopy(points, 0, inactive, 0, index);
					inactive_index += 2;
				}

				int rightIndex = circX - 1;

				int[] ltt = drawCircle(leftIndex, circY, radius, CirclePart.left(onBottom));
				if (!onBottom) {
					mirrorCirclePoints(ltt);
				}
				System.arraycopy(ltt, 0, points, index, ltt.length);
				index += ltt.length;

				if (!active) {
					System.arraycopy(ltt, 0, inactive, inactive_index, 2);
					inactive_index += 2;
				}

				int[] rt = drawCircle(rightIndex + width - (radius * 2), circY, radius, CirclePart.right(onBottom));
				if (!onBottom) {
					mirrorCirclePoints(rt);
				}
				System.arraycopy(rt, 0, points, index, rt.length);
				index += rt.length;

				if (!active) {
					System.arraycopy(rt, rt.length - 4, inactive, inactive_index, 2);
					inactive[inactive_index] -= 1;
					inactive_index += 2;
				}

				points[index++] = bounds.width + rightIndex - radius;
				points[index++] = bottomY;

				if (!active) {
					System.arraycopy(points, index - 2, inactive, inactive_index, 2);
					inactive[inactive_index] -= 1;
					inactive_index += 2;
				}
				gc.setClipping(points[0], onBottom ? bounds.y - header : bounds.y,
						parent.getSize().x + INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH,
						bounds.y + bounds.height);

				Color color = hotUnselectedTabsColorBackground;
				if (color == null) {
					// Fallback: if color was not set, use white for highlighting
					// hot tab.
					color = gc.getDevice().getSystemColor(SWT.COLOR_WHITE);
				}
				gc.setBackground(color);

				int[] tmpPoints = new int[index];
				System.arraycopy(points, 0, tmpPoints, 0, index);
				gc.fillPolygon(tmpPoints);

				if (tabOutlineColor == null)
					tabOutlineColor = gc.getDevice().getSystemColor(SWT.COLOR_BLACK);
				gc.setForeground(tabOutlineColor);

				if (active) {
					gc.drawPolyline(tmpPoints);
				} else {
					gc.drawLine(inactive[0], inactive[1], inactive[2], inactive[3]);
					gc.drawLine(inactive[4], inactive[5], inactive[6], inactive[7]);
				}
				gc.setClipping((Rectangle) null);
			}

			// gc.setForeground(outerKeyline);
			// gc.drawPolyline(shape);
		}
	}

	private static void mirrorCirclePoints(int[] circle) {
		for (int i = 0; i < circle.length / 2; i += 2) {
			int tmp = circle[i];
			circle[i] = circle[circle.length - i - 2];
			circle[circle.length - i - 2] = tmp;
			tmp = circle[i + 1];
			circle[i + 1] = circle[circle.length - i - 1];
			circle[circle.length - i - 1] = tmp;
		}
	}

	static int[] drawCircle(int xC, int yC, int r, CirclePart circlePart) {
		int x = 0, y = r, u = 1, v = 2 * r - 1, e = 0;
		int[] points = new int[1024];
		int[] pointsMirror = new int[1024];
		int loop = 0;
		int loopMirror = 0;
		while (x < y) {
			loop = drawCirclePoint(loop, xC, yC, points, x, y, circlePart);
			x++;
			e += u;
			u += 2;
			if (v < 2 * e) {
				y--;
				e -= v;
				v -= 2;
			}
			if (x > y)
				break;
			loopMirror = drawCirclePoint(loopMirror, xC, yC, pointsMirror, y, x, circlePart);
			// grow?
			if ((loop + 1) > points.length) {
				int length = points.length * 2;
				int[] newPointTable = new int[length];
				int[] newPointTableMirror = new int[length];
				System.arraycopy(points, 0, newPointTable, 0, points.length);
				points = newPointTable;
				System.arraycopy(pointsMirror, 0, newPointTableMirror, 0, pointsMirror.length);
				pointsMirror = newPointTableMirror;
			}
		}
		int[] finalArray = new int[loop + loopMirror];
		System.arraycopy(points, 0, finalArray, 0, loop);
		for (int i = loopMirror - 1, j = loop; i > 0; i = i - 2, j = j + 2) {
			int tempY = pointsMirror[i];
			int tempX = pointsMirror[i - 1];
			finalArray[j] = tempX;
			finalArray[j + 1] = tempY;
		}
		return finalArray;
	}

	private static int drawCirclePoint(int loop, int xC, int yC, int[] points, int x, int y, CirclePart circlePart) {
		switch (circlePart) {
		case RIGHT_BOTTOM:
			points[loop++] = xC + x;
			points[loop++] = yC + y;
			break;
		case RIGHT_TOP:
			points[loop++] = xC + y;
			points[loop++] = yC - x;
			break;
		case LEFT_TOP:
			points[loop++] = xC - x;
			points[loop++] = yC - y;
			break;
		case LEFT_BOTTOM:
			points[loop++] = xC - y;
			points[loop++] = yC + x;
			break;
		}
		return loop;
	}

	static RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

	static int blend(int v1, int v2, int ratio) {
		int b = (ratio * v1 + (100 - ratio) * v2) / 100;
		return Math.min(255, b);
	}


	public ImageData blur(Image src, int radius, int sigma) {
		float[] kernel = create1DKernel(radius, sigma);

		ImageData imgPixels = src.getImageData();
		int width = imgPixels.width;
		int height = imgPixels.height;

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				RGB rgb = imgPixels.palette.getRGB(imgPixels.getPixel(x, y));
				if (rgb.red == 255 && rgb.green == 255 && rgb.blue == 255) {
					inPixels[offset] = (rgb.red << 16) | (rgb.green << 8) | rgb.blue;
				} else {
					inPixels[offset] = (imgPixels.getAlpha(x, y) << 24) | (rgb.red << 16) | (rgb.green << 8) | rgb.blue;
				}
				offset++;
			}
		}

		convolve(kernel, inPixels, outPixels, width, height, true);
		convolve(kernel, outPixels, inPixels, height, width, true);

		ImageData dst = new ImageData(imgPixels.width, imgPixels.height, 24, new PaletteData(0xff0000, 0xff00, 0xff));

		dst.setPixels(0, 0, inPixels.length, inPixels, 0);
		offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (inPixels[offset] == -1) {
					dst.setAlpha(x, y, 0);
				} else {
					int a = (inPixels[offset] >> 24) & 0xff;
					// if (a < 150) a = 0;
					dst.setAlpha(x, y, a);
				}
				offset++;
			}
		}
		return dst;
	}

	private void convolve(float[] kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha) {
		int kernelWidth = kernel.length;
		int kernelMid = kernelWidth / 2;
		for (int y = 0; y < height; y++) {
			int index = y;
			int currentLine = y * width;
			for (int x = 0; x < width; x++) {
				// do point
				float a = 0, r = 0, g = 0, b = 0;
				for (int k = -kernelMid; k <= kernelMid; k++) {
					float val = kernel[k + kernelMid];
					int xcoord = x + k;
					if (xcoord < 0)
						xcoord = 0;
					if (xcoord >= width)
						xcoord = width - 1;
					int pixel = inPixels[currentLine + xcoord];
					// float alp = ((pixel >> 24) & 0xff);
					a += val * ((pixel >> 24) & 0xff);
					r += val * (((pixel >> 16) & 0xff));
					g += val * (((pixel >> 8) & 0xff));
					b += val * (((pixel) & 0xff));
				}
				int ia = alpha ? clamp((int) (a + 0.5)) : 0xff;
				int ir = clamp((int) (r + 0.5));
				int ig = clamp((int) (g + 0.5));
				int ib = clamp((int) (b + 0.5));
				outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
				index += height;
			}
		}

	}

	private int clamp(int value) {
		if (value > 255)
			return 255;
		if (value < 0)
			return 0;
		return value;
	}

	private float[] create1DKernel(int radius, int sigma) {
		// guideline: 3*sigma should be the radius
		int size = radius * 2 + 1;
		float[] kernel = new float[size];
		int radiusSquare = radius * radius;
		float sigmaSquare = 2 * sigma * sigma;
		float piSigma = 2 * (float) Math.PI * sigma;
		float sqrtSigmaPi2 = (float) Math.sqrt(piSigma);
		int start = size / 2;
		int index = 0;
		float total = 0;
		for (int i = -start; i <= start; i++) {
			float d = i * i;
			if (d > radiusSquare) {
				kernel[index] = 0;
			} else {
				kernel[index] = (float) Math.exp(-(d) / sigmaSquare) / sqrtSigmaPi2;
			}
			total += kernel[index];
			index++;
		}
		for (int i = 0; i < size; i++) {
			kernel[i] /= total;
		}
		return kernel;
	}

	public Rectangle getPadding() {
		return new Rectangle(paddingTop, paddingRight, paddingBottom, paddingLeft);
	}

	public void setPadding(int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		this.paddingTop = paddingTop;
		this.paddingBottom = paddingBottom;
		parent.redraw();
	}

	@Override
	public void setCornerRadius(int radius) {
		cornerSize = (radius < 6) ? 0 : radius;
		parent.redraw();
	}

	@Override
	public void setOuterKeyline(Color color) {
		this.outerKeylineColor = color;
		// TODO: HACK! Should be set based on pseudo-state.
		if (color != null) {
			setActive(!(color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255));
		}
		parent.redraw();
	}

	@Override
	public void setSelectedTabHighlight(Color color) {
		this.selectedTabHighlightColor = color;
		parent.redraw();
	}

	@Override
	public void setSelectedTabFill(Color color) {
		setSelectedTabFill(new Color[] { color }, new int[] { 100 });
	}

	@Override
	public void setSelectedTabFill(Color[] colors, int[] percents) {
		selectedTabFillColors = colors;
		selectedTabFillPercents = percents;
		parent.redraw();
	}

	@Override
	public void setUnselectedTabsColor(Color color) {
		setUnselectedTabsColor(new Color[] { color }, new int[] { 100 });
	}

	@Override
	public void setUnselectedTabsColor(Color[] colors, int[] percents) {
		unselectedTabsColors = colors;
		unselectedTabsPercents = percents;
		parent.redraw();
	}

	@Override
	public void setTabOutline(Color color) {
		this.tabOutlineColor = color;
		parent.redraw();
	}

	@Override
	public void setInnerKeyline(Color color) {
		this.innerKeylineColor = color;
		parent.redraw();
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets whether to use a custom tab background (reusing tab colors and
	 * gradients), or default one from plain CTabFolder (using widget background
	 * color).
	 */
	@Override
	public void setDrawCustomTabContentBackground(boolean drawCustomTabContentBackground) {
		this.drawCustomTabContentBackground = drawCustomTabContentBackground;
	}

	/**
	 * Draws tab content background, deriving the colors from the tab colors.
	 */
	private void drawCustomBackground(GC gc, Rectangle bounds, int state) {
		boolean selected = (state & SWT.SELECTED) != 0;
		boolean vertical = selected ? parentWrapper.isSelectionGradientVertical() : parentWrapper.isGradientVertical();
		Rectangle partHeaderBounds = computeTrim(PART_HEADER, state, bounds.x, bounds.y, bounds.width, bounds.height);

		drawUnselectedTabBackground(gc, partHeaderBounds, state, vertical, parent.getBackground());
		drawSelectedTabBackground(gc, partHeaderBounds, state, vertical, parent.getBackground());
	}

	private void drawUnselectedTabBackground(GC gc, Rectangle partHeaderBounds, int state, boolean vertical,
			Color defaultBackground) {
		if (unselectedTabsColors == null) {
			boolean selected = (state & SWT.SELECTED) != 0;
			unselectedTabsColors = selected ? parentWrapper.getSelectionGradientColors()
					: parentWrapper.getGradientColors();
			unselectedTabsPercents = selected ? parentWrapper.getSelectionGradientPercents()
					: parentWrapper.getGradientPercents();
		}
		if (unselectedTabsColors == null) {
			unselectedTabsColors = new Color[] { gc.getDevice().getSystemColor(SWT.COLOR_WHITE) };
			unselectedTabsPercents = new int[] { 100 };
		}

		drawBackground(gc, partHeaderBounds.x, partHeaderBounds.y - 1, partHeaderBounds.width, partHeaderBounds.height,
				defaultBackground, unselectedTabsColors, unselectedTabsPercents, vertical);
	}

	private void drawSelectedTabBackground(GC gc, Rectangle partHeaderBounds, int state, boolean vertical,
			Color defaultBackground) {
		Color[] colors = selectedTabFillColors;
		int[] percents = selectedTabFillPercents;

		if (colors != null && colors.length == 2) {
			colors = new Color[] { colors[1], colors[1] };
		}
		if (colors == null) {
			boolean selected = (state & SWT.SELECTED) != 0;
			colors = selected ? parentWrapper.getSelectionGradientColors() : parentWrapper.getGradientColors();
			percents = selected ? parentWrapper.getSelectionGradientPercents() : parentWrapper.getGradientPercents();
		}
		if (colors == null) {
			colors = new Color[] { gc.getDevice().getSystemColor(SWT.COLOR_WHITE) };
			percents = new int[] { 100 };
		}

		boolean onBottom = parent.getTabPosition() == SWT.BOTTOM;
		int borderTop = onBottom ? INNER_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH : TOP_KEYLINE_WIDTH + OUTER_KEYLINE_WIDTH;
		Rectangle parentBounds = parent.getBounds();
		int y = (onBottom) ? 0 : partHeaderBounds.y + partHeaderBounds.height - 1;
		int height = (onBottom) ? parentBounds.height - partHeaderBounds.height + 2 * paddingTop + 2 * borderTop
				: parentBounds.height - partHeaderBounds.height;

		drawBackground(gc, partHeaderBounds.x, y, partHeaderBounds.width, height, defaultBackground, colors, percents,
				vertical);
	}

	/*
	 * Copied the relevant parts from the package private
	 * org.eclipse.swt.custom.CTabFolderRenderer.drawBackground(GC, int[], int, int,
	 * int, int, Color, Image, Color[], int[], boolean) method.
	 */
	private void drawBackground(GC gc, int x, int y, int width, int height, Color defaultBackground, Color[] colors,
			int[] percents, boolean vertical) {
		if (colors != null) {
			// draw gradient
			if (colors.length == 1) {
				Color background = colors[0] != null ? colors[0] : defaultBackground;
				gc.setBackground(background);
				gc.fillRectangle(x, y, width, height);
			} else {
				if (vertical) {
					if ((parent.getStyle() & SWT.BOTTOM) != 0) {
						int pos = 0;
						if (percents[percents.length - 1] < 100) {
							pos = (100 - percents[percents.length - 1]) * height / 100;
							gc.setBackground(defaultBackground);
							gc.fillRectangle(x, y, width, pos);
						}
						Color lastColor = colors[colors.length - 1];
						if (lastColor == null)
							lastColor = defaultBackground;
						for (int i = percents.length - 1; i >= 0; i--) {
							gc.setForeground(lastColor);
							lastColor = colors[i];
							if (lastColor == null)
								lastColor = defaultBackground;
							gc.setBackground(lastColor);
							int percentage = i > 0 ? percents[i] - percents[i - 1] : percents[i];
							int gradientHeight = percentage * height / 100;
							gc.fillGradientRectangle(x, y + pos, width, gradientHeight, true);
							pos += gradientHeight;
						}
					} else {
						Color lastColor = colors[0];
						if (lastColor == null)
							lastColor = defaultBackground;
						int pos = 0;
						for (int i = 0; i < percents.length; i++) {
							gc.setForeground(lastColor);
							lastColor = colors[i + 1];
							if (lastColor == null)
								lastColor = defaultBackground;
							gc.setBackground(lastColor);
							int percentage = i > 0 ? percents[i] - percents[i - 1] : percents[i];
							int gradientHeight = percentage * height / 100;
							gc.fillGradientRectangle(x, y + pos, width, gradientHeight, true);
							pos += gradientHeight;
						}
						if (pos < height) {
							gc.setBackground(defaultBackground);
							gc.fillRectangle(x, pos, width, height - pos + 1);
						}
					}
				} else { // horizontal gradient
					y = 0;
					height = parent.getSize().y;
					Color lastColor = colors[0];
					if (lastColor == null)
						lastColor = defaultBackground;
					int pos = 0;
					for (int i = 0; i < percents.length; ++i) {
						gc.setForeground(lastColor);
						lastColor = colors[i + 1];
						if (lastColor == null)
							lastColor = defaultBackground;
						gc.setBackground(lastColor);
						int gradientWidth = (percents[i] * width / 100) - pos;
						gc.fillGradientRectangle(x + pos, y, gradientWidth, height, false);
						pos += gradientWidth;
					}
					if (pos < width) {
						gc.setBackground(defaultBackground);
						gc.fillRectangle(x + pos, y, width - pos, height);
					}
				}
			}
		} else // draw a solid background using default background in shape
		if ((parent.getStyle() & SWT.NO_BACKGROUND) != 0 || !defaultBackground.equals(parent.getBackground())) {
			gc.setBackground(defaultBackground);
			gc.fillRectangle(x, y, width, height);
		}
	}

	private static class CTabFolderWrapper extends ReflectionSupport<CTabFolder> {
		private Field selectionGradientVerticalField;

		private Field gradientVerticalField;

		private Field selectionGradientColorsField;

		private Field selectionGradientPercentsField;

		private Field gradientColorsField;

		private Field gradientPercentsField;

		public CTabFolderWrapper(CTabFolder instance) {
			super(instance);
		}

		public boolean isSelectionGradientVertical() {
			if (selectionGradientVerticalField == null) {
				selectionGradientVerticalField = getField("selectionGradientVertical"); //$NON-NLS-1$
			}
			Boolean result = (Boolean) getFieldValue(selectionGradientVerticalField);
			return result != null ? result : true;
		}

		public boolean isGradientVertical() {
			if (gradientVerticalField == null) {
				gradientVerticalField = getField("gradientVertical"); //$NON-NLS-1$
			}
			Boolean result = (Boolean) getFieldValue(gradientVerticalField);
			return result != null ? result : true;
		}

		public Color[] getSelectionGradientColors() {
			if (selectionGradientColorsField == null) {
				selectionGradientColorsField = getField("selectionGradientColorsField"); //$NON-NLS-1$
			}
			return (Color[]) getFieldValue(selectionGradientColorsField);
		}

		public int[] getSelectionGradientPercents() {
			if (selectionGradientPercentsField == null) {
				selectionGradientPercentsField = getField("selectionGradientPercents"); //$NON-NLS-1$
			}
			return (int[]) getFieldValue(selectionGradientPercentsField);
		}

		public Color[] getGradientColors() {
			if (gradientColorsField == null) {
				gradientColorsField = getField("gradientColors"); //$NON-NLS-1$
			}
			return (Color[]) getFieldValue(gradientColorsField);
		}

		public int[] getGradientPercents() {
			if (gradientPercentsField == null) {
				gradientPercentsField = getField("gradientPercents"); //$NON-NLS-1$
			}
			return (int[]) getFieldValue(gradientPercentsField);
		}
	}

	private static class ReflectionSupport<T> {
		private T instance;

		public ReflectionSupport(T instance) {
			this.instance = instance;
		}

		protected Object getFieldValue(Field field) {
			Object value = null;
			if (field != null) {
				boolean accessible = field.isAccessible();
				try {
					field.setAccessible(true);
					value = field.get(instance);
				} catch (Exception exc) {
					// do nothing
				} finally {
					field.setAccessible(accessible);
				}
			}
			return value;
		}

		protected Field getField(String name) {
			Class<?> cls = instance.getClass();
			while (!cls.equals(Object.class)) {
				try {
					return cls.getDeclaredField(name);
				} catch (Exception exc) {
					cls = cls.getSuperclass();
				}
			}
			return null;
		}
	}

	@Override
	public void setSelectedTabHighlightTop(boolean drawTabHiglightOnTop) {
		this.drawTabHighlightOnTop = drawTabHiglightOnTop;
		parent.redraw();
	}

	private void cornerRadiusPreferenceChanged() {
		boolean useRound = getSwtRendererPreference(USE_ROUND_TABS, USE_ROUND_TABS_DEFAULT);
		setCornerRadius(useRound ? 16 : 0);
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(USE_ROUND_TABS)) {
			cornerRadiusPreferenceChanged();
		} else if (event.getKey().equals(HIDE_ICONS_FOR_VIEW_TABS)) {
			hideIconsForViewTabsPreferenceChanged();
		} else if (event.getKey().equals(SHOW_FULL_TEXT_FOR_VIEW_TABS)) {
			showFullTextForViewTabsPreferenceChanged();
		}
	}

	private void showFullTextForViewTabsPreferenceChanged() {
		boolean showFullText = getSwtRendererPreference(SHOW_FULL_TEXT_FOR_VIEW_TABS,
				SHOW_FULL_TEXT_FOR_VIEW_TABS_DEFAULT);
		if (!isPartOfEditorStack()) {
			if (showFullText) {
				parent.setMinimumCharacters(MAX_VIEW_CHARS);
			} else {
				parent.setMinimumCharacters(MIN_VIEW_CHARS);
			}
			parent.redraw();
		}
	}

	private void hideIconsForViewTabsPreferenceChanged() {
		boolean hideIcons = getSwtRendererPreference(HIDE_ICONS_FOR_VIEW_TABS, HIDE_ICONS_FOR_VIEW_TABS_DEFAULT);
		if (!isPartOfEditorStack()) {
			parent.setSelectedImageVisible(!hideIcons);
			parent.setUnselectedImageVisible(!hideIcons);
			parent.redraw();
		}
	}

	private boolean isPartOfEditorStack() {
		MUIElement element = (MUIElement) parent.getData(AbstractPartRenderer.OWNING_ME);
		return PartStackUtil.isEditorStack(element);
	}

	private boolean getSwtRendererPreference(String prefName, boolean defaultValue) {
		return Platform.getPreferencesService().getBoolean(PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT,
				prefName, defaultValue, null);
	}
}
