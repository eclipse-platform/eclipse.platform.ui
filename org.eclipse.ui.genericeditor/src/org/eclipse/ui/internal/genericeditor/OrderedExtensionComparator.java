/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.internal.genericeditor.TextHoverRegistry.TextHoverExtension;

/**
 * A comparator that allows to sort elements according to their relative
 * placement (isBefore and isAfter)
 *
 */
class OrderedExtensionComparator implements Comparator<TextHoverExtension> {

	private Map<String, TextHoverExtension> extensionsById;

	public OrderedExtensionComparator(Collection<TextHoverExtension> extensions) {
		Assert.isNotNull(extensions);
		this.extensionsById = extensions.stream().collect(Collectors.toMap(TextHoverExtension::getId, Function.identity()));
	}

	@Override
	public int compare(TextHoverExtension arg0, TextHoverExtension arg1) {
		if (isDeclaredAsBefore(arg0, arg1) || isDeclaredAsAfter(arg1, arg0)) {
			return -1;
		}
		if (isDeclaredAsAfter(arg0, arg1) || isDeclaredAsBefore(arg1, arg0)) {
			return +1;
		}
		return arg0.toString().compareTo(arg1.toString());
	}

	private boolean isDeclaredAsBefore(TextHoverExtension arg0, TextHoverExtension arg1) {
		String before0 = arg0.getIsBefore();
		if (before0 == null) {
			return false;
		}
		if ("*".equals(before0) && !"*".equals(arg1.getIsBefore())) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		String id1 = arg1.getId();
		if (id1 == null) {
			return false; 
		}
		if (before0.equals(id1)) {
			return true;
		}
		String after1 = arg1.getIsAfter();
		if (after1 == null) {
			return false;
		}
		return isDeclaredAsAfter(arg0, this.extensionsById.get(after1));
	}

	private boolean isDeclaredAsAfter(TextHoverExtension arg0, TextHoverExtension arg1) {
		String after0 = arg0.getIsAfter();
		if (after0 == null) {
			return false;
		}
		if ("*".equals(after0) && !"*".equals(arg1.getIsAfter())) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		String id1 = arg1.getId();
		if (id1 == null) {
			return false; 
		}
		if (after0.equals(id1)) {
			return true;
		}
		String before1 = arg1.getIsBefore();
		if (before1 == null) {
			return false;
		}
		return isDeclaredAsAfter(arg0, this.extensionsById.get(before1));
	}

}