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
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.internal.forms.Messages;

/**
 * A custom selectable control that can be used to control areas that can be
 * expanded or collapsed.
 * <p>
 * This is an abstract class. Subclasses are responsible for rendering the
 * control using decoration and hover decoration color. Control should be
 * rendered based on the current expansion state.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>None</dd>
 * </dl>
 *
 * @since 3.0
 */
public abstract class ToggleHyperlink extends AbstractHyperlink {
	protected int innerWidth;
	protected int innerHeight;
	protected boolean hover;
	private boolean expanded;
	private Color decorationColor;
	private Color hoverColor;
	/**
	 * Creates a control in a provided composite.
	 *
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */
	public ToggleHyperlink(Composite parent, int style) {
		super(parent, style);
		Listener listener = e -> {
			switch (e.type) {
			case SWT.MouseEnter:
				hover = true;
				redraw();
				break;
			case SWT.MouseExit:
				hover = false;
				redraw();
				break;
			case SWT.KeyDown:
				onKeyDown(e);
				break;
			}
		};
		addListener(SWT.MouseEnter, listener);
		addListener(SWT.MouseExit, listener);
		addListener(SWT.KeyDown, listener);
		addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				setExpanded(!isExpanded());
			}
		});
		initAccessible();
	}

	/**
	 * Sets the color of the decoration.
	 *
	 * @param decorationColor color to set
	 */
	public void setDecorationColor(Color decorationColor) {
		this.decorationColor = decorationColor;
	}
	/**
	 * Returns the color of the decoration.
	 *
	 * @return decoration color
	 */
	public Color getDecorationColor() {
		return decorationColor;
	}
	/**
	 * Sets the hover color of decoration. Hover color will be used when mouse
	 * enters the decoration area.
	 *
	 * @param hoverColor
	 *            the hover color to use
	 */
	public void setHoverDecorationColor(Color hoverColor) {
		this.hoverColor = hoverColor;
	}
	/**
	 * Returns the hover color of the decoration.
	 *
	 * @return the hover color of the decoration.
	 * @since 3.1
	 */
	public Color getHoverDecorationColor() {
		return hoverColor;
	}

	/**
	 * Returns the hover color of the decoration.
	 *
	 * @return the hover color of the decoration.
	 * @deprecated use <code>getHoverDecorationColor</code>
	 * @see #getHoverDecorationColor()
	 */
	@Deprecated
	public Color geHoverDecorationColor() {
		return hoverColor;
	}
	/**
	 * Computes the size of the control.
	 *
	 * @param wHint
	 *            width hint
	 * @param hHint
	 *            height hint
	 * @param changed
	 *            if true, flush any saved layout state
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width = innerWidth + 2 * marginWidth;
		int height = innerHeight + 2 * marginHeight;
		if (wHint != SWT.DEFAULT) {
			width = wHint;
		}
		if (hHint != SWT.DEFAULT) {
			height = hHint;
		}

		Rectangle trim = computeTrim(0, 0, width, height);
		return new Point(trim.width, trim.height);
	}
	/**
	 * Returns the expansion state of the toggle control. When toggle is in the
	 * normal (downward) state, the value is <samp>true </samp>. Collapsed
	 * control will return <samp>false </samp>.
	 *
	 * @return <samp>false </samp> if collapsed, <samp>true </samp> otherwise.
	 */
	public boolean isExpanded() {
		return expanded;
	}
	/**
	 * Sets the expansion state of the twistie control
	 *
	 * @param expanded the expansion state
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		redraw();
	}
	private void initAccessible() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
			@Override
			public void getName(AccessibleEvent e) {
				e.result = Messages.ToggleHyperlink_accessibleName;
			}
			@Override
			public void getDescription(AccessibleEvent e) {
				getName(e);
			}
		});
		getAccessible().addAccessibleControlListener(
				new AccessibleControlAdapter() {
					@Override
					public void getChildAtPoint(AccessibleControlEvent e) {
						Point testPoint = toControl(new Point(e.x, e.y));
						if (getBounds().contains(testPoint)) {
							e.childID = ACC.CHILDID_SELF;
						}
					}
					@Override
					public void getLocation(AccessibleControlEvent e) {
						Rectangle location = getBounds();
						Point pt = toDisplay(new Point(location.x, location.y));
						e.x = pt.x;
						e.y = pt.y;
						e.width = location.width;
						e.height = location.height;
					}
					@Override
					public void getSelection (AccessibleControlEvent e) {
						if (ToggleHyperlink.this.getSelection()) {
							e.childID = ACC.CHILDID_SELF;
						}
					}

					@Override
					public void getFocus (AccessibleControlEvent e) {
						if (ToggleHyperlink.this.getSelection()) {
							e.childID = ACC.CHILDID_SELF;
						}
					}
					@Override
					public void getChildCount(AccessibleControlEvent e) {
						e.detail = 0;
					}
					@Override
					public void getRole(AccessibleControlEvent e) {
						e.detail = ACC.ROLE_TREE;
					}
					@Override
					public void getState(AccessibleControlEvent e) {
						e.detail = ToggleHyperlink.this.isExpanded()
								? ACC.STATE_EXPANDED
								: ACC.STATE_COLLAPSED;
					}
					@Override
					public void getValue(AccessibleControlEvent e) {
						if (e.childID == ACC.CHILDID_SELF) {
							String name = Messages.ToggleHyperlink_accessibleName;
							if (getParent() instanceof ExpandableComposite) {
								name = Messages.ToggleHyperlink_accessibleColumn+((ExpandableComposite)getParent()).getText();
								int index = name.indexOf('&');
								if (index != -1) {
									name = name.substring(0, index) + name.substring(index + 1);
								}
							}
							e.result = name;
						}
					}
				});
	}
	// set bogus childIDs on link activation to ensure state is read on expand/collapse
	@Override
	void triggerAccessible() {
		getAccessible().setFocus(getAccessibleChildID());
	}
	private int getAccessibleChildID() {
		return ToggleHyperlink.this.isExpanded() ? 1 : 2;
	}

	private void onKeyDown(Event e) {
		if (e.keyCode==SWT.ARROW_RIGHT) {
			// expand if collapsed
			if (!isExpanded()) {
				handleActivate(e);
			}
			e.doit=false;
		}
		else if (e.keyCode==SWT.ARROW_LEFT) {
			// collapse if expanded
			if (isExpanded()) {
				handleActivate(e);
			}
			e.doit=false;
		}
	}
}
