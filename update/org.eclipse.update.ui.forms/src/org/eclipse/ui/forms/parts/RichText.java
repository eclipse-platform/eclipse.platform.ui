/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.parts;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.internal.parts.HyperlinkSegment;
import org.eclipse.ui.forms.internal.parts.Locator;
import org.eclipse.ui.forms.internal.parts.Paragraph;
import org.eclipse.ui.forms.internal.parts.ParagraphSegment;
import org.eclipse.ui.forms.internal.parts.RichTextModel;
import org.eclipse.ui.forms.internal.parts.TextSegment;
import org.eclipse.update.ui.forms.internal.AbstractSectionForm;
import org.eclipse.update.ui.forms.internal.FormsPlugin;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.update.ui.forms.internal.ILayoutExtension;

public class RichText extends Canvas {
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
	private RichTextModel model;
	private Vector listeners;
	private Hashtable imageTable = new Hashtable();

	private HyperlinkSegment entered;
	private boolean mouseDown = false;
	private Point dragOrigin;
	private Action openAction;
	private Action copyShortcutAction;
	private boolean loading = true;
	private String loadingText = "Loading...";

	private class RichTextLayout extends Layout implements ILayoutExtension {
		public RichTextLayout() {
		}

		public int getMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}

		public int getMinimumWidth(Composite parent, boolean changed) {
			return 30;
		}

		/*
		 * @see Layout#computeSize(Composite, int, int, boolean)
		 */

		public Point computeSize(
			Composite composite,
			int wHint,
			int hHint,
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
			GC gc = new GC(RichText.this);
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

			GC gc = new GC(RichText.this);
			gc.setFont(getFont());

			Locator loc = new Locator();

			int width = wHint != SWT.DEFAULT ? wHint : 0;

			FontMetrics fm = gc.getFontMetrics();
			int lineHeight = fm.getHeight();

			for (int i = 0; i < paragraphs.length; i++) {
				Paragraph p = paragraphs[i];

				if (i > 0
					&& getParagraphsSeparated()
					&& p.getAddVerticalSpace())
					loc.y += getParagraphSpacing(lineHeight);

				loc.rowHeight = 0;
				loc.indent = p.getIndent();
				loc.x = p.getIndent();

				ParagraphSegment[] segments = p.getSegments();
				if (segments.length > 0) {
					for (int j = 0; j < segments.length; j++) {
						ParagraphSegment segment = segments[j];
						segment.advanceLocator(gc, wHint, loc, imageTable);
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
	 * Contructs a new rich text widget in the provided parent and using the
	 * styles.
	 * 
	 * @param parent
	 *            rich text parent control
	 * @param style
	 *            the widget style
	 */
	public RichText(Composite parent, int style) {
		super(parent, style);
		setLayout(new RichTextLayout());
		model = new RichTextModel();

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				model.dispose();
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
					exitLink(entered);
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
	}
	
	/**
	 * Test for focus.
	 * 
	 * @return <samp>true</samp> if the widget has focus.
	 */
	public boolean getFocus() {
		return hasFocus;
	}
	/**
	 * Test if the widget is currently processing the text it is about to
	 * render.
	 * 
	 * @return <samp>true</samp> if the widget is still loading the text,
	 *         <samp>false</samp> otherwise.
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
	 * the input stream that created from a remote URL.
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
	 *            <samp>true</samp> if paragraphs are separated,</samp>
	 *            false</samp> otherwise.
	 */

	public void setParagraphsSeparated(boolean value) {
		paragraphsSeparated = value;
	}

	/**
	 * Tests if there is some inter-paragraph spacing.
	 * 
	 * @return <samp>true</samp> if paragraphs are separated, <samp>false
	 *         </samp> otherwise.
	 */

	public boolean getParagraphsSeparated() {
		return paragraphsSeparated;
	}
	
	
	/**
	 * Registers the image referenced by the provided key. 
	 * <p>
	 * For <samp>img</samp> tags, an object of a type <samp>Image</samp>
	 * must be registered using the key equivalent to the value of the <samp>
	 * href</samp> attribute.
	 * @param key
	 *            unique key that matches the value of the <samp>href</samp>
	 *            attribute.
	 * @param image
	 *            an object of a type <samp>Image</samp>.
	 */
	public void setImage(String key, Image image) {
		imageTable.put(key, image);
	}
	/**
	 * Renders the provided text. Text can be rendered as-is, or by parsing the
	 * formatting tags. Optionally, untagged text can be converted to
	 * hyperlinks.
	 * 
	 * @param text
	 *            the text to render
	 * @param parseTags
	 *            if <samp>true</samp>, formatting tags will be parsed.
	 *            Otherwise, text will be rendered as-is.
	 * @param expandURLs
	 *            if <samp>true</samp>, URLs found in the untagged text will
	 *            be converted into hyperlinks.
	 */
	public void setText(String text, boolean parseTags, boolean expandURLs) {
		try {
			if (parseTags)
				model.parseTaggedText(text, expandURLs);
			else
				model.parseRegularText(text, expandURLs);
		} catch (CoreException e) {
			FormsPlugin.logException(e);
		} finally {
			loading = false;
		}
	}
	/**
	 * Renders the contents of the stream. Optionally, URLs in untagged text
	 * can be converted into hyperlinks.
	 * 
	 * @param is
	 *            stream to render
	 * @param expandURLs
	 *            if <samp>true</samp>, URLs found in untagged text will be
	 *            converted into hyperlinks.
	 */
	public void setContents(InputStream is, boolean expandURLs) {
		try {
			model.parseInputStream(is, expandURLs);
		} catch (CoreException e) {
			FormsPlugin.logException(e);
		} finally {
			loading = false;
		}
	}
	/**
	 * Sets the focus to the first hyperlink, or the widget itself if there are
	 * no hyperlinks.
	 * 
	 * @return <samp>true</samp> if the control got focus, <samp>false
	 *         </samp> otherwise.
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
	 * @param listener
	 */
	public void addHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null)
			listeners = new Vector();
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	/**
	 * Removes the hyperlink listener.
	 * @param listener
	 */
	public void removeHyperlinkListener(HyperlinkListener listener) {
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
		openAction = new Action() {
			public void run() {
				activateSelectedLink();
			}
		};
		openAction.setText(
			FormsPlugin.getResourceString("FormEgine.linkPopup.open"));
		copyShortcutAction = new Action() {
			public void run() {
				copyShortcut(model.getSelectedLink());
			}
		};
		copyShortcutAction.setText(
			FormsPlugin.getResourceString("FormEgine.linkPopup.copyShortcut"));
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

		accessible
			.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = toControl(new Point(e.x, e.y));
				e.childID =
					(getBounds().contains(pt))
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
				enterLink(segmentUnder);
				paintFocusTransfer(oldLink, segmentUnder);
			}
			mouseDown = true;
			dragOrigin = new Point(e.x, e.y);
		} else {
			if (e.button == 1) {
				HyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
				if (segmentUnder != null) {
					activateLink(segmentUnder);
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
				exitLink(entered);
				paintLinkHover(entered, false);
				entered = null;
			}
			setCursor(null);
		} else {
			if (segmentUnder instanceof HyperlinkSegment) {
				HyperlinkSegment linkUnder = (HyperlinkSegment) segmentUnder;
				if (entered == null) {
					entered = linkUnder;
					enterLink(linkUnder);
					paintLinkHover(entered, true);
					setCursor(
						model.getHyperlinkSettings().getHyperlinkCursor());
				}
			} else {
				if (entered != null) {
					exitLink(entered);
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
			exitLink(current);

		boolean valid = model.traverseLinks(next);

		HyperlinkSegment newLink = model.getSelectedLink();

		if (valid)
			enterLink(newLink);
		paintFocusTransfer(current, newLink);
		if (newLink != null)
			ensureVisible(newLink);
		return !valid;
	}

	private void handleFocusChange() {
		if (hasFocus) {
			model.traverseLinks(true);
			enterLink(model.getSelectedLink());
			paintFocusTransfer(null, model.getSelectedLink());
		} else {
			paintFocusTransfer(model.getSelectedLink(), null);
			model.selectLink(null);
		}
	}

	private void enterLink(HyperlinkSegment link) {
		if (link == null || listeners==null)
			return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, link.getHref(), link.getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkEntered(e);
		}
	}

	private void exitLink(HyperlinkSegment link) {
		if (link == null || listeners==null)
			return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, link.getHref(), link.getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkExited(e);
		}
	}

	private void paintLinkHover(HyperlinkSegment link, boolean hover) {
		GC gc = new GC(this);

		HyperlinkSettings settings = getHyperlinkSettings();

		gc.setForeground(
			hover ? settings.getActiveForeground() : settings.getForeground());
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
			activateLink(link);
	}

	private void activateLink(HyperlinkSegment link) {
		setCursor(model.getHyperlinkSettings().getBusyCursor());
		if (listeners!=null) {
			int size = listeners.size();
			HyperlinkEvent e = new HyperlinkEvent(this, link.getHref(), link.getText());
			for (int i = 0; i < size; i++) {
				HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
				listener.linkActivated(e);
			}
		}
		if (!isDisposed())
			setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
	}

	private void paint(PaintEvent e) {
		int width = getClientArea().width;

		GC gc = e.gc;
		gc.setFont(getFont());
		gc.setForeground(getForeground());
		gc.setBackground(getBackground());

		Locator loc = new Locator();
		loc.marginWidth = marginWidth;
		loc.marginHeight = marginHeight;
		loc.x = marginWidth;
		loc.y = marginHeight;

		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();

		if (loading) {
			int textWidth = gc.textExtent(loadingText).x;
			gc.drawText(
				loadingText,
				width / 2 - textWidth / 2,
				getClientArea().height / 2 - lineHeight / 2);
			return;
		}

		Paragraph[] paragraphs = model.getParagraphs();

		HyperlinkSegment selectedLink = model.getSelectedLink();

		for (int i = 0; i < paragraphs.length; i++) {
			Paragraph p = paragraphs[i];

			if (i > 0 && paragraphsSeparated && p.getAddVerticalSpace())
				loc.y += getParagraphSpacing(lineHeight);

			loc.indent = p.getIndent();
			loc.resetCaret();
			loc.rowHeight = 0;
			p.paint(gc, width, loc, lineHeight, imageTable, selectedLink);
		}
	}
	private int getParagraphSpacing(int lineHeight) {
		return lineHeight / 2;
	}
	
	private void paintFocusTransfer(
			HyperlinkSegment oldLink,
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

	private void contributeLinkActions(
		IMenuManager manager,
		HyperlinkSegment link) {
		manager.add(openAction);
		manager.add(copyShortcutAction);
		manager.add(new Separator());
	}

	private void copyShortcut(HyperlinkSegment link) {
		String text = link.getText();
		Clipboard clipboard = new Clipboard(getDisplay());
		clipboard.setContents(
			new Object[] { text },
			new Transfer[] { TextTransfer.getInstance()});
	}

	private void ensureVisible(HyperlinkSegment segment) {
		Rectangle bounds = segment.getBounds();
		ScrolledComposite scomp = getScrolledComposite();
		if (scomp == null)
			return;
		Point origin = AbstractSectionForm.getControlLocation(scomp, this);
		origin.x += bounds.x;
		origin.y += bounds.y;
		AbstractSectionForm.ensureVisible(
			scomp,
			origin,
			new Point(bounds.width, bounds.height));
	}
	private ScrolledComposite getScrolledComposite() {
		Composite parent = getParent();
		while (parent != null) {
			if (parent instanceof ScrolledComposite)
				return (ScrolledComposite) parent;
			parent = parent.getParent();
		}
		return null;
	}

	private void handleDrag(MouseEvent e) {
	}
}