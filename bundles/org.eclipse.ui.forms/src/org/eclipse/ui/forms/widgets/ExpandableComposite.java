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
import java.util.Vector;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.internal.widgets.*;
import org.eclipse.ui.forms.internal.widgets.FormsResources;
/**
 * This composite is capable of expanding or collapsing a single client that is
 * its direct child. The composite renders an expansion toggle affordance
 * (according to the chosen style), and a title that also acts as a hyperlink
 * (can be selected and is traversable). The client is layed out below the
 * title when expanded, or hidden when collapsed.
 * 
 * TODO (dejan) - spell out subclass contract
 * @since 3.0
 */
public class ExpandableComposite extends Composite {
	/**
	 * If this style is used, a twistie will be used to render the expansion
	 * toggle.
	 */
	public static final int TWISTIE = 1 << 1;
	/**
	 * If this style is used, a tree node with either + or - signs will be used
	 * to render the expansion toggle.
	 */
	public static final int TREE_NODE = 1 << 2;
	/**
	 * If this style is used, the title text will be rendered as a hyperlink
	 * that can individually accept focus. Otherwise, it will still act like a
	 * hyperlink, but only the toggle control will accept focus.
	 */
	public static final int FOCUS_TITLE = 1 << 3;
	/**
	 * If this style is used, the client origin will be vertically aligned with
	 * the title text. Otherwise, it will start at x = 0.
	 */
	public static final int CLIENT_INDENT = 1 << 4;
	/**
	 * If this style is used, computed size of the composite will take the
	 * client width into consideration only in the expanded state. Otherwise,
	 * client width will always be taken into acount.
	 */
	public static final int COMPACT = 1 << 5;
	/**
	 * If this style is used, the control will be created in the expanded
	 * state. This state can later be changed programmatically or by the user
	 * if TWISTIE or TREE_NODE style is used.
	 */
	public static final int EXPANDED = 1 << 6;
	/**
	 * Width of the margin that will be added around the control (default is
	 * 0).
	 */
	public int marginWidth = 0;
	/**
	 * Height of the margin that will be added around the control (default is
	 * 0).
	 */
	public int marginHeight = 0;
	private int GAP = 4;
	private int VSPACE = 3;
	private int SEPARATOR_HEIGHT = 2;
	private int expansionStyle = TWISTIE | FOCUS_TITLE | EXPANDED;
	private boolean expanded;
	private Control client;
	private Vector listeners;
	protected ToggleHyperlink toggle;
	protected Control textLabel;
	private class ExpandableLayout extends Layout implements ILayoutExtension {
		protected void layout(Composite parent, boolean changed) {
			Rectangle clientArea = parent.getClientArea();
			int x = marginWidth;
			int y = marginHeight;
			Point tsize = null;
			if (toggle != null)
				tsize = toggle.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			int twidth = clientArea.width - marginWidth - marginWidth;
			if (tsize != null)
				twidth -= tsize.x + GAP;
			Point size = textLabel.computeSize(twidth, SWT.DEFAULT, changed);
			if (textLabel instanceof Label) {
				Point defSize = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT,
						changed);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			if (toggle != null) {
				GC gc = new GC(ExpandableComposite.this);
				gc.setFont(getFont());
				FontMetrics fm = gc.getFontMetrics();
				int fontHeight = fm.getHeight();
				gc.dispose();
				int ty = fontHeight / 2 - tsize.y / 2 + 1;
				ty = Math.max(ty, 0);
				toggle.setLocation(x, ty);
				toggle.setSize(tsize);
				x += tsize.x + GAP;
			}
			textLabel.setBounds(x, y, size.x, size.y);
			y += size.y;
			if (getSeparatorControl() != null) {
				y += VSPACE;
				getSeparatorControl().setBounds(marginWidth, y,
						clientArea.width - marginWidth - marginWidth,
						SEPARATOR_HEIGHT);
				y += SEPARATOR_HEIGHT;
				if (expanded)
					y += VSPACE;
			}
			if (expanded) {
				int areaWidth = clientArea.width - marginWidth - marginWidth;
				int cx = marginWidth;
				if ((expansionStyle & CLIENT_INDENT) != 0) {
					cx = x;
					areaWidth -= x;
				}
				if (client != null) {
					Point dsize = null;
					Control desc = getDescriptionControl();
					if (desc != null) {
						dsize = desc.computeSize(areaWidth, SWT.DEFAULT,
								changed);
						desc.setBounds(cx, y, dsize.x, dsize.y);
						y += dsize.y + VSPACE;
					}
					int cwidth = clientArea.width - marginWidth - marginWidth
							- cx;
					int cheight = clientArea.height - marginHeight
							- marginHeight - y;
					client.setBounds(cx, y, cwidth, cheight);
				}
			}
		}
		protected Point computeSize(Composite parent, int wHint, int hHint,
				boolean changed) {
			int width = 0, height = 0;
			Point tsize = null;
			int twidth = 0;
			if (toggle != null) {
				tsize = toggle.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
				twidth = tsize.x + GAP;
			}
			int innerwHint = wHint;
			if (innerwHint != SWT.DEFAULT)
				innerwHint -= twidth;
			Point size = textLabel
					.computeSize(innerwHint, SWT.DEFAULT, changed);
			if (textLabel instanceof Label) {
				Point defSize = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT,
						changed);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			width = size.x;
			height = size.y;
			if (getSeparatorControl() != null) {
				height += VSPACE + SEPARATOR_HEIGHT;
				if (expanded && client != null)
					height += VSPACE;
			}
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				int cwHint = wHint;
				if ((expansionStyle & CLIENT_INDENT) != 0)
					cwHint = innerwHint;
				Point dsize = null;
				Point csize = client.computeSize(FormUtil.getWidthHint(cwHint,
						client), SWT.DEFAULT, changed);
				if (getDescriptionControl() != null) {
					int dwHint = cwHint;
					if (dwHint == SWT.DEFAULT) {
						dwHint = csize.x;
						if ((expansionStyle & CLIENT_INDENT) != 0)
							dwHint -= twidth;
					}
					dsize = getDescriptionControl().computeSize(dwHint,
							SWT.DEFAULT, changed);
				}
				if (dsize != null) {
					if ((expansionStyle & CLIENT_INDENT) != 0)
						dsize.x -= twidth;
					width = Math.max(width, dsize.x);
					if (expanded)
						height += dsize.y + VSPACE;
				}
				if ((expansionStyle & CLIENT_INDENT) != 0)
					csize.x -= twidth;
				width = Math.max(width, csize.x);
				if (expanded)
					height += csize.y;
			}
			if (toggle != null) {
				height = height - size.y + Math.max(size.y, tsize.y);
				width += twidth;
			}
			return new Point(width + marginWidth + marginWidth, height
					+ marginHeight + marginHeight);
		}
		public int computeMinimumWidth(Composite parent, boolean changed) {
			int width = 0;
			Point size = textLabel.computeSize(5, SWT.DEFAULT, changed);
			width = size.x;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				Point dsize = null;
				if (getDescriptionControl() != null) {
					dsize = getDescriptionControl().computeSize(5, SWT.DEFAULT,
							changed);
					width = Math.max(width, dsize.x);
				}
				int cwidth = FormUtil.computeMinimumWidth(client, changed);
				width = Math.max(width, cwidth);
			}
			if (toggle != null) {
				Point tsize = toggle.computeSize(SWT.DEFAULT, SWT.DEFAULT,
						changed);
				width += tsize.x + GAP;
			}
			return width + marginWidth + marginWidth;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.forms.parts.ILayoutExtension#computeMinimumWidth(org.eclipse.swt.widgets.Composite,
		 *      boolean)
		 */
		public int computeMaximumWidth(Composite parent, boolean changed) {
			int width = 0;
			Point size = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT,
					changed);
			width = size.x;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				Point dsize = null;
				if (getDescriptionControl() != null) {
					dsize = getDescriptionControl().computeSize(SWT.DEFAULT,
							SWT.DEFAULT, changed);
					width = Math.max(width, dsize.x);
				}
				int cwidth = FormUtil.computeMaximumWidth(client, changed);
				width = Math.max(width, cwidth);
			}
			if (toggle != null) {
				Point tsize = toggle.computeSize(SWT.DEFAULT, SWT.DEFAULT,
						changed);
				width += tsize.x + GAP;
			}
			return width + marginWidth + marginWidth;
		}
	}
	/**
	 * Creates an expandable composite using a TWISTIE toggle.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            SWT style bits
	 */
	public ExpandableComposite(Composite parent, int style) {
		this(parent, style, TWISTIE);
	}
	/**
	 * Creates the expandable composite in the provided parent.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the control style
	 * @param expansionStyle
	 *            the style of the expansion widget (TREE_NODE, TWISTIE,
	 *            CLIENT_INDENT, COMPACT, FOCUS_TITLE)
	 */
	public ExpandableComposite(Composite parent, int style, int expansionStyle) {
		super(parent, style);
		this.expansionStyle = expansionStyle;
		super.setLayout(new ExpandableLayout());
		listeners = new Vector();
		if ((expansionStyle & TWISTIE) != 0)
			toggle = new Twistie(this, SWT.NULL);
		else if ((expansionStyle & TREE_NODE) != 0)
			toggle = new TreeNode(this, SWT.NULL);
		else
			expanded = true;
		if ((expansionStyle & EXPANDED) != 0)
			expanded = true;
		if (toggle != null) {
			toggle.setExpanded(expanded);
			toggle.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					toggleState();
				}
			});
		}
		if ((expansionStyle & FOCUS_TITLE) != 0) {
			Hyperlink link = new Hyperlink(this, SWT.WRAP);
			link.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					toggle.setExpanded(!toggle.isExpanded());
					toggleState();
				}
			});
			textLabel = link;
		} else {
			final Label label = new Label(this, SWT.WRAP);
			if (!isFixedStyle()) {
				label.setCursor(FormsResources.getHandCursor());
				label.addListener(SWT.MouseDown, new Listener() {
					public void handleEvent(Event e) {
						if (toggle != null)
							toggle.setFocus();
					}
				});
				label.addListener(SWT.MouseUp, new Listener() {
					public void handleEvent(Event e) {
						label.setCursor(FormsResources.getBusyCursor());
						toggle.setExpanded(!toggle.isExpanded());
						toggleState();
						label.setCursor(FormsResources.getHandCursor());
					}
				});
			}
			textLabel = label;
		}
		if (textLabel!=null)
			textLabel.setMenu(getMenu());
	}
	/**
	 * Prevents assignment of the layout manager - expandable composite uses
	 * its own layout.
	 */
	public final void setLayout(Layout layout) {
	}
	/**
	 * Sets the background of all the custom controls in the expandable.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		textLabel.setBackground(bg);
		if (toggle != null)
			toggle.setBackground(bg);
	}
	/**
	 * Sets the foreground of all the custom controls in the expandable.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		textLabel.setForeground(fg);
		if (toggle != null)
			toggle.setForeground(fg);
	}
	/**
	 * Sets the color of the toggle control.
	 * 
	 * @param c
	 *            the color object
	 */
	public void setToggleColor(Color c) {
		if (toggle != null)
			toggle.setDecorationColor(c);
	}
	/**
	 * Sets the active color of the toggle control (when the mouse enters the
	 * toggle area).
	 * 
	 * @param c
	 *            the active color object
	 */
	public void setActiveToggleColor(Color c) {
		if (toggle != null)
			toggle.setHoverDecorationColor(c);
	}
	/**
	 * Sets the fonts of all the custom controls in the expandable.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		textLabel.setFont(font);
		if (toggle != null)
			toggle.setFont(font);
	}
	/**
	 * Sets the client of this expandable composite. The client must not be
	 * <samp>null </samp> and must be a direct child of this container.
	 * 
	 * @param client
	 *            the client that will be expanded or collapsed
	 */
	public void setClient(Control client) {
		Assert.isTrue(client != null && client.getParent().equals(this));
		this.client = client;
	}
	/**
	 * Returns the current expandable client.
	 * 
	 * @return the client control
	 */
	public Control getClient() {
		return client;
	}
	/**
	 * Sets the title of the expandable composite. The title will act as a
	 * hyperlink and activating it will toggle the client between expanded and
	 * collapsed state.
	 * 
	 * @param title
	 *            the new title string
	 * @see #getTitle
	 */
	public void setText(String title) {
		if (textLabel instanceof Label)
			((Label) textLabel).setText(title);
		else
			((Hyperlink) textLabel).setText(title);
	}
	/**
	 * Returns the title string.
	 * 
	 * @return the title string
	 * @see #setTitle
	 */
	public String getText() {
		if (textLabel instanceof Label)
			return ((Label) textLabel).getText();
		else
			return ((Hyperlink) textLabel).getText();
	}
	/**
	 * Tests the expanded state of the composite.
	 * 
	 * @return <samp>true </samp> if expanded, <samp>false </samp> if
	 *         collapsed.
	 */
	public boolean isExpanded() {
		return expanded;
	}
	/**
	 * Returns the bitwise-ORed style bits for the expansion control.
	 * 
	 * @return
	 */
	public int getExpansionStyle() {
		return expansionStyle;
	}
	/**
	 * Programmatically changes expanded state.
	 * 
	 * @param expanded
	 *            the new expanded state
	 */
	public void setExpanded(boolean expanded) {
		internalSetExpanded(expanded);
		if (toggle != null)
			toggle.setExpanded(expanded);
	}
	/**
	 * Performs the expansion state change for the expandable control.
	 * 
	 * @param expanded
	 *            the expansion state
	 */
	protected void internalSetExpanded(boolean expanded) {
		if (this.expanded != expanded) {
			this.expanded = expanded;
			if (getDescriptionControl() != null)
				getDescriptionControl().setVisible(expanded);
			if (client != null)
				client.setVisible(expanded);
			layout();
		}
	}
	/**
	 * Adds the listener that will be notified when the expansion state
	 * changes.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addExpansionListener(ExpansionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	/**
	 * Removes the expansion listener.
	 * 
	 * @param listener
	 *            the listner to remove
	 */
	public void removeExpansionListener(ExpansionListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	private void toggleState() {
		boolean newState = !isExpanded();
		fireExpanding(newState, true);
		internalSetExpanded(!isExpanded());
		fireExpanding(newState, false);
	}
	private void fireExpanding(boolean state, boolean before) {
		int size = listeners.size();
		if (size == 0)
			return;
		ExpansionEvent e = new ExpansionEvent(this, state);
		for (int i = 0; i < size; i++) {
			ExpansionListener listener = (ExpansionListener) listeners.get(i);
			if (before)
				listener.expansionStateChanging(e);
			else
				listener.expansionStateChanged(e);
		}
	}
	/**
	 * Returns description control that will be placed under the title if
	 * present.
	 * 
	 * @return the description control or <samp>null </samp> if not used.
	 */
	protected Control getDescriptionControl() {
		return null;
	}
	/**
	 * Returns the separator control that will be placed between the title and
	 * the description if present.
	 * 
	 * @return the separator control or <samp>null </samp> if not used.
	 */
	protected Control getSeparatorControl() {
		return null;
	}
	/**
	 * Computes the size of the expandable composite.
	 * 
	 * @see org.eclipse.swt.widgets.Composite#computeSize
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point size;
		ExpandableLayout layout = (ExpandableLayout) getLayout();
		if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
			size = layout.computeSize(this, wHint, hHint, changed);
		} else {
			size = new Point(wHint, hHint);
		}
		Rectangle trim = computeTrim(0, 0, size.x, size.y);
		return new Point(trim.width, trim.height);
	}
	/**
	 * Returns <samp>true </samp> if the composite is fixed i.e. cannot be
	 * expanded or collapsed. Fixed control will still contain the title,
	 * separator and description (if present) as well as the client, but will
	 * be in the permanent expanded state and the toggle affordance will not be
	 * shown.
	 * 
	 * @return <samp>true </samp> if the control is fixed in the expanded
	 *         state, <samp>false </samp> if it can be collapsed.
	 */
	protected boolean isFixedStyle() {
		return (expansionStyle & TWISTIE) == 0
				&& (expansionStyle & TREE_NODE) == 0;
	}
}