package org.eclipse.team.tests.ccvs.core.compatible;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.awtui.TestRunner;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.JUnitTestCase;

public class ModuleTest extends JUnitTestCase {
	
	SameResultEnv env1;
	
	public ModuleTest() {
		this(null);
	}
	
	public ModuleTest(String arg) {
		super(arg);
		env1 = new SameResultEnv(arg, getFile("checkout1"));
	}

	public void setUp() throws Exception {
		env1.setUp();

		// Set the project to the content we need ...
		env1.deleteRemoteResource("CVSROOT/modules");
		env1.deleteRemoteResource("CVSROOT/modules,v");
	}
	
	public void tearDown() throws Exception {
		env1.tearDown();
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ModuleTest.class);
		return new CompatibleTestSetup(suite);
	}	
	
	private void setUpModuleFile(String[] change) throws Exception {
		
		// Write the modules-file
		env1.execute("co",EMPTY_ARGS,new String[]{"CVSROOT"});
		env1.writeToFile("CVSROOT/modules",change);
		
		// Send it up to the server
		env1.execute("add",new String[]{"-m","m"},new String[]{"modules"},"CVSROOT");
		env1.execute("ci",new String[]{"-m","m"},new String[]{"CVSROOT"});
		env1.deleteFile("CVSROOT");		
	}
	
	public void testSimpleModule() throws Exception {
		
		setUpModuleFile(new String[]{"mod1 proj2"});
		env1.createRemoteProject("proj2",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt","f2/f3/e.txt"});
		
		env1.execute("co",EMPTY_ARGS,new String[]{"mod1"});
		env1.execute("co",new String[] {"-d", "mod1-copy"}, new String[]{"mod1"});
		
		env1.appendToFile("mod1/a.txt","Append");
		env1.execute("ci",new String[]{"-m","m"},new String[]{"mod1"});
		
		env1.execute("update",EMPTY_ARGS,new String[]{"mod1"});
		env1.execute("update",EMPTY_ARGS,new String[]{"mod1-copy"});
	}

	public void testCompositeModule() throws Exception {
		
		setUpModuleFile(new String[]{
			"mod1-f1 proj2/f1",
			"mod1-f2 proj2/f2",
			"mod1f &mod1-f1 &mod1-f2"});
		env1.createRemoteProject("proj2",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt","f2/f3/e.txt"});
		
		env1.execute("co",EMPTY_ARGS,new String[]{"mod1f"});
		env1.execute("co",new String[] {"-d", "mod1f-copy"}, new String[]{"mod1f"});
		
		env1.appendToFile("mod1f/mod1-f1/b.txt","Append");
		env1.appendToFile("mod1f/mod1-f2/d.txt","Append");
		env1.execute("ci",new String[]{"-m","m"},new String[]{"mod1f"});
		
		env1.execute("update",EMPTY_ARGS,new String[]{"mod1f"});
		env1.execute("update",EMPTY_ARGS,new String[]{"mod1f-copy"});
	}

	public void testCompositeAliasModule() throws Exception {
		
		setUpModuleFile(new String[]{
			"mod1-f1 proj2/f1",
			"mod1t proj2/f1 b.txt",
			"mod1-f2 &proj2/f2 &mod1t", // XXX &proj2 is not a moduel definition!!!
			"mod1f -a mod1-f1 mod1-f2"});
		env1.createRemoteProject("proj2",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt","f2/f3/e.txt"});
		
		env1.execute("co",EMPTY_ARGS,new String[]{"mod1f"});
		env1.execute("co",new String[] {"-d", "mod1f-copy"}, new String[]{"mod1f"});
		
		env1.appendToFile("mod1-f1/c.txt","Append");
		env1.appendToFile("mod1-f2/mod1t/b.txt","Append");
		env1.appendToFile("mod1-f1/b.txt","Append");
		env1.execute("ci",new String[]{"-m","m"},new String[]{"mod1-f1","mod1-f2"});
		
		env1.execute("update",EMPTY_ARGS,new String[]{"mod1-f1","mod1-f2"});
		env1.execute("update",EMPTY_ARGS,new String[]{"mod1f-copy"});
	}
	
	public void testSelfReferencingModule() throws Exception {
		
		// Setup the modules file and 
		setUpModuleFile(new String[]{
				"project1 project1 &project2", 
				"project2 project2"});
		env1.createRemoteProject("project1",new String[]{"a.txt","f1/b.txt","f1/c.txt","f2/d.txt","f2/f3/e.txt"});
		env1.createRemoteProject("project2",new String[]{"e.txt","f10/b.txt","f10/c.txt","f20/d.txt","f20/f30/e.txt"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"project1"});
		env1.execute("co",new String[] {"-d", "project1-copy"}, new String[]{"project1"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("project1/a.txt","Append");
		env1.appendToFile("project1/project2/e.txt","Append More");
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"project1"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"project1"});
		env1.execute("update",EMPTY_ARGS,new String[]{"project1-copy"});
	}
	
	public void testMinusD() throws Exception {
		
		// Setup the modules file and 
		setUpModuleFile(new String[]{
				"help-docs -d docs common/docs", 
				"macros common/macros",
				"project project &help-docs" });
		env1.createRemoteProject("common",new String[]{"docs/readme.txt","macros/macro1"});
		env1.createRemoteProject("project",new String[]{"file-p2.txt"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"help-docs"});
		env1.execute("co",new String[] {"-d", "docs-copy"}, new String[]{"help-docs"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("docs/readme.txt","Append");;
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"docs"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"docs"});
		env1.execute("update",EMPTY_ARGS,new String[]{"docs-copy"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"macros"});
		env1.execute("co",new String[] {"-d", "macros-copy"}, new String[]{"macros"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("macros/macro1","Append");;
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"macros"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"macros"});
		env1.execute("update",EMPTY_ARGS,new String[]{"macros-copy"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"project"});
		env1.execute("co",new String[] {"-d", "project-copy"}, new String[]{"project"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("project/docs/readme.txt","Append");;
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"project"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"project"});
		env1.execute("update",EMPTY_ARGS,new String[]{"project-copy"});
	}
	
	public void testFileAlias() throws Exception {
		
		// Setup the modules file and 
		setUpModuleFile(new String[]{
				"project3-src  project3/src",
				"project3-src_file -a project3-src/file.c project3-src/file.h",
				"project3-sub  project3/sub &project3-src_file" });
		env1.createRemoteProject("project3",new String[]{"src/file.c", "src/file.h", "sub/file-sub.txt"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"project3-sub"});
		env1.execute("co",new String[] {"-d", "project3-sub-copy"}, new String[]{"project3-sub"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("project3-sub/project3-src/file.c","Append");
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"project3-sub"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"project3-sub"});
		env1.execute("update",EMPTY_ARGS,new String[]{"project3-sub-copy"});
	}
	
	public void testEmbedding() throws Exception {
		
		// Setup the modules file and 
		setUpModuleFile(new String[]{
			"macros common/macros",
			"project4 project4 &macros",
			"project5-project4 -d extensions/project4 project4",
			"project5 project5 &project5-project4 &macros", });
		env1.createRemoteProject("common",new String[]{"docs/readme.txt","macros/macro1"});
		env1.createRemoteProject("project4",new String[]{"file-p4.txt"});
		env1.createRemoteProject("project5",new String[]{"file-p5.txt"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"project4"});
		env1.execute("co",new String[] {"-d", "project4-copy"}, new String[]{"project4"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("project4/file-p4.txt","Append");
		env1.appendToFile("project4/macros/macro1","Append");;
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"project4"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"project4"});
		env1.execute("update",EMPTY_ARGS,new String[]{"project4-copy"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"project5"});
		env1.execute("co",new String[] {"-d", "project5-copy"}, new String[]{"project5"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("project5/file-p5.txt","Append");
		env1.appendToFile("project5/extensions/project4/file-p4.txt","Append");
		env1.appendToFile("project5/macros/macro1","Append");
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"project5"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"project5"});
		env1.execute("update",EMPTY_ARGS,new String[]{"project5-copy"});
		
	}
	
	public void testEmbeddingDirectories() throws Exception {
		
		// Setup the modules file and 
		setUpModuleFile(new String[]{
			"project6-dirA -d dirA project6/A",
			"project6-dirB -d dirB project6/B",
			"project6 &project6-dirA &project6-dirB" });
		env1.createRemoteProject("project6",new String[]{"A/a.txt", "B/b.txt", "c.txt"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"project6"});
		env1.execute("co",new String[] {"-d", "project6-copy"}, new String[]{"project6"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("project6/dirA/a.txt","Append");
		env1.appendToFile("project6/dirB/b.txt","Append");
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"project6"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"project6"});
		env1.execute("update",EMPTY_ARGS,new String[]{"project6-copy"});
	}
	
	public void testAliasPackaging() throws Exception {
		
		// Setup the modules file and 
		setUpModuleFile(new String[]{
			"project7-common -a project7/common",
			"project7-pc -a project7-common project7/pc",
			"project7-linux -a project7-common project7/linux" });
		env1.createRemoteProject("project7",new String[]{"common/com.txt", "pc/file.txt", "linux/file.txt"});
		
		// Checkout the module and a copy of the module 
		env1.execute("co",EMPTY_ARGS,new String[]{"project7-pc"});
		// XXX The reference client does not allow the following checkout
//		env1.execute("co",new String[] {"-d", "project7-pc-copy"}, new String[]{"project7-pc"});
		
		// Change some files in directories mapped to different remote dirs
		env1.appendToFile("project7/common/com.txt","Append");
		env1.appendToFile("project7/pc/file.txt","Append");
		env1.execute("ci", new String[]{"-m","m"}, new String[]{"project7"});
		
		// Update the project and the copy
		env1.execute("update",EMPTY_ARGS,new String[]{"project7"});
//		env1.execute("update",EMPTY_ARGS,new String[]{"project7-pc-copy"});
	}
	
}

