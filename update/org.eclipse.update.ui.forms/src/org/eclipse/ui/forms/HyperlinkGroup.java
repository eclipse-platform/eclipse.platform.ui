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
package org.eclipse.ui.forms;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.parts.*;

/**
 * Manages a group of hyperlinks. It tracks
 * activation, updates normal and active colors and
 * updates underline state depending on the underline 
 * preference. Hyperlink labels are added to the group
 * after creation and are automatically removed from
 * the group when they are disposed.
 * 
 * @since 3.0
 */

public class HyperlinkGroup extends HyperlinkSettings {
	private ArrayList links;
	private HyperlinkLabel lastActivated;
	private HyperlinkLabel lastEntered;
	private GroupListener listener;

	private class GroupListener implements Listener, HyperlinkListener {
		public void handleEvent(Event e) {
			switch (e.type) {
				case SWT.MouseDown :
					onMouseDown(e);
					break;
				case SWT.Dispose :
					unhook((HyperlinkLabel) e.widget);
					break;
			}
		}
		public void linkActivated(HyperlinkEvent e) {
		}

		public void linkEntered(HyperlinkEvent e) {
			if (lastEntered != null) {
				linkExited(lastEntered);
			}
			HyperlinkLabel link = (HyperlinkLabel) e.widget;
			link.setCursor(getHyperlinkCursor());
			if (getActiveBackground() != null)
				link.setBackground(getActiveBackground());
			if (getActiveForeground() != null)
				link.setForeground(getActiveForeground());
			if (getHyperlinkUnderlineMode() == UNDERLINE_ROLLOVER)
				link.setUnderlined(true);
			lastEntered = link;
		}

		public void linkExited(HyperlinkEvent e) {
			linkExited((HyperlinkLabel) e.widget);
		}
		private void linkExited(HyperlinkLabel link) {
			link.setCursor(null);
			if (getHyperlinkUnderlineMode() == UNDERLINE_ROLLOVER)
				link.setUnderlined(false);
			if (getBackground() != null)
				link.setBackground(getBackground());
			if (getForeground() != null)
				link.setForeground(getForeground());
			if (lastEntered == link)
				lastEntered = null;
		}
	}
	
	/**
	 * Creates a hyperlink group.
	 */

	public HyperlinkGroup() {
		listener = new GroupListener();
		links = new ArrayList();
	}

	/**
	 * Returns the link that has been active the last, or <code>null</code>
	 * if no link has been active yet or the last active link has been
	 * disposed.
	 * 
	 * @return the last active link or <code>null</code>
	 */
	public HyperlinkLabel getLastActivated() {
		return lastActivated;
	}
	/**
	 * Adds a hyperlink to the group to be jointly managed. Hyperlink will be
	 * managed until it is disposed. Settings like colors, cursors and modes
	 * will affect all managed hyperlinks.
	 * 
	 * @param link
	 */

	public void add(HyperlinkLabel link) {
		if (getBackground() != null)
			link.setBackground(getBackground());
		if (getForeground() != null)
			link.setForeground(getForeground());
		if (getHyperlinkUnderlineMode() == UNDERLINE_ALWAYS)
			link.setUnderlined(true);
		hook(link);
	}

	public void setBackground(Color color) {
		super.setBackground(color);
		for (int i = 0; i < links.size(); i++) {
			HyperlinkLabel label = (HyperlinkLabel) links.get(i);
			label.setBackground(color);
		}
	}

	public void setForeground(Color color) {
		super.setForeground(color);
		for (int i = 0; i < links.size(); i++) {
			HyperlinkLabel label = (HyperlinkLabel) links.get(i);
			label.setForeground(color);
		}
	}

	public void setHyperlinkUnderlineMode(int newHyperlinkUnderlineMode) {
		super.setHyperlinkUnderlineMode(newHyperlinkUnderlineMode);
		for (int i = 0; i < links.size(); i++) {
			HyperlinkLabel label = (HyperlinkLabel) links.get(i);
			label.setUnderlined(newHyperlinkUnderlineMode == UNDERLINE_ALWAYS);
		}
	}

	private void hook(HyperlinkLabel link) {
		link.addListener(SWT.MouseDown, listener);
		link.addHyperlinkListener(listener);
		link.addListener(SWT.Dispose, listener);
	}

	private void unhook(HyperlinkLabel link) {
		link.removeListener(SWT.MouseDown, listener);
		link.removeHyperlinkListener(listener);
		if (lastActivated == link)
			lastActivated = null;
		if (lastEntered == link)
			lastEntered = null;
		links.remove(link);
	}

	private void onMouseDown(Event e) {
		if (e.button == 1)
			return;
		lastActivated = (HyperlinkLabel) e.widget;
	}
}