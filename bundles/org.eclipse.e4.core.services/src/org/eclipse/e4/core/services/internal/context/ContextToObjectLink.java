/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;

/**
 * Implements injection of context values into an object. Tracks context changes and makes the
 * corresponding updates to injected objects. See class comment of {@link ContextInjectionFactory}
 * for details on the injection algorithm.
 */
public class ContextToObjectLink implements IRunAndTrack, IContextConstants {

	static class ProcessMethodsResult {
		List outMethods = new ArrayList();
		List postConstructMethods = new ArrayList();
		Set seenMethods = new HashSet();

		boolean seen(Method method) {
			// uniquely identify methods by name+parameter types
			StringBuffer sig = new StringBuffer();
			sig.append(method.getName());
			Class[] parms = method.getParameterTypes();
			for (int i = 0; i < parms.length; i++) {
				sig.append(parms[i]);
				sig.append(',');
			}
			return !seenMethods.add(sig.toString());
		}
	}

	abstract private class Processor {

		protected boolean isSetter;
		protected boolean shouldProcessOutMethods = false;
		protected boolean shouldProcessPostConstruct = false;
		protected Object userObject;

		public Processor(boolean isSetter) {
			this.isSetter = isSetter;
		}

		void processField(Field field, String injectName, boolean optional) {
			// do nothing by default
		}

		void processMethod(Method method, boolean optional) {
			// do nothing by default
		}

		void processOutMethod(Method m, String name) {
			// do nothing by default
		}

		void processPostConstructMethod(Method m) {
			// do nothing by default
		}

		public void setObject(Object userObject) {
			this.userObject = userObject;
		}
	}

	private static class PropertyChangeListenerImplementation implements PropertyChangeListener {
		private final String name;
		private final IEclipseContext outputContext;

		PropertyChangeListenerImplementation(String name, IEclipseContext outputContext) {
			this.name = name;
			this.outputContext = outputContext;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			outputContext.set(name, evt.getNewValue());
		}
	}

	private static final String IN = ".In";//$NON-NLS-1$
	private static final String INJECT = ".Inject";//$NON-NLS-1$
	final static private String JAVA_OBJECT = "java.lang.Object"; //$NON-NLS-1$
	private static final String NAMED = ".Named";//$NON-NLS-1$

	private static final String OUT = ".Out";//$NON-NLS-1$

	private static final String POST_CONSTRUCT = ".PostConstruct";//$NON-NLS-1$

	private static final String PRE_DESTROY = ".PreDestroy";//$NON-NLS-1$

	// annotation names
	private static final String RESOURCE = ".Resource"; //$NON-NLS-1$
	protected IEclipseContext context;

	final protected String fieldPrefix;

	final protected int fieldPrefixLength;

	final protected String setMethodPrefix;

	protected List userObjects = new ArrayList(3); // start small

	public ContextToObjectLink(IEclipseContext context, String fieldPrefix, String setMethodPrefix) {
		this.context = context;
		this.fieldPrefix = (fieldPrefix != null) ? fieldPrefix : INJECTION_FIELD_PREFIX;
		this.setMethodPrefix = (setMethodPrefix != null) ? setMethodPrefix
				: INJECTION_SET_METHOD_PREFIX;

		fieldPrefixLength = this.fieldPrefix.length();
	}

	/**
	 * Calculates alternative spelling of the key: "log" <-> "Log", if any. Returns null if there is
	 * no alternate.
	 */
	protected String altKey(String key) {
		if (key.length() == 0)
			return null;
		char firstChar = key.charAt(0);
		String candidate = null;
		if (Character.isUpperCase(firstChar)) {
			firstChar = Character.toLowerCase(firstChar);
			if (key.length() == 1)
				candidate = Character.toString(firstChar);
			else
				candidate = Character.toString(firstChar) + key.substring(1);
		} else if (Character.isLowerCase(firstChar)) {
			firstChar = Character.toUpperCase(firstChar);
			if (key.length() == 1)
				candidate = Character.toString(firstChar);
			else
				candidate = Character.toString(firstChar) + key.substring(1);
		}
		return candidate;
	}

	void callMethod(Object object, Method m, Object[] args) {
		try {
			if (!m.isAccessible()) {
				m.setAccessible(true);
				try {
					m.invoke(object, args);
				} finally {
					m.setAccessible(false);
				}
			} else {
				m.invoke(object, args);
			}
		} catch (Exception e) {
			logWarning(object, e);
		}
	}

	private void findAndCallDispose(Object object, Class objectsClass, ProcessMethodsResult result) {
		// 1. Try a method with dispose annotation
		Method[] methods = objectsClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getParameterTypes().length > 0)
				continue;
			try {
				Object[] annotations = (Object[]) method.getClass().getMethod("getAnnotations", //$NON-NLS-1$
						new Class[0]).invoke(method, new Object[0]);
				for (int j = 0; j < annotations.length; j++) {
					Object annotation = annotations[j];
					try {
						String annotationName = ((Class) annotation.getClass().getMethod(
								"annotationType", new Class[0]).invoke(annotation, new Object[0])) //$NON-NLS-1$
								.getName();
						if (annotationName.endsWith(PRE_DESTROY)) {
							if (!result.seen(method))
								callMethod(object, method, null);
						}
					} catch (Exception ex) {
						logWarning(method, ex);
					}
				}
			} catch (Exception e) {
				// ignore - no annotation support
			}
		}
		// 2. Try IEclipseContextAware#contextDisposed(IEclipseContext)
		try {
			Method dispose = objectsClass.getDeclaredMethod(
					IContextConstants.INJECTION_DISPOSE_CONTEXT_METHOD,
					new Class[] { IEclipseContext.class });
			// only call this method if we haven't found any other dispose methods yet
			if (result.seenMethods.isEmpty() && !result.seen(dispose))
				callMethod(object, dispose, new Object[] { context });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		// 3. Try contextDisposed()
		try {
			Method dispose = objectsClass.getDeclaredMethod(
					IContextConstants.INJECTION_DISPOSE_CONTEXT_METHOD, new Class[0]);
			// only call this method if we haven't found any other dispose methods yet
			if (result.seenMethods.isEmpty() && !result.seen(dispose))
				callMethod(object, dispose, null);
			return;
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}

		// 4. Try dispose()
		try {
			Method dispose = objectsClass.getDeclaredMethod("dispose", null); //$NON-NLS-1$
			// only call this method if we haven't found any other dispose methods yet
			if (result.seenMethods.isEmpty() && !result.seen(dispose))
				callMethod(object, dispose, null);
			return;
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}

		// 5. Recurse on superclass
		Class superClass = objectsClass.getSuperclass();
		if (!superClass.getName().equals(JAVA_OBJECT)) {
			findAndCallDispose(object, superClass, result);
		}
	}

	protected String findKey(String key, Class clazz) {
		if (context.containsKey(key)) // priority goes to exact match
			return key;
		// alternate capitalization of the first char if possible
		String candidate = altKey(key);
		if (candidate != null) {
			if (context.containsKey(candidate)) {
				return candidate;
			}
		}
		// try type name
		if (context.containsKey(clazz.getName())) {
			return clazz.getName();
		}
		return null;
	}

	private void handleAdd(final ContextChangeEvent event) {
		final String name = event.getName();
		if (EclipseContext.PARENT.equals(name)) {
			handleParentChange(event);
			return;
		}
		Processor processor = new Processor(true) {
			void processField(final Field field, String injectName, boolean optional) {
				String injectKey = findKey(name, field.getType());
				if (injectKey != null
						&& (keyMatches(name, injectName) || field.getType().getName().equals(name)))
					setField(userObject, field, event.getContext().get(injectKey));
			}

			void processMethod(final Method method, boolean optional) {
				String candidateName = method.getName();
				if (candidateName.length() <= setMethodPrefix.length())
					return;
				candidateName = candidateName.substring(setMethodPrefix.length());
				Class[] parameterTypes = method.getParameterTypes();
				// only inject methods with a single parameter
				if (parameterTypes.length != 1)
					return;
				// on add event, only inject the method corresponding to the added context key
				if (keyMatches(name, candidateName)) {
					String key = findKey(name, parameterTypes[0]);
					setMethod(userObject, method, event.getContext().get(key, parameterTypes));
				}
			}
		};
		Object[] objectsCopy = safeObjectsCopy();
		for (int i = 0; i < objectsCopy.length; i++) {
			processClassHierarchy(objectsCopy[i], processor);
		}
	}

	private void handleParentChange(final ContextChangeEvent event) {
		final EclipseContext eventContext = (EclipseContext) event.getContext();
		final EclipseContext oldParent = (EclipseContext) event.getOldValue();
		final EclipseContext newParent = (EclipseContext) eventContext.get(EclipseContext.PARENT);
		if (oldParent == newParent)
			return;
		Processor processor = new Processor(true) {
			/**
			 * Returns whether the value associated with the given key is affected by the parent
			 * change.
			 */
			private boolean hasChanged(String key) {
				// if value is local then parent change has no effect
				if (eventContext.getLocal(key) != null)
					return false;
				Object oldValue = oldParent == null ? null : oldParent.internalGet(eventContext,
						key, null, false);
				Object newValue = newParent == null ? null : newParent.internalGet(eventContext,
						key, null, false);
				return oldValue != newValue;
			}

			void processField(final Field field, String injectName, boolean optional) {
				String key = findKey(injectName, field.getType());
				if (key != null) {
					if (hasChanged(key))
						setField(event.getArguments()[0], field, event.getContext().get(key));
				} else {
					if (!optional) {
						throw new IllegalStateException("Could not set " + field //$NON-NLS-1$
								+ " because of missing: " + injectName); //$NON-NLS-1$
					}
				}
			}

			void processMethod(final Method method, boolean optional) {
				String candidateName = method.getName();
				if (candidateName.length() <= setMethodPrefix.length())
					return;
				candidateName = candidateName.substring(setMethodPrefix.length());
				Class[] parameterTypes = method.getParameterTypes();
				// only inject methods with a single parameter
				if (parameterTypes.length != 1)
					return;
				// when initializing, inject every method that has a match in the context
				String key = findKey(candidateName, parameterTypes[0]);
				if (key != null) {
					if (hasChanged(key))
						setMethod(userObject, method, event.getContext().get(key, parameterTypes));
				} else {
					if (!optional) {
						throw new IllegalStateException("Could not invoke " + method //$NON-NLS-1$
								+ " because of missing: " + candidateName); //$NON-NLS-1$
					}
				}
			}
		};
		Object[] objectsCopy = safeObjectsCopy();
		for (int i = 0; i < objectsCopy.length; i++) {
			processClassHierarchy(objectsCopy[i], processor);
		}
	}

	private void handleDispose(ContextChangeEvent event) {
		for (Iterator it = userObjects.iterator(); it.hasNext();) {
			WeakReference ref = (WeakReference) it.next();
			Object referent = ref.get();
			if (referent != null)
				findAndCallDispose(referent, referent.getClass(), new ProcessMethodsResult());
		}
	}

	private void handleInitial(final ContextChangeEvent event) {
		if (event.getArguments() == null || event.getArguments().length == 0
				|| event.getArguments()[0] == null)
			throw new IllegalArgumentException();
		Processor processor = new Processor(true) {
			void processField(final Field field, String injectName, boolean optional) {
				String key = findKey(injectName, field.getType());
				if (key != null) {
					setField(event.getArguments()[0], field, event.getContext().get(key));
				} else {
					if (!optional) {
						throw new IllegalStateException("Could not set " + field //$NON-NLS-1$
								+ " because of missing: " + injectName); //$NON-NLS-1$
					}
				}
			}

			void processMethod(final Method method, boolean optional) {
				String candidateName = method.getName();
				if (candidateName.length() <= setMethodPrefix.length())
					return;
				candidateName = candidateName.substring(setMethodPrefix.length());
				Class[] parameterTypes = method.getParameterTypes();
				// only inject methods with a single parameter
				if (parameterTypes.length != 1)
					return;
				// when initializing, inject every method that has a match in the context
				String key = findKey(candidateName, parameterTypes[0]);
				if (key != null) {
					setMethod(userObject, method, event.getContext().get(key, parameterTypes));
				} else {
					if (!optional) {
						throw new IllegalStateException("Could not invoke " + method //$NON-NLS-1$
								+ " because of missing: " + candidateName); //$NON-NLS-1$
					}
				}
			}

			public void processOutMethod(Method m, final String name) {
				final EclipseContext outputContext = (EclipseContext) event.getContext().get(
						IContextConstants.OUTPUTS);
				if (outputContext == null) {
					throw new IllegalStateException("No output context available for @Out " + m //$NON-NLS-1$
							+ " in " + userObject); //$NON-NLS-1$
				}
				final Object value;
				try {
					if (!m.isAccessible()) {
						m.setAccessible(true);
						try {
							value = m.invoke(userObject, new Object[0]);
						} finally {
							m.setAccessible(false);
						}
					} else {
						value = m.invoke(userObject, new Object[0]);
					}
					// this has to be done asynchronously, see bug 281659
					outputContext.schedule(new Runnable() {
						public void run() {
							outputContext.set(name, value);
						}
					});

					Method addListener = userObject.getClass().getMethod(
							"addPropertyChangeListener", //$NON-NLS-1$
							new Class[] { String.class, PropertyChangeListener.class });
					callMethod(userObject, addListener, new Object[] { name,
							new PropertyChangeListenerImplementation(name, outputContext) });
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			void processPostConstructMethod(Method m) {
				Object[] methodArgs = null;
				if (m.getParameterTypes().length == 1)
					methodArgs = new Object[] { context };
				try {
					if (!m.isAccessible()) {
						m.setAccessible(true);
						try {
							m.invoke(userObject, methodArgs);
						} finally {
							m.setAccessible(false);
						}
					} else {
						m.invoke(userObject, methodArgs);
					}
				} catch (Exception e) {
					logWarning(userObject, e);
				}
			}
		};
		processor.shouldProcessOutMethods = true;
		processor.shouldProcessPostConstruct = true;
		processClassHierarchy(event.getArguments()[0], processor);

		WeakReference ref = new WeakReference(event.getArguments()[0]);
		synchronized (userObjects) {
			userObjects.add(ref);
		}
	}

	private void handleRemove(final ContextChangeEvent event) {
		final String name = event.getName();
		if (EclipseContext.PARENT.equals(name)) {
			handleParentChange(event);
			return;
		}
		Processor processor = new Processor(false) {
			void processField(final Field field, String injectName, boolean optional) {
				if (keyMatches(name, injectName) || field.getType().getName().equals(name))
					setField(userObject, field, null);
			}

			void processMethod(final Method method, boolean optional) {
				String candidateName = method.getName();
				if (candidateName.length() <= setMethodPrefix.length())
					return;
				candidateName = candidateName.substring(setMethodPrefix.length());
				Class[] parameterTypes = method.getParameterTypes();
				// only inject methods with a single parameter
				if (parameterTypes.length != 1)
					return;
				if (keyMatches(name, candidateName))
					setMethod(userObject, method, null);
			}
		};
		Object[] objectsCopy = safeObjectsCopy();
		for (int i = 0; i < objectsCopy.length; i++) {
			processClassHierarchy(objectsCopy[i], processor);
		}
	}

	/**
	 * Returns whether the given method is a post-construction process method, as defined by the
	 * class comment of {@link ContextInjectionFactory}.
	 */
	private boolean isPostConstruct(Method method) {
		if (!method.getName().equals(IContextConstants.INJECTION_SET_CONTEXT_METHOD))
			return false;
		Class[] parms = method.getParameterTypes();
		if (parms.length == 0)
			return true;
		if (parms.length == 1 && parms[0].equals(IEclipseContext.class))
			return true;
		return false;
	}

	protected boolean keyMatches(String key1, String key2) {
		if (key1 == null && key2 == null)
			return true;
		if (key1 == null || key2 == null)
			return false;
		if (key1.equals(key2))
			return true;
		String candidate = altKey(key2);
		if (candidate == null) // no alternative spellings
			return false;
		return key1.equals(candidate);
	}

	void logWarning(Object destination, Exception e) {
		System.out.println("Injection failed " + destination.toString()); //$NON-NLS-1$
		if (e != null)
			e.printStackTrace();
		// TBD convert this into real logging
		// String msg = NLS.bind("Injection failed", destination.toString());
		// RuntimeLog.log(new Status(IStatus.WARNING,
		// IRuntimeConstants.PI_COMMON, 0, msg, e));
	}

	public boolean notify(final ContextChangeEvent event) {
		switch (event.getEventType()) {
		case ContextChangeEvent.INITIAL:
			handleInitial(event);
			break;
		case ContextChangeEvent.ADDED:
			handleAdd(event);
			break;
		case ContextChangeEvent.REMOVED:
			handleRemove(event);
			break;
		case ContextChangeEvent.DISPOSE:
			handleDispose(event);
			break;
		}
		return (!userObjects.isEmpty());
	}

	/**
	 * Make the processor visit all declared members on the given class and all superclasses
	 */
	private void processClass(Class objectsClass, Processor processor, ProcessMethodsResult result) {
		if (processor.isSetter) {
			processFields(objectsClass, processor);
			processMethods(objectsClass, processor, result);
		} else {
			processMethods(objectsClass, processor, result);
			processFields(objectsClass, processor);
		}
		// recurse on superclass
		Class superClass = objectsClass.getSuperclass();
		if (!superClass.getName().equals(JAVA_OBJECT)) {
			processClass(superClass, processor, result);
		}
	}

	/**
	 * For setters: we set fields first, them methods. Otherwise, clear methods first, fields next
	 */
	private void processClassHierarchy(Object userObject, Processor processor) {
		processor.setObject(userObject);
		Class objectsClass = userObject.getClass();
		ProcessMethodsResult processMethodsResult = new ProcessMethodsResult();
		processClass(objectsClass, processor, processMethodsResult);
		if (processor.shouldProcessPostConstruct) {
			for (Iterator it = processMethodsResult.postConstructMethods.iterator(); it.hasNext();) {
				Method m = (Method) it.next();
				processor.processPostConstructMethod(m);
			}
		}
		if (!processor.shouldProcessOutMethods)
			return;
		for (Iterator it = processMethodsResult.outMethods.iterator(); it.hasNext();) {
			Method m = (Method) it.next();
			String name = m.getName();
			if (name.startsWith("get") && name.length() > 3) { //$NON-NLS-1$
				name = name.substring(3);
				char firstChar = name.charAt(0);
				if (Character.isUpperCase(firstChar)) {
					firstChar = Character.toLowerCase(firstChar);
					if (name.length() == 1) {
						name = Character.toString(firstChar);
					} else {
						name = Character.toString(firstChar) + name.substring(1);
					}
				}
			}
			processor.processOutMethod(m, name);
		}
	}

	/**
	 * Make the processor visit all declared fields on the given class.
	 */
	private void processFields(Class objectsClass, Processor processor) {
		Field[] fields = objectsClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String injectName = field.getName();
			boolean inject = false;
			boolean optional = true;
			try {
				Object[] annotations = (Object[]) field.getClass().getMethod("getAnnotations", //$NON-NLS-1$
						new Class[0]).invoke(field, new Object[0]);
				for (int j = 0; j < annotations.length; j++) {
					Object annotation = annotations[j];
					try {
						String annotationName = ((Class) annotation.getClass().getMethod(
								"annotationType", new Class[0]).invoke(annotation, new Object[0])) //$NON-NLS-1$
								.getName();
						if (annotationName.endsWith(INJECT) || annotationName.endsWith(IN)) {
							inject = true;
							try {
								optional = ((Boolean) annotation.getClass().getMethod("optional",//$NON-NLS-1$
										new Class[0]).invoke(annotation, new Object[0]))
										.booleanValue();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (annotationName.endsWith(NAMED)) {
							try {
								injectName = (String) annotation.getClass().getMethod("value",//$NON-NLS-1$
										new Class[0]).invoke(annotation, new Object[0]);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (annotationName.endsWith(RESOURCE)) {
							inject = true;
							String resourceName = null;
							try {
								resourceName = (String) annotation.getClass().getMethod("name",//$NON-NLS-1$
										new Class[0]).invoke(annotation, new Object[0]);
							} catch (Exception e) {
								logWarning(field, e);
							}
							if (resourceName != null && !resourceName.equals("")) {//$NON-NLS-1$
								injectName = resourceName;
							}
						}
					} catch (Exception e1) {
						logWarning(field, e1);
					}
				}
			} catch (Exception e2) {
				// ignore - no annotation support
			}
			if (!inject && injectName.startsWith(fieldPrefix)) {
				inject = true;
				injectName = injectName.substring(fieldPrefixLength);
			}
			if (inject) {
				processor.processField(field, injectName, optional);
			}
		}
	}

	/**
	 * Make the processor visit all declared methods on the given class.
	 */
	private ProcessMethodsResult processMethods(Class objectsClass, Processor processor,
			ProcessMethodsResult result) {
		Method[] methods = objectsClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			// don't process methods already visited in subclasses
			if (result.seen(method))
				continue;
			if (isPostConstruct(method)) {
				result.postConstructMethods.add(method);
				continue;
			}
			String candidateName = method.getName();
			boolean inject = candidateName.startsWith(setMethodPrefix);
			boolean optional = false;
			try {
				Object[] annotations = (Object[]) method.getClass().getMethod("getAnnotations", //$NON-NLS-1$
						new Class[0]).invoke(method, new Object[0]);
				for (int j = 0; j < annotations.length; j++) {
					Object annotation = annotations[j];
					try {
						String annotationName = ((Class) annotation.getClass().getMethod(
								"annotationType", new Class[0]).invoke(annotation, new Object[0]))//$NON-NLS-1$
								.getName();
						if (annotationName.endsWith(INJECT) || annotationName.endsWith(IN)) {
							inject = true;
							try {
								optional = ((Boolean) annotation.getClass().getMethod("optional",//$NON-NLS-1$
										new Class[0]).invoke(annotation, new Object[0]))
										.booleanValue();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (processor.shouldProcessPostConstruct
								&& annotationName.endsWith(POST_CONSTRUCT)) {
							inject = false;
							result.postConstructMethods.add(method);
						} else if (processor.shouldProcessOutMethods
								&& annotationName.endsWith(OUT)) {
							inject = false;
							result.outMethods.add(method);
						}
					} catch (Exception ex) {
						logWarning(method, ex);
					}
				}
			} catch (Exception e) {
				// ignore - no annotation support
			}
			if (inject) {
				processor.processMethod(method, optional);
			}
		}
		return result;
	}

	private Object[] safeObjectsCopy() {
		Object[] result;
		int pos = 0;
		synchronized (userObjects) {
			result = new Object[userObjects.size()];
			for (Iterator i = userObjects.iterator(); i.hasNext();) {
				WeakReference ref = (WeakReference) i.next();
				Object userObject = ref.get();
				if (userObject == null) { // user object got GCed, clean up refs
					// for future
					i.remove();
					continue;
				}
				result[pos] = userObject;
				pos++;
			}
		}
		if (pos == result.length)
			return result;
		// reallocate the array
		Object[] tmp = new Object[pos];
		System.arraycopy(result, 0, tmp, 0, pos);
		return tmp;
	}

	protected boolean setField(Object userObject, Field field, Object value) {
		if ((value != null) && !field.getType().isAssignableFrom(value.getClass())) {
			// TBD add debug option
			return false;
		}

		boolean wasAccessible = true;
		if (!field.isAccessible()) {
			field.setAccessible(true);
			wasAccessible = false;
		}
		try {
			field.set(userObject, value);
		} catch (IllegalArgumentException e) {
			logWarning(field, e);
			return false;
		} catch (IllegalAccessException e) {
			logWarning(field, e);
			return false;
		} finally {
			if (!wasAccessible)
				field.setAccessible(false);
		}
		return true;
	}

	protected boolean setMethod(Object userObject, Method method, Object value) {
		Class[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1)
			return false;
		if ((value != null) && !parameterTypes[0].isAssignableFrom(value.getClass()))
			return false;

		boolean wasAccessible = true;
		if (!method.isAccessible()) {
			method.setAccessible(true);
			wasAccessible = false;
		}
		try {
			method.invoke(userObject, new Object[] { value });
		} catch (IllegalArgumentException e) {
			logWarning(method, e);
			return false;
		} catch (IllegalAccessException e) {
			logWarning(method, e);
			return false;
		} catch (InvocationTargetException e) {
			logWarning(method, e);
			return false;
		} finally {
			if (!wasAccessible)
				method.setAccessible(false);
		}
		return true;
	}

	public String toString() {
		return "InjectionTracker(" + context + ')'; //$NON-NLS-1$
	}
}
