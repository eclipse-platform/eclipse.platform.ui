package org.eclipse.e4.tools.compat.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.contributions.IContributionFactorySpi;
import org.eclipse.e4.core.services.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Create the contribution factory.
 */
public class ReflectionContributionFactory implements IContributionFactory {

	private IExtensionRegistry registry;
	private Map<String, Object> languages;
	
	@Inject
	private PackageAdmin packageAdmin;
	
	@Inject
	private Logger logger;

	/**
	 * Create a reflection factory.
	 * 
	 * @param registry
	 *            to read languages.
	 */
	@Inject
	public ReflectionContributionFactory(IExtensionRegistry registry) {
		this.registry = registry;
		processLanguages();
	}

	public Object call(Object object, String uriString, String methodName, IEclipseContext context,
			Object defaultValue) {
		if (uriString != null) {
			URI uri = new URI(uriString);
			if (uri.segments.length > 3) {
				String prefix = uri.segments[2];
				IContributionFactorySpi factory = (IContributionFactorySpi) languages.get(prefix);
				return factory.call(object, methodName, context, defaultValue);
			}
		}

		Method targetMethod = null;

		Method[] methods = object.getClass().getMethods();

		// Optimization: if there's only one method, use it.
		if (methods.length == 1) {
			targetMethod = methods[0];
		} else {
			ArrayList<Method> toSort = new ArrayList<Method>();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				// Filter out non-public constructors
				if ((method.getModifiers() & Modifier.PUBLIC) != 0
						&& method.getName().equals(methodName)) {
					toSort.add(method);
				}
			}

			// Sort the methods by descending number of method
			// arguments
			Collections.sort(toSort, new Comparator<Method>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				public int compare(Method m1, Method m2) {
					int l1 = m1.getParameterTypes().length;
					int l2 = m2.getParameterTypes().length;

					return l1 - l2;
				}
			});

			// Find the first satisfiable method
			for (Iterator<Method> iter = toSort.iterator(); iter.hasNext() && targetMethod == null;) {
				Method next = iter.next();

				boolean satisfiable = true;

				Class<?>[] params = next.getParameterTypes();
				for (int i = 0; i < params.length && satisfiable; i++) {
					Class<?> clazz = params[i];

					if (!context.containsKey(clazz.getName())
							&& !IEclipseContext.class.equals(clazz)) {
						satisfiable = false;
					}
				}

				if (satisfiable) {
					targetMethod = next;
				}
			}
		}

		if (targetMethod == null) {
			if (defaultValue != null) {
				return defaultValue;
			}
			throw new RuntimeException(
					"could not find satisfiable method " + methodName + " in class " + object.getClass()); //$NON-NLS-1$//$NON-NLS-2$
		}

		Class<?>[] paramKeys = targetMethod.getParameterTypes();

		try {
			logger.debug("calling: " + methodName);
			Object[] params = new Object[paramKeys.length];
			for (int i = 0; i < params.length; i++) {
				if (IEclipseContext.class.equals(paramKeys[i])) {
					params[i] = context;
				} else {
					params[i] = context.get(paramKeys[i].getName());
				}
			}

			return targetMethod.invoke(object, params);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Object create(String uriString, IEclipseContext context) {
		if (uriString == null) {
			return null;
		}
		URI uri = new URI(uriString);
		Bundle bundle = getBundle(uri);
		Object contribution;
		if (bundle != null) {
			contribution = createFromBundle(bundle, context, uri);
		} else {
			contribution = null;
			logger.error("Unable to retrive the bundle from the URI: "+ uriString);
		}
		return contribution;
	}

	protected Object createFromBundle(Bundle bundle, IEclipseContext context, URI uri) {
		Object contribution;
		if (uri.segments.length > 3) {
			String prefix = uri.segments[2];
			IContributionFactorySpi factory = (IContributionFactorySpi) languages.get(prefix);
			StringBuffer resource = new StringBuffer(uri.segments[3]);
			for (int i = 4; i < uri.segments.length; i++) {
				resource.append('/');
				resource.append(uri.segments[i]);
			}
			contribution = factory.create(bundle, resource.toString(), context);
		} else {
			String clazz = uri.segments[2];
			try {
				Class<?> targetClass = bundle.loadClass(clazz);
				contribution = ContextInjectionFactory.make(targetClass, context);
				if (contribution == null) {
					String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
							+ bundle.getBundleId() + "'"; //$NON-NLS-1$
					logger.error(message);
				}
			} catch (ClassNotFoundException e) {
				contribution = null;
				String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				logger.error(message);
			} catch (InjectionException e) {
				contribution = null;
				String message = "Unable to create class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				logger.error(message);
			}
		}
		return contribution;
	}

	protected void processLanguages() {
		languages = new HashMap<String, Object>();
		String extId = "org.eclipse.e4.languages"; //$NON-NLS-1$
		IConfigurationElement[] languageElements = registry.getConfigurationElementsFor(extId);
		for (int i = 0; i < languageElements.length; i++) {
			IConfigurationElement languageElement = languageElements[i];
			try {
				languages.put(languageElement.getAttribute("name"), //$NON-NLS-1$
						languageElement.createExecutableExtension("contributionFactory")); //$NON-NLS-1$
			} catch (InvalidRegistryObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Bundle getBundle(URI platformURI) {
		return getBundleForName(platformURI.segments[1]);
	}

	public Bundle getBundle(String uriString) {
		return getBundle(new URI(uriString));
	}
	
	public Bundle getBundleForName(String bundlename) {
		if( packageAdmin == null ) {
			Bundle bundle =  FrameworkUtil.getBundle(getClass());
			BundleContext context = bundle.getBundleContext();
			ServiceReference reference = context.getServiceReference(PackageAdmin.class.getName());
			packageAdmin = (PackageAdmin) context.getService(reference);			
		}
		
		Bundle[] bundles = packageAdmin.getBundles(bundlename, null);
		if (bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	static class URI {
		String[] segments;
		String uri;
		
		private URI(String uriString) {
			segments = uriString.substring(uriString.indexOf('/')+1).split("/");
		}
	}
}
