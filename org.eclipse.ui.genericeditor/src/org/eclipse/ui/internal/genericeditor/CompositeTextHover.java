/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.internal.genericeditor.TextHoverRegistry.TextHoverExtension;

/**
 * A text hover that delegates its operations to children
 * provided in constructor and returns the first interesting result.
 *
 * @since 1.0
 */
public class CompositeTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

	private List<TextHoverExtension> hoversToConsider;
	private TextHoverExtension currentHover = null;

	public CompositeTextHover(List<TextHoverExtension> hoversToConsider) {
		Assert.isNotNull(hoversToConsider);
		this.hoversToConsider = hoversToConsider;
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		for (TextHoverExtension hover : this.hoversToConsider) {
			ITextHover delegate = hover.getDelegate();
			Object res = null;
			if (delegate instanceof ITextHoverExtension2) {
				res = ((ITextHoverExtension2)delegate).getHoverInfo2(textViewer, hoverRegion);
			} else {
				res = delegate.getHoverInfo(textViewer, hoverRegion);
			}
			if (res != null) {
				currentHover = hover;
				return res;
			}
		}
		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (this.currentHover != null) {
			ITextHover hover = this.currentHover.getDelegate();
			if (hover instanceof ITextHoverExtension) {
				return ((ITextHoverExtension)hover).getHoverControlCreator();
			}
		}
		return null;
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		for (TextHoverExtension hover : this.hoversToConsider) {
			ITextHover delegate = hover.getDelegate();
			String res = delegate.getHoverInfo(textViewer, hoverRegion);
			if (res != null) {
				currentHover = hover;
				return res;
			}
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		for (TextHoverExtension hover : this.hoversToConsider) {
			ITextHover delegate = hover.getDelegate();
			IRegion res = delegate.getHoverRegion(textViewer, offset);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

}
