package org.eclipse.team.tests.ccvs.core.cvsresources;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class ResourceSyncInfoTest extends EclipseTest {

	public ResourceSyncInfoTest() {
		super();
	}
	
	public ResourceSyncInfoTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ResourceSyncInfoTest.class);
		return new CVSTestSetup(suite);
	}
		
	/**
	 * Testing that the resource sync parses entry lines correctly.
	 */
	public void testEntryLineParsing() {
		String entryLine;
		
		// testing malformed entry lines first
		try {
			new ResourceSyncInfo("//////", null, null);			
			fail();
		} catch(CVSException e) {
		}
		try {
			new ResourceSyncInfo("//1.1///", null, null);			
			fail();
		} catch(CVSException e) {
		}
		try {
			new ResourceSyncInfo("/file.txt////", null, null);			
			fail();
		} catch(CVSException e) {
		}
		try {
			new ResourceSyncInfo("/file.txt//////////", null, null);			
			fail();
		} catch(CVSException e) {
		}
	}
	
	/**
	 * Testing that the entry line constructor
	 */
	public void testEntryLineConstructor() {		
		try {
			ResourceSyncInfo info;
			info = new ResourceSyncInfo("/file.java/1.1/27 Sep 2001 18:00:16/-k/", null, null);
			info = new ResourceSyncInfo("/file.java/-1.1/27 Sep 2001 18:00:16/-k/", null, null);
			assertTrue(info.isDeleted());
			
			info = new ResourceSyncInfo("/file.java/0/27 Sep 2001 18:00:16/-k/", null, null);
			assertTrue(info.isAdded());
			
			info = new ResourceSyncInfo("/file.java/0/27 Sep 2001 18:00:16/-k/Tv1", null, null);
			assertTrue(info.getTag() != null);
			
			String timestamp = "10 Sep 2000 18:00:16";
			info = new ResourceSyncInfo("/file.java/0/27 Sep 2001 18:00:16/-k/Tv1", null, timestamp);
			assertTrue(info.getTimeStamp().equals(timestamp));
			
			String permissions = "u=rwx,g=rwx,o=rwx";
			info = new ResourceSyncInfo("/file.java/0/27 Sep 2001 18:00:16/-k/Tv1", permissions, null);
			assertTrue(info.getPermissions().equals(permissions));
			
			info = new ResourceSyncInfo("D/file.java////", null, null);
			assertTrue(info.isDirectory());
			
		} catch(CVSException e) {
			fail("end");
		}
	}
	
	/**
	 * Testing the parameter constructor
	 */
	public void testConstructor() {
		ResourceSyncInfo info;
		
		info = new ResourceSyncInfo("folder");
		assertTrue(info.isDirectory());
		
		info = new ResourceSyncInfo("file.txt", "-2.34", "27 Sep 2001 18:00:16", "", null, "");
		assertTrue(info.isDeleted());
		assertTrue(info.getRevision().equals("2.34"));
		
		info = new ResourceSyncInfo("file.txt", "0", "27 Sep 2001 18:00:16", "", null, "");
		assertTrue(info.isAdded());
		
		CVSTag tag = new CVSTag("v1", CVSTag.VERSION);
		info = new ResourceSyncInfo("file.txt", "1.1", "27 Sep 2001 18:00:16", "", tag, "");
		CVSTag newTag = info.getTag();
		assertTrue(newTag.getName().equals(tag.getName()) && newTag.getType() == tag.getType());
		assertTrue(info.getRevision().equals("1.1"));
	}
}

