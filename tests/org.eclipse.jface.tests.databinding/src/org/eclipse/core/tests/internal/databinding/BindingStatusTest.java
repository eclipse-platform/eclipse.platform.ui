package org.eclipse.core.tests.internal.databinding;
import junit.framework.TestCase;

import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

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

/**
 * @since 1.1
 */
public class BindingStatusTest extends TestCase {
	private BindingStatus bindingStatus;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		bindingStatus = BindingStatus.ok();
	}

	public void testMessageIsFromStatus() throws Exception {
		String message = "error message";
		IStatus status = ValidationStatus.error(message);

		bindingStatus.add(status);
		assertEquals(message, bindingStatus.getMessage());
	}

	public void testExceptionIsFromStatus() throws Exception {
		IllegalArgumentException e = new IllegalArgumentException();
		Status status = new Status(0, Policy.JFACE_DATABINDING, 0, "", e);

		bindingStatus.add(status);
		assertEquals(e, bindingStatus.getException());
	}

	public void testPluginIsFromStatus() throws Exception {
		String plugin = "test";
		Status status = new Status(0, plugin, 0, "", null);

		bindingStatus.add(status);
		assertEquals(plugin, bindingStatus.getPlugin());
	}

	public void testCodeIsFromStatus() throws Exception {
		int code = 1;
		Status status = new Status(0, Policy.JFACE_DATABINDING, code, "", null);

		bindingStatus.add(status);
		assertEquals(code, status.getCode());
	}

	public void testSeverityIsFromStatus() throws Exception {
		IStatus status = ValidationStatus.error("");

		bindingStatus.add(status);
		assertEquals(IStatus.ERROR, status.getSeverity());
	}

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

	public void testEqual() throws Exception {
		BindingStatus status1 = BindingStatus.ok();
		BindingStatus status2 = BindingStatus.ok();

		assertEquals(status1, status2);
	}

	public void testNotEqual() throws Exception {
		BindingStatus status1 = BindingStatus.ok();
		BindingStatus status2 = BindingStatus.ok();

		status2.add(ValidationStatus.error(""));
		assertFalse(status1.equals(status2));
	}

	public void testHashCode() throws Exception {
		BindingStatus status1 = BindingStatus.ok();
		BindingStatus status2 = BindingStatus.ok();

		assertEquals(status1.hashCode(), status2.hashCode());
	}

	public void testOkInitializesStatus() throws Exception {
		BindingStatus status = BindingStatus.ok();
		assertEquals(Policy.JFACE_DATABINDING, status.getPlugin());
		assertEquals("", status.getMessage());
		assertEquals(0, status.getCode());
		assertEquals(0, status.getChildren().length);
		assertNull(status.getException());
	}
}
