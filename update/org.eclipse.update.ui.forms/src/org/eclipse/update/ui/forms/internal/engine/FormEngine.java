package org.eclipse.update.ui.forms.internal.engine;

import java.io.InputStream;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.update.ui.forms.internal.HyperlinkSettings;

public class FormEngine extends Canvas {
	public static final String URL_HANDLER_ID = "urlHandler";
	boolean hasFocus;
	boolean paragraphsSeparated = true;
	String text;
	TextModel model;
	Hashtable objectTable = new Hashtable();
	public int marginWidth = 0;
	public int marginHeight = 1;
	IHyperlinkSegment entered;
	boolean mouseDown = false;
	Point dragOrigin;

	public boolean getFocus() {
		return hasFocus;
	}
	
	public int getParagraphSpacing(int lineHeight) {
		return lineHeight/2;
	}

	public void setParagraphsSeparated(boolean value) {
		paragraphsSeparated = value;
	}

	/**
	 * Constructor for SelectableFormLabel
	 */
	public FormEngine(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormEngineLayout());
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
				if (e.character=='\r') {
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
					exitLink(entered);
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
	}

	private void handleMouseClick(MouseEvent e, boolean down) {
		if (down) {
			// select a hyperlink
			IHyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
			if (segmentUnder != null) {
				IHyperlinkSegment oldLink = model.getSelectedLink();
				model.selectLink(segmentUnder);
				enterLink(segmentUnder);
				paintFocusTransfer(oldLink, segmentUnder);
			}
			mouseDown=true;
			dragOrigin = new Point(e.x, e.y);
		} else {
			IHyperlinkSegment segmentUnder = model.findHyperlinkAt(e.x, e.y);
			if (segmentUnder != null) {
				activateLink(segmentUnder);
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
		ITextSegment segmentUnder = model.findSegmentAt(e.x, e.y);
		
		if (segmentUnder == null) {
			if (entered != null) {
				exitLink(entered);
				entered = null;
			}
			setCursor(null);
		} else {
			if (segmentUnder instanceof IHyperlinkSegment) {
				IHyperlinkSegment linkUnder = (IHyperlinkSegment)segmentUnder;
				if (entered == null) {
					entered = linkUnder;
					enterLink(linkUnder);
					setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
				}
			}
			else {
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
		IHyperlinkSegment current = model.getSelectedLink();
		if (current!=null) exitLink(current);
		
		boolean valid = model.traverseLinks(next);
		
		if (valid)
			enterLink(model.getSelectedLink());
		paintFocusTransfer(current, model.getSelectedLink());
		return !valid;
	}

	public IHyperlinkSegment getSelectedLink() {
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

	private void enterLink(IHyperlinkSegment link) {
		if (link == null)
			return;
		HyperlinkAction action = link.getAction(objectTable);
		if (action != null)
			action.linkEntered(link);
	}

	private void exitLink(IHyperlinkSegment link) {
		if (link == null)
			return;
		HyperlinkAction action = link.getAction(objectTable);
		if (action != null)
			action.linkExited(link);
	}

	private void activateSelectedLink() {
		IHyperlinkSegment link = model.getSelectedLink();
		if (link != null)
			activateLink(link);
	}

	private void activateLink(IHyperlinkSegment link) {
		setCursor(model.getHyperlinkSettings().getBusyCursor());
		HyperlinkAction action = link.getAction(objectTable);
		if (action != null)
			action.linkActivated(link);
		setCursor(model.getHyperlinkSettings().getHyperlinkCursor());
	}

	protected void paint(PaintEvent e) {
		int width = getClientArea().width;
		IParagraph[] paragraphs = model.getParagraphs();

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

		IHyperlinkSegment selectedLink = model.getSelectedLink();

		for (int i = 0; i < paragraphs.length; i++) {
			IParagraph p = paragraphs[i];

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
			//FIXME at least log
		}
	}
	public void load(InputStream is, boolean expandURLs) {
		try {
			model.parseInputStream(is, expandURLs);
		} catch (CoreException e) {
			// FIXME at least log
		}
	}

	public boolean setFocus() {
		/*
		if (!model.hasFocusSegments())
			return false;
		*/
		return super.setFocus();
	}
	
	private void paintFocusTransfer(IHyperlinkSegment oldLink, IHyperlinkSegment newLink) {
		GC gc = new GC(this);
		Color bg = getBackground();
		Color fg = getForeground();
		
		gc.setFont(getFont());

		if (oldLink!=null) {
			gc.setBackground(bg);
			gc.setForeground(fg);
			oldLink.paintFocus(gc, bg, fg, false);
		}
		if (newLink!=null) {
			gc.setBackground(bg);
			gc.setForeground(fg);
			newLink.paintFocus(gc, bg, fg, true);
		}
		gc.dispose();
	}
	/**
	 * Gets the marginWidth.
	 * @return Returns a int
	 */
	public int getMarginWidth() {
		return marginWidth;
	}

	/**
	 * Sets the marginWidth.
	 * @param marginWidth The marginWidth to set
	 */
	public void setMarginWidth(int marginWidth) {
		this.marginWidth = marginWidth;
	}

	/**
	 * Gets the marginHeight.
	 * @return Returns a int
	 */
	public int getMarginHeight() {
		return marginHeight;
	}

	/**
	 * Sets the marginHeight.
	 * @param marginHeight The marginHeight to set
	 */
	public void setMarginHeight(int marginHeight) {
		this.marginHeight = marginHeight;
	}

}