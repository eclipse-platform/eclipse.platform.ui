package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.tests.viewers.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

public class TestList extends TestBrowser {
	public Viewer createViewer(Composite parent) {
		ListViewer viewer= new ListViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new TestModelContentProvider());
		return viewer;
	}
	public static void main(String[] args) {
		TestList browser = new TestList();
		browser.setBlockOnOpen(true);
		browser.open(TestElement.createModel(3, 10));
	}
/**
 * 
 */
protected void viewerFillMenuBar(MenuManager mgr) {
}
}
