/*******************************************************************************
 * Copyright (c) 2004, 2023 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.resource;


/**
 * Thrown when allocation of an SWT device resource fails
 *
 * @since 3.1
 */
public class DeviceResourceException extends RuntimeException {

	private Throwable cause;

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 11454598756198L;

	/**
	 * Creates a DeviceResourceException indicating an error attempting to
	 * create a resource and an embedded low-level exception describing the cause
	 *
	 * @param missingResource the failed resource
	 * @param cause cause of the exception (or null if none)
	 */
	public DeviceResourceException(DeviceResourceDescriptor<?> missingResource, Throwable cause) {
		super("Unable to create resource " + missingResource); //$NON-NLS-1$
		// don't pass the cause to super, to allow compilation against JCL Foundation (bug 80059)
		this.cause = cause;
	}

	/**
	 * Creates a DeviceResourceException indicating an error attempting to
	 * create a resource
	 *
	 * @param missingResource the failed resource
	 */
	public DeviceResourceException(DeviceResourceDescriptor<?> missingResource) {
		this(missingResource, null);
	}

	/**
	 * Returns the cause of this throwable or <code>null</code> if the
	 * cause is nonexistent or unknown.
	 *
	 * @return the cause or <code>null</code>
	 * @since 3.1
	 */
	@Override
	public Throwable getCause() {
		return cause;
	}

}
