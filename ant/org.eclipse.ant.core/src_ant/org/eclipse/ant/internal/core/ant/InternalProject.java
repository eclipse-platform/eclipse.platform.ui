package org.eclipse.ant.internal.core.ant;

/**********************************************************************
Copyright (c) 2003 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;

/**
 * A subclass of Project to facilitate "faster" parsing with
 * less garbage generated.
 * 
 * Only three tasks are loaded (property, taskdef and 
 * typedef: three tasks that can be defined outside of a target on Ant 1.5.1 or older).
 *
 * Datatypes are loaded as required.
 */
public class InternalProject extends Project {

	Hashtable typeNameToClassName = null;
	/**
	 * 
	 */
	public InternalProject() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#init()
	 */
	public void init() throws BuildException {
		setJavaVersionProperty();

		try {
			Class taskClass = Class.forName("org.apache.tools.ant.taskdefs.Property"); //$NON-NLS-1$
			addTaskDefinition("property", taskClass); //$NON-NLS-1$
			taskClass = Class.forName("org.apache.tools.ant.taskdefs.Typedef"); //$NON-NLS-1$
			addTaskDefinition("typedef", taskClass); //$NON-NLS-1$
			taskClass = Class.forName("org.apache.tools.ant.taskdefs.Taskdef"); //$NON-NLS-1$
			addTaskDefinition("taskdef", taskClass); //$NON-NLS-1$
		} catch (NoClassDefFoundError e) {
			throw new BuildException(InternalAntMessages.getString("InternalAntRunner.Missing_Class"), e); //$NON-NLS-1$
		} catch (ClassNotFoundException c) {
			throw new BuildException(InternalAntMessages.getString("InternalAntRunner.Missing_Class"), c); //$NON-NLS-1$
		}

		setSystemProperties();
	}

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#createDataType(java.lang.String)
	 */
	public Object createDataType(String typeName) throws BuildException {
		if (typeNameToClassName == null) {
			initializeTypes();
		}
		String className = (String) typeNameToClassName.get(typeName);

		Class c = null;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException e) {
		}

		if (c == null) {
			return null;
		}

		try {
			Constructor ctor = null;
			boolean noArg = false;
			// DataType can have a "no arg" constructor or take a single
			// Project argument.
			try {
				ctor = c.getConstructor(new Class[0]);
				noArg = true;
			} catch (NoSuchMethodException nse) {
				ctor = c.getConstructor(new Class[] { Project.class });
				noArg = false;
			}

			Object o = null;
			if (noArg) {
				o = ctor.newInstance(new Object[0]);
			} else {
				o = ctor.newInstance(new Object[] { this });
			}
			if (o instanceof ProjectComponent) {
				((ProjectComponent) o).setProject(this);
			}
			return o;
		} catch (InvocationTargetException ite) {
			return null;
		} catch (Throwable t) {
			return null;
		}
	}

	/**
	 * 
	 */
	private void initializeTypes() {
		typeNameToClassName = new Hashtable(18);
		String dataDefs = "/org/apache/tools/ant/types/defaults.properties"; //$NON-NLS-1$
		try {
			Properties props = new Properties();
			InputStream in = Project.class.getResourceAsStream(dataDefs);
			if (in == null) {
				return;
			}
			props.load(in);
			in.close();

			Enumeration enum = props.propertyNames();
			while (enum.hasMoreElements()) {
				String key = (String) enum.nextElement();
				String value = props.getProperty(key);
				typeNameToClassName.put(key, value);
			}
		} catch (IOException ioe) {
			return;
		}

	}
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#getDataTypeDefinitions()
	 */
	public Hashtable getDataTypeDefinitions() {
		if (typeNameToClassName == null) {
			initializeTypes();
		}
		return typeNameToClassName;
	}
}
