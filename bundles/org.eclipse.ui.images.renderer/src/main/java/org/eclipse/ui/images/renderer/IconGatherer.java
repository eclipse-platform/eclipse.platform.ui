/*******************************************************************************
 * (c) Copyright 2015 l33t labs LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     l33t labs LLC and others - initial contribution
 *******************************************************************************/

package org.eclipse.ui.images.renderer;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * <p>Utility to find and organize icons for rendering.</p>
 * 
 * @author tmccrary@l33tlabs.com
 *
 */
public class IconGatherer {

    /**
     * <p>Searches the root resources directory for svg icons and adds them to a
     * collection for later rasterization.</p>	
     *
     * @param outputName
     * @param iconDir
     * @param outputBase
     * @param outputDir2
     */ 
    public static void gatherIcons(List<IconEntry> icons, String extension, File rootDir, File iconDir, File outputBase, boolean generateDisabledDirs) {
        File[] listFiles = iconDir.listFiles();

        for (File child : listFiles) {
            if (child.isDirectory()) {
            	if(child.getName().startsWith("d")) {
            		continue;
            	}
            	
                gatherIcons(icons, extension, rootDir, child, outputBase, generateDisabledDirs);
                continue;
            }

            if (!child.getName().endsWith(extension)) {
                continue;
            }

            // Compute a relative path for the output dir
            URI rootUri = rootDir.toURI();
            URI iconUri = iconDir.toURI();

            String relativePath = rootUri.relativize(iconUri).getPath();
            File outputDir = new File(outputBase, relativePath);
            File disabledOutputDir = null;

            File parentFile = child.getParentFile();

            /* Determine if/where to put a disabled version of the icon
               Eclipse traditionally uses a prefix of d for disabled, e for
               enabled in the folder name */
            if (generateDisabledDirs && parentFile != null) {
                String parentDirName = parentFile.getName();
                if (parentDirName.startsWith("e")) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("d");
                    builder.append(parentDirName.substring(1, parentDirName.length()));

                    // Disabled variant folder name
                    String disabledVariant = builder.toString();

                    // The parent's parent, to create the disabled directory in
                    File setParent = parentFile.getParentFile();

                    // The source directory's disabled folder
                    File disabledSource = new File(setParent, disabledVariant);

                    // Compute a relative path, so we can create the output folder
                    String path = rootUri.relativize(
                              disabledSource.toURI()).getPath();

                    // Create the output folder, so a disabled icon is generated
                    disabledOutputDir = new File(outputBase, path);
                    if(!disabledOutputDir.exists()) {
                        disabledOutputDir.mkdirs();
                    }
                }
            }

            IconEntry icon = createIcon(child, outputDir, disabledOutputDir);

            icons.add(icon);
        }
    }
    
    /**
     * <p>Creates an IconEntry, which contains information about rendering an icon such
     * as the source file, where to render, what alternative types of output to
     * generate, etc.</p>
     *
     * @param input the source of the icon file (SVG document)
     * @param outputPath the path of the rasterized version to generate
     * @param disabledPath the path of the disabled (desaturated) icon, if one is required
     *
     * @return an IconEntry describing the rendering operation
     */
    public static IconEntry createIcon(File input, File outputPath, File disabledPath) {
        String name = input.getName();
        String[] split = name.split("\\.(?=[^\\.]+$)");

        IconEntry def = new IconEntry(split[0], input, outputPath, disabledPath, new int[0]);

        return def;
    }

}
