/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jface.text.source;


/**
 * Extension interface for {@link IAnnotationModelListener}. Introduces a
 * notification mechanism that notifies the user by means of <code>AnnotationModelEvent</code>s.
 * Thus, more detailed information can be sent to the listener. This mechanism replaces the original notification
 * mechanism of <code>IAnnotationModelListener</code>.
 *
 * @since 2.0
 */
public interface IAnnotationModelListenerExtension {

	/**
	 * Called if a model change occurred on the given model.
	 *
	 * @param event the event to be sent out
	 */
	void modelChanged(AnnotationModelEvent event);
}
