/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.serializers;

/**
 * CSS HTML Serializer configuration used by {@link CSSSerializer} to filter the
 * attribute type of the HTML widget like input[type='text'].
 */
public class CSSHTMLSerializerConfiguration extends CSSSerializerConfiguration {

	public static final CSSSerializerConfiguration INSTANCE = new CSSHTMLSerializerConfiguration();

	public CSSHTMLSerializerConfiguration() {
		super.addAttributeFilter("type");
	}
}
