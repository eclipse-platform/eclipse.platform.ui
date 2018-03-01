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
package org.eclipse.tips.json;

import org.eclipse.tips.core.Tip;

/**
 * Decorator of {@link Tip} to mark that this it is constructed from a Json
 * structure.
 *
 */
public interface IJsonTip {

	/**
	 * Returns the JsonObject that describes this Tip.
	 *
	 * @return the JsonObject describing the tip.
	 */
	public String getJsonObject();
}