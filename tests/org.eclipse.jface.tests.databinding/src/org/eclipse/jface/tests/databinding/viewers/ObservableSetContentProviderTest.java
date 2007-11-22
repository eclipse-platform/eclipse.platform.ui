/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Boris Bokowski, IBM - bug 209484
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import java.util.Arrays;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 * 
 */
public class ObservableSetContentProviderTest extends AbstractSWTTestCase {
	public void testKnownElementsRealm() throws Exception {
		ObservableSetContentProvider contentProvider = new ObservableSetContentProvider();
		assertSame("realm for the known elements should be the SWT realm",
				SWTObservables.getRealm(Display.getDefault()), contentProvider
						.getKnownElements().getRealm());
	}
	
	public void testKnownElementsAfterSetInput() {
		ObservableSetContentProvider contentProvider = new ObservableSetContentProvider();
		TableViewer tableViewer = new TableViewer(getShell());
		tableViewer.setContentProvider(contentProvider);
		assertEquals(0, contentProvider.getKnownElements().size());
		WritableSet input = new WritableSet(Arrays.asList(new String[] {"one","two","three"}), String.class);
		tableViewer.setInput(input);
		assertEquals(3, contentProvider.getKnownElements().size());
	}
}
