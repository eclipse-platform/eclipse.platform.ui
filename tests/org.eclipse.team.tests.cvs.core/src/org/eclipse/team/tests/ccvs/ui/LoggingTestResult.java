package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.StringTokenizer;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.TeamException;

public class LoggingTestResult extends TestResult {
	protected Stack groupStack;
	protected PerformanceTimer currentTask;
	protected PrintStream logStream;
	protected Stack /* of String */ elements;
	protected String indent;
	
	/**
	 * Creates a logging test result.
	 * @param logStream the output stream, or null to disable logging
	 */
	public LoggingTestResult(PrintStream logStream) {
		this.logStream = logStream;
		this.elements = new Stack();
		this.indent = "";
		groupStack = new Stack();
		currentTask = null;
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
	 * Prints a warning message to the log file.
	 * @param message the message, or null
	 * @param error an exception with a stack trace, or null
	 * @param status a status code, or null
	 */
	public void printWarning(String message, Throwable error, IStatus status) {
		printAbort("warning", message, error, status);
	}
	
	/**
	 * Called by the JUnit framework when an error occurs.
	 * @param test the test
	 * @param error the exception that occurred 
	 */
	public void addError(Test test, Throwable error) {
		printAbort("error", null, error, null);
		super.addError(test, error);
	}
	
	/**
	 * Called by the JUnit framework when an assertion failure occurs.
	 * @param test the test
	 * @param error the exception that occurred
	 */
	public void addFailure(Test test, AssertionFailedError error) {
		printAbort("failure", null, error, null);
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
		Assert.assertTrue(! groupStack.empty());
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

	protected void printXMLElementData(String line) {
		// XXX need to escape certain characters in element data
		println(line);
	}
	
	protected void printAbort(String type, String message, Throwable error, IStatus status) {
		if (status == null && error != null) {
			if (error instanceof CoreException) {
				status = ((CoreException) error).getStatus();
			} else if (error instanceof TeamException) {
				status = ((TeamException) error).getStatus();
			}
		}
		if (message == null && error != null) {
			message = error.getMessage();
			if (message == null) {
				message = error.getClass().getName();
			}
		}
		if (message == null && status != null) {
			message = status.getMessage();
		}
		if (message == null) message = "";
		startXMLElement("abort", new String[] { "type", "message" },
			new String[] { type, message });
		if (status != null) printStatus(status);
		if (error != null) printStackTrace(error);
		endXMLElement();
	}
	
	protected void printStatus(IStatus status) {
		startXMLElement("status", new String[] { "severity", "code", "plugin", "message" },
			new String[] {
				Integer.toString(status.getSeverity()),
				Integer.toString(status.getCode()),
				status.getPlugin(), status.getMessage() });
		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; ++i) {
				printStatus(children[i]);
			}
		}
		endXMLElement();
	}
	
	protected void printStackTrace(Throwable error) {
		// XXX need a better way to serialize the stack trace
		String trace = BaseTestRunner.getFilteredTrace(error);
		StringTokenizer tok = new StringTokenizer(trace, "\r\n");
		if (! tok.hasMoreTokens()) return; // empty trace?
		tok.nextToken(); // skip message line
		startXMLElement("trace", null, null);
		while (tok.hasMoreTokens()) {
			String frame = tok.nextToken();
			printXMLElementData(frame);
		}
		endXMLElement();
	}
	
	protected void println(String line) {
		if (logStream != null) logStream.println(indent + line);
	}
}
