package org.eclipse.core.resources.ant;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
/**
 * Subclasses FileUtils from Ant to extend its funtionalities.
 */
public class EclipseFileUtils extends FileUtils {

public void copyFile(File sourceFile, File destFile, FilterSetCollection filters, boolean overwrite, boolean preserveLastModified) throws IOException {
	super.copyFile(sourceFile, destFile, filters, overwrite, preserveLastModified);
	CoreFileSystemLibrary.copyAttributes(sourceFile.getAbsolutePath(), destFile.getAbsolutePath(), preserveLastModified);
}
}