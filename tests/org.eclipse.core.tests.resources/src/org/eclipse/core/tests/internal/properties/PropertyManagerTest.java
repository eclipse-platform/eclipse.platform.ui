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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
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
	/* create common objects */
	PropertyManager manager = new PropertyManager((Workspace) getWorkspace());

	/* server properties */
	QualifiedName propName = new QualifiedName("test", "prop");
	String propValue = "this is the property value";
	manager.setProperty(projects[0], propName, propValue);
	assertTrue("1.1", manager.getProperty(projects[0], propName).equals(propValue));
	manager.copy(projects[0], projects[1], IResource.DEPTH_INFINITE);
	assertTrue("1.2", manager.getProperty(projects[1], propName).equals(propValue));
	/* test overwrite */
	propValue = "change property value";
	manager.setProperty(projects[0], propName, propValue);
	assertTrue("1.3", manager.getProperty(projects[0], propName).equals(propValue));
	manager.copy(projects[0], projects[1], IResource.DEPTH_INFINITE);
	assertTrue("1.4", manager.getProperty(projects[1], propName).equals(propValue));
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
