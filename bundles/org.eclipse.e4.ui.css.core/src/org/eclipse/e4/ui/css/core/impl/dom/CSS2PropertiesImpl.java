/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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

	@Override
	public String getAzimuth() {
		return engine.retrieveCSSProperty(widget, "azimut", null);
	}

	@Override
	public String getBackground() {
		return engine.retrieveCSSProperty(widget, "background", null);
	}

	@Override
	public String getBackgroundAttachment() {
		return engine
				.retrieveCSSProperty(widget, "background-attachment", null);
	}

	@Override
	public String getBackgroundColor() {
		return engine.retrieveCSSProperty(widget, "background-color", null);
	}

	@Override
	public String getBackgroundImage() {
		return engine.retrieveCSSProperty(widget, "background-image", null);
	}

	@Override
	public String getBackgroundPosition() {
		return engine.retrieveCSSProperty(widget, "background-position", null);
	}

	@Override
	public String getBackgroundRepeat() {
		return engine.retrieveCSSProperty(widget, "background-repeat", null);
	}

	@Override
	public String getBorder() {
		return engine.retrieveCSSProperty(widget, "border", null);
	}

	@Override
	public String getBorderBottom() {
		return engine.retrieveCSSProperty(widget, "border-bottom", null);
	}

	@Override
	public String getBorderBottomColor() {
		return engine.retrieveCSSProperty(widget, "border-bottom-color", null);
	}

	@Override
	public String getBorderBottomStyle() {
		return engine.retrieveCSSProperty(widget, "border-bottom-style", null);
	}

	@Override
	public String getBorderBottomWidth() {
		return engine.retrieveCSSProperty(widget, "border-bottom-width", null);
	}

	@Override
	public String getBorderCollapse() {
		return engine.retrieveCSSProperty(widget, "border-collapse", null);
	}

	@Override
	public String getBorderColor() {
		return engine.retrieveCSSProperty(widget, "border-color", null);
	}

	@Override
	public String getBorderLeft() {
		return engine.retrieveCSSProperty(widget, "border-left", null);
	}

	@Override
	public String getBorderLeftColor() {
		return engine.retrieveCSSProperty(widget, "border-left-color", null);
	}

	@Override
	public String getBorderLeftStyle() {
		return engine.retrieveCSSProperty(widget, "border-left-style", null);
	}

	@Override
	public String getBorderLeftWidth() {
		return engine.retrieveCSSProperty(widget, "border-left-width", null);
	}

	@Override
	public String getBorderRight() {
		return engine.retrieveCSSProperty(widget, "border-right", null);
	}

	@Override
	public String getBorderRightColor() {
		return engine.retrieveCSSProperty(widget, "border-right-color", null);
	}

	@Override
	public String getBorderRightStyle() {
		return engine.retrieveCSSProperty(widget, "border-right-style", null);
	}

	@Override
	public String getBorderRightWidth() {
		return engine.retrieveCSSProperty(widget, "border-right-width", null);
	}

	@Override
	public String getBorderSpacing() {
		return engine.retrieveCSSProperty(widget, "border-spacing", null);
	}

	@Override
	public String getBorderStyle() {
		return engine.retrieveCSSProperty(widget, "border-style", null);
	}

	@Override
	public String getBorderTop() {
		return engine.retrieveCSSProperty(widget, "border-top", null);
	}

	@Override
	public String getBorderTopColor() {
		return engine.retrieveCSSProperty(widget, "border-top-color", null);
	}

	@Override
	public String getBorderTopStyle() {
		return engine.retrieveCSSProperty(widget, "border-top-style", null);
	}

	@Override
	public String getBorderTopWidth() {
		return engine.retrieveCSSProperty(widget, "border-top-width", null);
	}

	@Override
	public String getBorderWidth() {
		return engine.retrieveCSSProperty(widget, "border-width", null);
	}

	@Override
	public String getBottom() {
		return engine.retrieveCSSProperty(widget, "border-bottom", null);
	}

	@Override
	public String getCaptionSide() {
		return engine.retrieveCSSProperty(widget, "caption-side", null);
	}

	@Override
	public String getClear() {
		return engine.retrieveCSSProperty(widget, "clear", null);
	}

	@Override
	public String getClip() {
		return engine.retrieveCSSProperty(widget, "clip", null);
	}

	@Override
	public String getColor() {
		return engine.retrieveCSSProperty(widget, "color", null);
	}

	@Override
	public String getContent() {
		return engine.retrieveCSSProperty(widget, "content", null);
	}

	@Override
	public String getCounterIncrement() {
		return engine.retrieveCSSProperty(widget, "counter-increment", null);
	}

	@Override
	public String getCounterReset() {
		return engine.retrieveCSSProperty(widget, "counter-reset", null);
	}

	@Override
	public String getCssFloat() {
		return engine.retrieveCSSProperty(widget, "float", null);
	}

	@Override
	public String getCue() {
		return engine.retrieveCSSProperty(widget, "cue", null);
	}

	@Override
	public String getCueAfter() {
		return engine.retrieveCSSProperty(widget, "cue-after", null);
	}

	@Override
	public String getCueBefore() {
		return engine.retrieveCSSProperty(widget, "cue-before", null);
	}

	@Override
	public String getCursor() {
		return engine.retrieveCSSProperty(widget, "cursor", null);
	}

	@Override
	public String getDirection() {
		return engine.retrieveCSSProperty(widget, "direction", null);
	}

	@Override
	public String getDisplay() {
		return engine.retrieveCSSProperty(widget, "display", null);
	}

	@Override
	public String getElevation() {
		return engine.retrieveCSSProperty(widget, "elevation", null);
	}

	@Override
	public String getEmptyCells() {
		return engine.retrieveCSSProperty(widget, "empty-cells", null);
	}

	@Override
	public String getFont() {
		return engine.retrieveCSSProperty(widget, "font", null);
	}

	@Override
	public String getFontFamily() {
		return engine.retrieveCSSProperty(widget, "font-family", null);
	}

	@Override
	public String getFontSize() {
		return engine.retrieveCSSProperty(widget, "font-size", null);
	}

	@Override
	public String getFontSizeAdjust() {
		return engine.retrieveCSSProperty(widget, "font_size-adjust", null);
	}

	@Override
	public String getFontStretch() {
		return engine.retrieveCSSProperty(widget, "font-stretch", null);
	}

	@Override
	public String getFontStyle() {
		return engine.retrieveCSSProperty(widget, "font-style", null);
	}

	@Override
	public String getFontVariant() {
		return engine.retrieveCSSProperty(widget, "font-variant", null);
	}

	@Override
	public String getFontWeight() {
		return engine.retrieveCSSProperty(widget, "font-weight", null);
	}

	@Override
	public String getHeight() {
		return engine.retrieveCSSProperty(widget, "height", null);
	}

	@Override
	public String getLeft() {
		return engine.retrieveCSSProperty(widget, "left", null);
	}

	@Override
	public String getLetterSpacing() {
		return engine.retrieveCSSProperty(widget, "letter-spacing", null);
	}

	@Override
	public String getLineHeight() {
		return engine.retrieveCSSProperty(widget, "line-height", null);
	}

	@Override
	public String getListStyle() {
		return engine.retrieveCSSProperty(widget, "list-style", null);
	}

	@Override
	public String getListStyleImage() {
		return engine.retrieveCSSProperty(widget, "list-style-image", null);
	}

	@Override
	public String getListStylePosition() {
		return engine.retrieveCSSProperty(widget, "list-style-position", null);
	}

	@Override
	public String getListStyleType() {
		return engine.retrieveCSSProperty(widget, "list-style-type", null);
	}

	@Override
	public String getMargin() {
		return engine.retrieveCSSProperty(widget, "margin", null);
	}

	@Override
	public String getMarginBottom() {
		return engine.retrieveCSSProperty(widget, "margin-bottom", null);
	}

	@Override
	public String getMarginLeft() {
		return engine.retrieveCSSProperty(widget, "margin-left", null);
	}

	@Override
	public String getMarginRight() {
		return engine.retrieveCSSProperty(widget, "margin-right", null);
	}

	@Override
	public String getMarginTop() {
		return engine.retrieveCSSProperty(widget, "margin-top", null);
	}

	@Override
	public String getMarkerOffset() {
		return engine.retrieveCSSProperty(widget, "marker-offset", null);
	}

	@Override
	public String getMarks() {
		return engine.retrieveCSSProperty(widget, "marks", null);
	}

	@Override
	public String getMaxHeight() {
		return engine.retrieveCSSProperty(widget, "max-height", null);
	}

	@Override
	public String getMaxWidth() {
		return engine.retrieveCSSProperty(widget, "max-width", null);
	}

	@Override
	public String getMinHeight() {
		return engine.retrieveCSSProperty(widget, "min-height", null);
	}

	@Override
	public String getMinWidth() {
		return engine.retrieveCSSProperty(widget, "min-width", null);
	}

	@Override
	public String getOrphans() {
		return engine.retrieveCSSProperty(widget, "orphans", null);
	}

	@Override
	public String getOutline() {
		return engine.retrieveCSSProperty(widget, "outline", null);
	}

	@Override
	public String getOutlineColor() {
		return engine.retrieveCSSProperty(widget, "outline-color", null);
	}

	@Override
	public String getOutlineStyle() {
		return engine.retrieveCSSProperty(widget, "outline-style", null);
	}

	@Override
	public String getOutlineWidth() {
		return engine.retrieveCSSProperty(widget, "outline-width", null);
	}

	@Override
	public String getOverflow() {
		return engine.retrieveCSSProperty(widget, "overflow", null);
	}

	@Override
	public String getPadding() {
		return engine.retrieveCSSProperty(widget, "padding", null);
	}

	@Override
	public String getPaddingBottom() {
		return engine.retrieveCSSProperty(widget, "padding-bottom", null);
	}

	@Override
	public String getPaddingLeft() {
		return engine.retrieveCSSProperty(widget, "padding-left", null);
	}

	@Override
	public String getPaddingRight() {
		return engine.retrieveCSSProperty(widget, "padding-right", null);
	}

	@Override
	public String getPaddingTop() {
		return engine.retrieveCSSProperty(widget, "padding-top", null);
	}

	@Override
	public String getPage() {
		return engine.retrieveCSSProperty(widget, "page", null);
	}

	@Override
	public String getPageBreakAfter() {
		return engine.retrieveCSSProperty(widget, "page-break-after", null);
	}

	@Override
	public String getPageBreakBefore() {
		return engine.retrieveCSSProperty(widget, "page-break-before", null);
	}

	@Override
	public String getPageBreakInside() {
		return engine.retrieveCSSProperty(widget, "page-break-inside", null);
	}

	@Override
	public String getPause() {
		return engine.retrieveCSSProperty(widget, "pause", null);
	}

	@Override
	public String getPauseAfter() {
		return engine.retrieveCSSProperty(widget, "pause-after", null);
	}

	@Override
	public String getPauseBefore() {
		return engine.retrieveCSSProperty(widget, "pause-before", null);
	}

	@Override
	public String getPitch() {
		return engine.retrieveCSSProperty(widget, "pitch", null);
	}

	@Override
	public String getPitchRange() {
		return engine.retrieveCSSProperty(widget, "pitch-range", null);
	}

	@Override
	public String getPlayDuring() {
		return engine.retrieveCSSProperty(widget, "play-during", null);
	}

	@Override
	public String getPosition() {
		return engine.retrieveCSSProperty(widget, "position", null);
	}

	@Override
	public String getQuotes() {
		return engine.retrieveCSSProperty(widget, "quotes", null);
	}

	@Override
	public String getRichness() {
		return engine.retrieveCSSProperty(widget, "richness", null);
	}

	@Override
	public String getRight() {
		return engine.retrieveCSSProperty(widget, "right", null);
	}

	@Override
	public String getSize() {
		return engine.retrieveCSSProperty(widget, "size", null);
	}

	@Override
	public String getSpeak() {
		return engine.retrieveCSSProperty(widget, "speak", null);
	}

	@Override
	public String getSpeakHeader() {
		return engine.retrieveCSSProperty(widget, "speak-header", null);
	}

	@Override
	public String getSpeakNumeral() {
		return engine.retrieveCSSProperty(widget, "speak-numeral", null);
	}

	@Override
	public String getSpeakPunctuation() {
		return engine.retrieveCSSProperty(widget, "speak-punctuation", null);
	}

	@Override
	public String getSpeechRate() {
		return engine.retrieveCSSProperty(widget, "speech-rate", null);
	}

	@Override
	public String getStress() {
		return engine.retrieveCSSProperty(widget, "stress", null);
	}

	@Override
	public String getTableLayout() {
		return engine.retrieveCSSProperty(widget, "table-layout", null);
	}

	@Override
	public String getTextAlign() {
		return engine.retrieveCSSProperty(widget, "text-align", null);
	}

	@Override
	public String getTextDecoration() {
		return engine.retrieveCSSProperty(widget, "text-decoration", null);
	}

	@Override
	public String getTextIndent() {
		return engine.retrieveCSSProperty(widget, "text-indent", null);
	}

	@Override
	public String getTextShadow() {
		return engine.retrieveCSSProperty(widget, "text-shadow", null);
	}

	@Override
	public String getTextTransform() {
		return engine.retrieveCSSProperty(widget, "text-transform", null);
	}

	@Override
	public String getTop() {
		return engine.retrieveCSSProperty(widget, "top", null);
	}

	@Override
	public String getUnicodeBidi() {
		return engine.retrieveCSSProperty(widget, "unicode-bidi", null);
	}

	@Override
	public String getVerticalAlign() {
		return engine.retrieveCSSProperty(widget, "vertical-align", null);
	}

	@Override
	public String getVisibility() {
		return engine.retrieveCSSProperty(widget, "visibility", null);
	}

	@Override
	public String getVoiceFamily() {
		return engine.retrieveCSSProperty(widget, "voice-family", null);
	}

	@Override
	public String getVolume() {
		return engine.retrieveCSSProperty(widget, "volume", null);
	}

	@Override
	public String getWhiteSpace() {
		return engine.retrieveCSSProperty(widget, "white-space", null);
	}

	@Override
	public String getWidows() {
		return engine.retrieveCSSProperty(widget, "widows", null);
	}

	@Override
	public String getWidth() {
		return engine.retrieveCSSProperty(widget, "width", null);
	}

	@Override
	public String getWordSpacing() {
		return engine.retrieveCSSProperty(widget, "word-spacing", null);
	}

	@Override
	public String getZIndex() {
		return engine.retrieveCSSProperty(widget, "z-index", null);
	}

	@Override
	public void setAzimuth(String azimuth) throws DOMException {
		parseAndApplyStyle("azimuth", azimuth);
	}

	@Override
	public void setBackground(String background) throws DOMException {
		parseAndApplyStyle("background", background);
	}

	@Override
	public void setBackgroundAttachment(String backgroundAttachment)
			throws DOMException {
		parseAndApplyStyle("background-attachment", backgroundAttachment);
	}

	@Override
	public void setBackgroundColor(String backgroundColor) throws DOMException {
		parseAndApplyStyle("background-color", backgroundColor);
	}

	@Override
	public void setBackgroundImage(String backgroundImage) throws DOMException {
		parseAndApplyStyle("background-image", backgroundImage);
	}

	@Override
	public void setBackgroundPosition(String backgroundPosition)
			throws DOMException {
		parseAndApplyStyle("background-position", backgroundPosition);
	}

	@Override
	public void setBackgroundRepeat(String backgroundRepeat)
			throws DOMException {
		parseAndApplyStyle("background-repeat", backgroundRepeat);
	}

	@Override
	public void setBorder(String border) throws DOMException {
		parseAndApplyStyle("border", border);
	}

	@Override
	public void setBorderBottom(String borderBottom) throws DOMException {
		parseAndApplyStyle("border-bottom", borderBottom);
	}

	@Override
	public void setBorderBottomColor(String borderColor) throws DOMException {
		parseAndApplyStyle("border-color", borderColor);
	}

	@Override
	public void setBorderBottomStyle(String borderBottomStyle)
			throws DOMException {
		parseAndApplyStyle("border-bottom-style", borderBottomStyle);
	}

	@Override
	public void setBorderBottomWidth(String borderBottomWidth)
			throws DOMException {
		parseAndApplyStyle("border-bottom-width", borderBottomWidth);
	}

	@Override
	public void setBorderCollapse(String borderCollapse) throws DOMException {
		parseAndApplyStyle("border-collapse", borderCollapse);
	}

	@Override
	public void setBorderColor(String borderColor) throws DOMException {
		parseAndApplyStyle("border-color", borderColor);
	}

	@Override
	public void setBorderLeft(String borderLeft) throws DOMException {
		parseAndApplyStyle("border-left", borderLeft);
	}

	@Override
	public void setBorderLeftColor(String borderLeftColor) throws DOMException {
		parseAndApplyStyle("border-left-color", borderLeftColor);
	}

	@Override
	public void setBorderLeftStyle(String borderLeftStyle) throws DOMException {
		parseAndApplyStyle("border-left-style", borderLeftStyle);
	}

	@Override
	public void setBorderLeftWidth(String borderLeftWidth) throws DOMException {
		parseAndApplyStyle("border-left-width", borderLeftWidth);
	}

	@Override
	public void setBorderRight(String borderRight) throws DOMException {
		parseAndApplyStyle("border-right", borderRight);
	}

	@Override
	public void setBorderRightColor(String borderRightColor)
			throws DOMException {
		parseAndApplyStyle("border-right-color", borderRightColor);
	}

	@Override
	public void setBorderRightStyle(String borderRightStyle)
			throws DOMException {
		parseAndApplyStyle("border-right-style", borderRightStyle);
	}

	@Override
	public void setBorderRightWidth(String borderRightWidth)
			throws DOMException {
		parseAndApplyStyle("border-right-width", borderRightWidth);
	}

	@Override
	public void setBorderSpacing(String borderSpacing) throws DOMException {
		parseAndApplyStyle("border-spacing", borderSpacing);
	}

	@Override
	public void setBorderStyle(String borderStyle) throws DOMException {
		parseAndApplyStyle("border-style", borderStyle);
	}

	@Override
	public void setBorderTop(String borderTop) throws DOMException {
		parseAndApplyStyle("border-top", borderTop);
	}

	@Override
	public void setBorderTopColor(String borderTopColor) throws DOMException {
		parseAndApplyStyle("border-top-color", borderTopColor);
	}

	@Override
	public void setBorderTopStyle(String borderTopStyle) throws DOMException {
		parseAndApplyStyle("border-top-style", borderTopStyle);
	}

	@Override
	public void setBorderTopWidth(String borderTopWidth) throws DOMException {
		parseAndApplyStyle("border-top-width", borderTopWidth);
	}

	@Override
	public void setBorderWidth(String borderWidth) throws DOMException {
		parseAndApplyStyle("border-width", borderWidth);
	}

	@Override
	public void setBottom(String bottom) throws DOMException {
		parseAndApplyStyle("bottom", bottom);
	}

	@Override
	public void setCaptionSide(String captionSide) throws DOMException {
		parseAndApplyStyle("caption-side", captionSide);
	}

	@Override
	public void setClear(String clear) throws DOMException {
		parseAndApplyStyle("clear", clear);
	}

	@Override
	public void setClip(String clip) throws DOMException {
		parseAndApplyStyle("clip", clip);
	}

	@Override
	public void setColor(String color) throws DOMException {
		parseAndApplyStyle("color", color);
	}

	@Override
	public void setContent(String content) throws DOMException {
		parseAndApplyStyle("content", content);
	}

	@Override
	public void setCounterIncrement(String counterIncrement)
			throws DOMException {
		parseAndApplyStyle("counter-increment", counterIncrement);
	}

	@Override
	public void setCounterReset(String counterReset) throws DOMException {
		parseAndApplyStyle("counter-reset", counterReset);
	}

	@Override
	public void setCssFloat(String cssFloat) throws DOMException {
		parseAndApplyStyle("float", cssFloat);
	}

	@Override
	public void setCue(String cue) throws DOMException {
		parseAndApplyStyle("cue", cue);
	}

	@Override
	public void setCueAfter(String cueAfter) throws DOMException {
		parseAndApplyStyle("cue-after", cueAfter);
	}

	@Override
	public void setCueBefore(String cueBefore) throws DOMException {
		parseAndApplyStyle("cue-before", cueBefore);
	}

	@Override
	public void setCursor(String cursor) throws DOMException {
		parseAndApplyStyle("cursor", cursor);
	}

	@Override
	public void setDirection(String direction) throws DOMException {
		parseAndApplyStyle("direction", direction);
	}

	@Override
	public void setDisplay(String display) throws DOMException {
		parseAndApplyStyle("display", display);
	}

	@Override
	public void setElevation(String elevation) throws DOMException {
		parseAndApplyStyle("elevation", elevation);
	}

	@Override
	public void setEmptyCells(String emptyCells) throws DOMException {
		parseAndApplyStyle("empty-cells", emptyCells);
	}

	@Override
	public void setFont(String font) throws DOMException {
		parseAndApplyStyle("font", font);
	}

	@Override
	public void setFontFamily(String fontFamily) throws DOMException {
		parseAndApplyStyle("font-family", fontFamily);
	}

	@Override
	public void setFontSize(String fontSize) throws DOMException {
		parseAndApplyStyle("font-size", fontSize);
	}

	@Override
	public void setFontSizeAdjust(String fontSizeAdjust) throws DOMException {
		parseAndApplyStyle("font-size-adjust", fontSizeAdjust);
	}

	@Override
	public void setFontStretch(String fontStretch) throws DOMException {
		parseAndApplyStyle("font-stretch", fontStretch);
	}

	@Override
	public void setFontStyle(String fontStyle) throws DOMException {
		parseAndApplyStyle("font-style", fontStyle);
	}

	@Override
	public void setFontVariant(String fontVariant) throws DOMException {
		parseAndApplyStyle("font-variant", fontVariant);
	}

	@Override
	public void setFontWeight(String fontWeight) throws DOMException {
		parseAndApplyStyle("font-weight", fontWeight);
	}

	@Override
	public void setHeight(String height) throws DOMException {
		parseAndApplyStyle("height", height);
	}

	@Override
	public void setLeft(String left) throws DOMException {
		parseAndApplyStyle("left", left);
	}

	@Override
	public void setLetterSpacing(String letterSpacing) throws DOMException {
		parseAndApplyStyle("letter-spacing", letterSpacing);
	}

	@Override
	public void setLineHeight(String lineHeight) throws DOMException {
		parseAndApplyStyle("line-height", lineHeight);
	}

	@Override
	public void setListStyle(String listStyle) throws DOMException {
		parseAndApplyStyle("list-style", listStyle);

	}

	@Override
	public void setListStyleImage(String listStyleImage) throws DOMException {
		parseAndApplyStyle("list-style-image", listStyleImage);
	}

	@Override
	public void setListStylePosition(String listStylePosition)
			throws DOMException {
		parseAndApplyStyle("list-style-position", listStylePosition);
	}

	@Override
	public void setListStyleType(String listStyleType) throws DOMException {
		parseAndApplyStyle("list-style-type", listStyleType);
	}

	@Override
	public void setMargin(String margin) throws DOMException {
		parseAndApplyStyle("margin", margin);
	}

	@Override
	public void setMarginBottom(String marginBottom) throws DOMException {
		parseAndApplyStyle("margin-bottom", marginBottom);
	}

	@Override
	public void setMarginLeft(String marginLeft) throws DOMException {
		parseAndApplyStyle("margin-left", marginLeft);
	}

	@Override
	public void setMarginRight(String marginRight) throws DOMException {
		parseAndApplyStyle("margin-right", marginRight);
	}

	@Override
	public void setMarginTop(String marginTop) throws DOMException {
		parseAndApplyStyle("margin-top", marginTop);
	}

	@Override
	public void setMarkerOffset(String markerOffset) throws DOMException {
		parseAndApplyStyle("marker-offset", markerOffset);
	}

	@Override
	public void setMarks(String marks) throws DOMException {
		parseAndApplyStyle("marks", marks);
	}

	@Override
	public void setMaxHeight(String maxHeight) throws DOMException {
		parseAndApplyStyle("max-height", maxHeight);
	}

	@Override
	public void setMaxWidth(String maxWidth) throws DOMException {
		parseAndApplyStyle("max-width", maxWidth);
	}

	@Override
	public void setMinHeight(String minHeight) throws DOMException {
		parseAndApplyStyle("min-height", minHeight);
	}

	@Override
	public void setMinWidth(String minWidth) throws DOMException {
		parseAndApplyStyle("min-width", minWidth);
	}

	@Override
	public void setOrphans(String orphans) throws DOMException {
		parseAndApplyStyle("orphans", orphans);
	}

	@Override
	public void setOutline(String outline) throws DOMException {
		parseAndApplyStyle("outline", outline);
	}

	@Override
	public void setOutlineColor(String outlineColor) throws DOMException {
		parseAndApplyStyle("outline-color", outlineColor);
	}

	@Override
	public void setOutlineStyle(String outlineStyle) throws DOMException {
		parseAndApplyStyle("outline-style", outlineStyle);
	}

	@Override
	public void setOutlineWidth(String outlineWidth) throws DOMException {
		parseAndApplyStyle("outline-width", outlineWidth);
	}

	@Override
	public void setOverflow(String overflow) throws DOMException {
		parseAndApplyStyle("overflow", overflow);
	}

	@Override
	public void setPadding(String padding) throws DOMException {
		parseAndApplyStyle("padding", padding);
	}

	@Override
	public void setPaddingBottom(String paddingBottom) throws DOMException {
		parseAndApplyStyle("padding-bottom", paddingBottom);
	}

	@Override
	public void setPaddingLeft(String paddingLeft) throws DOMException {
		parseAndApplyStyle("padding-left", paddingLeft);
	}

	@Override
	public void setPaddingRight(String paddingRight) throws DOMException {
		parseAndApplyStyle("padding-right", paddingRight);
	}

	@Override
	public void setPaddingTop(String paddingTop) throws DOMException {
		parseAndApplyStyle("padding-top", paddingTop);
	}

	@Override
	public void setPage(String page) throws DOMException {
		parseAndApplyStyle("page", page);
	}

	@Override
	public void setPageBreakAfter(String pageBreakAfter) throws DOMException {
		parseAndApplyStyle("page-break-after", pageBreakAfter);
	}

	@Override
	public void setPageBreakBefore(String pageBreakBefore) throws DOMException {
		parseAndApplyStyle("page-break-before", pageBreakBefore);
	}

	@Override
	public void setPageBreakInside(String pageBreakInside) throws DOMException {
		parseAndApplyStyle("page-break-inside", pageBreakInside);
	}

	@Override
	public void setPause(String pause) throws DOMException {
		parseAndApplyStyle("pause", pause);
	}

	@Override
	public void setPauseAfter(String pauseAfter) throws DOMException {
		parseAndApplyStyle("pause-after", pauseAfter);
	}

	@Override
	public void setPauseBefore(String pauseBefore) throws DOMException {
		parseAndApplyStyle("pause-before", pauseBefore);
	}

	@Override
	public void setPitch(String pitch) throws DOMException {
		parseAndApplyStyle("pitch", pitch);
	}

	@Override
	public void setPitchRange(String pitchRange) throws DOMException {
		parseAndApplyStyle("pitch-range", pitchRange);
	}

	@Override
	public void setPlayDuring(String playDuring) throws DOMException {
		parseAndApplyStyle("playDuring", playDuring);
	}

	@Override
	public void setPosition(String position) throws DOMException {
		parseAndApplyStyle("position", position);
	}

	@Override
	public void setQuotes(String quotes) throws DOMException {
		parseAndApplyStyle("quotes", quotes);
	}

	@Override
	public void setRichness(String richness) throws DOMException {
		parseAndApplyStyle("richness", richness);

	}

	@Override
	public void setRight(String right) throws DOMException {
		parseAndApplyStyle("right", right);

	}

	@Override
	public void setSize(String size) throws DOMException {
		parseAndApplyStyle("size", size);

	}

	@Override
	public void setSpeak(String speak) throws DOMException {
		parseAndApplyStyle("speak", speak);

	}

	@Override
	public void setSpeakHeader(String speakHeader) throws DOMException {
		parseAndApplyStyle("speak-header", speakHeader);

	}

	@Override
	public void setSpeakNumeral(String speakNumeral) throws DOMException {
		parseAndApplyStyle("speak-numeral", speakNumeral);

	}

	@Override
	public void setSpeakPunctuation(String speakPunctuation)
			throws DOMException {
		parseAndApplyStyle("speak-punctuation", speakPunctuation);

	}

	@Override
	public void setSpeechRate(String speechRate) throws DOMException {
		parseAndApplyStyle("speech-rate", speechRate);

	}

	@Override
	public void setStress(String stress) throws DOMException {
		parseAndApplyStyle("stress", stress);

	}

	@Override
	public void setTableLayout(String tableLayout) throws DOMException {
		parseAndApplyStyle("table-layout", tableLayout);

	}

	@Override
	public void setTextAlign(String textAlign) throws DOMException {
		parseAndApplyStyle("text-align", textAlign);

	}

	@Override
	public void setTextDecoration(String textDecoration) throws DOMException {
		parseAndApplyStyle("text-decoration", textDecoration);
	}

	@Override
	public void setTextIndent(String textIndent) throws DOMException {
		parseAndApplyStyle("text-indent", textIndent);
	}

	@Override
	public void setTextShadow(String textShadow) throws DOMException {
		parseAndApplyStyle("text-shadow", textShadow);
	}

	@Override
	public void setTextTransform(String textTransform) throws DOMException {
		parseAndApplyStyle("text-transform", textTransform);
	}

	@Override
	public void setTop(String top) throws DOMException {
		parseAndApplyStyle("top", top);
	}

	@Override
	public void setUnicodeBidi(String unicodeBidi) throws DOMException {
		parseAndApplyStyle("unicode-bidi", unicodeBidi);
	}

	@Override
	public void setVerticalAlign(String verticalAlign) throws DOMException {
		parseAndApplyStyle("vertical-align", verticalAlign);
	}

	@Override
	public void setVisibility(String visibility) throws DOMException {
		parseAndApplyStyle("visibility", visibility);
	}

	@Override
	public void setVoiceFamily(String voiceFamily) throws DOMException {
		parseAndApplyStyle("voice-family", voiceFamily);
	}

	@Override
	public void setVolume(String volume) throws DOMException {
		parseAndApplyStyle("volume", volume);
	}

	@Override
	public void setWhiteSpace(String whiteSpace) throws DOMException {
		parseAndApplyStyle("white-space", whiteSpace);
	}

	@Override
	public void setWidows(String widows) throws DOMException {
		parseAndApplyStyle("widows", widows);
	}

	@Override
	public void setWidth(String width) throws DOMException {
		parseAndApplyStyle("width", width);
	}

	@Override
	public void setWordSpacing(String wordSpacing) throws DOMException {
		parseAndApplyStyle("word-spacing", wordSpacing);
	}

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
