package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import java.util.*;
import org.eclipse.core.internal.resources.ResourceStatus; // illegal ref
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Utility class to create status objects.
 *
 * This class is temporary and will be removed in the future.
 * It is a temporary workaround for 1FTKIAP: ITPCORE:ALL - Status/MultiStatus API
 *
 * @private - This class is an internal implementation class and should
 * not be referenced or subclassed outside of the workbench
 *
 * <p>
 * [Issue: Implementation contains illegal references to
 *  org.eclipse.core.internal.resources.ResourceStatus,
 * ]
 * </p>
 */
public class StatusUtil {
/**
 *	Answer a flat collection of the passed status and its recursive children
 */
protected static List flatten(IStatus aStatus) {
	List result = new ArrayList();

	if (aStatus.isMultiStatus()) {
		IStatus[] children = aStatus.getChildren();
		for (int i = 0; i < children.length; i++){
			IStatus currentChild = children[i];
			if (currentChild.isMultiStatus()) {
				Iterator childStatiiEnum = flatten(currentChild).iterator();
				while (childStatiiEnum.hasNext())
					result.add(childStatiiEnum.next());
			} else
				result.add(currentChild);
		}
	} else
		result.add(aStatus);
	
	return result;
}
/**
 * This method must not be called outside the workbench.
 *
 * Utility method for creating status.
 *
 * This method will be removed in the future when kernel includes
 * support for creating status.
 * See 1FTKIAP: ITPCORE:ALL - Status/MultiStatus API
 *
 * Ultimately callers should be able to build their own multi status
 * and add to it rather than building a collection of status then converting it.
 * See 1FTQDWJ: ITPCORE:ALL - API - Status/MultiStatus - should able to add to a multi status
 */
protected static IStatus newStatus(
		IStatus[] stati, 
		IPath path, 
		String message, 
		Throwable exception) {

	Assert.isTrue(message != null);
	Assert.isTrue(message.trim().length() != 0);

	UIHackFinder.fixPR(); 	//1FTKIAP: ITPCORE:ALL - Status/MultiStatus API
	return new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, 
		stati, message, exception);
}
/**
 * This method must not be called outside the workbench.
 *
 * Utility method for creating status.
 *
 * This method will be removed in the future when kernel includes
 * support for creating status.
 * See 1FTKIAP: ITPCORE:ALL - Status/MultiStatus API
 */
public static IStatus newStatus(
	int code, 
	IPath path, 
	String message, 
	Throwable exception) {

	Assert.isTrue(message != null);
	Assert.isTrue(message.trim().length() != 0);

	UIHackFinder.fixPR(); 	//1FTKIAP: ITPCORE:ALL - Status/MultiStatus API
	return new ResourceStatus(code, path, message, exception);
}
/**
 * This method must not be called outside the workbench.
 *
 * Utility method for creating status.
 *
 * This method will be removed in the future when kernel includes
 * support for creating status.
 * See 1FTKIAP: ITPCORE:ALL - Status/MultiStatus API
 *
 * Ultimately callers should be able to build their own multi status
 * and add to it rather than building a collection of status then converting it.
 * See 1FTQDWJ: ITPCORE:ALL - API - Status/MultiStatus - should able to add to a multi status
 */
public static IStatus newStatus(
		List vector, 
		IPath path, 
		String message, 
		Throwable exception) {

	UIHackFinder.fixPR(); 	//1FTKIAP: ITPCORE:ALL - Status/MultiStatus API
	
	List flatStatusCollection = new ArrayList();
	Iterator statusEnum = vector.iterator();
	while (statusEnum.hasNext()) {
		IStatus currentStatus = (IStatus)statusEnum.next();
		Iterator childrenEnum = flatten(currentStatus).iterator();
		while (childrenEnum.hasNext())
			flatStatusCollection.add(childrenEnum.next());
	}

	int flatStatusCollectionSize = flatStatusCollection.size();
	IStatus[] stati = new IStatus[flatStatusCollectionSize];
	for (int i = 0; i < flatStatusCollectionSize; i++)
		stati[i] = (IStatus)flatStatusCollection.get(i);

	return newStatus(stati, path, message, exception);
}
}
