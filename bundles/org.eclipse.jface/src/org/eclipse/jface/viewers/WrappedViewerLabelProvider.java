/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * The WrappedViewerLabelProvider is a label provider that allows
 * {@link ILabelProvider}, {@link IColorProvider} and {@link IFontProvider} to
 * be mapped to a ColumnLabelProvider.
 *
 * @since 3.3
 */
class WrappedViewerLabelProvider extends ColumnLabelProvider {

	private static ILabelProvider defaultLabelProvider = new LabelProvider();

	private ILabelProvider labelProvider = defaultLabelProvider;

	private IColorProvider colorProvider;

	private IFontProvider fontProvider;

	private IViewerLabelProvider viewerLabelProvider;

	private ITreePathLabelProvider treePathLabelProvider;

	/**
	 * Create a new instance of the receiver based on labelProvider.
	 */
	public WrappedViewerLabelProvider(IBaseLabelProvider labelProvider) {
		super();
		setProviders(labelProvider);
	}

	/**
	 * Set the any providers for the receiver that can be adapted from provider.
	 *
	 * @param provider
	 *            {@link Object}
	 */
	public void setProviders(Object provider) {
		if (provider instanceof ITreePathLabelProvider)
			treePathLabelProvider = ((ITreePathLabelProvider) provider);

		if (provider instanceof IViewerLabelProvider)
			viewerLabelProvider = ((IViewerLabelProvider) provider);

		if (provider instanceof ILabelProvider)
			labelProvider = ((ILabelProvider) provider);

		if (provider instanceof IColorProvider)
			colorProvider = (IColorProvider) provider;

		if (provider instanceof IFontProvider)
			fontProvider = (IFontProvider) provider;

	}

	@Override
	public Font getFont(Object element) {
		if (fontProvider == null) {
			return null;
		}

		return fontProvider.getFont(element);

	}

	@Override
	public Color getBackground(Object element) {
		if (colorProvider == null) {
			return null;
		}

		return colorProvider.getBackground(element);
	}

	@Override
	public String getText(Object element) {
		return getLabelProvider().getText(element);
	}

	@Override
	public Image getImage(Object element) {
		return getLabelProvider().getImage(element);
	}

	@Override
	public Color getForeground(Object element) {
		if (colorProvider == null) {
			return null;
		}

		return colorProvider.getForeground(element);
	}

	/**
	 * Get the label provider
	 *
	 * @return {@link ILabelProvider}
	 */
	ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Get the color provider
	 *
	 * @return {@link IColorProvider}
	 */
	IColorProvider getColorProvider() {
		return colorProvider;
	}

	/**
	 * Get the font provider
	 *
	 * @return {@link IFontProvider}.
	 */
	IFontProvider getFontProvider() {
		return fontProvider;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		if(viewerLabelProvider == null && treePathLabelProvider == null){
			// inlined super implementation with performance optimizations
			cell.setText(getText(element));
			Image image = getImage(element);
			cell.setImage(image);
			if (colorProvider != null) {
				cell.setBackground(getBackground(element));
				cell.setForeground(getForeground(element));
			}
			if (fontProvider != null) {
				cell.setFont(getFont(element));
			}
			return;
		}

		ViewerLabel label = new ViewerLabel(cell.getText(), cell.getImage());

		if (treePathLabelProvider != null) {
			TreePath treePath = cell.getViewerRow().getTreePath();

			Assert.isNotNull(treePath);
			treePathLabelProvider.updateLabel(label, treePath);
		} else if (viewerLabelProvider != null) {
			viewerLabelProvider.updateLabel(label, element);
		}
		if (!label.hasNewForeground() && colorProvider != null)
			label.setForeground(getForeground(element));

		if (!label.hasNewBackground() && colorProvider != null)
			label.setBackground(getBackground(element));

		if (!label.hasNewFont() && fontProvider != null)
			label.setFont(getFont(element));

		applyViewerLabel(cell, label);
	}

	private void applyViewerLabel(ViewerCell cell, ViewerLabel label) {
		if (label.hasNewText()) {
			cell.setText(label.getText());
		}
		if (label.hasNewImage()) {
			cell.setImage(label.getImage());
		}
		if (colorProvider!= null || label.hasNewBackground()) {
			cell.setBackground(label.getBackground());
		}
		if (colorProvider!= null || label.hasNewForeground()) {
			cell.setForeground(label.getForeground());
		}
		if (fontProvider!= null || label.hasNewFont()) {
			cell.setFont(label.getFont());
		}
	}
}
