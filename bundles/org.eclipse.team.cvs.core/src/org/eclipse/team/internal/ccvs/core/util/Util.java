package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.*;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * Unsorted static helper-methods 
 */
public class Util {
	/**
	 * Get the extention of the path of resource
	 * relative to the path of root
	 * 
	 * @throws CVSException if root is not a root-folder of resource
	 */
	public static String getRelativePath(String rootName, String resourceName) 
		throws CVSException {

		if (!resourceName.startsWith(rootName) || rootName.length() > resourceName.length()) {
			throw new CVSException(Policy.bind("Util.Internal_error,_resource_does_not_start_with_root_3")); //$NON-NLS-1$
		}
		
		// Otherwise we would get an ArrayOutOfBoundException
		// in case of two equal Resources
		if (rootName.length() == resourceName.length()) {
			return ""; //$NON-NLS-1$
		}
		
		// Remove leading slash if there is one
		String result = resourceName.substring(rootName.length()).replace('\\', '/');
		if (result.startsWith("/")) { //$NON-NLS-1$
			result = result.substring(1);
		}
		return result;
	}
	
	/**
	 * Append the prefix and suffix to form a valid CVS path.
	 */
	public static String appendPath(String prefix, String suffix) {
		if (prefix.endsWith(Session.SERVER_SEPARATOR)) {
			if (suffix.startsWith(Session.SERVER_SEPARATOR))
				return prefix + suffix.substring(1);
			else
				return prefix + suffix;
		} else if (suffix.startsWith(Session.SERVER_SEPARATOR))
			return prefix + suffix;
		else
			return prefix + Session.SERVER_SEPARATOR + suffix;
	}

	public static void logError(String message, Throwable throwable) {
		CVSProviderPlugin.log(new Status(IStatus.ERROR, CVSProviderPlugin.ID, IStatus.ERROR, message, throwable));
	}

	/**
	 * If the number of segments in the relative path of <code>resource</code> to <code>root</code> is 
	 * greater than <code>split</code> then the returned path is truncated to <code>split</code> number
	 * of segments and '...' is shown as the first segment of the path.
	 */
	public static String toTruncatedPath(ICVSResource resource, ICVSFolder root, int split) {
		try {
			IPath path = new Path(resource.getRelativePath(root));
			int segments = path.segmentCount();
			if(segments>split) {				
				IPath last = path.removeFirstSegments(segments - split);
				return "..." + path.SEPARATOR + last.toString(); //$NON-NLS-1$
			}
			return path.toString();
		} catch(CVSException e) {
			return resource.getName();
		}
	}
}