/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Martin Donnelly (m2a3@eircom.net) - patch (see Bugzilla #145997)
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageGcDrawer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.internal.forms.Messages;
import org.eclipse.ui.internal.forms.widgets.ControlSegment;
import org.eclipse.ui.internal.forms.widgets.FormFonts;
import org.eclipse.ui.internal.forms.widgets.FormTextModel;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.eclipse.ui.internal.forms.widgets.IFocusSelectable;
import org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment;
import org.eclipse.ui.internal.forms.widgets.ImageSegment;
import org.eclipse.ui.internal.forms.widgets.Locator;
import org.eclipse.ui.internal.forms.widgets.Paragraph;
import org.eclipse.ui.internal.forms.widgets.ParagraphSegment;
import org.eclipse.ui.internal.forms.widgets.SelectionData;
import org.eclipse.ui.internal.forms.widgets.TextSegment;

/**
 * This class is a read-only text control that is capable of rendering wrapped
 * text. Text can be rendered as-is or by parsing the formatting XML tags.
 * Independently, words that start with http:// can be converted into hyperlinks
 * on the fly.
 * <p>
 * When configured to use formatting XML, the control requires the root element
 * <code>form</code> to be used and requires any ampersand (&amp;) characters in the text to
 * be replaced by the entity <b>&amp;amp;</b>. The following tags can be children of the
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
 * text that is rendered as a bullet. For image, it is the href of the image to
 * be rendered as a bullet.</li>
 * <li><b>indent </b>- the number of pixels to indent the text in the list item
 * </li>
 * <li><b>bindent </b>- the number of pixels to indent the bullet itself</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Text in paragraphs and list items will be wrapped according to the width of
 * the control. The following tags can appear as children of either <b>p </b> or
 * <b>li </b> elements:
 * <ul>
 * <li><b>img </b>- to render an image. Element accepts attribute 'href' that
 * is a key to the <code>Image</code> set using 'setImage' method. Vertical
 * position of image relative to surrounding text is optionally controlled by
 * the attribute <b>align</b> that can have values <b>top</b>, <b>middle</b>
 * and <b>bottom</b></li>
 * <li><b>a </b>- to render a hyperlink. Element accepts attribute 'href' that
 * will be provided to the hyperlink listeners via HyperlinkEvent object. The
 * element also accepts 'nowrap' attribute (default is false). When set to
 * 'true', the hyperlink will not be wrapped. Hyperlinks automatically created
 * when 'http://' is encountered in text are not wrapped.</li>
 * <li><b>b </b>- the enclosed text will use bold font.</li>
 * <li><b>br </b>- forced line break (no attributes).</li>
 * <li><b>span </b>- the enclosed text will have the color and font specified
 * in the element attributes. Color is provided using 'color' attribute and is a
 * key to the Color object set by 'setColor' method. Font is provided using
 * 'font' attribute and is a key to the Font object set by 'setFont' method. As with
 * hyperlinks, it is possible to block wrapping by setting 'nowrap' to true
 * (false by default).
 * </li>
 * <li><b>control (new in 3.1)</b> - to place a control that is a child of the
 * text control. Element accepts attribute 'href' that is a key to the Control
 * object set using 'setControl' method. Optionally, attribute 'fill' can be set
 * to <code>true</code> to make the control fill the entire width of the text.
 * Form text is not responsible for creating or disposing controls, it only
 * places them relative to the surrounding text. Similar to <b>img</b>,
 * vertical position of the control can be set using the <b>align</b>
 * attribute. In addition, <b>width</b> and <b>height</b> attributes can
 * be used to force the dimensions of the control. If not used,
 * the preferred control size will be used.
 * </ul>
 * <p>
 * None of the elements can nest. For example, you cannot have <b>b </b> inside
 * a <b>span </b>. This was done to keep everything simple and transparent.
 * Since 3.1, an exception to this rule has been added to support nesting images
 * and text inside the hyperlink tag (<b>a</b>). Image enclosed in the
 * hyperlink tag acts as a hyperlink, can be clicked on and can accept and
 * render selection focus. When both text and image is enclosed, selection and
 * rendering will affect both as a single hyperlink.
 * </p>
 * <p>
 * Since 3.1, it is possible to select text. Text selection can be
 * programmatically accessed and also copied to clipboard. Non-textual objects
 * (images, controls etc.) in the selection range are ignored.
 * <p>
 * Care should be taken when using this control. Form text is not an HTML
 * browser and should not be treated as such. If you need complex formatting
 * capabilities, use Browser widget. If you need editing capabilities and
 * font/color styles of text segments is all you need, use StyleText widget.
 * Finally, if all you need is to wrap text, use SWT Label widget and create it
 * with SWT.WRAP style.
 *
 * @see FormToolkit
 * @see TableWrapLayout
 * @since 3.0
 */
public class FormText extends Canvas {
	/**
	 * The object ID to be used when registering action to handle URL hyperlinks
	 * (those that should result in opening the web browser). Value is
	 * "urlHandler".
	 */
	public static final String URL_HANDLER_ID = "urlHandler"; //$NON-NLS-1$

	/**
	 * Value of the horizontal margin (default is 0).
	 */
	public int marginWidth = 0;

	/**
	 * Value of tue vertical margin (default is 1).
	 */
	public int marginHeight = 1;

	// private fields
	private static final boolean DEBUG_TEXT = false;//"true".equalsIgnoreCase(Platform.getDebugOption(FormUtil.DEBUG_TEXT));
	private static final boolean DEBUG_TEXTSIZE = false;//"true".equalsIgnoreCase(Platform.getDebugOption(FormUtil.DEBUG_TEXTSIZE));

	private static final boolean DEBUG_FOCUS = false;//"true".equalsIgnoreCase(Platform.getDebugOption(FormUtil.DEBUG_FOCUS));

	private boolean hasFocus;

	private boolean paragraphsSeparated = true;

	private FormTextModel model;

	private ListenerList<IHyperlinkListener> listeners;

	private Hashtable<String, Object> resourceTable = new Hashtable<>();

	private IHyperlinkSegment entered;

	private IHyperlinkSegment armed;

	private boolean mouseFocus = false;

	private boolean controlFocusTransfer = false;

	private boolean inSelection = false;

	private SelectionData selData;

	private static final String INTERNAL_MENU = "__internal_menu__"; //$NON-NLS-1$

	private static final String CONTROL_KEY = "__segment__"; //$NON-NLS-1$

	private class FormTextLayout extends Layout implements ILayoutExtension {
		public FormTextLayout() {
		}

		@Override
		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}

		@Override
		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeSize(parent, 5, SWT.DEFAULT, true).x;
		}

		@Override
		public Point computeSize(Composite composite, int wHint, int hHint,
				boolean changed) {
			long start = 0;

			if (DEBUG_TEXT)
				start = System.currentTimeMillis();
			int innerWidth = wHint;
			if (innerWidth != SWT.DEFAULT)
				innerWidth -= marginWidth * 2;
			Point textSize = computeTextSize(innerWidth);
			int textWidth = textSize.x + 2 * marginWidth;
			int textHeight = textSize.y + 2 * marginHeight;
			Point result = new Point(textWidth, textHeight);
			if (DEBUG_TEXT) {
				long stop = System.currentTimeMillis();
				System.out.println("FormText computeSize: " + (stop - start) //$NON-NLS-1$
						+ "ms"); //$NON-NLS-1$
			}
			if (DEBUG_TEXTSIZE) {
				System.out.println("FormText ("+model.getAccessibleText()+"), computeSize: wHint="+wHint+", result="+result); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return result;
		}

		private Point computeTextSize(int wHint) {
			Paragraph[] paragraphs = model.getParagraphs();
			GC gc = new GC(FormText.this);
			gc.setFont(getFont());
			Locator loc = new Locator();
			int width = wHint != SWT.DEFAULT ? wHint : 0;
			FontMetrics fm = gc.getFontMetrics();
			int lineHeight = fm.getHeight();
			boolean selectableInTheLastRow = false;
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
					selectableInTheLastRow = false;
					int pwidth = 0;
					for (ParagraphSegment segment : segments) {
						segment.advanceLocator(gc, wHint, loc, resourceTable, false);
						if (wHint != SWT.DEFAULT) {
							width = Math.max(width, loc.width);
						} else {
							pwidth = Math.max(pwidth, loc.width);
						}
						if (segment instanceof IFocusSelectable)
							selectableInTheLastRow = true;
					}
					if (wHint == SWT.DEFAULT)
						width = Math.max(width, pwidth);
					loc.y += loc.rowHeight;
				} else {
					// empty new line
					loc.y += lineHeight;
				}
			}
			gc.dispose();
			if (selectableInTheLastRow)
				loc.y += 1;
			return new Point(width, loc.y);
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			long start = 0;

			if (DEBUG_TEXT) {
				start = System.currentTimeMillis();
			}
			selData = null;
			Rectangle carea = composite.getClientArea();
			if (DEBUG_TEXTSIZE) {
				System.out.println("FormText layout ("+model.getAccessibleText()+"), carea="+carea); //$NON-NLS-1$ //$NON-NLS-2$
			}
			GC gc = new GC(composite);
			gc.setFont(getFont());
			ensureBoldFontPresent(getFont());
			gc.setForeground(getForeground());
			gc.setBackground(getBackground());

			Locator loc = new Locator();
			loc.marginWidth = marginWidth;
			loc.marginHeight = marginHeight;
			loc.y = marginHeight;
			FontMetrics fm = gc.getFontMetrics();
			int lineHeight = fm.getHeight();

			Paragraph[] paragraphs = model.getParagraphs();
			IHyperlinkSegment selectedLink = getSelectedLink();
			for (int i = 0; i < paragraphs.length; i++) {
				Paragraph p = paragraphs[i];
				if (i > 0 && paragraphsSeparated && p.getAddVerticalSpace())
					loc.y += getParagraphSpacing(lineHeight);
				loc.indent = p.getIndent();
				loc.resetCaret();
				loc.rowHeight = 0;
				p.layout(gc, carea.width, loc, lineHeight, resourceTable,
						selectedLink);
			}
			gc.dispose();
			if (DEBUG_TEXT) {
				long stop = System.currentTimeMillis();
				System.out.println("FormText.layout: " + (stop - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Contructs a new form text widget in the provided parent and using the
	 * styles.
	 * <p>
	 * The only valid style bit for <code>FormText</code> is <code>SWT.NO_FOCUS</code>.
	 * This will cause the widget to always refuse focus.
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
		addDisposeListener(e -> {
			model.dispose();
			disposeResourceTable(true);
		});
		addPaintListener(this::paint);
		addListener(SWT.KeyDown, e -> {
			if (e.character == '\r') {
				activateSelectedLink();
				return;
			}
		});
		addListener(SWT.Traverse, e -> {
			if (DEBUG_FOCUS)
				System.out.println("Traversal: " + e); //$NON-NLS-1$
			switch (e.detail) {
			case SWT.TRAVERSE_PAGE_NEXT:
			case SWT.TRAVERSE_PAGE_PREVIOUS:
			case SWT.TRAVERSE_ARROW_NEXT:
			case SWT.TRAVERSE_ARROW_PREVIOUS:
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
		});
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (!hasFocus) {
					hasFocus = true;
					if (DEBUG_FOCUS) {
						System.out.println("FormText: focus gained"); //$NON-NLS-1$
					}
					if (!mouseFocus && !controlFocusTransfer) {
						handleFocusChange();
					}
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (DEBUG_FOCUS) {
					System.out.println("FormText: focus lost"); //$NON-NLS-1$
				}
				if (hasFocus) {
					hasFocus = false;
					if (!controlFocusTransfer)
						handleFocusChange();
				}
			}
		});
		addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// select a link
				handleMouseClick(e, true);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// activate a link
				handleMouseClick(e, false);
			}
		});
		addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseEnter(MouseEvent e) {
				handleMouseMove(e);
			}

			@Override
			public void mouseExit(MouseEvent e) {
				if (entered != null) {
					exitLink(entered, e.stateMask);
					paintLinkHover(entered, false);
					entered = null;
					setCursor(null);
				}
			}

			@Override
			public void mouseHover(MouseEvent e) {
				handleMouseHover(e);
			}
		});
		addMouseMoveListener(this::handleMouseMove);
		initAccessible();
		ensureBoldFontPresent(getFont());
		createMenu();
		// we will handle traversal of controls, if any
		setTabList(new Control[] {});
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
	 * @deprecated not used any more - returns <code>false</code>
	 */
	@Deprecated
	public boolean isLoading() {
		return false;
	}

	/**
	 * Returns the text that will be shown in the control while the real content
	 * is loading.
	 *
	 * @return loading text message
	 * @deprecated loading text is not used since 3.1
	 */
	@Deprecated
	public String getLoadingText() {
		return null;
	}

	/**
	 * Sets the text that will be shown in the control while the real content is
	 * loading. This is significant when content to render is loaded from the
	 * input stream that was created from a remote URL, and the time to load the
	 * entire content is nontrivial.
	 *
	 * @param loadingText
	 *            loading text message
	 * @deprecated use setText(loadingText, false, false);
	 */
	@Deprecated
	public void setLoadingText(String loadingText) {
		setText(loadingText, false, false);
	}

	/**
	 * If paragraphs are separated, spacing will be added between them. Otherwise,
	 * new paragraphs will simply start on a new line with no spacing.
	 *
	 * @param value <code>true</code> if paragraphs are separated, <code>false
	 *              </code> otherwise.
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
		resourceTable.put("i." + key, image); //$NON-NLS-1$
	}

	/**
	 * Registers the color referenced by the provided key.
	 * <p>
	 * For <samp>span </samp> tags, an object of a type <samp>Color </samp> must
	 * be registered using the key equivalent to the value of the <samp>color
	 * </samp> attribute.
	 *
	 * @param key
	 *            unique key that matches the value of the <samp>color </samp>
	 *            attribute.
	 * @param color
	 *            an object of the type <samp>Color </samp> or <samp>null</samp>
	 *            if the key needs to be cleared.
	 */
	public void setColor(String key, Color color) {
		String fullKey = "c." + key; //$NON-NLS-1$
		if (color == null)
			resourceTable.remove(fullKey);
		else
			resourceTable.put(fullKey, color);
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
	 *            an object of the type <samp>Font </samp> or <samp>null</samp>
	 *            if the key needs to be cleared.
	 */
	public void setFont(String key, Font font) {
		String fullKey = "f." + key; //$NON-NLS-1$
		if (font == null)
			resourceTable.remove(fullKey);
		else
			resourceTable.put(fullKey, font);
		model.clearCache(fullKey);
	}

	/**
	 * Registers the control referenced by the provided key.
	 * <p>
	 * For <samp>control</samp> tags, an object of a type <samp>Control</samp>
	 * must be registered using the key equivalent to the value of the
	 * <samp>control</samp> attribute.
	 *
	 * @param key
	 *            unique key that matches the value of the <samp>control</samp>
	 *            attribute.
	 * @param control
	 *            an object of the type <samp>Control</samp> or <samp>null</samp>
	 *            if the existing control at the specified key needs to be
	 *            removed.
	 * @since 3.1
	 */
	public void setControl(String key, Control control) {
		String fullKey = "o." + key; //$NON-NLS-1$
		if (control == null)
			resourceTable.remove(fullKey);
		else
			resourceTable.put(fullKey, control);
	}

	/**
	 * Sets the font to use to render the default text (text that does not have
	 * special font property assigned). Bold font will be constructed from this
	 * font.
	 *
	 * @param font
	 *            the default font to use
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		model.clearCache(null);
		Font boldFont = (Font) resourceTable.get(FormTextModel.BOLD_FONT_ID);
		if (boldFont != null) {
			FormFonts.getInstance().markFinished(boldFont, getDisplay());
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
	 *            if <samp>true </samp>, URLs found in the untagged text will be
	 *            converted into hyperlinks.
	 */
	public void setText(String text, boolean parseTags, boolean expandURLs) {
		disposeResourceTable(false);
		entered = null;
		if (parseTags)
			model.parseTaggedText(text, expandURLs);
		else
			model.parseRegularText(text, expandURLs);
		hookControlSegmentFocus();
		layout();
		redraw();
	}

	/**
	 * Sets the contents of the stream. Optionally, URLs in untagged text can be
	 * converted into hyperlinks. The caller is responsible for closing the
	 * stream.
	 *
	 * @param is
	 *            stream to render
	 * @param expandURLs
	 *            if <samp>true </samp>, URLs found in untagged text will be
	 *            converted into hyperlinks.
	 */
	public void setContents(InputStream is, boolean expandURLs) {
		entered = null;
		disposeResourceTable(false);
		model.parseInputStream(is, expandURLs);
		hookControlSegmentFocus();
		layout();
		redraw();
	}

	private void hookControlSegmentFocus() {
		Paragraph[] paragraphs = model.getParagraphs();
		if (paragraphs == null)
			return;
		Listener listener = e -> {
			switch (e.type) {
			case SWT.FocusIn:
				if (!controlFocusTransfer)
					syncControlSegmentFocus((Control) e.widget);
				break;
			case SWT.Traverse:
				if (DEBUG_FOCUS)
					System.out.println("Control traversal: " + e); //$NON-NLS-1$
				switch (e.detail) {
				case SWT.TRAVERSE_PAGE_NEXT:
				case SWT.TRAVERSE_PAGE_PREVIOUS:
				case SWT.TRAVERSE_ARROW_NEXT:
				case SWT.TRAVERSE_ARROW_PREVIOUS:
					e.doit = false;
					return;
				}
				Control c = (Control) e.widget;
				ControlSegment segment = (ControlSegment) c.getData(CONTROL_KEY);
				if (e.detail == SWT.TRAVERSE_TAB_NEXT)
					e.doit = advanceControl(c, segment, true);
				else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
					e.doit = advanceControl(c, segment, false);
				if (!e.doit)
					e.detail = SWT.TRAVERSE_NONE;
				break;
			}
		};
		for (Paragraph p : paragraphs) {
			ParagraphSegment[] segments = p.getSegments();
			for (ParagraphSegment segment : segments) {
				if (segment instanceof ControlSegment) {
					ControlSegment cs = (ControlSegment) segment;
					Control c = cs.getControl(resourceTable);
					if (c != null) {
						if (c.getData(CONTROL_KEY) == null) {
							// first time - hook
							c.setData(CONTROL_KEY, cs);
							attachTraverseListener(c, listener);
						}
					}
				}
			}
		}
	}

	private void attachTraverseListener(Control c, Listener listener) {
		if (c instanceof Composite) {
			Composite parent = (Composite) c;
			Control[] children = parent.getChildren();
			for (Control element : children) {
				attachTraverseListener(element, listener);
			}
			if (c instanceof Canvas) {
				// If Canvas, the control iteself can accept
				// traverse events and should be monitored
				c.addListener(SWT.Traverse, listener);
				c.addListener(SWT.FocusIn, listener);
			}
		} else {
			c.addListener(SWT.Traverse, listener);
			c.addListener(SWT.FocusIn, listener);
		}
	}

	/**
	 * If we click on the control randomly, our internal book-keeping will be
	 * off. We need to update the model and mark the control segment and
	 * currently selected. Hyperlink that may have had focus must also be
	 * exited.
	 *
	 * @param control
	 *            the control that got focus
	 */
	private void syncControlSegmentFocus(Control control) {
		ControlSegment cs = null;

		while (control != null) {
			cs = (ControlSegment) control.getData(CONTROL_KEY);
			if (cs != null)
				break;
			control = control.getParent();
		}
		if (cs == null)
			return;
		IFocusSelectable current = model.getSelectedSegment();
		// If the model and the control match, all is well
		if (current == cs)
			return;
		IHyperlinkSegment oldLink = null;
		if (current != null && current instanceof IHyperlinkSegment) {
			oldLink = (IHyperlinkSegment) current;
			exitLink(oldLink, SWT.NULL);
		}
		if (DEBUG_FOCUS)
			System.out.println("Sync control: " + cs + ", oldLink=" + oldLink); //$NON-NLS-1$ //$NON-NLS-2$
		model.select(cs);
		if (oldLink != null)
			paintFocusTransfer(oldLink, null);
		// getAccessible().setFocus(model.getSelectedSegmentIndex());
	}

	private boolean advanceControl(Control c, ControlSegment segment,
			boolean next) {
		Composite parent = c.getParent();
		if (parent == this) {
			// segment-level control
			IFocusSelectable nextSegment = model.getNextFocusSegment(next);
			if (nextSegment != null) {
				controlFocusTransfer = true;
				super.forceFocus();
				controlFocusTransfer = false;
				model.select(segment);
				return advance(next);
			}
			// nowhere to go
			return setFocusToNextSibling(this, next);
		}
		if (setFocusToNextSibling(c, next))
			return true;
		// still here - must go one level up
		segment = (ControlSegment) parent.getData(CONTROL_KEY);
		return advanceControl(parent, segment, next);
	}

	private boolean setFocusToNextSibling(Control c, boolean next) {
		Composite parent = c.getParent();
		Control[] children = parent.getTabList();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			if (child == c) {
				// here
				if (next) {
					for (int j = i + 1; j < children.length; j++) {
						Control nc = children[j];
						if (nc.setFocus())
							return false;
					}
				} else {
					for (int j = i - 1; j >= 0; j--) {
						Control pc = children[j];
						if (pc.setFocus())
							return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Controls whether whitespace inside paragraph and list items is normalized.
	 * Note that the new value will not affect the current text in the control, only
	 * subsequent calls to <code>setText</code> or <code>setContents</code>.
	 * <p>
	 * If normalized:
	 * </p>
	 * <ul>
	 * <li>all white space characters will be condensed into at most one when
	 * between words.</li>
	 * <li>new line characters will be ignored and replaced with one white space
	 * character</li>
	 * <li>white space characters after the opening tags and before the closing tags
	 * will be trimmed</li>
	 * </ul>
	 *
	 * @param value <code>true</code> if whitespace is normalized,
	 *              <code>false</code> otherwise.
	 */
	public void setWhitespaceNormalized(boolean value) {
		model.setWhitespaceNormalized(value);
	}

	/**
	 * Tests whether whitespace inside paragraph and list item is normalized.
	 *
	 * @see #setWhitespaceNormalized(boolean)
	 * @return <code>true</code> if whitespace is normalized,
	 *         <code>false</code> otherwise.
	 */
	public boolean isWhitespaceNormalized() {
		return model.isWhitespaceNormalized();
	}

	/**
	 * Disposes the internal menu if created and sets the menu provided as a
	 * parameter.
	 *
	 * @param menu
	 *            the menu to associate with this text control
	 */
	@Override
	public void setMenu(Menu menu) {
		Menu currentMenu = super.getMenu();
		if (currentMenu != null && INTERNAL_MENU.equals(currentMenu.getData())) {
			// internal menu set
			if (menu != null) {
				currentMenu.dispose();
				super.setMenu(menu);
			}
		} else
			super.setMenu(menu);
	}

	private void createMenu() {
		Menu menu = new Menu(this);
		final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
		copyItem.setText(Messages.FormText_copy);

		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == copyItem) {
					copy();
				}
			}
		};
		copyItem.addSelectionListener(listener);
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				copyItem.setEnabled(canCopy());
			}

			@Override
			public void menuHidden(MenuEvent e) {
			}
		});
		menu.setData(INTERNAL_MENU);
		super.setMenu(menu);
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
			listeners = new ListenerList<>();
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
	 * Adds a selection listener. A Selection event is sent by the widget when
	 * the selection has changed.
	 * <p>
	 * <code>widgetDefaultSelected</code> is not called for FormText.
	 * </p>
	 *
	 * @param listener
	 *            the listener
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT when listener is null</li>
	 *                </ul>
	 * @since 3.1
	 */
	public void addSelectionListener(SelectionListener listener) {
		addTypedListener(listener, SWT.Selection);
	}

	/**
	 * Removes the specified selection listener.
	 *
	 * @param listener
	 *            the listener
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT when listener is null</li>
	 *                </ul>
	 * @since 3.1
	 */
	public void removeSelectionListener(SelectionListener listener) {
		removeTypedListener(SWT.Selection, listener);
	}

	/**
	 * Returns the selected text.
	 *
	 * @return selected text, or an empty String if there is no selection.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * @since 3.1
	 */

	public String getSelectionText() {
		checkWidget();
		if (selData != null)
			return selData.getSelectionText();
		return ""; //$NON-NLS-1$
	}

	/**
	 * Tests if the text is selected and can be copied into the clipboard.
	 *
	 * @return <code>true</code> if the selected text can be copied into the
	 *         clipboard, <code>false</code> otherwise.
	 * @since 3.1
	 */
	public boolean canCopy() {
		return selData != null && selData.canCopy();
	}

	/**
	 * Copies the selected text into the clipboard. Does nothing if no text is
	 * selected or the text cannot be copied for any other reason.
	 *
	 * @since 3.1
	 */

	public void copy() {
		if (!canCopy())
			return;
		Clipboard clipboard = new Clipboard(getDisplay());
		Object[] o = new Object[] { getSelectionText() };
		Transfer[] t = new Transfer[] { TextTransfer.getInstance() };
		clipboard.setContents(o, t);
		clipboard.dispose();
	}

	/**
	 * Returns the reference of the hyperlink that currently has keyboard focus,
	 * or <code>null</code> if there are no hyperlinks in the receiver or no
	 * hyperlink has focus at the moment.
	 *
	 * @return href of the selected hyperlink or <code>null</code> if none
	 *         selected.
	 * @since 3.1
	 */
	public Object getSelectedLinkHref() {
		IHyperlinkSegment link = getSelectedLink();
		return link != null ? link.getHref() : null;
	}

	/**
	 * Returns the text of the hyperlink that currently has keyboard focus, or
	 * <code>null</code> if there are no hyperlinks in the receiver or no
	 * hyperlink has focus at the moment.
	 *
	 * @return text of the selected hyperlink or <code>null</code> if none
	 *         selected.
	 * @since 3.1
	 */
	public String getSelectedLinkText() {
		IHyperlinkSegment link = getSelectedLink();
		return link != null ? link.getText() : null;
	}

	private IHyperlinkSegment getSelectedLink() {
		IFocusSelectable segment = model.getSelectedSegment();
		if (segment != null && segment instanceof IHyperlinkSegment)
			return (IHyperlinkSegment) segment;
		return null;
	}

	private void initAccessible() {
		Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				if (e.childID == ACC.CHILDID_SELF)
					e.result = model.getAccessibleText();
				else {
					int linkCount = model.getHyperlinkCount();
					if (e.childID >= 0 && e.childID < linkCount) {
						IHyperlinkSegment link = model.getHyperlink(e.childID);
						e.result = link.getText();
					}
				}
			}

			@Override
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
				int linkCount = model.getHyperlinkCount();
				if (e.result == null && e.childID >= 0 && e.childID < linkCount) {
					IHyperlinkSegment link = model.getHyperlink(e.childID);
					e.result = link.getText();
				}
			}
		});
		accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = toControl(new Point(e.x, e.y));
				IHyperlinkSegment link = model.findHyperlinkAt(pt.x, pt.y);
				if (link != null)
					e.childID = model.indexOf(link);
				else
					e.childID = ACC.CHILDID_SELF;
			}

			@Override
			public void getLocation(AccessibleControlEvent e) {
				Rectangle location = null;
				if (e.childID != ACC.CHILDID_SELF
						&& e.childID != ACC.CHILDID_NONE) {
					int index = e.childID;
					IHyperlinkSegment link = model.getHyperlink(index);
					if (link != null) {
						location = link.getBounds();
					}
				}
				if (location == null) {
					location = getBounds();
				}
				Point pt = toDisplay(new Point(location.x, location.y));
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			@Override
			public void getFocus(AccessibleControlEvent e) {
				int childID = ACC.CHILDID_NONE;

				if (model.hasFocusSegments()) {
					int selectedIndex = model.getSelectedSegmentIndex();
					if (selectedIndex != -1) {
						childID = selectedIndex;
					}
				}
				e.childID = childID;
			}

			@Override
			public void getDefaultAction (AccessibleControlEvent e) {
				if (model.getHyperlinkCount() > 0) {
					e.result = SWT.getMessage ("SWT_Press"); //$NON-NLS-1$
				}
			}

			@Override
			public void getChildCount(AccessibleControlEvent e) {
				e.detail = model.getHyperlinkCount();
			}

			@Override
			public void getRole(AccessibleControlEvent e) {
				int role = 0;
				int childID = e.childID;
				int linkCount = model.getHyperlinkCount();
				if (childID == ACC.CHILDID_SELF) {
					if (linkCount > 0) {
						role = ACC.ROLE_LINK;
					} else {
						role = ACC.ROLE_TEXT;
					}
				} else if (childID >= 0 && childID < linkCount) {
					role = ACC.ROLE_LINK;
				}
				e.detail = role;
			}

			@Override
			public void getSelection(AccessibleControlEvent e) {
				int selectedIndex = model.getSelectedSegmentIndex();
				e.childID = (selectedIndex == -1) ? ACC.CHILDID_NONE
						: selectedIndex;
			}

			@Override
			public void getState(AccessibleControlEvent e) {
				int linkCount = model.getHyperlinkCount();
				int selectedIndex = model.getSelectedSegmentIndex();
				int state = 0;
				int childID = e.childID;
				if (childID == ACC.CHILDID_SELF) {
					state = ACC.STATE_NORMAL;
				} else if (childID >= 0 && childID < linkCount) {
					state = ACC.STATE_SELECTABLE;
					if (isFocusControl()) {
						state |= ACC.STATE_FOCUSABLE;
					}
					if (selectedIndex == childID) {
						state |= ACC.STATE_SELECTED;
						if (isFocusControl()) {
							state |= ACC.STATE_FOCUSED;
						}
					}
				}
				state |= ACC.STATE_READONLY;
				e.detail = state;
			}

			@Override
			public void getChildren(AccessibleControlEvent e) {
				int linkCount = model.getHyperlinkCount();
				Object[] children = new Object[linkCount];
				for (int i = 0; i < linkCount; i++) {
					children[i] = Integer.valueOf(i);
				}
				e.children = children;
			}

			@Override
			public void getValue(AccessibleControlEvent e) {
				// e.result = model.getAccessibleText();
			}
		});
	}

	private void startSelection(MouseEvent e) {
		inSelection = true;
		selData = new SelectionData(e);
		redraw();
		Form form = FormUtil.getForm(this);
		if (form != null)
			form.setSelectionText(this);
	}

	private void endSelection(MouseEvent e) {
		inSelection = false;
		if (selData != null) {
			if (!selData.isEnclosed())
				selData = null;
			else
				computeSelection();
		}
		notifySelectionChanged();
	}

	private void computeSelection() {
		GC gc = new GC(this);
		Paragraph[] paragraphs = model.getParagraphs();
		IHyperlinkSegment selectedLink = getSelectedLink();
		if (getDisplay().getFocusControl() != this)
			selectedLink = null;
		for (Paragraph p : paragraphs) {
			p.computeSelection(gc, resourceTable, selectedLink, selData);
		}
		gc.dispose();
	}

	void clearSelection() {
		selData = null;
		if (!isDisposed()) {
			redraw();
			notifySelectionChanged();
		}
	}

	private void notifySelectionChanged() {
		Event event = new Event();
		event.widget = this;
		event.display = this.getDisplay();
		event.type = SWT.Selection;
		notifyListeners(SWT.Selection, event);
		// A listener could have caused the widget to be disposed
		if (!isDisposed()) {
			getAccessible().selectionChanged();
		}
	}

	private void handleDrag(MouseEvent e) {
		if (selData != null) {
			ScrolledComposite scomp = FormUtil.getScrolledComposite(this);
			if (scomp != null) {
				FormUtil.ensureVisible(scomp, this, e);
			}
			selData.update(e);
			redraw();
		}
	}

	private void handleMouseClick(MouseEvent e, boolean down) {
		if (DEBUG_FOCUS)
			System.out.println("FormText: mouse click(" + down + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		if (down) {
			// select a hyperlink
			mouseFocus = true;
			IHyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
			if (segmentUnder != null) {
				IHyperlinkSegment oldLink = getSelectedLink();
				if (getDisplay().getFocusControl() != this) {
					setFocus();
				}
				model.selectLink(segmentUnder);
				enterLink(segmentUnder, e.stateMask);
				paintFocusTransfer(oldLink, segmentUnder);
			}
			if (e.button == 1) {
				startSelection(e);
				armed = segmentUnder;
			}
			else {
			}
		} else {
			if (e.button == 1) {
				endSelection(e);
				if (isDisposed()) return;
				IHyperlinkSegment segmentUnder = model
						.findHyperlinkAt(e.x, e.y);
				if (segmentUnder != null && armed == segmentUnder && selData == null) {
					activateLink(segmentUnder, e.stateMask);
					armed = null;
				}
			}
			mouseFocus = false;
		}
	}

	private void handleMouseHover(MouseEvent e) {
	}

	private void updateTooltipText(ParagraphSegment segment) {
		String tooltipText = null;
		if (segment != null) {
			tooltipText = segment.getTooltipText();
		}
		String currentTooltipText = getToolTipText();

		if ((currentTooltipText != null && tooltipText == null)
				|| (currentTooltipText == null && tooltipText != null))
			setToolTipText(tooltipText);
	}

	private void handleMouseMove(MouseEvent e) {
		if (inSelection) {
			handleDrag(e);
			return;
		}
		ParagraphSegment segmentUnder = model.findSegmentAt(e.x, e.y);
		updateTooltipText(segmentUnder);
		if (segmentUnder == null) {
			if (entered != null) {
				exitLink(entered, e.stateMask);
				paintLinkHover(entered, false);
				entered = null;
			}
			setCursor(null);
		} else if (segmentUnder instanceof IHyperlinkSegment) {
			IHyperlinkSegment linkUnder = (IHyperlinkSegment) segmentUnder;
			if (entered!=null && linkUnder!=entered) {
				// Special case: links are so close that there are 0 pixels between.
				// Must exit the link before entering the next one.
				exitLink(entered, e.stateMask);
				paintLinkHover(entered, false);
				entered = null;
			}
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
			if (segmentUnder instanceof TextSegment)
				setCursor(model.getHyperlinkSettings().getTextCursor());
			else
				setCursor(null);
		}
	}

	private boolean advance(boolean next) {
		if (DEBUG_FOCUS)
			System.out.println("Advance: next=" + next); //$NON-NLS-1$
		IFocusSelectable current = model.getSelectedSegment();
		if (current != null && current instanceof IHyperlinkSegment)
			exitLink((IHyperlinkSegment) current, SWT.NULL);
		IFocusSelectable newSegment = null;
		boolean valid = false;
		// get the next segment that can accept focus. Links
		// can always accept focus but controls may not
		while (!valid) {
			if (!model.traverseFocusSelectableObjects(next))
				break;
			newSegment = model.getSelectedSegment();
			if (newSegment == null)
				break;
			valid = setControlFocus(next, newSegment);
		}
		IHyperlinkSegment newLink = newSegment instanceof IHyperlinkSegment ? (IHyperlinkSegment) newSegment
				: null;
		if (valid)
			enterLink(newLink, SWT.NULL);
		IHyperlinkSegment oldLink = current instanceof IHyperlinkSegment ? (IHyperlinkSegment) current
				: null;
		if (oldLink != null || newLink != null)
			paintFocusTransfer(oldLink, newLink);
		if (newLink != null)
			ensureVisible(newLink);
		if (newLink != null)
			getAccessible().setFocus(model.getSelectedSegmentIndex());
		return !valid;
	}

	private boolean setControlFocus(boolean next, IFocusSelectable selectable) {
		controlFocusTransfer = true;
		boolean result = selectable.setFocus(resourceTable, next);
		controlFocusTransfer = false;
		return result;
	}

	private void handleFocusChange() {
		if (DEBUG_FOCUS) {
			System.out.println("Handle focus change: hasFocus=" + hasFocus //$NON-NLS-1$
					+ ", mouseFocus=" + mouseFocus); //$NON-NLS-1$
		}
		if (hasFocus) {
			boolean advance = true;
			if (!mouseFocus) {
				// if (model.restoreSavedLink() == false)
				boolean valid = false;
				IFocusSelectable selectable = null;
				while (!valid) {
					if (!model.traverseFocusSelectableObjects(advance))
						break;
					selectable = model.getSelectedSegment();
					if (selectable == null)
						break;
					valid = setControlFocus(advance, selectable);
				}
				if (selectable != null)
					ensureVisible(selectable);
				if (selectable instanceof IHyperlinkSegment) {
					enterLink((IHyperlinkSegment) selectable, SWT.NULL);
					paintFocusTransfer(null, (IHyperlinkSegment) selectable);
				}
			}
		} else {
			paintFocusTransfer(getSelectedLink(), null);
			model.selectLink(null);
		}
		if (!model.hasFocusSegments())
			redraw();
	}

	private void enterLink(IHyperlinkSegment link, int stateMask) {
		if (link == null || listeners == null)
			return;
		HyperlinkEvent he = new HyperlinkEvent(this, link.getHref(), link
				.getText(), stateMask);
		for (IHyperlinkListener listener : listeners) {
			listener.linkEntered(he);
		}
	}

	private void exitLink(IHyperlinkSegment link, int stateMask) {
		if (link == null || listeners == null)
			return;
		HyperlinkEvent he = new HyperlinkEvent(this, link.getHref(), link
				.getText(), stateMask);
		for (IHyperlinkListener listener : listeners) {
			listener.linkExited(he);
		}
	}

	private void paintLinkHover(IHyperlinkSegment link, boolean hover) {
		GC gc = new GC(this);
		HyperlinkSettings settings = getHyperlinkSettings();
		Color newFg = hover ? settings.getActiveForeground() : settings
				.getForeground();
		if (newFg != null)
			gc.setForeground(newFg);
		gc.setBackground(getBackground());
		gc.setFont(getFont());
		boolean selected = (link == getSelectedLink());
		((ParagraphSegment) link).paint(gc, hover, resourceTable, selected,
				selData, null);
		gc.dispose();
	}

	private void activateSelectedLink() {
		IHyperlinkSegment link = getSelectedLink();
		if (link != null)
			activateLink(link, SWT.NULL);
	}

	private void activateLink(IHyperlinkSegment link, int stateMask) {
		setCursor(model.getHyperlinkSettings().getBusyCursor());
		if (listeners != null) {
			int size = listeners.size();
			HyperlinkEvent e = new HyperlinkEvent(this, link.getHref(), link
					.getText(), stateMask);
			Object [] listenerList = listeners.getListeners();
			for (int i = 0; i < size; i++) {
				IHyperlinkListener listener = (IHyperlinkListener) listenerList[i];
				listener.linkActivated(e);
			}
		}
		if (!isDisposed() && model.linkExists(link)) {
			setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
		}
	}

	private void ensureBoldFontPresent(Font regularFont) {
		Font boldFont = (Font) resourceTable.get(FormTextModel.BOLD_FONT_ID);
		if (boldFont != null)
			return;
		boldFont = FormFonts.getInstance().getBoldFont(getDisplay(), regularFont);
		resourceTable.put(FormTextModel.BOLD_FONT_ID, boldFont);
	}

	private void paint(PaintEvent e) {
		GC gc = e.gc;
		gc.setFont(getFont());
		ensureBoldFontPresent(getFont());
		gc.setForeground(getForeground());
		gc.setBackground(getBackground());
		Point size = getSize();
		Rectangle paintBounds;
		if (size.x == 0 && size.y == 0) {
			// avoids crash on image creation with (0,0) image size
			paintBounds = new Rectangle(e.x, e.y, e.width, e.height);
		} else {
			paintBounds = new Rectangle(0, 0, size.x, size.y);
		}
		repaint(gc, paintBounds.x, paintBounds.y, paintBounds.width, paintBounds.height);
	}

	private void repaint(GC gc, int x, int y, int width, int height) {
		Color bg = getEnabled() ? getBackground() : getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		Color fg = getEnabled() ? getForeground() : getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);

		Paragraph[] paragraphs = model.getParagraphs();
		IHyperlinkSegment selectedLink = getDisplay().getFocusControl() != this ? null : getSelectedLink();

		final ImageGcDrawer textDrawer = (textGC, iWidth, iHeight) -> {
			textGC.setForeground(fg);
			textGC.setBackground(bg);
			textGC.setFont(getFont());
			textGC.fillRectangle(0, 0, iWidth, iHeight);
			Rectangle repaintRegion = new Rectangle(x, y, iWidth, iHeight);
			for (Paragraph p : paragraphs) {
				p.paint(textGC, repaintRegion, resourceTable, selectedLink, selData);
			}
			if (hasFocus && !model.hasFocusSegments()) {
				textGC.drawFocus(x, y, iWidth, iHeight);
			}
		};
		Image textBuffer = new Image(getDisplay(), textDrawer, width, height);

		gc.drawImage(textBuffer, x, y);
		textBuffer.dispose();
	}

	private int getParagraphSpacing(int lineHeight) {
		return lineHeight / 2;
	}

	private void paintFocusTransfer(IHyperlinkSegment oldLink,
			IHyperlinkSegment newLink) {
		if (oldLink != null) {
			Rectangle r = oldLink.getBounds();
			redraw(r.x, r.y, r.width, r.height, true);
			update();
		}
		if (newLink != null) {
			GC gc = new GC(this);
			Color bg = getBackground();
			Color fg = getForeground();
			gc.setFont(getFont());
			gc.setBackground(bg);
			gc.setForeground(fg);
			newLink.paintFocus(gc, bg, fg, true, null);
			gc.dispose();
		}
	}

	private void ensureVisible(IFocusSelectable segment) {
		if (mouseFocus) {
			mouseFocus = false;
			return;
		}
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

	/**
	 * Overrides the method by fully trusting the layout manager (computed width
	 * or height may be larger than the provider width or height hints). Callers
	 * should be prepared that the computed width is larger than the provided
	 * wHint.
	 *
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
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
		if (DEBUG_TEXTSIZE)
			System.out.println("FormText Computed size: "+trim); //$NON-NLS-1$
		return new Point(trim.width, trim.height);
	}

	private void disposeResourceTable(boolean disposeBoldFont) {
		if (disposeBoldFont) {
			Font boldFont = (Font) resourceTable
					.get(FormTextModel.BOLD_FONT_ID);
			if (boldFont != null) {
				FormFonts.getInstance().markFinished(boldFont, getDisplay());
				resourceTable.remove(FormTextModel.BOLD_FONT_ID);
			}
		}
		ArrayList<String> imagesToRemove = new ArrayList<>();
		for (Enumeration<String> enm = resourceTable.keys(); enm.hasMoreElements();) {
			String key = enm.nextElement();
			if (key.startsWith(ImageSegment.SEL_IMAGE_PREFIX)) {
				Object obj = resourceTable.get(key);
				if (obj instanceof Image) {
					Image image = (Image) obj;
					if (!image.isDisposed()) {
						image.dispose();
						imagesToRemove.add(key);
					}
				}
			}
		}
		for (String element : imagesToRemove) {
			resourceTable.remove(element);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		redraw();
	}

	@Override
	public boolean setFocus() {
		mouseFocus = true;
		FormUtil.setFocusScrollingEnabled(this, false);
		boolean result = super.setFocus();
		mouseFocus = false;
		FormUtil.setFocusScrollingEnabled(this, true);
		return result;
	}
}
