package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.tests.util.*;

public class IWorkingSetTest extends UITestCase {
	final static String WORKING_SET_NAME_1 = "ws1";
	final static String WORKING_SET_NAME_2 = "ws2";	
	
	IWorkspace fWorkspace;
	IWorkingSet fWorkingSet;
	
	public IWorkingSetTest(String testName) {
		super(testName);		
	}
	protected void setUp() throws Exception {
		super.setUp();                                                      
		IWorkingSetManager workingSetManager = fWorkbench.getWorkingSetManager();	
		
		fWorkspace = ResourcesPlugin.getWorkspace();		
		fWorkingSet = workingSetManager.createWorkingSet(WORKING_SET_NAME_1, new IAdaptable[] {fWorkspace.getRoot()});
	}
	public void testGetName() throws Throwable {
		assertEquals(WORKING_SET_NAME_1, fWorkingSet.getName());
	}
	public void testGetElements() throws Throwable {
		assertEquals(fWorkspace.getRoot(), fWorkingSet.getElements()[0]);		
	}
	public void testSetElements() throws Throwable {
		boolean exceptionThrown = false;
		
		try {
			fWorkingSet.setElements(null);
		}
		catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		IProject p1 = FileUtil.createProject("TP1");
		IFile f1 = FileUtil.createFile("f1.txt", p1);
		IAdaptable[] elements = new IAdaptable[] {f1, p1};
		fWorkingSet.setElements(elements);
		assertTrue(ArrayUtil.equals(elements, fWorkingSet.getElements()));
		
		fWorkingSet.setElements(new IAdaptable[] {f1});
		assertEquals(f1, fWorkingSet.getElements()[0]);

		fWorkingSet.setElements(new IAdaptable[] {});
		assertEquals(0, fWorkingSet.getElements().length);
	}
	public void testSetName() throws Throwable {
		boolean exceptionThrown = false;
		
		try {
			fWorkingSet.setName(null);
		}
		catch (RuntimeException exception) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		fWorkingSet.setName(WORKING_SET_NAME_2);
		assertEquals(WORKING_SET_NAME_2, fWorkingSet.getName());
		
		fWorkingSet.setName("");
		assertEquals("", fWorkingSet.getName());

		fWorkingSet.setName(" ");
		assertEquals(" ", fWorkingSet.getName());
	}

}