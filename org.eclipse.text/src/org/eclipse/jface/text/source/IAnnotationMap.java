/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jface.text.source;


import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;


/**
 * An annotation map is a map specialized for the requirements of an annotation
 * model. The annotation map supports a customizable lock object which is used
 * to synchronize concurrent operations on the map (see
 * {@link org.eclipse.jface.text.ISynchronizable}. The map supports two
 * iterator methods, one for the values and one for the keys of the map. The
 * returned iterators are robust, i.e. they work on a copy of the values and
 * keys set that is made at the point in time the iterator methods are called.
 * <p>
 * The returned collections of the methods <code>values</code>,
 * <code>entrySet</code>, and <code>keySet</code> are not synchronized on
 * the annotation map's lock object.
 * <p>
 *
 * @see org.eclipse.jface.text.source.IAnnotationModel
 * @since 3.0
 */
public interface IAnnotationMap extends Map<Annotation, Position>, ISynchronizable {

	/**
	 * Returns an iterator for a copy of this annotation map's values.
	 *
	 * @return an iterator for a copy of this map's values
	 */
	Iterator<Position> valuesIterator();

	/**
	 * Returns an iterator for a copy of this map's key set.
	 *
	 * @return an iterator for a copy of this map's key set
	 */
	Iterator<Annotation> keySetIterator();

	/**
	 * {@inheritDoc}
	 *
	 * The returned set is not synchronized on this annotation map's lock object.
	 */
	@Override
	Set<Entry<Annotation, Position>> entrySet();

	/**
	 * {@inheritDoc}
	 *
	 * The returned set is not synchronized on this annotation map's lock object.
	 */
	@Override
	Set<Annotation> keySet();

	/**
	 * {@inheritDoc}
	 *
	 * The returned collection is not synchronized on this annotation map's lock object.
	 */
	@Override
	Collection<Position> values();
}
