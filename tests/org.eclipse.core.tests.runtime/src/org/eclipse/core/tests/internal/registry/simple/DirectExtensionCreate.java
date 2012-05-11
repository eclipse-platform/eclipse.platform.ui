/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.registry.spi.ConfigurationElementAttribute;
import org.eclipse.core.internal.registry.spi.ConfigurationElementDescription;
import org.eclipse.core.runtime.*;

/**
 * Tests programmatic creation of extension and extension point by using direct 
 * methods on the ExtensionRegistry.
 * 
 * Note that in present those methods are internal, but might be exposed as
 * APIs in the future.
 * 
 * @since 3.2
 */
public class DirectExtensionCreate extends BaseExtensionRegistryRun {

	public DirectExtensionCreate() {
		super();
	}

	public DirectExtensionCreate(String name) {
		super(name);
	}

	public void testExtensionPointAddition() {
		IContributor contributor = ContributorFactorySimple.createContributor("1"); //$NON-NLS-1$ 
		String extensionPointId = "DirectExtPoint"; //$NON-NLS-1$
		String extensionPointLabel = "Direct Extension Point"; //$NON-NLS-1$
		String extensionPointSchemaRef = "schema/ExtensionPointTest.exsd"; //$NON-NLS-1$

		/********************************************************************************************** 
		 * Add extension point:
		 * 
		 * <extension-point id="DirectExtPoint" 
		 * 		name="Direct Extension Point" 
		 * 		schema="schema/ExtensionPointTest.exsd"/>
		 * 
		 *********************************************************************************************/

		((ExtensionRegistry) simpleRegistry).addExtensionPoint(extensionPointId, contributor, false, extensionPointLabel, extensionPointSchemaRef, userToken);

		String namespace = contributor.getName();
		IExtensionPoint extensionPoint = simpleRegistry.getExtensionPoint(qualifiedName(namespace, extensionPointId));
		assertNotNull(extensionPoint);
		assertTrue(extensionPointSchemaRef.equals(extensionPoint.getSchemaReference()));
		assertTrue(extensionPointLabel.equals(extensionPoint.getLabel()));

		// add second contribution in the same namespace
		String extensionPointAltId = "DirectExtPointAlt"; //$NON-NLS-1$
		String extensionPointAltLabel = "Second direct extension point"; //$NON-NLS-1$
		assertTrue(((ExtensionRegistry) simpleRegistry).addExtensionPoint(extensionPointAltId, contributor, false, extensionPointAltLabel, extensionPointSchemaRef, userToken));

		IExtensionPoint extensionPointAlt = simpleRegistry.getExtensionPoint(qualifiedName(namespace, extensionPointAltId));
		assertNotNull(extensionPointAlt);
		assertTrue(extensionPointSchemaRef.equals(extensionPointAlt.getSchemaReference()));
		assertTrue(extensionPointAltLabel.equals(extensionPointAlt.getLabel()));

		/**********************************************************************************************
		 * Add extension:
		 * <extension id="DirectExtensionID" name="Direct Extension" point="DirectExtPoint">
		 * 		<StorageDevice deviceURL="theShienneMountain">
		 * 			<BackupDevice backupURL="SkyLab"/>
		 * 			<BackupDevice backupURL="OceanFloor"/>
		 * 		</StorageDevice>
		 * </extension>
		 *********************************************************************************************/
		String extensionId = "DirectExtensionID"; //$NON-NLS-1$
		String extensionLabel = "Direct Extension"; //$NON-NLS-1$

		String nameChildDesc = "BackupDevice"; //$NON-NLS-1$
		String propNameChildDesc = "backupURL"; //$NON-NLS-1$
		String propValueChildDesc1 = "SkyLab"; //$NON-NLS-1$
		String propValueChildDesc2 = "OceanFloor"; //$NON-NLS-1$

		ConfigurationElementAttribute propChildDesc1 = new ConfigurationElementAttribute(propNameChildDesc, propValueChildDesc1);
		ConfigurationElementDescription childDesc1 = new ConfigurationElementDescription(nameChildDesc, propChildDesc1, null, null);

		ConfigurationElementAttribute propChildDesc2 = new ConfigurationElementAttribute(propNameChildDesc, propValueChildDesc2);
		ConfigurationElementDescription childDesc2 = new ConfigurationElementDescription(nameChildDesc, propChildDesc2, null, null);

		String extensionName = "StorageDevice"; //$NON-NLS-1$
		String extensionProrName1 = "deviceURL"; //$NON-NLS-1$
		String extensionPropValue1 = "theShienneMountain"; //$NON-NLS-1$
		String extensionProrName2 = "primary"; //$NON-NLS-1$
		String extensionPropValue2 = "true"; //$NON-NLS-1$
		ConfigurationElementAttribute prop1 = new ConfigurationElementAttribute(extensionProrName1, extensionPropValue1);
		ConfigurationElementAttribute prop2 = new ConfigurationElementAttribute(extensionProrName2, extensionPropValue2);
		String extensionValue = "SomeValue"; //$NON-NLS-1$

		ConfigurationElementDescription description = new ConfigurationElementDescription(extensionName, new ConfigurationElementAttribute[] {prop1, prop2}, extensionValue, new ConfigurationElementDescription[] {childDesc1, childDesc2});

		assertTrue(((ExtensionRegistry) simpleRegistry).addExtension(extensionId, contributor, false, extensionLabel, extensionPointId, description, userToken));

		IExtension[] namespaceExtensions = simpleRegistry.getExtensions(namespace);
		assertNotNull(namespaceExtensions);
		assertTrue(namespaceExtensions.length == 1);
		IExtension[] extensions = extensionPoint.getExtensions();
		assertNotNull(extensions);
		assertTrue(extensions.length == 1);
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			String storedExtensionId = extension.getUniqueIdentifier();
			assertTrue(storedExtensionId.equals(qualifiedName(namespace, extensionId)));
			String extensionNamespace = extension.getNamespaceIdentifier();
			assertTrue(extensionNamespace.equals(namespace));
			String extensionContributor = extension.getContributor().getName();
			assertTrue(extensionContributor.equals(namespace));
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			assertNotNull(configElements);
			for (int j = 0; j < configElements.length; j++) {
				IConfigurationElement configElement = configElements[j];
				String configElementName = configElement.getName();
				assertTrue(configElementName.equals(extensionName));
				String configElementValue = configElement.getValue();
				assertTrue(configElementValue.equals(extensionValue));
				String[] attributeNames = configElement.getAttributeNames();
				assertTrue(attributeNames.length == 2);
				IConfigurationElement[] configElementChildren = configElement.getChildren();
				assertTrue(configElementChildren.length == 2);
			}
		}
	}

	public static Test suite() {
		return new TestSuite(DirectExtensionCreate.class);
	}

}
