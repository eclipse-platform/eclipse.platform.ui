/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * In order to provide backward compatibility for clients of
 * <code>IRevisionRulerColumn</code>, extension interfaces are used as a means
 * of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link IRevisionRulerColumnExtension} since
 * version 3.3 allowing to register a selection listener on revisions and a configurable rendering mode.
 * </li>
 * </ul>
 *
 * @since 3.2
 * @see RevisionInformation
 * @see IRevisionRulerColumnExtension
 */
public interface IRevisionRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {
	/**
	 * Sets the revision information.
	 *
	 * @param info the new revision information, or <code>null</code> to reset the ruler
	 */
	void setRevisionInformation(RevisionInformation info);
}