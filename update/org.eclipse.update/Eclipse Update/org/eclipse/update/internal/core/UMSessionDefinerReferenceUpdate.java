package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 */
import org.eclipse.core.internal.boot.update.*;
import org.eclipse.core.internal.boot.LaunchInfo;

public class UMSessionDefinerReferenceUpdate implements ISessionDefiner {
/**
 * UMUpdaterCached constructor comment.
 */
public UMSessionDefinerReferenceUpdate() {
	super();
}
/**
 * Creates one or more operations for the current parcel.
 */
public void defineComponentOperation(UMSessionManagerParcel parcel, IInstallable descriptor, boolean bVerifyJars) {

	IComponentDescriptor componentDescriptor = null;

	if (descriptor instanceof IComponentDescriptor) {
		componentDescriptor = (IComponentDescriptor) descriptor;
	}
	else if (descriptor instanceof IComponentEntryDescriptor) {
		componentDescriptor = ((IComponentEntryDescriptor) descriptor).getComponentDescriptor();
	}
	if (componentDescriptor == null)
		return; //LINDA- error condition 

	UMSessionManagerOperation operation = null;

	// Create download operation
	//--------------------------
	operation = parcel.createOperation();
	operation.setAction(UpdateManagerConstants.OPERATION_COPY);
	operation.setId(componentDescriptor.getUniqueIdentifier());
	operation.setSource(componentDescriptor.getDownloadURL().toExternalForm());
	String downloadTarget = UMEclipseTree.getStagingArea().toExternalForm() + componentDescriptor.getDirName() + ".jar";
	operation.setTarget(downloadTarget);
	operation.setData(descriptor);

	// Create jar verification operation
	//----------------------------------
	if (bVerifyJars == true) {
		operation = parcel.createOperation();
		operation.setAction(UpdateManagerConstants.OPERATION_VERIFY_JAR);
		operation.setId(componentDescriptor.getUniqueIdentifier());
		operation.setSource(downloadTarget);
		operation.setTarget(null);
		operation.setData(descriptor);
	}

	// Create apply operation for component files (files not in plugins/ dir structure)
	//---------------------------------------------------------------------------------
	operation = parcel.createOperation();
	operation.setAction(UpdateManagerConstants.OPERATION_UNZIP_INSTALL);
	operation.setId(componentDescriptor.getUniqueIdentifier());
	operation.setSource(downloadTarget);
	String applyTarget = UMEclipseTree.getBaseInstallURL().toExternalForm();
	//	String applyTarget = "file:///c:/temp/x/" ;
	operation.setTarget(applyTarget);
	operation.setData(descriptor);

	// Create apply operation for plug-ins		
	//------------------------------------

	operation = parcel.createOperation();
	operation.setAction(UpdateManagerConstants.OPERATION_UNZIP_PLUGINS);
	operation.setId(componentDescriptor.getUniqueIdentifier());
	operation.setSource(downloadTarget);
	// by specifiying the install dir with a trailing slash, the unzip method
	// will just unzip the contents of the jar into the dir
	applyTarget = UMEclipseTree.getBaseInstallURL().toExternalForm();
	//	applyTarget = "file:///c:/temp/x/" ;
	operation.setTarget(applyTarget);
	operation.setData(descriptor);

}
/**
 * Creates one or more operations for the current session.
 */
public void defineOperations(UMSessionManagerSession session, IInstallable[] descriptors, boolean bVerifyJars) {

	for (int i = 0; i < descriptors.length; ++i) {

		// Products
		//---------
		if (descriptors[i] instanceof IProductDescriptor) {

			UMSessionManagerParcel parcel = null;

			// Create parcel
			//--------------
			parcel = session.createParcel();
			parcel.setType(IManifestAttributes.PRODUCT);
			parcel.setAction(UpdateManagerConstants.OPERATION_ACTION_INSTALL);
			parcel.setId(((IProductDescriptor) descriptors[i]).getUniqueIdentifier());
			parcel.setData((IProductDescriptor) descriptors[i]);

			defineProductOperation(parcel, descriptors[i], bVerifyJars);
		}

		// Components
		//-----------
		else if (descriptors[i] instanceof IComponentDescriptor) {

			UMSessionManagerParcel parcel = null;

			// Create parcel
			//--------------
			parcel = session.createParcel();
			parcel.setType(IManifestAttributes.COMPONENT);
			parcel.setAction(UpdateManagerConstants.OPERATION_ACTION_INSTALL);
			parcel.setId(((IComponentDescriptor) descriptors[i]).getUniqueIdentifier());
			parcel.setData((IComponentDescriptor) descriptors[i]);

			defineComponentOperation(parcel, descriptors[i], bVerifyJars);
		}

		// Component Entries
		//------------------
		else if (descriptors[i] instanceof IComponentEntryDescriptor) {

			UMSessionManagerParcel parcel = null;

			// Create parcel
			//--------------
			parcel = session.createParcel();
			parcel.setType(IManifestAttributes.COMPONENT);
			parcel.setAction(UpdateManagerConstants.OPERATION_ACTION_INSTALL);
			parcel.setId(((IComponentEntryDescriptor) descriptors[i]).getUniqueIdentifier());
			parcel.setData((IComponentEntryDescriptor) descriptors[i]);

			defineComponentOperation(parcel, descriptors[i], bVerifyJars);
		}
	}
}
/**
 * Creates one or more operations for the current parcel.
 */
public void defineProductOperation(UMSessionManagerParcel parcel, IInstallable descriptor, boolean bVerifyJars) {

	IProductDescriptor productDescriptor = (IProductDescriptor) descriptor;
	UMSessionManagerOperation operation = null;

	// Create download operation
	//--------------------------
	operation = parcel.createOperation();
	operation.setAction(UpdateManagerConstants.OPERATION_COPY);
	operation.setId(productDescriptor.getUniqueIdentifier());
	operation.setSource(productDescriptor.getDownloadURL().toExternalForm());
	String downloadTarget = UMEclipseTree.getStagingArea().toString() + productDescriptor.getDirName() + ".jar";
	operation.setTarget(downloadTarget);
	operation.setData(productDescriptor);

	// Create jar verification operation
	//----------------------------------
	if (bVerifyJars == true) {
		operation = parcel.createOperation();
		operation.setAction(UpdateManagerConstants.OPERATION_VERIFY_JAR);
		operation.setId(productDescriptor.getUniqueIdentifier());
		operation.setSource(downloadTarget);
		operation.setTarget(null);
		operation.setData(productDescriptor);
	}

	// Create apply operation for product files     
	//-----------------------------------------
	operation = parcel.createOperation();
	operation.setAction(UpdateManagerConstants.OPERATION_UNZIP_INSTALL);
	operation.setId(productDescriptor.getUniqueIdentifier());
	operation.setSource(downloadTarget);
	String applyTarget = UMEclipseTree.getBaseInstallURL().toExternalForm();
	operation.setTarget(applyTarget);
	operation.setData(productDescriptor);

	// Create component sub-parcels
	//-----------------------------
	IComponentEntryDescriptor[] entries = productDescriptor.getComponentEntries();
	for (int j = 0; j < entries.length; ++j) {
		if (entries[j].isSelected()) {

			UMSessionManagerParcel subparcel = null;
			// Create parcel for each component
			//---------------------------------
			subparcel = parcel.createParcel();
			subparcel.setType(IManifestAttributes.COMPONENT);
			subparcel.setAction(UpdateManagerConstants.OPERATION_ACTION_INSTALL);
			subparcel.setId(entries[j].getUniqueIdentifier());
			subparcel.setData(entries[j]);

			defineComponentOperation(subparcel, entries[j], bVerifyJars);
		}
	}

	// Create apply operation for product files     
	//-----------------------------------------
	String app = LaunchInfo.getCurrent().getApplicationConfigurationIdentifier();
	if ((app != null) && (app.equals(productDescriptor.getUniqueIdentifier()))) { 	// dominant app?
		operation = parcel.createOperation();
		operation.setAction(UpdateManagerConstants.OPERATION_UNZIP_BINDIR);
		operation.setId(productDescriptor.getUniqueIdentifier());
		operation.setSource(downloadTarget);
		operation.setTarget(applyTarget);
		operation.setData(productDescriptor);
	}
}
}
