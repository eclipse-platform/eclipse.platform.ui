/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
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
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.sac;

/**
 * Exception used when SAC parser is not retrieved.
 */
public class ParserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4339161134287845644L;

	public ParserNotFoundException(Throwable throwable) {
		super(throwable);
	}
}
