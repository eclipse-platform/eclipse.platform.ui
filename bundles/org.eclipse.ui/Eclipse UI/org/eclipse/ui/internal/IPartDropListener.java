package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
public interface IPartDropListener {
public void dragOver(PartDropEvent event);
public void drop(PartDropEvent event);
}
