/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;

import org.eclipse.core.runtime.*;

/**
 * Exception thrown when IOException during downloading features
 * 
 * @since 3.0
 */
public class FeatureDownloadException extends CoreException {
	/**
	 * Construct the exception indicating enclosing CoreException
	 * 
	 * @since 3.0
	 */
	public FeatureDownloadException(String msg, Exception e) {
		super(
			new Status(
				IStatus.INFO,
				"org.eclipse.update.core", //$NON-NLS-1$
				IStatus.OK,
				msg,
				e));
	}

}
