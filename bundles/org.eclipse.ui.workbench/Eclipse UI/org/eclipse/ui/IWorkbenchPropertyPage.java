package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferencePage;

/**
 * Interface for workbench property pages. Property pages generally show up in
 * the workbench's Property Pages dialog.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in an extension contributed to the workbench's property page extension point 
 * (named <code>"org.eclipse.ui.propertyPages"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.propertyPages"&GT;
 *      &LT;page id="com.example.myplugin.props"
 *         name="Knobs"
 *         objectClass="org.eclipse.core.resources.IResource"
 *         class="com.example.myplugin.MyPropertyPage" /&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p>
 */
public interface IWorkbenchPropertyPage extends IPreferencePage {
/**
 * Returns the object that owns the properties shown in this property page.
 *
 * @return the object that owns the properties in this page
 */
public IAdaptable getElement();
/**
 * Sets the object that owns the properties shown in this property page.
 * The page is expected to store this object and provide it if
 * <code>getElement</code> is called.
 *
 * @param the object that owns the properties in this page
 */
public void setElement(IAdaptable element);
}
