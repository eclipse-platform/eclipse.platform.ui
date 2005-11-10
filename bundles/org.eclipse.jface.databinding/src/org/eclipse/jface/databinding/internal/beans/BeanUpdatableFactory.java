package org.eclipse.jface.databinding.internal.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IValidationContext;
import org.eclipse.jface.databinding.PropertyDescription;

/**
 * @since 3.2
 * 
 */
public class BeanUpdatableFactory implements IUpdatableFactory {

	public IUpdatable createUpdatable(Map properties, Object description,
			IValidationContext validationContext) {
		if (description instanceof PropertyDescription) {
			PropertyDescription propertyDescription = (PropertyDescription) description;
			if (propertyDescription.getObject() != null) {
				Object object = propertyDescription.getObject();
				BeanInfo beanInfo;
				try {
					beanInfo = Introspector.getBeanInfo(object.getClass());
				} catch (IntrospectionException e) {
					// cannot introspect, give up
					return null;
				}
				PropertyDescriptor[] propertyDescriptors = beanInfo
						.getPropertyDescriptors();
				for (int i = 0; i < propertyDescriptors.length; i++) {
					PropertyDescriptor descriptor = propertyDescriptors[i];
					if (descriptor.getName().equals(
							propertyDescription.getPropertyID())) {
						if (descriptor.getPropertyType().isArray()) {
							return new JavaBeanUpdatableCollection(object,
									descriptor);
						}
						return new JavaBeanUpdatableValue(object, descriptor);
					}
				}
			}
		}
		return null;
	}

}
