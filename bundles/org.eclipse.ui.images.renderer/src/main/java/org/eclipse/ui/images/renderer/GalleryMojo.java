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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

/**
 * <p>Mojo which renders galleries for comparing
 * and evaluating icons..</p>
 *
 * @goal render-galleries
 * @phase generate-resources
 */
public class GalleryMojo extends AbstractMojo {

	/** Maven logger */
    Log log;

    /** Used for finding gif files by extension. */
    public static final String GIF_EXT = ".gif";

	/**
	 * <p>Mojo takes rendered images and generates various galleries for
	 * testing and evaluation.</p>
	 */
    public void execute() throws MojoExecutionException, MojoFailureException {
        log = getLog();
        
        File iconDirectoryRoot = new File("eclipse-png/");

        Map<String, List<IconEntry>> galleryIconSets = new HashMap<String, List<IconEntry>>();
        
        // Search each subdir in the root dir for svg icons
        for (File file : iconDirectoryRoot.listFiles()) {
            if(!file.isDirectory()) {
                continue;
            }

            List<IconEntry> icons = new ArrayList<IconEntry>();
            IconGatherer.gatherIcons(icons, "png", file, file, iconDirectoryRoot, false);
            
            galleryIconSets.put(file.getName(), icons);
        }

        File mavenTargetDir = new File("target/");
        File galleryDir = new File(mavenTargetDir, "gallery/");
        File gifCompare = new File(galleryDir, "gifcompare/");
        File master = new File(galleryDir, "master/");
        
        if(galleryDir.exists()) {
        	galleryDir.delete();
        }

    	galleryDir.mkdirs();
    	gifCompare.mkdirs();
    	master.mkdirs();
    	
        renderGalleries(galleryDir, gifCompare, master, galleryIconSets, 16, 800);
    }
    
    /**
     * <p>Renders each icon set into a gallery image for reviewing and showing off
     * icons, and then composes them into a master gallery image.</p>
     * 
     * @param rasterizer
     * @param galleryDir
     * @param gifCompare 
     * @param master 
     * @param iconSize
     * @param width
     */
    public void renderGalleries(File galleryDir,  File gifCompare, File master, Map<String, List<IconEntry>> iconSets, int iconSize, int width) {
        // Render each icon set and a master list
        List<IconEntry> masterList = new ArrayList<IconEntry>();
        
        for (Entry<String, List<IconEntry>> entry : iconSets.entrySet()) {
            String key = entry.getKey();
            List<IconEntry> value = entry.getValue();

            masterList.addAll(value);

            log.info("Creating gallery for: " + key);
            renderGallery(galleryDir, key, value, iconSize, width, 3);
            renderGifCompareGallery(gifCompare, key, value, iconSize, width, 6);
        }

        // Render the master image
        log.info("Rendering master icon gallery...");
        renderMasterGallery(galleryDir, master, "-gallery.png", iconSize, iconSize + width, true);
        renderMasterGallery(galleryDir, master, "-gallery.png", iconSize, iconSize + width, false);
        
        // Master gif compare
        //renderMasterGallery(outputDir, "-gifcompare.png", iconSize, iconSize + width, false);
    }

	/**
     * <p>Renders comparison images, the new png/svg icons vs old gifs.</p>
     * 
     * @param outputDir
     * @param key
     * @param icons
     * @param iconSize
     * @param width
     * @param margin
     */
    private void renderGifCompareGallery(File outputDir, String key, List<IconEntry> icons, int iconSize, int width, int margin) {
    	int leftColumnWidth = 300;
    	int textHeaderHeight = 31;
        int outputSize = iconSize;
        int widthTotal = (outputSize * 4) + (margin * 6) + leftColumnWidth;
        
        int rowHeight = iconSize + (margin * 2);
        
        // Compute the height and add some room for the text header (31 px)
        int height = (icons.size() * rowHeight) + textHeaderHeight;

        BufferedImage bi = new BufferedImage(widthTotal + iconSize, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.GRAY);
        g.drawString("SVG Icon Set: " + key + " - Count: " + icons.size(), 8, 20);

        int x = leftColumnWidth;
        int y = textHeaderHeight;

        // Render
        ResampleOp resampleOp = new ResampleOp(outputSize, outputSize);
        resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
        // resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Oversharpened);
        resampleOp.setNumberOfThreads(Runtime.getRuntime()
                .availableProcessors());

        int second = leftColumnWidth + margin + iconSize;
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, widthTotal + 10, height);
        
        g.setColor(Color.DARK_GRAY);
        g.fillRect(second + (margin / 2) + iconSize + 20, 0, (margin * 2) + (iconSize * 2) + 10, height);

        g.drawString(key + " (GIF / PNG)", 15, 20);
        
        Collections.sort(icons);
        
        // Render each icon into the gallery grid
        for (IconEntry entry : icons) {

            if (entry.inputPath == null) {
                continue;
            }
            
            try {
                BufferedImage pngImage = ImageIO.read(entry.inputPath);

                // Munge the gif path
                File gifLocalPath = new File(entry.inputPath.getParentFile(), entry.nameBase + GIF_EXT);
                String absoluteLocalPath = gifLocalPath.getAbsolutePath();
                String gifAbsPath = absoluteLocalPath.replaceFirst("eclipse-png", "eclipse-gif");
                File gifPath = new File(gifAbsPath);

            	log.debug("Search for GIF...");
            	log.debug("Entry path: " + entry.inputPath.getAbsolutePath());
            	log.debug("GIF path: " + gifPath.getAbsolutePath());
            	
                BufferedImage gifImage = null;
                
                if(gifPath.exists()) {
                	gifImage = ImageIO.read(gifPath);
                } else {
                	log.debug("GIF not found: " + gifPath.getAbsolutePath());
                }
                
                g.drawString(entry.nameBase, 5, y + (margin * 3));

                g.drawLine(0, y, widthTotal, y);
                
                if(gifImage != null) {
                	g.drawImage(gifImage, leftColumnWidth, y + margin, null);
                }
                
                g.drawImage(pngImage, second, y + margin, null);
                
                if(gifImage != null) {
                	g.drawImage(gifImage, second + margin + iconSize + 30, y + margin, null);
                }
                
                g.drawImage(pngImage, second + (margin * 2) + (iconSize * 2) + 30, y + margin, null);

                y += iconSize + margin * 2;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error rendering icon for gallery: " + entry.inputPath.getAbsolutePath());
                continue;
            }
        }

        try {
            // Write the gallery image to disk
        	String outputName = key + "-" + iconSize + "-gifcompare.png";
            ImageIO.write(bi, "PNG", new File(outputDir, outputName));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error writing gif comparison gallery: " + e.getMessage());
        }
    }
    
    
    /**
     * <p>Renders an icon set into a grid within an image.</p>
     * 
     * @param outputRoot
     * @param key
     * @param icons
     */
    private void renderGallery(File outputRoot, String key, List<IconEntry> icons,
            int iconSize, int width, int margin) {
        int textHeaderHeight = 31;
        int outputSize = iconSize;
        int outputTotal = outputSize + (margin * 2);
        int div = width / outputTotal;
        int rowCount = icons.size() / div;
        
        if (width % outputTotal > 0) {
            rowCount++;
        }

        // Compute the height and add some room for the text header (31 px)
        int height = Math.max(outputTotal, rowCount * outputTotal)
                + textHeaderHeight;

        BufferedImage bi = new BufferedImage(width + iconSize, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.GRAY);
        g.drawString("SVG Icon Set: " + key + " - Count: " + icons.size(), 8, 20);

        int x = 1;
        int y = textHeaderHeight;

        // Render
        ResampleOp resampleOp = new ResampleOp(outputSize, outputSize);
        resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
        // resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Oversharpened);
        resampleOp.setNumberOfThreads(Runtime.getRuntime().availableProcessors());

        // Render each icon into the gallery grid
        for (IconEntry def : icons) {
            try {
                if (def.inputPath == null) {
                    log.error("Undefined gallery image for : " + def.nameBase);
                    continue;
                }
                
                BufferedImage iconImage = ImageIO.read(def.inputPath);
                BufferedImage sizedImage = resampleOp.filter(iconImage, null);

                g.drawImage(sizedImage, x + margin, y + margin, null);

                x += outputTotal;

                if (x >= width) {
                    x = 1;
                    y += outputTotal;
                }
            } catch (Exception e) {
                log.error("Error rendering icon for gallery: " + def.inputPath.getAbsolutePath());
                e.printStackTrace();
                continue;
            }
        }

        try {
            // Write the gallery image to disk
        	String outputName = key + "-" + iconSize + "-gallery.png";
            ImageIO.write(bi, "PNG", new File(outputRoot, outputName));
        } catch (IOException e) {
            log.error("Error writing icon: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * <p>Renders a master gallery image that contains every icon set at the
     * current resolution.</p>
     * 
     * @param root
     * @param iconSize
     * @param width
     * @param dark
     */
    private void renderMasterGallery(File root, File output, String fileEnding, int iconSize, int width,
            boolean dark) {
        int headerHeight = 30;
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (File file : root.listFiles()) {
            if (file.getName().endsWith(iconSize + fileEnding)) {
                BufferedImage set = null;
                try {
                    set = ImageIO.read(file);
                } catch (IOException e) {
                    log.error("Error reading icon: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
                images.add(set);
                headerHeight += set.getHeight();
            }
        }

        BufferedImage bi = new BufferedImage(width, headerHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (dark) {
            g.setColor(Color.DARK_GRAY);
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        g.setColor(Color.BLACK);
        g.drawString("SVG Icons for Eclipse - Count: "
                + iconSize + "x" + iconSize
                + " Rendered: " + new Date().toString(), 8, 20);

        int x = 0;
        int y = 31;

        // Draw each icon set image into the uber gallery
        for (BufferedImage image : images) {
            g.drawImage(image, x, y, null);
            y += image.getHeight();
        }
 
        try {
            // Write the uber gallery to disk
            String bgState = (dark) ? "dark" : "light";
            String outputName = "global-svg-" + iconSize + "-" + bgState + fileEnding + "-icons.png";
            ImageIO.write(bi, "PNG", new File(output, outputName));
        } catch (IOException e) {
            log.error("Error writing gallery: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
