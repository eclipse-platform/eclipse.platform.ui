/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 * 
 */
public class ObservableSetContentProviderTest extends TestCase {
	public void testKnownElementsRealm() throws Exception {
		ObservableSetContentProvider contentProvider = new ObservableSetContentProvider();
		assertSame("realm for the known elements should be the SWT realm",
				SWTObservables.getRealm(Display.getDefault()), contentProvider
						.getKnownElements().getRealm());
	}
}
