package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Interface for objects interested in getting informed about 
 * annotation model changes. Changes are the addition or removal
 * of annotations managed by the model. Clients may implement
 * this interface.
 *
 * @see IAnnotationModel
 */
public interface IAnnotationModelListener {

	/**
	 * Called if a model change occurred on the given model.
	 *
	 * @param model the changed annotation model
	 */
	void modelChanged(IAnnotationModel model);
}
