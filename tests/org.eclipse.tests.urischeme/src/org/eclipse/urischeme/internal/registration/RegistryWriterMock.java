/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     SAP SE - initial API and implementation
*******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryWriterMock implements IRegistryWriter {

	List<String> addedSchemes = new ArrayList<>();
	List<String> removedSchemes = new ArrayList<>();
	Map<String, String> schemeToHandlerPath = new HashMap<>();

	@Override
	public void addScheme(String scheme, String launcherPath) throws IllegalArgumentException {
		addedSchemes.add(scheme);
	}

	@Override
	public void removeScheme(String scheme) throws IllegalArgumentException {
		removedSchemes.add(scheme);
	}

	@Override
	public String getRegisteredHandlerPath(String scheme) {
		return schemeToHandlerPath.get(scheme);
	}

}
