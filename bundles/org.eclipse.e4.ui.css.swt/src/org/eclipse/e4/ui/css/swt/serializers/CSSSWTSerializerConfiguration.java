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
package org.eclipse.e4.ui.css.swt.serializers;

import org.eclipse.e4.ui.css.core.serializers.CSSSerializerConfiguration;

/**
 * {@link CSSSerializerConfiguration} configuration used to get style of SWT control.
 */
public class CSSSWTSerializerConfiguration extends CSSSerializerConfiguration {

	public static final CSSSerializerConfiguration INSTANCE = new CSSSWTSerializerConfiguration();

	public CSSSWTSerializerConfiguration() {
		super.addAttributeFilter("style");
	}

}
