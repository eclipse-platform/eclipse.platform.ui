/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;

/**
 * @since 1.1
 */
public class AggregateValidationStatusTest extends AbstractSWTTestCase {
	public void testAggregateValidationStatusValueType() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		AggregateValidationStatus status = new AggregateValidationStatus(dbc
				.getBindings(), AggregateValidationStatus.MAX_SEVERITY);
		assertEquals(IStatus.class, status.getValueType());
	}

	public void testConstructor_DefaultRealm() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		AggregateValidationStatus status = new AggregateValidationStatus(dbc
				.getBindings(), AggregateValidationStatus.MAX_SEVERITY);
		assertEquals(Realm.getDefault(), status.getRealm());
	}
}
