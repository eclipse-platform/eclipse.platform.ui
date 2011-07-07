/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
