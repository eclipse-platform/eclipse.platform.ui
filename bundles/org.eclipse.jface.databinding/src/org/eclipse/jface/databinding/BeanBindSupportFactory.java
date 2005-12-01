package org.eclipse.jface.databinding;

import java.lang.reflect.Method;

import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;

/**
 * A BindSupportFactory that will automatically grab validators from an object's properties,
 * if a get&lt;PropertyName>Validator method is defined.  Makes it easy to associate
 * validators with the properties that they are responsible for validating.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * 
 * @since 3.2
 */
public class BeanBindSupportFactory implements IBindSupportFactory {

	public IValidator createValidator(Class fromType, Class toType, Object modelDescription) {
		if (modelDescription instanceof Property) {
			Property property = (Property) modelDescription;
			String propertyName = (String) property.getPropertyID();
			String getValidatorMethodName = "get" + upperCaseFirstLetter(propertyName) + "Validator"; //$NON-NLS-1$ //$NON-NLS-2$
			
			Class objectClass = property.getObject().getClass();
			
			Method getValidator;
			try {
				getValidator = objectClass.getMethod(getValidatorMethodName, new Class[] {});
			} catch (Exception e) {
				return null;
			}
			
			try {
				IValidator result = (IValidator) getValidator.invoke(property.getObject(), new Object[] {});
				return result;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

    private String upperCaseFirstLetter(String name) {
        String result = name.substring(0, 1).toUpperCase() + name.substring(1);
        return result;
    }

    public IConverter createConverter(Class fromType, Class toType, Object modelDescription) {
		return null;
	}

}
