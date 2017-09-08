/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
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

/**
 * A text hover that delegates its operations to children
 * provided in constructor and returns the first interesting result.
 *
 * @since 1.0
 */
public class CompositeTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

	private List<ITextHover> hoversToConsider;
	private ITextHover currentHover = null;

	public CompositeTextHover(List<ITextHover> hoversToConsider) {
		Assert.isNotNull(hoversToConsider);
		this.hoversToConsider = hoversToConsider;
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		for (ITextHover hover : this.hoversToConsider) {
			Object res = null;
			if (hover instanceof ITextHoverExtension2) {
				res = ((ITextHoverExtension2)hover).getHoverInfo2(textViewer, hoverRegion);
			} else {
				res = hover.getHoverInfo(textViewer, hoverRegion);
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
		ITextHover hover = this.currentHover;
		if (hover != null) {
			if (hover instanceof ITextHoverExtension) {
				return ((ITextHoverExtension)hover).getHoverControlCreator();
			}
		}
		return null;
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		for (ITextHover hover : this.hoversToConsider) {
			String res = hover.getHoverInfo(textViewer, hoverRegion);
			if (res != null) {
				currentHover = hover;
				return res;
			}
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		for (ITextHover hover : this.hoversToConsider) {
			IRegion res = hover.getHoverRegion(textViewer, offset);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

}
