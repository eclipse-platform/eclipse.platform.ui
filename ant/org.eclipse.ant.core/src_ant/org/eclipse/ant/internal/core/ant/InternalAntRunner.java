package org.eclipse.ant.internal.core.ant;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999, 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.internal.core.*;
import org.eclipse.ant.internal.core.Policy;
import org.eclipse.ant.internal.core.Task;
import org.eclipse.core.runtime.IConfigurationElement;
/**
 * Eclipse application entry point into Ant. Derived from the original Ant Main class
 * to ensure that the functionality is equivalent when running in the platform.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class InternalAntRunner {

	/**
	 *
	 */
	protected List buildListeners;

	/**
	 *
	 */
	protected String buildFileLocation;

	/** Targets we want to run. */
	protected Vector targets;

	/**
	 *
	 */
	protected Map userProperties;

	/** Our current message output status. Follows Project.MSG_XXX */
	protected int messageOutputLevel = Project.MSG_INFO;

	/** Indicates whether output to the log is to be unadorned. */
	protected boolean emacsMode = false;

	/** Indicates we should only parse and display the project help information */
	protected boolean projectHelp = false;

	/** Stream that we are using for logging */
	private PrintStream out = System.out;

	/** Stream that we are using for logging error messages */
	private PrintStream err = System.err;

	/**
	 * The Ant logger class. There may be only one logger. It will have the
	 * right to use the 'out' PrintStream. The class must implements the BuildLogger
	 * interface.
	 */
	protected String loggerClassname = null;

	// properties
	private static final String PROPERTY_ECLIPSE_RUNNING = "eclipseRunning";

public InternalAntRunner() {
	buildListeners = new ArrayList(5);
}

/**
 * Adds a build listener.
 * 
 * @param buildListener a build listener
 */
public void addBuildListeners(List classNames) {
	List result = new ArrayList(10);
	try {
		for (Iterator iterator = classNames.iterator(); iterator.hasNext();) {
			String className = (String) iterator.next();
			Class listener = Class.forName(className);
			result.add(listener.newInstance());
		}
	} catch (Exception e) {
		throw new BuildException(e);
	}
	this.buildListeners = result;
}

/**
 * Adds a build logger .
 * 
 * @param
 */
public void addBuildLogger(String className) {
	this.loggerClassname = className;
}

/**
 * Adds user properties.
 */
public void addUserProperties(Map properties) {
	this.userProperties = properties;
}

protected void addBuildListeners(Project project) {
	try {
		project.addBuildListener(createLogger());
		for (Iterator iterator = buildListeners.iterator(); iterator.hasNext();)
			project.addBuildListener((BuildListener) iterator.next());
	} catch (Exception e) {
		throw new BuildException(e);
	}
}


protected void setProperties(Project project) {
	project.setUserProperty(PROPERTY_ECLIPSE_RUNNING, "true");
    project.setUserProperty("ant.file" , buildFileLocation);
	project.setUserProperty("ant.version", getAntVersion());
    if (userProperties == null)
    	return;
    for (Iterator iterator = userProperties.entrySet().iterator(); iterator.hasNext();) {
		Map.Entry entry = (Map.Entry) iterator.next();
		project.setUserProperty((String) entry.getKey(), (String) entry.getValue());
	}
}

protected void setTasks(Project project) {
	List tasks = AntCorePlugin.getPlugin().getPreferences().getTasks();
	if (tasks == null)
		return;
	try {
		for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
			Task task = (Task) iterator.next();
			Class taskClass = Class.forName(task.getClassName());
			project.addTaskDefinition(task.getTaskName(), taskClass);
		}
	} catch (Exception e) {
		throw new BuildException(e);
	}
}

protected void setTypes(Project project) {
	List types = AntCorePlugin.getPlugin().getPreferences().getTypes();
	if (types == null)
		return;
	try {
		for (Iterator iterator = types.iterator(); iterator.hasNext();) {
			Type type = (Type) iterator.next();
			Class typeClass = Class.forName(type.getClassName());
			project.addDataTypeDefinition(type.getTypeName(), typeClass);
		}
	} catch (Exception e) {
		throw new BuildException(e);
	}
}


/**
 * Parses the build script and adds necessary information into
 * the given project.
 */
protected void parseScript(Project project) {
	File buildFile = new File(buildFileLocation);
	ProjectHelper.configureProject(project, buildFile);
}

/**
 * Runs the build script.
 */
public void run() {
	Project project = new Project();
	Throwable error = null;
    PrintStream originalErr = System.err;
    PrintStream originalOut = System.out;
	try {
        System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
        System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
		project.log(Policy.bind("label.buildFile", buildFileLocation));
        fireBuildStarted(project);
		project.init();
		addBuildListeners(project);
		setProperties(project);
		setTasks(project);
		setTypes(project);
		parseScript(project);
		if (projectHelp) {
			printHelp(project);
			return;
		}
		if (targets != null && !targets.isEmpty())
			project.executeTargets(targets);
		else
			project.executeTarget(project.getDefaultTarget());
	} catch(RuntimeException e) {
        error = e;
        throw e;
    } catch(Error e) {
        error = e;
        throw e;
	} finally {
        System.setErr(originalErr);
        System.setOut(originalOut);
		fireBuildFinished(project, error);
	}
}

/**
 * Logs a message with the client that lists the targets
 * in a project
 * 
 * @param project the project to list targets from
 */
private void printTargets(Project project) {
    // find the target with the longest name
    int maxLength = 0;
    Enumeration ptargets = project.getTargets().elements();
    String targetName;
    String targetDescription;
    Target currentTarget;
    // split the targets in top-level and sub-targets depending
    // on the presence of a description
    Vector topNames = new Vector();
    Vector topDescriptions = new Vector();
    Vector subNames = new Vector();

    while (ptargets.hasMoreElements()) {
        currentTarget = (Target)ptargets.nextElement();
        targetName = currentTarget.getName();
        targetDescription = currentTarget.getDescription();
        // maintain a sorted list of targets
        if (targetDescription == null) {
            int pos = findTargetPosition(subNames, targetName);
            subNames.insertElementAt(targetName, pos);
        } else {
            int pos = findTargetPosition(topNames, targetName);
            topNames.insertElementAt(targetName, pos);
            topDescriptions.insertElementAt(targetDescription, pos);
            if (targetName.length() > maxLength) {
                maxLength = targetName.length();
            }
        }
    }

    String defaultTarget = project.getDefaultTarget();
    if (defaultTarget != null && !"".equals(defaultTarget)) { // shouldn't need to check but...
        Vector defaultName = new Vector();
        Vector defaultDesc = null;
        defaultName.addElement(defaultTarget);

        int indexOfDefDesc = topNames.indexOf(defaultTarget);
        if (indexOfDefDesc >= 0) {
            defaultDesc = new Vector();
            defaultDesc.addElement(topDescriptions.elementAt(indexOfDefDesc));
        }
        printTargets(project, defaultName, defaultDesc, Policy.bind("label.defaultTarget"), maxLength);

    }

    printTargets(project, topNames, topDescriptions, Policy.bind("label.mainTargets"), maxLength);
    printTargets(project, subNames, null, Policy.bind("label.subTargets"), 0);
}

/**
 * Returns the appropriate insertion index for a given string into a sorted collection.
 * 
 * @return the insertion index
 * @param names the initial collection of sorted strings
 * @param name the string whose insertion index into <code>names</code> is to be determined
 */
private int findTargetPosition(Vector names, String name) {
	int result = names.size();
	for (int i = 0; i < names.size() && result == names.size(); i++) {
		if (name.compareTo((String) names.elementAt(i)) < 0)
			result = i;
	}
	return result;
}

/**
 * Logs a message with the client that lists the target names and optional descriptions
 * 
 * @param names the targets names
 * @param descriptions the corresponding descriptions
 * @param heading the message heading
 * @param maxlen maximum length that can be allocated for a name
 */
private void printTargets(Project project, Vector names, Vector descriptions, String heading, int maxlen) {
	// now, start printing the targets and their descriptions
	String lSep = System.getProperty("line.separator");
	// got a bit annoyed that I couldn't find a pad function
	String spaces = "    ";
	while (spaces.length() < maxlen) {
		spaces += spaces;
	}
	StringBuffer msg = new StringBuffer();
	msg.append(heading + lSep + lSep);
	for (int i= 0; i < names.size(); i++) {
		msg.append(" ");
		msg.append(names.elementAt(i));
		if (descriptions != null) {
			msg.append(spaces.substring(0, maxlen - ((String) names.elementAt(i)).length() + 2));
			msg.append(descriptions.elementAt(i));
		}
		msg.append(lSep);
	}
	logMessage(project, msg.toString(), Project.MSG_INFO);
}

/**
 * Invokes the building of a project object and executes a build using either a given
 * target or the default target.
 *
 * @param argArray the command line arguments
 * @exception execution exceptions
 */
public void run(Object argArray) throws Exception {
	boolean success = processCommandLine(getArrayList((String[]) argArray));
    if (!success)
        return;
	try {
		run();
	} catch (Exception e) {
		printMessage(e);
		throw e;
	}
}

/**
 * Prints the message of the Throwable if it is not null.
 * 
 * @param t the throwable whose message is to be displayed
 */
protected void printMessage(Throwable t) {
	String message= t.getMessage();
	if (message != null)
		logMessage(null, message, Project.MSG_ERR);
}

/**
 * Creates and returns the default build logger for logging build events to the ant log.
 * 
 * @return the default build logger for logging build events to the ant log
 */
protected BuildLogger createLogger() {
	BuildLogger logger = null;
	if (loggerClassname != null) {
		try {
			logger = (BuildLogger) (Class.forName(loggerClassname).newInstance());
		} catch (Exception e) {
			String message = Policy.bind("exception.cannotCreateLogger", loggerClassname);
			logMessage(null, message, Project.MSG_ERR);
			throw new BuildException(e);
		}
	} else {
		logger = new DefaultLogger();
	}
	logger.setMessageOutputLevel(messageOutputLevel);
	logger.setOutputPrintStream(out);
	logger.setErrorPrintStream(err);
	logger.setEmacsMode(emacsMode);
	return logger;
}

/**
 * We only have to do this because Project.fireBuildStarted is protected. If it becomes
 * public we should remove this method and call the appropriate one.
 */
private void fireBuildStarted(Project project) {
    BuildEvent event = new BuildEvent(project);
    for (Iterator iterator = buildListeners.iterator(); iterator.hasNext();) {
        BuildListener listener = (BuildListener) iterator.next();
        listener.buildStarted(event);
	}
}

/**
 * We only have to do this because Project.fireBuildFinished is protected. If it becomes
 * public we should remove this method and call the appropriate one.
 */
private void fireBuildFinished(Project project, Throwable error) {
    BuildEvent event = new BuildEvent(project);
    event.setException(error);
    for (Iterator iterator = buildListeners.iterator(); iterator.hasNext();) {
        BuildListener listener = (BuildListener) iterator.next();
        listener.buildFinished(event);
	}
}

protected void logMessage(Project project, String message, int priority) {
	if (project == null)
		project = new Project();
    BuildEvent event = new BuildEvent(project);
    event.setMessage(message, priority);
    for (Iterator iterator = buildListeners.iterator(); iterator.hasNext();) {
        BuildListener listener = (BuildListener) iterator.next();
        listener.messageLogged(event);
	}
}

/**
 * Sets the buildFileLocation.
 * 
 * @param buildFileLocation the file system location of the build file
 */
public void setBuildFileLocation(String buildFileLocation) {
	this.buildFileLocation = buildFileLocation;
}

/**
 * Sets the message output level. Use -1 for none.
 * 
 * @param 
 */
public void setMessageOutputLevel(int level) {
	this.messageOutputLevel = level;
}

/**
 * Sets the execution targets.
 * 
 */
public void setExecutionTargets(Vector executiongTargets) {
	targets = executiongTargets;
}

protected static String getAntVersion() throws BuildException {
    try {
        Properties props = new Properties();
        InputStream in = Main.class.getResourceAsStream("/org/apache/tools/ant/version.txt");
        props.load(in);
        in.close();
        
        StringBuffer msg = new StringBuffer();
        msg.append(Policy.bind("usage.antVersion"));
        msg.append(props.getProperty("VERSION") + " ");
        msg.append(Policy.bind("usage.compiledOn"));
        msg.append(props.getProperty("DATE"));
        return msg.toString();
    } catch (IOException ioe) {
        throw new BuildException(Policy.bind("exception.cannotLoadVersionInfo", ioe.getMessage()));
    } catch (NullPointerException npe) {
        throw new BuildException(Policy.bind("exception.cannotLoadVersionInfo", ""));
    }
}

/**
 * Looks for interesting command line arguments. Returns true is it is OK to run
 * the script.
 */
protected boolean processCommandLine(List commands) {
	// looks for flag-like commands
	if (commands.remove("-help")) {
		printUsage();
		return false;
	} 
	if (commands.remove("-version")) {
		printVersion();
		return false;
	} 
	if (commands.remove("-quiet") || commands.remove("-q")) {
		messageOutputLevel = Project.MSG_WARN;
	} 
	if (commands.remove("-verbose") || commands.remove("-v")) {
		messageOutputLevel = Project.MSG_VERBOSE;
	} 
	if (commands.remove("-debug")) {
		messageOutputLevel = Project.MSG_DEBUG;
	}
	if (commands.remove("-emacs")) {
		emacsMode = true;
	}
	if (commands.remove("-projecthelp")) {
		projectHelp = true;
	} 
	
	// look for argumments
	String[] args = getArguments(commands, "-logfile");
	if (args == null) {
		args = getArguments(commands, "-l");
	}
	if (args != null) {
		try {
			File logFile = new File(args[0]);
			out = new PrintStream(new FileOutputStream(logFile));
			err = out;
		} catch (IOException e) {
			logMessage(null, Policy.bind("exception.cannotWriteToLog"), Project.MSG_INFO);
			return false;
		}
	}

	args = getArguments(commands, "-buildfile");
	if (args == null) {
		args = getArguments(commands, "-file");
		if (args == null)
			args = getArguments(commands, "-f");
	}
	if (args != null) {
		buildFileLocation = args[0];
		targets = new Vector();
		for (int i = 1; i < args.length; i++)
			targets.add(args[i]);
	}

	args = getArguments(commands, "-listener");
	if (args != null)
		buildListeners.add(args[0]);

	args = getArguments(commands, "-logger");
	if (args != null)
		loggerClassname = args[0];

	processProperties(commands);

	return true;
}

protected void processProperties(List commands) {
	userProperties = new HashMap(10);
	String[] args = (String[]) commands.toArray(new String[commands.size()]);
	for (int i = 0; i < args.length; i++) {
		String arg = args[i];
		if (arg.startsWith("-D")) {

			/* Interestingly enough, we get to here when a user
			 * uses -Dname=value. However, in some cases, the JDK
			 * goes ahead * and parses this out to args
			 *   {"-Dname", "value"}
			 * so instead of parsing on "=", we just make the "-D"
			 * characters go away and skip one argument forward.
			 *
			 * I don't know how to predict when the JDK is going
			 * to help or not, so we simply look for the equals sign.
			 */

			String name = arg.substring(2, arg.length());
			String value = null;
			int posEq = name.indexOf("=");
			if (posEq > 0) {
				value = name.substring(posEq + 1);
				name = name.substring(0, posEq);
			} else if (i < args.length - 1)
				value = args[++i];
	
			userProperties.put(name, value);
			commands.remove(args[i]);
		}
	}
}

/**
 * Print the project description, if any
 */
protected void printHelp(Project project) {
	if (project.getDescription() != null)
		logMessage(project, project.getDescription(), Project.MSG_INFO);
	printTargets(project);
}

/**
 * Logs a message with the client indicating the version of <b>Ant</b> that this class
 * fronts.
 */
protected void printVersion() {
 	logMessage(null, getAntVersion(), Project.MSG_INFO);
}

/**
 * Logs a message with the client outlining the usage of <b>Ant</b>.
 */
protected void printUsage() {
	String lSep = System.getProperty("line.separator");
	StringBuffer msg = new StringBuffer();
	msg.append("ant [" + Policy.bind("usage.options") + "] [" 
				+ Policy.bind("usage.target") + " ["
				+ Policy.bind("usage.target") + "2 ["
				+ Policy.bind("usage.target") + "3] ...]]" + lSep);
	msg.append(Policy.bind("usage.Options") + ": " + lSep);
	msg.append("  -help                  " + Policy.bind("usage.printMessage") + lSep);
	msg.append("  -projecthelp           " + Policy.bind("usage.projectHelp") + lSep);
	msg.append("  -version               " + Policy.bind("usage.versionInfo") + lSep);
	msg.append("  -quiet                 " + Policy.bind("usage.beQuiet") + lSep);
	msg.append("  -verbose               " + Policy.bind("usage.beVerbose") + lSep);
	msg.append("  -debug                 " + Policy.bind("usage.printDebugInfo") + lSep);
	msg.append("  -emacs                 " + Policy.bind("usage.emacsLog") + lSep);
	msg.append("  -logfile <file>        " + Policy.bind("usage.useFile") + lSep);
	msg.append("  -logger <classname>    " + Policy.bind("usage.logClass") + lSep);
	msg.append("  -listener <classname>  " + Policy.bind("usage.listenerClass") + lSep);
	msg.append("  -buildfile <file>      " + Policy.bind("usage.fileToBuild") + lSep);
	msg.append("  -D<property>=<value>   " + Policy.bind("usage.propertiesValues") + lSep);
//	msg.append("  -find <file>           " + Policy.bind("usage.findFileToBuild") + lSep);
	
	logMessage(null, msg.toString(), Project.MSG_INFO);
}

/**
 * From a command line list, get the array of arguments of a given parameter.
 * The parameter and its arguments are removed from the list.
 * @return null if the parameter is not found or has no arguments
 */
protected String[] getArguments(List commands, String param) {
	int index = commands.indexOf(param);
	if (index == -1)
		return null;
	commands.remove(index);
	if (index == commands.size()) // if this is the last command
		return null;
	List args = new ArrayList(commands.size());
	while (index < commands.size()) { // while not the last command
		String command = (String) commands.get(index);
		if (command.startsWith("-")) // is it a new parameter?
			break;
		args.add(command);
		commands.remove(index);
	}
	if (args.isEmpty())
		return null;
	return (String[]) args.toArray(new String[args.size()]);
}

/**
 * Helper method to ensure an array is converted into an ArrayList.
 */
private ArrayList getArrayList(String[] args) {
	// We could be using Arrays.asList() here, but it does not specify
	// what kind of list it will return. We do need a list that
	// implements the method List.remove(int) and ArrayList does.
	ArrayList result = new ArrayList(args.length);
	for (int i = 0; i < args.length; i++)
		result.add(args[i]);
	return result;
}
}