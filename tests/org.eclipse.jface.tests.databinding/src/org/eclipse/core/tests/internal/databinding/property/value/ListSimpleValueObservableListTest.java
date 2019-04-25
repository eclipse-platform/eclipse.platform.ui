/*******************************************************************************
 * Copyright (c) 2010 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 301410)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.property.value;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

public class ListSimpleValueObservableListTest extends
		AbstractDefaultRealmTestCase {

	@Test
	public void testBug301410() {
		BeanProperties.value(Bean.class, "value").observeDetail(
				new WritableList()).dispose();
	}
}
