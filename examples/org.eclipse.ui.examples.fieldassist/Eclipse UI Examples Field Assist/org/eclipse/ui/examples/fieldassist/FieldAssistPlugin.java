package org.eclipse.ui.examples.fieldassist;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class FieldAssistPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static FieldAssistPlugin plugin;
	
	// Constants for the images and decorations
	static String DEC_ERROR = "org.eclipse.ui.examples.fieldassist.errorDecoration";
	static String DEC_REQUIRED = "org.eclipse.ui.examples.fieldassist.requiredDecoration";
	static String DEC_CONTENTASSIST = "org.eclipse.ui.examples.fieldassist.contentAssistDecoration";
	static String DEC_WARNING = "org.eclipse.ui.examples.fieldassist.warningDecoration";
	/**
	 * The constructor.
	 */
	public FieldAssistPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerFieldDecorations();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static FieldAssistPlugin getDefault() {
		return plugin;
	}
	
	/*
	 * Register the images and field decorations used by this application.
	 */
	private void registerFieldDecorations() {
		// First the images
		ImageRegistry registry = JFaceResources.getImageRegistry();
		registry.put(DEC_ERROR, ImageDescriptor.createFromFile(
						ExampleDialog.class, "images/error_cue.gif")); 
		registry.put(DEC_REQUIRED, ImageDescriptor.createFromFile(
				ExampleDialog.class, "images/required_field_cue.gif")); 
		registry.put(DEC_WARNING, ImageDescriptor.createFromFile(
				ExampleDialog.class, "images/warning_cue.gif")); 
		registry.put(DEC_CONTENTASSIST, ImageDescriptor.createFromFile(
				ExampleDialog.class, "images/content_proposal_cue.gif")); 

		// Now the decorations.  Only the first two since we need the key stroke
		// info to fully describe the content assist decoration.
		FieldDecorationRegistry decRegistry = FieldDecorationRegistry.getDefault();
		decRegistry.registerFieldDecoration(DEC_ERROR, TaskAssistExampleMessages.Decorator_Error, DEC_ERROR);
		decRegistry.registerFieldDecoration(DEC_REQUIRED, null, DEC_REQUIRED);
		decRegistry.registerFieldDecoration(DEC_WARNING, TaskAssistExampleMessages.Decorator_Warning, DEC_WARNING);
	}
}

