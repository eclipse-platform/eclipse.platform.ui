package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSFile;
import org.eclipse.team.internal.ccvs.core.resources.CVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.ManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.ManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.*;

/**
 * This class is the way to access the current implementations of 
 * ICVSResources and IMangedResources.
 * 
 * All methods with the word "Temp" in it, get a resource that is 
 * relative to a tempFolder rather then the root of the system.
 */
public class ResourceFactory {
	
	public static final File TEMP_ROOT = new File("C:\\temp");
	
	public static String getPath(File ioFile) {
		return ioFile.getAbsolutePath();
	}
		
	public static String getPath(ICVSResource cvsResource) {
		return getIO(cvsResource).getAbsolutePath();
	}
		
	public static String getPath(IManagedFolder managedResource) {
		return getIO(managedResource).getAbsolutePath();
	}
		
	public static File getIO(ICVSResource cvsResource) throws ClassCastException {
		return ((CVSResource) cvsResource).getIOResource();
	}
	
	public static File getIO(IManagedResource managedResource) throws ClassCastException {
		return getIO(getCvs(managedResource));
	}	
	
	public static File getTempIO(String path) {
		return new File(TEMP_ROOT,path);
	}
	
	public static File getIO(String path) {
		return new File(path);
	}
	
	public static ICVSResource getCvs(IManagedResource managedResource) throws ClassCastException {
		return ((ManagedResource) managedResource).getCVSResource();
	}

	public static ICVSFolder getCvs(IManagedFolder managedFolder) throws ClassCastException {
		return (ICVSFolder)((ManagedFolder) managedFolder).getCVSResource();
	}

	public static ICVSFile getCvs(IManagedFile managedFile) throws ClassCastException {
		return (ICVSFile)((ManagedFile) managedFile).getCVSResource();
	}

	public static ICVSResource getCvs(File ioFile) throws CVSException {
		
		if (!ioFile.exists()) {
			throw new CVSFileNotFoundException("File not Found " + ioFile);
		}
		
		if (ioFile.isDirectory()) {
			return CVSFolder.createFolderFrom(ioFile);
		} else if (ioFile.isFile()) {
			return CVSFile.createFileFrom(ioFile);
		} else {
			throw new CVSException("Unexpected error in ResourceFactory");
		}
	}
	
	public static ICVSResource getCvs(String path) throws CVSException {
		return getCvs(getIO(path));
	}
	
	public static ICVSResource getTempCvs(String path) throws CVSException {
		return getCvs(getTempIO(path));
	}
	
	public static ICVSFolder getCvsFolder(File ioFile) throws CVSException {
		return CVSFolder.createFolderFrom(ioFile);
	}

	public static ICVSFolder getCvsFolder(String path) throws CVSException {
		return getCvsFolder(getIO(path));
	}

	public static ICVSFolder getTempCvsFolder(String path) throws CVSException {
		return getCvsFolder(getTempIO(path));
	}

	public static ICVSFolder getCvsFolder(IManagedFolder managedFolder) throws CVSException {
		return getCvs(managedFolder);
	}

	public static ICVSFile getCvsFile(String path) throws CVSException {
		return getCvsFile(getIO(path));		
	}
	
	public static ICVSFile getTempCvsFile(String path) throws CVSException {
		return getCvsFile(getTempIO(path));		
	}
	
	public static ICVSFile getCvsFile(File ioFile) throws CVSException {
		return CVSFile.createFileFrom(ioFile);		
	}
	
	public static ICVSFile getCvsFile(IManagedFile managedFile) throws CVSException {
		return getCvs(managedFile);		
	}
	
	public static IManagedFolder getManaged(ICVSFolder cvsFolder) {
		return ManagedFolder.createResourceFrom(cvsFolder);
	}

	public static IManagedFile getManaged(ICVSFile cvsFile) {
		return ManagedFolder.createResourceFrom(cvsFile);
	}

	public static IManagedResource getManaged(ICVSResource cvsResource) {
		if (cvsResource.isFolder()) {
			return getManaged((ICVSFolder)cvsResource);
		} else {
			return getManaged((ICVSFile)cvsResource);
		}			
	}

	public static IManagedResource getManaged(File file) throws CVSException {
		return getManaged(getCvs(file));		
	}
	
	public static IManagedFolder getManagedFolder(File ioFile) throws CVSException {
		return getManaged(getCvsFolder(ioFile));
	}
	
	public static IManagedFolder getManagedFolder(String path) throws CVSException {
		return getManagedFolder(getIO(path));
	}
	
	public static IManagedFolder getTempManagedFolder(String path) throws CVSException {
		return getManagedFolder(getTempIO(path));
	}
	
	public static IManagedFolder getManagedFolder(ICVSFolder cvsFolder) throws CVSException {
		return getManaged(cvsFolder);
	}
	
	public static IManagedFile getManagedFile(File ioFile) throws CVSException {
		return getManaged(getCvsFile(ioFile));
	}	

	public static IManagedFile getTempManagedFile(String path) throws CVSException {
		return getManagedFile(getTempIO(path));
	}	

	public static IManagedFile getManagedFile(String path) throws CVSException {
		return getManagedFile(getIO(path));
	}	

	public static IManagedFile getManagedFile(ICVSFile cvsFile) throws CVSException {
		return getManaged(cvsFile);
	}	

}

