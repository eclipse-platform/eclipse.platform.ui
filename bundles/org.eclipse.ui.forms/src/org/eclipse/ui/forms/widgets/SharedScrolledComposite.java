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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.forms.widgets.*;
/**
 * This class is used to provide common scrolling services to a number
 * of controls in the toolkit. Classes that extend it are not
 * required to implement any method. 
 * 
 * @since 3.0
 */
public abstract class SharedScrolledComposite extends ScrolledComposite {
	private static final int H_SCROLL_INCREMENT = 5;
	private static final int V_SCROLL_INCREMENT = 64;
	/**
	 *  Creates the new instance.
	 * @param parent the parent composite
	 * @param style the style to use
	 */
	public SharedScrolledComposite(Composite parent, int style) {
		super(parent, style);
		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				reflow(true);
			}
		});
		initializeScrollBars();
	}
	/**
	 * Sets the foreground of the control and its content.
	 * 
	 * @param fg
	 *            the new foreground color
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (getContent()!=null)
			getContent().setForeground(fg);
	}
	/**
	 * Sets the background of the control and its content.
	 * 
	 * @param bg
	 *            the new background color
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (getContent()!=null)
			getContent().setBackground(bg);
	}
	/**
	 * Sets the font of the form. This font will be used to render the title
	 * text. It will not affect the body.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (getContent()!=null)
			getContent().setFont(font);
	}
	/**
	 * Overrides 'super' to pass the proper colors and font
	 */
	public void setContent(Control content) {
		super.setContent(content);
		if (content!=null) {
			content.setForeground(getForeground());
			content.setBackground(getBackground());
			content.setFont(getFont());
		}
	}
	/**
	 * If content is set, transfers focus to the content.
	 */
	public boolean setFocus() {
		if (getContent() != null)
			return getContent().setFocus();
		else
			return super.setFocus();
	}
	/**
	 * Recomputes the body layout and the scroll bars. The method should be
	 * used when changes somewhere in the form body invalidate the current
	 * layout and/or scroll bars.
	 * 
	 * @param flushCache if <code>true</code>, drop the cached data
	 */
	public void reflow(boolean flushCache) {
		Composite c = (Composite) getContent();
		Rectangle clientArea = getClientArea();
		if (c == null)
			return;
		c.layout(flushCache);
		Point newSize = c.computeSize(FormUtil
				.getWidthHint(clientArea.width, c), FormUtil.getHeightHint(
				clientArea.height, c), flushCache);
		c.setSize(newSize);
		setMinSize(newSize);
		FormUtil.updatePageIncrement(this);
	}
	private void initializeScrollBars() {
		ScrollBar hbar = getHorizontalBar();
		if (hbar != null) {
			hbar.setIncrement(H_SCROLL_INCREMENT);
		}
		ScrollBar vbar = getVerticalBar();
		if (vbar != null) {
			vbar.setIncrement(V_SCROLL_INCREMENT);
		}
		FormUtil.updatePageIncrement(this);
	}
}
