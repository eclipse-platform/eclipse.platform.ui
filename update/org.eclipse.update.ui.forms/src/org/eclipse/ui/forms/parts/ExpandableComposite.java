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
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.FormsResources;

/**
 * This composite is capable of expanding or collapsing a single client that is
 * its direct child. The composite renders a twistie in the upper-left corner,
 * followed by a title that also acts as a hyperlink (can be selected and
 * is traversable). The client is layed out below the title when expanded,
 * or hidden when collapsed.
 * 
 * @since 3.0
 */

public class ExpandableComposite extends Composite {
/**
 * If this style is used, twistie will be rendered as an expansion control.
 */
	public static final int TWISTIE = 1;
/**
 * If this style is used, tree-style rectangle with either + or - signs will
 * be used to render the expansion control.
 */
	public static final int TREE = 2;
	private boolean expandable=true;
	private boolean expanded;
	private Control client;
	protected Hyperlink textLabel;

	private class ExpandableLayout extends Layout {
		protected void layout(Composite parent, boolean changed) {
			Rectangle clientArea = parent.getClientArea();
			Point size =
				textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			int x = 0;
			int y = 1;

			if (isExpandable()) {
				x = 8 + 8;
			}
			textLabel.setBounds(x, y, size.x, size.y);

			if (isExpandable())
				y = Math.max(size.y, 8) + 2 + 2;
			else
				y = size.y + 2;
			if (expanded) {
				int areaWidth = clientArea.width - x;
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
			height = size.y + 2 + 2;
			if (expanded && client != null) {
				size = client.computeSize(wHint, SWT.DEFAULT, changed);
				width = Math.max(width, size.x);
				height += size.y;
			}
			if (isExpandable()) {
				height = Math.max(height, 8);
				width += 8 + 8;
			}
			return new Point(width, height);
		}
	}

	/**
	 * Creates the expandable composite in the provided parent.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the control style
	 */
	public ExpandableComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new ExpandableLayout());
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (isExpandable())
					repaint(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				Rectangle box = getBoxBounds(null);
				if (box.contains(e.x, e.y)) {
					setCursor(FormsResources.getBusyCursor());
					setExpanded(!isExpanded());
					setCursor(null);
				}
			}
		});
		textLabel = new Hyperlink(this, SWT.WRAP);
		textLabel.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(Control link) {
				if (isExpandable())
					setExpanded(!isExpanded());
			}
		});
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
	 * By default, the control is expandable. This property can be disabled if
	 * the client should not be reachable for any reason.
	 * 
	 * @param expandable
	 *            the new expandable property
	 */
	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	/**
	 * Tests whether the control can be expanded.
	 * 
	 * @return <samp>true</samp> if the control can be expanded by user
	 *         actions, <samp>false</samp> otherwise.
	 */
	public boolean isExpandable() {
		return expandable;
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

	private void repaint(PaintEvent e) {
		GC gc = e.gc;
		Rectangle box = getBoxBounds(gc);
		gc.setForeground(
			getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawRectangle(box);
		gc.setForeground(
			getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		gc.drawLine(box.x + 2, box.y + 4, box.x + 6, box.y + 4);
		if (!isExpanded()) {
			gc.drawLine(box.x + 4, box.y + 2, box.x + 4, box.y + 6);
		}
	}

	private Rectangle getBoxBounds(GC gc) {
		int x = 0;
		int y = 0;
		boolean noGC = false;

		if (gc == null) {
			gc = new GC(this);
			noGC = true;
		}
		gc.setFont(textLabel.getFont());
		int height = gc.getFontMetrics().getHeight();
		y = height / 2 - 4 + 1;
		y = Math.max(y, 0);
		if (noGC)
			gc.dispose();
		return new Rectangle(x, y, 8, 8);
	}
}