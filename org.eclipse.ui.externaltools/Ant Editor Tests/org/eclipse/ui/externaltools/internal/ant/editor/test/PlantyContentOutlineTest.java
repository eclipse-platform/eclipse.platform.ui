//
// PlantyContentOutlineTest.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ui.externaltools.internal.ant.editor.outline.OutlinePreparingHandler;
import org.eclipse.ui.externaltools.internal.ant.editor.outline.PlantyContentOutlinePage;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;

/**
 * Tests the correct creation of the outline for an xml file.
 * 
 * @version 19.10.2002
 * @author Alf Schiefelbein
 */
public class PlantyContentOutlineTest extends TestCase {

    private class TestOutlinePage extends PlantyContentOutlinePage {
        public TestOutlinePage() {
            super(null);
        }
        public XmlElement getContentOutline(IAdaptable input) {
            return super.getContentOutline(input);
        }
        public String getFileContentAsString(IFile aFile) {
            return super.getFileContentAsString(aFile);
        }
    }
    
    private class TestFile implements IFile {
        InputStream stream;
        public TestFile(InputStream anInputStream) {
            stream = anInputStream;
        }
        public void appendContents(
            InputStream source,
            boolean force,
            boolean keepHistory,
            IProgressMonitor monitor)
            throws CoreException {
        }
        public void appendContents(
            InputStream source,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }
        public void create(
            InputStream source,
            boolean force,
            IProgressMonitor monitor)
            throws CoreException {
        }
        public void create(
            InputStream source,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }
        public void delete(
            boolean force,
            boolean keepHistory,
            IProgressMonitor monitor)
            throws CoreException {
        }
        public InputStream getContents() throws CoreException {
            return stream;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#getContents(boolean)
         */
        public InputStream getContents(boolean force) throws CoreException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#getEncoding()
         */
        public int getEncoding() throws CoreException {
            return 0;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IStorage#getFullPath()
         */
        public IPath getFullPath() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#getHistory(IProgressMonitor)
         */
        public IFileState[] getHistory(IProgressMonitor monitor)
            throws CoreException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IStorage#getName()
         */
        public String getName() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IStorage#isReadOnly()
         */
        public boolean isReadOnly() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#move(IPath, boolean, boolean, IProgressMonitor)
         */
        public void move(
            IPath destination,
            boolean force,
            boolean keepHistory,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#setContents(IFileState, boolean, boolean, IProgressMonitor)
         */
        public void setContents(
            IFileState source,
            boolean force,
            boolean keepHistory,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#setContents(IFileState, int, IProgressMonitor)
         */
        public void setContents(
            IFileState source,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)
         */
        public void setContents(
            InputStream source,
            boolean force,
            boolean keepHistory,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IFile#setContents(InputStream, int, IProgressMonitor)
         */
        public void setContents(
            InputStream source,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#accept(IResourceVisitor, int, boolean)
         */
        public void accept(
            IResourceVisitor visitor,
            int depth,
            boolean includePhantoms)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#accept(IResourceVisitor, int, int)
         */
        public void accept(
            IResourceVisitor visitor,
            int depth,
            int memberFlags)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#accept(IResourceVisitor)
         */
        public void accept(IResourceVisitor visitor) throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#clearHistory(IProgressMonitor)
         */
        public void clearHistory(IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#copy(IPath, boolean, IProgressMonitor)
         */
        public void copy(
            IPath destination,
            boolean force,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#copy(IPath, int, IProgressMonitor)
         */
        public void copy(
            IPath destination,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#copy(IProjectDescription, boolean, IProgressMonitor)
         */
        public void copy(
            IProjectDescription description,
            boolean force,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#copy(IProjectDescription, int, IProgressMonitor)
         */
        public void copy(
            IProjectDescription description,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#createMarker(String)
         */
        public IMarker createMarker(String type) throws CoreException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#delete(boolean, IProgressMonitor)
         */
        public void delete(boolean force, IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#delete(int, IProgressMonitor)
         */
        public void delete(int updateFlags, IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#deleteMarkers(String, boolean, int)
         */
        public void deleteMarkers(
            String type,
            boolean includeSubtypes,
            int depth)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#exists()
         */
        public boolean exists() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#findMarker(long)
         */
        public IMarker findMarker(long id) throws CoreException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#findMarkers(String, boolean, int)
         */
        public IMarker[] findMarkers(
            String type,
            boolean includeSubtypes,
            int depth)
            throws CoreException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getFileExtension()
         */
        public String getFileExtension() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getLocation()
         */
        public IPath getLocation() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getMarker(long)
         */
        public IMarker getMarker(long id) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getModificationStamp()
         */
        public long getModificationStamp() {
            return 0;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getParent()
         */
        public IContainer getParent() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getPersistentProperty(QualifiedName)
         */
        public String getPersistentProperty(QualifiedName key)
            throws CoreException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getProject()
         */
        public IProject getProject() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getProjectRelativePath()
         */
        public IPath getProjectRelativePath() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getSessionProperty(QualifiedName)
         */
        public Object getSessionProperty(QualifiedName key)
            throws CoreException {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getType()
         */
        public int getType() {
            return 0;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#getWorkspace()
         */
        public IWorkspace getWorkspace() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#isAccessible()
         */
        public boolean isAccessible() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#isDerived()
         */
        public boolean isDerived() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#isLocal(int)
         */
        public boolean isLocal(int depth) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#isPhantom()
         */
        public boolean isPhantom() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#isSynchronized(int)
         */
        public boolean isSynchronized(int depth) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#isTeamPrivateMember()
         */
        public boolean isTeamPrivateMember() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#move(IPath, boolean, IProgressMonitor)
         */
        public void move(
            IPath destination,
            boolean force,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#move(IPath, int, IProgressMonitor)
         */
        public void move(
            IPath destination,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#move(IProjectDescription, boolean, boolean, IProgressMonitor)
         */
        public void move(
            IProjectDescription description,
            boolean force,
            boolean keepHistory,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#move(IProjectDescription, int, IProgressMonitor)
         */
        public void move(
            IProjectDescription description,
            int updateFlags,
            IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#refreshLocal(int, IProgressMonitor)
         */
        public void refreshLocal(int depth, IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#setDerived(boolean)
         */
        public void setDerived(boolean isDerived) throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#setLocal(boolean, int, IProgressMonitor)
         */
        public void setLocal(boolean flag, int depth, IProgressMonitor monitor)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#setPersistentProperty(QualifiedName, String)
         */
        public void setPersistentProperty(QualifiedName key, String value)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#setReadOnly(boolean)
         */
        public void setReadOnly(boolean readOnly) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#setSessionProperty(QualifiedName, Object)
         */
        public void setSessionProperty(QualifiedName key, Object value)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#setTeamPrivateMember(boolean)
         */
        public void setTeamPrivateMember(boolean isTeamPrivate)
            throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IResource#touch(IProgressMonitor)
         */
        public void touch(IProgressMonitor monitor) throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
         */
        public Object getAdapter(Class adapter) {
            return null;
        }

		public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor) throws CoreException {
		}

		public IPath getRawLocation() {
			return null;
		}

		public boolean isLinked() {
			return false;
		}

		public void accept(IResourceProxyVisitor fastVisitor, int memberFlags) throws CoreException {
		}

	} // class TestOutlinePage
    
    /**
     * Constructor for PlantyContentOutlineTest.
     */
    public PlantyContentOutlineTest(String arg0) {
        super(arg0);
    }

    /**
     * Tests parsing an XML file with the use of our OutlinePreparingHandler.
     */
    public void testOutlinePreparingHandler() throws SAXException, ParserConfigurationException, IOException {
        SAXParser tempParser = SAXParserFactory.newInstance().newSAXParser();

        OutlinePreparingHandler tempHandler = new OutlinePreparingHandler(new File(getClass().getResource("/de/gebit/planty/test/test2.xml").getFile()));
        InputStream tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/test2.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        XmlElement tempRootElement = tempHandler.getRootElement();

        assertEquals("bla", tempRootElement.getName());
        assertEquals(1, tempRootElement.getStartingRow());
        assertEquals(6, tempRootElement.getStartingColumn());
        assertEquals(9, tempRootElement.getEndingRow());
        assertEquals(7, tempRootElement.getEndingColumn());
        List tempChildNodes = tempRootElement.getChildNodes();
        assertEquals(2, tempChildNodes.size());

        XmlElement tempElement = (XmlElement)tempChildNodes.get(0);
        assertEquals("blub", tempElement.getName());
        assertEquals(2, tempElement.getStartingRow());
        assertEquals(9, tempElement.getStartingColumn());
        assertEquals(2, tempElement.getEndingRow());
        assertEquals(16, tempElement.getEndingColumn());

        tempElement = (XmlElement)tempChildNodes.get(1);
        assertEquals("klick", tempElement.getName());
        assertEquals(3, tempElement.getStartingRow());
        assertEquals(10, tempElement.getStartingColumn());
        assertEquals(8, tempElement.getEndingRow());
        assertEquals(11, tempElement.getEndingColumn());

        tempChildNodes = tempElement.getChildNodes();
        assertEquals(4, tempChildNodes.size());
		
		tempElement = (XmlElement)tempChildNodes.get(0);
        assertEquals("gurgel", tempElement.getName());
        assertEquals(4, tempElement.getStartingRow());
        assertEquals(13, tempElement.getStartingColumn());
        assertEquals(4, tempElement.getEndingRow());
        assertEquals(22, tempElement.getEndingColumn());

		tempElement = (XmlElement)tempChildNodes.get(1);
        assertEquals("hal", tempElement.getName());
        assertEquals(5, tempElement.getStartingRow());
        assertEquals(10, tempElement.getStartingColumn());
        assertEquals(5, tempElement.getEndingRow());
        assertEquals(16, tempElement.getEndingColumn());

		tempElement = (XmlElement)tempChildNodes.get(2);
        assertEquals("klack", tempElement.getName());
        assertEquals(6, tempElement.getStartingRow());
        assertEquals(13, tempElement.getStartingColumn());
        assertEquals(6, tempElement.getEndingRow());
        assertEquals(13, tempElement.getEndingColumn());

		tempElement = (XmlElement)tempChildNodes.get(3);
        assertEquals("humpf", tempElement.getName());
        assertEquals(7, tempElement.getStartingRow());
        assertEquals(13, tempElement.getStartingColumn());
        assertEquals(7, tempElement.getEndingRow());
        assertEquals(13, tempElement.getEndingColumn());

		tempElement = (XmlElement)tempChildNodes.get(2);
        assertEquals("klack", tempElement.getName());
        assertEquals(6, tempElement.getStartingRow());
        assertEquals(13, tempElement.getStartingColumn());
        assertEquals(6, tempElement.getEndingRow());
        assertEquals(13, tempElement.getEndingColumn());


//        tempHandler = new PlantySaxDefaultHandler(4, 8);
//        tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/test2.xml");
//        tempParser.parse(tempStream, tempHandler);
//        tempElement = tempHandler.getParentElement(true);
//        assertNotNull(tempElement);
//        assertEquals("klick", tempElement.getTagName());
//        tempChildNodes = tempElement.getChildNodes();
//        assertEquals(4, tempChildNodes.getLength());
//        assertEquals("gurgel", ((Element)tempChildNodes.item(0)).getTagName());
//        assertEquals("hal", ((Element)tempChildNodes.item(1)).getTagName());
//        assertEquals("klack", ((Element)tempChildNodes.item(2)).getTagName());
//        assertEquals("humpf", ((Element)tempChildNodes.item(3)).getTagName());
//
//        tempHandler = new PlantySaxDefaultHandler(3, 1);
//        tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/test3.xml");
//        try {
//            tempParser.parse(tempStream, tempHandler);
//        } catch(SAXParseException e) {
//        }
//        tempElement = tempHandler.getParentElement(true);
//        assertNotNull(tempElement);
//        assertEquals("bla", tempElement.getTagName());
//
//        tempHandler = new PlantySaxDefaultHandler(0, 46);
//        tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/test4.xml");
//        try {
//            tempParser.parse(tempStream, tempHandler);
//        } catch(SAXParseException e) {
//        }
//        tempElement = tempHandler.getParentElement(true);
//        assertNotNull(tempElement);
//        assertEquals("target", tempElement.getTagName());

    }

	public boolean isMsOs() {
		String tempSeparator = System.getProperty("line.separator");
		if(tempSeparator.length() > 1) {
			return true;
		}
		return false;
	}
	
    /**
     * Tests the creation of the XmlElement, that includes parsing a file
     * and determining the correct location of the tags.
     */
    public void testCreationOfOutlineTree() {
        InputStream tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/buildtest1.xml");
        TestFile tempFile = new TestFile(tempStream);
        TestOutlinePage tempPage = new TestOutlinePage();
        XmlElement tempElement = tempPage.getContentOutline(tempFile);
        assertNotNull(tempElement);
        
        // Get the content as string
        tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/buildtest1.xml");
        tempFile = new TestFile(tempStream);
        String tempWholeDocumentString = tempPage.getFileContentAsString(tempFile);

        
        // <project>
        XmlElement tempProjectEle = (XmlElement)tempElement.getChildNodes().get(0);
        assertNotNull(tempProjectEle);
        assertEquals(2, tempProjectEle.getStartingRow());
        assertEquals(1, tempProjectEle.getStartingColumn());
        int tempOffset = tempWholeDocumentString.indexOf("<project");
        if(isMsOs()) {
        	tempOffset++;
        }    
	    assertEquals(tempOffset, tempProjectEle.getOffset());
        
        List tempList = tempProjectEle.getChildNodes();
		
		// <property name="propD">
		XmlElement tempEle = (XmlElement)tempList.get(0);
        assertEquals(3, tempEle.getStartingRow());
        assertEquals(2, tempEle.getStartingColumn()); // with tab in file
        assertEquals(3, tempEle.getEndingRow());
        assertEquals(40, tempEle.getEndingColumn());  // with tab in file
        tempOffset = tempWholeDocumentString.indexOf("<property");
        if(isMsOs()) {
        	tempOffset+=2;
        }    
        assertEquals(tempOffset, tempEle.getOffset());
// (T)
        int tempLength = "<property name=\"propD\" value=\"valD\" />".length();
        assertEquals(tempLength, tempEle.getLength());

		
		// <property file="buildtest1.properties">
		tempEle = (XmlElement)tempList.get(1);
        assertEquals(4, tempEle.getStartingRow());
        assertEquals(5, tempEle.getStartingColumn()); // no tab
        assertEquals(4, tempEle.getEndingRow());
        assertEquals(46, tempEle.getEndingColumn());
		
		// <property name="propV">
		tempEle = (XmlElement)tempList.get(2);
        assertEquals(5, tempEle.getStartingRow());
        assertEquals(5, tempEle.getStartingColumn());
        assertEquals(5, tempEle.getEndingRow());
        assertEquals(43, tempEle.getEndingColumn());
		
		// <target name="main">
		tempEle = (XmlElement)tempList.get(3);
        assertEquals(6, tempEle.getStartingRow());
        assertEquals(5, tempEle.getStartingColumn());
        assertEquals(8, tempEle.getEndingRow());
        assertEquals(14, tempEle.getEndingColumn());
		
		// <property name="property_in_target">
		tempEle = (XmlElement)tempEle.getChildNodes().get(0);
        assertEquals(7, tempEle.getStartingRow());
        assertEquals(9, tempEle.getStartingColumn());
        assertEquals(7, tempEle.getEndingRow());
        assertEquals(58, tempEle.getEndingColumn());
        tempOffset = tempWholeDocumentString.indexOf("<property name=\"property_in_target\"");
        if(isMsOs()) {
        	tempOffset+=6;
        }    
        assertEquals(tempOffset, tempEle.getOffset());
		
        assertEquals(9, tempProjectEle.getEndingRow());
        assertEquals(11, tempProjectEle.getEndingColumn());
                
    }

    /**
     * Tests the creation of the XmlElement, that includes parsing a non-valid 
     * file.
     */
    public void testParsingOfNonValidFile() {
        InputStream tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/buildtest2.xml");
        TestFile tempFile = new TestFile(tempStream);
        TestOutlinePage tempPage = new TestOutlinePage();
        XmlElement tempElement = tempPage.getContentOutline(tempFile);
        assertNotNull(tempElement);

        // Get the content as string
        tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/buildtest2.xml");
        tempFile = new TestFile(tempStream);
        String tempWholeDocumentString = tempPage.getFileContentAsString(tempFile);

        XmlElement tempProjectEle = (XmlElement)tempElement.getChildNodes().get(0);
        List tempList = tempProjectEle.getChildNodes();

		// <target name="main">
		XmlElement tempEle = (XmlElement)tempList.get(2);
        assertEquals(5, tempEle.getStartingRow());
        assertEquals(2, tempEle.getStartingColumn()); // with tab in file
        assertEquals(0, tempEle.getEndingRow());
        assertEquals(0, tempEle.getEndingColumn());
        int tempOffset = tempWholeDocumentString.indexOf("<target name=\"main\"");
        if(isMsOs()) {
        	tempOffset+=4;
        }    
	    assertEquals(tempOffset, tempEle.getOffset()); // ???
    }

	
	/**
	 * Tests whether the outline can handle empty files.
	 */
	public void testWithEmptyBuildFile() {
        InputStream tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/emptyfile.xml");
        TestFile tempFile = new TestFile(tempStream);
        TestOutlinePage tempPage = new TestOutlinePage();
        XmlElement tempElement = tempPage.getContentOutline(tempFile);
        assertNotNull(tempElement);
	}		

	
	/**
	 * Some testing of getting the right location of tags.
	 */
	public void testAdvancedTaskLocationing() {
        InputStream tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/outline_select_test_build.xml");
        TestFile tempFile = new TestFile(tempStream);
        TestOutlinePage tempPage = new TestOutlinePage();
        XmlElement tempElement = tempPage.getContentOutline(tempFile);
        assertNotNull(tempElement);
        
        // Get the content as string
        tempStream = getClass().getResourceAsStream("/de/gebit/planty/test/outline_select_test_build.xml");
        tempFile = new TestFile(tempStream);
        String tempWholeDocumentString = tempPage.getFileContentAsString(tempFile);
        
        // <project>
        XmlElement tempProjectEle = (XmlElement)tempElement.getChildNodes().get(0);
        assertNotNull(tempProjectEle);
        assertEquals(2, tempProjectEle.getStartingRow());
        assertEquals(1, tempProjectEle.getStartingColumn());
        int tempOffset = tempWholeDocumentString.indexOf("<project");
        if(isMsOs()) {
        	tempOffset++;
        }    
	    assertEquals(tempOffset, tempProjectEle.getOffset());
        
		// <target name="properties">
        XmlElement tempEle = (XmlElement)tempProjectEle.getChildNodes().get(1);
        assertNotNull(tempEle);
        assertEquals("properties", tempEle.getAttributeNamed("name").getValue());
        assertEquals(16, tempEle.getStartingRow());
        assertEquals(2, tempEle.getStartingColumn());
        tempOffset = tempWholeDocumentString.indexOf("<target name=\"properties\"");
        if(isMsOs()) {
        	tempOffset+=15;
        }    
	    assertEquals(tempOffset, tempEle.getOffset());

//        List tempList = tempProjectEle.getChildNodes();
	}


    public static Test suite() {
        TestSuite suite = new TestSuite("PlantyContentOutlineTest");
        suite.addTest(new PlantyContentOutlineTest("testOutlinePreparingHandler"));
        suite.addTest(new PlantyContentOutlineTest("testCreationOfOutlineTree"));
        suite.addTest(new PlantyContentOutlineTest("testParsingOfNonValidFile"));
        suite.addTest(new PlantyContentOutlineTest("testAdvancedTaskLocationing"));
        suite.addTest(new PlantyContentOutlineTest("testWithEmptyBuildFile"));
        return suite;
    }

}
