package org.eclipse.e4.tools.compat.internal;

import java.util.HashMap;
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
@SuppressWarnings({ "restriction", "deprecation" })
public class ReflectionContributionFactory implements IContributionFactory {

	private final IExtensionRegistry registry;
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

	@Override
	public Object create(String uriString, IEclipseContext context, IEclipseContext staticContext) {
		return doCreate(uriString, context, staticContext);
	}

	@Override
	public Object create(String uriString, IEclipseContext context) {
		return doCreate(uriString, context, null);
	}

	private Object doCreate(String uriString, IEclipseContext context, IEclipseContext staticContext) {
		if (uriString == null) {
			return null;
		}
		// translate old-style platform:/plugin/ class specifiers into new-style
		// bundleclass:// URIs
		if (uriString.startsWith("platform:/plugin/")) { //$NON-NLS-1$
			logger.error("platform-style URIs deprecated for referencing types: " + uriString); //$NON-NLS-1$
			uriString = uriString
				.replace("platform:/plugin/", "bundleclass://"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.error("URI rewritten as: " + uriString); //$NON-NLS-1$
		}

		final URI uri = URI.createURI(uriString);
		final Bundle bundle = getBundle(uri);
		Object contribution;
		if (bundle != null) {
			contribution = createFromBundle(bundle, context, staticContext, uri);
		} else {
			contribution = null;
			logger.error("Unable to retrive the bundle from the URI: " //$NON-NLS-1$
				+ uriString);
		}
		return contribution;
	}

	protected Object createFromBundle(Bundle bundle, IEclipseContext context,
		IEclipseContext staticContext, URI uri) {
		Object contribution;
		if (uri.segmentCount() > 1) {
			final String prefix = uri.segment(0);
			final IContributionFactorySpi factory = (IContributionFactorySpi) languages.get(prefix);
			if (factory == null) {
				final String message = "Unsupported contribution factory type '" + prefix + "'"; //$NON-NLS-1$ //$NON-NLS-2$
				logger.error(message);
				return null;
			}
			final StringBuffer resource = new StringBuffer(uri.segment(1));
			for (int i = 2; i < uri.segmentCount(); i++) {
				resource.append('/');
				resource.append(uri.segment(i));
			}
			contribution = factory.create(bundle, resource.toString(), context);
		} else {
			final String clazz = uri.segment(0);
			try {
				final Class<?> targetClass = bundle.loadClass(clazz);
				if (staticContext == null) {
					contribution = ContextInjectionFactory.make(targetClass, context);
				} else {
					contribution = ContextInjectionFactory
						.make(targetClass, context, staticContext);
				}

				if (contribution == null) {
					final String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
					logger.error(message);
				}
			} catch (final ClassNotFoundException e) {
				contribution = null;
				final String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
					+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				logger.error(e, message);
			} catch (final InjectionException e) {
				contribution = null;
				final String message = "Unable to create class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
					+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				logger.error(e, message);
			}
		}
		return contribution;
	}

	protected void processLanguages() {
		languages = new HashMap<String, Object>();
		final String extId = "org.eclipse.e4.languages"; //$NON-NLS-1$
		final IConfigurationElement[] languageElements = registry.getConfigurationElementsFor(extId);
		for (int i = 0; i < languageElements.length; i++) {
			final IConfigurationElement languageElement = languageElements[i];
			try {
				languages.put(languageElement.getAttribute("name"), //$NON-NLS-1$
					languageElement.createExecutableExtension("contributionFactory")); //$NON-NLS-1$
			} catch (final InvalidRegistryObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Bundle getBundle(URI platformURI) {
		return getBundleForName(platformURI.authority());
	}

	@Override
	public Bundle getBundle(String uriString) {
		return getBundle(new URI(uriString));
	}

	public Bundle getBundleForName(String bundlename) {
		if (packageAdmin == null) {
			final Bundle bundle = FrameworkUtil.getBundle(getClass());
			final BundleContext context = bundle.getBundleContext();
			final ServiceReference<?> reference = context.getServiceReference(PackageAdmin.class.getName());
			packageAdmin = (PackageAdmin) context.getService(reference);
		}

		final Bundle[] bundles = packageAdmin.getBundles(bundlename, null);
		if (bundles == null) {
			return null;
		}
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	static class URI {
		String scheme;
		String authority;
		String[] segments = new String[0];

		URI(String uriString) {
			final int colon = uriString.indexOf(':');
			if (colon < 0) {
				throw new IllegalArgumentException("invalid URI"); //$NON-NLS-1$
			}
			scheme = uriString.substring(0, colon);
			uriString = uriString.substring(colon + 1);
			if (uriString.startsWith("//")) { //$NON-NLS-1$
				final int authEnd = uriString.indexOf('/', 2);
				if (authEnd < 0) {
					authority = uriString.substring(2);
				} else {
					authority = uriString.substring(2, authEnd);
					segments = uriString.substring(authEnd + 1).split("/"); //$NON-NLS-1$
				}
			} else {
				segments = uriString.substring(uriString.indexOf('/') + 1).split("/"); //$NON-NLS-1$
			}
		}

		public String authority() {
			return authority;
		}

		public String segment(int i) {
			return segments[i];
		}

		public int segmentCount() {
			return segments.length;
		}

		static URI createURI(String uriString) {
			return new URI(uriString);
		}
	}
}
