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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.internal.forms.Policy;
import org.eclipse.ui.internal.forms.widgets.*;

/**
 * This class is a read-only text control that is capable of rendering wrapped
 * text. Text can be rendered as-is or by parsing the formatting XML tags.
 * Independently, words that start with http:// can be converted into hyperlinks
 * on the fly.
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
 * is a key to the Image set using 'setImage' method.</li>
 * <li><b>a </b>- to render a hyperlink. Element accepts attribute 'href' that
 * will be provided to the hyperlink listeners via HyperlinkEvent object. The
 * element also accepts 'nowrap' attribute (default is false). When set to
 * 'true', the hyperlink will not be wrapped.</li>
 * <li><b>b </b>- the enclosed text will use bold font.</li>
 * <li><b>br </b>- forced line break (no attributes).</li>
 * <li><b>span </b>- the enclosed text will have the color and font specified
 * in the element attributes. Color is provided using 'color' attribute and is a
 * key to the Color object set by 'setColor' method. Font is provided using
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
 * <p>
 * Although the class is not marked final,
 * 
 * @see FormToolkit
 * @see TableWrapLayout
 * @since 3.0
 */
public final class FormText extends Canvas {
	/**
	 * The object ID to be used when registering action to handle URL hyperlinks
	 * (those that should result in opening the web browser). Value is
	 * "urlHandler".
	 */
	public static final String URL_HANDLER_ID = "urlHandler";

	/**
	 * Value of the horizontal margin (default is 0).
	 */
	public int marginWidth = 0;

	/**
	 * Value of tue vertical margin (default is 1).
	 */
	public int marginHeight = 1;

	private static final boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption(FormUtil.DEBUG_TEXT)); //$NON-NLS-1$//$NON-NLS-2$	

	// private fields
	private boolean hasFocus;

	private boolean paragraphsSeparated = true;

	private FormTextModel model;

	private Vector listeners;

	private Hashtable resourceTable = new Hashtable();

	private IHyperlinkSegment entered;

	private boolean mouseFocus = false;

	private boolean mouseDown = false;

	private SelectionData selData;

	private Action openAction;

	private Action copyShortcutAction;

	private static final String INTERNAL_MENU = "__internal_menu__";

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
			long start = 0;

			if (DEBUG)
				start = System.currentTimeMillis();
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
			if (DEBUG) {
				long stop = System.currentTimeMillis();
				System.out.println("FormText computeSize: " + (stop - start)
						+ "ms");
			}
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
			boolean linksInTheLastRow = false;
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
					linksInTheLastRow = false;
					for (int j = 0; j < segments.length; j++) {
						ParagraphSegment segment = segments[j];
						segment.advanceLocator(gc, wHint, loc, resourceTable,
								false);
						width = Math.max(width, loc.width);
						if (segment instanceof IHyperlinkSegment)
							linksInTheLastRow = true;
					}
					loc.y += loc.rowHeight;
				} else {
					// empty new line
					loc.y += lineHeight;
				}
			}
			gc.dispose();
			if (linksInTheLastRow)
				loc.y += 1;
			return new Point(width, loc.y);
		}

		protected void layout(Composite composite, boolean flushCache) {
			long start = 0;

			if (DEBUG) {
				start = System.currentTimeMillis();
			}
			selData = null;
			Rectangle carea = composite.getClientArea();
			GC gc = new GC(composite);
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

			Paragraph[] paragraphs = model.getParagraphs();
			IHyperlinkSegment selectedLink = model.getSelectedLink();
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
			if (DEBUG) {
				long stop = System.currentTimeMillis();
				System.out.println("FormText.layout: " + (stop - start) + "ms");
			}
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
				disposeResourceTable(true);
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
		createMenu();
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
	public void setLoadingText(String loadingText) {
		setText(loadingText, false, false);
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
	 * For <samp>span </samp> tags, an object of a type <samp>Color </samp> must
	 * be registered using the key equivalent to the value of the <samp>color
	 * </samp> attribute.
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
	 * Sets the font to use to render the default text (text that does not have
	 * special font property assigned). Bold font will be constructed from this
	 * font.
	 * 
	 * @param font
	 *            the default font to use
	 */
	public void setFont(Font font) {
		super.setFont(font);
		Font boldFont = (Font) resourceTable.get(FormTextModel.BOLD_FONT_ID);
		if (boldFont != null) {
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
		layout();
		redraw();
	}

	/**
	 * Controls whether whitespace inside paragraph and list items is
	 * normalized.
	 * <p>
	 * If normalized:
	 * <ul>
	 * <li>all white space characters will be condensed into at most one when
	 * between words.</li>
	 * <li>new line characters will be ignored and replaced with one white
	 * space character</li>
	 * <li>white space characters after the opening tags and before the closing
	 * tags will be trimmed</li>
	 * 
	 * @param value
	 *            <code>true</code> if whitespace is normalized,
	 *            <code>false</code> otherwise.
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

	public void setMenu(Menu menu) {
		Menu currentMenu = super.getMenu();
		if (currentMenu != null && INTERNAL_MENU.equals(currentMenu.getData())) {
			currentMenu.dispose();
		}
		super.setMenu(menu);
	}

	private void createMenu() {
		Menu menu = new Menu(this);
		final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
		copyItem.setText(Policy.getMessage("FormText.copy"));

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == copyItem) {
					copy();
				}
			}
		};
		copyItem.addSelectionListener(listener);
		menu.addMenuListener(new MenuListener() {
			public void menuShown(MenuEvent e) {
				copyItem.setEnabled(canCopy());
			}

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
	 * Adds a selection listener. A Selection event is sent by the widget when
	 * the selection has changed.
	 * <p>
	 * When <code>widgetSelected</code> is called, the event x amd y fields
	 * contain the start and end caret indices of the selection.
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
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
	}

	/**
	 * Removes the specified selection listener.
	 * <p>
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
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		removeListener(SWT.Selection, listener);
	}

	/**
	 * Returns the selected text.
	 * <p>
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
		else
			return "";
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
	 * Copies the selected text into the clipboard.
	 * 
	 * @since. 3.1
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
		IHyperlinkSegment link = model.getSelectedLink();
		if (link != null)
			return link.getHref();
		return null;
	}

	/**
	 * Context menu is about to show - override to add actions to the menu
	 * manager. Subclasses are required to call 'super' when overriding.
	 * 
	 * @param manager
	 *            the pop-up menu manager
	 */
	protected void contextMenuAboutToShow(IMenuManager manager) {
		IHyperlinkSegment link = model.getSelectedLink();
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
				e.childID = (getBounds().contains(pt)) ? ACC.CHILDID_SELF
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

	private void startSelection(MouseEvent e) {
		mouseDown = true;
		selData = new SelectionData(e);
		redraw();
		Form form = FormUtil.getForm(this);
		if (form != null)
			form.setSelectionText(this);
	}

	private void endSelection(MouseEvent e) {
		mouseDown = false;
		if (selData != null && !selData.isEnclosed())
			selData = null;
		notifySelectionChanged();
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
		if (down) {
			// select a hyperlink
			mouseFocus = true;
			IHyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
			if (segmentUnder != null) {
				IHyperlinkSegment oldLink = model.getSelectedLink();
				model.selectLink(segmentUnder);
				enterLink(segmentUnder, e.stateMask);
				paintFocusTransfer(oldLink, segmentUnder);
			}
			if (e.button == 1)
				startSelection(e);
			else {
			}
		} else {
			if (e.button == 1) {
				endSelection(e);
				IHyperlinkSegment segmentUnder = model
						.findHyperlinkAt(e.x, e.y);
				if (segmentUnder != null && selData == null) {
					activateLink(segmentUnder, e.stateMask);
				}
			}
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
		if (mouseDown) {
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
		} else {
			if (segmentUnder instanceof IHyperlinkSegment) {
				IHyperlinkSegment linkUnder = (IHyperlinkSegment) segmentUnder;
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
	}

	private boolean advance(boolean next) {
		IHyperlinkSegment current = model.getSelectedLink();
		if (current != null)
			exitLink(current, SWT.NULL);
		boolean valid = model.traverseLinks(next);
		IHyperlinkSegment newLink = model.getSelectedLink();
		if (valid)
			enterLink(newLink, SWT.NULL);
		paintFocusTransfer(current, newLink);
		if (newLink != null)
			ensureVisible(newLink);
		return !valid;
	}

	private void handleFocusChange() {
		if (hasFocus) {
			if (model.getSelectedLink() == null)
				model.traverseLinks(true);
			enterLink(model.getSelectedLink(), SWT.NULL);
			paintFocusTransfer(null, model.getSelectedLink());
			// ensureVisible(model.getSelectedLink());
		} else {
			paintFocusTransfer(model.getSelectedLink(), null);
			model.selectLink(null);
		}
	}

	private void enterLink(IHyperlinkSegment link, int stateMask) {
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

	private void exitLink(IHyperlinkSegment link, int stateMask) {
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

	private void paintLinkHover(IHyperlinkSegment link, boolean hover) {
		GC gc = new GC(this);
		HyperlinkSettings settings = getHyperlinkSettings();
		gc.setForeground(hover ? settings.getActiveForeground() : settings
				.getForeground());
		gc.setBackground(getBackground());
		gc.setFont(getFont());
		boolean selected = (link == model.getSelectedLink());
		((ParagraphSegment) link).paint(gc, hover, resourceTable, selected,
				selData, null);
		if (selected) {
			link.paintFocus(gc, getBackground(), getForeground(), false, null);
			link.paintFocus(gc, getBackground(), getForeground(), true, null);
		}
		gc.dispose();
	}

	private void activateSelectedLink() {
		IHyperlinkSegment link = model.getSelectedLink();
		if (link != null)
			activateLink(link, SWT.NULL);
	}

	private void activateLink(IHyperlinkSegment link, int stateMask) {
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
		if (!isDisposed() && model.linkExists(link)) {
			setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
			// IHyperlinkSegment selectedLink = model.getSelectedLink();
			// if (selectedLink!=link) {
			// if (selectedLink != null)
			// exitLink(selectedLink, SWT.NULL);
			// model.selectLink(link);
			// enterLink(link, SWT.NULL);
			// paintFocusTransfer(selectedLink, link);

			// }
		}
	}

	private void ensureBoldFontPresent(Font regularFont) {
		Font boldFont = (Font) resourceTable.get(FormTextModel.BOLD_FONT_ID);
		if (boldFont != null)
			return;
		boldFont = FormUtil.createBoldFont(getDisplay(), regularFont);
		resourceTable.put(FormTextModel.BOLD_FONT_ID, boldFont);
	}

	private void paint(PaintEvent e) {
		GC gc = e.gc;
		gc.setFont(getFont());
		ensureBoldFontPresent(getFont());
		gc.setForeground(getForeground());
		gc.setBackground(getBackground());
		repaint(gc, e.x, e.y, e.width, e.height);
	}

	private void repaint(GC gc, int x, int y, int width, int height) {
		Image textBuffer = new Image(getDisplay(), width, height);
		textBuffer.setBackground(getBackground());
		GC textGC = new GC(textBuffer, gc.getStyle());
		textGC.setForeground(getForeground());
		textGC.setBackground(getBackground());
		textGC.setFont(getFont());
		textGC.fillRectangle(0, 0, width, height);
		Rectangle repaintRegion = new Rectangle(x, y, width, height);

		Paragraph[] paragraphs = model.getParagraphs();
		IHyperlinkSegment selectedLink = model.getSelectedLink();
		if (getDisplay().getFocusControl() != this)
			selectedLink = null;
		for (int i = 0; i < paragraphs.length; i++) {
			Paragraph p = paragraphs[i];
			p
					.paint(textGC, repaintRegion, resourceTable, selectedLink,
							selData);
		}
		gc.drawImage(textBuffer, x, y);
		textGC.dispose();
		textBuffer.dispose();
	}

	private int getParagraphSpacing(int lineHeight) {
		return lineHeight / 2;
	}

	private void paintFocusTransfer(IHyperlinkSegment oldLink,
			IHyperlinkSegment newLink) {
		GC gc = new GC(this);
		Color bg = getBackground();
		Color fg = getForeground();
		gc.setFont(getFont());
		if (oldLink != null) {
			gc.setBackground(bg);
			gc.setForeground(fg);
			oldLink.paintFocus(gc, bg, fg, false, null);
		}
		if (newLink != null) {
			// ensureVisible(newLink);
			gc.setBackground(bg);
			gc.setForeground(fg);
			newLink.paintFocus(gc, bg, fg, true, null);
		}
		gc.dispose();
	}

	private void contributeLinkActions(IMenuManager manager,
			IHyperlinkSegment link) {
		manager.add(openAction);
		manager.add(copyShortcutAction);
		manager.add(new Separator());
	}

	/*
	 * private void copyShortcut(HyperlinkSegment link) { String text =
	 * link.getText(); Clipboard clipboard = new Clipboard(getDisplay());
	 * clipboard.setContents(new Object[]{text}, new Transfer[]{TextTransfer
	 * .getInstance()}); }
	 */
	private void ensureVisible(IHyperlinkSegment segment) {
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
	 * or height may be larger than the provider width or height hints).
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

	private void disposeResourceTable(boolean disposeBoldFont) {
		if (disposeBoldFont) {
			Font boldFont = (Font) resourceTable
					.get(FormTextModel.BOLD_FONT_ID);
			if (boldFont != null) {
				boldFont.dispose();
				resourceTable.remove(FormTextModel.BOLD_FONT_ID);
			}
		}
		ArrayList imagesToRemove = new ArrayList();
		for (Enumeration enm = resourceTable.keys(); enm.hasMoreElements();) {
			String key = (String) enm.nextElement();
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
		for (int i = 0; i < imagesToRemove.size(); i++) {
			resourceTable.remove(imagesToRemove.get(i));
		}
	}
}
