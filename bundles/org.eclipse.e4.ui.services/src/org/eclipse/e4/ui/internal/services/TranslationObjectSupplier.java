package org.eclipse.e4.ui.internal.services;

import java.lang.reflect.ParameterizedType;

import java.lang.reflect.Type;

import org.eclipse.e4.ui.services.ETranslationService;

import org.eclipse.e4.core.internal.contexts.ContextObjectSupplier;

import org.eclipse.e4.core.internal.di.Requestor;

import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;

import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;

public class TranslationObjectSupplier extends ExtendedObjectSupplier {

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor,
			boolean track, boolean group) {
		Class<?> descriptorsClass = getDesiredClass(descriptor.getDesiredType());

		Requestor req = (Requestor) requestor;
		ContextObjectSupplier sub = (ContextObjectSupplier) req
				.getPrimarySupplier();
		ETranslationService service = sub.getContext().get(
				ETranslationService.class);
		try {
			return service.createInstance(descriptorsClass);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Class<?> getDesiredClass(Type desiredType) {
		if (desiredType instanceof Class<?>)
			return (Class<?>) desiredType;
		if (desiredType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) desiredType).getRawType();
			if (rawType instanceof Class<?>)
				return (Class<?>) rawType;
		}
		return null;
	}

}