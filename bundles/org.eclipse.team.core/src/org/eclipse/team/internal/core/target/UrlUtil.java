package org.eclipse.team.internal.core.target;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class UrlUtil {
	
	public static IPath getTrailingPath(URL fullURL, URL startingURL) {
		IPath fullPath = new Path(fullURL.getPath());
		IPath startingPath = new Path(startingURL.getPath());
		int matchingCount = fullPath.matchingFirstSegments(startingPath);
		return fullPath.removeFirstSegments(matchingCount);
	}
	
	public static URL concat(String root, IPath end) throws MalformedURLException {
		return new URL(concatString(root, end));
	}

	private static String concatString(String root, IPath end) {
		if(end.isEmpty())
			return root;
		if(root.length() == 0)
			return end.toString();
			
		if(root.charAt(root.length() - 1) == Path.SEPARATOR)
			return root + end.toString();	//already has separator, just concat end
			
		return root + Path.SEPARATOR + end.toString();
	}
}
