/*******************************************************************************
 * (c) Copyright 2013 l33t labs LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     l33t labs LLC and others - initial contribution
 *******************************************************************************/

package org.eclipse.ui.images.renderer;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import com.jhlabs.image.ContrastFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.HSBAdjustFilter;

/**
 * <p>Mojo which renders SVG icons into PNG format.</p>
 *
 * @goal render-icons
 * @phase generate-resources
 */
public class RenderMojo extends AbstractMojo {

    /** Maven logger */
    Log log;

    /** Used for high res rendering support. */
    public static final String ECLIPSE_SVG_SCALE = "eclipse.svg.scale";

    /** Used to specify the number of render threads when rasterizing icons. */
    public static final String RENDERTHREADS = "eclipse.svg.renderthreads";

    /**
     * <p>IconEntry is used to define an icon to rasterize,
     * where to put it and the dimensions to render it at.</p>
     */
    class IconEntry {

        /** The name of the icon minus extension */
        String nameBase;

        /** The input path of the source svg files. */
        File inputPath;

        /**
         * The path rasterized versions of this icon should be written into.
         */
        File outputPath;

        /** The path to a disabled version of the icon (gets desaturated). */
        private File disabledPath;

        /**
         * Creates an IconEntry used for record keeping when
         * rendering a set of SVG icons.
         *
         * @param nameBase the name of the icon file, minus any extension
         * @param inputPath the SVG file that is rendered
         * @param outputPath the path to the rendered icon data
         * @param disabledPath the part to the disabled version of the output icon
         */
        public IconEntry(String nameBase, File inputPath, File outputPath,
                File disabledPath) {
            this.nameBase = nameBase;
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.disabledPath = disabledPath;
        }
    }

    /** A list of directories with svg sources to rasterize. */
    private List<IconEntry> icons;

    /** The pool used to render multiple icons concurrently. */
    private ExecutorService execPool;

    /** The number of threads to use when rendering icons. */
    private int threads;

    /**
     * A counter used to keep track of the number of rendered icons. Atomic is
     * used to make it easy to access between threads concurrently.
     */
    private AtomicInteger counter;

    /** List of icons that failed to render, made safe for parallel access */
    List<IconEntry> failedIcons = Collections
            .synchronizedList(new ArrayList<IconEntry>(5));

    /** The amount of scaling to apply to rasterized images. */
    private int outputScale;

    /** Used for creating desaturated icons */
    private GrayscaleFilter grayFilter;

    /** Used for creating desaturated icons */
    private HSBAdjustFilter desaturator;

    /** Reduces contrast for disabled icons. */
    private ContrastFilter decontrast;

    /**
     * @return the number of icons rendered at the time of the call
     */
    public int getIconsRendered() {
        return counter.get();
    }

    /**
     * @return the number of icons that failed during the rendering process
     */
    public int getFailedIcons() {
        return failedIcons.size();
    }

    /**
     * <p>Creates an IconEntry during the icon gather operation.</p>
     *
     * @param input the source of the icon file (SVG document)
     * @param outputPath the path of the rasterized version to generate
     * @param disabledPath the path of the disabled (desaturated) icon, if one is required
     *
     * @return an IconEntry describing the rendering operation
     */
    public IconEntry createIcon(File input, File outputPath, File disabledPath) {
        String name = input.getName();
        String[] split = name.split("\\.(?=[^\\.]+$)");

        IconEntry def = new IconEntry(split[0], input, outputPath, disabledPath);

        return def;
    }

    /**
     * <p>Generates raster images from the input SVG vector image.</p>
     *
     * @param icon
     *            the icon to render
     */
    public void rasterize(IconEntry icon) {
        if (icon == null) {
            log.error("Null icon definition, skipping.");
            failedIcons.add(icon);
            return;
        }

        if (icon.inputPath == null) {
            log.error("Null icon input path, skipping: "
                    + icon.nameBase);
            failedIcons.add(icon);
            return;
        }

        if (!icon.inputPath.exists()) {
            log.error("Input path specified does not exist, skipping: "
                            + icon.nameBase);
            failedIcons.add(icon);
            return;
        }

        if (icon.outputPath != null && !icon.outputPath.exists()) {
            icon.outputPath.mkdirs();
        }

        if (icon.disabledPath != null && !icon.disabledPath.exists()) {
            icon.disabledPath.mkdirs();
        }

        // Create the document to rasterize
        SVGDocument svgDocument = generateSVGDocument(icon);

        if(svgDocument == null) {
            return;
        }

        // Determine the output sizes (native, double, quad)
        // We render at quad size and resample down for output
        Element svgDocumentNode = svgDocument.getDocumentElement();
        String nativeWidthStr = svgDocumentNode.getAttribute("width");
        String nativeHeightStr = svgDocumentNode.getAttribute("height");

        int nativeWidth = Integer.parseInt(nativeWidthStr);
        int nativeHeight = Integer.parseInt(nativeHeightStr);

        int outputWidth = nativeWidth * outputScale;
        int outputHeight = nativeHeight * outputScale;

        // Guesstimate the PNG size in memory, BAOS will enlarge if necessary.
        int outputInitSize = nativeWidth * nativeHeight * 4 + 1024;
        ByteArrayOutputStream iconOutput = new ByteArrayOutputStream(
                outputInitSize);

        // Render to SVG
        try {
            log.info(Thread.currentThread().getName() + " "
                    + " Rasterizing: " + icon.nameBase + ".png at " + outputWidth
                    + "x" + outputHeight);

            TranscoderInput svgInput = new TranscoderInput(svgDocument);

            boolean success = renderIcon(icon.nameBase, outputWidth, outputHeight, svgInput, iconOutput);

            if (!success) {
                log.error("Failed to render icon: " + icon.nameBase + ".png, skipping.");
                failedIcons.add(icon);
                return;
            }
        } catch (Exception e) {
            log.error("Failed to render icon: " + e.getMessage());
            failedIcons.add(icon);
            return;
        }

        // Generate a buffered image from Batik's png output
        byte[] imageBytes = iconOutput.toByteArray();
        ByteArrayInputStream imageInputStream = new ByteArrayInputStream(imageBytes);

        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(imageInputStream);

            if(inputImage == null) {
                log.error("Failed to generate BufferedImage from rendered icon, ImageIO returned null: " + icon.nameBase);
                failedIcons.add(icon);
                return;
            }
        } catch (IOException e2) {
            log.error("Failed to generate BufferedImage from rendered icon: "  + icon.nameBase + " - " + e2.getMessage());
            failedIcons.add(icon);
            return;
        }

        writeIcon(icon, outputWidth, outputHeight, inputImage);
    }

    /**
     * <p>Generates a Batik SVGDocument for the supplied IconEntry's input
     * file.</p>
     *
     * @param icon the icon entry to generate an SVG document for
     *
     * @return a batik SVGDocument instance or null if one could not be generated
     */
    private SVGDocument generateSVGDocument(IconEntry icon) {
        // Load the document and find out the native height/width
        // We reuse the document later for rasterization
        SVGDocument svgDocument = null;
        try {
            FileInputStream iconDocumentStream = new FileInputStream(icon.inputPath);

            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

            // What kind of URI is batik expecting here??? the docs don't say
            svgDocument = f.createSVGDocument("file://" + icon.nameBase + ".svg", iconDocumentStream);
        } catch (Exception e3) {
            log.error("Error parsing SVG icon document: " + e3.getMessage());
            failedIcons.add(icon);
            return null;
        }
        return svgDocument;
    }

    /**
     * <p>Resizes the supplied inputImage to the specified width and height, using
     * lanczos resampling techniques.</p>
     *
     * @param icon the icon that's being resized
     * @param width the desired output width after rescaling operations
     * @param height the desired output height after rescaling operations
     * @param sourceImage the source image to resource
     */
    private void writeIcon(IconEntry icon, int width, int height, BufferedImage sourceImage) {
        try {
            ImageIO.write(sourceImage, "PNG", new File(icon.outputPath, icon.nameBase + ".png"));

            if (icon.disabledPath != null) {
                BufferedImage desaturated16 = desaturator.filter(
                        grayFilter.filter(sourceImage, null), null);

                BufferedImage deconstrast = decontrast.filter(desaturated16, null);

                ImageIO.write(deconstrast, "PNG", new File(icon.disabledPath, icon.nameBase + ".png"));
            }
        } catch (Exception e1) {
            log.error("Failed to resize rendered icon to output size: "  +
                               icon.nameBase + " - " + e1.getMessage());
            failedIcons.add(icon);
        }
    }

    /**
     * <p>Handles concurrently rasterizing the icons to
     * reduce the duration on multicore systems.</p>
     */
    public void rasterizeAll() {
        // The number of icons that haven't been distributed to
        // callables
        int remainingIcons = icons.size();

        // The number of icons to distribute to a rendering callable
        final int threadExecSize = icons.size() / this.threads;

        // The current offset to start a batch, as they're distributed
        // between rendering callables
        int batchOffset = 0;

        // A list of callables used to render icons on multiple threads
        // Each callable gets a set of icons to render
        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(
                this.threads);

        // Distribute the rasterization operations between multiple threads
        while (remainingIcons > 0) {
            // The current start index for the current batch
            final int batchStart = batchOffset;

            // Increment the offset to reflect this batch (used for the next batch)
            batchOffset += threadExecSize;

            // Determine this batch size, used for batches that have less than
            // threadExecSize at the end of the distribution operation
            int batchSize = 0;

            // Determine if we can fit a full batch in this callable
            // or if we are at the end of gathered icons
            if (remainingIcons > threadExecSize) {
                batchSize = threadExecSize;
            } else {
                // We have less than a full batch worth of remaining icons
                // just add them all
                batchSize = remainingIcons;
            }

            // Deincrement the remaining Icons
            remainingIcons -= threadExecSize;

            // Used for access in the callable's scope
            final int execCount = batchSize;

            // Create the callable and add it to the task pool
            Callable<Object> runnable = new Callable<Object>() {
                public Object call() throws Exception {
                    // Rasterize this batch
                    for (int count = 0; count < execCount; count++) {
                        rasterize(icons.get(batchStart + count));
                    }

                    // Update the render counter
                    counter.getAndAdd(execCount);
                    log.info("Finished rendering batch, index: " + batchStart);

                    return null;
                }
            };

            tasks.add(runnable);
        }

        // Execute the rasterization operations that
        // have been added to the pool
        try {
            execPool.invokeAll(tasks);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Print info about failed render operations, so they can be fixed
        log.info("Failed Icon Count: " + failedIcons.size());
        for (IconEntry icon : failedIcons) {
            log.info("Failed Icon: " + icon.nameBase);
        }

    }

    /**
     * Use batik to rasterize the input SVG into a raster image at the specified
     * image dimensions.
     *
     * @param width the width to render the icons at
     * @param height the height to render the icon at
     * @param input the SVG transcoder input
     * @param stream the stream to write the PNG data to
     */
    public boolean renderIcon(final String iconName, int width, int height,
            TranscoderInput tinput, OutputStream stream) {
        PNGTranscoder transcoder = new PNGTranscoder() {
            protected ImageRenderer createRenderer() {
                ImageRenderer renderer = super.createRenderer();

                RenderingHints renderHints = renderer.getRenderingHints();

                renderHints.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));

                renderHints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY));

                renderHints.add(new RenderingHints(RenderingHints.KEY_DITHERING,
                    RenderingHints.VALUE_DITHER_DISABLE));

                renderHints.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC));

                renderHints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));

                renderHints.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON));

                renderHints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
                    RenderingHints.VALUE_COLOR_RENDER_QUALITY));

                renderHints.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE));

                renderHints.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON));

                renderer.setRenderingHints(renderHints);

                return renderer;
            }
        };

        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(width));
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(height));

        transcoder.setErrorHandler(new ErrorHandler() {
            public void warning(TranscoderException arg0)
                    throws TranscoderException {
                log.error("Icon: " + iconName + " - WARN: " + arg0.getMessage());
            }

            public void fatalError(TranscoderException arg0)
                    throws TranscoderException {
                log.error("Icon: " + iconName + " - FATAL: " + arg0.getMessage());
            }

            public void error(TranscoderException arg0)
                    throws TranscoderException {
                log.error("Icon: " + iconName + " - ERROR: " + arg0.getMessage());
            }
        });

        // Transcode the SVG document input to a PNG via the output stream
        TranscoderOutput output = new TranscoderOutput(stream);

        try {
            transcoder.transcode(tinput, output);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Search the root resources directory for svg icons and add them to our
     * collection for rasterization later.</p>
     *
     * @param outputName
     * @param iconDir
     * @param outputBase
     * @param outputDir2
     */
    public void gatherIcons(String outputName, File rootDir, File iconDir,
            File outputBase) {

        File[] listFiles = iconDir.listFiles();

        for (File child : listFiles) {
            if (child.isDirectory()) {
                gatherIcons(outputName, rootDir, child, outputBase);
                continue;
            }

            if (!child.getName().endsWith("svg")) {
                return;
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
            if (parentFile != null) {
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
     * <p>Initializes rasterizer defaults</p>
     *
     * @param threads the number of threads to render with
     * @param scale multiplier to use with icon output dimensions
     */
    private void init(int threads, int scale) {
        this.threads = threads;
        this.outputScale = Math.max(1, scale);
        icons = new ArrayList<IconEntry>();
        execPool = Executors.newFixedThreadPool(threads);
        counter = new AtomicInteger();

        grayFilter = new GrayscaleFilter();

        desaturator = new HSBAdjustFilter();
        desaturator.setSFactor(0.0f);

        decontrast = new ContrastFilter();
             decontrast.setBrightness(2.9f);
             decontrast.setContrast(0.2f);
    }

    /**
     * @see AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        log = getLog();

        // Default to 2x the number of processor cores but allow override via jvm arg
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() * 2);
        String threadStr = System.getProperty(RENDERTHREADS);
        if (threadStr != null) {
            try {
                threads = Integer.parseInt(threadStr);
            } catch (Exception e) {
                e.printStackTrace();
                System.out
                        .println("Could not parse thread count, using default thread count");
            }
        }

        // if high res is enabled, the icons output size will be scaled by iconScale
        // Defaults to 1, meaning native size
        int iconScale = 1;
        String iconScaleStr = System.getProperty(ECLIPSE_SVG_SCALE);
        if(iconScaleStr != null) {
            iconScale = Integer.parseInt(iconScaleStr);
        }

        // Track the time it takes to render the entire set
        long totalStartTime = System.currentTimeMillis();

        // initialize defaults (the old renderer was instantiated via constructor)
        init(threads, iconScale);

        String workingDirectory = System.getProperty("user.dir");

        File outputDir = new File(workingDirectory+"/eclipse-png/");
        File iconDirectoryRoot = new File("eclipse-svg/");

        // Search each subdir in the root dir for svg icons
        for (File file : iconDirectoryRoot.listFiles()) {
            if(!file.isDirectory()) {
                continue;
            }

            String dirName = file.getName();

            // Where to place the rendered icon
            File outputBase = new File(outputDir, dirName);

            gatherIcons(dirName, file, file, outputBase);
        }

        log.info("Working directory: " + outputDir.getAbsolutePath());
        log.info("SVG Icon Directory: " + iconDirectoryRoot.getAbsolutePath());
        log.info("Rendering icons with " + threads + " threads, scaling output to " + iconScale + "x");
        long startTime = System.currentTimeMillis();

        // Render the icons
        rasterizeAll();

        // Print summary of operations
        int iconRendered = getIconsRendered();
        int failedIcons = getFailedIcons();
        int fullIconCount = iconRendered - failedIcons;

        log.info(fullIconCount + " Icons Rendered");
        log.info(failedIcons + " Icons Failed");
        log.info("Took: "    + (System.currentTimeMillis() - startTime) + " ms.");

        log.info("Rasterization operations completed, Took: "
                + (System.currentTimeMillis() - totalStartTime) + " ms.");
    }

}
