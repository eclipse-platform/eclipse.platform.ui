package org.eclipse.jface.internal.databinding.api.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.IObservableFactory;
import org.eclipse.jface.internal.databinding.api.Property;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.nonapi.beans.JavaBeanObservableValue;

/**
 * This is an optional IUpdatableFactory that forces all JavaBeans updatables
 * that it creates to be an IUpdatableValue, even if the actual type of
 * the property is a collection.
 * 
 * @since 3.2
 */
public class JavaBeansScalarUpdatableValueFactory extends Object implements
		IObservableFactory {

    public IObservable createObservable(Map properties, Object description, IDataBindingContext bindingContext) {
        if (! (description instanceof Property)) {
           return null;
        }
        Property property = (Property) description;
        Object collectionContainer = property.getObject();
        String propertyName = (String) property.getPropertyID();
        
        BeanInfo beanInfo = null;
        try {
           beanInfo = Introspector.getBeanInfo(collectionContainer.getClass());
        } catch (IntrospectionException e) {
           return null;
        }

        boolean found = false;
        int position = 0;
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        while (!found && position < pds.length) {
           if (pds[position].getName().equals(propertyName)) {
              found = true;
              break;
           }
           ++position;
        }
        if (!found) {
           return null;
        }
        IObservableValue updatable = new JavaBeanObservableValue(collectionContainer, pds[position]);
        return updatable;
     }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IObservableFactory#createObservable(org.eclipse.jface.internal.databinding.api.IDataBindingContext, java.lang.Object)
	 */
	public IObservable createObservable(IDataBindingContext bindingContext, Object description) {
		// TODO Auto-generated method stub
		return null;
	}

}
