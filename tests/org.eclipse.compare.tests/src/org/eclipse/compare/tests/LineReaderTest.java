package org.eclipse.compare.tests;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.compare.internal.patch.LineReader;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LineReaderTest extends TestCase {

	public void testReadEmpty() {
		LineReader lr= new LineReader(getReader("empty.txt")); //$NON-NLS-1$
		List inLines= lr.readLines();
		Assert.assertEquals(0, inLines.size());
	}

	public void testReadNormal() {
		LineReader lr= new LineReader(getReader("normal.txt")); //$NON-NLS-1$
		List inLines= lr.readLines();
		Assert.assertEquals(3, inLines.size());
		Assert.assertEquals("[1]\n", inLines.get(0)); //$NON-NLS-1$
		Assert.assertEquals("[2]\n", inLines.get(1)); //$NON-NLS-1$
		Assert.assertEquals("[3]\n", inLines.get(2)); //$NON-NLS-1$
	}

	public void testReadUnterminatedLastLine() {
		LineReader lr= new LineReader(getReader("unterminated.txt")); //$NON-NLS-1$
		List inLines= lr.readLines();
		Assert.assertEquals(3, inLines.size());
		Assert.assertEquals("[1]\n", inLines.get(0)); //$NON-NLS-1$
		Assert.assertEquals("[2]\n", inLines.get(1)); //$NON-NLS-1$
		Assert.assertEquals("[3]", inLines.get(2)); //$NON-NLS-1$
	}

	private BufferedReader getReader(String name) {
		InputStream resourceAsStream= getClass().getResourceAsStream("linereaderdata/" + name); //$NON-NLS-1$
		InputStreamReader reader2= new InputStreamReader(resourceAsStream);
		return new BufferedReader(reader2);
	}
}
