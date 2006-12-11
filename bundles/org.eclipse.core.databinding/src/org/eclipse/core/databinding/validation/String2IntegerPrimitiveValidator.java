/*
 * Copyright (C) 2005, 2006 db4objects Inc. (http://www.db4o.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 *     Boris Bokowski (IBM Corporation) - bug 118429
 */
package org.eclipse.core.databinding.validation;

import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * IntValidator. Validate String to int data input
 */
public class String2IntegerPrimitiveValidator implements IValidator {

	public IStatus validate(Object value) {
		try {
			Integer.parseInt((String) value);
			return Status.OK_STATUS;
		} catch (Throwable t) {
			return ValidationStatus.error(getHint());
		}
	}

	private String getHint() {
		return BindingMessages.getString("Validate_RangeStart") + Integer.MIN_VALUE + //$NON-NLS-1$
				BindingMessages.getString("and") + Integer.MAX_VALUE + "."; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
