package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.jface.resource.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;

public class IEditorRegistryTest extends TestCase {
	private IEditorRegistry fReg;

	public IEditorRegistryTest( String testName )
	{
		super( testName );		
	}
	
	public void setUp()
	{                                                              
		fReg = PlatformUI.getWorkbench().getEditorRegistry();		
	}

	public void testGetFileEditorMappings()
	{
		assert( Tool.check( fReg.getFileEditorMappings() ) );
	}
	
/*	public void testGetEditors() throws Throwable
	{		
		IFileEditorMapping[] mappings = fReg.getFileEditorMappings();
		IEditorDescriptor[] editors1, editors2;
		
		for( int i = 0; i < mappings.length; i ++ ){
			editors1 = fReg.getEditors( mappings[ i ].getLabel() );			
			assert( Tool.check( editors1 ) );
			editors2 = fReg.getEditors( Tool.getIFile( mappings[ i ].getLabel() ) );			
			assert( Tool.equals( editors1, editors2 ) );
		}

//		when there is no matching editor
		editors1 = fReg.getEditors( Tool.UnknownFileName[0] );
		assert( editors1.length == 0 );
		
		editors1 = fReg.getEditors( Tool.getIFile( Tool.UnknownFileName[1] ) );
		assert( editors1.length == 0 );
	}
	
	public void testSetDefaultEditor() throws Throwable
	{
		//IEditorDescriptor original = fReg.getDefaultEditor();
		IFileEditorMapping mapping = ( IFileEditorMapping )Tool.pick( fReg.getFileEditorMappings() );
		IEditorDescriptor editor = ( IEditorDescriptor )Tool.pick( mapping.getEditors() );
			
		String fileName = "whatever";
		IFile file = Tool.getIFile( fileName );

		fReg.setDefaultEditor( file, Tool.TextEditorID );
		System.out.println( fReg.getDefaultEditor( file ) );		
	}*/
	
	public void testFindEditor() 
	{
		IEditorDescriptor editor = fReg.findEditor( Tool.TextEditorID );
		assertEquals( editor.getId(), Tool.TextEditorID );
	}

	public void testGetDefaultEditor()
	{		
		assertNotNull( fReg.getDefaultEditor() );	
	}

	public void testGetDefaultEditor_String()
	{		
		//editor not found
		IEditorDescriptor editor = fReg.getDefaultEditor( Tool.UnknownFileName[0] );
		assertNull( editor );

		//otherwise
		
		//IFileEditorMapping[] mappings = fReg.getFileEditorMappings();
	}

/*	public void testGetImageDescriptor() throws Throwable
	{
		IFileEditorMapping[] mappings = fReg.getFileEditorMappings();
		ImageDescriptor image1, image2; 
		String fileName;

		for( int i = 0; i < mappings.length; i ++ ){
			fileName = mappings[ i ].getLabel();
			image1 = fReg.getImageDescriptor( fileName );
			image2 = fReg.getImageDescriptor( Tool.getIFile( fileName ) );
			assertNotNull( image1 );
			assertEquals( image1, image2 );
		}	
		
	//default image
		image1 = fReg.getImageDescriptor( Tool.UnknownFileName[0] );
		assertNotNull( image1 );

		image2 = fReg.getImageDescriptor( Tool.UnknownFileName[1] );
		assertNotNull( image2 );
		assertEquals( image1, image2 );

		image2 = fReg.getImageDescriptor( Tool.getIFile( Tool.UnknownFileName[0] ) );
		assertNotNull( image2 );
		assertEquals( image1, image2 );

		image2 = fReg.getImageDescriptor( Tool.getIFile( Tool.UnknownFileName[1] ) );
		assertNotNull( image2 );
		assertEquals( image1, image2 );
	}*/
}