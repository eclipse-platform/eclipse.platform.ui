package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

public class LoggingTestResult extends TestResult {
	protected Stack groupStack;
	protected PerformanceTimer currentTask;
	protected PrintStream logStream;
	protected Stack /* of String */ elements;
	protected String indent;
	protected boolean loggingEnabled;
	
	public LoggingTestResult(PrintStream logStream) {
		this.logStream = logStream;
		this.elements = new Stack();
		this.indent = "";
		this.loggingEnabled = true;
		groupStack = new Stack();
		currentTask = null;
	}
	
	/**
	 * Enables or disables logging.
	 */
	public void setLogging(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}
	
	/**
	 * Marks the beginning of a series of log entries.
	 */
	public void startLog(long timestamp, String sdkBuild) {
		println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		startXMLElement("log", new String[] { "timestamp", "sdkbuild" }, new String[] {
			new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss").format(new Date(timestamp)),
			sdkBuild });
	}
	
	/**
	 * Marks the end of a series of log entries.
	 */
	public void endLog() {
		endXMLElement();
	}
	
	/**
	 * Called by the JUnit framework when an error occurs.
	 * @param test the test
	 * @param error the exception that occurred 
	 */
	public void addError(Test test, Throwable error) {
		printAbort("error", error);
		super.addError(test, error);
	}
	
	/**
	 * Called by the JUnit framework when an assertion failure occurs.
	 * @param test the test
	 * @param error the exception that occurred
	 */
	public void addFailure(Test test, AssertionFailedError error) {
		printAbort("failure", error);
		super.addFailure(test, error);
	}
	
	/**
	 * Called by the JUnit framework to mark the beginning of a test case.
	 * @param test the test
	 */
	public void startTest(Test test) {
		if (test instanceof TestCase) {
			TestCase testCase = (TestCase) test;
			startXMLElement("case", new String[] { "class", "name" },
				new String[] { testCase.getClass().getName(), testCase.getName() });
			groupStack.clear();
			currentTask = null;
		}
		super.startTest(test);
	}

	/**
	 * Called by the JUnit framework to mark the end of a test case.
	 * @param test the test
	 */
	public void endTest(Test test) {
		if (test instanceof TestCase) {
			TestCase testCase = (TestCase) test;
			if (currentTask != null) endTask();
			while (! groupStack.isEmpty()) endGroup();
			endXMLElement();
		}
		super.endTest(test);
	}
	
	/**
	 * Marks the beginning of a new task group.
	 * @param groupName the name for the group
	 */
	public void startGroup(String groupName) {
		Assert.assertNull(currentTask);
		startXMLElement("group", new String[] { "name" }, new String[] { groupName });
		groupStack.push(groupName);
	}
	
	/**
	 * Marks the end of the active task group.
	 */
	public void endGroup() {
		Assert.assertNull(currentTask);
		endXMLElement();
		groupStack.pop();
	}

	/**
	 * Marks the beginning of a new task.
	 * @param taskName the name for the task
	 */
	public void startTask(String taskName) {
		Assert.assertNull(currentTask);
		startXMLElement("task", new String[] { "name" }, new String[] { taskName });
		currentTask = new PerformanceTimer(taskName);
		currentTask.start();
	}
	
	/**
	 * Marks the end of the active task.
	 */
	public void endTask() {
		Assert.assertNotNull(currentTask);
		currentTask.stop();
		printXMLElement("result", new String[] { "elapsed" },
			new String[] { Integer.toString(currentTask.getTotalMillis()) });
		endXMLElement();
		currentTask = null;
	}
	
	/**
	 * Returns the name of the active group, or null if none.
	 */
	public String getGroupName() {
		return groupStack.isEmpty() ? null : (String) groupStack.peek();
	}
	
	/**
	 * Returns the name of the active task, or null if none.
	 */
	public String getTaskName() {
		return (currentTask == null) ? null : currentTask.getName();
	}

	protected void startXMLElement(String name, String[] attributes, String[] values) {
		println(formatXMLElement(name, attributes, values, false));
		elements.push(name);
		indent += "  ";
	}
		
	protected void printXMLElement(String name, String[] attributes, String[] values) {
		println(formatXMLElement(name, attributes, values, true));
	}

	protected String formatXMLElement(String name, String[] attributes, String[] values, boolean quickEnd) {
		// XXX need to escape certain characters in attribute values
		StringBuffer buffer = new StringBuffer("<");
		buffer.append(name);
		if (attributes != null && values != null) {
			for (int i = 0; i < attributes.length; ++i) {
				buffer.append(' ');
				buffer.append(attributes[i]);
				buffer.append("=\"");
				buffer.append(values[i]);
				buffer.append('"');
			}
		}
		if (quickEnd) buffer.append('/');
		buffer.append('>');
		return buffer.toString();
	}
	
	protected void endXMLElement() {
		indent = indent.substring(2);
		String name = (String) elements.pop();
		println("</" + name + ">");
	}
	
	protected void printAbort(String type, Throwable error) {
		String message = error.getMessage();
		if (message == null) message = "";
		startXMLElement("abort", new String[] { "type", "message" },
			new String[] { type, message });
		if (loggingEnabled) {
			// XXX need to escape certain characters
			// XXX need a better way of serializing the stack trace
			error.printStackTrace(logStream);
		}
		endXMLElement();
	}
	
	protected void println(String line) {
		if (loggingEnabled) {
			logStream.println(indent + line);
		}
	}
}
