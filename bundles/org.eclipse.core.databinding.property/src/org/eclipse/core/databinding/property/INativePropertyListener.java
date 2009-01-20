/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * Marker interface for "native" property listeners. A native listener
 * implements the listener interface supported by the source object, and parlays
 * events received from the source object to the property change listener
 * provided when the native listener was constructed.
 * 
 * @since 1.2
 * @see SimpleValueProperty#adaptListener(ISimplePropertyListener)
 * @see SimpleListProperty#adaptListener(ISimplePropertyListener)
 * @see SimpleSetProperty#adaptListener(ISimplePropertyListener)
 * @see SimpleMapProperty#adaptListener(ISimplePropertyListener)
 */
public interface INativePropertyListener {
}
