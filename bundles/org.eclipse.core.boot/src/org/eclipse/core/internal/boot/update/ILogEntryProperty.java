package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
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
