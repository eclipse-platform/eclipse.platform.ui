package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.EventObject;

/**
 * Event object describing a label provider state change.
 *
 * @see ILabelProviderListener
 */
public class LabelProviderChangedEvent extends EventObject {

	/**
	 * The element whose label needs to be updated or <code>null</code>.
	 */
	private Object[] elements;
/**
 * Creates a new event for the given source, indicating that all labels
 * provided by the source are no longer valid and should be updated.
 *
 * @param source the label provider
 */
public LabelProviderChangedEvent(IBaseLabelProvider source) {
	super(source);
}

/**
 * Creates a new event for the given source, indicating that the label
 * provided by the source for the given elements is no longer valid and should be updated.
 *
 * @param source the label provider
 * @param element the elements whose labels have changed
 */
public LabelProviderChangedEvent(IBaseLabelProvider source, Object[] newElements) {
	super(source);
	this.elements = newElements;
}

/**
 * Creates a new event for the given source, indicating that the label
 * provided by the source for the given element is no longer valid and should be updated.
 *
 * @param source the label provider
 * @param element the element whose label has changed
 */
public LabelProviderChangedEvent(IBaseLabelProvider source, Object newElement) {
	super(source);
	this.elements = new Object[1];
	this.elements[0] = newElement;
}
/**
 * Returns the first element whose label needs to be updated,
 * or <code>null</code> if all labels need to be updated.
 *
 * @return the element whose label needs to be updated or <code>null</code>
 */
public Object getElement() {
	if(this.elements == null|| this.elements.length == 0)
		return null;
	else 
		return this.elements[0];
}

/**
 * Returns the elements whose labels need to be updated,
 * or <code>null</code> if all labels need to be updated.
 *
 * @return the elements whose labels need to be updated or <code>null</code>
 */
public Object[] getElements() {
	if(this.elements == null)
		return null;
	else 
		return this.elements;
}
}
