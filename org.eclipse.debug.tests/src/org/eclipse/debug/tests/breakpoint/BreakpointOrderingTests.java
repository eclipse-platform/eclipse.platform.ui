/*******************************************************************************
 *  Copyright (c) 2000, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.breakpoint;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsComparator;
import org.eclipse.debug.tests.TestsPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;

/**
 * Test the ordering used in the breakpoints view.
 * 
 * Using a special Comparator which sorts breakpoint texts like file:1, file:2 and file:11 in a numerical ordering.
 */
public class BreakpointOrderingTests extends TestCase {
	
	public BreakpointOrderingTests(String name) {
		super(name);
	}
	
	/**
	 * Test only implementation of IBreakpoint.
	 */
	static class TestBreakpoint implements IBreakpoint {

		private String fText;
		private IMarker fMarker = null;

		TestBreakpoint(String text) {
			this(text, IBreakpoint.BREAKPOINT_MARKER);
		}
		
		TestBreakpoint(String text, final String markerType) {
			fText = text;
			final IResource resource = ResourcesPlugin.getWorkspace().getRoot();
			IWorkspaceRunnable wr = new IWorkspaceRunnable() {

				public void run( IProgressMonitor monitor ) throws CoreException {
					// create the marker
					setMarker(resource.createMarker(markerType));
				}
			};
			try {
				ResourcesPlugin.getWorkspace().run( wr, null );
			}
			catch ( CoreException e ) {
				fail("Unexpected exception: " + e.toString());
			}
			
		}
		
		void ReportFailure(String msg) throws CoreException {
			throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, msg));
		}
		
		
		String getText() {
			return fText;
		}

		public void delete() throws CoreException {
			fMarker.delete();
		}

		public IMarker getMarker() {
			return fMarker;
		}

		public String getModelIdentifier() {
			return "Test";
		}

		public boolean isEnabled() throws CoreException {
			fail("not implemented in test");
			return false;
		}

		public boolean isPersisted() throws CoreException {
			fail("not implemented in test");
			return false;
		}

		public boolean isRegistered() throws CoreException {
			fail("not implemented in test");
			return false;
		}

		public void setEnabled(boolean enabled) throws CoreException {
			fail("not implemented in test");
		}

		public void setMarker(IMarker marker) throws CoreException {
			assertTrue(fMarker == null && marker != null);
			fMarker = marker;
		}

		public void setPersisted(boolean registered) throws CoreException {
			fail("not implemented in test");
		}

		public void setRegistered(boolean registered) throws CoreException {
			fail("not implemented in test");
			
		}

		public Object getAdapter(Class adapter) {
			fail("not implemented in test");
			return null;
		}

	}

	/**
	 * Test only implementation of ILineBreakpoint.
	 */
	static class TestLineBreakpoint extends TestBreakpoint implements ILineBreakpoint {
		private int fLineNum;

		TestLineBreakpoint(String text, int lineNum) {
			super(text, IBreakpoint.LINE_BREAKPOINT_MARKER);
			fLineNum = lineNum;		
		}

		public int getLineNumber() throws CoreException {
			return fLineNum;
		}

		public int getCharEnd() throws CoreException {
			fail("not implemented in test");
			return 0;
		}

		public int getCharStart() throws CoreException {
			fail("not implemented in test");
			return 0;
		}
	}

	/**
	 * Test only implementation of StructuredViewer.
	 */
	StructuredViewer fTestViewer = new StructuredViewer() {

		public IBaseLabelProvider getLabelProvider() {
			return fDebugModelPres;
		}

		protected Widget doFindInputItem(Object element) {
			fail("not implemented in test");
			return null;
		}

		protected Widget doFindItem(Object element) {
			fail("not implemented in test");
			return null;
		}

		protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
			fail("not implemented in test");
		}

		protected List getSelectionFromWidget() {
			fail("not implemented in test");
			return null;
		}

		protected void internalRefresh(Object element) {
			fail("not implemented in test");
		}

		public void reveal(Object element) {
			fail("not implemented in test");			
		}

		protected void setSelectionToWidget(List l, boolean reveal) {
			fail("not implemented in test");
		}

		public Control getControl() {
			fail("not implemented in test");
			return null;
		}};	
	
	// Test debug model presentation for label text retrieval. 
	IDebugModelPresentation fDebugModelPres = new IDebugModelPresentation() {

		public void computeDetail(IValue value, IValueDetailListener listener) {
			fail("not implemented in test");
		}

		public Image getImage(Object element) {
			fail("not implemented in test");
			return null;
		}

		public String getText(Object element) {
			assertTrue("Unexpected element", element instanceof TestBreakpoint);
			return ((TestBreakpoint)element).getText();
		}

		public void setAttribute(String attribute, Object value) {			
			fail("not implemented in test");
		}

		public void addListener(ILabelProviderListener listener) {
			fail("not implemented in test");
		}

		public void dispose() {
			fail("not implemented in test");
		}

		public boolean isLabelProperty(Object element, String property) {
			fail("not implemented in test");
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			fail("not implemented in test");
		}

		public String getEditorId(IEditorInput input, Object element) {
			fail("not implemented in test");
			return null;
		}

		public IEditorInput getEditorInput(Object element) {
			fail("not implemented in test");
			return null;
		}};
	
	// Test vector with some UNIX paths
	TestBreakpoint[] createTestBreakpoints0() {
		TestBreakpoint[] fTestBps = { 
			new TestBreakpoint(""),
			new TestBreakpoint("/file/text.c"),
			new TestBreakpoint("/file/text.c:1"),
			new TestLineBreakpoint("", 0),
			new TestLineBreakpoint("/file/text.c", 0),
			new TestLineBreakpoint("/file/text.c", 1),
			new TestLineBreakpoint("/file/text.c:", 0),
			new TestLineBreakpoint("/file/text.c:0", 0),
			new TestLineBreakpoint("/file/text.c:1", 1),
			new TestLineBreakpoint("/file/text.c:0002", 2),
			new TestLineBreakpoint("/file/text.c:3xxx", 3),
			new TestLineBreakpoint("/file/text.c:10xxx", 10),
			new TestLineBreakpoint("/file/text.c:a_01", 1),
			new TestLineBreakpoint("/file/text.c:a_01a", 1),
			new TestLineBreakpoint("/file/text.c:a_09", 9),
			new TestLineBreakpoint("/file/text.c:a_09a", 9),
			new TestLineBreakpoint("/file/text.c:a_011", 11),
			new TestLineBreakpoint("/file/text.c:a_011a", 11),
			new TestLineBreakpoint("/file/text.c:y", 0),
		};
		return fTestBps;
	}	
		
	// Check plain numbers 
	TestBreakpoint[] createTestBreakpoints1() {
		TestBreakpoint[] fTestBps = { 
			new TestLineBreakpoint("0", 0), 
			new TestLineBreakpoint("1", 1), 
			new TestLineBreakpoint("1_a", 1), 
			new TestLineBreakpoint("001_b", 1), 
			new TestLineBreakpoint("01_c", 1), 
			new TestLineBreakpoint("3", 3), 
			new TestLineBreakpoint("10", 10), 
			new TestLineBreakpoint("11", 11), 
			new TestLineBreakpoint("20", 20), 
			new TestLineBreakpoint("110", 110), 
			new TestLineBreakpoint("112", 112), 
			new TestLineBreakpoint("112a", 112), 
			new TestLineBreakpoint("112b", 112), 
		};
		return fTestBps;
	}	
			
	// Test consistent behavior with leading 0's
	TestBreakpoint[] createTestBreakpoints2() {
		TestBreakpoint[] fTestBps = { 
			new TestLineBreakpoint("0", 0), 
			new TestLineBreakpoint("00", 0), 
			new TestLineBreakpoint("0000", 0), 
			new TestLineBreakpoint("0001", 1), 
			new TestLineBreakpoint("0010", 10), 
			new TestLineBreakpoint("1000", 1000), 
			new TestLineBreakpoint("10000", 10000), 
		};
		return fTestBps;
	}	
			
	// Test Win32 paths
	TestBreakpoint[] createTestBreakpoints3() {
		TestBreakpoint[] fTestBps = { 
			new TestLineBreakpoint(":a", 0),
			new TestLineBreakpoint("c:\\file\\text.c:1", 1),
			new TestLineBreakpoint("c:\\file\\text.c:2", 2),
			new TestLineBreakpoint("d:\\file\\text.c:3", 3),
		};
		return fTestBps;
	}	

	public void testBreakpointOrdering0() throws CoreException {
		executeTest(createTestBreakpoints0());
	}
	public void testBreakpointOrdering1() throws CoreException {
		executeTest(createTestBreakpoints1());
	}
	public void testBreakpointOrdering2() throws CoreException {
		executeTest(createTestBreakpoints2());
	}
	public void testBreakpointOrdering3() throws CoreException {
		executeTest(createTestBreakpoints3());
	}
	

	/**
	 * Test expected ordering.
	 * Expecting the same ordering as in which the BP's are returned by createTestBreakpoints.
	 */
	void executeTest(TestBreakpoint[] testBps) throws CoreException {
		BreakpointsComparator bpCompare = new BreakpointsComparator();
		try {
			boolean failed = false;
			for (int inner = 0; inner < testBps.length; inner++) {
				TestBreakpoint testInner = testBps[inner];
				for (int outer = 0; outer < testBps.length; outer++) {
					TestBreakpoint testOuter = testBps[outer];
					int res = bpCompare.compare(fTestViewer, testInner, testOuter);
					boolean equalCheck = (res == 0) == (inner == outer);
					boolean ltCheck = (res < 0) == (inner < outer);
					if (!equalCheck) {
						System.err.println("Equal Comparison in between " + inner + " and " + outer + " ("+testBps[inner].getText()+ " and "+testBps[outer].getText()+") failed" );
						failed = true;
					}
					if (!ltCheck) {
						System.err.println("Less Comparison in between " + inner + " and " + outer + " ("+testBps[inner].getText()+ " and "+testBps[outer].getText()+") failed" );
						failed = true;
					}					
				}			
			}
			assertFalse(failed);
		} finally {
			for (int index = 0; index < testBps.length; index++) {
				testBps[index].delete();
			}
		}		
	}
}