/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.revisions;


/**
 * A listener which is notified when revision information changes.
 *
 * @see RevisionInformation
 * @see IRevisionRulerColumnExtension
 * @since 3.3
 */
public interface IRevisionListener {
	/**
	 * Notifies the receiver that the revision information has been updated. This typically occurs
	 * when revision information is being displayed in an editor and the annotated document is
	 * modified.
	 *
	 * @param e the revision event describing the change
	 */
	void revisionInformationChanged(RevisionEvent e);
}
