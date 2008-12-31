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
