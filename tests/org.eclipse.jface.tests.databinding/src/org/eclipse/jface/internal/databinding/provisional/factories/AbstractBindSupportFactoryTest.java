/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.factories;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

import junit.framework.TestCase;

/**
 * @since 3.2
 *
 */
public class AbstractBindSupportFactoryTest extends TestCase {

	protected DataBindingContext ctx;
	
	protected void setUp() {
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));

        ctx = new DataBindingContext();
	}

	protected void assertNoErrorsFound() {
		IStatus status = AggregateValidationStatus.getStatusMaxSeverity(ctx.getBindings());
		assertTrue("No errors should be found, but found " + status , status.isOK());
	}

	protected void assertErrorsFound() {
		IStatus status = AggregateValidationStatus.getStatusMaxSeverity(ctx.getBindings());
		assertFalse("Errors should be found, but found none.", status.isOK());
	}

}
