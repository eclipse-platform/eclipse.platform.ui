/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import java.util.Vector;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.eclipse.ui.internal.forms.widgets.FormsResources;

/**
 * This composite is capable of expanding or collapsing a single client that is
 * its direct child. The composite renders an expansion toggle affordance
 * (according to the chosen style), and a title that also acts as a hyperlink
 * (can be selected and is traversable). The client is layed out below the title
 * when expanded, or hidden when collapsed.
 * <p>
 * The widget can be instantiated as-is, or subclassed to modify some aspects of
 * it.
 * 
 * <p>
 * Since 3.1, left/right arrow keys can be used to control the expansion state.
 * If several expandable composites are created in the same parent, up/down
 * arrow keys can be used to traverse between them. Expandable text accepts
 * mnemonics and mnemonic activation will toggle the expansion state.
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
	 * If this style is used, the control will be created in the expanded state.
	 * This state can later be changed programmatically or by the user if
	 * TWISTIE or TREE_NODE style is used.
	 */
	public static final int EXPANDED = 1 << 6;

	/**
	 * If this style is used, title bar decoration will be painted behind the
	 * text.
	 */
	public static final int TITLE_BAR = 1 << 8;

	/**
	 * If this style is used, a short version of the title bar decoration will
	 * be painted behind the text. This style is useful when a more descrete
	 * option is needed for the title bar.
	 * 
	 * @since 3.1
	 */
	public static final int SHORT_TITLE_BAR = 1 << 9;

	/**
	 * If this style is used, title will not be rendered.
	 */
	public static final int NO_TITLE = 1 << 12;

	/**
	 * By default, text client is right-aligned. If this style is used, it will
	 * be positioned after the text control and vertically centered with it.
	 */
	public static final int LEFT_TEXT_CLIENT_ALIGNMENT = 1 << 13;

	/**
	 * Width of the margin that will be added around the control (default is 0).
	 */
	public int marginWidth = 0;

	/**
	 * Height of the margin that will be added around the control (default is
	 * 0).
	 */
	public int marginHeight = 0;

	/**
	 * Vertical spacing between the title area and the composite client control
	 * (default is 3).
	 */
	public int clientVerticalSpacing = 3;

	private static final Point NULL_SIZE = new Point(0, 0);

	private int VSPACE = 3;

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

			if (hasTitleBar()) {
				thmargin = GAP;
				tvmargin = GAP;
			}
			int x = marginWidth + thmargin;
			int y = marginHeight + tvmargin;
			Point tsize = NULL_SIZE;
			Point tcsize = NULL_SIZE;
			if (toggle != null)
				tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int twidth = clientArea.width - marginWidth - marginWidth
					- thmargin - thmargin;
			//System.out.println(getText()+", Inner width: "+twidth); //$NON-NLS-1$
			if (tsize.x > 0)
				twidth -= tsize.x + GAP;
			if (textClient != null)
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			if (tcsize.x > 0)
				twidth -= tcsize.x + GAP;
			Point size = NULL_SIZE;
			if (textLabel != null)
				size = textLabelCache.computeSize(twidth, SWT.DEFAULT);
			if (textLabel instanceof Label) {
				Point defSize = textLabelCache.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			if (toggle != null) {
				GC gc = new GC(ExpandableComposite.this);
				gc.setFont(getFont());
				FontMetrics fm = gc.getFontMetrics();
				int textHeight = fm.getHeight();
				gc.dispose();
				if (textClient != null
						&& (expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					textHeight = Math.max(textHeight, tcsize.y);
				}
				int ty = textHeight / 2 - tsize.y / 2 + 1;
				ty = Math.max(ty, 0);
				ty += marginHeight + tvmargin;
				toggle.setLocation(x, ty);
				toggle.setSize(tsize);
				x += tsize.x + GAP;
			}
			if (textLabel != null) {
				int ty = y;
				if (textClient != null
						&& (expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					if (size.y < tcsize.y)
						ty = tcsize.y / 2 - size.y / 2 + marginHeight
								+ tvmargin;
				}
				textLabelCache.setBounds(x, ty, size.x, size.y);
				//System.out
				//		.println(getText()
				//				+ ", Actual text size: " + size + ", cwidth=" + clientArea.width); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (textClient != null) {
				int tcx;
				if ((expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					tcx = x + size.x + GAP;
					/*
					 * if (tsize.y > tcsize.y) tcy = tsize.y + marginHeight +
					 * tvmargin - tcsize.y; else tcy = y;
					 */
				} else {
					tcx = clientArea.width - tcsize.x - marginWidth - thmargin;
					// tcy = y;
				}
				textClientCache.setBounds(tcx, y, tcsize.x, tcsize.y);
			}
			int tbarHeight = 0;
			if (size.y>0)
				tbarHeight = size.y;
			if (tcsize.y > 0)
				tbarHeight = Math.max(tbarHeight, tcsize.y);
			y += tbarHeight;
			if (hasTitleBar())
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
				int areaWidth = clientArea.width - marginWidth - marginWidth
						- thmargin - thmargin;
				int cx = marginWidth + thmargin;
				if ((expansionStyle & CLIENT_INDENT) != 0) {
					cx = x;
					areaWidth -= x;
				}
				if (client != null) {
					Point dsize = null;
					Control desc = getDescriptionControl();
					if (desc != null) {
						dsize = descriptionCache.computeSize(areaWidth,
								SWT.DEFAULT);
						descriptionCache.setBounds(cx, y, dsize.x, dsize.y);
						y += dsize.y + clientVerticalSpacing;
					} else
						y += clientVerticalSpacing - VSPACE;
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
			Point tsize = NULL_SIZE;
			int twidth = 0;
			if (toggle != null) {
				tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				twidth = tsize.x + GAP;
			}
			int thmargin = 0;
			int tvmargin = 0;

			if (hasTitleBar()) {
				thmargin = GAP;
				tvmargin = GAP;
			}
			int innerwHint = wHint;
			if (innerwHint != SWT.DEFAULT)
				innerwHint -= twidth;

			int innertHint = innerwHint;

			Point tcsize = NULL_SIZE;
			if (textClient != null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (innertHint != SWT.DEFAULT)
					innertHint -= GAP + tcsize.x;
			}
			Point size = NULL_SIZE;

			if (textLabel != null)
				size = textLabelCache.computeSize(innertHint, SWT.DEFAULT);
			if (textLabel instanceof Label) {
				Point defSize = textLabelCache.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			//System.out.println(getText()
			//		+ ", Computed text size: " + size + ", wHint=" + wHint); //$NON-NLS-1$ //$NON-NLS-2$
			if (size.x > 0)
				width = size.x;
			if (tcsize.x>0)
				width += GAP + tcsize.x;
			height = tcsize.y > 0 ? Math.max(tcsize.y, size.y) : size.y;
			if (getSeparatorControl() != null) {
				height += VSPACE + SEPARATOR_HEIGHT;
				if (expanded && client != null)
					height += VSPACE;
			}
			if (hasTitleBar())
				height += VSPACE;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				int cwHint = wHint;

				if (cwHint != SWT.DEFAULT) {
					cwHint -= marginWidth + marginWidth + thmargin + thmargin;
					if ((expansionStyle & CLIENT_INDENT) != 0)
						cwHint = innerwHint - marginWidth - marginWidth;
				}
				Point dsize = null;
				Point csize = clientCache.computeSize(FormUtil.getWidthHint(
						cwHint, client), SWT.DEFAULT);
				if (getDescriptionControl() != null) {
					int dwHint = cwHint;
					if (dwHint == SWT.DEFAULT) {
						dwHint = csize.x;
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
				} else
					height += clientVerticalSpacing - VSPACE;
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
			
			/*
			 * return new Point(width + marginWidth + marginWidth + thmargin +
			 * thmargin, height + marginHeight + marginHeight + tvmargin +
			 * tvmargin);
			 */
			Point result = new Point(width + marginWidth + marginWidth
					+ thmargin + thmargin, height + marginHeight + marginHeight
					+ tvmargin + tvmargin);
			//System.out.println(getText()
			//		+ ", Computed total size: " + result + ", wHint=" + wHint + ", innerWidth="+width); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return result;
		}

		public int computeMinimumWidth(Composite parent, boolean changed) {
			initCache(changed);

			int width = 0;
			Point size = NULL_SIZE;
			if (textLabel != null)
				size = textLabelCache.computeSize(5, SWT.DEFAULT);
			Point tcsize = NULL_SIZE;
			if (textClient != null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			int thmargin = 0;

			if (hasTitleBar()) {
				thmargin = GAP;
			}
			if (size.x > 0)
				width = size.x;
			if (tcsize.x > 0)
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
			return width + marginWidth + marginWidth + thmargin + thmargin;
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
			Point size = NULL_SIZE;
			if (textLabel != null)
				size = textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point tcsize = NULL_SIZE;
			int thmargin = 0;

			if (hasTitleBar()) {
				thmargin = GAP;
			}
			if (textClient != null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			if (size.x > 0)
				width = size.x;
			if (tcsize.x > 0)
				width += GAP + tcsize.x;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				Point dsize = null;
				if (getDescriptionControl() != null) {
					dsize = descriptionCache.computeSize(SWT.DEFAULT,
							SWT.DEFAULT);
					width = Math.max(width, dsize.x);
				}
				int cwidth = FormUtil.computeMaximumWidth(client, changed);
				width = Math.max(width, cwidth);
			}
			if (toggle != null) {
				Point tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				width += tsize.x + GAP;
			}
			return width + marginWidth + marginWidth + thmargin + thmargin;
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
		if (hasTitleBar()) {
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
			toggle.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					if (textLabel instanceof Label && !isFixedStyle())
						textLabel.setForeground(toggle.hover ? toggle
								.getHoverDecorationColor() : null);
				}
			});
			toggle.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.ARROW_UP)
						verticalMove(false);
					else if (e.keyCode == SWT.ARROW_DOWN)
						verticalMove(true);
				}
			});
		}
		if ((expansionStyle & FOCUS_TITLE) != 0) {
			Hyperlink link = new Hyperlink(this, SWT.WRAP);
			link.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					programmaticToggleState();
				}
			});
			textLabel = link;
		} else if ((expansionStyle & NO_TITLE) == 0) {
			final Label label = new Label(this, SWT.WRAP);
			if (!isFixedStyle()) {
				label.setCursor(FormsResources.getHandCursor());
				Listener listener = new Listener() {
					public void handleEvent(Event e) {
						switch (e.type) {
						case SWT.MouseDown:
							if (toggle != null)
								toggle.setFocus();
							break;
						case SWT.MouseUp:
							label.setCursor(FormsResources.getBusyCursor());
							programmaticToggleState();
							label.setCursor(FormsResources.getHandCursor());
							break;
						case SWT.MouseEnter:
							if (toggle != null) {
								label.setForeground(toggle
										.getHoverDecorationColor());
								toggle.hover = true;
								toggle.redraw();
							}
							break;
						case SWT.MouseExit:
							if (toggle != null) {
								label.setForeground(null);
								toggle.hover = false;
								toggle.redraw();
							}
							break;
						}
					}
				};
				label.addListener(SWT.MouseDown, listener);
				label.addListener(SWT.MouseUp, listener);
				label.addListener(SWT.MouseEnter, listener);
				label.addListener(SWT.MouseExit, listener);
			}
			textLabel = label;
		}
		if (textLabel != null) {
			textLabel.setMenu(getMenu());
			textLabel.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_MNEMONIC) {
						// steal the mnemonic
						if (!isVisible() || !isEnabled())
							return;
						if (FormUtil.mnemonicMatch(getText(), e.character)) {
							e.doit = false;
							programmaticToggleState();
							setFocus();
						}
					}
				}
			});
		}
	}

	/**
	 * Overrides 'super' to pass the menu to the text label.
	 * 
	 * @param menu
	 *            the menu from the parent to attach to this control.
	 */

	public void setMenu(Menu menu) {
		if (textLabel != null)
			textLabel.setMenu(menu);
		super.setMenu(menu);
	}

	/**
	 * Prevents assignment of the layout manager - expandable composite uses its
	 * own layout.
	 */
	public final void setLayout(Layout layout) {
	}

	/**
	 * Sets the background of all the custom controls in the expandable.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (textLabel != null)
			textLabel.setBackground(bg);
		if (toggle != null)
			toggle.setBackground(bg);
	}

	/**
	 * Sets the foreground of all the custom controls in the expandable.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (textLabel != null)
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
		if (textLabel != null)
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
			return ""; //$NON-NLS-1$
	}

	/**
	 * Tests the expanded state of the composite.
	 * 
	 * @return <samp>true </samp> if expanded, <samp>false </samp> if collapsed.
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
	 * Adds the listener that will be notified when the expansion state changes.
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
		internalSetExpanded(newState);
		fireExpanding(newState, false);
		if (newState)
			FormUtil.ensureVisible(this);
	}

	/**
	 * If TITLE_BAR or SHORT_TITLE_BAR style is used, title bar decoration will
	 * be painted behind the text in this method. The default implementation
	 * does nothing - subclasses are responsible for rendering the title area.
	 * 
	 * @param e
	 *            the paint event
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
	 * separator and description (if present) as well as the client, but will be
	 * in the permanent expanded state and the toggle affordance will not be
	 * shown.
	 * 
	 * @return <samp>true </samp> if the control is fixed in the expanded state,
	 *         <samp>false </samp> if it can be collapsed.
	 */
	protected boolean isFixedStyle() {
		return (expansionStyle & TWISTIE) == 0
				&& (expansionStyle & TREE_NODE) == 0;
	}

	/**
	 * Returns the text client control.
	 * 
	 * @return Returns the text client control if specified, or
	 *         <code>null</code> if not.
	 */
	public Control getTextClient() {
		return textClient;
	}

	/**
	 * Sets the text client control. Text client is a control that is a child of
	 * the expandable composite and is placed to the right of the text. It can
	 * be used to place small image hyperlinks. If more than one control is
	 * needed, use Composite to hold them. Care should be taken that the height
	 * of the control is comparable to the height of the text.
	 * 
	 * @param textClient
	 *            the textClient to set or <code>null</code> if not needed any
	 *            more.
	 */
	public void setTextClient(Control textClient) {
		if (this.textClient != null)
			this.textClient.dispose();
		this.textClient = textClient;
	}

	protected boolean hasTitleBar() {
		return (getExpansionStyle() & TITLE_BAR) != 0
				|| (getExpansionStyle() & SHORT_TITLE_BAR) != 0;
	}

	private void verticalMove(boolean down) {
		Composite parent = getParent();
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			if (child == this) {
				ExpandableComposite sibling = getSibling(children, i, down);
				if (sibling != null && sibling.toggle != null) {
					sibling.setFocus();
				}
				break;
			}
		}
	}

	private ExpandableComposite getSibling(Control[] children, int index,
			boolean down) {
		int loc = down ? index + 1 : index - 1;
		while (loc >= 0 && loc < children.length) {
			Control c = children[loc];
			if (c instanceof ExpandableComposite && c.isVisible())
				return (ExpandableComposite) c;
			loc = down ? loc + 1 : loc - 1;
		}
		return null;
	}

	private void programmaticToggleState() {
		if (toggle != null)
			toggle.setExpanded(!toggle.isExpanded());
		toggleState();
	}
}