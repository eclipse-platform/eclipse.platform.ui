package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.internal.plugins.IPluginVisitor;
import org.eclipse.core.internal.plugins.PluginRegistry;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The DecoratorManager is the class that handles all of the
 * decorators defined in the image.
 */

public class DecoratorManager {

	//Hold onto the list of listeners to be told if a change has occured
	private Collection listeners = new HashSet();

	//The cachedDecorators are a 1-1 mapping of type to decorator.
	private HashMap cachedDecorators = new HashMap();

	/**
	 * Create a new instance of the receiver and load the
	 * settings from the installed plug-ins.
	 */

	public DecoratorManager() {
		DecoratorRegistryReader reader = new DecoratorRegistryReader();
		reader.readRegistry(Platform.getPluginRegistry());
		cachedDecorators = (HashMap) reader.registryValues.clone();
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

		ILabelDecorator decorator = getDecoratorFor(element);
		if (decorator == null)
			return text;
		else
			return decorator.decorateText(text, element);
	}

	/**
	 * Decorate the image provided for the element type.
	 * Return null if there are none defined for this type.
	 */
	public Image decorateImage(Image image, Object element) {

		ILabelDecorator decorator = getDecoratorFor(element);
		if (decorator == null)
			return image;
		else
			return decorator.decorateImage(image, element);
	}

	/**
	 * Get the decorator registered for elements of this type.
	 * If there is one return it. If not search for one first
	 * via superclasses and then via interfaces.
	 * If still nothing is found then add in a decorator that
	 * does nothing.
	 */
	private ILabelDecorator getDecoratorFor(Object element) {

		Class elementClass = element.getClass();
		Object existing = cachedDecorators.get(elementClass.getName());
		if (existing != null)
			return (ILabelDecorator) existing;

		List allClasses = new ArrayList();
		ILabelDecorator decorator = null;

		if (decorator == null) {
			allClasses = computeClassOrder(elementClass);
			decorator = findDecorator(allClasses);
		}

		//Nothing cached so look for the interfaces;
		if (decorator == null) {
			allClasses.add(elementClass);
			decorator = findDecorator(computeInterfaceOrder(allClasses));
		}

		if (decorator == null)
			decorator = NullDecorator.getNullDecorator();

		cachedDecorators.put(element.getClass().getName(), decorator);
		return decorator;
	}

	/** 
	 * Find a defined decorator that has a type that is the same
	 * as one of the classes. We assume that there is an entry for
	 * at least the type defined in the plugin.xml.
	 */
	private ILabelDecorator findDecorator(Collection classes) {
		Iterator iterator = classes.iterator();
		while (iterator.hasNext()) {
			String key = ((Class) iterator.next()).getName();
			if (cachedDecorators.containsKey(key))
				return (ILabelDecorator) cachedDecorators.get(key);
		}
		return null;
	}

	/**
	* Return whether or not the decorator registered for element
	* has a label property called property name.
	*/
	public boolean isLabelProperty(Object element, String property) {
		ILabelDecorator decorator = getDecoratorFor(element);
		if (decorator != null && decorator.isLabelProperty(element, property))
			return true;
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
}