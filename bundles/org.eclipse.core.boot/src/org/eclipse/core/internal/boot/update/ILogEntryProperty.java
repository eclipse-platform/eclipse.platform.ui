package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ILogEntryProperty {
/**
 * 
 * @return java.lang.String
 */
public String getName();
/**
 * 
 */
public String getValue();
/**
 * 
 * @param strName java.lang.String
 */
public void setName(String name);
/**
 * 
 * @param strValue java.lang.String
 */
public void setValue(String value);
}
