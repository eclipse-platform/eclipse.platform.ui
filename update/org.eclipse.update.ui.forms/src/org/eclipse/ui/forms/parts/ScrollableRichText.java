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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.forms.FormWidgetFactory;

public class ScrollableRichText {
	private ScrolledComposite scomp;
	private RichText richText;
	private String text;
	private FormWidgetFactory factory;
	private int style;

	public ScrollableRichText(int style) {
		this.style = style;
	}

	public void createControl(Composite parent) {
		factory = new FormWidgetFactory(parent.getDisplay());
		scomp = new ScrolledComposite(parent, style);
		scomp.setBackground(factory.getBackgroundColor());
		richText = factory.createRichText(scomp, false);
		richText.marginWidth = 2;
		richText.marginHeight = 2;
		richText.setHyperlinkSettings(factory.getHyperlinkHandler());
		scomp.setContent(richText);
		scomp.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				updateSize();
			}
		});
		richText.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (factory != null) {
					factory.dispose();
					factory = null;
				}
			}
		});
		if (text != null)
			loadText(text);
	}

	public Control getControl() {
		return scomp;
	}

	public void setText(String text) {
		this.text = text;
		loadText(text);
	}

	private void loadText(String text) {
		if (richText != null) {
			String markup = text;
			if (!markup.startsWith("<form>"))
				markup = "<form>" + text + "</form>";
			richText.setText(markup, true, false);
			updateSize();
			richText.redraw();
			scomp.layout();
		}
	}
	private void updateSize() {
		Rectangle ssize = scomp.getClientArea();
		int swidth = ssize.width;
		ScrollBar vbar = scomp.getVerticalBar();
		if (vbar != null) {
			swidth -= vbar.getSize().x;
		}
		Point size = richText.computeSize(swidth, SWT.DEFAULT, true);
		richText.setSize(size);
	}
	public void dispose() {
		factory.dispose();
	}
}