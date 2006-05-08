/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;


/**
 * <p>
 * Stores information about the non-leaf components of a location until such
 * time as the leaf component can be created. This is used to support the order
 * in which a legacy action-based extension is parsed.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
final class LegacyLocationInfo {

	/**
	 * The part as it would appear in an instance of {@link SPart}. This value
	 * is <code>null</code> iff this location info does not represent an
	 * {@link SPart} instance.
	 */
	private final String part;

	/**
	 * Whether the leaf element should be converted into an instance of
	 * {@link SPopup}.
	 */
	private final boolean popupMenu;

	/**
	 * Constructs an instance of {@link LegacyLocationInfo}. This is used for
	 * {@link IRegisryConstants#ELEMENT_OBJECT_CONTRIBUTION}. This will just
	 * convert any {@link SBar} instances into {@link SPopup} instances.
	 */
	LegacyLocationInfo() {
		this(null, true);
	}

	/**
	 * Constructs an instance of {@link LegacyLocationInfo}. This is used for
	 * {@link IRegisryConstants#ELEMENT_VIEW_CONTRIBUTION}.
	 * 
	 * @param part
	 *            The part as it would appear in an instance of {@link SPart}.
	 *            This value is <code>null</code> iff this location info does
	 *            not represent an {@link SPart} instance.
	 */
	LegacyLocationInfo(final String part) {
		this(part, false);
	}

	/**
	 * Constructs an instance of {@link LegacyLocationInfo}. This is used for
	 * {@link IRegisryConstants#ELEMENT_VIEWER_CONTRIBUTION}.
	 * 
	 * @param part
	 *            The part as it would appear in an instance of {@link SPart}.
	 *            This value is <code>null</code> iff this location info does
	 *            not represent an {@link SPart} instance.
	 * @param popupMenu
	 *            Whether the leaf element should be converted into a popup
	 *            element (if it is not one already).
	 */
	LegacyLocationInfo(final String part, final boolean popupMenu) {
		this.part = part;
		this.popupMenu = popupMenu;
	}

	/**
	 * Appends the leaf element to this location, and returns the result.
	 * 
	 * @param leafElement
	 *            The leaf element to append; must not be <code>null</code>.
	 * @return The location element containing the information in this instance
	 *         as well as in the leaf element; never <code>null</code>.
	 */
	final LocationElement append(final LeafLocationElement leafElement) {
		if ((popupMenu) && (leafElement instanceof SBar)) {
			final SBar bar = (SBar) leafElement;
			final String path = bar.getPath();
			final SPopup popup = new SPopup(null, path);
			if (part == null) {
				return popup; // object contrib
			}

			return new SPart(part, SPart.TYPE_ID, popup); // viewer contrib
		}

		if (part != null) {
			return new SPart(part, SPart.TYPE_ID, leafElement); // view contrib
		}

		return leafElement; // unknown
	}
}
