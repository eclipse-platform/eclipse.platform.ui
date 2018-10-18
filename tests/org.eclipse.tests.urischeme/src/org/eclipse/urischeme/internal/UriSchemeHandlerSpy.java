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
package org.eclipse.urischeme.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.urischeme.IUriSchemeHandler;

public class UriSchemeHandlerSpy implements IUriSchemeHandler {

	public Collection<String> uris = new ArrayList<>();

		@Override
		public void handle(String uri) {
			uris.add(uri);
		}
}