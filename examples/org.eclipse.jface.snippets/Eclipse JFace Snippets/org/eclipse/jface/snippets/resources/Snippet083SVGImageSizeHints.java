/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.snippets.resources;

import java.net.URI;
import java.net.URL;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A snippet to demonstrate SVG image size hints using path-based and
 * query-parameter-based size detection.
 *
 * <p>
 * This demonstrates two ways to control SVG rendering size:
 * </p>
 * <ol>
 * <li><b>Path-based hints:</b> Place SVG in folders like /icons/16x16/ or
 * /icons/32x32/</li>
 * <li><b>Query parameter hints:</b> Add ?size=WIDTHxHEIGHT to the URL</li>
 * </ol>
 *
 * <p>
 * This allows using a single SVG file at different sizes without creating
 * multiple scaled versions or restricting the SVG design size.
 * </p>
 */
public class Snippet083SVGImageSizeHints {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("SVG Image Size Hints Demo");
		GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(shell);

		CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).hint(700, 500).applyTo(tabFolder);

		createOverviewTab(tabFolder);
		createVisualDemoTab(tabFolder, display);
		createCodeExamplesTab(tabFolder);
		createUseCasesTab(tabFolder);

		tabFolder.setSelection(0);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private static void createOverviewTab(CTabFolder folder) {
		CTabItem tab = new CTabItem(folder, SWT.NONE);
		tab.setText("Overview");

		Composite content = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(content);
		tab.setControl(content);

		Text text = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);

		text.setText("""
				SVG Image Size Hints
				====================

				JFace now supports automatic size detection for SVG images through two mechanisms:

				1. PATH-BASED SIZE HINTS
				   SVGs placed in folders with size patterns (e.g., /icons/16x16/, /icons/32x32/)
				   are automatically rendered at that size.

				   Example: bundle://plugin.id/icons/16x16/icon.svg → renders at 16x16 pixels

				2. QUERY PARAMETER SIZE HINTS
				   You can specify size using query parameters for maximum flexibility.
				   This allows using the same SVG at different sizes.

				   Example: bundle://plugin.id/icons/icon.svg?size=16x16
				   Example: bundle://plugin.id/icons/icon.svg?size=128x128

				HIGH-DPI SUPPORT
				Both methods work with high-DPI displays. The hint specifies the base size,
				and JFace automatically scales for display zoom levels:

				   16x16 at 100% zoom → 16x16 pixels
				   16x16 at 150% zoom → 24x24 pixels
				   16x16 at 200% zoom → 32x32 pixels

				PRECEDENCE
				Query parameters take precedence over path-based hints, so you can override
				the path-based size if needed.
				""");
	}

	private static void createVisualDemoTab(CTabFolder folder, Display display) {
		CTabItem tab = new CTabItem(folder, SWT.NONE);
		tab.setText("Visual Demo");

		Composite content = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(10, 10).spacing(10, 10).numColumns(3).applyTo(content);
		tab.setControl(content);

		// Try to load demo SVG
		URL svgUrl = Snippet083SVGImageSizeHints.class.getResource("demo-icon.svg");

		if (svgUrl == null) {
			Label error = new Label(content, SWT.WRAP);
			error.setText("ERROR: Could not find demo-icon.svg resource.\n\n"
					+ "Expected location: /org/eclipse/jface/snippets/resources/demo-icon.svg\n\n"
					+ "Please ensure the SVG file is present in the resources folder.");
			error.setForeground(display.getSystemColor(SWT.COLOR_RED));
			GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(error);
			return;
		}

		Label info = new Label(content, SWT.WRAP);
		info.setText("The same SVG file rendered at different sizes using query parameter hints:");
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(info);

		// Demo various sizes
		addImageDemo(content, "16x16 pixels", svgUrl, "?size=16x16", display);
		addImageDemo(content, "32x32 pixels", svgUrl, "?size=32x32", display);
		addImageDemo(content, "48x48 pixels", svgUrl, "?size=48x48", display);
		addImageDemo(content, "64x64 pixels", svgUrl, "?size=64x64", display);
		addImageDemo(content, "96x96 pixels", svgUrl, "?size=96x96", display);
		addImageDemo(content, "128x128 pixels (native)", svgUrl, "", display);

		Label note = new Label(content, SWT.WRAP);
		note.setText("\nNote: All images above are from the same SVG file. "
				+ "The size hints control the rendering size without modifying the source file.");
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(note);
	}

	private static void createCodeExamplesTab(CTabFolder folder) {
		CTabItem tab = new CTabItem(folder, SWT.NONE);
		tab.setText("Code Examples");

		Composite content = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(content);
		tab.setControl(content);

		Text text = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);
		text.setFont(org.eclipse.jface.resource.JFaceResources.getTextFont());

		text.setText("""
				// EXAMPLE 1: Using query parameter size hints
				URL iconUrl = URI.create("bundle://my.plugin/icons/search.svg?size=16x16").toURL();
				ImageDescriptor desc = ImageDescriptor.createFromURL(iconUrl);
				Image icon = desc.createImage();

				// EXAMPLE 2: Using path-based size hints
				URL iconUrl = FileLocator.find(bundle, new Path("icons/16x16/search.svg"));
				ImageDescriptor desc = ImageDescriptor.createFromURL(iconUrl);
				Image icon = desc.createImage();

				// EXAMPLE 3: Dynamic sizing at runtime
				String baseUrl = "bundle://my.plugin/icons/icon.svg";
				String sizeParam = "?size=" + desiredWidth + "x" + desiredHeight;
				URL iconUrl = URI.create(baseUrl + sizeParam).toURL();
				ImageDescriptor desc = ImageDescriptor.createFromURL(iconUrl);

				// EXAMPLE 4: Using with ImageDescriptor registry
				ImageDescriptor desc = ImageDescriptor.createFromURL(
				    URI.create("bundle://my.plugin/icons/toolbar.svg?size=16x16").toURL()
				);
				JFaceResources.getImageRegistry().put("toolbar.icon", desc);

				// EXAMPLE 5: File URLs with query parameters
				File svgFile = new File("/path/to/icon.svg");
				URL fileUrl = svgFile.toURI().toURL();
				String urlWithSize = fileUrl.toExternalForm() + "?size=32x32";
				URL sizedUrl = URI.create(urlWithSize).toURL();
				ImageDescriptor desc = ImageDescriptor.createFromURL(sizedUrl);

				// EXAMPLE 6: Multiple query parameters
				URL url = URI.create("bundle://my.plugin/icons/icon.svg?theme=dark&size=24x24").toURL();
				// Note: Only 'size' parameter is used for size hints

				// IMPORTANT: Use URI.create().toURL() instead of new URL(String)
				// The URL(String) constructor is deprecated in Java 21
				""");
	}

	private static void createUseCasesTab(CTabFolder folder) {
		CTabItem tab = new CTabItem(folder, SWT.NONE);
		tab.setText("Use Cases");

		Composite content = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(content);
		tab.setControl(content);

		Text text = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);

		text.setText("""
				Use Cases for SVG Size Hints
				=============================

				1. TOOLBAR ICONS
				   Use ?size=16x16 for consistent toolbar icon sizing across different themes
				   and plugins, without requiring each plugin to scale their SVGs.

				   Example: Command contributions can use a single SVG at toolbar size.

				2. WIZARD IMAGES
				   Use ?size=128x128 or larger for wizard header graphics, while using the
				   same SVG at smaller sizes in tree views or preference pages.

				   Example: One brand SVG for all sizes in a feature.

				3. VIEW ICONS
				   Place SVGs in /icons/16x16/ folder structure for automatic sizing in
				   Eclipse view tabs, tree items, and list items.

				   Example: Package Explorer, Project Explorer use consistent 16x16 icons.

				4. MULTI-RESOLUTION SUPPORT
				   One SVG file serves all sizes without duplication. No need to maintain
				   separate PNG files for 16x16, 32x32, 48x48, etc.

				   Example: Plugin can ship one SVG instead of 5+ PNG files.

				5. DYNAMIC UI SCALING
				   Support user preferences for icon sizes or accessibility features by
				   dynamically adjusting query parameters at runtime.

				   Example: Large icon mode in toolbars for accessibility.

				6. THEME VARIATIONS
				   Combine with path-based loading to support light/dark themes while
				   controlling size independently.

				   Example: icons/dark/16x16/icon.svg and ?size= parameter

				7. RESPONSIVE LAYOUTS
				   Adjust icon sizes based on available space or zoom level without
				   creating multiple image files.

				   Example: Sidebar icons that scale with panel width.

				8. RETINA/HIGH-DPI DISPLAYS
				   Automatic scaling to 150% and 200% zoom ensures crisp rendering on
				   high-DPI displays without manual intervention.

				   Example: MacBook Retina displays automatically get 2x scaled icons.
				""");
	}

	private static void addImageDemo(Composite parent, String description, URL baseUrl, String queryParam,
			Display display) {
		Composite demoComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(demoComposite);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(demoComposite);

		try {
			String urlString = baseUrl.toExternalForm() + queryParam;
			URL url = URI.create(urlString).toURL();
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
			Image image = descriptor.createImage(display);

			if (image == null) {
				createErrorLabel(demoComposite, description, "Failed to create image from descriptor");
				return;
			}

			Canvas canvas = new Canvas(demoComposite, SWT.BORDER);
			int width = image.getBounds().width;
			int height = image.getBounds().height;
			GridDataFactory.swtDefaults().hint(width + 4, height + 4).align(SWT.CENTER, SWT.CENTER).applyTo(canvas);

			canvas.addPaintListener(e -> {
				if (!image.isDisposed()) {
					e.gc.drawImage(image, 2, 2);
				}
			});

			Label label = new Label(demoComposite, SWT.CENTER);
			label.setText(description);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);

			Label sizeLabel = new Label(demoComposite, SWT.CENTER);
			sizeLabel.setText("(" + width + "×" + height + ")");
			sizeLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(sizeLabel);

			canvas.addDisposeListener(e -> {
				if (!image.isDisposed()) {
					image.dispose();
				}
			});
		} catch (Exception e) {
			createErrorLabel(demoComposite, description,
					"Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private static void createErrorLabel(Composite parent, String description, String errorMsg) {
		Label error = new Label(parent, SWT.WRAP);
		error.setText(description + "\n" + errorMsg);
		error.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).applyTo(error);
	}
}
