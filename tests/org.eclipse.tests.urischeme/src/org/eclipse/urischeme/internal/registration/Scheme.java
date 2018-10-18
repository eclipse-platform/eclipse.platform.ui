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

import org.eclipse.urischeme.IScheme;

/**
 * Implementation of {@link IScheme} for testing purpose.
 *
 */
public class Scheme implements IScheme {

	private String name;
	private String desription;

	public Scheme(String name, String desription) {
		this.name = name;
		this.desription = desription;

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return desription;
	}

}
