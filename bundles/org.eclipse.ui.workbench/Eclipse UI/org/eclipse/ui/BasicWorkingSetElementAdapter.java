/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Basic IWorkingSetElementAdapter implementation that allows plugins to decribe
 * simple declarative element adapters.
 * <p>
 * The executable extension format for this class is as follows:<br/>
 * <code>&lt;workingSet
 * elementAdapterClass="org.eclipse.ui.BasicWorkingSetElementAdapter:class1.to.adapt.to[;option1=value1][;option2=value2],class2.to.adapt.to[;option1=value1][;option2=value2],..."&gt;
 * ... &lt;/workingSet&gt;</code>
 * </p>
 * <p>
 * The valid options are:<br/>
 * <dl>
 * <dt>adapt</dt>
 * <dd>Values: <code>true</code> or <code>true</code>. Specifies whether
 * or not the platform {@link org.eclipse.core.runtime.IAdapterManager} and the
 * {@link org.eclipse.core.runtime.IAdaptable} interface should be consulted.</dd>
 * </dl>
 * </p>
 * 
 * Please see the {@link #adaptElements(IWorkingSet, IAdaptable[])} method for
 * details on behavior of this implementation.
 * 
 * @since 3.3
 */
public final class BasicWorkingSetElementAdapter implements
		IWorkingSetElementAdapter, IExecutableExtension {

	private class Type {
		private static final int NONE = 0;
		private static final int ADAPT = 1;
		String className;
		int flags;
	}

	private Type[] preferredTypes = new Type[0];

	private ServiceTracker packageTracker;

	/**
	 * When invoked this method will iterate over all classes specified as
	 * IExecutableExtension arguements to this class in order and compare with
	 * the elements. If the element is directly assignable to the provided class
	 * then it is added to the result array as is. If the class has specified
	 * "adapt=true" as an argument and there is an available adapter in the
	 * platform IAdapterManager then it is returned. Finally, if "adapt=true"
	 * and the class is already loaded (determined by inspecting exported
	 * bundles via the platform PackageAdmin) a direct query for the adapter is
	 * made on the object and if it is not <code>null</code> then it is
	 * returned.
	 * <p>
	 * A consequence of the above is that it is possible for this method to
	 * return differing results based on the state of bundles loaded within the
	 * system.
	 * </p>
	 * 
	 * @see org.eclipse.ui.IWorkingSetElementAdapter#adaptElements(org.eclipse.ui.IWorkingSet,
	 *      org.eclipse.core.runtime.IAdaptable[])
	 * @see org.eclipse.core.runtime.IAdapterManager#getAdapter(Object, String)
	 * @see org.osgi.service.packageadmin.PackageAdmin#getExportedPackage(String)
	 */
	public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements) {
		List adaptedElements = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IAdaptable adaptable = adapt(elements[i]);
			if (adaptable != null)
				adaptedElements.add(adaptable);
		}

		return (IAdaptable[]) adaptedElements
				.toArray(new IAdaptable[adaptedElements.size()]);
	}

	/**
	 * Adapt the given adaptable. Compares the given adaptable against the list
	 * of desired types and returns the first type that generates a match.
	 * 
	 * @param adaptable
	 *            the adaptable to adapt
	 * @return the resultant adaptable. May be the same adaptable, a new
	 *         adaptable, or <code>null</code>.
	 */
	private IAdaptable adapt(IAdaptable adaptable) {
		for (int i = 0; i < preferredTypes.length; i++) {
			IAdaptable adaptedAdaptable = adapt(preferredTypes[i], adaptable);
			if (adaptedAdaptable != null)
				return adaptedAdaptable;
		}
		return null;
	}

	/**
	 * Adapt the given adaptable given the reference type.
	 * 
	 * @param type
	 *            the reference type
	 * @param adaptable
	 *            the adaptable to adapt
	 * @return the resultant adaptable. May be the same adaptable, a new
	 *         adaptable, or <code>null</code>.
	 */
	private IAdaptable adapt(Type type, IAdaptable adaptable) {
		IAdapterManager adapterManager = Platform.getAdapterManager();
		Class[] directClasses = adapterManager.computeClassOrder(adaptable
				.getClass());
		for (int i = 0; i < directClasses.length; i++) {
			Class clazz = directClasses[i];
			if (clazz.getName().equals(type.className))
				return adaptable;
		}

		if ((type.flags & Type.ADAPT) != 0) {
			Object adapted = adapterManager.getAdapter(adaptable,
					type.className);
			if (adapted instanceof IAdaptable)
				return (IAdaptable) adapted;

			PackageAdmin admin = getPackageAdmin();
			if (admin != null) {
				int lastDot = type.className.lastIndexOf('.');
				if (lastDot > 0) { // this lives in a package
					String packageName = type.className.substring(0, lastDot);
					ExportedPackage[] packages = admin
							.getExportedPackages(packageName);
					if (packages != null && packages.length == 1) {
						// if there is exactly one exporter of this
						// package
						// we can go further
						if (packages[0].getExportingBundle().getState() == Bundle.ACTIVE) {
							try {
								// if the bundle is loaded we can safely get the
								// class object and check for an adapter on the
								// object directly
								adapted = adaptable.getAdapter(packages[0]
										.getExportingBundle().loadClass(
												type.className));
								if (adapted instanceof IAdaptable)
									return (IAdaptable) adapted;

							} catch (ClassNotFoundException e) {
								WorkbenchPlugin.log(e);
							}
						}
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkingSetElementAdapter#dispose()
	 */
	public void dispose() {
		if (packageTracker != null)
			packageTracker.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {

		if (data instanceof String) {
			List preferredTypes = new ArrayList(0);
			for (StringTokenizer toker = new StringTokenizer((String) data, ","); toker.hasMoreTokens();) {//$NON-NLS-1$
				String classNameAndOptions = toker.nextToken();
				Type record = new Type();
				parseOptions(classNameAndOptions, record);
				preferredTypes.add(record);
			}
			this.preferredTypes = (Type[]) preferredTypes
					.toArray(new Type[preferredTypes.size()]);
		}
	}

	/**
	 * Parse classname/option strings in the form:<br/>
	 * <code>some.package.Class[:option1=value1][:option2=value2]...
	 * 
	 * @param classNameAndOptions the class name and possibly options to parse
	 * @param record the record to fill
	 */
	private void parseOptions(String classNameAndOptions, Type record) {
		for (StringTokenizer toker = new StringTokenizer(classNameAndOptions,
				";"); toker.hasMoreTokens();) { //$NON-NLS-1$
			String token = toker.nextToken();
			if (record.className == null)
				record.className = token;
			else {
				for (StringTokenizer pair = new StringTokenizer(token, "="); pair.hasMoreTokens();) {//$NON-NLS-1$
					if (pair.countTokens() == 2) {
						String param = pair.nextToken();
						String value = pair.nextToken();
						if ("adapt".equals(param)) { //$NON-NLS-1$
							record.flags ^= "true".equals(value) ? Type.ADAPT : Type.NONE; //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	/**
	 * Prime the PackageAdmin service tracker and return the service (if
	 * available).
	 * 
	 * @return the PackageAdmin service or null if it is not available
	 */
	private PackageAdmin getPackageAdmin() {
		if (packageTracker == null) {
			packageTracker = new ServiceTracker(WorkbenchPlugin.getDefault()
					.getBundleContext(), PackageAdmin.class.getName(), null);
			packageTracker.open();
		}

		return (PackageAdmin) packageTracker.getService();
	}
}
