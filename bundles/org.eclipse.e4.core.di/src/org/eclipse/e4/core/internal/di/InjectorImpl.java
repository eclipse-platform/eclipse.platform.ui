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
import java.lang.reflect.InvocationTargetException;
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

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.AbstractObjectSupplier;
import org.eclipse.e4.core.di.IBinding;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.IRequestor;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.ObjectDescriptorFactory;
import org.eclipse.e4.core.internal.di.osgi.ProviderHelper;

/**
 * Reflection-based dependency injector.
 */
public class InjectorImpl implements IInjector {

	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$
	// XXX remove
	// plug-in class that gets replaced in Java 1.5+
	final private static AnnotationsSupport annotationSupport = new AnnotationsSupport();

	// TBD thread safety
	private Map<String, AbstractObjectSupplier> extendedSuppliers = new HashMap<String, AbstractObjectSupplier>();

	private Map<AbstractObjectSupplier, List<WeakReference<?>>> injectedObjects = new HashMap<AbstractObjectSupplier, List<WeakReference<?>>>();
	private HashMap<Class<?>, Object> singletonCache = new HashMap<Class<?>, Object>();
	private Map<Class<?>, Set<IBinding>> bindings = new HashMap<Class<?>, Set<IBinding>>();

	public boolean inject(Object object, AbstractObjectSupplier objectSupplier) {
		// Two stages: first, go and collect {requestor, descriptor[] }
		ArrayList<Requestor> requestors = new ArrayList<Requestor>();
		processClassHierarchy(object, objectSupplier, false /* no static */, true /* track */, true /* normal order */,
				requestors);
		// Ask suppliers to fill actual values {requestor, descriptor[], actualvalues[] }
		if (!resolveRequestorArgs(requestors, objectSupplier, false))
			return false;

		// Call requestors in order
		for (Requestor requestor : requestors) {
			try {
				if (requestor.isResolved())
					requestor.execute();
			} catch (InvocationTargetException e) {
				logError("Injection failed for the object \"" + object.toString()
						+ "\". Unable to execute \"" + requestor.toString() + "\"");
				return false;
			} catch (InstantiationException e) {
				logError("Injection failed for the object \"" + object.toString()
						+ "\". Unable to execute \"" + requestor.toString() + "\"");
				return false;
			}
		}
		rememberInjectedObject(object, objectSupplier);

		// TBD current tests assume that @PostConstruct methods will be
		// called after injection; however, name implies that it is only
		// called when the object is constructed. Fix this after the 1.4/1.5 merge.
		try {
			processPostConstruct(object, object.getClass(), objectSupplier,
					new ArrayList<Class<?>>(5));
		} catch (InvocationTargetException e) {
			logError("Injection failed for the object \"" + object.toString()
					+ "\". Unable to process post-construct methods.", e);
			return false;
		} catch (InstantiationException e) {
			logError("Injection failed for the object \"" + object.toString()
					+ "\". Unable to process post-construct methods.", e);
			return false;
		}
		return true;
	}

	private void rememberInjectedObject(Object object, AbstractObjectSupplier objectSupplier) {
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

	private boolean forgetInjectedObject(Object object, AbstractObjectSupplier objectSupplier) {
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

	private List<WeakReference<?>> forgetSupplier(AbstractObjectSupplier objectSupplier) {
		synchronized (injectedObjects) {
			if (!injectedObjects.containsKey(objectSupplier))
				return null;
			return injectedObjects.remove(objectSupplier);
		}
	}

	private List<WeakReference<?>> getSupplierObjects(AbstractObjectSupplier objectSupplier) {
		synchronized (injectedObjects) {
			if (!injectedObjects.containsKey(objectSupplier))
				return null;
			return injectedObjects.get(objectSupplier);
		}
	}

	public boolean uninject(Object object, AbstractObjectSupplier objectSupplier) {
		if (!forgetInjectedObject(object, objectSupplier))
			return false; // not injected at this time
		// Two stages: first, go and collect {requestor, descriptor[] }
		ArrayList<Requestor> requestors = new ArrayList<Requestor>();
		processClassHierarchy(object, objectSupplier, false /* no static */, true /* track */,
				false /* inverse order */, requestors);
		// might still need to get resolved values from secondary suppliers
		// Ask suppliers to fill actual values {requestor, descriptor[], actualvalues[] }
		if (!resolveRequestorArgs(requestors, null, true /* fill with nulls */))
			return false;

		// Call requestors in order
		for (Requestor requestor : requestors) {
			try {
				requestor.execute();
			} catch (InvocationTargetException e) {
				logError("Uninjection failed for the object \"" + object.toString()
						+ "\". Unable to execute \"" + requestor.toString() + "\"");
				return false;
			} catch (InstantiationException e) {
				logError("Uninjection failed for the object \"" + object.toString()
						+ "\". Unable to execute \"" + requestor.toString() + "\"");
				return false;
			}
		}
		return true;
	}

	public Object invoke(Object object, String methodName, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException, InjectionException {
		Object result = invokeUsingClass(object, object.getClass(), methodName,
				IInjector.NOT_A_VALUE, objectSupplier);
		if (result == IInjector.NOT_A_VALUE) {
			throw new InjectionException("Unable to find matching method to invoke");
		}
		return result;
	}

	public Object invoke(Object object, String methodName, Object defaultValue,
			AbstractObjectSupplier objectSupplier) throws InvocationTargetException {
		return invokeUsingClass(object, object.getClass(), methodName, defaultValue, objectSupplier);
	}

	private Object invokeUsingClass(Object userObject, Class<?> currentClass, String methodName,
			Object defaultValue, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException {
		Method[] methods = currentClass.getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
			Method method = methods[j];
			if (!method.getName().equals(methodName))
				continue;
			MethodRequestor requestor = new MethodRequestor(method, this, objectSupplier, userObject, false, false, true);

			Object[] actualArgs = resolveArgs(requestor, objectSupplier, false);
			int unresolved = unresolved(actualArgs);
			if (unresolved != -1) {
				logError("Injection failed for object \""
						+ requestor.getRequestingObject().toString()
						+ "\". Unable to find value for \""
						+ requestor.getDependentObjects()[unresolved] + "\"");
				return null;
			}
			requestor.setResolvedArgs(actualArgs);

			try {
				return requestor.execute();
			} catch (InstantiationException e) {
				// TBD clean up the error handling; need to propagate original exception
				logError("Exception occured in the injected method " + method.getName(), e);
				return null;
			}
		}
		Class<?> superClass = currentClass.getSuperclass();
		if (superClass == null) {
			return defaultValue;
		}
		return invokeUsingClass(userObject, superClass, methodName, defaultValue, objectSupplier);
	}

	public Object make(Class<?> clazz, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException, InstantiationException {
		IObjectDescriptor descriptor = ObjectDescriptorFactory.make(clazz, null);
		return make(descriptor, objectSupplier);
	}

	public Object make(IObjectDescriptor descriptor, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException, InstantiationException {
		IBinding binding = findBinding(descriptor);
		if (binding == null) {
			Class<?> desiredClass = descriptor.getElementClass();
			return internalMake(desiredClass, objectSupplier);
		}
		return internalMake(binding.getImplementationClass(), objectSupplier);
	}

	private Object internalMake(Class<?> clazz, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException, InstantiationException {

		boolean isSingleton = clazz.isAnnotationPresent(Singleton.class);
		if (isSingleton) {
			synchronized (singletonCache) {
				if (singletonCache.containsKey(clazz))
					return singletonCache.get(clazz);
			}
		}

		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		// Sort the constructors by descending number of constructor arguments
		ArrayList<Constructor<?>> sortedConstructors = new ArrayList<Constructor<?>>(
				constructors.length);
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
			InjectionProperties cProps = annotationSupport.getInjectProperties(constructor);
			if (!cProps.shouldInject() && constructor.getParameterTypes().length != 0)
				continue;

			ConstructorRequestor requestor = new ConstructorRequestor(constructor, this, objectSupplier);
			Object[] actualArgs = resolveArgs(requestor, objectSupplier, false);
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

		logError("Could not find satisfiable constructor in class " + clazz.getName());
		return null;
	}

	public boolean injectStatic(Class<?> clazz, AbstractObjectSupplier objectSupplier) {
		// TBD add processing on a null object
		Object object;
		try {
			object = make(clazz, objectSupplier);
		} catch (InvocationTargetException e) {
			// try-catch won't be necessary once we stop creating an object
			e.printStackTrace();
			return false;
		} catch (InstantiationException e) {
			// try-catch won't be necessary once we stop creating an object
			e.printStackTrace();
			return false;
		}

		// TBD this is copy/paste from as invoke() with static = true.
		// Two stages: first, go and collect {requestor, descriptor[] }
		ArrayList<Requestor> requestors = new ArrayList<Requestor>();
		processClassHierarchy(object, objectSupplier, true /* static */, true /* track */, true /* normal order */,
				requestors);
		// Ask suppliers to fill actual values {requestor, descriptor[], actualvalues[] }
		if (!resolveRequestorArgs(requestors, objectSupplier, false))
			return false;

		// Call requestors in order
		for (Requestor requestor : requestors) {
			try {
				requestor.execute();
			} catch (InvocationTargetException e) {
				logError("Injection failed for the object \"" + object.toString()
						+ "\". Unable to execute \"" + requestor.toString() + "\"");
				return false;
			} catch (InstantiationException e) {
				logError("Injection failed for the object \"" + object.toString()
						+ "\". Unable to execute \"" + requestor.toString() + "\"");
				return false;
			}
		}

		return true;
	}

	public boolean update(IRequestor[] requestors, AbstractObjectSupplier objectSupplier) {
		ArrayList<Requestor> list = new ArrayList<Requestor>(requestors.length);
		for (IRequestor requestor : requestors) {
			list.add((Requestor) requestor);
		}
		resolveRequestorArgs(list, objectSupplier, true);

		// Call requestors in order
		for (IRequestor requestor : requestors) {
			try {
				((Requestor) requestor).execute();
			} catch (InvocationTargetException e) {
				logError("Injection failed for the object \""
						+ requestor.getRequestingObject().toString() + "\". Unable to execute \""
						+ requestor.toString() + "\"");
				return false;
			} catch (InstantiationException e) {
				logError("Injection failed for the object \""
						+ requestor.getRequestingObject().toString() + "\". Unable to execute \""
						+ requestor.toString() + "\"");
				return false;
			}
		}
		return true;
	}

	public boolean disposed(AbstractObjectSupplier objectSupplier) {
		List<WeakReference<?>> references = getSupplierObjects(objectSupplier);
		if (references == null)
			return true;
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
			processPreDestory(objects[i], objectSupplier, objects[i].getClass(), new ArrayList<Class<?>>(5));
			uninject(objects[i], objectSupplier);
		}
		forgetSupplier(objectSupplier);
		return true;
	}

	private void processPreDestory(Object userObject, AbstractObjectSupplier objectSupplier, Class<?> objectClass,
			ArrayList<Class<?>> classHierarchy) {
		Class<?> superClass = objectClass.getSuperclass();
		if (superClass != null && !superClass.getName().equals(JAVA_OBJECT)) {
			classHierarchy.add(objectClass);
			processPreDestory(userObject, objectSupplier, superClass, classHierarchy);
			classHierarchy.remove(objectClass);
		}
		Method[] methods = objectClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getParameterTypes().length > 0) // TBD why?
				continue;
			if (!annotationSupport.isPreDestory(method))
				continue;
			if (!isOverridden(method, classHierarchy)) {
				// TBD optional @PreDestory? might make sense if we allow args on those methods
				MethodRequestor requestor = new MethodRequestor(method, this, objectSupplier, userObject, false, false,
						false);
				try {
					requestor.execute();
				} catch (InvocationTargetException e) {
					logError("Unable to call pre-destory method \"" + method.getName() + "\"", e);
				} catch (InstantiationException e) {
					logError("Unable to call pre-destory method \"" + method.getName() + "\"", e);
				}
			}
		}
		if (userObject instanceof IDisposable)
			((IDisposable) userObject).dispose();
	}

	private boolean resolveRequestorArgs(ArrayList<Requestor> requestors,
			AbstractObjectSupplier objectSupplier, boolean fillNulls) {
		for (Requestor requestor : requestors) {
			Object[] actualArgs = resolveArgs(requestor, objectSupplier, fillNulls);
			int unresolved = unresolved(actualArgs);
			if (unresolved == -1) {
				requestor.setResolvedArgs(actualArgs);
				continue;
			}

			if (requestor.isOptional())
				requestor.setResolvedArgs(null);
			else {
				logError("Injection failed for object \""
						+ requestor.getRequestingObject().toString()
						+ "\". Unable to find value for \""
						+ requestor.getDependentObjects()[unresolved] + "\"");
				return false;
			}
		}
		return true;
	}

	private Object[] resolveArgs(Requestor requestor, AbstractObjectSupplier objectSupplier,
			boolean fillNulls) {
		IObjectDescriptor[] descriptors = requestor.getDependentObjects();

		// 0) initial fill - all are unresolved
		Object[] actualArgs = new Object[descriptors.length];
		for (int i = 0; i < actualArgs.length; i++) {
			actualArgs[i] = NOT_A_VALUE;
		}

		// 1) check if we have a Provider<T>
		for (int i = 0; i < actualArgs.length; i++) {
			Class<?> providerClass = getProviderType(descriptors[i].getElementType());
			if (providerClass == null)
				continue;
			actualArgs[i] = new ProviderImpl<Class<?>>(descriptors[i], this, objectSupplier);
			descriptors[i] = null; // mark as used
		}

		// 2) use the primary supplier
		if (objectSupplier != null) {
			Object[] primarySupplierArgs = objectSupplier.get(descriptors, requestor);
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
			AbstractObjectSupplier extendedSupplier = findExtendedSupplier(descriptors[i]);
			if (extendedSupplier == null)
				continue;
			Object result = extendedSupplier.get(descriptors[i], requestor);
			if (result != NOT_A_VALUE) {
				actualArgs[i] = result;
				descriptors[i] = null; // mark as used
			}
		}

		// 4) try the bindings
		for (int i = 0; i < actualArgs.length; i++) {
			if (descriptors[i] == null)
				continue; // already resolved
			IBinding binding = findBinding(descriptors[i]);
			if (binding != null) {
				try {
					actualArgs[i] = internalMake(binding.getImplementationClass(), objectSupplier);
				} catch (InvocationTargetException e) {
					logError("Unable to create object for class \""
							+ binding.getImplementationClass() + "\".", e);
					continue;
				} catch (InstantiationException e) {
					logError("Unable to create object for class \""
							+ binding.getImplementationClass() + "\".", e);
					continue;
				}
			}
		}

		// 5) post process
		descriptors = requestor.getDependentObjects(); // reset nulled out values
		for (int i = 0; i < descriptors.length; i++) {
			// check that values are of a correct type
			if (actualArgs[i] != null && actualArgs[i] != IInjector.NOT_A_VALUE) {
				Class<?> descriptorsClass = descriptors[i].getElementClass();
				if (!descriptorsClass.isAssignableFrom(actualArgs[i].getClass()))
					actualArgs[i] = IInjector.NOT_A_VALUE;
			}
			// replace optional unresolved values with null
			if (descriptors[i].isOptional() && actualArgs[i] == IInjector.NOT_A_VALUE)
				actualArgs[i] = null;
			else if (fillNulls && actualArgs[i] == IInjector.NOT_A_VALUE)
				actualArgs[i] = null;
		}

		return actualArgs;
	}

	private AbstractObjectSupplier findExtendedSupplier(IObjectDescriptor descriptor) {
		Annotation[] qualifiers = descriptor.getQualifiers();
		if (qualifiers == null)
			return null;
		for (Annotation qualifier : qualifiers) {
			// TBD wrap in class-not-found if no OSGi
			String key;
			Type type = qualifier.annotationType();
			if (type instanceof Class<?>) {
				key = ((Class<?>)type).getName();
			} else
				continue;
			
			AbstractObjectSupplier supplier = ProviderHelper.findProvider(key);
			if (supplier != null)
				return supplier;
			// TBD use cache
//			if (extendedSuppliers.containsKey(qualifier))
//				return extendedSuppliers.get(qualifier);
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

	private void processClassHierarchy(Object userObject, AbstractObjectSupplier objectSupplier, boolean processStatic, boolean track,
			boolean normalOrder, List<Requestor> requestors) {
		processClass(userObject, objectSupplier, (userObject == null) ? null : userObject.getClass(),
				new ArrayList<Class<?>>(5), processStatic, track, normalOrder, requestors);
	}

	/**
	 * Make the processor visit all declared members on the given class and all superclasses
	 */
	private void processClass(Object userObject, AbstractObjectSupplier objectSupplier, Class<?> objectsClass,
			ArrayList<Class<?>> classHierarchy, boolean processStatic, boolean track,
			boolean normalOrder, List<Requestor> requestors) {
		// order: superclass, fields, methods
		if (objectsClass != null) {
			Class<?> superClass = objectsClass.getSuperclass();
			if (!superClass.getName().equals(JAVA_OBJECT)) {
				classHierarchy.add(objectsClass);
				processClass(userObject, objectSupplier, superClass, classHierarchy, processStatic, track,
						normalOrder, requestors);
				classHierarchy.remove(objectsClass);
			}
		}
		if (normalOrder) {
			processFields(userObject, objectSupplier, objectsClass, processStatic, track, requestors);
			processMethods(userObject, objectSupplier, objectsClass, classHierarchy, processStatic, track,
					requestors);
		} else {
			processMethods(userObject, objectSupplier, objectsClass, classHierarchy, processStatic, track,
					requestors);
			processFields(userObject, objectSupplier, objectsClass, processStatic, track, requestors);
		}
	}

	/**
	 * Make the processor visit all declared fields on the given class.
	 */
	private void processFields(Object userObject, AbstractObjectSupplier objectSupplier, Class<?> objectsClass, boolean processStatic,
			boolean track, List<Requestor> requestors) {
		Field[] fields = objectsClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (Modifier.isStatic(field.getModifiers()) != processStatic)
				continue;

			// XXX limit to "should inject"
			InjectionProperties properties = annotationSupport.getInjectProperties(field);
			// XXX this will be removed on 1.4/1.5 merge
			if (!properties.shouldInject())
				continue;

			requestors.add(new FieldRequestor(field, this, objectSupplier, userObject, track, properties.groupUpdates(),
					properties.isOptional()));
		}
	}

	/**
	 * Make the processor visit all declared methods on the given class.
	 * 
	 * @throws InvocationTargetException
	 */
	private void processMethods(final Object userObject, AbstractObjectSupplier objectSupplier, Class<?> objectsClass,
			ArrayList<Class<?>> classHierarchy, boolean processStatic, boolean track,
			List<Requestor> requestors) {
		Method[] methods = objectsClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			final Method method = methods[i];
			if (isOverridden(method, classHierarchy))
				continue; // process in the subclass
			if (Modifier.isStatic(method.getModifiers()) != processStatic)
				continue;

			InjectionProperties properties = annotationSupport.getInjectProperties(method);

			// replace with @Event("name") qualifier
			// if (properties.getHandlesEvent() != null) {
			// // XXX this is wrong, but it will be removed anyway
			// ObjectDescriptor desc = ObjectDescriptor.make(IEventBroker.class);
			// IEventBroker eventBroker = (IEventBroker) objectProvider.get(desc);
			// eventBroker.subscribe(properties.getHandlesEvent(), null, new EventHandler() {
			// public void handleEvent(Event event) {
			// Object data = event.getProperty(IEventBroker.DATA);
			// boolean wasAccessible = method.isAccessible();
			// if (!wasAccessible) {
			// method.setAccessible(true);
			// }
			// try {
			// method.invoke(userObject, data);
			// } catch (Exception e) {
			// throw new RuntimeException(e);
			// } finally {
			// if (!wasAccessible) {
			// method.setAccessible(false);
			// }
			// }
			// }
			// }, properties.getEventHeadless());
			// continue;
			// }

			if (!properties.shouldInject())
				continue;

			requestors.add(new MethodRequestor(method, this, objectSupplier, userObject, track,
					properties.groupUpdates(), properties.isOptional()));
		}
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

	private void processPostConstruct(Object userObject, Class<?> objectClass,
			AbstractObjectSupplier objectSupplier, ArrayList<Class<?>> classHierarchy)
			throws InvocationTargetException, InstantiationException {
		Class<?> superClass = objectClass.getSuperclass();
		if (superClass != null && !superClass.getName().equals(JAVA_OBJECT)) {
			classHierarchy.add(objectClass);
			processPostConstruct(userObject, superClass, objectSupplier, classHierarchy);
			classHierarchy.remove(objectClass);
		}
		Method[] methods = objectClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (!isPostConstruct(method))
				continue;
			if (isOverridden(method, classHierarchy))
				continue;

			MethodRequestor requestor = new MethodRequestor(method, this, objectSupplier, userObject, false, false, false);
			Object[] actualArgs = resolveArgs(requestor, objectSupplier, false);
			int unresolved = unresolved(actualArgs);
			if (unresolved != -1) {
				logError("Injection failed for object \""
						+ requestor.getRequestingObject().toString()
						+ "\". Unable to find value for \""
						+ requestor.getDependentObjects()[unresolved] + "\"");
				continue;
			}
			requestor.setResolvedArgs(actualArgs);

			try {
				requestor.execute();
			} catch (InvocationTargetException e) {
				logError("Unable to call post-construct method \"" + method.getName() + "\"", e);
			} catch (InstantiationException e) {
				logError("Unable to call post-construct method \"" + method.getName() + "\"", e);
			}
		}
	}

	/**
	 * Returns whether the given method is a post-construction process method, as defined by the
	 * class comment of {@link ContextInjectionFactory}.
	 */
	private boolean isPostConstruct(Method method) {
		return annotationSupport.isPostConstruct(method);
	}

	/**
	 * Returns null if not a provider
	 */
	private Class<?> getProviderType(Type type) {
		if (!(type instanceof ParameterizedType))
			return null;
		Type rawType = ((ParameterizedType) type).getRawType();
		if (!(rawType instanceof Class<?>))
			return null;
		boolean isProvider = ((Class<?>) rawType).equals(Provider.class);
		if (!isProvider)
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
		Class<?> clazz = binding.getDescribedClass();
		synchronized (bindings) {
			if (bindings.containsKey(clazz)) {
				Set<IBinding> collection = bindings.get(clazz);
				String desiredQualifierName = binding.getQualifierName();
				for (Iterator<IBinding> i = collection.iterator(); i.hasNext();) {
					IBinding collectionBinding = i.next();
					if (eq(collectionBinding.getQualifierName(), desiredQualifierName)) {
						i.remove();
						break;
					}
				}
				collection.add(binding);
			} else {
				Set<IBinding> collection = new HashSet<IBinding>(1);
				collection.add(binding);
				bindings.put(clazz, collection);
			}
		}
		return binding;
	}

	private IBinding findBinding(IObjectDescriptor descriptor) {
		Class<?> desiredClass = getProviderType(descriptor.getElementType());
		if (desiredClass == null)
			desiredClass = descriptor.getElementClass();
		synchronized (bindings) {
			if (!bindings.containsKey(desiredClass))
				return null;
			Set<IBinding> collection = bindings.get(desiredClass);
			String desiredQualifierName = null;
			if (descriptor.hasQualifier(Named.class)) {
				Object namedAnnotation = descriptor.getQualifier(Named.class);
				desiredQualifierName = ((Named) namedAnnotation).value();
			} else {
				Annotation[] annotations = descriptor.getQualifiers();
				if (annotations != null) {
					for(Annotation annotation : annotations) {
						desiredQualifierName = annotation.annotationType().getName();
						break;
					}
				}
			}

			for (Iterator<IBinding> i = collection.iterator(); i.hasNext();) {
				IBinding collectionBinding = i.next();
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

	// TBD implement logging
	private void logError(String msg) {
		logError(msg, new InjectionException());
	}

	private void logError(String msg, Throwable e) {
		if (msg != null)
			System.err.println(msg);
		if (e != null)
			e.printStackTrace();
	}

}
