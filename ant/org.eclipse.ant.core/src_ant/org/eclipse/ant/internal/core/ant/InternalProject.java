package org.eclipse.ant.internal.core.ant;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;

public class InternalProject extends Project {

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

	   String dataDefs = "/org/apache/tools/ant/types/defaults.properties";

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
	   try {
		   Properties props = new Properties();
		   InputStream in = Project.class.getResourceAsStream(dataDefs);
		   if (in == null) {
			   throw new BuildException("Can't load default datatype list");
		   }
		   props.load(in);
		   in.close();

		   Enumeration enum = props.propertyNames();
		   while (enum.hasMoreElements()) {
			   String key = (String) enum.nextElement();
			   String value = props.getProperty(key);
			   try {
				   Class dataClass = Class.forName(value);
				   addDataTypeDefinition(key, dataClass);
			   } catch (NoClassDefFoundError ncdfe) {
				   // ignore...
			   } catch (ClassNotFoundException cnfe) {
				   // ignore...
			   }
		   }
	   } catch (IOException ioe) {
		   throw new BuildException("Can't load default datatype list");
	   }
	   setSystemProperties();
	}
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#createDataType(java.lang.String)
	 */
	public Object createDataType(String typeName) throws BuildException {
	   Class c = (Class) getDataTypeDefinitions().get(typeName);

	   if (c == null) {
		   return null;
	   }

	   try {
		   java.lang.reflect.Constructor ctor = null;
		   boolean noArg = false;
		   // DataType can have a "no arg" constructor or take a single
		   // Project argument.
		   try {
			   ctor = c.getConstructor(new Class[0]);
			   noArg = true;
		   } catch (NoSuchMethodException nse) {
			   ctor = c.getConstructor(new Class[] {Project.class});
			   noArg = false;
		   }

		   Object o = null;
		   if (noArg) {
				o = ctor.newInstance(new Object[0]);
		   } else {
				o = ctor.newInstance(new Object[] {this});
		   }
		   if (o instanceof ProjectComponent) {
			   ((ProjectComponent) o).setProject(this);
		   }
		   String msg = "   +DataType: " + typeName;
		   log (msg, MSG_DEBUG);
		   return o;
	   } catch (java.lang.reflect.InvocationTargetException ite) {
		   Throwable t = ite.getTargetException();
		   String msg = "Could not create datatype of type: "
				+ typeName + " due to " + t;
		   throw new BuildException(msg, t);
	   } catch (Throwable t) {
		   String msg = "Could not create datatype of type: "
				+ typeName + " due to " + t;
		   throw new BuildException(msg, t);
	   }
	}

}
