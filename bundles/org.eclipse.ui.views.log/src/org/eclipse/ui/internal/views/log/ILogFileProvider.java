/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import java.util.Map;

/**
 * Provides log files.
 */
public interface ILogFileProvider {

	/**
	 * Returns a Map of java.io.File log files indexed by String names.
	 *
	 * @return Map of java.io.File log files index by String names.
	 * @since 3.4
	 */
	Map<String, String> getLogSources();
}