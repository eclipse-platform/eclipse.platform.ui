package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;

public class IPerspectiveDescriptorTest extends TestCase {

	private IPerspectiveDescriptor fPer;
	private IPerspectiveRegistry fReg;
	
	public IPerspectiveDescriptorTest( String testName )
	{
		super( testName );
	}
	
	public void setUp()
	{
		fPer = ( IPerspectiveDescriptor )Man.pick( PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives() );
		//fPer.
	}
	
	public void testGetId()
	{
		assertNotNull( fPer.getId() );
	}
	
	public void testGetLabel()
	{
		assertNotNull( fPer.getLabel() );	
	}

//	This always fails
	public void testGetImageDescriptor()
	{
		assertNotNull( fPer.getImageDescriptor() );
	}
	
	public void testThis()
	{
//		opne
	}
}

