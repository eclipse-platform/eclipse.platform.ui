/*******************************************************************************
 * Copyright (c) 2015 EclipseSource Munich GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugen Neufeld - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.minmax;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * Helper Class used by the MinMaxAddon and the TrimStack to create and parse
 * trimstack ids.
 */
public class TrimStackIdHelper {

	private static final String ID_SUFFIX = "(minimized)"; //$NON-NLS-1$

	/**
	 * This is used to identify the different parts of an trimstack id.
	 */
	public enum TrimStackIdPart {
		/**
		 * The id of the element in the trimstack.
		 */
		ELEMENT_ID, /**
					 * The id of the window where the trimstack is added to.
					 */
		WINDOW_ID, /**
					 * The id of the perspective where the trimstack is added
					 * to.
					 */
		PERSPECTIVE_ID
	}

	private TrimStackIdHelper() {
	}

	/**
	 * Helper method to parse the trimstackid.
	 *
	 * @param trimStackId
	 *            the id to parse
	 * @return a mapping from TrimStackIdPart to id
	 */
	public static Map<TrimStackIdPart, String> parseTrimStackId(String trimStackId) {
		int index = trimStackId.indexOf('(');
		String stackId = trimStackId.substring(0, index);
		Map<TrimStackIdPart, String> result = new LinkedHashMap<TrimStackIdHelper.TrimStackIdPart, String>();
		result.put(TrimStackIdPart.ELEMENT_ID, stackId);
		String suffix = trimStackId.substring(index);
		if (ID_SUFFIX.equalsIgnoreCase(suffix))
			return result;
		String windowPerspId = suffix.substring(1, suffix.length() - 1);
		int windowIdEnd = windowPerspId.indexOf(')');
		if (windowIdEnd != -1) {
			String windowId = windowPerspId.substring(0, windowIdEnd);
			String perspId = windowPerspId.substring(windowPerspId.indexOf('(') + 1, windowPerspId.length());
			result.put(TrimStackIdPart.WINDOW_ID, windowId);
			result.put(TrimStackIdPart.PERSPECTIVE_ID, perspId);
		} else {
			result.put(TrimStackIdPart.PERSPECTIVE_ID, windowPerspId);
		}
		return result;
	}

	/**
	 * Helper method to create an id for the trimstack.
	 *
	 * @param element
	 *            The Element that will be added to the trim
	 * @param perspective
	 *            The perspective where the trim will be added to
	 * @param window
	 *            The window where the trim will be added to
	 * @return The id for the trimstack
	 */
	public static String createTrimStackId(MUIElement element,MPerspective perspective,MWindow window){
		StringBuilder sb = new StringBuilder(element.getElementId());
		if (perspective == null) {
			sb.append(ID_SUFFIX);
		} else {
			if (window != null && window.getElementId() != null)
				sb.append('(' + window.getElementId() + ')' + '.');
			if (perspective.getElementId() != null)
				sb.append('(' + perspective.getElementId() + ')');
		}
		return sb.toString();
	}
}
