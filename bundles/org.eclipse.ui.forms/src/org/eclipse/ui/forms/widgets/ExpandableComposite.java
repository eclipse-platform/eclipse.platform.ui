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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.internal.forms.widgets.*;
/**
 * This composite is capable of expanding or collapsing a single client that is
 * its direct child. The composite renders an expansion toggle affordance
 * (according to the chosen style), and a title that also acts as a hyperlink
 * (can be selected and is traversable). The client is layed out below the
 * title when expanded, or hidden when collapsed.
 * <p>The widget can be instantiated as-is, or subclassed to
 * modify some aspects of it.
 * 
 * @see Section
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
	 * If this style is used, title bar decoration will be painted behind
	 * the text.
	 */
	public static final int TITLE_BAR = 1 << 8;
	/**
	 * If this style is used, title will not be rendered.
	 */
	public static final int NO_TITLE = 1<<12;
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
	private int VSPACE = 3;
	public int clientVerticalSpacing = VSPACE;
	protected int GAP = 4;

	private int SEPARATOR_HEIGHT = 2;
	private int expansionStyle = TWISTIE | FOCUS_TITLE | EXPANDED;
	private boolean expanded;
	private Control textClient;
	private Control client;
	private Vector listeners;
	protected ToggleHyperlink toggle;
	protected Control textLabel;
	private class ExpandableLayout extends Layout implements ILayoutExtension {

		private SizeCache toggleCache = new SizeCache();
		private SizeCache textClientCache = new SizeCache();
		private SizeCache textLabelCache = new SizeCache();
		private SizeCache descriptionCache = new SizeCache();
		private SizeCache clientCache = new SizeCache();
	    	    
	    private void initCache(boolean shouldFlush) {
	        toggleCache.setControl(toggle);
	        textClientCache.setControl(textClient);
	        textLabelCache.setControl(textLabel);
	        descriptionCache.setControl(getDescriptionControl());
	        clientCache.setControl(client);
	        
	        if (shouldFlush) {
		        toggleCache.flush();
		        textClientCache.flush();
		        textLabelCache.flush();
		        descriptionCache.flush();
		        clientCache.flush();
	        }
	    }
	    
		protected void layout(Composite parent, boolean changed) {
		    initCache(changed);
		    
			Rectangle clientArea = parent.getClientArea();
			int thmargin = 0;
			int tvmargin = 0;
			
			if ((expansionStyle & TITLE_BAR)!=0) {
				thmargin = GAP;
				tvmargin = GAP;
			}
			int x = marginWidth + thmargin;
			int y = marginHeight + tvmargin;
			Point tsize = null;
			Point tcsize = null;
			if (toggle != null)
				tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int twidth = clientArea.width - marginWidth - marginWidth - thmargin - thmargin;
			if (tsize != null)
				twidth -= tsize.x + GAP;
			if (textClient !=null)
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			if (tcsize!=null)
				twidth -= tcsize.x + GAP;
			Point size = null;
			if (textLabel!=null)
				size = textLabelCache.computeSize(twidth, SWT.DEFAULT);
			if (textLabel instanceof Label) {
				Point defSize = textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
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
				ty += marginHeight + tvmargin;
				toggle.setLocation(x, ty);
				toggle.setSize(tsize);
				x += tsize.x + GAP;
			}
			if (textLabel!=null)
				textLabelCache.setBounds(x, y, size.x, size.y);
			if (textClient!=null) {
				int tcx = clientArea.width - tcsize.x-thmargin;
				textClientCache.setBounds(tcx, y, tcsize.x, tcsize.y);
			}
			if (size!=null)
				y += size.y;
			if ((expansionStyle & TITLE_BAR) != 0) 
				y += tvmargin;
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
				int areaWidth = clientArea.width - marginWidth - marginWidth - thmargin-thmargin;
				int cx = marginWidth + thmargin;
				if ((expansionStyle & CLIENT_INDENT) != 0) {
					cx = x;
					areaWidth -= x;
				}
				if (client != null) {
					Point dsize = null;
					Control desc = getDescriptionControl();
					if (desc != null) {
						dsize = descriptionCache.computeSize(areaWidth, SWT.DEFAULT);
						descriptionCache.setBounds(cx, y, dsize.x, dsize.y);
						y += dsize.y + clientVerticalSpacing;
					}
					else
						y += clientVerticalSpacing - VSPACE;
					//int cwidth = clientArea.width - marginWidth - marginWidth
							//- cx;
					int cwidth = areaWidth;
					int cheight = clientArea.height - marginHeight
							- marginHeight - y;
					clientCache.setBounds(cx, y, cwidth, cheight);
				}
			}
		}
		protected Point computeSize(Composite parent, int wHint, int hHint,
				boolean changed) {
		    
		    initCache(changed);
		    
			int width = 0, height = 0;
			Point tsize = null;
			int twidth = 0;
			if (toggle != null) {
				tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				twidth = tsize.x + GAP;
			}
			int thmargin = 0;
			int tvmargin = 0;
			
			if ((expansionStyle & TITLE_BAR)!=0) {
				thmargin = GAP;
				tvmargin = GAP;
			}
			int innerwHint = wHint;
			if (innerwHint != SWT.DEFAULT)
				innerwHint -= twidth;
			
			int innertHint = innerwHint;
			
			Point tcsize = null;
			if (textClient!=null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (innertHint!=SWT.DEFAULT)
					innertHint -= GAP + tcsize.x;
			}
			Point size = null;
			
			if (textLabel!=null)
				size = textLabelCache.computeSize(innertHint, SWT.DEFAULT);
			if (textLabel instanceof Label) {
				Point defSize = textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			if (size!=null)
				width = size.x;
			int sizey = size!=null?size.y:0;
			height = tcsize!=null?Math.max(tcsize.y, sizey):sizey;
			if (getSeparatorControl() != null) {
				height += VSPACE + SEPARATOR_HEIGHT;
				if (expanded && client != null)
					height += VSPACE;
			}
			if ((expansionStyle & TITLE_BAR) != 0) 
				height += VSPACE;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				int cwHint = wHint;
				
				if (cwHint!=SWT.DEFAULT)
					cwHint -= tvmargin + tvmargin;
				if ((expansionStyle & CLIENT_INDENT) != 0)
					cwHint = innerwHint;
				Point dsize = null;
				Point csize = clientCache.computeSize(FormUtil.getWidthHint(cwHint,
						client), SWT.DEFAULT);
				if (getDescriptionControl() != null) {
					int dwHint = cwHint;
					if (dwHint == SWT.DEFAULT) {
						dwHint = csize.x - tvmargin - tvmargin;
						if ((expansionStyle & CLIENT_INDENT) != 0)
							dwHint -= twidth;
					}
					dsize = descriptionCache.computeSize(dwHint, SWT.DEFAULT);
				}
				if (dsize != null) {
					if ((expansionStyle & CLIENT_INDENT) != 0)
						dsize.x -= twidth;
					width = Math.max(width, dsize.x);
					if (expanded)
						height += dsize.y + clientVerticalSpacing;
				}
				else
					height += clientVerticalSpacing - VSPACE;
				if ((expansionStyle & CLIENT_INDENT) != 0)
					csize.x -= twidth;
				width = Math.max(width, csize.x);
				if (expanded)
					height += csize.y;
			}
			if (toggle != null) {
				height = height - sizey + Math.max(sizey, tsize.y);
				width += twidth;
			}
			return new Point(width + marginWidth + marginWidth+thmargin+thmargin, height
					+ marginHeight + marginHeight+tvmargin+tvmargin);
		}
		public int computeMinimumWidth(Composite parent, boolean changed) {
		    
		    initCache(changed);
		    
			int width = 0;
			Point size = null;
			if (textLabel!=null)
				size = textLabelCache.computeSize(5, SWT.DEFAULT);
			Point tcsize=null;
			if (textClient!=null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			int thmargin = 0;
			int tvmargin = 0;
			
			if ((expansionStyle & TITLE_BAR)!=0) {
				thmargin = GAP;
				tvmargin = GAP;
			}			
			if (size!=null)
				width = size.x;
			if (tcsize!=null)
				width += GAP + tcsize.x;

			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				Point dsize = null;
				if (getDescriptionControl() != null) {
					dsize = descriptionCache.computeSize(5, SWT.DEFAULT);
					width = Math.max(width, dsize.x);
				}
				int cwidth = FormUtil.computeMinimumWidth(client, changed);
				width = Math.max(width, cwidth);
			}
			if (toggle != null) {
				Point tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				width += tsize.x + GAP;
			}
			return width + marginWidth + marginWidth+thmargin+thmargin;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.forms.parts.ILayoutExtension#computeMinimumWidth(org.eclipse.swt.widgets.Composite,
		 *      boolean)
		 */
		public int computeMaximumWidth(Composite parent, boolean changed) {
		    
		    initCache(changed);
		    
			int width = 0;
			Point size = null;
			if (textLabel!=null)
				textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point tcsize=null;
			int thmargin = 0;
			int tvmargin = 0;
			
			if ((expansionStyle & TITLE_BAR)!=0) {
				thmargin = GAP;
				tvmargin = GAP;
			}			
			if (textClient!=null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			if (size!=null)
				width = size.x;
			if (tcsize!=null)
				width += GAP + tcsize.x;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				Point dsize = null;
				if (getDescriptionControl() != null) {
					dsize = descriptionCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					width = Math.max(width, dsize.x);
				}
				int cwidth = FormUtil.computeMaximumWidth(client, changed);
				width = Math.max(width, cwidth);
			}
			if (toggle != null) {
				Point tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				width += tsize.x + GAP;
			}
			return width + marginWidth + marginWidth+thmargin+thmargin;
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
		if ((expansionStyle & TITLE_BAR) != 0) {
			this.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					onPaint(e);
				}
			});
		}
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
		} else if ((expansionStyle & NO_TITLE) == 0) {
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
		if (textLabel!=null)
			textLabel.setBackground(bg);
		if (toggle != null)
			toggle.setBackground(bg);
	}
	/**
	 * Sets the foreground of all the custom controls in the expandable.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (textLabel!=null)
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
		if (textLabel!=null)
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
		else if (textLabel instanceof Hyperlink)
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
		else if (textLabel instanceof Hyperlink)
			return ((Hyperlink) textLabel).getText();
		else
			return "";
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
	public void addExpansionListener(IExpansionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	/**
	 * Removes the expansion listener.
	 * 
	 * @param listener
	 *            the listner to remove
	 */
	public void removeExpansionListener(IExpansionListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	private void toggleState() {
		boolean newState = !isExpanded();
		fireExpanding(newState, true);
		internalSetExpanded(!isExpanded());
		fireExpanding(newState, false);
	}
/**
 * If TITLE_BAR style is used, title bar decoration will
 * be painted behind the text in this method. The default
 * implementation does nothing - subclasses are responsible
 * for rendering the title area.
 * @param e the paint event
 */
	protected void onPaint(PaintEvent e) {
	}
	private void fireExpanding(boolean state, boolean before) {
		int size = listeners.size();
		if (size == 0)
			return;
		ExpansionEvent e = new ExpansionEvent(this, state);
		for (int i = 0; i < size; i++) {
			IExpansionListener listener = (IExpansionListener) listeners.get(i);
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
	/**
	 * Returns the text client control.
	 * @return Returns the text client control if specified, or <code>null</code> if
	 * not.
	 */
	public Control getTextClient() {
		return textClient;
	}
	/**
	 * Sets the text client control. Text client is a control that 
	 * is a child of the expandable composite and is placed to the right
	 * of the text. It can be used to place small image hyperlinks. If
	 * more than one control is needed, use Composite to hold them. Care should
	 * be taken that the height of the control is comparable to the
	 * height of the text.
	 * @param textClient the textClient to set or <code>null</code> if not
	 * needed any more.
	 */
	public void setTextClient(Control textClient) {
		if (this.textClient!=null)
			this.textClient.dispose();
		this.textClient = textClient;
	}
}