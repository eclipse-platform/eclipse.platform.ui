package org.eclipse.ant.internal.core.ant;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
import java.text.MessageFormat;
import java.util.*;

import org.apache.tools.ant.*;
import org.eclipse.ant.core.*;
import org.eclipse.core.runtime.*;

/**
 * Eclipse application entry point into Ant. Derived from the original Ant Main class
 * to ensure that the functionality is equivalent when running in the platform.
 */
public class InternalAntRunner {

	public InternalAntRunner() {
	}

	protected IProgressMonitor monitor;

	protected List buildListeners;

	protected String buildFileLocation;

	/** 
	 * Targets we want to run.	 */
	protected Vector targets;

	protected Map userProperties;
	
	protected Project currentProject;
	
	protected BuildLogger buildLogger= null;
	
	/**
	 * Cache of the Ant version information when it has been loaded	 */
	protected static String antVersion= null;

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
	 * right to use the 'out' PrintStream. The class must implement the BuildLogger
	 * interface.
	 */
	protected String loggerClassname = null;

	/** Extra arguments to be parsed as command line arguments. */
	protected String[] extraArguments = null;

	private static final String PROPERTY_ECLIPSE_RUNNING = "eclipse.running"; //$NON-NLS-1$

	/**
	 * Adds a build listener.
	 * 
	 * @param buildListener a build listener
	 */
	public void addBuildListeners(List classNames) {
		if (buildListeners == null) {
			buildListeners = new ArrayList(classNames.size());
		}
		buildListeners.addAll(classNames);
	}

	/**
	 * Adds a build logger.
	 */
	public void addBuildLogger(String className) {
		loggerClassname = className;
	}

	/**
	 * Adds user properties.
	 */
	public void addUserProperties(Map properties) {
		userProperties = properties;
	}

	protected void addBuildListeners(Project project) {
		String className= null;
		try {
			BuildLogger logger= createLogger();
			if (logger != null) {
				project.addBuildListener(logger);
			}
			if (buildListeners != null) {
				for (Iterator iterator = buildListeners.iterator(); iterator.hasNext();) {
					className = (String) iterator.next();
					Class listener = Class.forName(className);
					project.addBuildListener((BuildListener) listener.newInstance());
				}
			}
		} catch (ClassCastException e) {
			String message = MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.{0}_which_was_specified_to_be_a_build_listener_is_not_an_instance_of_org.apache.tools.ant.BuildListener._1"), new String[]{className}); //$NON-NLS-1$
			logMessage(null, message, Project.MSG_ERR);
			throw new BuildException(message);
		} catch (BuildException e) {
			throw e;
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	protected void setProperties(Project project) {
		project.setUserProperty(PROPERTY_ECLIPSE_RUNNING, "true"); //$NON-NLS-1$
		project.setUserProperty("ant.file", getBuildFileLocation()); //$NON-NLS-1$
		project.setUserProperty("ant.version", getAntVersion()); //$NON-NLS-1$
		
		if (userProperties == null) {
			return;
		}
		for (Iterator iterator = userProperties.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			project.setUserProperty((String) entry.getKey(), (String) entry.getValue());
		}
	}

	protected void setTasks(Project project) {
		List tasks = AntCorePlugin.getPlugin().getPreferences().getTasks();
		if (tasks == null) {
			return;
		}
		try {
			for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
				org.eclipse.ant.core.Task task = (org.eclipse.ant.core.Task) iterator.next();
				Class taskClass = Class.forName(task.getClassName());
				project.addTaskDefinition(task.getTaskName(), taskClass);
			}
		} catch (ClassNotFoundException e) {
			throw new BuildException(e);
		}
	}

	protected void setTypes(Project project) {
		List types = AntCorePlugin.getPlugin().getPreferences().getTypes();
		if (types == null) {
			return;
		}
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
		File buildFile = new File(getBuildFileLocation());
		if (!buildFile.exists()) {
			throw new BuildException(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Buildfile__{0}_does_not_exist_!_1"), //$NON-NLS-1$
						 new String[]{buildFile.getAbsolutePath()}));
		}
		ProjectHelper.configureProject(project, buildFile);
	}

	/**
	 * Gets all the target information from the build script.
	 * Returns a two dimension array. Each row represents a
	 * target, where the first column is the name and the
	 * second column is the description. The last row is
	 * special and represents the name of the default target.
	 * This default target name is in the first column, the
	 * second column is null. Note, the default name can be
	 * null.
	 */
	public String[][] getTargets() {
		// create a project and initialize it
		Project antProject = new Project();
		antProject.init();
		antProject.setProperty("ant.file", getBuildFileLocation()); //$NON-NLS-1$
		parseScript(antProject);
		String defaultName = antProject.getDefaultTarget();
		Collection targets = antProject.getTargets().values();
		String[][] infos = new String[targets.size() + 1][2];
		Iterator enum = targets.iterator();
		int i = 0;
		while (enum.hasNext()) {
			Target target = (Target) enum.next();
			infos[i][0] = target.getName();
			infos[i][1] = target.getDescription();
			i++;
		}
		infos[i][0] = defaultName;
		return infos;
	}

	/**
	 * Runs the build script.
	 */
	public void run() {
		run(getArrayList(extraArguments));
	}

	protected void printArguments(Project project) {
		if ((messageOutputLevel != Project.MSG_DEBUG) && (messageOutputLevel != Project.MSG_VERBOSE)) {
			return;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < extraArguments.length; i++) {
			sb.append(extraArguments[i]);
			sb.append(' ');
		}
		project.log(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Arguments__{0}_2"), new String[]{sb.toString().trim()})); //$NON-NLS-1$
	}

	protected void createMonitorBuildListener(Project project) {
		if (monitor == null) {
			return;
		}
		List chosenTargets = targets;
		if (chosenTargets == null || chosenTargets.isEmpty()) {
			chosenTargets = new ArrayList(1);
			String defltTarget= project.getDefaultTarget();
			if (defltTarget != null) {
				chosenTargets.add(defltTarget);
			}
		}
		project.addBuildListener(new ProgressBuildListener(project, chosenTargets, monitor));
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
		List topNames = new ArrayList();
		List topDescriptions = new ArrayList();
		List subNames = new ArrayList();

		while (ptargets.hasMoreElements()) {
			currentTarget = (Target) ptargets.nextElement();
			targetName = currentTarget.getName();
			targetDescription = currentTarget.getDescription();
			if (targetDescription == null) {
				subNames.add(targetName);
			} else {
				topNames.add(targetName);
				topDescriptions.add(targetDescription);
				if (targetName.length() > maxLength) {
					maxLength = targetName.length();
				}
			}
		}

		Collections.sort(subNames);
		Collections.sort(topNames);
		Collections.sort(topDescriptions);
		
		String defaultTarget = project.getDefaultTarget();
		if (defaultTarget != null && !"".equals(defaultTarget)) { // shouldn't need to check but... //$NON-NLS-1$
			List defaultName = new ArrayList(1);
			List defaultDesc = null;
			defaultName.add(defaultTarget);

			int indexOfDefDesc = topNames.indexOf(defaultTarget);
			if (indexOfDefDesc >= 0) {
				defaultDesc = new ArrayList(1);
				defaultDesc.add(topDescriptions.get(indexOfDefDesc));
			}
			printTargets(project, defaultName, defaultDesc, InternalAntMessages.getString("InternalAntRunner.Default_target__3"), maxLength); //$NON-NLS-1$

		}

		printTargets(project, topNames, topDescriptions, InternalAntMessages.getString("InternalAntRunner.Main_targets__4"), maxLength); //$NON-NLS-1$
		printTargets(project, subNames, null, InternalAntMessages.getString("InternalAntRunner.Subtargets__5"), 0); //$NON-NLS-1$
	}

	/**
	 * Logs a message with the client that lists the target names and optional descriptions
	 * 
	 * @param names the targets names
	 * @param descriptions the corresponding descriptions
	 * @param heading the message heading
	 * @param maxlen maximum length that can be allocated for a name
	 */
	private void printTargets(Project project, List names, List descriptions, String heading, int maxlen) {
		// now, start printing the targets and their descriptions
		String lSep = System.getProperty("line.separator"); //$NON-NLS-1$
		
		String spaces = "    "; //$NON-NLS-1$
		while (spaces.length() < maxlen) {
			spaces += spaces;
		}
		StringBuffer msg = new StringBuffer();
		msg.append(heading + lSep + lSep);
		for (int i = 0; i < names.size(); i++) {
			msg.append(' ');
			msg.append(names.get(i));
			if (descriptions != null) {
				msg.append(spaces.substring(0, maxlen - ((String) names.get(i)).length() + 2));
				msg.append(descriptions.get(i));
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
		run(getArrayList((String[]) argArray));
	}

	/**
	 * Note that the list passed to this method must support
	 * List#remove(Object)	 */
	protected void run(List argList) {
		setCurrentProject(new Project());
		Throwable error = null;
		PrintStream originalErr = System.err;
		PrintStream originalOut = System.out;
		SecurityManager originalSM= System.getSecurityManager();
		
		boolean executeScript= true;
		boolean canceled= false;
		try {
			getCurrentProject().init();
			if (argList != null) {
				executeScript= preprocessCommandLine(argList);
			
				if (!executeScript) {
					return;
				}
				processProperties(argList);
			}
			
			setProperties(getCurrentProject());
			addBuildListeners(getCurrentProject());
			System.setOut(new PrintStream(new DemuxOutputStream(getCurrentProject(), false)));
			System.setErr(new PrintStream(new DemuxOutputStream(getCurrentProject(), true)));

			fireBuildStarted(getCurrentProject());
			
			if (argList != null) {
				try {
					executeScript= processCommandLine(argList);
				} catch (BuildException e) {
					executeScript= false;
					throw e;
				}
			}
			if (!executeScript) {
				return;
			}
			
			getCurrentProject().log(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Build_file__{0}_1"), new String[]{getBuildFileLocation()})); //$NON-NLS-1$

			setTasks(getCurrentProject());
			setTypes(getCurrentProject());
			
			parseScript(getCurrentProject());
			createMonitorBuildListener(getCurrentProject());
			
			if (projectHelp) {
				printHelp(getCurrentProject());
				return;
			}
			
			if (extraArguments != null) {
				printArguments(getCurrentProject());
			}
			System.setSecurityManager(new AntSecurityManager(originalSM));
			
			if (targets != null && !targets.isEmpty()) {
				getCurrentProject().executeTargets(targets);
			} else {
				getCurrentProject().executeTarget(getCurrentProject().getDefaultTarget());
			}
		} catch (OperationCanceledException e) {
			canceled= true;
			logMessage(getCurrentProject(), e.getMessage(), Project.MSG_INFO);
			throw e;
		} catch (AntSecurityException e) {
			//expected
		} catch (RuntimeException e) {
			error = e;
			throw e;
		} catch (Error e) {
			error = e;
			throw e;
		} finally {
			System.setErr(originalErr);
			System.setOut(originalOut);
			System.setSecurityManager(originalSM);
			//close any user specified build log
			if (err != originalErr) {
				err.close();
			}
			if (out != originalOut) {
				out.close();
			}
			if (executeScript && !canceled) {
				fireBuildFinished(getCurrentProject(), error);
			}
		}
	}
	
	/**
	 * Prints the message of the Throwable if it is not null.
	 * 
	 * @param t the throwable whose message is to be displayed
	 */
	protected void printMessage(Throwable t) {
		String message = t.getMessage();
		if (message != null) {
			logMessage(getCurrentProject(), message, Project.MSG_ERR);
		}
	}

	/**
	 * Creates and returns the default build logger for logging build events to the ant log.
	 * 
	 * @return the default build logger for logging build events to the ant log
	 * 		Can return <code>null</code> if no logging is to occur.
	 */
	protected BuildLogger createLogger() {
		if (loggerClassname != null) {
			try {
				buildLogger = (BuildLogger) (Class.forName(loggerClassname).newInstance());
			} catch (ClassCastException e) {
				String message = MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.{0}_which_was_specified_to_perform_logging_is_not_an_instance_of_org.apache.tools.ant.BuildLogger._2"), new String[]{loggerClassname}); //$NON-NLS-1$
				logMessage(null, message, Project.MSG_ERR);
				throw new BuildException(message);
			} catch (Exception e) {
				String message = MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Unable_to_instantiate_logger__{0}_6"), new String[]{loggerClassname}); //$NON-NLS-1$
				logMessage(null, message, Project.MSG_ERR);
				throw new BuildException(message, e);
			}
			buildLogger.setMessageOutputLevel(messageOutputLevel);
			buildLogger.setOutputPrintStream(out);
			buildLogger.setErrorPrintStream(err);
			buildLogger.setEmacsMode(emacsMode);
		} 

		return buildLogger;
	}

	/**
	 * We only have to do this because Project.fireBuildStarted is protected. If it becomes
	 * public we should remove this method and call the appropriate one.
	 */
	private void fireBuildStarted(Project project) {
		BuildEvent event = new BuildEvent(project);
		for (Iterator iterator = project.getBuildListeners().iterator(); iterator.hasNext();) {
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
		
		if(usingXmlLogger()) {
			//generate the log file in the correct location
			String fileName= project.getProperty("XmlLogger.file"); //$NON-NLS-1$
			if (fileName == null) {
				fileName= "log.xml"; //$NON-NLS-1$
			}
			String realPath= new Path(getBuildFileLocation()).toFile().getAbsolutePath();
			IPath path= new Path(realPath);
			path= path.removeLastSegments(1);
			path= path.addTrailingSeparator();
			path= path.append(fileName);
		
			project.setProperty("XmlLogger.file", path.toOSString()); //$NON-NLS-1$
		}
		if (error == null) {
			logMessage(project, InternalAntMessages.getString("InternalAntRunner.BUILD_SUCCESSFUL_1"), Project.MSG_INFO); //$NON-NLS-1$
		} else {
			event.setException(error);
		}
		for (Iterator iterator = project.getBuildListeners().iterator(); iterator.hasNext();) {
			BuildListener listener = (BuildListener) iterator.next();
			listener.buildFinished(event);
		}
	}

	private boolean usingXmlLogger() {
		if (buildLogger instanceof XmlLogger) {
			return true;
		}
		if (buildListeners != null) {
			Enumeration e= getCurrentProject().getBuildListeners().elements();
			while (e.hasMoreElements()) {
				BuildListener element = (BuildListener) e.nextElement();
				if (element instanceof XmlLogger) {
					return true;
				}
			}
		}
		
		return false;
	}

	protected void logMessage(Project project, String message, int priority) {
		if (project != null) {
			project.log(message, priority);	
		} else {
			if (buildListeners != null) {
				project = new Project();
				BuildEvent event = new BuildEvent(project);
				event.setMessage(message, priority);
				//notify the build listeners that are not registered as
				//no project existed
				for (Iterator iterator = buildListeners.iterator(); iterator.hasNext();) {
					try {
						BuildListener listener = (BuildListener) iterator.next();
						listener.messageLogged(event);
					} catch (ClassCastException e) {
						//ignore we could be trying to log that a build listener is the
						//wrong type of class
					}
				}
			}
		}
	}

	/**
	 * Sets the buildFileLocation.
	 * 
	 * @param buildFileLocation the file system location of the build file
	 */
	public void setBuildFileLocation(String buildFileLocation) {
		this.buildFileLocation = buildFileLocation;
		if (getCurrentProject() != null) {
			getCurrentProject().setUserProperty("ant.file", buildFileLocation); //$NON-NLS-1$
		}
	}

	protected String getBuildFileLocation() {
		if (buildFileLocation == null) {
			buildFileLocation = new File("build.xml").getAbsolutePath(); //$NON-NLS-1$
		}
		return buildFileLocation;
	}

	/**
	 * Sets the message output level. Use -1 for none.
	 */
	public void setMessageOutputLevel(int level) {
		messageOutputLevel = level;
	}

	/**
	 * Sets the extra user arguments
	 */
	public void setArguments(String[] args) {
		extraArguments = args;
	}

	/**
	 * Sets the execution targets.
	 */
	public void setExecutionTargets(String[] executionTargets) {
		targets = new Vector(executionTargets.length);
		for (int i = 0; i < executionTargets.length; i++) {
			targets.add(executionTargets[i]);
		}
	}

	protected static String getAntVersion() throws BuildException {
		if (antVersion == null) {
			try {
				Properties props = new Properties();
				InputStream in = Main.class.getResourceAsStream("/org/apache/tools/ant/version.txt"); //$NON-NLS-1$
				props.load(in);
				in.close();

				StringBuffer msg = new StringBuffer();
				msg.append(InternalAntMessages.getString("InternalAntRunner.Ant_version__7")); //$NON-NLS-1$
				msg.append(props.getProperty("VERSION") + ' '); //$NON-NLS-1$ 
				msg.append(InternalAntMessages.getString("InternalAntRunner.compiled_on__8")); //$NON-NLS-1$
				msg.append(props.getProperty("DATE")); //$NON-NLS-1$
				antVersion= msg.toString();
			} catch (IOException ioe) {
				throw new BuildException(MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Could_not_load_the_version_information._{0}_9"), new String[]{ioe.getMessage()})); //$NON-NLS-1$
			} catch (NullPointerException npe) {
				throw new BuildException(InternalAntMessages.getString("InternalAntRunner.Could_not_load_the_version_information._10")); //$NON-NLS-1$
			}
		}
		return antVersion;
	}

	protected boolean preprocessCommandLine(List commands) {
		
		String[] args = getArguments(commands, "-listener"); //$NON-NLS-1$
		if (args != null) {
			if (args.length == 0) {
				throw new BuildException(InternalAntMessages.getString("InternalAntRunner.You_must_specify_a_classname_when_using_the_-listener_argument_1")); //$NON-NLS-1$
			} 
			if (buildListeners == null) {
				buildListeners= new ArrayList(1);
			}
			buildListeners.add(args[0]);
		}

		args = getArguments(commands, "-logger"); //$NON-NLS-1$
		if (args != null) {
			if (args.length == 0) {
				throw new BuildException(InternalAntMessages.getString("InternalAntRunner.You_must_specify_a_classname_when_using_the_-logger_argument_2")); //$NON-NLS-1$
			} 
			loggerClassname = args[0];
		}
		
		return true;
	}
	
	/**
	 * Looks for interesting command line arguments. 
	 * Returns whether it is OK to run the script.
	 */
	protected boolean processCommandLine(List commands) {
		
		if (commands.remove("-help")) { //$NON-NLS-1$
			printUsage();
			return false;
		}
		
		if (commands.remove("-version")) { //$NON-NLS-1$
			printVersion();
			return false;
		}
		
		if (commands.remove("-verbose") || commands.remove("-v")) { //$NON-NLS-1$ //$NON-NLS-2$
			printVersion();
			messageOutputLevel = Project.MSG_VERBOSE;
		}
		
		if (commands.remove("-debug")) { //$NON-NLS-1$
			printVersion();
			messageOutputLevel = Project.MSG_DEBUG;
		}
		
		if (commands.remove("-quiet") || commands.remove("-q")) { //$NON-NLS-1$ //$NON-NLS-2$
			messageOutputLevel = Project.MSG_WARN;
		}

		if (commands.remove("-emacs")) { //$NON-NLS-1$
			emacsMode = true;
		}
		if (commands.remove("-projecthelp")) { //$NON-NLS-1$
			projectHelp = true;
		}
		
		String[] args = getArguments(commands, "-logfile"); //$NON-NLS-1$
		if (args == null) {
			args = getArguments(commands, "-l"); //$NON-NLS-1$
		}
		if (args != null) {
			if (args.length == 0) {
				String message= InternalAntMessages.getString("InternalAntRunner.You_must_specify_a_log_file_when_using_the_-log_argument_3"); //$NON-NLS-1$
				logMessage(currentProject, message, Project.MSG_ERR); 
				throw new BuildException(message);
			} 
			try {
				createLogFile(args[0]);
			} catch (IOException e) {
				// just log message and ignore exception
				logMessage(getCurrentProject(), MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Could_not_write_to_the_specified_log_file__{0}._Make_sure_the_path_exists_and_you_have_write_permissions._2"), new String[]{args[0]}), Project.MSG_ERR); //$NON-NLS-1$
				return false;
			}
		
		}
		
		args = getArguments(commands, "-buildfile"); //$NON-NLS-1$
		if (args == null) {
			args = getArguments(commands, "-file"); //$NON-NLS-1$
			if (args == null) {
				args = getArguments(commands, "-f"); //$NON-NLS-1$
			}
		}
		
		if (args != null) {
			if (args.length == 0) {
				String message= InternalAntMessages.getString("InternalAntRunner.You_must_specify_a_buildfile_when_using_the_-buildfile_argument_4"); //$NON-NLS-1$
				logMessage(currentProject, message, Project.MSG_ERR); 
				throw new BuildException(message);
			} 
			setBuildFileLocation(args[0]);
		}
		
		args= getArguments(commands, "-propertyfile"); //$NON-NLS-1$
		if (args != null) {
			logMessage(currentProject, InternalAntMessages.getString("InternalAntRunner.-propertyfile_option_not_yet_implemented_6"), Project.MSG_INFO); //$NON-NLS-1$
			return false;
		}
		
		args= getArguments(commands, "-inputhandler"); //$NON-NLS-1$
		if (args != null) {
			logMessage(currentProject, InternalAntMessages.getString("InternalAntRunner.-inputhandler_option_not_yet_implemented_8"), Project.MSG_INFO); //$NON-NLS-1$
			return false;
		}
		
		args= getArguments(commands, "-find"); //$NON-NLS-1$
		if (args != null) {
			logMessage(currentProject, InternalAntMessages.getString("InternalAntRunner.-find_option_not_yet_implemented_10"), Project.MSG_INFO); //$NON-NLS-1$
			return false;
		}

		if (commands != null && !commands.isEmpty()) {
			if (!processTargets(commands)) {
				//unrecognized argument
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Checks for targets specified at the command line.
	 * Returns whether execution should continue; false if
	 * an unrecognized argument is encountered.	 */
	protected boolean processTargets(List commands) {
		if (targets == null) {
			targets = new Vector(commands.size());
		}
		for (Iterator iter = commands.iterator(); iter.hasNext();) {
			String arg = (String) iter.next();
			if (!arg.startsWith("-")) {
				targets.add(arg);
			} else {
				//unrecognized args
				logMessage(getCurrentProject(), MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Unknown_argument__{0}_2"), new Object[]{arg}), Project.MSG_ERR); //$NON-NLS-1$
				printUsage();
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates the log file with the name specified by the user.
	 * If the fileName is not absolute, the file will be created in the
	 * working directory if specified or in the same directory as the location
	 * of the build file.	 */
	protected void createLogFile(String fileName) throws FileNotFoundException, IOException {
		IPath path= new Path(fileName);
		if (!path.isAbsolute()) {
			String base= getCurrentProject().getUserProperty("basedir");
			if (base != null) {
				File baseDir= new File(base);
				if (baseDir != null) {
					//relative to the base dir
					path= new Path(baseDir.getAbsolutePath());
				} 
			}else {
				//relative to the build file location
				path= new Path(getBuildFileLocation());
				path= path.removeLastSegments(1);
			}
			path= path.addTrailingSeparator();
			path= path.append(fileName);
		}
		
		File logFile= path.toFile();
		
		//this stream is closed in the finally block of run(list)
		out = new PrintStream(new FileOutputStream(logFile));
		err = out;
		logMessage(getCurrentProject(), MessageFormat.format(InternalAntMessages.getString("InternalAntRunner.Using_{0}_file_as_build_log._1"), new String[]{logFile.getCanonicalPath()}), Project.MSG_INFO); //$NON-NLS-1$
		if (buildLogger != null) {
			buildLogger.setErrorPrintStream(err);
			buildLogger.setOutputPrintStream(out);
		}
	}

	/**
	 * Processes cmd line properties and adds the user properties to the project
	 * Any user properties that have been explicitly set are set on the project as well.	 * 	 */
	protected void processProperties(List commands) {
		
		String[] args = (String[]) commands.toArray(new String[commands.size()]);
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("-D")) { //$NON-NLS-1$

				/* Interestingly enough, we get to here when a user
				 * uses -Dname=value. However, in some cases, the JDK
				 * goes ahead and parses this out to args
				 *   {"-Dname", "value"}
				 * so instead of parsing on "=", we just make the "-D"
				 * characters go away and skip one argument forward.
				 *
				 * I don't know how to predict when the JDK is going
				 * to help or not, so we simply look for the equals sign.
				 */

				String name = arg.substring(2, arg.length());
				String value = null;
				int posEq = name.indexOf("="); //$NON-NLS-1$
				if (posEq > 0) {
					value = name.substring(posEq + 1);
					name = name.substring(0, posEq);
				} else if (i < args.length - 1) {
					value = args[++i];
				}

				getCurrentProject().setUserProperty(name, value);
				commands.remove(args[i]);
			}
		}
	}

	/**
	 * Print the project description, if any
	 */
	protected void printHelp(Project project) {
		if (project.getDescription() != null) {
			logMessage(project, project.getDescription(), Project.MSG_INFO);
		}
		printTargets(project);
	}

	/**
	 * Logs a message with the client indicating the version of <b>Ant</b> that this class
	 * fronts.
	 */
	protected void printVersion() {
		logMessage(getCurrentProject(), getAntVersion(), Project.MSG_INFO);
	}

	/**
	 * Logs a message with the client outlining the usage of <b>Ant</b>.
	 */
	protected void printUsage() {
		String lSep = System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer msg = new StringBuffer();
		msg.append("ant ["); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.options_13")); //$NON-NLS-1$
		msg.append("] ["); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.target_15")); //$NON-NLS-1$
		msg.append(" ["); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.target_15")); //$NON-NLS-1$
		msg.append("2 ["); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.target_15")); //$NON-NLS-1$
		msg.append("3] ...]]"); //$NON-NLS-1$
		msg.append(lSep);
		msg.append(InternalAntMessages.getString("InternalAntRunner.Options___21")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-help\t\t\t\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.print_this_message_23")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-projecthelp\t\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.print_project_help_information_25")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-version\t\t\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.print_the_version_information_and_exit_27")); //$NON-NLS-1$
		msg.append(lSep); 
	 	msg.append("\t-diagnostics\t\t\t\t\t\t"); //$NON-NLS-1$
	 	msg.append(InternalAntMessages.getString("InternalAntRunner.print_information_that_might_be_helpful_to_12")); //$NON-NLS-1$
	 	msg.append(lSep);
        msg.append(InternalAntMessages.getString("InternalAntRunner._t_t_t_t_t_t_t_t_t_t_tdiagnose_or_report_problems._13")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-quiet, -q\t\t\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.be_extra_quiet_29")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-verbose, -v\t\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.be_extra_verbose_31")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-debug\t\t\t\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.print_debugging_information_33")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-emacs\t\t\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.produce_logging_information_without_adornments_35")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-logfile\t<file>\t\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.use_given_file_for_log_37")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t\t-l\t\t<file>"); //$NON-NLS-1$		msg.append(InternalAntMessages.getString("InternalAntRunner._t_t_t_t_t_t_t_t_____15")); //$NON-NLS-1$
		msg.append(lSep);  
		msg.append("\t-logger <classname>\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.the_class_which_is_to_perform_logging_39")); //$NON-NLS-1$
		msg.append(lSep);  
		msg.append("\t-listener <classname>\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.add_an_instance_of_class_as_a_project_listener_41")); //$NON-NLS-1$
		msg.append(lSep); 
		msg.append("\t-buildfile\t<file>\t\t\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.use_given_buildfile_43")); //$NON-NLS-1$
		msg.append(lSep); 
		msg.append("\t\t-file\t\t<file>"); //$NON-NLS-1$		msg.append(InternalAntMessages.getString("InternalAntRunner._t_t_t_t_t_t_t_____1"));  //$NON-NLS-1$
		msg.append(lSep);
        msg.append("\t\t-f\t\t\t<file>"); //$NON-NLS-1$        msg.append(InternalAntMessages.getString("InternalAntRunner._t_t_t_t_t_t_t_____1")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-D<property>=<value>\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.use_value_for_given_property_45")); //$NON-NLS-1$
		msg.append(lSep);
		msg.append("\t-propertyfile <name>\t\t"); //$NON-NLS-1$
		msg.append(InternalAntMessages.getString("InternalAntRunner.load_all_properties_from_file_with_-D_19")); //$NON-NLS-1$
		msg.append(lSep);
        msg.append(InternalAntMessages.getString("InternalAntRunner._t_t_t_t_t_t_t_t_t_t_tproperties_taking_precedence_20")); //$NON-NLS-1$
        msg.append(lSep);
        msg.append("\t-inputhandler <class>\t\t"); //$NON-NLS-1$       	msg.append(InternalAntMessages.getString("InternalAntRunner.the_class_which_will_handle_input_requests_22")); //$NON-NLS-1$
        msg.append(lSep);
        msg.append("\t-find <file>\t\t\t\t\t\t"); //$NON-NLS-1$
        msg.append(InternalAntMessages.getString("InternalAntRunner.search_for_buildfile_towards_the_root_of_the_24")); //$NON-NLS-1$
        msg.append(lSep);
        msg.append(InternalAntMessages.getString("InternalAntRunner._t_t_t_t_t_t_t_t_t_t_tfilesystem_and_use_it_25")); //$NON-NLS-1$
        msg.append(lSep);

		logMessage(getCurrentProject(), msg.toString(), Project.MSG_INFO);
	}

	/**
	 * From a command line list, get the array of arguments of a given parameter.
	 * The parameter and its arguments are removed from the list.
	 * 
	 * @return null if the parameter is not found 
	 * 			or an empty array if no arguments are found
	 */
	protected String[] getArguments(List commands, String param) {
		int index = commands.indexOf(param);
		if (index == -1) {
			return null;
		}
		commands.remove(index);
		if (index == commands.size()) {// if this is the last command
			return new String[]{};
		}
		List args = new ArrayList(commands.size());
		while (index < commands.size()) { // while not the last command
			String command = (String) commands.get(index);
			if (command.startsWith("-")) { // is it a new parameter? //$NON-NLS-1$
				break;
			}
			args.add(command);
			commands.remove(index);
		}
		if (args.isEmpty()) {
			return new String[]{};
		}
		return (String[]) args.toArray(new String[args.size()]);
	}

	/**
	 * Helper method to ensure an array is converted into an ArrayList.
	 */
	private ArrayList getArrayList(String[] args) {
		if (args == null) {
			return null;
		}
		// We could be using Arrays.asList() here, but it does not specify
		// what kind of list it will return. We need a list that
		// implements the method List.remove(Object) and ArrayList does.
		ArrayList result = new ArrayList(args.length);
		for (int i = 0; i < args.length; i++) {
			result.add(args[i]);
		}
		return result;
	}

	/**
	 * Sets the build progress monitor.
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	protected Project getCurrentProject() {
		return currentProject;
	}

	protected void setCurrentProject(Project currentProject) {
		this.currentProject = currentProject;
	}
	
	public String getBuildExceptionErrorMessage(Throwable t) {
		if (t instanceof BuildException) {
			return t.toString();
		}
		return null;
	}
}