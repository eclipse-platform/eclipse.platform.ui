package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.*;
/**
 * A Component "entry" descriptor holds information about a component
 * obtained from the containing Product's jar manifest file.
 * This info is kept separate from IComponentDescriptor since it 
 * pertains to the relationship between the component and the product
 * that included it.
 */

public interface IComponentEntryDescriptor extends IInstallable {

/** Returns whether the component is newer than the component passed as
 * parameter
 * This method returns <code>true</code> if
 * the version number is greater than the one of the component given
 */
public int compare(IComponentDescriptor comp);
/** Returns whether the component is newer than the component passed as
 * parameter
 * This method returns <code>true</code> if
 * the version number is greater than the one of the component given
 */
public int compare(IComponentEntryDescriptor comp);
public URL getCompInstallURL() ;
/**
 * Returns the ComponentDescriptor this component entry maps to
 *
 * @return the ComponentDescriptor this component entry maps to
 */

public IComponentDescriptor getComponentDescriptor() ;
/**
 * Returns the Product this component entry belongs to
 *
 * @return the Product this component entry belongs to
 */

public IProductDescriptor getContainingProduct() ;
public URL getProdInstallURL() ;
/**
 * Returns the Registry this component entry belongs to
 *
 * @return the Registry this component entry belongs to
 */

public IUMRegistry getUMRegistry() ;
/**
 * Returns the unique identifier of this component.
 * This identifier is a non-empty string and is unique 
 * within the Update Manager.
 *
 * @return the unique identifier of the component
 */
public String getUniqueIdentifier();
/**
 * Returns the component version identifier.
 *
 * @return the component version identifier
 */
public VersionIdentifier getVersionIdentifier();
/**
 * Returns the component version string.
 *
 * @return the component version string
 */
public String getVersionStr();
/**
 * Returns whether the product allows a compatible upgraded component version
 * be used at runtime (true) or only the version which was packaged with
 * the product can be used (false).  Default is false.
 *
 * @return whether the product allows a compatible upgraded component version be used at runtime (true). (default is <code> "false" </code>)
 */
public boolean isAllowedToUpgrade();
/**
 * Checks all the upgrade rules to determine if this remote component entry can
 * be downloaded and installed
 * 
 * - isCompatibleWith() the upgrade is to a new minor or service, not a new major
 * - isUpdateable() - checking that all containing products allow the existing installed
 *                    component to be upgraded
 * @return whether this remote component entry can be downloaded and installed.
 */
public int isInstallable(IComponentDescriptor comp);
public boolean isInstalled() ;
/**
 * Returns whether the component is an optionally installable part of
 * the product (true) or is required to be always installed (false).
 * Default is true.
 *
 * @return whether the component is an optionally installable part of the product (true). (default is <code> "true" </code>)
 */
public boolean isOptionalForInstall();
/**
 * Returns whether this plug-in has been selected for installation.  Default is false.
 *
 * @return whether this plug-in has been selected for installation.  (default is <code> "false" </code>)
 */
public boolean isSelected() ;
public void isSelected(boolean sel);
/**
 * Returns whether the component entry described by this descriptor
 * can be updated or not.  The following condition is checked:
 * 
 * - All its containing products allow the component entry to be updated
 *
 * @return <code>true</code> if this component entry is updateable, and
 *   <code>false</code> otherwise
 */
public boolean isUpdateable();
}
