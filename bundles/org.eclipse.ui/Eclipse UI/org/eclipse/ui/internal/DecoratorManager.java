package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

/**
 * The DecoratorManager is the class that handles all of the
 * decorators defined in the image.
 */

public class DecoratorManager implements ILabelDecorator{

	//Hold onto the list of listeners to be told if a change has occured
	private Collection listeners = new HashSet();

	//The cachedDecorators are a 1-many mapping of type to decorator.
	private HashMap cachedDecorators = new HashMap();

	//The definitions are definitions read from the registry
	private DecoratorDefinition[] definitions;

	/**
	 * Create a new instance of the receiver and load the
	 * settings from the installed plug-ins.
	 */

	public DecoratorManager() {
		DecoratorRegistryReader reader = new DecoratorRegistryReader();
		Collection values = reader.readRegistry(Platform.getPluginRegistry());
		definitions = new DecoratorDefinition[values.size()];
		values.toArray(definitions);
	}

	/**
	 * Add the listener to the list of listeners.
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove the listener from the list.
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Inform all of the listeners that require an update
	 */
	private void fireListeners(LabelProviderChangedEvent event) {
		Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			ILabelProviderListener listener = (ILabelProviderListener) iterator.next();
			listener.labelProviderChanged(event);
		}
	}

	/**
	 * Decorate the text provided for the element type.
	 * Return null if there are none defined for this type.
	 */
	public String decorateText(String text, Object element) {

		ILabelDecorator[] decorators = getDecoratorsFor(element);
		String result = text;
		for (int i = 0; i < decorators.length; i++) {
			result = decorators[i].decorateText(text, element);
		}
		return result;
	}

	/**
	 * Decorate the image provided for the element type.
	 * Return null if there are none defined for this type.
	 */
	public Image decorateImage(Image image, Object element) {

		ILabelDecorator[] decorators = getDecoratorsFor(element);
		Image result = image;
		for (int i = 0; i < decorators.length; i++) {
			result = decorators[i].decorateImage(image, element);
		}
		return result;
	}

	/**
	 * Get the decorators registered for elements of this type.
	 * If there is one return it. If not search for one first
	 * via superclasses and then via interfaces.
	 * If still nothing is found then add in a decorator that
	 * does nothing.
	 */
	private ILabelDecorator[] getDecoratorsFor(Object element) {

		Class elementClass = element.getClass();
		String className = elementClass.getName();
		if (cachedDecorators.containsKey(className))
			return (ILabelDecorator[]) cachedDecorators.get(className);

		List allClasses = new ArrayList();
		allClasses.add(element.getClass());
		ArrayList decorators = new ArrayList();
		DecoratorDefinition[] enabledDefinitions = enabledDefinitions();

		allClasses.addAll(computeClassOrder(elementClass));
		findDecorators(allClasses, enabledDefinitions, decorators);

		findDecorators(
			computeInterfaceOrder(allClasses),
			enabledDefinitions,
			decorators);

		ILabelDecorator[] decoratorArray = new ILabelDecorator[decorators.size()];
		decorators.toArray(decoratorArray);
		cachedDecorators.put(element.getClass().getName(), decoratorArray);
		return decoratorArray;
	}

	/** 
	 * Find a defined decorators that have a type that is the same
	 * as one of the classes. 
	 */
	private void findDecorators(
		Collection classList,
		DecoratorDefinition[] enabledDefinitions,
		ArrayList result) {

		Iterator classes = classList.iterator();
		while (classes.hasNext()) {
			String className = ((Class) classes.next()).getName();
			for (int i = 0; i < enabledDefinitions.length; i++) {
				if (className.equals(enabledDefinitions[i].getObjectClass()))
					result.add(enabledDefinitions[i].getDecorator());
			}
		}

	}

	/**
	* Return whether or not the decorator registered for element
	* has a label property called property name.
	*/
	public boolean isLabelProperty(Object element, String property) {
		ILabelDecorator[] decorators = getDecoratorsFor(element);
		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].isLabelProperty(element, property))
				return true;
		}
		return false;
	}

	/**
	* Returns the class search order starting with <code>extensibleClass</code>.
	* The search order is defined in this class' comment.
	*/
	private Vector computeClassOrder(Class extensibleClass) {
		Vector result = new Vector(4);
		Class clazz = extensibleClass;
		while (clazz != null) {
			result.addElement(clazz);
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	/**
	 * Returns the interface search order for the class hierarchy described
	 * by <code>classList</code>.
	 * The search order is defined in this class' comment.
	 */
	private List computeInterfaceOrder(List classList) {
		List result = new ArrayList(4);
		Map seen = new HashMap(4);
		for (Iterator list = classList.iterator(); list.hasNext();) {
			Class[] interfaces = ((Class) list.next()).getInterfaces();
			internalComputeInterfaceOrder(interfaces, result, seen);
		}
		return result;
	}

	/**
	* Add interface Class objects to the result list based
	* on the class hierarchy. Interfaces will be searched
	* based on their position in the result list.
	*/

	private void internalComputeInterfaceOrder(
		Class[] interfaces,
		List result,
		Map seen) {
		List newInterfaces = new ArrayList(seen.size());
		for (int i = 0; i < interfaces.length; i++) {
			Class interfac = interfaces[i];
			if (seen.get(interfac) == null) {
				result.add(interfac);
				seen.put(interfac, interfac);
				newInterfaces.add(interfac);
			}
		}
		for (Iterator newList = newInterfaces.iterator(); newList.hasNext();)
			internalComputeInterfaceOrder(
				((Class) newList.next()).getInterfaces(),
				result,
				seen);
	}
	
	/**
	 * Return the enabled decorator definitions
	 */
	private DecoratorDefinition[] enabledDefinitions(){
		ArrayList result = new ArrayList();
		for(int i = 0; i < definitions.length; i++){
			if(definitions[i].isEnabled())
				result.add(definitions[i]);
		}
		DecoratorDefinition[] returnArray = new DecoratorDefinition[result.size()];
		result.toArray(returnArray);
		return returnArray;
	}
	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

}