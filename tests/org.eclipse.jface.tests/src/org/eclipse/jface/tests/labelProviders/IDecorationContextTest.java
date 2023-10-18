/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.tests.labelProviders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.junit.Test;

/**
 * Most of the setup has been taken from
 * org.eclipse.jface.snippets.viewers.Snippet010OwnerDraw.java
 *
 * @since 3.4
 *
 */
public class IDecorationContextTest {

	private static IDecorationContext getDecorationContext() {
		return new IDecorationContext() {

			@Override
			public String[] getProperties() {
				return null;
			}

			@Override
			public Object getProperty(String property) {
				return null;
			}
		};
	}

	private static IStyledLabelProvider getStyledLabelProvider() {
		return new IStyledLabelProvider() {

			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public StyledString getStyledText(Object element) {
				return null;
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
		};
	}

	private static ILabelDecorator getLabelDecorator() {
		return new ILabelDecorator() {

			@Override
			public Image decorateImage(Image image, Object element) {
				return null;
			}

			@Override
			public String decorateText(String text, Object element) {
				return null;
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
		};
	}

	private static DecoratingStyledCellLabelProvider getDecoratingStyledCellLabelProvider(
			boolean nullDecorationContext) {
		return nullDecorationContext
				? new DecoratingStyledCellLabelProvider(getStyledLabelProvider(), getLabelDecorator(), null)
				: new DecoratingStyledCellLabelProvider(getStyledLabelProvider(), getLabelDecorator(),
						getDecorationContext());
	}

	@Test
	public void testDefaultContextIsUsed() {
		// Create a DecoratingStyledCellLabelProvider with a null
		// decorationContext
		assertEquals(getDecoratingStyledCellLabelProvider(true).getDecorationContext(),
				DecorationContext.DEFAULT_CONTEXT);

	}

	@Test
	public void testSetDecorationContextNull() {
		DecoratingStyledCellLabelProvider label = getDecoratingStyledCellLabelProvider(false);
		try {
			label.setDecorationContext(null);
			fail("DecoratingStyledCellLabelProvider.setDecorationContext did not throw an exception when passed null");
		} catch (AssertionFailedException e) {
			// A Good Thing.
		}
	}

}
