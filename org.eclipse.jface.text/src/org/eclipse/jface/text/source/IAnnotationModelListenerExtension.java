/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.source;
 
/**
 * Extension interface for <code>IAnnotationModelListener</code>. Introduces a
 * notification mechanism that notifies the userby means of <code>AnnotationModelEvent</code>s.
 * Thus, more detailed information can be sent to the listener. Will replace the original notification
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
