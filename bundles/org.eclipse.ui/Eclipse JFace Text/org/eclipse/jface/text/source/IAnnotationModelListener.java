package org.eclipse.jface.text.source;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
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
