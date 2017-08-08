/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.Comparator;

import org.eclipse.core.runtime.content.IContentType;

/**
 * Compares extension so that the ones with the most "specialized" content-types are returned first.
 *
 * @param <T>
 */
public class ContentTypeSpecializationComparator<T> implements Comparator<GenericContentTypeRelatedExtension<T>> {

	@Override
	public int compare(GenericContentTypeRelatedExtension<T> o1, GenericContentTypeRelatedExtension<T> o2) {
		return depth(o2.targetContentType) - depth(o1.targetContentType);
	}

	private static int depth(IContentType targetContentType) {
		int res = 0;
		IContentType current = targetContentType;
		while (current != null) {
			res++;
			current = current.getBaseType();
		}
		return res;
	}

}
