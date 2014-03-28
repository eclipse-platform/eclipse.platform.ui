/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSS2Properties;

/**
 * w3c {@link CSS2Properties} implementation.
 */
public class CSS2PropertiesImpl implements CSS2Properties {

	protected Object widget;

	protected CSSEngine engine;

	public CSS2PropertiesImpl(Object widget, CSSEngine engine) {
		this.widget = widget;
		this.engine = engine;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getAzimuth()
	 */
	@Override
	public String getAzimuth() {
		return engine.retrieveCSSProperty(widget, "azimut", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBackground()
	 */
	@Override
	public String getBackground() {
		return engine.retrieveCSSProperty(widget, "background", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBackgroundAttachment()
	 */
	@Override
	public String getBackgroundAttachment() {
		return engine
				.retrieveCSSProperty(widget, "background-attachment", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBackgroundColor()
	 */
	@Override
	public String getBackgroundColor() {
		return engine.retrieveCSSProperty(widget, "background-color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBackgroundImage()
	 */
	@Override
	public String getBackgroundImage() {
		return engine.retrieveCSSProperty(widget, "background-image", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBackgroundPosition()
	 */
	@Override
	public String getBackgroundPosition() {
		return engine.retrieveCSSProperty(widget, "background-position", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBackgroundRepeat()
	 */
	@Override
	public String getBackgroundRepeat() {
		return engine.retrieveCSSProperty(widget, "background-repeat", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorder()
	 */
	@Override
	public String getBorder() {
		return engine.retrieveCSSProperty(widget, "border", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderBottom()
	 */
	@Override
	public String getBorderBottom() {
		return engine.retrieveCSSProperty(widget, "border-bottom", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderBottomColor()
	 */
	@Override
	public String getBorderBottomColor() {
		return engine.retrieveCSSProperty(widget, "border-bottom-color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderBottomStyle()
	 */
	@Override
	public String getBorderBottomStyle() {
		return engine.retrieveCSSProperty(widget, "border-bottom-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderBottomWidth()
	 */
	@Override
	public String getBorderBottomWidth() {
		return engine.retrieveCSSProperty(widget, "border-bottom-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderCollapse()
	 */
	@Override
	public String getBorderCollapse() {
		return engine.retrieveCSSProperty(widget, "border-collapse", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderColor()
	 */
	@Override
	public String getBorderColor() {
		return engine.retrieveCSSProperty(widget, "border-color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderLeft()
	 */
	@Override
	public String getBorderLeft() {
		return engine.retrieveCSSProperty(widget, "border-left", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderLeftColor()
	 */
	@Override
	public String getBorderLeftColor() {
		return engine.retrieveCSSProperty(widget, "border-left-color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderLeftStyle()
	 */
	@Override
	public String getBorderLeftStyle() {
		return engine.retrieveCSSProperty(widget, "border-left-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderLeftWidth()
	 */
	@Override
	public String getBorderLeftWidth() {
		return engine.retrieveCSSProperty(widget, "border-left-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderRight()
	 */
	@Override
	public String getBorderRight() {
		return engine.retrieveCSSProperty(widget, "border-right", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderRightColor()
	 */
	@Override
	public String getBorderRightColor() {
		return engine.retrieveCSSProperty(widget, "border-right-color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderRightStyle()
	 */
	@Override
	public String getBorderRightStyle() {
		return engine.retrieveCSSProperty(widget, "border-right-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderRightWidth()
	 */
	@Override
	public String getBorderRightWidth() {
		return engine.retrieveCSSProperty(widget, "border-right-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderSpacing()
	 */
	@Override
	public String getBorderSpacing() {
		return engine.retrieveCSSProperty(widget, "border-spacing", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderStyle()
	 */
	@Override
	public String getBorderStyle() {
		return engine.retrieveCSSProperty(widget, "border-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderTop()
	 */
	@Override
	public String getBorderTop() {
		return engine.retrieveCSSProperty(widget, "border-top", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderTopColor()
	 */
	@Override
	public String getBorderTopColor() {
		return engine.retrieveCSSProperty(widget, "border-top-color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderTopStyle()
	 */
	@Override
	public String getBorderTopStyle() {
		return engine.retrieveCSSProperty(widget, "border-top-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderTopWidth()
	 */
	@Override
	public String getBorderTopWidth() {
		return engine.retrieveCSSProperty(widget, "border-top-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBorderWidth()
	 */
	@Override
	public String getBorderWidth() {
		return engine.retrieveCSSProperty(widget, "border-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getBottom()
	 */
	@Override
	public String getBottom() {
		return engine.retrieveCSSProperty(widget, "border-bottom", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCaptionSide()
	 */
	@Override
	public String getCaptionSide() {
		return engine.retrieveCSSProperty(widget, "caption-side", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getClear()
	 */
	@Override
	public String getClear() {
		return engine.retrieveCSSProperty(widget, "clear", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getClip()
	 */
	@Override
	public String getClip() {
		return engine.retrieveCSSProperty(widget, "clip", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getColor()
	 */
	@Override
	public String getColor() {
		return engine.retrieveCSSProperty(widget, "color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getContent()
	 */
	@Override
	public String getContent() {
		return engine.retrieveCSSProperty(widget, "content", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCounterIncrement()
	 */
	@Override
	public String getCounterIncrement() {
		return engine.retrieveCSSProperty(widget, "counter-increment", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCounterReset()
	 */
	@Override
	public String getCounterReset() {
		return engine.retrieveCSSProperty(widget, "counter-reset", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCssFloat()
	 */
	@Override
	public String getCssFloat() {
		return engine.retrieveCSSProperty(widget, "float", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCue()
	 */
	@Override
	public String getCue() {
		return engine.retrieveCSSProperty(widget, "cue", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCueAfter()
	 */
	@Override
	public String getCueAfter() {
		return engine.retrieveCSSProperty(widget, "cue-after", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCueBefore()
	 */
	@Override
	public String getCueBefore() {
		return engine.retrieveCSSProperty(widget, "cue-before", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getCursor()
	 */
	@Override
	public String getCursor() {
		return engine.retrieveCSSProperty(widget, "cursor", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getDirection()
	 */
	@Override
	public String getDirection() {
		return engine.retrieveCSSProperty(widget, "direction", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getDisplay()
	 */
	@Override
	public String getDisplay() {
		return engine.retrieveCSSProperty(widget, "display", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getElevation()
	 */
	@Override
	public String getElevation() {
		return engine.retrieveCSSProperty(widget, "elevation", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getEmptyCells()
	 */
	@Override
	public String getEmptyCells() {
		return engine.retrieveCSSProperty(widget, "empty-cells", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFont()
	 */
	@Override
	public String getFont() {
		return engine.retrieveCSSProperty(widget, "font", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFontFamily()
	 */
	@Override
	public String getFontFamily() {
		return engine.retrieveCSSProperty(widget, "font-family", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFontSize()
	 */
	@Override
	public String getFontSize() {
		return engine.retrieveCSSProperty(widget, "font-size", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFontSizeAdjust()
	 */
	@Override
	public String getFontSizeAdjust() {
		return engine.retrieveCSSProperty(widget, "font_size-adjust", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFontStretch()
	 */
	@Override
	public String getFontStretch() {
		return engine.retrieveCSSProperty(widget, "font-stretch", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFontStyle()
	 */
	@Override
	public String getFontStyle() {
		return engine.retrieveCSSProperty(widget, "font-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFontVariant()
	 */
	@Override
	public String getFontVariant() {
		return engine.retrieveCSSProperty(widget, "font-variant", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getFontWeight()
	 */
	@Override
	public String getFontWeight() {
		return engine.retrieveCSSProperty(widget, "font-weight", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getHeight()
	 */
	@Override
	public String getHeight() {
		return engine.retrieveCSSProperty(widget, "height", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getLeft()
	 */
	@Override
	public String getLeft() {
		return engine.retrieveCSSProperty(widget, "left", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getLetterSpacing()
	 */
	@Override
	public String getLetterSpacing() {
		return engine.retrieveCSSProperty(widget, "letter-spacing", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getLineHeight()
	 */
	@Override
	public String getLineHeight() {
		return engine.retrieveCSSProperty(widget, "line-height", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getListStyle()
	 */
	@Override
	public String getListStyle() {
		return engine.retrieveCSSProperty(widget, "list-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getListStyleImage()
	 */
	@Override
	public String getListStyleImage() {
		return engine.retrieveCSSProperty(widget, "list-style-image", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getListStylePosition()
	 */
	@Override
	public String getListStylePosition() {
		return engine.retrieveCSSProperty(widget, "list-style-position", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getListStyleType()
	 */
	@Override
	public String getListStyleType() {
		return engine.retrieveCSSProperty(widget, "list-style-type", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMargin()
	 */
	@Override
	public String getMargin() {
		return engine.retrieveCSSProperty(widget, "margin", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMarginBottom()
	 */
	@Override
	public String getMarginBottom() {
		return engine.retrieveCSSProperty(widget, "margin-bottom", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMarginLeft()
	 */
	@Override
	public String getMarginLeft() {
		return engine.retrieveCSSProperty(widget, "margin-left", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMarginRight()
	 */
	@Override
	public String getMarginRight() {
		return engine.retrieveCSSProperty(widget, "margin-right", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMarginTop()
	 */
	@Override
	public String getMarginTop() {
		return engine.retrieveCSSProperty(widget, "margin-top", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMarkerOffset()
	 */
	@Override
	public String getMarkerOffset() {
		return engine.retrieveCSSProperty(widget, "marker-offset", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMarks()
	 */
	@Override
	public String getMarks() {
		return engine.retrieveCSSProperty(widget, "marks", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMaxHeight()
	 */
	@Override
	public String getMaxHeight() {
		return engine.retrieveCSSProperty(widget, "max-height", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMaxWidth()
	 */
	@Override
	public String getMaxWidth() {
		return engine.retrieveCSSProperty(widget, "max-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMinHeight()
	 */
	@Override
	public String getMinHeight() {
		return engine.retrieveCSSProperty(widget, "min-height", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getMinWidth()
	 */
	@Override
	public String getMinWidth() {
		return engine.retrieveCSSProperty(widget, "min-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getOrphans()
	 */
	@Override
	public String getOrphans() {
		return engine.retrieveCSSProperty(widget, "orphans", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getOutline()
	 */
	@Override
	public String getOutline() {
		return engine.retrieveCSSProperty(widget, "outline", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getOutlineColor()
	 */
	@Override
	public String getOutlineColor() {
		return engine.retrieveCSSProperty(widget, "outline-color", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getOutlineStyle()
	 */
	@Override
	public String getOutlineStyle() {
		return engine.retrieveCSSProperty(widget, "outline-style", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getOutlineWidth()
	 */
	@Override
	public String getOutlineWidth() {
		return engine.retrieveCSSProperty(widget, "outline-width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getOverflow()
	 */
	@Override
	public String getOverflow() {
		return engine.retrieveCSSProperty(widget, "overflow", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPadding()
	 */
	@Override
	public String getPadding() {
		return engine.retrieveCSSProperty(widget, "padding", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPaddingBottom()
	 */
	@Override
	public String getPaddingBottom() {
		return engine.retrieveCSSProperty(widget, "padding-bottom", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPaddingLeft()
	 */
	@Override
	public String getPaddingLeft() {
		return engine.retrieveCSSProperty(widget, "padding-left", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPaddingRight()
	 */
	@Override
	public String getPaddingRight() {
		return engine.retrieveCSSProperty(widget, "padding-right", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPaddingTop()
	 */
	@Override
	public String getPaddingTop() {
		return engine.retrieveCSSProperty(widget, "padding-top", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPage()
	 */
	@Override
	public String getPage() {
		return engine.retrieveCSSProperty(widget, "page", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPageBreakAfter()
	 */
	@Override
	public String getPageBreakAfter() {
		return engine.retrieveCSSProperty(widget, "page-break-after", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPageBreakBefore()
	 */
	@Override
	public String getPageBreakBefore() {
		return engine.retrieveCSSProperty(widget, "page-break-before", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPageBreakInside()
	 */
	@Override
	public String getPageBreakInside() {
		return engine.retrieveCSSProperty(widget, "page-break-inside", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPause()
	 */
	@Override
	public String getPause() {
		return engine.retrieveCSSProperty(widget, "pause", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPauseAfter()
	 */
	@Override
	public String getPauseAfter() {
		return engine.retrieveCSSProperty(widget, "pause-after", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPauseBefore()
	 */
	@Override
	public String getPauseBefore() {
		return engine.retrieveCSSProperty(widget, "pause-before", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPitch()
	 */
	@Override
	public String getPitch() {
		return engine.retrieveCSSProperty(widget, "pitch", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPitchRange()
	 */
	@Override
	public String getPitchRange() {
		return engine.retrieveCSSProperty(widget, "pitch-range", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPlayDuring()
	 */
	@Override
	public String getPlayDuring() {
		return engine.retrieveCSSProperty(widget, "play-during", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getPosition()
	 */
	@Override
	public String getPosition() {
		return engine.retrieveCSSProperty(widget, "position", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getQuotes()
	 */
	@Override
	public String getQuotes() {
		return engine.retrieveCSSProperty(widget, "quotes", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getRichness()
	 */
	@Override
	public String getRichness() {
		return engine.retrieveCSSProperty(widget, "richness", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getRight()
	 */
	@Override
	public String getRight() {
		return engine.retrieveCSSProperty(widget, "right", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getSize()
	 */
	@Override
	public String getSize() {
		return engine.retrieveCSSProperty(widget, "size", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getSpeak()
	 */
	@Override
	public String getSpeak() {
		return engine.retrieveCSSProperty(widget, "speak", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getSpeakHeader()
	 */
	@Override
	public String getSpeakHeader() {
		return engine.retrieveCSSProperty(widget, "speak-header", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getSpeakNumeral()
	 */
	@Override
	public String getSpeakNumeral() {
		return engine.retrieveCSSProperty(widget, "speak-numeral", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getSpeakPunctuation()
	 */
	@Override
	public String getSpeakPunctuation() {
		return engine.retrieveCSSProperty(widget, "speak-punctuation", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getSpeechRate()
	 */
	@Override
	public String getSpeechRate() {
		return engine.retrieveCSSProperty(widget, "speech-rate", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getStress()
	 */
	@Override
	public String getStress() {
		return engine.retrieveCSSProperty(widget, "stress", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getTableLayout()
	 */
	@Override
	public String getTableLayout() {
		return engine.retrieveCSSProperty(widget, "table-layout", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getTextAlign()
	 */
	@Override
	public String getTextAlign() {
		return engine.retrieveCSSProperty(widget, "text-align", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getTextDecoration()
	 */
	@Override
	public String getTextDecoration() {
		return engine.retrieveCSSProperty(widget, "text-decoration", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getTextIndent()
	 */
	@Override
	public String getTextIndent() {
		return engine.retrieveCSSProperty(widget, "text-indent", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getTextShadow()
	 */
	@Override
	public String getTextShadow() {
		return engine.retrieveCSSProperty(widget, "text-shadow", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getTextTransform()
	 */
	@Override
	public String getTextTransform() {
		return engine.retrieveCSSProperty(widget, "text-transform", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getTop()
	 */
	@Override
	public String getTop() {
		return engine.retrieveCSSProperty(widget, "top", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getUnicodeBidi()
	 */
	@Override
	public String getUnicodeBidi() {
		return engine.retrieveCSSProperty(widget, "unicode-bidi", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getVerticalAlign()
	 */
	@Override
	public String getVerticalAlign() {
		return engine.retrieveCSSProperty(widget, "vertical-align", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getVisibility()
	 */
	@Override
	public String getVisibility() {
		return engine.retrieveCSSProperty(widget, "visibility", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getVoiceFamily()
	 */
	@Override
	public String getVoiceFamily() {
		return engine.retrieveCSSProperty(widget, "voice-family", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getVolume()
	 */
	@Override
	public String getVolume() {
		return engine.retrieveCSSProperty(widget, "volume", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getWhiteSpace()
	 */
	@Override
	public String getWhiteSpace() {
		return engine.retrieveCSSProperty(widget, "white-space", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getWidows()
	 */
	@Override
	public String getWidows() {
		return engine.retrieveCSSProperty(widget, "widows", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getWidth()
	 */
	@Override
	public String getWidth() {
		return engine.retrieveCSSProperty(widget, "width", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getWordSpacing()
	 */
	@Override
	public String getWordSpacing() {
		return engine.retrieveCSSProperty(widget, "word-spacing", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#getZIndex()
	 */
	@Override
	public String getZIndex() {
		return engine.retrieveCSSProperty(widget, "z-index", null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setAzimuth(java.lang.String)
	 */
	@Override
	public void setAzimuth(String azimuth) throws DOMException {
		parseAndApplyStyle("azimuth", azimuth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBackground(java.lang.String)
	 */
	@Override
	public void setBackground(String background) throws DOMException {
		parseAndApplyStyle("background", background);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBackgroundAttachment(java.lang.String)
	 */
	@Override
	public void setBackgroundAttachment(String backgroundAttachment)
			throws DOMException {
		parseAndApplyStyle("background-attachment", backgroundAttachment);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBackgroundColor(java.lang.String)
	 */
	@Override
	public void setBackgroundColor(String backgroundColor) throws DOMException {
		parseAndApplyStyle("background-color", backgroundColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBackgroundImage(java.lang.String)
	 */
	@Override
	public void setBackgroundImage(String backgroundImage) throws DOMException {
		parseAndApplyStyle("background-image", backgroundImage);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBackgroundPosition(java.lang.String)
	 */
	@Override
	public void setBackgroundPosition(String backgroundPosition)
			throws DOMException {
		parseAndApplyStyle("background-position", backgroundPosition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBackgroundRepeat(java.lang.String)
	 */
	@Override
	public void setBackgroundRepeat(String backgroundRepeat)
			throws DOMException {
		parseAndApplyStyle("background-repeat", backgroundRepeat);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorder(java.lang.String)
	 */
	@Override
	public void setBorder(String border) throws DOMException {
		parseAndApplyStyle("border", border);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderBottom(java.lang.String)
	 */
	@Override
	public void setBorderBottom(String borderBottom) throws DOMException {
		parseAndApplyStyle("border-bottom", borderBottom);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderBottomColor(java.lang.String)
	 */
	@Override
	public void setBorderBottomColor(String borderColor) throws DOMException {
		parseAndApplyStyle("border-color", borderColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderBottomStyle(java.lang.String)
	 */
	@Override
	public void setBorderBottomStyle(String borderBottomStyle)
			throws DOMException {
		parseAndApplyStyle("border-bottom-style", borderBottomStyle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderBottomWidth(java.lang.String)
	 */
	@Override
	public void setBorderBottomWidth(String borderBottomWidth)
			throws DOMException {
		parseAndApplyStyle("border-bottom-width", borderBottomWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderCollapse(java.lang.String)
	 */
	@Override
	public void setBorderCollapse(String borderCollapse) throws DOMException {
		parseAndApplyStyle("border-collapse", borderCollapse);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderColor(java.lang.String)
	 */
	@Override
	public void setBorderColor(String borderColor) throws DOMException {
		parseAndApplyStyle("border-color", borderColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderLeft(java.lang.String)
	 */
	@Override
	public void setBorderLeft(String borderLeft) throws DOMException {
		parseAndApplyStyle("border-left", borderLeft);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderLeftColor(java.lang.String)
	 */
	@Override
	public void setBorderLeftColor(String borderLeftColor) throws DOMException {
		parseAndApplyStyle("border-left-color", borderLeftColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderLeftStyle(java.lang.String)
	 */
	@Override
	public void setBorderLeftStyle(String borderLeftStyle) throws DOMException {
		parseAndApplyStyle("border-left-style", borderLeftStyle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderLeftWidth(java.lang.String)
	 */
	@Override
	public void setBorderLeftWidth(String borderLeftWidth) throws DOMException {
		parseAndApplyStyle("border-left-width", borderLeftWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderRight(java.lang.String)
	 */
	@Override
	public void setBorderRight(String borderRight) throws DOMException {
		parseAndApplyStyle("border-right", borderRight);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderRightColor(java.lang.String)
	 */
	@Override
	public void setBorderRightColor(String borderRightColor)
			throws DOMException {
		parseAndApplyStyle("border-right-color", borderRightColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderRightStyle(java.lang.String)
	 */
	@Override
	public void setBorderRightStyle(String borderRightStyle)
			throws DOMException {
		parseAndApplyStyle("border-right-style", borderRightStyle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderRightWidth(java.lang.String)
	 */
	@Override
	public void setBorderRightWidth(String borderRightWidth)
			throws DOMException {
		parseAndApplyStyle("border-right-width", borderRightWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderSpacing(java.lang.String)
	 */
	@Override
	public void setBorderSpacing(String borderSpacing) throws DOMException {
		parseAndApplyStyle("border-spacing", borderSpacing);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderStyle(java.lang.String)
	 */
	@Override
	public void setBorderStyle(String borderStyle) throws DOMException {
		parseAndApplyStyle("border-style", borderStyle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderTop(java.lang.String)
	 */
	@Override
	public void setBorderTop(String borderTop) throws DOMException {
		parseAndApplyStyle("border-top", borderTop);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderTopColor(java.lang.String)
	 */
	@Override
	public void setBorderTopColor(String borderTopColor) throws DOMException {
		parseAndApplyStyle("border-top-color", borderTopColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderTopStyle(java.lang.String)
	 */
	@Override
	public void setBorderTopStyle(String borderTopStyle) throws DOMException {
		parseAndApplyStyle("border-top-style", borderTopStyle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderTopWidth(java.lang.String)
	 */
	@Override
	public void setBorderTopWidth(String borderTopWidth) throws DOMException {
		parseAndApplyStyle("border-top-width", borderTopWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBorderWidth(java.lang.String)
	 */
	@Override
	public void setBorderWidth(String borderWidth) throws DOMException {
		parseAndApplyStyle("border-width", borderWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setBottom(java.lang.String)
	 */
	@Override
	public void setBottom(String bottom) throws DOMException {
		parseAndApplyStyle("bottom", bottom);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setCaptionSide(java.lang.String)
	 */
	@Override
	public void setCaptionSide(String captionSide) throws DOMException {
		parseAndApplyStyle("caption-side", captionSide);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setClear(java.lang.String)
	 */
	@Override
	public void setClear(String clear) throws DOMException {
		parseAndApplyStyle("clear", clear);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setClip(java.lang.String)
	 */
	@Override
	public void setClip(String clip) throws DOMException {
		parseAndApplyStyle("clip", clip);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setColor(java.lang.String)
	 */
	@Override
	public void setColor(String color) throws DOMException {
		parseAndApplyStyle("color", color);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setContent(java.lang.String)
	 */
	@Override
	public void setContent(String content) throws DOMException {
		parseAndApplyStyle("content", content);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setCounterIncrement(java.lang.String)
	 */
	@Override
	public void setCounterIncrement(String counterIncrement)
			throws DOMException {
		parseAndApplyStyle("counter-increment", counterIncrement);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setCounterReset(java.lang.String)
	 */
	@Override
	public void setCounterReset(String counterReset) throws DOMException {
		parseAndApplyStyle("counter-reset", counterReset);
	}

	@Override
	public void setCssFloat(String cssFloat) throws DOMException {
		parseAndApplyStyle("float", cssFloat);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setCue(java.lang.String)
	 */
	@Override
	public void setCue(String cue) throws DOMException {
		parseAndApplyStyle("cue", cue);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setCueAfter(java.lang.String)
	 */
	@Override
	public void setCueAfter(String cueAfter) throws DOMException {
		parseAndApplyStyle("cue-after", cueAfter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setCueBefore(java.lang.String)
	 */
	@Override
	public void setCueBefore(String cueBefore) throws DOMException {
		parseAndApplyStyle("cue-before", cueBefore);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setCursor(java.lang.String)
	 */
	@Override
	public void setCursor(String cursor) throws DOMException {
		parseAndApplyStyle("cursor", cursor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setDirection(java.lang.String)
	 */
	@Override
	public void setDirection(String direction) throws DOMException {
		parseAndApplyStyle("direction", direction);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setDisplay(java.lang.String)
	 */
	@Override
	public void setDisplay(String display) throws DOMException {
		parseAndApplyStyle("display", display);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setElevation(java.lang.String)
	 */
	@Override
	public void setElevation(String elevation) throws DOMException {
		parseAndApplyStyle("elevation", elevation);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setEmptyCells(java.lang.String)
	 */
	@Override
	public void setEmptyCells(String emptyCells) throws DOMException {
		parseAndApplyStyle("empty-cells", emptyCells);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFont(java.lang.String)
	 */
	@Override
	public void setFont(String font) throws DOMException {
		parseAndApplyStyle("font", font);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFontFamily(java.lang.String)
	 */
	@Override
	public void setFontFamily(String fontFamily) throws DOMException {
		parseAndApplyStyle("font-family", fontFamily);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFontSize(java.lang.String)
	 */
	@Override
	public void setFontSize(String fontSize) throws DOMException {
		parseAndApplyStyle("font-size", fontSize);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFontSizeAdjust(java.lang.String)
	 */
	@Override
	public void setFontSizeAdjust(String fontSizeAdjust) throws DOMException {
		parseAndApplyStyle("font-size-adjust", fontSizeAdjust);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFontStretch(java.lang.String)
	 */
	@Override
	public void setFontStretch(String fontStretch) throws DOMException {
		parseAndApplyStyle("font-stretch", fontStretch);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFontStyle(java.lang.String)
	 */
	@Override
	public void setFontStyle(String fontStyle) throws DOMException {
		parseAndApplyStyle("font-style", fontStyle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFontVariant(java.lang.String)
	 */
	@Override
	public void setFontVariant(String fontVariant) throws DOMException {
		parseAndApplyStyle("font-variant", fontVariant);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setFontWeight(java.lang.String)
	 */
	@Override
	public void setFontWeight(String fontWeight) throws DOMException {
		parseAndApplyStyle("font-weight", fontWeight);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setHeight(java.lang.String)
	 */
	@Override
	public void setHeight(String height) throws DOMException {
		parseAndApplyStyle("height", height);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setLeft(java.lang.String)
	 */
	@Override
	public void setLeft(String left) throws DOMException {
		parseAndApplyStyle("left", left);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setLetterSpacing(java.lang.String)
	 */
	@Override
	public void setLetterSpacing(String letterSpacing) throws DOMException {
		parseAndApplyStyle("letter-spacing", letterSpacing);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setLineHeight(java.lang.String)
	 */
	@Override
	public void setLineHeight(String lineHeight) throws DOMException {
		parseAndApplyStyle("line-height", lineHeight);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setListStyle(java.lang.String)
	 */
	@Override
	public void setListStyle(String listStyle) throws DOMException {
		parseAndApplyStyle("list-style", listStyle);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setListStyleImage(java.lang.String)
	 */
	@Override
	public void setListStyleImage(String listStyleImage) throws DOMException {
		parseAndApplyStyle("list-style-image", listStyleImage);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setListStylePosition(java.lang.String)
	 */
	@Override
	public void setListStylePosition(String listStylePosition)
			throws DOMException {
		parseAndApplyStyle("list-style-position", listStylePosition);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setListStyleType(java.lang.String)
	 */
	@Override
	public void setListStyleType(String listStyleType) throws DOMException {
		parseAndApplyStyle("list-style-type", listStyleType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMargin(java.lang.String)
	 */
	@Override
	public void setMargin(String margin) throws DOMException {
		parseAndApplyStyle("margin", margin);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMarginBottom(java.lang.String)
	 */
	@Override
	public void setMarginBottom(String marginBottom) throws DOMException {
		parseAndApplyStyle("margin-bottom", marginBottom);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMarginLeft(java.lang.String)
	 */
	@Override
	public void setMarginLeft(String marginLeft) throws DOMException {
		parseAndApplyStyle("margin-left", marginLeft);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMarginRight(java.lang.String)
	 */
	@Override
	public void setMarginRight(String marginRight) throws DOMException {
		parseAndApplyStyle("margin-right", marginRight);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMarginTop(java.lang.String)
	 */
	@Override
	public void setMarginTop(String marginTop) throws DOMException {
		parseAndApplyStyle("margin-top", marginTop);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMarkerOffset(java.lang.String)
	 */
	@Override
	public void setMarkerOffset(String markerOffset) throws DOMException {
		parseAndApplyStyle("marker-offset", markerOffset);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMarks(java.lang.String)
	 */
	@Override
	public void setMarks(String marks) throws DOMException {
		parseAndApplyStyle("marks", marks);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMaxHeight(java.lang.String)
	 */
	@Override
	public void setMaxHeight(String maxHeight) throws DOMException {
		parseAndApplyStyle("max-height", maxHeight);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMaxWidth(java.lang.String)
	 */
	@Override
	public void setMaxWidth(String maxWidth) throws DOMException {
		parseAndApplyStyle("max-width", maxWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMinHeight(java.lang.String)
	 */
	@Override
	public void setMinHeight(String minHeight) throws DOMException {
		parseAndApplyStyle("min-height", minHeight);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setMinWidth(java.lang.String)
	 */
	@Override
	public void setMinWidth(String minWidth) throws DOMException {
		parseAndApplyStyle("min-width", minWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setOrphans(java.lang.String)
	 */
	@Override
	public void setOrphans(String orphans) throws DOMException {
		parseAndApplyStyle("orphans", orphans);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setOutline(java.lang.String)
	 */
	@Override
	public void setOutline(String outline) throws DOMException {
		parseAndApplyStyle("outline", outline);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setOutlineColor(java.lang.String)
	 */
	@Override
	public void setOutlineColor(String outlineColor) throws DOMException {
		parseAndApplyStyle("outline-color", outlineColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setOutlineStyle(java.lang.String)
	 */
	@Override
	public void setOutlineStyle(String outlineStyle) throws DOMException {
		parseAndApplyStyle("outline-style", outlineStyle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setOutlineWidth(java.lang.String)
	 */
	@Override
	public void setOutlineWidth(String outlineWidth) throws DOMException {
		parseAndApplyStyle("outline-width", outlineWidth);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setOverflow(java.lang.String)
	 */
	@Override
	public void setOverflow(String overflow) throws DOMException {
		parseAndApplyStyle("overflow", overflow);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPadding(java.lang.String)
	 */
	@Override
	public void setPadding(String padding) throws DOMException {
		parseAndApplyStyle("padding", padding);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPaddingBottom(java.lang.String)
	 */
	@Override
	public void setPaddingBottom(String paddingBottom) throws DOMException {
		parseAndApplyStyle("padding-bottom", paddingBottom);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPaddingLeft(java.lang.String)
	 */
	@Override
	public void setPaddingLeft(String paddingLeft) throws DOMException {
		parseAndApplyStyle("padding-left", paddingLeft);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPaddingRight(java.lang.String)
	 */
	@Override
	public void setPaddingRight(String paddingRight) throws DOMException {
		parseAndApplyStyle("padding-right", paddingRight);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPaddingTop(java.lang.String)
	 */
	@Override
	public void setPaddingTop(String paddingTop) throws DOMException {
		parseAndApplyStyle("padding-top", paddingTop);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPage(java.lang.String)
	 */
	@Override
	public void setPage(String page) throws DOMException {
		parseAndApplyStyle("page", page);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPageBreakAfter(java.lang.String)
	 */
	@Override
	public void setPageBreakAfter(String pageBreakAfter) throws DOMException {
		parseAndApplyStyle("page-break-after", pageBreakAfter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPageBreakBefore(java.lang.String)
	 */
	@Override
	public void setPageBreakBefore(String pageBreakBefore) throws DOMException {
		parseAndApplyStyle("page-break-before", pageBreakBefore);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPageBreakInside(java.lang.String)
	 */
	@Override
	public void setPageBreakInside(String pageBreakInside) throws DOMException {
		parseAndApplyStyle("page-break-inside", pageBreakInside);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPause(java.lang.String)
	 */
	@Override
	public void setPause(String pause) throws DOMException {
		parseAndApplyStyle("pause", pause);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPauseAfter(java.lang.String)
	 */
	@Override
	public void setPauseAfter(String pauseAfter) throws DOMException {
		parseAndApplyStyle("pause-after", pauseAfter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPauseBefore(java.lang.String)
	 */
	@Override
	public void setPauseBefore(String pauseBefore) throws DOMException {
		parseAndApplyStyle("pause-before", pauseBefore);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPitch(java.lang.String)
	 */
	@Override
	public void setPitch(String pitch) throws DOMException {
		parseAndApplyStyle("pitch", pitch);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPitchRange(java.lang.String)
	 */
	@Override
	public void setPitchRange(String pitchRange) throws DOMException {
		parseAndApplyStyle("pitch-range", pitchRange);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPlayDuring(java.lang.String)
	 */
	@Override
	public void setPlayDuring(String playDuring) throws DOMException {
		parseAndApplyStyle("playDuring", playDuring);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setPosition(java.lang.String)
	 */
	@Override
	public void setPosition(String position) throws DOMException {
		parseAndApplyStyle("position", position);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setQuotes(java.lang.String)
	 */
	@Override
	public void setQuotes(String quotes) throws DOMException {
		parseAndApplyStyle("quotes", quotes);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setRichness(java.lang.String)
	 */
	@Override
	public void setRichness(String richness) throws DOMException {
		parseAndApplyStyle("richness", richness);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setRight(java.lang.String)
	 */
	@Override
	public void setRight(String right) throws DOMException {
		parseAndApplyStyle("right", right);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setSize(java.lang.String)
	 */
	@Override
	public void setSize(String size) throws DOMException {
		parseAndApplyStyle("size", size);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setSpeak(java.lang.String)
	 */
	@Override
	public void setSpeak(String speak) throws DOMException {
		parseAndApplyStyle("speak", speak);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setSpeakHeader(java.lang.String)
	 */
	@Override
	public void setSpeakHeader(String speakHeader) throws DOMException {
		parseAndApplyStyle("speak-header", speakHeader);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setSpeakNumeral(java.lang.String)
	 */
	@Override
	public void setSpeakNumeral(String speakNumeral) throws DOMException {
		parseAndApplyStyle("speak-numeral", speakNumeral);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setSpeakPunctuation(java.lang.String)
	 */
	@Override
	public void setSpeakPunctuation(String speakPunctuation)
			throws DOMException {
		parseAndApplyStyle("speak-punctuation", speakPunctuation);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setSpeechRate(java.lang.String)
	 */
	@Override
	public void setSpeechRate(String speechRate) throws DOMException {
		parseAndApplyStyle("speech-rate", speechRate);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setStress(java.lang.String)
	 */
	@Override
	public void setStress(String stress) throws DOMException {
		parseAndApplyStyle("stress", stress);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setTableLayout(java.lang.String)
	 */
	@Override
	public void setTableLayout(String tableLayout) throws DOMException {
		parseAndApplyStyle("table-layout", tableLayout);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setTextAlign(java.lang.String)
	 */
	@Override
	public void setTextAlign(String textAlign) throws DOMException {
		parseAndApplyStyle("text-align", textAlign);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setTextDecoration(java.lang.String)
	 */
	@Override
	public void setTextDecoration(String textDecoration) throws DOMException {
		parseAndApplyStyle("text-decoration", textDecoration);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setTextIndent(java.lang.String)
	 */
	@Override
	public void setTextIndent(String textIndent) throws DOMException {
		parseAndApplyStyle("text-indent", textIndent);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setTextShadow(java.lang.String)
	 */
	@Override
	public void setTextShadow(String textShadow) throws DOMException {
		parseAndApplyStyle("text-shadow", textShadow);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setTextTransform(java.lang.String)
	 */
	@Override
	public void setTextTransform(String textTransform) throws DOMException {
		parseAndApplyStyle("text-transform", textTransform);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setTop(java.lang.String)
	 */
	@Override
	public void setTop(String top) throws DOMException {
		parseAndApplyStyle("top", top);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setUnicodeBidi(java.lang.String)
	 */
	@Override
	public void setUnicodeBidi(String unicodeBidi) throws DOMException {
		parseAndApplyStyle("unicode-bidi", unicodeBidi);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setVerticalAlign(java.lang.String)
	 */
	@Override
	public void setVerticalAlign(String verticalAlign) throws DOMException {
		parseAndApplyStyle("vertical-align", verticalAlign);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setVisibility(java.lang.String)
	 */
	@Override
	public void setVisibility(String visibility) throws DOMException {
		parseAndApplyStyle("visibility", visibility);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setVoiceFamily(java.lang.String)
	 */
	@Override
	public void setVoiceFamily(String voiceFamily) throws DOMException {
		parseAndApplyStyle("voice-family", voiceFamily);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setVolume(java.lang.String)
	 */
	@Override
	public void setVolume(String volume) throws DOMException {
		parseAndApplyStyle("volume", volume);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setWhiteSpace(java.lang.String)
	 */
	@Override
	public void setWhiteSpace(String whiteSpace) throws DOMException {
		parseAndApplyStyle("white-space", whiteSpace);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setWidows(java.lang.String)
	 */
	@Override
	public void setWidows(String widows) throws DOMException {
		parseAndApplyStyle("widows", widows);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setWidth(java.lang.String)
	 */
	@Override
	public void setWidth(String width) throws DOMException {
		parseAndApplyStyle("width", width);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setWordSpacing(java.lang.String)
	 */
	@Override
	public void setWordSpacing(String wordSpacing) throws DOMException {
		parseAndApplyStyle("word-spacing", wordSpacing);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.w3c.dom.css.CSS2Properties#setZIndex(java.lang.String)
	 */
	@Override
	public void setZIndex(String zIndex) throws DOMException {
		parseAndApplyStyle("z-index", zIndex);
	}

	/**
	 * Parse and apply CSS property name <code>propertyName</code> with value
	 * <code>propertyValue</code> to the widget.
	 *
	 * @param propertyName
	 * @param propertyValue
	 */
	protected void parseAndApplyStyle(String propertyName, String propertyValue) {
		try {
			String property = propertyName + ":" + propertyValue;
			engine.parseAndApplyStyleDeclaration(widget, property);
		} catch (Exception e) {
			throw new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
		}
	}

}
