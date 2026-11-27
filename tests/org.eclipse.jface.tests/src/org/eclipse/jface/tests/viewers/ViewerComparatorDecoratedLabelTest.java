/*******************************************************************************
 * Copyright (c) 2026 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Philipp Salzgeber (Bachmann electronic GmbH) - initial API and implementation, Issue #4516
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.junit.jupiter.api.Test;

/**
 * Decorations are computed in a background job and may change the decorated
 * label while a sort runs, so {@link ViewerComparator} must sort by the
 * undecorated label (Bug 364735, Issue #4516).
 */
public class ViewerComparatorDecoratedLabelTest {

	/**
	 * Decorates like a SCM decorator: the element "b" gets a dirty prefix that
	 * sorts before letters, "a" stays undecorated.
	 */
	private static final class PrefixDecorator extends LabelDecorator {
		@Override
		public String decorateText(String text, Object element) {
			return "b".equals(element) ? "> " + text + " [branch]" : text;
		}

		@Override
		public String decorateText(String text, Object element, IDecorationContext context) {
			return decorateText(text, element);
		}

		@Override
		public Image decorateImage(Image image, Object element) {
			return image;
		}

		@Override
		public Image decorateImage(Image image, Object element, IDecorationContext context) {
			return image;
		}

		@Override
		public boolean prepareDecoration(Object element, String originalText, IDecorationContext context) {
			return true;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}

	private static final class StyledLabelProvider extends LabelProvider implements IStyledLabelProvider {
		@Override
		public StyledString getStyledText(Object element) {
			return new StyledString(String.valueOf(element));
		}
	}

	/**
	 * Shaped like the Common Navigator's label provider: a styled decorating
	 * provider that is also an {@link ILabelProvider} returning the decorated
	 * text.
	 */
	private static final class NavigatorLikeLabelProvider extends DecoratingStyledCellLabelProvider
			implements ILabelProvider {
		NavigatorLikeLabelProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator) {
			super(labelProvider, decorator, null);
		}

		@Override
		public String getText(Object element) {
			return getStyledText(element).getString();
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}

	/** A content viewer without widgets; only its label provider matters here. */
	private static final class LabelProviderViewer extends ContentViewer {
		@Override
		public Control getControl() {
			return null;
		}

		@Override
		public ISelection getSelection() {
			return null;
		}

		@Override
		public void refresh() {
		}

		@Override
		public void setSelection(ISelection selection, boolean reveal) {
		}
	}

	private final ViewerComparator comparator = new ViewerComparator();

	@Test
	public void styledDecorationsAreIgnoredWhenSorting() {
		LabelProviderViewer viewer = new LabelProviderViewer();
		viewer.setLabelProvider(new NavigatorLikeLabelProvider(new StyledLabelProvider(), new PrefixDecorator()));

		assertTrue(comparator.compare(viewer, "b", "a") > 0,
				"b must sort after a although its decorated label starts with '>'");
		assertEquals(0, comparator.compare(viewer, "a", "a"));
	}

	@Test
	public void plainDecorationsAreIgnoredWhenSorting() {
		LabelProviderViewer viewer = new LabelProviderViewer();
		viewer.setLabelProvider(new DecoratingLabelProvider(new LabelProvider(), new PrefixDecorator()));

		assertTrue(comparator.compare(viewer, "b", "a") > 0,
				"b must sort after a although its decorated label starts with '>'");
	}
}
