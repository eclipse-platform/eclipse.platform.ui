/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.revisions;

import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;

/**
 * A vertical ruler column capable of displaying revision (annotate) information.
 *
 * @since 3.2
 * @see RevisionInformation
 */
public interface IRevisionRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {
	/**
	 * Sets the revision information.
	 * 
	 * @param info the new revision information, or <code>null</code> to reset the ruler
	 */
	void setRevisionInformation(RevisionInformation info);
}