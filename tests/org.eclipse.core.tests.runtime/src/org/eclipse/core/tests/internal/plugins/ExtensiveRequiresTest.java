/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.internal.plugins;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.tests.harness.*;
import java.io.*;
import java.net.URL;
import junit.framework.*;
import org.xml.sax.*;

public class ExtensiveRequiresTest extends EclipseWorkspaceTest {

public ExtensiveRequiresTest() {
	super(null);
}

public ExtensiveRequiresTest(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new ExtensiveRequiresTest("noRequiresTest"));
	suite.addTest(new ExtensiveRequiresTest("emptyRequiresTest"));
	suite.addTest(new ExtensiveRequiresTest("oneRequiresTest"));
	suite.addTest(new ExtensiveRequiresTest("multiRequiresTest"));
	suite.addTest(new ExtensiveRequiresTest("matchRequiresTest"));
	suite.addTest(new ExtensiveRequiresTest("exportRequiresTest"));
	suite.addTest(new ExtensiveRequiresTest("optionalRequiresTest"));
	return suite;
}

public void noRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveRequiresTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/noRequiresTest.xml");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("1.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("1.1 Got the right plugin", plugin.getId().equals("noRequiresTest"));
	assertNull("1.2 No prerequisites", plugin.getRequires());
}

public void emptyRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveRequiresTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/emptyRequiresTest.xml");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("2.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("2.1 Got the right plugin", plugin.getId().equals("emptyRequiresTest"));
	assertNull("2.2 No prerequisites", plugin.getRequires());
}

public void oneRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveRequiresTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/oneRequiresTest.xml");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("3.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("3.1 Got the right plugin", plugin.getId().equals("oneRequiresTest"));
	PluginPrerequisiteModel[] prereqArray = plugin.getRequires();
	assertTrue("3.2 One prerequisite", prereqArray.length == 1);
	assertTrue("3.3 Right prerequisite", prereqArray[0].getPlugin().equals("tests.b"));
}

public void multiRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveRequiresTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/multiRequiresTest.xml");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("4.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("4.1 Got the right plugin", plugin.getId().equals("multiRequiresTest"));
	PluginPrerequisiteModel[] prereqArray = plugin.getRequires();
	assertTrue("4.2 Six prerequisites", prereqArray.length == 6);
	
	// Check each of the prerequisites
    // <import plugin="firstPrerequisite"/>
	PluginPrerequisiteModel prereq = prereqArray[0];
    assertTrue("4.3 1st prerequisite name", prereq.getPlugin().equals("firstPrerequisite"));
    assertEquals("4.4 1st prerequisite match", prereq.getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
    assertTrue("4.5 1st prerequisite export", !prereq.getExport());
    assertTrue("4.6 1st prerequisite optional", !prereq.getOptional());
    assertNull("4.7 1st prerequisite version", prereq.getVersion());
    
    // <import plugin="secondPrerequisite" version="1.0.0"/>
	prereq = prereqArray[1];
    assertTrue("4.8 2nd prerequisite name", prereq.getPlugin().equals("secondPrerequisite"));
    assertEquals("4.9 2nd prerequisite match", prereq.getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
    assertTrue("4.10 2nd prerequisite export", !prereq.getExport());
    assertTrue("4.11 2nd prerequisite optional", !prereq.getOptional());
    assertTrue("4.12 2nd prerequisite version", prereq.getVersion().equals("1.0.0"));
    
    // <import plugin="thirdPrerequisite"/>
	prereq = prereqArray[2];
    assertTrue("4.13 3rd prerequisite name", prereq.getPlugin().equals("thirdPrerequisite"));
    assertEquals("4.14 3rd prerequisite match", prereq.getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
    assertTrue("4.15 3rd prerequisite export", !prereq.getExport());
    assertTrue("4.16 3rd prerequisite optional", !prereq.getOptional());
    assertNull("4.17 3rd prerequisite version", prereq.getVersion());
    
    // <import plugin="fourthPrerequisite"/>
	prereq = prereqArray[3];
    assertTrue("4.18 4th prerequisite name", prereq.getPlugin().equals("fourthPrerequisite"));
    assertEquals("4.19 4th prerequisite match", prereq.getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
    assertTrue("4.20 4th prerequisite export", !prereq.getExport());
    assertTrue("4.21 4th prerequisite optional", !prereq.getOptional());
    assertNull("4.22 4th prerequisite version", prereq.getVersion());
    
    // <import plugin="fifthPrerequisite"/>
	prereq = prereqArray[4];
    assertTrue("4.23 5th prerequisite name", prereq.getPlugin().equals("fifthPrerequisite"));
    assertEquals("4.24 5th prerequisite match", prereq.getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
    assertTrue("4.25 5th prerequisite export", !prereq.getExport());
    assertTrue("4.26 5th prerequisite optional", !prereq.getOptional());
    assertNull("4.27 5th prerequisite version", prereq.getVersion());
    
    // <import plugin="sixthPrerequisite" version="6.9.10"/>
	prereq = prereqArray[5];
    assertTrue("4.28 6th prerequisite name", prereq.getPlugin().equals("sixthPrerequisite"));
    assertEquals("4.29 6th prerequisite match", prereq.getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
    assertTrue("4.30 6th prerequisite export", !prereq.getExport());
    assertTrue("4.31 6th prerequisite optional", !prereq.getOptional());
    assertTrue("4.32 6th prerequisite version", prereq.getVersion().equals("6.9.10"));
}

public void matchRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveRequiresTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath[] = new String[6];
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/match1RequiresTest.xml");
	pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/match2RequiresTest.xml");
	pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/match3RequiresTest.xml");
	pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/match4RequiresTest.xml");
	pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/match5RequiresTest.xml");
	pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/match6RequiresTest.xml");
	URL pluginURLs[] = new URL[6];
	for (int i = 0; i < pluginPath.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPath[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath, true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("5.0 Four plugins", pluginDescriptors.length == 6);
	
	// <import plugin="tests.b"/>
	IPluginDescriptor pluginArray[] = registry.getPluginDescriptors("match1RequiresTest");
	assertTrue("5.1 Got the right plugin", pluginArray.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginArray[0];
	PluginPrerequisiteModel[] prereqArray = plugin.getRequires();
	assertTrue("5.2 One prerequisite", prereqArray.length == 1);
	PluginPrerequisiteModel prereq = prereqArray[0];
	assertTrue("5.3 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("5.4 Unspecified match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
	
	// <import plugin="tests.b" match="compatible"/>
	pluginArray = registry.getPluginDescriptors("match2RequiresTest");
	assertTrue("5.5 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("5.6 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("5.7 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("5.8 Specified compatible match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE);
	
	// <import plugin="tests.b" match="equivalent"/>
	pluginArray = registry.getPluginDescriptors("match3RequiresTest");
	assertTrue("5.9 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("5.10 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("5.11 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("5.12 Specified exact match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT);
	
	// <import plugin="tests.c" match="perfect"/>
	pluginArray = registry.getPluginDescriptors("match4RequiresTest");
	assertTrue("5.13 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("5.14 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("5.15 Right prerequisite", prereq.getPlugin().equals("tests.c"));
	assertTrue("5.16 Specified exact match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_PERFECT);
	
	// <import plugin="tests.c" match="greaterOrEqual"/>
	pluginArray = registry.getPluginDescriptors("match5RequiresTest");
	assertTrue("5.17 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("5.18 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("5.19 Right prerequisite", prereq.getPlugin().equals("tests.c"));
	assertTrue("5.20 Specified exact match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL);
	
	pluginArray = registry.getPluginDescriptors("match6RequiresTest");
	assertTrue("5.21 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("5.22 Three prerequisites", prereqArray.length == 5);

	// <import plugin="tests.a"/>
	prereq = prereqArray[0];
	assertTrue("5.23 Right prerequisite", prereq.getPlugin().equals("tests.a"));
	assertTrue("5.24 Unspecified match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED);
    // <import plugin="tests.b" match="greaterOrEqual"/>
	prereq = prereqArray[1];
	assertTrue("5.25 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("5.26 Specified exact match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL);
    // <import plugin="tests.c" match="compatible"/>
	prereq = prereqArray[2];
	assertTrue("5.27 Right prerequisite", prereq.getPlugin().equals("tests.c"));
	assertTrue("5.28 Specified compatible match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE);
    // <import plugin="tests.c" match="equivalent"/>
	prereq = prereqArray[3];
	assertTrue("5.29 Right prerequisite", prereq.getPlugin().equals("tests.c"));
	assertTrue("5.30 Specified compatible match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT);
    // <import plugin="tests.c" match="perfect"/>
	prereq = prereqArray[4];
	assertTrue("5.31 Right prerequisite", prereq.getPlugin().equals("tests.c"));
	assertTrue("5.32 Specified compatible match", prereq.getMatchByte() == PluginPrerequisiteModel.PREREQ_MATCH_PERFECT);
}

public void exportRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveRequiresTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath[] = new String[4];
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/export1RequiresTest.xml");
	pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/export2RequiresTest.xml");
	pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/export3RequiresTest.xml");
	pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/export4RequiresTest.xml");
	URL pluginURLs[] = new URL[4];
	for (int i = 0; i < pluginPath.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPath[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath, true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("6.0 Four plugins", pluginDescriptors.length == 4);
	
	// <import plugin="tests.b"/>
	IPluginDescriptor pluginArray[] = registry.getPluginDescriptors("export1RequiresTest");
	assertTrue("6.1 Got the right plugin", pluginArray.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginArray[0];
	PluginPrerequisiteModel[] prereqArray = plugin.getRequires();
	assertTrue("6.2 One prerequisite", prereqArray.length == 1);
	PluginPrerequisiteModel prereq = prereqArray[0];
	assertTrue("6.3 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("6.4 Unspecified export", !prereq.getExport());
	
	// <import plugin="tests.b" export="true"/>
	pluginArray = registry.getPluginDescriptors("export2RequiresTest");
	assertTrue("6.5 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("6.6 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("6.7 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("6.8 Specified export", prereq.getExport());
	
	// <import plugin="tests.b" export="false"/>
	pluginArray = registry.getPluginDescriptors("export3RequiresTest");
	assertTrue("6.9 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("6.10 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("6.11 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("6.12 Specified false export", !prereq.getExport());
	
	pluginArray = registry.getPluginDescriptors("export4RequiresTest");
	assertTrue("6.13 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("6.14 Three prerequisites", prereqArray.length == 3);
	// <import plugin="tests.a" export="true"/>
	prereq = prereqArray[0];
	assertTrue("6.15 Right prerequisite", prereq.getPlugin().equals("tests.a"));
	assertTrue("6.16 Specified export", prereq.getExport());
    // <import plugin="tests.b"/>
	prereq = prereqArray[1];
	assertTrue("6.17 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("6.18 Unspecified export", !prereq.getExport());
    // <import plugin="tests.c" export="false"/>
	prereq = prereqArray[2];
	assertTrue("6.19 Right prerequisite", prereq.getPlugin().equals("tests.c"));
	assertTrue("6.20 Specified false export", !prereq.getExport());
}

public void optionalRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveRequiresTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath[] = new String[4];
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/optional1RequiresTest.xml");
	pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/optional2RequiresTest.xml");
	pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/optional3RequiresTest.xml");
	pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveRequiresTest/optional4RequiresTest.xml");
	URL pluginURLs[] = new URL[4];
	for (int i = 0; i < pluginPath.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPath[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath, true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("7.0 Four plugins", pluginDescriptors.length == 4);
	
	// <import plugin="tests.b"/>
	IPluginDescriptor pluginArray[] = registry.getPluginDescriptors("optional1RequiresTest");
	assertTrue("7.1 Got the right plugin", pluginArray.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginArray[0];
	PluginPrerequisiteModel[] prereqArray = plugin.getRequires();
	assertTrue("7.2 One prerequisite", prereqArray.length == 1);
	PluginPrerequisiteModel prereq = prereqArray[0];
	assertTrue("7.3 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("7.4 Unspecified optional", !prereq.getOptional());
	
	// <import plugin="tests.b" optional="true"/>
	pluginArray = registry.getPluginDescriptors("optional2RequiresTest");
	assertTrue("7.5 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("7.6 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("7.7 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("7.8 Specified optional", prereq.getOptional());
	
	// <import plugin="tests.b" optional="false"/>
	pluginArray = registry.getPluginDescriptors("optional3RequiresTest");
	assertTrue("7.9 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("7.10 One prerequisite", prereqArray.length == 1);
	prereq = prereqArray[0];
	assertTrue("7.11 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("7.12 Specified false optional", !prereq.getOptional());
	
	pluginArray = registry.getPluginDescriptors("optional4RequiresTest");
	assertTrue("7.13 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	prereqArray = plugin.getRequires();
	assertTrue("7.14 Three prerequisites", prereqArray.length == 3);
	// <import plugin="tests.a" optional="true"/>
	prereq = prereqArray[0];
	assertTrue("7.15 Right prerequisite", prereq.getPlugin().equals("tests.a"));
	assertTrue("7.16 Specified optional", prereq.getOptional());
    // <import plugin="tests.b"/>
	prereq = prereqArray[1];
	assertTrue("7.17 Right prerequisite", prereq.getPlugin().equals("tests.b"));
	assertTrue("7.18 Unspecified optional", !prereq.getOptional());
    // <import plugin="tests.c" optional="false"/>
	prereq = prereqArray[2];
	assertTrue("7.19 Right prerequisite", prereq.getPlugin().equals("tests.c"));
	assertTrue("7.20 Specified false optional", !prereq.getOptional());
}

}

