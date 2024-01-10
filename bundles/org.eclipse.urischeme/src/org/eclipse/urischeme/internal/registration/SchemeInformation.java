/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.util.Objects;

import org.eclipse.urischeme.ISchemeInformation;

/**
 * The pojo for holding information of a scheme w.r.t. the handling eclipse.
 */
public class SchemeInformation implements ISchemeInformation {

	private String name;
	private String description;
	private boolean handled;
	private String handlerInstanceLocation;

	public SchemeInformation(String schemeName, String schemeDescription) {
		this.name = schemeName;
		this.description = schemeDescription;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isHandled() {
		return handled;
	}

	@Override
	public String getHandlerInstanceLocation() {
		return handlerInstanceLocation;
	}

	/**
	 * Sets the handled value to true if scheme is handled by current Eclipse
	 * installation and false otherwise
	 */
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	public void setHandlerLocation(String handlerInstanceLocation) {
		this.handlerInstanceLocation = handlerInstanceLocation;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass()) {
			return false;
		}
		SchemeInformation other = (SchemeInformation) o;
		return Objects.equals(this.name, other.name) //
				&& Objects.equals(this.description, other.description) //
				&& this.handled == other.handled //
				&& Objects.equals(this.handlerInstanceLocation, other.handlerInstanceLocation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, handled, handlerInstanceLocation);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SchemeInformation ["); //$NON-NLS-1$
		if (name != null) {
			builder.append("name="); //$NON-NLS-1$
			builder.append(name);
			builder.append(", "); //$NON-NLS-1$
		}
		if (description != null) {
			builder.append("description="); //$NON-NLS-1$
			builder.append(description);
			builder.append(", "); //$NON-NLS-1$
		}
		builder.append("handled="); //$NON-NLS-1$
		builder.append(handled);
		builder.append(", "); //$NON-NLS-1$
		if (handlerInstanceLocation != null) {
			builder.append("handlerInstanceLocation="); //$NON-NLS-1$
			builder.append(handlerInstanceLocation);
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}
