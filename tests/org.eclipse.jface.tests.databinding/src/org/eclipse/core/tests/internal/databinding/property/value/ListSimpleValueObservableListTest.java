/*******************************************************************************
 * Copyright (c) 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 301410)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.property.value;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class ListSimpleValueObservableListTest extends
		AbstractDefaultRealmTestCase {

	public void testBug301410() {
		BeanProperties.value(Bean.class, "value").observeDetail(
				new WritableList()).dispose();
	}
}
