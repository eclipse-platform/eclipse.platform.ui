/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Tests path variables.
 */
public class IPathVariableTest extends ResourceTest {
	IPathVariableManager manager = getWorkspace().getPathVariableManager();

	class PathVariableChangeVerifier implements IPathVariableChangeListener {
		class VerificationFailedException extends Exception {
			/**
			 * All serializable objects should have a stable serialVersionUID
			 */
			private static final long serialVersionUID = 1L;

			VerificationFailedException(String message) {
				super(message);
			}
		}

		class Event {
			int type;
			String name;
			IPath value;

			Event(int type, String name, IPath value) {
				this.type = type;
				this.name = name;
				this.value = value;
			}

			public boolean equals(Object obj) {
				if (obj == null || !(obj instanceof Event))
					return false;
				Event that = (Event) obj;
				if (this.type != that.type || !this.name.equals(that.name))
					return false;
				return this.value == null ? that.value == null : this.value.equals(that.value);
			}

			public String toString() {
				StringBuffer buffer = new StringBuffer();
				buffer.append("Event(");
				buffer.append("type: ");
				buffer.append(stringForType(type));
				buffer.append(" name: ");
				buffer.append(name);
				buffer.append(" value: ");
				buffer.append(value);
				buffer.append(")");
				return buffer.toString();
			}

			String stringForType(int typeValue) {
				switch (typeValue) {
					case IPathVariableChangeEvent.VARIABLE_CREATED :
						return "CREATED";
					case IPathVariableChangeEvent.VARIABLE_CHANGED :
						return "CHANGED";
					case IPathVariableChangeEvent.VARIABLE_DELETED :
						return "DELETED";
					default :
						return "UNKNOWN";
				}
			}
		}

		List expected = new ArrayList();
		List actual = new ArrayList();

		void addExpectedEvent(int type, String name, IPath value) {
			expected.add(new Event(type, name, value));
		}

		void verify() throws VerificationFailedException {
			String message;
			if (expected.size() != actual.size()) {
				message = "Expected size: " + expected.size() + " does not equal actual size: " + actual.size() + "\n";
				message += dump();
				throw new VerificationFailedException(message);
			}
			for (Iterator i = expected.iterator(); i.hasNext();) {
				Event e = (Event) i.next();
				if (!actual.contains(e)) {
					message = "Expected and actual results differ.\n";
					message += dump();
					throw new VerificationFailedException(message);
				}
			}
		}

		void reset() {
			expected = new ArrayList();
			actual = new ArrayList();
		}

		public void pathVariableChanged(IPathVariableChangeEvent event) {
			actual.add(new Event(event.getType(), event.getVariableName(), event.getValue()));
		}

		String dump() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Expected:\n");
			for (Iterator i = expected.iterator(); i.hasNext();)
				buffer.append("\t" + i.next() + "\n");
			buffer.append("Actual:\n");
			for (Iterator i = actual.iterator(); i.hasNext();)
				buffer.append("\t" + i.next() + "\n");
			return buffer.toString();
		}
	}

	/**
	 * Default constructor for this class. 
	 */
	public IPathVariableTest() {
		super();
	}

	/**
	 * Constructor for the class. 
	 */
	public IPathVariableTest(String name) {
		super(name);
	}

	/**
	 * Return the tests to run.
	 *  
	 * @see org.eclipse.core.tests.harness.ResourceTest#suite()
	 */
	public static Test suite() {
		return new TestSuite(IPathVariableTest.class);
	}

	/**
	 * Test IPathVariableManager#getPathVariableNames
	 */
	public void testGetPathVariableNames() {
		String[] names = null;

		// should be empty to start
		assertTrue("0.0", manager.getPathVariableNames().length == 0);

		// add one
		try {
			manager.setValue("one", getRandomLocation());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		names = manager.getPathVariableNames();
		assertTrue("1.1", names.length == 1);
		assertTrue("1.2", names[0].equals("one"));

		// add another
		try {
			manager.setValue("two", Path.ROOT);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		names = manager.getPathVariableNames();
		assertTrue("2.1", names.length == 2);
		assertTrue("2.2", contains(names, "one"));
		assertTrue("2.3", contains(names, "two"));

		// remove one
		try {
			manager.setValue("one", null);
		} catch (CoreException e) {
			fail("3.0", e);
		}
		names = manager.getPathVariableNames();
		assertTrue("3.1", names.length == 1);
		assertTrue("3.2", names[0].equals("two"));

		// remove the last one	
		try {
			manager.setValue("two", null);
		} catch (CoreException e) {
			fail("4.0", e);
		}
		names = manager.getPathVariableNames();
		assertTrue("4.1", names.length == 0);
	}

	/**
	 * Test IPathVariableManager#getValue and IPathVariableManager#setValue
	 */
	public void testGetSetValue() {
		boolean WINDOWS = java.io.File.separatorChar == '\\';
		IPath pathOne = WINDOWS ? new Path("C:\\temp") : new Path("/temp");
		IPath pathTwo = new Path("/blort/backup");
		//add device if necessary
		pathTwo = new Path(pathTwo.toFile().getAbsolutePath());
		IPath pathOneEdit = WINDOWS ? new Path("D:/foobar") : new Path("/foobar");

		// nothing to begin with	
		assertNull("0.0", manager.getValue("one"));

		// add a value to the table
		try {
			manager.setValue("one", pathOne);
		} catch (CoreException e) {
			fail("1.0", e);
		}
		IPath value = manager.getValue("one");
		assertNotNull("1.1", value);
		assertEquals("1.2", pathOne, value);

		// add another value
		try {
			manager.setValue("two", pathTwo);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		value = manager.getValue("two");
		assertNotNull("2.1", value);
		assertEquals("2.2", pathTwo, value);

		// edit the first value
		try {
			manager.setValue("one", pathOneEdit);
		} catch (CoreException e) {
			fail("3.0", e);
		}
		value = manager.getValue("one");
		assertNotNull("3.1", value);
		assertTrue("3.2", pathOneEdit.equals(value));

		// setting with value == null will remove
		try {
			manager.setValue("one", null);
		} catch (CoreException e) {
			fail("4.0", e);
		}
		assertNull("4.1", manager.getValue("one"));

		// set values with bogus names 
		try {
			manager.setValue("ECLIPSE$HOME", Path.ROOT);
			fail("5.0 Accepted invalid variable name in setValue()");
		} catch (CoreException ce) {
			// success
		}

		// set value with relative path
		try {
			manager.setValue("one", new Path("foo/bar"));
			fail("5.0 Accepted invalid variable value in setValue()");
		} catch (CoreException ce) {
			// success
		}

		// set invalid value (with invalid segment)
		if (WINDOWS) {
			String invalidPathString = "C:/a/\\::/b";
			IPath invalidPath = Path.fromPortableString(invalidPathString);
			assertTrue("6.0", invalidPath.isAbsolute());
			assertTrue("6.1", !Path.EMPTY.isValidPath(invalidPathString));
			assertTrue("6.2", !manager.validateValue(invalidPath).isOK());
			try {
				manager.setValue("one", invalidPath);
				fail("6.3 Accepted invalid variable value in setValue()");
			} catch (CoreException ce) {
				// success
			}
		}

	}

	/**
	 * Test IPathVariableManager#isDefined
	 */
	public void testIsDefined() {
		assertTrue("0.0", !manager.isDefined("one"));
		try {
			manager.setValue("one", Path.ROOT);
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertTrue("1.1", manager.isDefined("one"));
		try {
			manager.setValue("one", null);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		assertTrue("2.1", !manager.isDefined("one"));
	}

	/**
	 * Test IPathVariableManager#resolvePath
	 */
	public void testResolvePath() {
		final boolean WINDOWS = java.io.File.separatorChar == '\\';
		IPath pathOne = WINDOWS ? new Path("c:/temp/foo") : new Path("/temp/foo");
		IPath pathTwo = new Path("/blort/backup");
		//add device if necessary
		pathTwo = new Path(pathTwo.toFile().getAbsolutePath());

		try {
			// for WINDOWS - the device id for windows will be changed to upper case 
			// in the variable stored in the manager
			manager.setValue("one", pathOne);
		} catch (CoreException e) {
			fail("0.1", e);
		}
		try {
			manager.setValue("two", pathTwo);
		} catch (CoreException e) {
			fail("0.2", e);
		}

		// one substitution
		IPath path = new Path("one/bar");
		IPath expected = new Path("/temp/foo/bar").setDevice(WINDOWS ? "C:" : null);
		IPath actual = manager.resolvePath(path);
		assertEquals("1.0", expected, actual);

		// another substitution
		path = new Path("two/myworld");
		expected = new Path("/blort/backup/myworld");
		expected = new Path(expected.toFile().getAbsolutePath());
		actual = manager.resolvePath(path);
		assertEquals("2.0", expected, actual);

		// variable not defined
		path = new Path("three/nothere");
		expected = path;
		actual = manager.resolvePath(path);
		assertEquals("3.0", expected, actual);

		// device
		path = new Path("/one").setDevice(WINDOWS ? "C:" : null);
		expected = path;
		actual = manager.resolvePath(path);
		assertEquals("4.0", expected, actual);

		// device2
		if (WINDOWS) {
			path = new Path("C:two");
			expected = path;
			actual = manager.resolvePath(path);
			assertEquals("5.0", expected, actual);
		}

		// absolute
		path = new Path("/one");
		expected = path;
		actual = manager.resolvePath(path);
		assertEquals("6.0", expected, actual);
		
		// just resolving, check if the variable stored in the manager is canonicalized
		if (WINDOWS) {
			path = new Path("one");
			expected = FileUtil.canonicalPath(pathOne);
			actual = manager.resolvePath(path);
			// the path stored in the manager is canonicalized, so the device id of actual will be upper case
			assertEquals("7.0", expected, actual);
		}

		// null
		path = null;
		assertNull("7.0", manager.resolvePath(path));

	}

	/**
	 * Test IPathVariableManager#testValidateName
	 */
	public void testValidateName() {

		// valid names 	
		assertTrue("0.0", manager.validateName("ECLIPSEHOME").isOK());
		assertTrue("0.1", manager.validateName("ECLIPSE_HOME").isOK());
		assertTrue("0.2", manager.validateName("ECLIPSE_HOME_1").isOK());
		assertTrue("0.3", manager.validateName("_").isOK());

		// invalid names
		assertTrue("1.0", !manager.validateName("1FOO").isOK());
		assertTrue("1.1", !manager.validateName("FOO%BAR").isOK());
		assertTrue("1.2", !manager.validateName("FOO$BAR").isOK());
		assertTrue("1.3", !manager.validateName(" FOO").isOK());
		assertTrue("1.4", !manager.validateName("FOO ").isOK());

	}

	/**
	 * Test IPathVariableManager#addChangeListener and IPathVariableManager#removeChangeListener
	 */
	public void testListeners() {
		PathVariableChangeVerifier listener = new PathVariableChangeVerifier();
		manager.addChangeListener(listener);
		IPath pathOne = new Path("/blort/foobar");
		//add device if necessary
		pathOne = new Path(pathOne.toFile().getAbsolutePath());
		IPath pathOneEdit = pathOne.append("myworld");

		try {

			// add a variable
			try {
				manager.setValue("one", pathOne);
			} catch (CoreException e) {
				fail("1.0", e);
			}
			listener.addExpectedEvent(IPathVariableChangeEvent.VARIABLE_CREATED, "one", pathOne);
			try {
				listener.verify();
			} catch (PathVariableChangeVerifier.VerificationFailedException e) {
				fail("1.1", e);
			}

			// change a variable
			listener.reset();
			try {
				manager.setValue("one", pathOneEdit);
			} catch (CoreException e) {
				fail("2.0", e);
			}
			listener.addExpectedEvent(IPathVariableChangeEvent.VARIABLE_CHANGED, "one", pathOneEdit);
			try {
				listener.verify();
			} catch (PathVariableChangeVerifier.VerificationFailedException e) {
				fail("2.1", e);
			}

			// remove a variable
			listener.reset();
			try {
				manager.setValue("one", null);
			} catch (CoreException e) {
				fail("3.0", e);
			}
			listener.addExpectedEvent(IPathVariableChangeEvent.VARIABLE_DELETED, "one", null);
			try {
				listener.verify();
			} catch (PathVariableChangeVerifier.VerificationFailedException e) {
				fail("3.1", e);
			}

		} finally {
			manager.removeChangeListener(listener);
		}
	}

	boolean contains(Object[] array, Object obj) {
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(obj))
				return true;
		return false;
	}

	/**
	 * Ensure there are no path variables in the workspace.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		String[] names = manager.getPathVariableNames();
		for (int i = 0; i < names.length; i++) {
			manager.setValue(names[i], null);
		}
	}

}
