package org.eclipse.core.tests.internal.databinding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;

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

/**
 * @since 1.1
 */
public class BindingStatusTest {
	private BindingStatus bindingStatus;

	@Before
	public void setUp() throws Exception {
		bindingStatus = BindingStatus.ok();
	}

	@Test
	public void testMessageIsFromStatus() throws Exception {
		String message = "error message";
		IStatus status = ValidationStatus.error(message);

		bindingStatus.add(status);
		assertEquals(message, bindingStatus.getMessage());
	}

	@Test
	public void testExceptionIsFromStatus() throws Exception {
		IllegalArgumentException e = new IllegalArgumentException();
		Status status = new Status(0, Policy.JFACE_DATABINDING, 0, "", e);

		bindingStatus.add(status);
		assertEquals(e, bindingStatus.getException());
	}

	@Test
	public void testPluginIsFromStatus() throws Exception {
		String plugin = "test";
		Status status = new Status(0, plugin, 0, "", null);

		bindingStatus.add(status);
		assertEquals(plugin, bindingStatus.getPlugin());
	}

	@Test
	public void testCodeIsFromStatus() throws Exception {
		int code = 1;
		Status status = new Status(0, Policy.JFACE_DATABINDING, code, "", null);

		bindingStatus.add(status);
		assertEquals(code, status.getCode());
	}

	@Test
	public void testSeverityIsFromStatus() throws Exception {
		IStatus status = ValidationStatus.error("");

		bindingStatus.add(status);
		assertEquals(IStatus.ERROR, status.getSeverity());
	}

	@Test
	public void testLowerSeverityDoesNotOverwriteGreaterSeverity() throws Exception {
		String info = "info";
		String error = "error";

		bindingStatus.add(ValidationStatus.error(error));
		assertEquals(IStatus.ERROR, bindingStatus.getSeverity());
		assertEquals(error, bindingStatus.getMessage());

		bindingStatus.add(ValidationStatus.info(info));
		assertEquals(IStatus.ERROR, bindingStatus.getSeverity());
		assertEquals(error, bindingStatus.getMessage());

		IStatus[] children = bindingStatus.getChildren();
		assertEquals(2, children.length);
		assertEquals(IStatus.ERROR, children[0].getSeverity());
		assertEquals(IStatus.INFO, children[1].getSeverity());
	}

	@Test
	public void testEqual() throws Exception {
		BindingStatus status1 = BindingStatus.ok();
		BindingStatus status2 = BindingStatus.ok();

		assertEquals(status1, status2);
	}

	@Test
	public void testNotEqual() throws Exception {
		BindingStatus status1 = BindingStatus.ok();
		BindingStatus status2 = BindingStatus.ok();

		status2.add(ValidationStatus.error(""));
		assertFalse(status1.equals(status2));
	}

	@Test
	public void testHashCode() throws Exception {
		BindingStatus status1 = BindingStatus.ok();
		BindingStatus status2 = BindingStatus.ok();

		assertEquals(status1.hashCode(), status2.hashCode());
	}

	@Test
	public void testOkInitializesStatus() throws Exception {
		BindingStatus status = BindingStatus.ok();
		assertEquals(Policy.JFACE_DATABINDING, status.getPlugin());
		assertEquals("", status.getMessage());
		assertEquals(0, status.getCode());
		assertEquals(0, status.getChildren().length);
		assertNull(status.getException());
	}
}
