package org.eclipse.ant.core;

import org.eclipse.core.runtime.IConfigurationElement;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapterFactory;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import java.util.Map;

/**
 * Static helper functionality for finding the correct compiler adapter to use for the current
 * Javac task.  This class logically wrappers the standard Ant 
 * <code>CompilerAdapterFactory</code>.  It changes the behvaior to use the
 * Ant plug-ins object class registry to find the compiler adapter class.  This is needed
 * because of the platform's class loading strategy (otherwise the Ant plug-in would likely not
 * be able to see the other classes).
 */

public class EclipseCompilerAdapterFactory {

private EclipseCompilerAdapterFactory() {
}
public static CompilerAdapter getCompiler(String compilerType, Task task) throws BuildException {
	try {
		return CompilerAdapterFactory.getCompiler(compilerType, task);
	} catch (BuildException be) {
		if (AntPlugin.getPlugin() == null)
			throw be;
		Map objects = AntPlugin.getPlugin().getObjectExtensions();
		if (objects == null)
			throw be;
		IConfigurationElement declaration = (IConfigurationElement) objects.get(compilerType);
		if (declaration == null)
			throw be;
		String className = declaration.getAttribute(AntPlugin.CLASS);
		try {
			Class clazz = declaration.getDeclaringExtension().getDeclaringPluginDescriptor().getPluginClassLoader().loadClass(className);
			return (CompilerAdapter) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new BuildException(className + " notfound", e);
		} catch (ClassCastException e) {
			throw new BuildException(className + " is not a compiler adapter", e);
		} catch (Exception e) {
			throw new BuildException("Exception creating " + className, e);
		}
	}
}
}
