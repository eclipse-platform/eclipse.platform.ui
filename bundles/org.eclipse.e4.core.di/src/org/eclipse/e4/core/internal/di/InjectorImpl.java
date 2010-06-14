/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.di;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.e4.core.di.IBinding;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;

/**
 * Reflection-based dependency injector.
 */
public class InjectorImpl implements IInjector {

	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$

	private Map<PrimaryObjectSupplier, List<WeakReference<?>>> injectedObjects = new HashMap<PrimaryObjectSupplier, List<WeakReference<?>>>();
	private Set<WeakReference<Class<?>>> injectedClasses = new HashSet<WeakReference<Class<?>>>();
	private HashMap<Class<?>, Object> singletonCache = new HashMap<Class<?>, Object>();
	private Map<Class<?>, Set<Binding>> bindings = new HashMap<Class<?>, Set<Binding>>();

	public void inject(Object object, PrimaryObjectSupplier objectSupplier) {
		// Two stages: first, go and collect {requestor, descriptor[] }
		ArrayList<Requestor> requestors = new ArrayList<Requestor>();
		processClassHierarchy(object, objectSupplier, false /* no static */, true /* track */, true /* normal order */, requestors);

		// if we are not establishing any links to the injected object (nothing to inject,
		// or constructor only), create a pseudo-link to track supplier's disposal
		boolean haveLink = false;
		for (Requestor requestor : requestors) {
			if (requestor.shouldTrack())
				haveLink = true;
		}
		if (!haveLink)
			requestors.add(new ClassRequestor(object.getClass(), this, objectSupplier, object, true));

		// Then ask suppliers to fill actual values {requestor, descriptor[], actualvalues[] }
		resolveRequestorArgs(requestors, objectSupplier, false, true);

		// Call requestors in order
		for (Requestor requestor : requestors) {
			if (requestor.isResolved())
				requestor.execute();
		}
		rememberInjectedObject(object, objectSupplier);

		// We call @PostConstruct after injection. This means that is is called 
		// as a part of both #make() and #inject().
		processAnnotated(PostConstruct.class, object, object.getClass(), objectSupplier, new ArrayList<Class<?>>(5));
	}

	private void rememberInjectedObject(Object object, PrimaryObjectSupplier objectSupplier) {
		synchronized (injectedObjects) {
			List<WeakReference<?>> list;
			if (!injectedObjects.containsKey(objectSupplier)) {
				list = new ArrayList<WeakReference<?>>();
				injectedObjects.put(objectSupplier, list);
			} else
				list = injectedObjects.get(objectSupplier);
			for (WeakReference<?> ref : list) {
				if (object == ref.get())
					return; // we already have it
			}
			list.add(new WeakReference<Object>(object));
		}
	}

	private boolean forgetInjectedObject(Object object, PrimaryObjectSupplier objectSupplier) {
		synchronized (injectedObjects) {
			if (!injectedObjects.containsKey(objectSupplier))
				return false;
			List<WeakReference<?>> list = injectedObjects.get(objectSupplier);
			for (Iterator<WeakReference<?>> i = list.iterator(); i.hasNext();) {
				WeakReference<?> ref = i.next();
				if (object == ref.get())
					i.remove();
				return true;
			}
			return false;
		}
	}

	private List<WeakReference<?>> forgetSupplier(PrimaryObjectSupplier objectSupplier) {
		synchronized (injectedObjects) {
			if (!injectedObjects.containsKey(objectSupplier))
				return null;
			return injectedObjects.remove(objectSupplier);
		}
	}

	private List<WeakReference<?>> getSupplierObjects(PrimaryObjectSupplier objectSupplier) {
		synchronized (injectedObjects) {
			if (!injectedObjects.containsKey(objectSupplier))
				return null;
			return injectedObjects.get(objectSupplier);
		}
	}

	public void uninject(Object object, PrimaryObjectSupplier objectSupplier) {
		if (!forgetInjectedObject(object, objectSupplier))
			return; // not injected at this time
		// Two stages: first, go and collect {requestor, descriptor[] }
		ArrayList<Requestor> requestors = new ArrayList<Requestor>();
		processClassHierarchy(object, objectSupplier, false /* no static */, true /* track */, false /* inverse order */, requestors);
		// might still need to get resolved values from secondary suppliers
		// Ask suppliers to fill actual values {requestor, descriptor[], actualvalues[] }
		resolveRequestorArgs(requestors, null, true /* fill with nulls */, false);

		// Call requestors in order
		for (Requestor requestor : requestors) {
			requestor.execute();
		}
	}

	public Object invoke(Object object, Class<? extends Annotation> qualifier, PrimaryObjectSupplier objectSupplier) {
		Object result = invokeUsingClass(object, object.getClass(), qualifier, IInjector.NOT_A_VALUE, objectSupplier, true);
		if (result == IInjector.NOT_A_VALUE)
			throw new InjectionException("Unable to find matching method to invoke"); //$NON-NLS-1$
		return result;
	}

	public Object invoke(Object object, Class<? extends Annotation> qualifier, Object defaultValue, PrimaryObjectSupplier objectSupplier) {
		return invokeUsingClass(object, object.getClass(), qualifier, defaultValue, objectSupplier, false);
	}

	private Object invokeUsingClass(Object userObject, Class<?> currentClass, Class<? extends Annotation> qualifier, Object defaultValue, PrimaryObjectSupplier objectSupplier, boolean throwUnresolved) {
		Method[] methods = currentClass.getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
			Method method = methods[j];
			if (method.getAnnotation(qualifier) == null)
				continue;
			MethodRequestor requestor = new MethodRequestor(method, this, objectSupplier, userObject, false);

			Object[] actualArgs = resolveArgs(requestor, objectSupplier, false, false);
			int unresolved = unresolved(actualArgs);
			if (unresolved != -1) {
				if (throwUnresolved)
					reportUnresolvedArgument(requestor, unresolved);
				continue;
			}
			requestor.setResolvedArgs(actualArgs);
			return requestor.execute();
		}
		Class<?> superClass = currentClass.getSuperclass();
		if (superClass == null)
			return defaultValue;

		return invokeUsingClass(userObject, superClass, qualifier, defaultValue, objectSupplier, throwUnresolved);
	}

	public <T> T make(Class<T> clazz, PrimaryObjectSupplier objectSupplier) {
		IObjectDescriptor descriptor = new ObjectDescriptor(clazz, null);
		Binding binding = findBinding(descriptor);
		Class<?> implementationClass;
		if (binding == null)
			implementationClass = getDesiredClass(descriptor.getDesiredType());
		else
			implementationClass = binding.getImplementationClass();
		return clazz.cast(internalMake(implementationClass, objectSupplier));
	}

	public Object makeFromProvider(IObjectDescriptor descriptor, PrimaryObjectSupplier objectSupplier) {
		Binding binding = findBinding(descriptor);
		Class<?> implementationClass;
		if (binding == null)
			implementationClass = getProviderType(descriptor.getDesiredType());
		else
			implementationClass = binding.getImplementationClass();
		if (objectSupplier != null) {
			IObjectDescriptor actualClass = new ObjectDescriptor(implementationClass, null);
			Object[] result = objectSupplier.get(new IObjectDescriptor[] {actualClass}, null, false, false);
			if (result[0] != IInjector.NOT_A_VALUE)
				return result[0];
		}
		return internalMake(implementationClass, objectSupplier);
	}

	private Object internalMake(Class<?> clazz, PrimaryObjectSupplier objectSupplier) {

		boolean isSingleton = clazz.isAnnotationPresent(Singleton.class);
		if (isSingleton) {
			synchronized (singletonCache) {
				if (singletonCache.containsKey(clazz))
					return singletonCache.get(clazz);
			}
		}

		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		// Sort the constructors by descending number of constructor arguments
		ArrayList<Constructor<?>> sortedConstructors = new ArrayList<Constructor<?>>(constructors.length);
		for (Constructor<?> constructor : constructors)
			sortedConstructors.add(constructor);
		Collections.sort(sortedConstructors, new Comparator<Constructor<?>>() {
			public int compare(Constructor<?> c1, Constructor<?> c2) {
				int l1 = c1.getParameterTypes().length;
				int l2 = c2.getParameterTypes().length;
				return l2 - l1;
			}
		});

		for (Constructor<?> constructor : sortedConstructors) {
			// skip private and protected constructors; allow public and package visibility
			int modifiers = constructor.getModifiers();
			if (((modifiers & Modifier.PRIVATE) != 0) || ((modifiers & Modifier.PROTECTED) != 0))
				continue;

			// unless this is the default constructor, it has to be tagged
			if (!constructor.isAnnotationPresent(Inject.class) && constructor.getParameterTypes().length != 0)
				continue;

			ConstructorRequestor requestor = new ConstructorRequestor(constructor, this, objectSupplier);
			Object[] actualArgs = resolveArgs(requestor, objectSupplier, false, false);
			if (unresolved(actualArgs) != -1)
				continue;
			requestor.setResolvedArgs(actualArgs);

			Object newInstance = requestor.execute();
			if (newInstance != null) {
				inject(newInstance, objectSupplier);
				if (isSingleton) {
					synchronized (singletonCache) { // TBD this is not quite right, synch the method
						singletonCache.put(clazz, newInstance);
					}
				}
				return newInstance;
			}
		}
		throw new InjectionException("Could not find satisfiable constructor in " + clazz.getName()); //$NON-NLS-1$
	}

	public void resolveArguments(IRequestor requestor) {
		ArrayList<Requestor> list = new ArrayList<Requestor>(1);
		list.add((Requestor) requestor);
		resolveRequestorArgs(list, ((Requestor) requestor).getPrimarySupplier(), true, false /* no nested track */);
	}

	public void disposed(PrimaryObjectSupplier objectSupplier) {
		List<WeakReference<?>> references = getSupplierObjects(objectSupplier);
		if (references == null)
			return;
		Object[] objects = new Object[references.size()];
		int count = 0;
		for (WeakReference<?> ref : references) {
			Object object = ref.get();
			if (object != null) {
				objects[count] = object;
				count++;
			}
		}
		for (int i = 0; i < count; i++) {
			processAnnotated(PreDestroy.class, objects[i], objects[i].getClass(), objectSupplier, new ArrayList<Class<?>>(5));
			if (objects[i] instanceof IDisposable)
				((IDisposable) objects[i]).dispose();
			uninject(objects[i], objectSupplier);
		}
		forgetSupplier(objectSupplier);
	}

	private void resolveRequestorArgs(ArrayList<Requestor> requestors, PrimaryObjectSupplier objectSupplier, boolean fillNulls, boolean track) {
		for (Requestor requestor : requestors) {
			Object[] actualArgs = resolveArgs(requestor, objectSupplier, fillNulls, track);
			int unresolved = unresolved(actualArgs);
			if (unresolved == -1) {
				requestor.setResolvedArgs(actualArgs);
				continue;
			}

			if (requestor.isOptional())
				requestor.setResolvedArgs(null);
			else
				reportUnresolvedArgument(requestor, unresolved);
		}
	}

	private void reportUnresolvedArgument(Requestor requestor, int argIndex) {
		StringBuffer tmp = new StringBuffer();
		tmp.append("Unable to process \""); //$NON-NLS-1$
		tmp.append(requestor.toString());
		tmp.append("\": no actual value was found for the argument \""); //$NON-NLS-1$
		tmp.append(requestor.getDependentObjects()[argIndex].toString());
		tmp.append("\"."); //$NON-NLS-1$
		throw new InjectionException(tmp.toString());
	}

	private Object[] resolveArgs(Requestor requestor, PrimaryObjectSupplier objectSupplier, boolean fillNulls, boolean track) {
		IObjectDescriptor[] descriptors = requestor.getDependentObjects();

		// 0) initial fill - all are unresolved
		Object[] actualArgs = new Object[descriptors.length];
		for (int i = 0; i < actualArgs.length; i++) {
			actualArgs[i] = NOT_A_VALUE;
		}

		// 1) check if we have a Provider<T>
		for (int i = 0; i < actualArgs.length; i++) {
			Class<?> providerClass = getProviderType(descriptors[i].getDesiredType());
			if (providerClass == null)
				continue;
			actualArgs[i] = new ProviderImpl<Class<?>>(descriptors[i], this, objectSupplier);
			descriptors[i] = null; // mark as used
		}

		// 2) use the primary supplier
		if (objectSupplier != null) {
			Object[] primarySupplierArgs = objectSupplier.get(descriptors, requestor, requestor.shouldTrack() && track, requestor.shouldGroupUpdates());
			for (int i = 0; i < actualArgs.length; i++) {
				if (descriptors[i] == null)
					continue; // already resolved
				if (primarySupplierArgs[i] != NOT_A_VALUE) {
					actualArgs[i] = primarySupplierArgs[i];
					descriptors[i] = null; // mark as used
				}
			}
		}

		// 3) try extended suppliers
		for (int i = 0; i < actualArgs.length; i++) {
			if (descriptors[i] == null)
				continue; // already resolved
			ExtendedObjectSupplier extendedSupplier = findExtendedSupplier(descriptors[i], objectSupplier);
			if (extendedSupplier == null)
				continue;
			Object result = extendedSupplier.get(descriptors[i], requestor, requestor.shouldTrack() && track, requestor.shouldGroupUpdates());
			if (result != NOT_A_VALUE) {
				actualArgs[i] = result;
				descriptors[i] = null; // mark as used
			}
		}

		// 4) try the bindings
		for (int i = 0; i < actualArgs.length; i++) {
			if (descriptors[i] == null)
				continue; // already resolved
			Binding binding = findBinding(descriptors[i]);
			if (binding != null) {
				actualArgs[i] = internalMake(binding.getImplementationClass(), objectSupplier);
				if (actualArgs[i] != NOT_A_VALUE)
					descriptors[i] = null; // mark as used
			}
		}

		// 5) create simple classes (implied bindings) - unless we uninject or optional
		if (!fillNulls && !requestor.isOptional()) {
			for (int i = 0; i < actualArgs.length; i++) {
				if (descriptors[i] == null)
					continue; // already resolved
				if (descriptors[i].hasQualifier(Optional.class))
					continue;
				Object result = null;
				try {
					result = internalMake(getDesiredClass(descriptors[i].getDesiredType()), objectSupplier);
				} catch (InjectionException e) {
					// ignore
				}
				if (result != null && result != NOT_A_VALUE) {
					actualArgs[i] = result;
					descriptors[i] = null; // mark as used
				}
			}
		}

		// 6) post process
		descriptors = requestor.getDependentObjects(); // reset nulled out values
		for (int i = 0; i < descriptors.length; i++) {
			// check that values are of a correct type
			if (actualArgs[i] != null && actualArgs[i] != IInjector.NOT_A_VALUE) {
				Class<?> descriptorsClass = getDesiredClass(descriptors[i].getDesiredType());
				if (descriptorsClass.isPrimitive()) { // support type autoboxing
					if (descriptorsClass.equals(boolean.class))
						descriptorsClass = Boolean.class;
					else if (descriptorsClass.equals(int.class))
						descriptorsClass = Integer.class;
					else if (descriptorsClass.equals(char.class))
						descriptorsClass = Character.class;
					else if (descriptorsClass.equals(float.class))
						descriptorsClass = Float.class;
					else if (descriptorsClass.equals(double.class))
						descriptorsClass = Double.class;
					else if (descriptorsClass.equals(long.class))
						descriptorsClass = Long.class;
					else if (descriptorsClass.equals(short.class))
						descriptorsClass = Short.class;
					else if (descriptorsClass.equals(byte.class))
						descriptorsClass = Byte.class;
				}
				if (!descriptorsClass.isAssignableFrom(actualArgs[i].getClass()))
					actualArgs[i] = IInjector.NOT_A_VALUE;
			}
			// replace optional unresolved values with null
			if (actualArgs[i] == IInjector.NOT_A_VALUE && descriptors[i].hasQualifier(Optional.class))
				actualArgs[i] = null;
			else if (fillNulls && actualArgs[i] == IInjector.NOT_A_VALUE)
				actualArgs[i] = null;
		}

		return actualArgs;
	}

	private ExtendedObjectSupplier findExtendedSupplier(IObjectDescriptor descriptor, PrimaryObjectSupplier objectSupplier) {
		Annotation[] qualifiers = descriptor.getQualifiers();
		if (qualifiers == null)
			return null;
		for (Annotation qualifier : qualifiers) {
			String key;
			Type type = qualifier.annotationType();
			if (type instanceof Class<?>) {
				key = ((Class<?>) type).getName();
			} else
				continue;

			ExtendedObjectSupplier supplier;
			try {
				// use qualified name to refer to a class that might be missing
				supplier = org.eclipse.e4.core.internal.di.osgi.ProviderHelper.findProvider(key, objectSupplier);
			} catch (NoClassDefFoundError e) {
				return null; // OSGi framework not present 
			}
			if (supplier != null)
				return supplier;
		}
		return null;
	}

	private int unresolved(Object[] actualArgs) {
		for (int i = 0; i < actualArgs.length; i++) {
			if (actualArgs[i] == IInjector.NOT_A_VALUE)
				return i;
		}
		return -1;
	}

	private void processClassHierarchy(Object userObject, PrimaryObjectSupplier objectSupplier, boolean processStatic, boolean track, boolean normalOrder, List<Requestor> requestors) {
		processClass(userObject, objectSupplier, (userObject == null) ? null : userObject.getClass(), new ArrayList<Class<?>>(5), processStatic, track, normalOrder, requestors);
	}

	/**
	 * Make the processor visit all declared members on the given class and all superclasses
	 */
	private void processClass(Object userObject, PrimaryObjectSupplier objectSupplier, Class<?> objectsClass, ArrayList<Class<?>> classHierarchy, boolean processStatic, boolean track, boolean normalOrder, List<Requestor> requestors) {
		// order: superclass, fields, methods
		if (objectsClass != null) {
			Class<?> superClass = objectsClass.getSuperclass();
			if (superClass != null && !superClass.getName().equals(JAVA_OBJECT)) {
				classHierarchy.add(objectsClass);
				processClass(userObject, objectSupplier, superClass, classHierarchy, processStatic, track, normalOrder, requestors);
				classHierarchy.remove(objectsClass);
			}
		}
		boolean injectedStaticFields;
		boolean injectedStaticMethods;
		if (normalOrder) {
			injectedStaticFields = processFields(userObject, objectSupplier, objectsClass, processStatic, track, requestors);
			injectedStaticMethods = processMethods(userObject, objectSupplier, objectsClass, classHierarchy, processStatic, track, requestors);
		} else {
			injectedStaticMethods = processMethods(userObject, objectSupplier, objectsClass, classHierarchy, processStatic, track, requestors);
			injectedStaticFields = processFields(userObject, objectSupplier, objectsClass, processStatic, track, requestors);
		}
		if (injectedStaticFields || injectedStaticMethods)
			rememberInjectedStatic(objectsClass);
	}

	private boolean hasInjectedStatic(Class<?> objectsClass) {
		synchronized (injectedClasses) {
			for (WeakReference<Class<?>> ref : injectedClasses) {
				Class<?> injectedClass = ref.get();
				if (injectedClass == null)
					continue;
				if (injectedClass == objectsClass) // use pointer comparison
					return true;
			}
			return false;
		}
	}

	private void rememberInjectedStatic(Class<?> objectsClass) {
		synchronized (injectedClasses) {
			injectedClasses.add(new WeakReference<Class<?>>(objectsClass));
		}
	}

	/**
	 * Make the processor visit all declared fields on the given class.
	 */
	private boolean processFields(Object userObject, PrimaryObjectSupplier objectSupplier, Class<?> objectsClass, boolean processStatic, boolean track, List<Requestor> requestors) {
		boolean injectedStatic = false;
		Field[] fields = objectsClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (Modifier.isStatic(field.getModifiers())) {
				if (hasInjectedStatic(objectsClass))
					continue;
				injectedStatic = true;
			}
			if (!field.isAnnotationPresent(Inject.class))
				continue;
			requestors.add(new FieldRequestor(field, this, objectSupplier, userObject, track));
		}
		return injectedStatic;
	}

	/**
	 * Make the processor visit all declared methods on the given class.
	 */
	private boolean processMethods(final Object userObject, PrimaryObjectSupplier objectSupplier, Class<?> objectsClass, ArrayList<Class<?>> classHierarchy, boolean processStatic, boolean track, List<Requestor> requestors) {
		boolean injectedStatic = false;
		Method[] methods = objectsClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			final Method method = methods[i];
			if (isOverridden(method, classHierarchy))
				continue; // process in the subclass
			if (Modifier.isStatic(method.getModifiers())) {
				if (hasInjectedStatic(objectsClass))
					continue;
				injectedStatic = true;
			}
			if (!method.isAnnotationPresent(Inject.class))
				continue;
			requestors.add(new MethodRequestor(method, this, objectSupplier, userObject, track));
		}
		return injectedStatic;
	}

	/**
	 * Checks if a given method is overridden with an injectable method.
	 */
	private boolean isOverridden(Method method, ArrayList<Class<?>> classHierarchy) {
		int modifiers = method.getModifiers();
		if (Modifier.isPrivate(modifiers))
			return false;
		if (Modifier.isStatic(modifiers))
			return false;
		// method is not private if we reached this line, check not(public OR protected)
		boolean isDefault = !(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers));

		for (Class<?> subClass : classHierarchy) {
			try {
				subClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
			} catch (SecurityException e) {
				continue;
			} catch (NoSuchMethodException e) {
				continue; // this is the desired outcome
			}
			if (isDefault) { // must be in the same package to override
				Package originalPackage = method.getDeclaringClass().getPackage();
				Package overridePackage = subClass.getPackage();

				if (originalPackage == null && overridePackage == null)
					return true;
				if (originalPackage == null || overridePackage == null)
					return false;
				if (originalPackage.equals(overridePackage))
					return true;
			} else
				return true;
		}
		return false;
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

	/**
	 * Returns null if not a provider
	 */
	private Class<?> getProviderType(Type type) {
		if (!(type instanceof ParameterizedType))
			return null;
		Type rawType = ((ParameterizedType) type).getRawType();
		if (!Provider.class.equals(rawType))
			return null;
		Type[] actualTypes = ((ParameterizedType) type).getActualTypeArguments();
		if (actualTypes.length != 1)
			return null;
		if (!(actualTypes[0] instanceof Class<?>))
			return null;
		return (Class<?>) actualTypes[0];
	}

	public IBinding addBinding(Class<?> clazz) {
		return addBinding(new Binding(clazz, this));
	}

	public IBinding addBinding(IBinding binding) {
		Binding internalBinding = (Binding) binding;
		Class<?> clazz = internalBinding.getDescribedClass();
		synchronized (bindings) {
			if (bindings.containsKey(clazz)) {
				Set<Binding> collection = bindings.get(clazz);
				String desiredQualifierName = internalBinding.getQualifierName();
				for (Iterator<Binding> i = collection.iterator(); i.hasNext();) {
					Binding collectionBinding = i.next();
					if (eq(collectionBinding.getQualifierName(), desiredQualifierName)) {
						i.remove();
						break;
					}
				}
				collection.add(internalBinding);
			} else {
				Set<Binding> collection = new HashSet<Binding>(1);
				collection.add(internalBinding);
				bindings.put(clazz, collection);
			}
		}
		return binding;
	}

	private Binding findBinding(IObjectDescriptor descriptor) {
		Class<?> desiredClass = getProviderType(descriptor.getDesiredType());
		if (desiredClass == null)
			desiredClass = getDesiredClass(descriptor.getDesiredType());
		synchronized (bindings) {
			if (!bindings.containsKey(desiredClass))
				return null;
			Set<Binding> collection = bindings.get(desiredClass);
			String desiredQualifierName = null;
			if (descriptor.hasQualifier(Named.class)) {
				Named namedAnnotation = descriptor.getQualifier(Named.class);
				desiredQualifierName = namedAnnotation.value();
			} else {
				Annotation[] annotations = descriptor.getQualifiers();
				if (annotations != null) {
					for (Annotation annotation : annotations) {
						desiredQualifierName = annotation.annotationType().getName();
						break;
					}
				}
			}

			for (Iterator<Binding> i = collection.iterator(); i.hasNext();) {
				Binding collectionBinding = i.next();
				if (eq(collectionBinding.getQualifierName(), desiredQualifierName))
					return collectionBinding;
			}
		}
		return null;
	}

	/**
	 * Are two, possibly null, string equal?
	 */
	private boolean eq(String str1, String str2) {
		if (str1 == null && str2 == null)
			return true;
		if (str1 == null || str2 == null)
			return false;
		return str1.equals(str2);
	}

	private void processAnnotated(Class<? extends Annotation> annotation, Object userObject, Class<?> objectClass, PrimaryObjectSupplier objectSupplier, ArrayList<Class<?>> classHierarchy) {
		Class<?> superClass = objectClass.getSuperclass();
		if (superClass != null && !superClass.getName().equals(JAVA_OBJECT)) {
			classHierarchy.add(objectClass);
			processAnnotated(annotation, userObject, superClass, objectSupplier, classHierarchy);
			classHierarchy.remove(objectClass);
		}
		Method[] methods = objectClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (!method.isAnnotationPresent(annotation))
				continue;
			if (isOverridden(method, classHierarchy))
				continue;

			MethodRequestor requestor = new MethodRequestor(method, this, objectSupplier, userObject, false);
			Object[] actualArgs = resolveArgs(requestor, objectSupplier, false, false);
			int unresolved = unresolved(actualArgs);
			if (unresolved != -1) {
				if (method.isAnnotationPresent(Optional.class))
					continue;
				reportUnresolvedArgument(requestor, unresolved);
			}
			requestor.setResolvedArgs(actualArgs);
			requestor.execute();
		}
	}

}
