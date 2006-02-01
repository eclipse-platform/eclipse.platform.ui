package org.eclipse.jface.databinding.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.internal.databinding.beans.JavaBeanUpdatableValue;

/**
 * This is an optional IUpdatableFactory that forces all JavaBeans updatables
 * that it creates to be an IUpdatableValue, even if the actual type of
 * the property is a collection.
 * 
 * @since 3.2
 */
public class JavaBeansScalarUpdatableValueFactory extends Object implements
		IUpdatableFactory {

    public IUpdatable createUpdatable(Map properties, Object description, IDataBindingContext bindingContext) {
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
        IUpdatableValue updatable = new JavaBeanUpdatableValue(collectionContainer, pds[position]);
        return updatable;
     }

}
