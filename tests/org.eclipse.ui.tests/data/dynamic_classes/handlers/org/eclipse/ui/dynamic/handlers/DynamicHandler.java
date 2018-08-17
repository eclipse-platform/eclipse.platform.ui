/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.ui.dynamic.handlers;import org.eclipse.core.commands.AbstractHandler;import org.eclipse.core.commands.ExecutionEvent;/** * A handler that sets a flag when it executes. * * @since 3.1.1 */public final class DynamicHandler extends AbstractHandler {	@Override
	public final Object execute(final ExecutionEvent event) {		return null;	}}