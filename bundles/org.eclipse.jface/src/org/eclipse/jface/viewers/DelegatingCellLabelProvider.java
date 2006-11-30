/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * A cell label provider that delegates all calls to another cell label
 * provider. Disposing of this label provider will not call dispose() on the
 * underlying label provider. This class is used to wrap label providers that
 * are set using ColumnViewer.setLabelProvider() instead of using
 * ViewerColumn.setLabelProvider().
 * 
 * @since 3.3
 * 
 */
/* package */class DelegatingCellLabelProvider extends CellLabelProvider {

	CellLabelProvider delegate;

	/**
	 * @param delegate
	 */
	public DelegatingCellLabelProvider(CellLabelProvider delegate) {
		this.delegate = delegate;
	}

	public void addListener(ILabelProviderListener listener) {
		delegate.addListener(listener);
	}

	public void dispose() {
		// don't do anything here
	}

	public Color getToolTipBackgroundColor(Object object) {
		return delegate.getToolTipBackgroundColor(object);
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return delegate.getToolTipDisplayDelayTime(object);
	}

	public Font getToolTipFont(Object object) {
		return delegate.getToolTipFont(object);
	}

	public Color getToolTipForegroundColor(Object object) {
		return delegate.getToolTipForegroundColor(object);
	}

	public Image getToolTipImage(Object object) {
		return delegate.getToolTipImage(object);
	}

	public Point getToolTipShift(Object object) {
		return delegate.getToolTipShift(object);
	}

	public int getToolTipStyle(Object object) {
		return delegate.getToolTipStyle(object);
	}

	public String getToolTipText(Object element) {
		return delegate.getToolTipText(element);
	}

	public int getToolTipTimeDisplayed(Object object) {
		return delegate.getToolTipTimeDisplayed(object);
	}

	public boolean isLabelProperty(Object element, String property) {
		return delegate.isLabelProperty(element, property);
	}

	public void removeListener(ILabelProviderListener listener) {
		delegate.removeListener(listener);
	}

	public void update(ViewerCell cell) {
		delegate.update(cell);
	}

	public boolean useNativeToolTip(Object object) {
		return delegate.useNativeToolTip(object);
	}

}
