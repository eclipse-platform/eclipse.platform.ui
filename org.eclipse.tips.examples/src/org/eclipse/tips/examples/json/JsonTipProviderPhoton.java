/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.examples.json;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.tips.json.JsonTipProvider;

public class JsonTipProviderPhoton extends JsonTipProvider {

	public JsonTipProviderPhoton() throws MalformedURLException {
		URL resource = getClass().getResource("provider.json");
		setJsonUrl(resource.toString());
	}

	@Override
	public String getID() {
		return getClass().getName();
	}
}
