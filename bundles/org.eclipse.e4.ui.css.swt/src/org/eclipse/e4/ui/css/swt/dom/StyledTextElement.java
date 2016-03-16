/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.dom.scrollbar.StyledTextThemedScrollBarAdapter;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.log.LogService;

public class StyledTextElement extends CompositeElement {

	public StyledTextElement(Composite composite, CSSEngine engine) {
		super(composite, engine);
	}

	public StyledText getStyledText() {
		return (StyledText) getControl();
	}

	private StyledTextThemedScrollBarAdapter getScrollbarAdapter() {
		return StyledTextThemedScrollBarAdapter.getScrollbarAdapter(getStyledText());
	}

	public void setScrollBarBackgroundColor(Color newColor) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarBackgroundColor(newColor);
		}
	}

	public void setScrollBarForegroundColor(Color newColor) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarForegroundColor(newColor);
		}
	}

	public void setScrollBarWidth(int width) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarWidth(width);
		}
	}

	public void setMouseNearScrollScrollBarWidth(int width) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setMouseNearScrollScrollBarWidth(width);
		}
	}

	public void setVerticalScrollBarVisible(boolean visible) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setVerticalScrollBarVisible(visible);
		}
	}

	public void setHorizontalScrollBarVisible(boolean visible) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setHorizontalScrollBarVisible(visible);
		}
	}

	public void setScrollBarBorderRadius(int radius) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarBorderRadius(radius);
		}
	}

	private void setScrollBarThemed(boolean themed) {
		StyledTextThemedScrollBarAdapter scrollbarAdapter = getScrollbarAdapter();
		if (scrollbarAdapter != null) {
			scrollbarAdapter.setScrollBarThemed(themed);
		}
	}

	@Override
	public void reset() {
		super.reset();
		// Default is not having the scroll bar themed.
		setScrollBarThemed(false);
	}

	/**
	 * @param cssText
	 *            either "true" or "false"
	 *
	 *            Note that the user may also set
	 *            -Dswt.enable.themedScrollBar=true/false and force it to
	 *            true/false regardless of the CSS value.
	 */
	public void setScrollBarThemed(String cssText) {
		String value = System.getProperty("swt.enable.themedScrollBar"); //$NON-NLS-1$
		if (value != null) {
			if ("true".equalsIgnoreCase(value)) {
				setScrollBarThemed(true);
			} else {
				setScrollBarThemed(false);
			}

		} else if ("true".equalsIgnoreCase(cssText)) { //$NON-NLS-1$
			setScrollBarThemed(true);

		} else if ("false".equalsIgnoreCase(cssText)) { //$NON-NLS-1$
			setScrollBarThemed(false);

		} else {
			CSSActivator.getDefault().log(LogService.LOG_WARNING,
					"Don't know how to handle setting value: " + cssText //$NON-NLS-1$
					+ " (supported: boolean or preference:bundle.qualifier.id/key)."); //$NON-NLS-1$
		}

	}


}
