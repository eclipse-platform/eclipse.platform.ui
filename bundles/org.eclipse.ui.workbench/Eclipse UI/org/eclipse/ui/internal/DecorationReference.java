package org.eclipse.ui.internal;


/**
 * A DecorationReference is a class that holds onto the starting
 * text and image of a decoration.
 */
class DecorationReference {

	Object element;
	Object adaptedElement;

	DecorationReference(Object object) {
		element = object;
	}

	DecorationReference(Object object, Object adaptedObject) {
		this(object);
		this.adaptedElement = adaptedObject;
	}

	/**
	 * Returns the adaptedElement.
	 * @return Object
	 */
	public Object getAdaptedElement() {
		return adaptedElement;
	}

	/**
	 * Returns the element.
	 * @return Object
	 */
	public Object getElement() {
		return element;
	}

}
