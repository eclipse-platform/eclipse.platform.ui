package org.eclipse.ui.internal.decorators;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/** 
 * The Decoration Result is the result of a decoration.
 */
class DecorationResult {

	private List prefixes;
	private List suffixes;
	private ImageDescriptor[] descriptors;

	DecorationResult(
		List prefixList,
		List suffixList,
		ImageDescriptor[] imageDescriptors) {
		prefixes = prefixList;
		suffixes = suffixList;
		descriptors = imageDescriptors;
	}


	/**
	 * Decorate the Image supplied with the overlays.
	 */
	Image decorateWithOverlays(Image image, OverlayCache overlayCache) {

		//Do not try to do anything if there is no source or overlays
		if (image == null || descriptors == null)
			return image;

		return overlayCache.applyDescriptors(image, descriptors);
	}

	/**
	 * Decorate the String supplied with the prefixes and suffixes.
	 */
	String decorateWithText(String text) {

		if (prefixes.isEmpty() && suffixes.isEmpty())
			return text;

		StringBuffer result = new StringBuffer();

		ListIterator prefixIterator = prefixes.listIterator();

		while (prefixIterator.hasNext()) {
			result.append(prefixIterator.next());
		}

		result.append(text);

		ListIterator suffixIterator = suffixes.listIterator();

		while (suffixIterator.hasNext()) {
			result.append(suffixIterator.next());
		}

		return result.toString();
	}


}
