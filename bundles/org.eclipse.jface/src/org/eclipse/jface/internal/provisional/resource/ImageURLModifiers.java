/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.provisional.resource;

/**
 * Holds the {@link IImageURLModifier} consulted when an image is loaded from a
 * URL.
 */
public final class ImageURLModifiers {

	/** Read from any thread. */
	private static volatile IImageURLModifier urlModifier;

	private ImageURLModifiers() {
	}

	/**
	 * Installs the modifier consulted when an image is loaded from a URL. Images
	 * created before are not reloaded.
	 *
	 * @param modifier the modifier, or <code>null</code> to remove it
	 */
	public static void setURLModifier(IImageURLModifier modifier) {
		urlModifier = modifier;
	}

	/**
	 * @return the installed modifier, or <code>null</code> if none is installed
	 */
	public static IImageURLModifier getURLModifier() {
		return urlModifier;
	}
}
