package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Extension to <code>IAnnotationModelListener</code>. Intention to replace
 * <code>IAnnotationModelListener</code>.
 */
public interface IAnnotationModelListenerExtension {
	
	/**
	 * Called if a model change occurred on the given model.
	 *
	 * @param event the event to be sent out
	 */
	void modelChanged(AnnotationModelEvent event);
}
