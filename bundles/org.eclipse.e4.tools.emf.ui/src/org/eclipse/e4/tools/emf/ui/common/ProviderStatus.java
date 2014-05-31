/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 436283
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.common;

/**
 * Provider status used in IProviderStatusCallback
 *
 * @author Steven Spungin
 *
 */
public enum ProviderStatus {
	INITIALIZING, READY, CANCELLED
}
