package org.eclipse.core.tests.internal.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Enumeration;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.properties.PropertyManager;
import org.eclipse.core.internal.properties.StoredProperty;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.localstore.LocalStoreTest;

public class PropertyManagerTest extends LocalStoreTest {
public PropertyManagerTest() {
	super(null);
}
public PropertyManagerTest(String name) {
	super(name);
}
public static Test suite() {
	//TestSuite suite = new TestSuite();
	//suite.addTest(new PropertyManagerTest("testSimpleUpdate"));
	//return suite;
	return new TestSuite(PropertyManagerTest.class);
}
public void testCopy() throws Throwable {
	PropertyManager manager = new PropertyManager((Workspace) getWorkspace());
	IProject source = projects[0];
	IFolder sourceFolder = source.getFolder("myfolder");
	IResource sourceFile = sourceFolder.getFile("myfile.txt");
	IProject destination = projects[1];
	IFolder destFolder = destination.getFolder(sourceFolder.getName());
	IResource destFile = destFolder.getFile(sourceFile.getName());
	QualifiedName propName = new QualifiedName("test", "prop");
	String propValue = "this is the property value";

	/* 
	 * persistent properties 
	 */ 
	manager.setProperty(source, propName, propValue);
	manager.setProperty(sourceFolder, propName, propValue);
	manager.setProperty(sourceFile, propName, propValue);

	assertNotNull("1.1", manager.getProperty(source, propName));
	assertTrue("1.2", manager.getProperty(source, propName).equals(propValue));
	assertNotNull("1.3", manager.getProperty(sourceFolder, propName));
	assertTrue("1.4", manager.getProperty(sourceFolder, propName).equals(propValue));
	assertNotNull("1.5", manager.getProperty(sourceFile, propName));
	assertTrue("1.6", manager.getProperty(sourceFile, propName).equals(propValue));

	// do the copy at the project level
	manager.copy(source, destination, IResource.DEPTH_INFINITE);

	assertNotNull("1.7", manager.getProperty(destination, propName));
	assertTrue("1.8", manager.getProperty(destination, propName).equals(propValue));

	assertNotNull("1.9", manager.getProperty(destFolder, propName));
	assertTrue("1.10", manager.getProperty(destFolder, propName).equals(propValue));
	assertNotNull("1.11", manager.getProperty(destFile, propName));
	assertTrue("1.12", manager.getProperty(destFile, propName).equals(propValue));

	// do the same thing but copy at the folder level
	manager.deleteProperties(source);
	manager.deleteProperties(destination);
	assertNull("2.0", manager.getProperty(source, propName));
	assertNull("2.1", manager.getProperty(sourceFolder, propName));
	assertNull("2.2", manager.getProperty(sourceFile, propName));
	assertNull("2.3", manager.getProperty(destination, propName));
	assertNull("2.4", manager.getProperty(destFolder, propName));
	assertNull("2.5", manager.getProperty(destFile, propName));
	manager.setProperty(sourceFolder, propName, propValue);
	manager.setProperty(sourceFile, propName, propValue);
	assertNotNull("2.6", manager.getProperty(sourceFolder, propName));
	assertTrue("2.7", manager.getProperty(sourceFolder, propName).equals(propValue));
	assertNotNull("2.8", manager.getProperty(sourceFile, propName));
	assertTrue("2.9", manager.getProperty(sourceFile, propName).equals(propValue));
	
	manager.copy(sourceFolder, destFolder, IResource.DEPTH_INFINITE);

	assertNotNull("2.10", manager.getProperty(destFolder, propName));
	assertTrue("2.11", manager.getProperty(destFolder, propName).equals(propValue));
	assertNotNull("2.12", manager.getProperty(destFile, propName));
	assertTrue("2.13", manager.getProperty(destFile, propName).equals(propValue));

	/* test overwrite */
	String newPropValue = "change property value";
	manager.setProperty(source, propName, newPropValue);
	assertTrue("2.0", manager.getProperty(source, propName).equals(newPropValue));
	manager.copy(source, destination, IResource.DEPTH_INFINITE);
	assertTrue("2.1", manager.getProperty(destination, propName).equals(newPropValue));
}
public void testDeleteProperties() throws Throwable {
	/* create common objects */
	PropertyManager manager = new PropertyManager((Workspace) getWorkspace());
	IFile target = projects[0].getFile("target");

	/* server properties */
	QualifiedName propName = new QualifiedName("eclipse", "prop");
	String propValue = "this is the property value";
	manager.setProperty(target, propName, propValue);
	assertTrue("1.1", manager.getProperty(target, propName).equals(propValue));
	/* delete */
	manager.deleteProperties((Resource) target);
	assertTrue("1.3", manager.getProperty(target, propName) == null);

	//test deep deletion of project properties	
	IProject source = projects[0];
	IFolder sourceFolder = source.getFolder("myfolder");
	IResource sourceFile = sourceFolder.getFile("myfile.txt");
	propName = new QualifiedName("test", "prop");
	propValue = "this is the property value";

	/* 
	 * persistent properties 
	 */ 
	manager.setProperty(source, propName, propValue);
	manager.setProperty(sourceFolder, propName, propValue);
	manager.setProperty(sourceFile, propName, propValue);

	assertNotNull("2.1", manager.getProperty(source, propName));
	assertTrue("2.2", manager.getProperty(source, propName).equals(propValue));
	assertNotNull("2.3", manager.getProperty(sourceFolder, propName));
	assertTrue("2.4", manager.getProperty(sourceFolder, propName).equals(propValue));
	assertNotNull("2.5", manager.getProperty(sourceFile, propName));
	assertTrue("2.6", manager.getProperty(sourceFile, propName).equals(propValue));
	
	//delete properties
	manager.deleteProperties(source, IResource.DEPTH_INFINITE);
	assertNull("3.1", manager.getProperty(source, propName));
	assertNull("3.2", manager.getProperty(sourceFolder, propName));
	assertNull("3.3", manager.getProperty(sourceFile, propName));

}
public void testProperties() throws Throwable {
	IProgressMonitor monitor = null;

	// create common objects
	PropertyManager manager = new PropertyManager((Workspace) getWorkspace());
	IFile target = projects[0].getFile("target");
	target.create(null, false, monitor);

	// these are the properties that we are going to use
	QualifiedName propName1 = new QualifiedName("org.eclipse.core.tests", "prop1");
	QualifiedName propName2 = new QualifiedName("org.eclipse.core.tests", "prop2");
	QualifiedName propName3 = new QualifiedName("org.eclipse.core.tests", "prop3");
	String propValue1 = "this is the property value1";
	String propValue2 = "this is the property value2";
	String propValue3 = "this is the property value3";
	Vector props = new Vector(3);
	props.addElement(new StoredProperty(propName1, propValue1));
	props.addElement(new StoredProperty(propName2, propValue2));
	props.addElement(new StoredProperty(propName3, propValue3));

	// set the properties individually and retrieve them
	for (Enumeration e = props.elements(); e.hasMoreElements();) {
		StoredProperty prop = (StoredProperty) e.nextElement();
		manager.setProperty(target, prop.getName(), prop.getStringValue());
		assertTrue("1.0." + prop.getName(), prop.getStringValue().equals(manager.getProperty(target, prop.getName())));
	}
	manager.deleteProperties((Resource) target);

	// remove trash
	target.delete(false, monitor);
}
public void testSimpleUpdate() {

	// create common objects
	IFile target = projects[0].getFile("target");
	try {
		target.create(getRandomContents(), true, getMonitor());
	} catch (CoreException e) {
		fail("0.0", e);
	}

	// prepare keys and values
	int N = 3;
	QualifiedName[] names = new QualifiedName[3];
	String[] values = new String[N];
	for (int i = 0; i < N; i++) {
		names[i] = new QualifiedName("org.eclipse.core.tests", "prop" + i);
		values[i] = "property value" + i;
	}

	// create properties
	for (int i = 0; i < N; i++) {
		try {
			target.setPersistentProperty(names[i], values[i]);
		} catch (CoreException e) {
			fail("1." + i, e);
		}
	}

	// verify
	for (int i = 0; i < N; i++) {
		try {
			assertTrue("2.0", target.getPersistentProperty(names[i]).equals(values[i]));
		} catch (CoreException e) {
			fail("3." + i, e);
		}
	}

	for (int j = 0; j < 20; j++) {

		// change properties
		for (int i = 0; i < N; i++) {
			try {
				values[i] = values[i] + " - changed";
				target.setPersistentProperty(names[i], values[i]);
			} catch (CoreException e) {
				fail("4." + i, e);
			}
		}

		// verify
		for (int i = 0; i < N; i++) {
			try {
				assertTrue("5.0", target.getPersistentProperty(names[i]).equals(values[i]));
			} catch (CoreException e) {
				fail("6." + i, e);
			}
		}

	}

	// remove trash
	try {
		target.delete(true, getMonitor());
	} catch (CoreException e) {
		fail("20.0", e);
	}
}
}
