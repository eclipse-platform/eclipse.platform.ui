package org.eclipse.core.tests.internal.runtime;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.runtime.PlatformLogReader;
import org.eclipse.core.internal.runtime.PlatformLogWriter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class LogSerializationTest extends RuntimeTest {
	
	protected File logFile = new File("c:\\temp\\test\\log.txt");

public LogSerializationTest(String name) {
	super(name);
}
public LogSerializationTest() {
	super("");
}
public static Test suite() {
	return new TestSuite(LogSerializationTest.class);
}
protected void assertEquals(String msg, IStatus[] expected, IStatus[] actual) {
	if (expected == null) {
		assertNull(msg + " expected null but got: " + actual, actual);
		return;
	}
	if (actual == null) {
		assertNull(msg + " expected " + expected + " but got null", expected);
		return;
	}
	assertEquals(msg + " different number of statuses", expected.length, actual.length);
	for (int i = 0, imax = expected.length; i < imax; i++) {
		assertEquals(msg + "differ at status " + i, expected[i], actual[i]);
	}
}
protected void assertEquals(String msg, IStatus expected, IStatus actual) {
	assertEquals(msg + " severity", expected.getSeverity(), actual.getSeverity());
	assertEquals(msg + " plugin-id", expected.getPlugin(), actual.getPlugin());
	assertEquals(msg + " code", expected.getCode(), actual.getCode());
	assertEquals(msg + " message", expected.getMessage(), actual.getMessage());
	assertEquals(msg + " exception", expected.getException(), actual.getException());
	assertEquals(msg + " children", expected.getChildren(), actual.getChildren());
}
protected void assertEquals(String msg, Throwable expected, Throwable actual) {
	if (expected == null) {
		assertNull(msg + " expected null but got: " + actual, actual);
		return;
	}
	if (actual == null) {
		assertNull(msg + " expected " + expected + " but got null", expected);
		return;
	}
	assertEquals(msg + " stack trace", 
		encodeStackTrace(expected),
		encodeStackTrace(actual));
	assertEquals(msg + " message", expected.getMessage(), actual.getMessage());
}
protected String encodeStackTrace(Throwable t) {
	StringWriter sWriter = new StringWriter();
	PrintWriter pWriter = new PrintWriter(sWriter);
	pWriter.println();
	t.printStackTrace(pWriter);
	pWriter.flush();
	return sWriter.toString();
}
protected void doTest(String msg, IStatus[] oldStats) {
	writeLog(oldStats);
	IStatus[] newStats = readLog();
	assertEquals(msg, oldStats, newStats);
}
protected void doTest(String msg, IStatus status) {
	doTest(msg, new IStatus[] {status});
}
protected IStatus[] readLog() {
	PlatformLogReader reader = new PlatformLogReader();
	return reader.readLogFile(logFile.getAbsolutePath());
}
protected void setUp() throws Exception {
	super.setUp();
}
protected void tearDown() throws Exception {
	super.tearDown();
	logFile.delete();
}
public void testSimpleSerialize() {
	IStatus status = new Status(IStatus.WARNING, "org.foo", 1, "This is the message", new NullPointerException());
	doTest("1.0", status);
}
protected void writeLog(IStatus status) {
	writeLog(new IStatus[] {status});
}
protected void writeLog(IStatus[] statuses) {
	PlatformLogWriter writer = new PlatformLogWriter(logFile);
	for (int i = 0; i < statuses.length; i++) {
		writer.logging(statuses[i], "org.eclipse.core.tests.runtime");
	}
	writer.shutdown();
}
}