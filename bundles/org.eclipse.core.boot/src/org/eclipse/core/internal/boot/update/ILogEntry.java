package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import java.util.*;
public interface ILogEntry {
/**
 * 
 * @param entryChild 
 */
public void addChildEntry(ILogEntry entryChild);
/**
 * 
 * @param attribute 
 */
public void addProperty(ILogEntryProperty attribute);
/**
 * Returns all child elements
 * @return 
 */
public ILogEntry[] getChildEntries();
/**
 * Returns an array of all child elements with this name
 *
 * @param elementName java.lang.String
 */
public ILogEntry[] getChildEntries(String elementName);
/**
 * Returns the first child entry with the specified name.
 * @return 
 * @param name java.lang.String
 */
public ILogEntry getChildEntry(String name);
/**
 * 
 * @return java.lang.String
 */
String getName();
/**
 * 
 * @return int
 */
public int getNumberOfChildEntries();
/**
 * 
 * @return int
 */
public int getNumberOfProperties();
/**
 * 
 * @return 
 */
public ILogEntryProperty[] getProperties();
/**
 * 
 * @param attributeName java.lang.String
 */
public ILogEntryProperty getProperty(String propertyName);
/**
 * Prints out this entry and its attributes.
 */
public void printPersistentEntryString(StringBuffer strb, int iIndentation);
/**
 * 
 */
public void removeAllChildEntries();
/**
 * 
 */
public void removeAllProperties();
/**
 * 
 * @return boolean
 * @param name java.lang.String
 */
public boolean removeProperty(String name);
}
