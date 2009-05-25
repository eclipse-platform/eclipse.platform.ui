/*
 * Copyright (C) 2005, 2008 db4objects Inc. (http://www.db4o.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 *     Boris Bokowski (IBM Corporation) - bug 118429
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix for 217940
 */
package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;

/**
 * ReadOnlyValidator. A validator that can be used as a partial validator for read-only fields.
 */
public class ReadOnlyValidator implements IValidator {

	private static ReadOnlyValidator singleton = null;

	/**
	 * Returns the ReadOnlyValidator
	 *
	 * @return the ReadOnlyValidator
	 */
	public static ReadOnlyValidator getDefault() {
		if (singleton == null) {
			singleton = new ReadOnlyValidator();
		}
		return singleton;
	}

	public IStatus validate(Object value) {
		// No changes are allowed
		return ValidationStatus.error(BindingMessages
				.getString(BindingMessages.VALIDATE_NO_CHANGE_ALLOWED_HELP));
	}

}
