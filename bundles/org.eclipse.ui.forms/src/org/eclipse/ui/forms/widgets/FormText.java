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
package org.eclipse.ui.forms.widgets;
import java.io.InputStream;
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.internal.forms.widgets.*;
/**
 * This class is a read-only text control that is capable of rendering wrapped
 * text. Text can be rendered as-is or by parsing the formatting XML tags.
 * Independently, words that start with http:// can be converted into
 * hyperlinks on the fly.
 * <p>
 * When configured to use formatting XML, the control requires the root element
 * <code>form</code> to be used. The following tags can be children of the
 * <code>form</code> element:
 * </p>
 * <ul>
 * <li><b>p </b>- for defining paragraphs. The following attributes are
 * allowed:
 * <ul>
 * <li><b>vspace </b>- if set to 'false', no vertical space will be added
 * (default is 'true')</li>
 * </ul>
 * </li>
 * <li><b>li </b>- for defining list items. The following attributes are
 * allowed:
 * <ul>
 * <li><b>vspace </b>- the same as with the <b>p </b> tag</li>
 * <li><b>style </b>- could be 'bullet' (default), 'text' and 'image'</li>
 * <li><b>value </b>- not used for 'bullet'. For text, it is the value of the
 * text to rendered as a bullet. For image, it is the href of the image to be
 * rendered as a bullet.</li>
 * <li><b>indent </b>- the number of pixels to indent the text in the list
 * item</li>
 * <li><b>bindent </b>- the number of pixels to indent the bullet itself
 * </li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Text in paragraphs and list items will be wrapped according to the width of
 * the control. The following tags can appear as children of either <b>p </b>
 * or <b>li </b> elements:
 * <ul>
 * <li><b>img </b>- to render an image. Element accepts attribute 'href' that
 * is a key to the Image set using 'setImage' method.</li>
 * <li><b>a </b>- to render a hyperlink. Element accepts attribute 'href'
 * that will be provided to the hyperlink listeners via HyperlinkEvent object.
 * The element also accepts 'nowrap' attribute (default is false). When set to
 * 'true', the hyperlink will not be wrapped.</li>
 * <li><b>b </b>- the enclosed text will use bold font.</li>
 * <li><b>br </b>- forced line break (no attributes).</li>
 * <li><b>span </b>- the enclosed text will have the color and font specified
 * in the element attributes. Color is provided using 'color' attribute and is
 * a key to the Color object set by 'setColor' method. Font is provided using
 * 'font' attribute and is a key to the Font object set by 'setFont' method.
 * </li>
 * </ul>
 * <p>
 * None of the elements can nest. For example, you cannot have <b>b </b> inside
 * a <b>span </b>. This was done to keep everything simple and transparent.
 * </p>
 * <p>
 * Care should be taken when using this control. Form text is not an HTML
 * browser and should not be treated as such. If you need complex formatting
 * capabilities, use Browser widget. If you need editing capabilities and
 * font/color styles of text segments is all you need, use StyleText widget.
 * Finally, if all you need is to wrap text, use SWT Label widget and create it
 * with SWT.WRAP style.
 * <p>
 * <p>
 * You should be careful not to ask the control to render large quantities of
 * text. It does not have advanced support for dirty regions and will repaint
 * fully each time. Instead, combine the control in a composite with other
 * controls and let SWT take care of the dirty regions.
 * </p>
 * <p>Although the class is not marked final,
 * @see FormToolkit
 * @see TableWrapLayout
 * @since 3.0
 */
public final class FormText extends Canvas {
	/**
	 * The object ID to be used when registering action to handle URL
	 * hyperlinks (those that should result in opening the web browser). Value
	 * is "urlHandler".
	 */
	public static final String URL_HANDLER_ID = "urlHandler";
	/**
	 * Value of the horizontal margin (default is 0).
	 */
	public int marginWidth = 0;
	/**
	 * Value of hte horizontal margin (default is 1).
	 */
	public int marginHeight = 1;
	//private fields
	private boolean hasFocus;
	private boolean paragraphsSeparated = true;
	private FormTextModel model;
	private Vector listeners;
	private Hashtable resourceTable = new Hashtable();
	private HyperlinkSegment entered;
	private boolean mouseDown = false;
	//private Point dragOrigin;
	private Action openAction;
	private Action copyShortcutAction;
	private boolean loading = true;
	//TODO translate this text
	private String loadingText = "Loading...";
	private class FormTextLayout extends Layout implements ILayoutExtension {
		public FormTextLayout() {
		}
		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}
		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeSize(parent, 5, SWT.DEFAULT, true).x;
		}
		/*
		 * @see Layout#computeSize(Composite, int, int, boolean)
		 */
		public Point computeSize(Composite composite, int wHint, int hHint,
				boolean changed) {
			int innerWidth = wHint;
			if (isLoading()) {
				return computeLoading();
			}
			if (innerWidth != SWT.DEFAULT)
				innerWidth -= marginWidth * 2;
			Point textSize = computeTextSize(innerWidth);
			int textWidth = textSize.x + 2 * marginWidth;
			int textHeight = textSize.y + 2 * marginHeight;
			Point result = new Point(textWidth, textHeight);
			return result;
		}
		private Point computeLoading() {
			GC gc = new GC(FormText.this);
			gc.setFont(getFont());
			String loadingText = getLoadingText();
			Point size = gc.textExtent(loadingText);
			gc.dispose();
			size.x += 2 * marginWidth;
			size.y += 2 * marginHeight;
			return size;
		}
		private Point computeTextSize(int wHint) {
			Paragraph[] paragraphs = model.getParagraphs();
			GC gc = new GC(FormText.this);
			gc.setFont(getFont());
			Locator loc = new Locator();
			int width = wHint != SWT.DEFAULT ? wHint : 0;
			FontMetrics fm = gc.getFontMetrics();
			int lineHeight = fm.getHeight();
			for (int i = 0; i < paragraphs.length; i++) {
				Paragraph p = paragraphs[i];
				if (i > 0 && getParagraphsSeparated()
						&& p.getAddVerticalSpace())
					loc.y += getParagraphSpacing(lineHeight);
				loc.rowHeight = 0;
				loc.indent = p.getIndent();
				loc.x = p.getIndent();
				ParagraphSegment[] segments = p.getSegments();
				if (segments.length > 0) {
					for (int j = 0; j < segments.length; j++) {
						ParagraphSegment segment = segments[j];
						segment.advanceLocator(gc, wHint, loc, resourceTable,
								false);
						width = Math.max(width, loc.width);
					}
					loc.y += loc.rowHeight;
				} else {
					// empty new line
					loc.y += lineHeight;
				}
			}
			gc.dispose();
			return new Point(width, loc.y);
		}
		protected void layout(Composite composite, boolean flushCache) {
		}
	}
	/**
	 * Contructs a new form text widget in the provided parent and using the
	 * styles.
	 * 
	 * @param parent
	 *            form text parent control
	 * @param style
	 *            the widget style
	 */
	public FormText(Composite parent, int style) {
		super(parent, SWT.NO_BACKGROUND | SWT.WRAP | style);
		setLayout(new FormTextLayout());
		model = new FormTextModel();
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				model.dispose();
				Font boldFont = (Font)resourceTable.get(FormTextModel.BOLD_FONT_ID);
				if (boldFont!=null) {
					boldFont.dispose();
				}
			}
		});
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (e.character == '\r') {
					activateSelectedLink();
					return;
				}
			}
		});
		addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				switch (e.detail) {
					case SWT.TRAVERSE_PAGE_NEXT :
					case SWT.TRAVERSE_PAGE_PREVIOUS :
					case SWT.TRAVERSE_ARROW_NEXT :
					case SWT.TRAVERSE_ARROW_PREVIOUS :
						e.doit = false;
						return;
				}
				if (!model.hasFocusSegments()) {
					e.doit = true;
					return;
				}
				if (e.detail == SWT.TRAVERSE_TAB_NEXT)
					e.doit = advance(true);
				else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
					e.doit = advance(false);
				else if (e.detail != SWT.TRAVERSE_RETURN)
					e.doit = true;
			}
		});
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (!hasFocus) {
					hasFocus = true;
					handleFocusChange();
				}
			}
			public void focusLost(FocusEvent e) {
				if (hasFocus) {
					hasFocus = false;
					handleFocusChange();
				}
			}
		});
		addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				// select a link
				handleMouseClick(e, true);
			}
			public void mouseUp(MouseEvent e) {
				// activate a link
				handleMouseClick(e, false);
			}
		});
		addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				handleMouseMove(e);
			}
			public void mouseExit(MouseEvent e) {
				if (entered != null) {
					exitLink(entered, e.stateMask);
					paintLinkHover(entered, false);
					entered = null;
					setCursor(null);
				}
			}
			public void mouseHover(MouseEvent e) {
				handleMouseHover(e);
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				handleMouseMove(e);
			}
		});
		initAccessible();
		makeActions();
		ensureBoldFontPresent(getFont());
	}
	/**
	 * Test for focus.
	 * 
	 * @return <samp>true </samp> if the widget has focus.
	 */
	public boolean getFocus() {
		return hasFocus;
	}
	/**
	 * Test if the widget is currently processing the text it is about to
	 * render.
	 * 
	 * @return <samp>true </samp> if the widget is still loading the text,
	 *         <samp>false </samp> otherwise.
	 */
	public boolean isLoading() {
		return loading;
	}
	/**
	 * Returns the text that will be shown in the control while the real
	 * content is loading.
	 * 
	 * @return loading text message
	 */
	public String getLoadingText() {
		return loadingText;
	}
	/**
	 * Sets the text that will be shown in the control while the real content
	 * is loading. This is significant when content to render is loaded from
	 * the input stream that was created from a remote URL, and the time to
	 * load the entire content is nontrivial.
	 * 
	 * @param loadingText
	 *            loading text message
	 */
	public void setLoadingText(String loadingText) {
		this.loadingText = loadingText;
	}
	/**
	 * If paragraphs are separated, spacing will be added between them.
	 * Otherwise, new paragraphs will simply start on a new line with no
	 * spacing.
	 * 
	 * @param value
	 *            <samp>true </samp> if paragraphs are separated, </samp> false
	 *            </samp> otherwise.
	 */
	public void setParagraphsSeparated(boolean value) {
		paragraphsSeparated = value;
	}
	/**
	 * Tests if there is some inter-paragraph spacing.
	 * 
	 * @return <samp>true </samp> if paragraphs are separated, <samp>false
	 *         </samp> otherwise.
	 */
	public boolean getParagraphsSeparated() {
		return paragraphsSeparated;
	}
	/**
	 * Registers the image referenced by the provided key.
	 * <p>
	 * For <samp>img </samp> tags, an object of a type <samp>Image </samp> must
	 * be registered using the key equivalent to the value of the <samp>href
	 * </samp> attribute used in the tag.
	 * 
	 * @param key
	 *            unique key that matches the value of the <samp>href </samp>
	 *            attribute.
	 * @param image
	 *            an object of a type <samp>Image </samp>.
	 */
	public void setImage(String key, Image image) {
		resourceTable.put("i." + key, image);
	}
	/**
	 * Registers the color referenced by the provided key.
	 * <p>
	 * For <samp>span </samp> tags, an object of a type <samp>Color </samp>
	 * must be registered using the key equivalent to the value of the <samp>
	 * color </samp> attribute.
	 * 
	 * @param key
	 *            unique key that matches the value of the <samp>color </samp>
	 *            attribute.
	 * @param color
	 *            an object of a type <samp>Color </samp>.
	 */
	public void setColor(String key, Color color) {
		resourceTable.put("c." + key, color);
	}
	/**
	 * Registers the font referenced by the provided key.
	 * <p>
	 * For <samp>span </samp> tags, an object of a type <samp>Font </samp> must
	 * be registered using the key equivalent to the value of the <samp>font
	 * </samp> attribute.
	 * 
	 * @param key
	 *            unique key that matches the value of the <samp>font </samp>
	 *            attribute.
	 * @param font
	 *            an object of a type <samp>Font </samp>.
	 */
	public void setFont(String key, Font font) {
		resourceTable.put("f." + key, font);
	}
	/**
	 * Sets the font to use to render the default
	 * text (text that does not have special font property
	 * assigned). Bold font will be constructed from
	 * this font.
	 * @param font the default font to use
	 */
	public void setFont(Font font) {
		super.setFont(font);
		Font boldFont = (Font)resourceTable.get(FormTextModel.BOLD_FONT_ID);
		if (boldFont!=null) {
			boldFont.dispose();
			resourceTable.remove(FormTextModel.BOLD_FONT_ID);
		}
		ensureBoldFontPresent(getFont());
	}
	/**
	 * Sets the provided text. Text can be rendered as-is, or by parsing the
	 * formatting tags. Optionally, sections of text starting with http:// will
	 * be converted to hyperlinks.
	 * 
	 * @param text
	 *            the text to render
	 * @param parseTags
	 *            if <samp>true </samp>, formatting tags will be parsed.
	 *            Otherwise, text will be rendered as-is.
	 * @param expandURLs
	 *            if <samp>true </samp>, URLs found in the untagged text will
	 *            be converted into hyperlinks.
	 */
	public void setText(String text, boolean parseTags, boolean expandURLs) {
		if (parseTags)
			model.parseTaggedText(text, expandURLs);
		else
			model.parseRegularText(text, expandURLs);
		loading = false;
		layout();
		redraw();
	}
	/**
	 * Sets the contents of the stream. Optionally, URLs in untagged text can
	 * be converted into hyperlinks. The caller is responsible for closing the
	 * stream.
	 * 
	 * @param is
	 *            stream to render
	 * @param expandURLs
	 *            if <samp>true </samp>, URLs found in untagged text will be
	 *            converted into hyperlinks.
	 */
	public void setContents(InputStream is, boolean expandURLs) {
		model.parseInputStream(is, expandURLs);
		loading = false;
		layout();
		redraw();
	}
	/**
	 * Controls whether whitespace inside paragraph and list
	 * items is normalized. 
	 * <p>If normalized:
	 * <ul>
	 * <li>all white space characters will
	 * be condensed into at most one when between words.</li> 
	 * <li>new line characters will be ignored and replaced
	 * with one white space character</li>
	 * <li>white space characters after the opening
	 * tags and before the closing tags will be trimmed</li>
	 * @param value <code>true</code> if whitespace is
	 * normalized, <code>false</code> otherwise.
	 */
	public void setWhitespaceNormalized(boolean value) {
		model.setWhitespaceNormalized(value);
	}
	/**
	 * Tests whether whitespace inside paragraph and
	 * list item is normalized.
	 * @see #setWhitespaceNormalized(boolean)
	 * @return <code>true</code> if whitespace is
	 * normalized, <code>false</code> otherwise.
	 */
	public boolean isWhitespaceNormalized() {
		return model.isWhitespaceNormalized();
	}
	/**
	 * Sets the focus to the first hyperlink, or the widget itself if there are
	 * no hyperlinks.
	 * 
	 * @return <samp>true </samp> if the control got focus, <samp>false </samp>
	 *         otherwise.
	 */
	public boolean setFocus() {
		/*
		 * if (!model.hasFocusSegments()) return false;
		 */
		return super.setFocus();
	}
	/**
	 * Returns the hyperlink settings that are in effect for this control.
	 * 
	 * @return current hyperlinks settings
	 */
	public HyperlinkSettings getHyperlinkSettings() {
		return model.getHyperlinkSettings();
	}
	/**
	 * Sets the hyperlink settings to be used for this control. Settings will
	 * affect things like hyperlink color, rendering style, cursor etc.
	 * 
	 * @param settings
	 *            hyperlink settings for this control
	 */
	public void setHyperlinkSettings(HyperlinkSettings settings) {
		model.setHyperlinkSettings(settings);
	}
	/**
	 * Adds a listener that will handle hyperlink events.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addHyperlinkListener(IHyperlinkListener listener) {
		if (listeners == null)
			listeners = new Vector();
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	/**
	 * Removes the hyperlink listener.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeHyperlinkListener(IHyperlinkListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}
	/**
	 * Context menu is about to show - override to add actions to the menu
	 * manager. Subclasses are required to call 'super' when overriding.
	 * 
	 * @param manager
	 *            the pop-up menu manager
	 */
	protected void contextMenuAboutToShow(IMenuManager manager) {
		HyperlinkSegment link = model.getSelectedLink();
		if (link != null)
			contributeLinkActions(manager, link);
	}
	private void makeActions() {
		/*
		 * openAction = new Action() { public void run() {
		 * activateSelectedLink(); } }; openAction.setText(
		 * FormsPlugin.getResourceString("FormEgine.linkPopup.open"));
		 * copyShortcutAction = new Action() { public void run() {
		 * copyShortcut(model.getSelectedLink()); } };
		 * copyShortcutAction.setText(
		 * FormsPlugin.getResourceString("FormEgine.linkPopup.copyShortcut"));
		 */
	}
	private String getAcessibleText() {
		return model.getAccessibleText();
	}
	private void initAccessible() {
		Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = getAcessibleText();
			}
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});
		accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = toControl(new Point(e.x, e.y));
				e.childID = (getBounds().contains(pt))
						? ACC.CHILDID_SELF
						: ACC.CHILDID_NONE;
			}
			public void getLocation(AccessibleControlEvent e) {
				Rectangle location = getBounds();
				Point pt = toDisplay(new Point(location.x, location.y));
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}
			public void getChildCount(AccessibleControlEvent e) {
				e.detail = 0;
			}
			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_TEXT;
			}
			public void getState(AccessibleControlEvent e) {
				e.detail = ACC.STATE_READONLY;
			}
		});
	}
	private void handleMouseClick(MouseEvent e, boolean down) {
		if (down) {
			// select a hyperlink
			HyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
			if (segmentUnder != null) {
				HyperlinkSegment oldLink = model.getSelectedLink();
				model.selectLink(segmentUnder);
				enterLink(segmentUnder, e.stateMask);
				paintFocusTransfer(oldLink, segmentUnder);
			}
			mouseDown = true;
			//dragOrigin = new Point(e.x, e.y);
		} else {
			if (e.button == 1) {
				HyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
				if (segmentUnder != null) {
					activateLink(segmentUnder, e.stateMask);
				}
			}
			mouseDown = false;
		}
	}
	private void handleMouseHover(MouseEvent e) {
	}
	private void handleMouseMove(MouseEvent e) {
		if (mouseDown) {
			handleDrag(e);
			return;
		}
		TextSegment segmentUnder = model.findSegmentAt(e.x, e.y);
		if (segmentUnder == null) {
			if (entered != null) {
				exitLink(entered, e.stateMask);
				paintLinkHover(entered, false);
				entered = null;
			}
			setCursor(null);
		} else {
			if (segmentUnder instanceof HyperlinkSegment) {
				HyperlinkSegment linkUnder = (HyperlinkSegment) segmentUnder;
				if (entered == null) {
					entered = linkUnder;
					enterLink(linkUnder, e.stateMask);
					paintLinkHover(entered, true);
					setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
				}
			} else {
				if (entered != null) {
					exitLink(entered, e.stateMask);
					paintLinkHover(entered, false);
					entered = null;
				}
				setCursor(model.getHyperlinkSettings().getTextCursor());
			}
		}
	}
	private boolean advance(boolean next) {
		HyperlinkSegment current = model.getSelectedLink();
		if (current != null)
			exitLink(current, SWT.NULL);
		boolean valid = model.traverseLinks(next);
		HyperlinkSegment newLink = model.getSelectedLink();
		if (valid)
			enterLink(newLink, SWT.NULL);
		paintFocusTransfer(current, newLink);
		if (newLink != null)
			ensureVisible(newLink);
		return !valid;
	}
	private void handleFocusChange() {
		if (hasFocus) {
			model.traverseLinks(true);
			enterLink(model.getSelectedLink(), SWT.NULL);
			paintFocusTransfer(null, model.getSelectedLink());
			ensureVisible(model.getSelectedLink());
		} else {
			paintFocusTransfer(model.getSelectedLink(), null);
			model.selectLink(null);
		}
	}
	private void enterLink(HyperlinkSegment link, int stateMask) {
		if (link == null || listeners == null)
			return;
		int size = listeners.size();
		HyperlinkEvent he = new HyperlinkEvent(this, link.getHref(), link
				.getText(), stateMask);
		for (int i = 0; i < size; i++) {
			IHyperlinkListener listener = (IHyperlinkListener) listeners.get(i);
			listener.linkEntered(he);
		}
	}
	private void exitLink(HyperlinkSegment link, int stateMask) {
		if (link == null || listeners == null)
			return;
		int size = listeners.size();
		HyperlinkEvent he = new HyperlinkEvent(this, link.getHref(), link
				.getText(), stateMask);
		for (int i = 0; i < size; i++) {
			IHyperlinkListener listener = (IHyperlinkListener) listeners.get(i);
			listener.linkExited(he);
		}
	}
	private void paintLinkHover(HyperlinkSegment link, boolean hover) {
		GC gc = new GC(this);
		HyperlinkSettings settings = getHyperlinkSettings();
		gc.setForeground(hover ? settings.getActiveForeground() : settings
				.getForeground());
		gc.setBackground(getBackground());
		gc.setFont(getFont());
		boolean selected = (link == model.getSelectedLink());
		link.repaint(gc, hover);
		if (selected) {
			link.paintFocus(gc, getBackground(), getForeground(), false);
			link.paintFocus(gc, getBackground(), getForeground(), true);
		}
		gc.dispose();
	}
	private void activateSelectedLink() {
		HyperlinkSegment link = model.getSelectedLink();
		if (link != null)
			activateLink(link, SWT.NULL);
	}
	private void activateLink(HyperlinkSegment link, int stateMask) {
		setCursor(model.getHyperlinkSettings().getBusyCursor());
		if (listeners != null) {
			int size = listeners.size();
			HyperlinkEvent e = new HyperlinkEvent(this, link.getHref(), link
					.getText(), stateMask);
			for (int i = 0; i < size; i++) {
				IHyperlinkListener listener = (IHyperlinkListener) listeners
						.get(i);
				listener.linkActivated(e);
			}
		}
		if (!isDisposed())
			setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
	}
	private void ensureBoldFontPresent(Font regularFont) {
		Font boldFont = (Font)resourceTable.get(FormTextModel.BOLD_FONT_ID);
		if (boldFont!=null) return;
		FontData[] fontDatas = regularFont.getFontData();
		for (int i = 0; i < fontDatas.length; i++) {
			fontDatas[i].setStyle(fontDatas[i].getStyle() | SWT.BOLD);
		}
		boldFont = new Font(getDisplay(), fontDatas);
		resourceTable.put(FormTextModel.BOLD_FONT_ID, boldFont);
	}
	private void paint(PaintEvent e) {
		Rectangle carea = getClientArea();
		GC gc = e.gc;
		gc.setFont(getFont());
		ensureBoldFontPresent(getFont());
		gc.setForeground(getForeground());
		gc.setBackground(getBackground());
		
		Locator loc = new Locator();
		loc.marginWidth = marginWidth;
		loc.marginHeight = marginHeight;
		loc.x = marginWidth;
		loc.y = marginHeight;
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		// Use double-buffering to reduce flicker
		Image textBuffer = new Image(getDisplay(), carea.width, carea.height);
		textBuffer.setBackground(getBackground());
		GC textGC = new GC(textBuffer, gc.getStyle());
		textGC.setForeground(getForeground());
		textGC.setBackground(getBackground());
		textGC.setFont(getFont());
		textGC.fillRectangle(0, 0, carea.width, carea.height);
		
		if (loading) {
			int textWidth = gc.textExtent(loadingText).x;
			textGC.drawText(loadingText, carea.width / 2 - textWidth / 2,
					getClientArea().height / 2 - lineHeight / 2);
		} else {
			Paragraph[] paragraphs = model.getParagraphs();
			HyperlinkSegment selectedLink = model.getSelectedLink();
			for (int i = 0; i < paragraphs.length; i++) {
				Paragraph p = paragraphs[i];
				if (i > 0 && paragraphsSeparated && p.getAddVerticalSpace())
					loc.y += getParagraphSpacing(lineHeight);
				loc.indent = p.getIndent();
				loc.resetCaret();
				loc.rowHeight = 0;
				p.paint(textGC, carea.width, loc, lineHeight, resourceTable,
						selectedLink);
			}
		}
		gc.drawImage(textBuffer, 0, 0);
		textGC.dispose();
		textBuffer.dispose();
	}
	private int getParagraphSpacing(int lineHeight) {
		return lineHeight / 2;
	}
	private void paintFocusTransfer(HyperlinkSegment oldLink,
			HyperlinkSegment newLink) {
		GC gc = new GC(this);
		Color bg = getBackground();
		Color fg = getForeground();
		gc.setFont(getFont());
		if (oldLink != null) {
			gc.setBackground(bg);
			gc.setForeground(fg);
			oldLink.paintFocus(gc, bg, fg, false);
		}
		if (newLink != null) {
			//ensureVisible(newLink);
			gc.setBackground(bg);
			gc.setForeground(fg);
			newLink.paintFocus(gc, bg, fg, true);
		}
		gc.dispose();
	}
	private void contributeLinkActions(IMenuManager manager,
			HyperlinkSegment link) {
		manager.add(openAction);
		manager.add(copyShortcutAction);
		manager.add(new Separator());
	}
	private void copyShortcut(HyperlinkSegment link) {
		String text = link.getText();
		Clipboard clipboard = new Clipboard(getDisplay());
		clipboard.setContents(new Object[]{text}, new Transfer[]{TextTransfer
				.getInstance()});
	}
	private void ensureVisible(HyperlinkSegment segment) {
		if (segment == null)
			return;
		Rectangle bounds = segment.getBounds();
		ScrolledComposite scomp = FormUtil.getScrolledComposite(this);
		if (scomp == null)
			return;
		Point origin = FormUtil.getControlLocation(scomp, this);
		origin.x += bounds.x;
		origin.y += bounds.y;
		FormUtil.ensureVisible(scomp, origin, new Point(bounds.width,
				bounds.height));
	}

	private void handleDrag(MouseEvent e) {
	}
	/**
	 * Overrides the method by fully trusting the layout manager (computed
	 * width or height may be larger than the provider width or height hints).
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point size;
		FormTextLayout layout = (FormTextLayout) getLayout();
		if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
			size = layout.computeSize(this, wHint, hHint, changed);
		} else {
			size = new Point(wHint, hHint);
		}
		Rectangle trim = computeTrim(0, 0, size.x, size.y);
		return new Point(trim.width, trim.height);
	}
}
