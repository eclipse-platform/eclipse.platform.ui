package org.eclipse.e4.ui.examples.css.rcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	
	private static final String PERSPECTIVE_ID = "org.eclipse.e4.ui.examples.css.rcp.perspective";
	private final static String STYLE_SHEET_PATH = "styles/stylesheet.css";
	public static ApplicationWorkbenchAdvisor INSTANCE;

	public CSSEngine engine;
	
	public ApplicationWorkbenchAdvisor() {
		super();
		INSTANCE = this;
	}
	
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    	return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	} 
	
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		engine = initializeStyling();
	}
	
	private CSSEngine initializeStyling() {
		// Instantiate SWT CSS Engine
		CSSEngine engine = new CSSSWTEngineImpl(Display.getDefault(), true);
		engine.setErrorHandler(new CSSErrorHandler() {
			public void error(Exception e) {
				e.printStackTrace();
			}
		});
		
		try {
			URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(STYLE_SHEET_PATH), null);
			InputStream stream = url.openStream();
			engine.parseStyleSheet(stream);	
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return engine;
	}
}
