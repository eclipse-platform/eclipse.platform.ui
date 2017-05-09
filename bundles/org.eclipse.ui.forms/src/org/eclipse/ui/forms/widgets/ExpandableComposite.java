/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kai Nacke - Fix for Bug 202382
 *     Bryan Hunt - Fix for Bug 245457
 *     Didier Villevalois - Fix for Bug 178534
 *     Robin Stocker - Fix for Bug 193034 (tool tip also on text)
 *     Alena Laskavaia - Bug 481604, Bug 482024
 *     Ralf Petter <ralf.petter@gmail.com> - Bug 183675
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
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
 * (can be selected and is traversable). The client is laid out below the title
 * when expanded, or hidden when collapsed.
 * <p>
 * The widget can be instantiated as-is, or subclassed to modify some aspects of
 * it. *
 * <p>
 * Since 3.1, left/right arrow keys can be used to control the expansion state.
 * If several expandable composites are created in the same parent, up/down
 * arrow keys can be used to traverse between them. Expandable text accepts
 * mnemonics and mnemonic activation will toggle the expansion state.
 *
 * <p>
 * While expandable composite recognize that different styles can be used to
 * render the title bar, and even defines the constants for these styles
 * (<code>TITLE_BAR</code> and <code>SHORT_TITLE_BAR</code> the actual painting
 * is done in the subclasses.
 *
 * @see Section
 * @since 3.0
 */
public class ExpandableComposite extends Canvas {
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
	 * client width will always be taken into account.
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
	 * be painted behind the text. This style is useful when a more discrete
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
	 * By default, a focus box is painted around the title when it receives focus.
	 * If this style is used, the focus box will not be painted.  This style does
	 * not apply when FOCUS_TITLE is used.
	 * @since 3.5
	 */
	public static final int NO_TITLE_FOCUS_BOX = 1 << 14;

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

	/**
	 * Vertical spacing between the title area and the description control
	 * (default is 0). The description control is normally placed at the new
	 * line as defined in the font used to render it. This value will be added
	 * to it.
	 *
	 * @since 3.3
	 */
	public int descriptionVerticalSpacing = 0;

	/**
	 * Horizontal margin around the inside of the title bar area when TITLE_BAR
	 * or SHORT_TITLE_BAR style is used. This variable is not used otherwise.
	 *
	 * @since 3.3
	 */
	public int titleBarTextMarginWidth = 6;

	/**
	 * The toggle widget used to expand the composite.
	 */
	protected ToggleHyperlink toggle;

	/**
	 * The text label for the title.
	 */
	protected Control textLabel;

	/**
	 * @deprecated this variable was left as protected by mistake. It will be
	 *             turned into static and hidden in the future versions. Do not
	 *             use them and do not change its value.
	 */
	@Deprecated
	protected int VGAP = 3;
	/**
	 * @deprecated this variable was left as protected by mistake. It will be
	 *             turned into static and hidden in the future versions. Do not
	 *             use it and do not change its value.
	 */
	@Deprecated
	protected int GAP = 4;

	static final int IGAP = 4;
	static final int IVGAP = 3;

	private static final Point NULL_SIZE = new Point(0, 0);

	private static final int VSPACE = 3;

	private static final int SEPARATOR_HEIGHT = 2;

	private int expansionStyle = TWISTIE | FOCUS_TITLE | EXPANDED;

	private boolean expanded;

	private Control textClient;

	private Control client;

	private ListenerList<IExpansionListener> listeners = new ListenerList<>();

	private Color titleBarForeground;

	private class ExpandableLayout extends Layout implements ILayoutExtension {

		private static final int MIN_WIDTH = -2;

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

		@Override
		protected void layout(Composite parent, boolean changed) {
			initCache(changed);

			Rectangle clientArea = parent.getClientArea();
			int thmargin = 0;
			int tvmargin = 0;

			if (hasTitleBar()) {
				thmargin = titleBarTextMarginWidth;
				tvmargin = IVGAP;
			}
			int x = marginWidth + thmargin;
			int y = marginHeight + tvmargin;
			// toggle
			Point toggleSize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			int width = clientArea.width - marginWidth - marginWidth - thmargin - thmargin;
			if (toggleSize.x > 0)
				width -= toggleSize.x + IGAP;

			// TODO: This code is common between computeSize and layout
			int gapBetweenTcAndLabel = (textClient != null && textLabel != null) ? IGAP : 0;

			int widthForTcAndLabel = Math.max(0, width - gapBetweenTcAndLabel);

			Point tcDefault = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point labelDefault = this.textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			int tcWidthBeforeSplit = Math.min(width, tcDefault.x);
			int labelWidthBeforeSplit = Math.min(width, labelDefault.x);

			int tcWidthAfterSplit = tcWidthBeforeSplit;
			int labelWidthAfterSplit = labelWidthBeforeSplit;

			int expectedWidthForTcAndLabel = tcWidthBeforeSplit + labelWidthBeforeSplit;

			if (expectedWidthForTcAndLabel > widthForTcAndLabel) {
				// this is heuristic since we don't have a reliable way to find
				// out if control can wrap. It checks if width of each label or
				// textClient is less then half
				// and gives them what they asked in this case
				if (labelWidthBeforeSplit < widthForTcAndLabel / 2) {
					labelWidthAfterSplit = labelWidthBeforeSplit;
				} else {
					labelWidthAfterSplit = widthForTcAndLabel * labelWidthBeforeSplit
							/ expectedWidthForTcAndLabel;
				}

				if (tcWidthBeforeSplit < widthForTcAndLabel / 2) {
					tcWidthAfterSplit = tcWidthBeforeSplit;
					labelWidthAfterSplit = widthForTcAndLabel - tcWidthAfterSplit;
				} else {
					tcWidthAfterSplit = widthForTcAndLabel - labelWidthAfterSplit;
				}
			}

			// TODO: Add support for fill alignment of textControl

			Point tcsize = textClientCache.computeSize(tcWidthAfterSplit, SWT.DEFAULT);
			Point size = textLabelCache.computeSize(labelWidthAfterSplit, SWT.DEFAULT);

			int height = Math.max(tcsize.y, size.y); // max of label/text client
			height = Math.max(height, toggleSize.y); // or max of toggle

			boolean leftAlignment = textClient != null && (expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0;
			if (toggle != null) {
				// if label control is absent we vertically center the toggle,
				// because the text client is usually a lot thicker
				int ty = (height - toggleSize.y + 1) / 2 + 1;
				ty = Math.max(ty, 0);
				ty += marginHeight + tvmargin;
				toggle.setLocation(x, ty);
				toggle.setSize(toggleSize);
				x += toggleSize.x + IGAP;
			}
			if (textLabel != null) {
				int ty = y;
				if (leftAlignment) {
					if (size.y < tcsize.y)
						ty = (tcsize.y - size.y) / 2 + marginHeight
								+ tvmargin;
				}
				textLabelCache.setBounds(x, ty, size.x, size.y);
			}

			if (textClient != null) {
				int tcwidth = clientArea.width - marginWidth - marginWidth - thmargin - thmargin;
				if (toggleSize.x > 0)
					tcwidth -= toggleSize.x + IGAP;
				if (size.x > 0)
					tcwidth -= size.x + IGAP;
				tcwidth = Math.min(tcsize.x, tcwidth);
				if (tcwidth < 0)
					tcwidth = 0;
				int tcx;
				if ((expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					tcx = x + ((size.x > 0) ? size.x + IGAP : 0);
				} else {
					tcx = clientArea.width - tcwidth - marginWidth - thmargin;
				}
				textClientCache.setBounds(tcx, y, tcwidth, height);
			}

			y += height;
			if (hasTitleBar())
				y += tvmargin;
			Control separatorControl = getSeparatorControl();
			if (separatorControl != null) {
				y += VSPACE;
				separatorControl.setBounds(marginWidth, y,
						clientArea.width - marginWidth - marginWidth,
						SEPARATOR_HEIGHT);
				y += SEPARATOR_HEIGHT;
			}
			if (expanded && client != null) {
				int areaWidth = clientArea.width - marginWidth - thmargin;
				int cx = marginWidth + thmargin;
				if ((expansionStyle & CLIENT_INDENT) != 0) {
					cx = x;
				}
				areaWidth -= cx;
				Control desc = getDescriptionControl();
				if (desc != null) {
					if (separatorControl != null) {
						y += VSPACE;
					}
					Point dsize = descriptionCache.computeSize(areaWidth, SWT.DEFAULT);
					y += descriptionVerticalSpacing;
					descriptionCache.setBounds(cx, y, areaWidth, dsize.y);
					y += dsize.y;
				}
				y += clientVerticalSpacing;
				int cwidth = areaWidth;
				int cheight = clientArea.height - marginHeight - marginHeight - y;
				clientCache.setBounds(cx, y, cwidth, cheight);
			}
		}

		@Override
		protected Point computeSize(Composite parent, int wHint, int hHint,
				boolean changed) {
			initCache(changed);

			Point toggleSize = NULL_SIZE;
			int toggleWidthPlusGap = 0;
			if (toggle != null) {
				toggleSize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				toggleWidthPlusGap = toggleSize.x + IGAP;
			}
			int thmargin = 0;
			int tvmargin = 0;

			if (hasTitleBar()) {
				thmargin = titleBarTextMarginWidth;
				tvmargin = IVGAP;
			}

			// TODO: This code is common between computeSize and layout
			int gapBetweenTcAndLabel = (textClient != null && textLabel != null) ? IGAP : 0;

			Point tcDefault = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point labelDefault = this.textLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			int width = 0;
			if (wHint == SWT.DEFAULT || wHint == MIN_WIDTH) {
				width += toggleWidthPlusGap;
				width += labelDefault.x;
				width += gapBetweenTcAndLabel;
				width += tcDefault.x;
			} else {
				width = wHint - marginWidth - marginWidth - thmargin - thmargin;
			}

			width = Math.max(0, width);

			int widthForTcAndLabel = Math.max(0, width - gapBetweenTcAndLabel - toggleWidthPlusGap);

			int tcWidthBeforeSplit = Math.min(width, tcDefault.x);
			int labelWidthBeforeSplit = Math.min(width, labelDefault.x);

			int tcWidthAfterSplit = tcWidthBeforeSplit;
			int labelWidthAfterSplit = labelWidthBeforeSplit;

			int expectedWidthForTcAndLabel = tcWidthBeforeSplit + labelWidthBeforeSplit;

			if (expectedWidthForTcAndLabel > widthForTcAndLabel) {
				labelWidthAfterSplit = widthForTcAndLabel * labelWidthBeforeSplit / expectedWidthForTcAndLabel;
				tcWidthAfterSplit = widthForTcAndLabel - labelWidthAfterSplit;
			}

			// TODO: Add support for fill alignment of textControl

			Point tcsize = textClientCache.computeSize(tcWidthAfterSplit, SWT.DEFAULT);
			Point size = textLabelCache.computeSize(labelWidthAfterSplit, SWT.DEFAULT);

			int height = Math.max(tcsize.y, size.y); // max of label/text client
			height = Math.max(height, toggleSize.y); // or max of toggle

			if (getSeparatorControl() != null) {
				height += VSPACE + SEPARATOR_HEIGHT;
			}
			// if (hasTitleBar())
			// height += VSPACE;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				int cwHint = wHint;
				int clientIndent = 0;
				if ((expansionStyle & CLIENT_INDENT) != 0)
					clientIndent = toggleWidthPlusGap;

				if (cwHint != SWT.DEFAULT && cwHint != MIN_WIDTH) {
					cwHint -= marginWidth + marginWidth + thmargin + thmargin;
					if ((expansionStyle & CLIENT_INDENT) != 0)
						if (tcsize.x > 0)
							cwHint -= toggleWidthPlusGap;
				}
				Point dsize = null;
				Point csize;
				if (cwHint == MIN_WIDTH) {
					int minWidth = clientCache.computeMinimumWidth();
					csize = clientCache.computeSize(minWidth, SWT.DEFAULT);
				} else {
					csize = clientCache.computeSize(cwHint, SWT.DEFAULT);
				}
				if (getDescriptionControl() != null) {
					int dwHint = cwHint;
					if (dwHint == SWT.DEFAULT || dwHint == MIN_WIDTH) {
						dwHint = csize.x;
						if ((expansionStyle & CLIENT_INDENT) != 0)
							dwHint -= toggleWidthPlusGap;
					}
					dsize = descriptionCache.computeSize(dwHint, SWT.DEFAULT);
					width = Math.max(width, dsize.x + clientIndent);
					if (expanded) {
						if (getSeparatorControl() != null) {
							height += VSPACE;
						}
						height += descriptionVerticalSpacing + dsize.y;
					}
				}
				width = Math.max(width, csize.x + clientIndent);
				if (expanded) {
					height += clientVerticalSpacing;
					height += csize.y;
				}
			}

			int resultWidth = width + marginWidth + marginWidth + thmargin + thmargin;

			if (wHint != SWT.DEFAULT && wHint != MIN_WIDTH) {
				resultWidth = wHint;
			}

			int resultHeight = height + marginHeight + marginHeight + tvmargin + tvmargin;

			if (hHint != SWT.DEFAULT) {
				resultHeight = hHint;
			}

			Point result = new Point(resultWidth, resultHeight);
			return result;
		}

		@Override
		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeSize(parent, MIN_WIDTH, SWT.DEFAULT, changed).x;
		}

		@Override
		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
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
	 *            the control style (as expected by SWT subclass)
	 * @param expansionStyle
	 *            the style of the expansion widget (TREE_NODE, TWISTIE,
	 *            CLIENT_INDENT, COMPACT, FOCUS_TITLE,
	 *            LEFT_TEXT_CLIENT_ALIGNMENT, NO_TITLE)
	 */
	public ExpandableComposite(Composite parent, int style, int expansionStyle) {
		super(parent, style);
		this.expansionStyle = expansionStyle;
		if ((expansionStyle & TITLE_BAR) != 0)
			setBackgroundMode(SWT.INHERIT_DEFAULT);
		super.setLayout(new ExpandableLayout());
		if (hasTitleBar()) {
			this.addPaintListener(e -> onPaint(e));
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
				@Override
				public void linkActivated(HyperlinkEvent e) {
					toggleState();
				}
			});
			toggle.addPaintListener(e -> {
				if (textLabel instanceof Label && !isFixedStyle())
					textLabel.setForeground(toggle.hover ? toggle.getHoverDecorationColor() : getTitleBarForeground());
			});
			toggle.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.ARROW_UP) {
						verticalMove(false);
						e.doit = false;
					} else if (e.keyCode == SWT.ARROW_DOWN) {
						verticalMove(true);
						e.doit = false;
					}
				}
			});
			if ((getExpansionStyle()&FOCUS_TITLE)==0) {
				toggle.paintFocus=false;
				toggle.addFocusListener(new FocusListener() {
					@Override
					public void focusGained(FocusEvent e) {
						if (textLabel != null) {
						    textLabel.redraw();
						}
					}

					@Override
					public void focusLost(FocusEvent e) {
						if (textLabel != null) {
						    textLabel.redraw();
						}
					}
				});
			}
		}
		if ((expansionStyle & FOCUS_TITLE) != 0) {
			Hyperlink link = new Hyperlink(this, SWT.WRAP);
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					programmaticToggleState();
				}
			});
			textLabel = link;
		} else if ((expansionStyle & NO_TITLE) == 0) {
			final Label label = new Label(this, SWT.WRAP);
			if (!isFixedStyle()) {
				label.setCursor(FormsResources.getHandCursor());
				Listener listener = e -> {
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
							label.setForeground(toggle.getHoverDecorationColor());
							toggle.hover = true;
							toggle.redraw();
						}
						break;
					case SWT.MouseExit:
						if (toggle != null) {
							label.setForeground(getTitleBarForeground());
							toggle.hover = false;
							toggle.redraw();
						}
						break;
					case SWT.Paint:
						if (toggle != null && (getExpansionStyle() & NO_TITLE_FOCUS_BOX) == 0) {
							paintTitleFocus(e.gc);
						}
						break;
					}
				};
				label.addListener(SWT.MouseDown, listener);
				label.addListener(SWT.MouseUp, listener);
				label.addListener(SWT.MouseEnter, listener);
				label.addListener(SWT.MouseExit, listener);
				label.addListener(SWT.Paint, listener);
			}
			textLabel = label;
		}
		if (textLabel != null) {
			textLabel.setMenu(getMenu());
			textLabel.addTraverseListener(e -> {
				if (e.detail == SWT.TRAVERSE_MNEMONIC) {
					// steal the mnemonic
					if (!isVisible() || !isEnabled())
						return;
					if (FormUtil.mnemonicMatch(getText(), e.character)) {
						e.doit = false;
						if (!isFixedStyle()) {
							programmaticToggleState();
						}
						setFocus();
					}
				}
			});
		}
	}

	@Override
	public boolean forceFocus() {
		return false;
	}

	/**
	 * Overrides 'super' to pass the menu to the text label.
	 *
	 * @param menu
	 *            the menu from the parent to attach to this control.
	 */

	@Override
	public void setMenu(Menu menu) {
		if (textLabel != null)
			textLabel.setMenu(menu);
		super.setMenu(menu);
	}

	/**
	 * Prevents assignment of the layout manager - expandable composite uses its
	 * own layout.
	 */
	@Override
	public final void setLayout(Layout layout) {
	}

	/**
	 * Sets the background of all the custom controls in the expandable.
	 */
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if ((getExpansionStyle() & TITLE_BAR) == 0) {
			if (textLabel != null)
				textLabel.setBackground(bg);
			if (toggle != null)
				toggle.setBackground(bg);
		}
	}

	/**
	 * Sets the foreground of all the custom controls in the expandable.
	 */
	@Override
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
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (textLabel != null)
			textLabel.setFont(font);
		if (toggle != null)
			toggle.setFont(font);
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (textLabel != null)
			textLabel.setEnabled(enabled);
		if (toggle != null)
			toggle.setEnabled(enabled);
		super.setEnabled(enabled);
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
	 * @see #getText()
	 */
	public void setText(String title) {
		if (textLabel instanceof Label) {
			((Label) textLabel).setText(title);
		} else if (textLabel instanceof Hyperlink) {
			((Hyperlink) textLabel).setText(title);
		} else {
			return;
		}
		layout();
	}

	@Override
	public void setToolTipText(String string) {
		super.setToolTipText(string);
		// Also set on label, otherwise it's just on the background without text.
		if (textLabel instanceof Label) {
			((Label) textLabel).setToolTipText(string);
		} else if (textLabel instanceof Hyperlink) {
			((Hyperlink) textLabel).setToolTipText(string);
		}
	}

	/**
	 * Returns the title string.
	 *
	 * @return the title string
	 * @see #setText(String)
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
	 * @return the bitwise-ORed style bits for the expansion control
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
			reflow();
		}
	}

	/**
	 * Adds the listener that will be notified when the expansion state changes.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addExpansionListener(IExpansionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the expansion listener.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeExpansionListener(IExpansionListener listener) {
		listeners.remove(listener);
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
	@Override
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

	/**
	 * Returns the difference in height between the text and the text client (if
	 * set). This difference can cause vertical alignment problems when two
	 * expandable composites are placed side by side, one with and one without
	 * the text client. Use this method obtain the value to add to either
	 * <code>descriptionVerticalSpacing</code> (if you have description) or
	 * <code>clientVerticalSpacing</code> to correct the alignment of the
	 * expandable without the text client.
	 *
	 * @return the difference in height between the text and the text client or
	 *         0 if no corrective action is needed.
	 * @since 3.3
	 */
	public int getTextClientHeightDifference() {
		if (textClient == null || textLabel == null)
			return 0;
		int theight = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int tcheight = textClient.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		return Math.max(tcheight - theight, 0);
	}

	/**
	 * Tests if this expandable composite renders a title bar around the text.
	 *
	 * @return <code>true</code> for <code>TITLE_BAR</code> or
	 *         <code>SHORT_TITLE_BAR</code> styles, <code>false</code>
	 *         otherwise.
	 */
	protected boolean hasTitleBar() {
		return (getExpansionStyle() & TITLE_BAR) != 0
				|| (getExpansionStyle() & SHORT_TITLE_BAR) != 0;
	}

	/**
	 * Sets the color of the title bar foreground when TITLE_BAR style is used.
	 *
	 * @param color
	 *            the title bar foreground
	 */
	public void setTitleBarForeground(Color color) {
		titleBarForeground = color;
		if (textLabel != null)
			textLabel.setForeground(color);
	}

	/**
	 * Returns the title bar foreground when TITLE_BAR style is used.
	 *
	 * @return the title bar foreground
	 */
	public Color getTitleBarForeground() {
		return titleBarForeground;
	}

	// end of APIs

	private void toggleState() {
		boolean newState = !isExpanded();
		fireExpanding(newState, true);
		internalSetExpanded(newState);
		fireExpanding(newState, false);
		if (newState)
			FormUtil.ensureVisible(this);
	}

	private void fireExpanding(boolean state, boolean before) {
		int size = listeners.size();
		if (size == 0)
			return;
		ExpansionEvent e = new ExpansionEvent(this, state);
		for (IExpansionListener listener : listeners) {
			if (before)
				listener.expansionStateChanging(e);
			else
				listener.expansionStateChanged(e);
		}
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

	private void paintTitleFocus(GC gc) {
		Point size = textLabel.getSize();
		gc.setBackground(textLabel.getBackground());
		gc.setForeground(textLabel.getForeground());
		if (toggle.isFocusControl())
			gc.drawFocus(0, 0, size.x, size.y);
	}

	void reflow() {
		Composite c = this;
		while (c != null) {
			c.setRedraw(false);
			c = c.getParent();
			if (c instanceof SharedScrolledComposite || c instanceof Shell) {
				break;
			}
		}
		c = this;
		while (c != null) {
			c.requestLayout();
			c = c.getParent();
			if (c instanceof SharedScrolledComposite) {
				((SharedScrolledComposite) c).reflow(true);
				break;
			}
		}
		c = this;
		while (c != null) {
			c.setRedraw(true);
			c = c.getParent();
			if (c instanceof SharedScrolledComposite || c instanceof Shell) {
				break;
			}
		}
	}
}
