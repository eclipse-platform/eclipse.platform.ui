package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.net.*;
import java.util.*;
public class ComponentEntryDescriptor
	extends ComponentEntryDescriptorModel
	implements IComponentEntryDescriptor {
	private NLResourceHelper fNLHelper = null;
	private boolean fSelected = false; // selected for installation
/**
 * ComponentEntryDescriptor constructor comment.
 */
public ComponentEntryDescriptor() {
	super();

}
/** Returns whether the component is newer than the component passed as
 * parameter
 * This method returns <code>true</code> if
 * the version number is greater than the one of the component given
 */
public int compare(IComponentDescriptor comp) {
	return new VersionComparator().compare(getVersionStr(), comp.getVersionStr());
}
/** Returns whether the component is newer than the component passed as
 * parameter
 * This method returns <code>true</code> if
 * the version number is greater than the one of the component given
 */
public int compare(IComponentEntryDescriptor comp) {
	return new VersionComparator().compare(getVersionStr(), comp.getVersionStr());
}
public URL getCompInstallURL() {
	try {
		return UMEclipseTree.appendTrailingSlash(_getCompInstallURL());
		
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
/**
 * Returns the ComponentDescriptor this component entry maps to
 *
 * @return the ComponentDescriptor this component entry maps to
 */
public IComponentDescriptor getComponentDescriptor() {
	IComponentDescriptor componentDescriptor = null;
	componentDescriptor = getUMRegistry().getComponentDescriptor(getUniqueIdentifier(),getVersionStr());
	return componentDescriptor;
}
/**
 * Returns the Product this component entry belongs to
 *
 * @return the Product this component entry belongs to
 */
public IProductDescriptor getContainingProduct() {
	return (IProductDescriptor) _getContainingProduct();
}
/**
 * Returns the name of the directory of this component entry in install/components
 * The standard format is compid_version
 *
 *
 * @return the component entry's directory name in install/components
 */
public java.lang.String getDirName() {
	
	String s = _getDirName();
	return s==null ? "" : s;
}
public String getLabel() {
	String s = _getName();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getProdInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
public URL getProdInstallURL() {
	try {
		return UMEclipseTree.appendTrailingSlash(_getProdInstallURL());
		
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
public IUMRegistry getUMRegistry() {
	
	return (IUMRegistry)_getUMRegistry();
}
public String getUniqueIdentifier() {
	
	return _getId();
}
public String getVersion() {

	return _getVersion();

}
public VersionIdentifier getVersionIdentifier() {

	try {
		return new VersionIdentifier(getVersion());
	} catch (Throwable e) {
		return new VersionIdentifier(0,0,0);
	}
}
/**
 * Returns the component version string.
 *
 * @return the component version string
 */
public java.lang.String getVersionStr() {
	return _getVersion();
}
public boolean isAllowedToUpgrade() {
	
	return _isUpgradeable();
}
/**
 * Checks all the upgrade rules to determine if this remote component entry can
 * be downloaded and installed
 * 
 * - isCompatibleWith() the upgrade is to a new minor or service, not a new major
 * - isUpdateable() - checking that all containing products allow the existing installed
 *                    component to be upgraded
 * @return whether this remote component entry can be downloaded and installed.
 */
public int isInstallable(IComponentDescriptor compInstalled) {

	// Ok if component is not currently installed
	//-------------------------------------------
	if (compInstalled == null)
		return UpdateManagerConstants.OK_TO_INSTALL;

	VersionIdentifier newVer = getVersionIdentifier();
	// Check whether the installed component's parent products allow it to be upgraded
	// if not upgradeable, can still install service
	//--------------------------------------------------------------------------------
	if (!compInstalled.isUpdateable()) {
		if (!newVer.isEquivalentTo(compInstalled.getVersionIdentifier()))
			return UpdateManagerConstants.NOT_UPDATABLE;
	}
			
	// Check version against installed component
	//------------------------------------------
	IProductDescriptor[] containingProds = compInstalled.getContainingProducts();
	if (containingProds.length == 0) {		// not constrained by a product
		if (this.compare(compInstalled) > 0)	// newer
			return UpdateManagerConstants.OK_TO_INSTALL;
		return UpdateManagerConstants.NOT_NEWER;
	} else {
		// Check version compatibility
		//----------------------------

		if (!newVer.isCompatibleWith(compInstalled.getVersionIdentifier())) // same major, newer minor or service
			return UpdateManagerConstants.NOT_COMPATIBLE;

		if (compare(compInstalled)== 0)
			return UpdateManagerConstants.NOT_NEWER;
	}
	
	return UpdateManagerConstants.OK_TO_INSTALL;
}
public boolean isInstalled() {
	
	return _isInstalled();
}
public boolean isOptionalForInstall() {
	return _isOptional();
}
public boolean isSelected() {
	return fSelected;
}
public void isSelected(boolean sel) {
	
	fSelected = sel;
}
/**
 * Returns whether the component described by this descriptor
 * can be updated or not.  The following condition is checked:
 *
 * - All its containing products allow the component to be updated
 */
public boolean isUpdateable() {
	// check self
	if (!isAllowedToUpgrade())
		return false;

	// look up the real McCoy in the registry and see what it says
	IComponentDescriptor comp = ((IUMRegistry) _getUMRegistry()).getComponentDescriptor(_getId(), _getVersion());
	if ((comp != null) && (!comp.isUpdateable()))
		return false;
	
	return true;
	
}
}
