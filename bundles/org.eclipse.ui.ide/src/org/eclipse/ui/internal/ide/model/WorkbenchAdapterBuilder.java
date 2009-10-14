/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.ui.ide.IDE;

/**
 * Registers the adapters on core constructs used in the workbench UI.
 * 
 * @deprecated advisors should call the org.eclipse.ui.ide.IDE method
 */
public final class WorkbenchAdapterBuilder {
	/**
	 * Creates extenders and registers
	 * 
	 * @deprecated advisors should call the org.eclipse.ui.ide.IDE method
	 */
	public static void registerAdapters() {
		IDE.registerAdapters();
	}
}
