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
package org.eclipse.update.ui.forms.richtext;

import java.io.InputStream;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.richtext.*;

public class RichText extends Canvas {
	public static final String URL_HANDLER_ID = "urlHandler";
	boolean hasFocus;
	boolean paragraphsSeparated = true;
	TextModel model;
	Hashtable objectTable = new Hashtable();
	public int marginWidth = 0;
	public int marginHeight = 1;
	HyperlinkSegment entered;
	boolean mouseDown = false;
	Point dragOrigin;
	private Action openAction;
	private Action copyShortcutAction;
	private boolean loading = true;
	private String loadingText = "Loading...";

	public class RichTextLayout extends Layout implements ILayoutExtension {
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
						segment.advanceLocator(gc, wHint, loc, objectTable);
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

	public boolean getFocus() {
		return hasFocus;
	}

	public boolean isLoading() {
		return loading;
	}

	public String getLoadingText() {
		return loadingText;
	}

	public void setLoadingText(String loadingText) {
		this.loadingText = loadingText;
	}

	public int getParagraphSpacing(int lineHeight) {
		return lineHeight / 2;
	}

	public void setParagraphsSeparated(boolean value) {
		paragraphsSeparated = value;
	}

	public boolean getParagraphsSeparated() {
		return paragraphsSeparated;
	}

	/**
	 * Constructor for SelectableFormLabel
	 */
	public RichText(Composite parent, int style) {
		super(parent, style);
		setLayout(new RichTextLayout());
		model = new TextModel();

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
				copyShortcut(getSelectedLink());
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

	private void handleDrag(MouseEvent e) {
	}

	public HyperlinkSettings getHyperlinkSettings() {
		return model.getHyperlinkSettings();
	}

	public void setHyperlinkSettings(HyperlinkSettings settings) {
		model.setHyperlinkSettings(settings);
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

	public HyperlinkSegment getSelectedLink() {
		return model.getSelectedLink();
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
		if (link == null)
			return;
		HyperlinkAction action = link.getAction(objectTable);
		if (action != null)
			action.linkEntered(link);
	}

	private void exitLink(HyperlinkSegment link) {
		if (link == null)
			return;
		HyperlinkAction action = link.getAction(objectTable);
		if (action != null)
			action.linkExited(link);
	}

	private void paintLinkHover(HyperlinkSegment link, boolean hover) {
		GC gc = new GC(this);

		HyperlinkSettings settings = getHyperlinkSettings();

		gc.setForeground(
			hover ? settings.getActiveForeground() : settings.getForeground());
		gc.setBackground(getBackground());
		gc.setFont(getFont());
		boolean selected = (link == getSelectedLink());
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
		HyperlinkAction action = link.getAction(objectTable);
		if (action != null)
			action.linkActivated(link);
		if (!isDisposed())
			setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
	}

	protected void paint(PaintEvent e) {
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
			p.paint(gc, width, loc, lineHeight, objectTable, selectedLink);
		}
	}

	public void registerTextObject(String key, Object value) {
		objectTable.put(key, value);
	}

	public void load(String text, boolean parseTags, boolean expandURLs) {
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
	public void load(InputStream is, boolean expandURLs) {
		try {
			model.parseInputStream(is, expandURLs);
		} catch (CoreException e) {
			FormsPlugin.logException(e);
		} finally {
			loading = false;
		}
	}

	public boolean setFocus() {
		/*
		 * if (!model.hasFocusSegments()) return false;
		 */
		return super.setFocus();
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
	/**
	 * Gets the marginWidth.
	 * 
	 * @return Returns a int
	 */
	public int getMarginWidth() {
		return marginWidth;
	}

	/**
	 * Sets the marginWidth.
	 * 
	 * @param marginWidth
	 *            The marginWidth to set
	 */
	public void setMarginWidth(int marginWidth) {
		this.marginWidth = marginWidth;
	}

	/**
	 * Gets the marginHeight.
	 * 
	 * @return Returns a int
	 */
	public int getMarginHeight() {
		return marginHeight;
	}

	/**
	 * Sets the marginHeight.
	 * 
	 * @param marginHeight
	 *            The marginHeight to set
	 */
	public void setMarginHeight(int marginHeight) {
		this.marginHeight = marginHeight;
	}

	public void contextMenuAboutToShow(IMenuManager manager) {
		HyperlinkSegment link = getSelectedLink();
		if (link != null)
			contributeLinkActions(manager, link);
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
	ScrolledComposite getScrolledComposite() {
		Composite parent = getParent();
		while (parent != null) {
			if (parent instanceof ScrolledComposite)
				return (ScrolledComposite) parent;
			parent = parent.getParent();
		}
		return null;
	}
}