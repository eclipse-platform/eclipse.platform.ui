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
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
/**
 * A custom selectable control that can be used to control areas that can be
 * expanded or collapsed.
 * <p>
 * This is an abstract class. Subclasses are responsible for rendering the
 * control using decoration and hover decoration color. Control should be
 * rendered based on the current expansion state.
 * 
 * @since 3.0
 */
public abstract class ToggleHyperlink extends AbstractHyperlink {
	protected int innerWidth;
	protected int innerHeight;
	private boolean expanded;
	protected boolean hover;
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
		addListener(SWT.MouseEnter, new Listener() {
			public void handleEvent(Event e) {
				hover = true;
				redraw();
			}
		});
		addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event e) {
				hover = false;
				redraw();
			}
		});
		addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				setExpanded(!isExpanded());
			}
		});
		initAccessible();
	}
	/**
	 * Sets the color of the decoration.
	 * 
	 * @param decorationColor
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
	 */
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
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width, height;
		if (wHint != SWT.DEFAULT)
			width = wHint;
		else
			width = innerWidth + 2 * marginWidth;
		if (hHint != SWT.DEFAULT)
			height = hHint;
		else
			height = innerHeight + 2 * marginHeight;
		return new Point(width, height);
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
	 * @param selection
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		redraw();
	}
	private void initAccessible() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});
		getAccessible().addAccessibleControlListener(
				new AccessibleControlAdapter() {
					public void getChildAtPoint(AccessibleControlEvent e) {
						Point testPoint = toControl(new Point(e.x, e.y));
						if (getBounds().contains(testPoint)) {
							e.childID = ACC.CHILDID_SELF;
						}
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
						e.detail = ACC.ROLE_TREE;
					}
					public void getState(AccessibleControlEvent e) {
						e.detail = ToggleHyperlink.this.isExpanded()
								? ACC.STATE_EXPANDED
								: ACC.STATE_COLLAPSED;
					}
					public void getValue(AccessibleControlEvent e) {
						e.result = ToggleHyperlink.this.isExpanded()
								? "1"
								: "0";
					}
				});
	}
}