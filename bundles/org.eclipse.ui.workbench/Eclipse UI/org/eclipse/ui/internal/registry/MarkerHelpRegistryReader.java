package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * This class is used to read marker help context ids and 
 * resolutions from the platform registry.
 */
public class MarkerHelpRegistryReader extends RegistryReader {
	private ArrayList markerQueries;
	private MarkerHelpRegistry markerHelpRegistry;
	private ArrayList currentAttributeNames;
	private ArrayList currentAttributeValues;

	private static final    String      TAG_HELP = "markerHelp";//$NON-NLS-1$
	private static final    String      TAG_RESOLUTION_GENERATOR = "markerResolutionGenerator";//$NON-NLS-1$
	private static final    String      TAG_ATTRIBUTE = "attribute";//$NON-NLS-1$ 
	private static final    String      ATT_TYPE = "markerType";//$NON-NLS-1$
	private static final    String      ATT_NAME = "name";//$NON-NLS-1$
	private static final    String      ATT_VALUE = "value";//$NON-NLS-1$

	/**
	 * Get the marker help that is defined in the plugin registry
	 * and add it to the given marker help registry.
	 * <p>
	 * Warning:
	 * The marker help registry must be passed in because this 
	 * method is called during the process of setting up the 
	 * marker help registry and at this time it has not been
	 * safely setup with the plugin.
	 * </p>
	 */
	public void addHelp(MarkerHelpRegistry registry) {
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		markerHelpRegistry = registry;
		markerQueries = new ArrayList();
		readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_MARKER_HELP);
		markerQueries = new ArrayList();
		readRegistry(pluginRegistry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_MARKER_RESOLUTION);
	}

	/**
	 * Processes one configuration element or child element.
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(TAG_HELP)) {
			readHelpElement(element);
			return true;
		}
		if (element.getName().equals(TAG_RESOLUTION_GENERATOR)) {
			readResolutionElement(element);
			return true;
		}
		if (element.getName().equals(TAG_ATTRIBUTE)) {
			readAttributeElement(element);
			return true;
		}
		return false;
	}

	/**
	 * Processes a help configuration element.
	 */
	private void readHelpElement(IConfigurationElement element) {
		// read type
		String type = element.getAttribute(ATT_TYPE);
		
		// read attributes and values
		currentAttributeNames = new ArrayList();
		currentAttributeValues = new ArrayList();
		readElementChildren(element);
		String[] attributeNames = (String[])currentAttributeNames.toArray(
			new String[currentAttributeNames.size()]);
		String[] attributeValues = (String[])currentAttributeValues.toArray(
			new String[currentAttributeValues.size()]);
		
		// add query to the registry
		MarkerQuery query = new MarkerQuery(type, attributeNames);
		MarkerQueryResult result = new MarkerQueryResult(attributeValues);
		markerHelpRegistry.addHelpQuery(query, result, element);
	}

	/**
	 * Processes a resolution configuration element.
	 */
	private void readResolutionElement(IConfigurationElement element) {
		// read type
		String type = element.getAttribute(ATT_TYPE);
		
		// read attributes and values
		currentAttributeNames = new ArrayList();
		currentAttributeValues = new ArrayList();
		readElementChildren(element);
		String[] attributeNames = (String[])currentAttributeNames.toArray(
			new String[currentAttributeNames.size()]);
		String[] attributeValues = (String[])currentAttributeValues.toArray(
			new String[currentAttributeValues.size()]);
		
		// add query to the registry
		MarkerQuery query = new MarkerQuery(type, attributeNames);
		MarkerQueryResult result = new MarkerQueryResult(attributeValues);
		markerHelpRegistry.addResolutionQuery(query, result, element);
	}

	/**
	 * Processes an attribute sub element.
	 */
	private void readAttributeElement(IConfigurationElement element) {
		String name = element.getAttribute(ATT_NAME);
		String value = element.getAttribute(ATT_VALUE);
		if (name != null && value != null) {
			currentAttributeNames.add(name);
			currentAttributeValues.add(value);
		}
	}			
}


