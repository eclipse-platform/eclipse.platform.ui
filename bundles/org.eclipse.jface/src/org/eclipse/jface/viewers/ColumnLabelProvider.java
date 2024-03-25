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

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * The ColumnLabelProvider is the label provider for viewers
 * that have column support such as {@link TreeViewer} and
 * {@link TableViewer}
 *
 * <p><b>This classes is intended to be subclassed</b></p>
 *
 * @since 3.3
 */
public class ColumnLabelProvider extends CellLabelProvider implements
		IFontProvider, IColorProvider, ILabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		cell.setText(getText(element));
		Image image = getImage(element);
		cell.setImage(image);
		cell.setBackground(getBackground(element));
		cell.setForeground(getForeground(element));
		cell.setFont(getFont(element));

	}

	@Override
	public Font getFont(Object element) {
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		return null;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		return element == null ? "" : element.toString();//$NON-NLS-1$
	}

	/**
	 * Creates a {@link ColumnLabelProvider} which implements the {@link #getText}
	 * method by calling the argument function.
	 *
	 * @param textFunction the function which returns the text
	 * @return The new ColumnLabelProvider
	 * @since 3.19
	 */
	public static ColumnLabelProvider createTextProvider(Function<Object, String> textFunction) {
		Objects.requireNonNull(textFunction);
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				return textFunction.apply(e);
			}
		};
	}

	/**
	 * Creates a {@link ColumnLabelProvider} which implements the {@link #getImage}
	 * method by calling the argument function.
	 *
	 * @param imageFunction the function which returns the image
	 * @return The new ColumnLabelProvider
	 * @since 3.19
	 */
	public static ColumnLabelProvider createImageProvider(Function<Object, Image> imageFunction) {
		Objects.requireNonNull(imageFunction);
		return new ColumnLabelProvider() {
			@Override
			public Image getImage(Object e) {
				return imageFunction.apply(e);
			}
		};
	}

	/**
	 * Creates a {@link ColumnLabelProvider} which implements both the
	 * {@link #getText} and {@link #getImage} methods by calling the argument
	 * functions.
	 *
	 * @param textFunction  the function which returns the text
	 * @param imageFunction the function which returns the image
	 * @return The new ColumnLabelProvider
	 * @since 3.19
	 */
	public static ColumnLabelProvider createTextImageProvider(Function<Object, String> textFunction,
			Function<Object, Image> imageFunction) {
		Objects.requireNonNull(textFunction);
		Objects.requireNonNull(imageFunction);
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object e) {
				return textFunction.apply(e);
			}
			@Override
			public Image getImage(Object e) {
				return imageFunction.apply(e);
			}
		};
	}

}
