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
import java.util.Vector;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;

/**
 * This composite is capable of expanding or collapsing a single client that is
 * its direct child. The composite renders a twistie in the upper-left corner,
 * followed by a title that also acts as a hyperlink (can be selected and is
 * traversable). The client is layed out below the title when expanded, or
 * hidden when collapsed.
 * 
 * @since 3.0
 */

public class ExpandableComposite extends Composite {
	/**
	 *  
	 */
	public static final int NONE = 0;
	/**
	 * If this style is used, twistie will be rendered as an expansion control.
	 */
	public static final int TWISTIE = 1;
	/**
	 * If this style is used, tree-style rectangle with either + or - signs
	 * will be used to render the expansion control.
	 */
	public static final int TREE = 2;
	private int GAP = 5;
	private int expansionStyle = TWISTIE;
	private boolean expanded;
	private Control client;
	private Vector listeners;
	protected ToggleHyperlink toggle;
	protected Hyperlink textLabel;

	private class ExpandableLayout extends Layout {
		protected void layout(Composite parent, boolean changed) {
			Rectangle clientArea = parent.getClientArea();
			Point size =
				textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			int x = 0;
			int y = 0;

			if (toggle != null) {
				Point tsize =
					toggle.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
				toggle.setLocation(x, size.y / 2 - tsize.y / 2);
				toggle.setSize(tsize);
				x += tsize.x + GAP;
			}
			textLabel.setBounds(x, y, size.x, size.y);

			if (expanded) {
				int areaWidth = clientArea.width - x;
				y += size.y;
				if (client != null) {
					size = client.computeSize(areaWidth, SWT.DEFAULT, changed);
					client.setBounds(x, y, size.x, size.y);
				}
			}
		}

		protected Point computeSize(
			Composite parent,
			int wHint,
			int hHint,
			boolean changed) {
			int width = 0, height = 0;
			Point size =
				textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			width = size.x;
			height = size.y;

			if (expanded && client != null) {
				Point csize = client.computeSize(wHint, SWT.DEFAULT, changed);
				width = Math.max(width, csize.x);
				height += csize.y;
			}
			if (toggle != null) {
				Point tsize =
					toggle.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
				height = height - size.y + Math.max(size.y, tsize.y);
				width += tsize.x + GAP;
			}
			return new Point(width, height);
		}
	}

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
	 *            the style of the expansion widget (TREE or TWISTIE)
	 */
	public ExpandableComposite(
		Composite parent,
		int style,
		int expansionStyle) {
		super(parent, style);
		setLayout(new ExpandableLayout());
		listeners = new Vector();
		if (expansionStyle == TWISTIE)
			toggle = new Twistie(this, SWT.NULL);
		if (toggle != null) {
			toggle.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					toggleState();
				}
			});
		}
		textLabel = new Hyperlink(this, SWT.WRAP);
		textLabel.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				if (getExpansionStyle() != NONE) {
					toggle.setExpanded(!toggle.isExpanded());
					toggleState();
				}
			}
		});
	}

	public void setBackground(Color bg) {
		super.setBackground(bg);
		textLabel.setBackground(bg);
		if (toggle != null)
			toggle.setBackground(bg);
	}

	public void setForeground(Color fg) {
		super.setForeground(fg);
		textLabel.setForeground(fg);
		if (toggle != null)
			toggle.setForeground(fg);
	}
	public void setFont(Font font) {
		super.setFont(font);
		textLabel.setFont(font);
		if (toggle != null)
			toggle.setFont(font);
	}
	/**
	 * Sets the client of this expandable composite. The client must not be
	 * <samp>null</samp> and must be a direct child of this container.
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
	public void setTitle(String title) {
		textLabel.setText(title);
	}
	/**
	 * Returns the title string.
	 * 
	 * @return the title string
	 * @see #setTitle
	 */
	public String getTitle() {
		return textLabel.getText();
	}

	/**
	 * Tests the expanded state of the composite.
	 * 
	 * @return <samp>true</samp> if expanded, <samp>false</samp> if
	 *         collapsed.
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
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
		if (this.expanded != expanded) {
			this.expanded = expanded;
			if (client != null)
				client.setVisible(expanded);
			layout();
		}
	}

	public void addExpansionListener(ExpansionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	public void removeExpansionListener(ExpansionListener listener) {
		listeners.remove(listener);
	}
	private void toggleState() {
		boolean newState = !isExpanded();
		fireExpanding(newState, true);
		setExpanded(!isExpanded());
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
}