/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.junit.Test;

/**
 * @since 1.1
 */
public class AggregateValidationStatusTest extends AbstractSWTTestCase {
	@Test
	public void testAggregateValidationStatusValueType() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		AggregateValidationStatus status = new AggregateValidationStatus(dbc
				.getBindings(), AggregateValidationStatus.MAX_SEVERITY);
		assertEquals(IStatus.class, status.getValueType());
	}

	@Test
	public void testConstructor_DefaultRealm() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		AggregateValidationStatus status = new AggregateValidationStatus(dbc
				.getBindings(), AggregateValidationStatus.MAX_SEVERITY);
		assertEquals(Realm.getDefault(), status.getRealm());
	}
}
