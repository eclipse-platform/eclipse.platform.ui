/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ui.PlatformUI;


/**
 * Utility class for accessing marker attributes. The static methods provided
 * on this class provide internal exception handling (unexpected
 * <code>CoreException</code>s are logged to workbench).
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class MarkerUtilities {

	/**
	 * Internal marker super type hierarchy cache.
	 * TODO this cache is currently unbound, i.e. only limited by the number of marker types
	 */
	private static class MarkerTypeHierarchy {

		private Map<String, String[]> fTypeMap;
		private Map<String, String[]> fSuperTypesCache= new HashMap<>();

		public String[] getSuperTypes(String typeName) {
			String[] cachedTypes= fSuperTypesCache.get(typeName);
			if (cachedTypes == null) {
				cachedTypes= computeSuperTypes(typeName);
				fSuperTypesCache.put(typeName, cachedTypes);
			}
			return cachedTypes;
		}

		private String[] computeSuperTypes(String typeName) {
			ArrayList<String> types= new ArrayList<>();
			appendAll(types, getDirectSuperTypes(typeName));
			int index= 0;
			while (index < types.size()) {
				String type= types.get(index++);
				appendAll(types, getDirectSuperTypes(type));
			}

			String[] superTypes= new String[types.size()];
			types.toArray(superTypes);
			return superTypes;
		}

		private String[] getDirectSuperTypes(String typeName) {
			return getTypeMap().get(typeName);
		}

		private <T> void appendAll(List<T> list, T[] objects) {
			if (objects == null)
				return;
			for (T o : objects) {
				if (!list.contains(o))
					list.add(o);
			}
		}

		private Map<String, String[]> getTypeMap() {
			if (fTypeMap == null)
				fTypeMap= readTypes();
			return fTypeMap;
		}

		private Map<String, String[]> readTypes() {
			HashMap<String, String[]> allTypes= new HashMap<>();
			IExtensionPoint point= Platform.getExtensionRegistry().getExtensionPoint(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MARKERS);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					ArrayList<String> types= new ArrayList<>();
					for (IConfigurationElement element : extension.getConfigurationElements()) {
						if (element.getName().equalsIgnoreCase("super")) { //$NON-NLS-1$
							String type = element.getAttribute("type"); //$NON-NLS-1$
							if (type != null) {
								types.add(type);
							}
						}
					}
					String[] superTypes= new String[types.size()];
					types.toArray(superTypes);
					allTypes.put(extension.getUniqueIdentifier(), superTypes);
				}
			}
			return allTypes;
		}
	}

	private static MarkerTypeHierarchy fgMarkerTypeHierarchy;



	/**
	 * Don't allow instantiation.
	 */
	private MarkerUtilities() {
	}

	/**
	 * Returns the ending character offset of the given marker.
	 *
	 * @param marker the marker
	 * @return the ending character offset, or <code>-1</code> if not set
	 * @see IMarker#CHAR_END
	 * @see IMarker#getAttribute(java.lang.String, int)
	 */
	public static int getCharEnd(IMarker marker) {
		return getIntAttribute(marker, IMarker.CHAR_END, -1);
	}

	/**
	 * Returns the starting character offset of the given marker.
	 *
	 * @param marker the marker
	 * @return the starting character offset, or <code>-1</code> if not set
	 * @see IMarker#CHAR_START
	 * @see IMarker#getAttribute(java.lang.String,int)
	 */
	public static int getCharStart(IMarker marker) {
		return getIntAttribute(marker, IMarker.CHAR_START, -1);
	}

	/**
	 * Returns the specified attribute of the given marker as an integer.
	 * Returns the given default if the attribute value is not an integer.
	 *
	 * @param marker		the marker
	 * @param attributeName	the name of the attribute
	 * @param defaultValue	the default value
	 * @return 				the attribute's value or the default value
	 * 							if the attribute does not exist or isn't an int
	 */
	private static int getIntAttribute(IMarker marker, String attributeName, int defaultValue) {
		if (marker.exists())
			return marker.getAttribute(attributeName, defaultValue);
		return defaultValue;
	}

	/**
	 * Returns the line number of the given marker.
	 *
	 * @param marker the marker
	 * @return the line number, or <code>-1</code> if not set
	 * @see IMarker#LINE_NUMBER
	 * @see IMarker#getAttribute(java.lang.String,int)
	 */
	public static int getLineNumber(IMarker marker) {
		return getIntAttribute(marker, IMarker.LINE_NUMBER, -1);
	}

	/**
	 * Returns the priority of the given marker.
	 *
	 * @param marker the marker
	 * @return the priority, or <code>IMarker.PRIORITY_NORMAL</code> if not set
	 * @see IMarker#PRIORITY
	 * @see IMarker#PRIORITY_NORMAL
	 * @see IMarker#getAttribute(java.lang.String,int)
	 */
	public static int getPriority(IMarker marker) {
		return getIntAttribute(marker, IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
	}

	/**
	 * Returns the severity of the given marker.
	 *
	 * @param marker the marker
	 * @return the priority, or <code>IMarker.SEVERITY_INFO</code> if not set
	 * @see IMarker#SEVERITY
	 * @see IMarker#SEVERITY_INFO
	 * @see IMarker#getAttribute(java.lang.String,int)
	 */
	public static int getSeverity(IMarker marker) {
		return getIntAttribute(marker, IMarker.SEVERITY, IMarker.SEVERITY_INFO);
	}

	/**
	 * Handles a core exception which occurs when accessing marker attributes.
	 *
	 * @param e the core exception
	 */
	private static void handleCoreException(CoreException e) {
		Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
		ILog log= ILog.of(bundle);
		log.log(e.getStatus());
	}

	/**
	 * Returns whether the given marker is of the given type (either directly or indirectly).
	 *
	 * @param marker the marker to be checked
	 * @param type the reference type
	 * @return <code>true</code>if maker is an instance of the reference type
	 */
	public static boolean isMarkerType(IMarker marker, String type) {
		if (marker != null) {
			try {
				return marker.exists() && marker.isSubtypeOf(type);
			} catch (CoreException x) {
				handleCoreException(x);
			}
		}
		return false;
	}

	/**
	 * Returns the marker type of the given marker or <code>null</code> if
	 * the type could not be determined.
	 *
	 * @param marker the marker
	 * @return the marker type
	 * @since 3.0
	 */
	public static String getMarkerType(IMarker marker) {
		try {
			return marker.getType();
		} catch (CoreException x) {
			handleCoreException(x);
		}
		return null;
	}

	/**
	 * Returns the message associated with the given marker.
	 *
	 * @param marker the marker
	 * @return the message associated with the marker or <code>null</code>
	 * @since 3.0
	 */
	public static String getMessage(IMarker marker) {
		return marker.getAttribute(IMarker.MESSAGE, null);
	}

	/**
	 * Sets the ending character offset of the given marker.
	 *
	 * @param marker the marker
	 * @param charEnd the ending character offset
	 * @see IMarker#CHAR_END
	 * @see IMarker#setAttribute(java.lang.String,int)
	 */
	public static void setCharEnd(IMarker marker, int charEnd) {
		setIntAttribute(marker, IMarker.CHAR_END, charEnd);
	}

	/**
	 * Sets the ending character offset in the given map using the standard
	 * marker attribute name as the key.
	 *
	 * @param map the map
	 * @param charEnd the ending character offset
	 * @see IMarker#CHAR_END
	 */
	public static void setCharEnd(Map<String, Object> map, int charEnd) {
		map.put(IMarker.CHAR_END, Integer.valueOf(charEnd));
	}

	/**
	 * Sets the starting character offset of the given marker.
	 *
	 * @param marker the marker
	 * @param charStart the starting character offset
	 * @see IMarker#CHAR_START
	 * @see IMarker#setAttribute(java.lang.String,int)
	 */
	public static void setCharStart(IMarker marker, int charStart) {
		setIntAttribute(marker, IMarker.CHAR_START, charStart);
	}

	/**
	 * Sets the starting character offset in the given map using the standard
	 * marker attribute name as the key.
	 *
	 * @param map the map
	 * @param charStart the starting character offset
	 * @see IMarker#CHAR_START
	 */
	public static void setCharStart(Map<String, Object> map, int charStart) {
		map.put(IMarker.CHAR_START, Integer.valueOf(charStart));
	}

	/**
	 * Sets the specified attribute of the given marker as an integer.
	 *
	 * @param marker the marker
	 * @param attributeName the attribute name
	 * @param value the int value
	 */
	private static void setIntAttribute(IMarker marker, String attributeName, int value) {
		try {
			if (marker.exists())
				marker.setAttribute(attributeName, value);
		} catch (CoreException e) {
			handleCoreException(e);
		}
	}

	/**
	 * Sets the line number of the given marker.
	 *
	 * @param marker the marker
	 * @param lineNum the line number
	 * @see IMarker#LINE_NUMBER
	 * @see IMarker#setAttribute(java.lang.String,int)
	 */
	public static void setLineNumber(IMarker marker, int lineNum) {
		setIntAttribute(marker, IMarker.LINE_NUMBER, lineNum);
	}

	/**
	 * Sets the line number in the given map using the standard marker attribute
	 * name as the key.
	 *
	 * @param map the map
	 * @param lineNum the line number
	 * @see IMarker#LINE_NUMBER
	 */
	public static void setLineNumber(Map<String, Object> map, int lineNum) {
		map.put(IMarker.LINE_NUMBER, Integer.valueOf(lineNum));
	}

	/**
	 * Sets the message in the given map using the standard marker attribute name
	 * as the key.
	 *
	 * @param map the map
	 * @param message the message
	 * @see IMarker#MESSAGE
	 */
	public static void setMessage(Map<String, Object> map, String message) {
		map.put(IMarker.MESSAGE, message);
	}

	/**
	 * Creates a marker on the given resource with the given type and attributes.
	 * <p>
	 * This method modifies the workspace (progress is not reported to the user).</p>
	 *
	 * @param resource the resource
	 * @param attributes the attribute map
	 * @param markerType the type of marker
	 * @throws CoreException if this method fails
	 * @see IResource#createMarker(java.lang.String)
	 */
	public static void createMarker(final IResource resource, final Map<String, Object> attributes, final String markerType) throws CoreException {

		IWorkspaceRunnable r= monitor -> {
			IMarker marker= resource.createMarker(markerType);
			marker.setAttributes(attributes);
		};

		resource.getWorkspace().run(r, null,IWorkspace.AVOID_UPDATE, null);
	}

	/**
	 * Returns the list of super types for the given marker.
	 * The list is a depth first list and maintains the sequence in which
	 * the super types are listed in the marker specification.
	 *
	 * @param markerType the marker's type
	 * @return a depth-first list of all super types of the given marker type
	 */
	public static String[] getSuperTypes(String markerType) {
		if (fgMarkerTypeHierarchy == null)
			fgMarkerTypeHierarchy= new MarkerTypeHierarchy();
		return fgMarkerTypeHierarchy.getSuperTypes(markerType);
	}

	/**
	 * Changes the given attribute key-value pairs of the map on this marker. The values must be
	 * <code>null</code> or an instance of one of the following classes: <code>String</code>,
	 * <code>Integer</code>, or <code>Boolean</code>. If a value is <code>null</code>, the new value
	 * of the attribute is considered to be undefined.
	 *
	 * <p>
	 * The values of the attributes cannot be <code>String</code> whose UTF encoding exceeds 65535
	 * bytes. On persistent markers this limit is enforced by an assertion.
	 * </p>
	 *
	 * <p>
	 * This method changes resources; these changes will be reported in a subsequent resource change
	 * event, including an indication that this marker has been modified.
	 * </p>
	 * 
	 * @param marker the marker
	 * @param attributeChanges map with to be executed attribute changes
	 * @see IMarker#setAttributes(String[], Object[])
	 * @since 3.17
	 */
	public static void changeAttributes(IMarker marker, Map<String, Object> attributeChanges) {

		Set<Entry<String, Object>> entries= attributeChanges.entrySet();
		int size= entries.size();

		String[] keys= new String[size];
		Object[] values= new Object[size];

		int i= 0;
		for (Entry<String, Object> e : entries) {
			keys[i]= e.getKey();
			values[i]= e.getValue();
			i++;
		}

		try {
			if (marker.exists()) {
				marker.setAttributes(keys, values);
			}
		} catch (CoreException e) {
			handleCoreException(e);
		}

	}
}
