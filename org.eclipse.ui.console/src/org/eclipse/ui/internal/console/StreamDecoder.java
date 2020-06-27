/*******************************************************************************
 * Copyright (c) 2017 Andreas Loth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andreas Loth - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.console;

import java.nio.charset.Charset;


/**
 * @deprecated class was moved to
 *             {@link org.eclipse.debug.internal.core.StreamDecoder}
 */
@Deprecated
public class StreamDecoder extends org.eclipse.debug.internal.core.StreamDecoder {

	public StreamDecoder(Charset charset) {
		super(charset);
	}
}
