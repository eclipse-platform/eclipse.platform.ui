// TODO djo: copyright
package org.eclipse.jface.internal.databinding.api.beans;

import java.lang.reflect.Method;

import org.eclipse.jface.internal.databinding.api.IBindSupportFactory;
import org.eclipse.jface.internal.databinding.api.Property;
import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;

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
				getValidator = objectClass.getMethod(getValidatorMethodName, new Class[] {Class.class});
			} catch (Exception e) {
				return null;
			}
			
			try {
				IValidator result = (IValidator) getValidator.invoke(property.getObject(), new Object[] {fromType});
				return result;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
	
	public IDomainValidator createDomainValidator(Class modelType, Object modelDescription) {
		if (modelDescription instanceof Property) {
			Property property = (Property) modelDescription;
			String propertyName = (String) property.getPropertyID();
			String getValidatorMethodName = "get" + upperCaseFirstLetter(propertyName) + "DomainValidator"; //$NON-NLS-1$ //$NON-NLS-2$
			
			Class objectClass = property.getObject().getClass();
			
			Method getValidator;
			try {
				getValidator = objectClass.getMethod(getValidatorMethodName, new Class[] {});
			} catch (Exception e) {
				return null;
			}
			
			try {
				IDomainValidator result = (IDomainValidator) getValidator.invoke(property.getObject(), new Object[] {});
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createValidator(java.lang.Object, java.lang.Object)
	 */
	public IValidator createValidator(Object fromType, Object toType) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createDomainValidator(java.lang.Object)
	 */
	public IDomainValidator createDomainValidator(Object modelType) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#createConverter(java.lang.Object, java.lang.Object)
	 */
	public IConverter createConverter(Object fromType, Object toType) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IBindSupportFactory#isAssignableFromTo(java.lang.Object, java.lang.Object)
	 */
	public Boolean isAssignableFromTo(Object fromType, Object toType) {
		// TODO Auto-generated method stub
		return null;
	}

}
