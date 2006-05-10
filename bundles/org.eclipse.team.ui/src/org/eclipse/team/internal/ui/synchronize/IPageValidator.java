/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

/**
 * Allows clients to perform custom page completion.
 * @since 3.0
 */
public interface IPageValidator {
    /**
  	 * If errorMessage in <code>null</code> then the page is complete otherwise
	 * the error message indicates that the reason why the page is not complete.
	 */
	public void setComplete(String errorMessage);
}
