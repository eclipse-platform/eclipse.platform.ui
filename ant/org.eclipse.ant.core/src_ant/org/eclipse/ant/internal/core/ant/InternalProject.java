/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.core.ant;

/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
 * less garbage generated. This class is not used on Ant 1.6 and newer
 * due to the improvements in lazy loading of these Ant versions.
 * 
 * Only three tasks are loaded (property, taskdef and 
 * typedef: three tasks that can be defined outside of a target on Ant 1.5.1 or older).
 *
 * Datatypes are loaded if requested.
 * 
 * Derived from the original Ant Project class
 */
public class InternalProject extends Project {

	private Hashtable typeNameToClass = null;
	
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
		if (typeNameToClass == null) {
			initializeTypes();
		}
		Class typeClass = (Class) typeNameToClass.get(typeName);

		if (typeClass == null) {
			return null;
		}

		try {
			Constructor ctor = null;
			boolean noArg = false;
			// DataType can have a "no arg" constructor or take a single
			// Project argument.
			try {
				ctor = typeClass.getConstructor(new Class[0]);
				noArg = true;
			} catch (NoSuchMethodException nse) {
				ctor = typeClass.getConstructor(new Class[] { Project.class });
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
	 * Initialize the mapping of data type name to data type classname
	 */
	private void initializeTypes() {
		typeNameToClass = new Hashtable(18);
		String dataDefs = "/org/apache/tools/ant/types/defaults.properties"; //$NON-NLS-1$
		try {
			Properties props = new Properties();
			InputStream in = Project.class.getResourceAsStream(dataDefs);
			if (in == null) {
				return;
			}
			props.load(in);
			in.close();

			Enumeration enumeration = props.propertyNames();
			while (enumeration.hasMoreElements()) {
				String typeName = (String) enumeration.nextElement();
				String className = props.getProperty(typeName);
				try {
					Class typeClass= Class.forName(className);
					typeNameToClass.put(typeName, typeClass);
				} catch (NoClassDefFoundError e) {
					//ignore
				} catch (ClassNotFoundException c) {
					//ignore
				}
			}
		} catch (IOException ioe) {
			return;
		}

	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#getDataTypeDefinitions()
	 */
	public Hashtable getDataTypeDefinitions() {
		if (typeNameToClass == null) {
			initializeTypes();
		}
		return typeNameToClass;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#addDataTypeDefinition(java.lang.String, java.lang.Class)
	 */
	public void addDataTypeDefinition(String typeName, Class typeClass) {
		getDataTypeDefinitions();
		typeNameToClass.put(typeName, typeClass);
	}
}
