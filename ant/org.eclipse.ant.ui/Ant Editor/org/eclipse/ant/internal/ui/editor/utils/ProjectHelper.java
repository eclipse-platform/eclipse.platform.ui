/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * Portions Copyright  2000-2004 The Apache Software Foundation
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Apache Software License v2.0 which 
 * accompanies this distribution and is available at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Contributors:
 *     IBM Corporation - derived implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.utils;
  
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.jface.text.BadLocationException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Derived from the original Ant ProjectHelper2 with help from the JAXPUtils class.
 * This class provides parsing for using a String as a source and provides
 * handlers that will continue parsing to completion upon hitting errors.
 */
public class ProjectHelper extends ProjectHelper2 {

	/**
	 * helper for path -> URI and URI -> path conversions.
	 */
	private static FileUtils fu= null;
	
	/**
	 * The buildfile that is to be parsed. Must be set if parsing is to
	 * be successful.
	 */
	private File buildFile= null;
	
	private static String currentEntityName= null;
	private static String currentEntityPath= null;
	
	private static int currentImportStackSize= 1;
	
	/**
	 * The Ant Model
	 */
	private static AntModel fgAntModel;

	/**
	 * The current Ant parsing context
	 */
	private static AntXMLContext fgAntContext;
	
	private static AntHandler elementHandler= new ElementHandler();
	private static AntHandler projectHandler= new ProjectHandler();
	private static AntHandler targetHandler= new TargetHandler();
	private static AntHandler mainHandler= new MainHandler();
	private static LexicalHandler lexicalHandler= new LexHandler();
	
	public static class ElementHandler extends ProjectHelper2.ElementHandler {
		
		private UnknownElement task= null;
		private Task currentTask= null;
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String tag, String qname, Attributes attrs, AntXMLContext context) {
			return ProjectHelper.elementHandler;
		}
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void onStartElement(String uri, String tag, String qname, Attributes attrs, AntXMLContext context) {
			try {
				RuntimeConfigurable wrapper= context.currentWrapper();
				currentTask= null;
				task= null;
				if (wrapper != null) {
					currentTask= (Task)wrapper.getProxy();
				}
				onStartElement0(uri, tag, qname, attrs, context);
				
				Locator locator= context.getLocator();
				getAntModel().addTask(task, currentTask, attrs, locator.getLineNumber(), locator.getColumnNumber());
				
			} catch (BuildException be) {
				Locator locator= context.getLocator();
				getAntModel().addTask(task, currentTask, attrs, locator.getLineNumber(), locator.getColumnNumber());
				getAntModel().error(be);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onEndElement(java.lang.String, java.lang.String, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void onEndElement(String uri, String tag, AntXMLContext context) {
			super.onEndElement(uri, tag, context);
			
			Locator locator= context.getLocator();
			getAntModel().setCurrentElementLength(locator.getLineNumber(), locator.getColumnNumber());
		}
		
		private void onStartElement0(String uri, String tag, String qname, Attributes attrs, AntXMLContext context) {
			
			RuntimeConfigurable parentWrapper = context.currentWrapper();
            Object parent = null;

            if (parentWrapper != null) {
                parent = parentWrapper.getProxy();
            }

            /* UnknownElement is used for tasks and data types - with
               delayed eval */
            task = new UnknownElement(tag);
            task.setProject(context.getProject());
            task.setNamespace(uri);
            task.setQName(qname);
            task.setTaskType(org.apache.tools.ant.ProjectHelper.genComponentName(task.getNamespace(), tag));
            task.setTaskName(qname);

            Location location = new Location(context.getLocator().getSystemId(),
                    context.getLocator().getLineNumber(),
                    context.getLocator().getColumnNumber());
            task.setLocation(location);
            task.setOwningTarget(context.getCurrentTarget());

            context.configureId(task, attrs);

            if (parent != null) {
                // Nested element
                ((UnknownElement) parent).addChild(task);
            }  else {
                // Task included in a target ( including the default one ).
                context.getCurrentTarget().addTask(task);
            }

            // container.addTask(task);
            // This is a nop in UE: task.init();

            RuntimeConfigurable wrapper
                = new RuntimeConfigurable(task, task.getTaskName());

            for (int i = 0; i < attrs.getLength(); i++) {
                String attrUri = attrs.getURI(i);
                if (attrUri != null
                    && !attrUri.equals("") //$NON-NLS-1$
                    && !attrUri.equals(uri)) {
                    continue; // Ignore attributes from unknown uris
                }
                String name = attrs.getLocalName(i);
                String value = attrs.getValue(i);
                // PR: Hack for ant-type value
                //  an ant-type is a component name which can
                // be namespaced, need to extract the name
                // and convert from qualified name to uri/name
                if (name.equals("ant-type")) { //$NON-NLS-1$
                    int index = value.indexOf(':');
                    if (index != -1) {
                        String prefix = value.substring(0, index);
                        String mappedUri = context.getPrefixMapping(prefix);
                        if (mappedUri == null) {
                            throw new BuildException(
                                "Unable to find XML NS prefix " + prefix); //$NON-NLS-1$
                        }
                        value = org.apache.tools.ant.ProjectHelper.genComponentName(mappedUri, value.substring(index + 1));
                    }
                }
                wrapper.setAttribute(name, value);
            }

            if (parentWrapper != null) {
                parentWrapper.addChild(wrapper);
            }

            context.pushWrapper(wrapper);
		}
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#characters(char[], int, int, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void characters(char[] buf, int start, int count, AntXMLContext context) {
			try {
				super.characters(buf, start, count, context);
			} catch (SAXParseException e) {
				ErrorHelper.handleErrorFromElementText(start, count, context, e);
			} catch (BuildException be) {
				ErrorHelper.handleErrorFromElementText(start, count, context, be);
			}
		}
		
		public void reset() {
			task= null;
			currentTask= null;
		}
	}
	
	public static class MainHandler extends ProjectHelper2.MainHandler {
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs, AntXMLContext context) throws SAXParseException {
			if (name.equals("project") //$NON-NLS-1$
					&& (uri.equals("") || uri.equals(ANT_CORE_URI))) { //$NON-NLS-1$
				return ProjectHelper.projectHandler;
			} 
			try {
				return super.onStartChild(uri, name, qname, attrs, context);
			} catch (SAXParseException e) {
				getAntModel().error(e);
				throw e;
			}
		}
	}
	
	public static class ProjectHandler extends ProjectHelper2.ProjectHandler {
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs, AntXMLContext context) {
			if (name.equals("target") //$NON-NLS-1$
					&& (uri.equals("") || uri.equals(ANT_CORE_URI))) { //$NON-NLS-1$
				return ProjectHelper.targetHandler;
			} 
			return ProjectHelper.elementHandler;
		}
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onEndElement(java.lang.String, java.lang.String, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void onEndElement(String uri, String tag, AntXMLContext context) {
			super.onEndElement(uri, tag, context);
			if (currentImportStackSize == 1) {
				Locator locator= context.getLocator();
				getAntModel().setCurrentElementLength(locator.getLineNumber(), locator.getColumnNumber());
			}
		}
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void onStartElement(String uri, String tag, String qname, Attributes attrs, AntXMLContext context) {
			try {
				super.onStartElement(uri, tag, qname, attrs, context);
			} catch (SAXParseException e) {
				getAntModel().error(e);
			} catch (BuildException be) {
				getAntModel().error(be);
			}
			if (currentImportStackSize == 1) {
				Locator locator= context.getLocator();
				getAntModel().addProject(context.getProject(), locator.getLineNumber(), locator.getColumnNumber());
			}
		}
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#characters(char[], int, int, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void characters(char[] buf, int start, int count, AntXMLContext context) {
			try {
				super.characters(buf, start, count, context);
			} catch (SAXParseException e) {
				ErrorHelper.handleErrorFromElementText(start, count, context, e);
			} catch (BuildException be) {
				ErrorHelper.handleErrorFromElementText(start, count, context, be);
			}
		}
	}
	
	public static class TargetHandler extends ProjectHelper2.TargetHandler {
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs, AntXMLContext context) {
			return ProjectHelper.elementHandler;
		}
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void onStartElement(String uri, String tag, String qname, Attributes attrs, AntXMLContext context) {
			try {
				super.onStartElement(uri, tag, qname, attrs, context);
				Target newTarget= context.getCurrentTarget();
				Locator locator= context.getLocator();
				getAntModel().addTarget(newTarget, locator.getLineNumber(), locator.getColumnNumber());
			} catch (SAXParseException e) {
				handleErrorInTarget(context, e);
			} catch (BuildException be) {
				handleErrorInTarget(context, be);
			}
		}
		
		private void handleErrorInTarget(AntXMLContext context, Exception e) {
			Target newTarget= context.getCurrentTarget();
			Locator locator= context.getLocator();
			getAntModel().addTarget(newTarget, locator.getLineNumber(), locator.getColumnNumber());
			getAntModel().errorFromElement(e, null, locator.getLineNumber(), locator.getColumnNumber());
		}
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onEndElement(java.lang.String, java.lang.String, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void onEndElement(String uri, String tag, AntXMLContext context) {
			super.onEndElement(uri, tag, context);
			Locator locator= context.getLocator();
			getAntModel().setCurrentElementLength(locator.getLineNumber(), locator.getColumnNumber());
		}
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#characters(char[], int, int, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void characters(char[] buf, int start, int count, AntXMLContext context) {
			try {
				super.characters(buf, start, count, context);
			} catch (SAXParseException e) {
				ErrorHelper.handleErrorFromElementText(start, count, context, e);
			} catch (BuildException be) {
				ErrorHelper.handleErrorFromElementText(start, count, context, be);
			}
		}
	}
	
	 public static class RootHandler extends ProjectHelper2.RootHandler {

		public RootHandler(AntXMLContext context, AntHandler rootHandler) {
			super(context, rootHandler);
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
		 */
		public void error(SAXParseException e) {
			getAntModel().error(e);
		}
		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		public void fatalError(SAXParseException e) {
			getAntModel().fatalError(e);
		}
		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
		 */
		public void warning(SAXParseException e) {
			getAntModel().warning(e);
		}
		/* (non-Javadoc)
		 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
		 */
		public InputSource resolveEntity(String publicId, String systemId) {
			InputSource source= super.resolveEntity(publicId, systemId);
			if (source != null) {
				String path = getFileUtils().fromURI(source.getSystemId());
				if (currentEntityName == null) {
					currentEntityPath= path;
				} else {
					getAntModel().addEntity(currentEntityName, path);
					currentEntityName= null;
				}
			}
			return source;
		}
	 }
	 
	 private static class ErrorHelper {
 		public static void handleErrorFromElementText(int start, int count, AntXMLContext context, Exception e) {
 			Locator locator= context.getLocator();
			int columnNumber= locator.getColumnNumber();
			if (columnNumber > -1) {
				int offset= start;
				try {
					offset= getAntModel().getOffset(locator.getLineNumber(), 1);
				} catch (BadLocationException e1) {
				}
				getAntModel().errorFromElementText(e, offset, locator.getColumnNumber());
			} else {
				getAntModel().errorFromElementText(e, start, count);
			}
		}
	 }
	 
	 private static class LexHandler implements LexicalHandler {
	 	/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
		 */
		public void endCDATA() throws SAXException {
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endDTD()
		 */
		public void endDTD() throws SAXException {
//			AntXMLContext context= getContext();
//			Locator locator= context.getLocator();
//			getAntModel().setCurrentElementLength(locator.getLineNumber(), locator.getColumnNumber());
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
		 */
		public void startCDATA() throws SAXException {
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
		 */
		public void comment(char[] ch, int start, int length) throws SAXException {
			AntXMLContext context= getContext();
			Locator locator= context.getLocator();
			if (locator != null) {
				getAntModel().addComment(locator.getLineNumber(), locator.getColumnNumber(), length);
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
		 */
		public void endEntity(String name) throws SAXException {
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
		 */
		public void startEntity(String name) throws SAXException {
			if (currentEntityPath == null) {
				currentEntityName= name;
			} else {
				getAntModel().addEntity(name, currentEntityPath);
				currentEntityPath= null;
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
		 */
		public void startDTD(String name, String publicId, String systemId) throws SAXException {
//			AntXMLContext context= getContext();
//			Locator locator= context.getLocator();
//			getAntModel().addDTD(name, locator.getLineNumber(), locator.getColumnNumber());
		}
	 }
	
	public ProjectHelper(AntModel model) {
		setAntModel(model);
	}

	/**
     * Parses the project file, configuring the project as it goes.
     *
     * @param project the current project
     * @param source  the xml source
     * @param handler the root handler to use (contains the current context)
     * @exception BuildException if the configuration is invalid or cannot
     *                           be read
     */
    public void parse(Project project, Object source, org.apache.tools.ant.helper.ProjectHelper2.RootHandler handler) throws BuildException {
    	
    	if (!(source instanceof String)) {
    		super.parse(project, source, handler);
    		return;
    	}
    	
    	AntXMLContext context= (AntXMLContext)project.getReference("ant.parsing.context"); //$NON-NLS-1$
		//switch to using "our" handler so parsing will continue on hitting errors.
    	handler= new RootHandler(context, mainHandler);
    	
        Reader stream= new StringReader((String)source);
             
        InputSource inputSource = null;
        try {
            /**
             * SAX 2 style parser used to parse the given file.
             */
            
        	//We cannot use the JAXPUtils support here as the underlying parser factory is cached and 
        	//will not reflect classpath changes that effect which XML parser will be returned.
        	//see bug 59764
        	//XMLReader parser = JAXPUtils.getNamespaceXMLReader();
        	XMLReader parser= getNamespaceXMLReader();
        	if (parser == null) {
        		throw new BuildException(ProjectHelperMessages.getString("ProjectHelper.0")); //$NON-NLS-1$
        	}
            String uri = null;
            if (buildFile != null) {
                uri = getFileUtils().toURI(buildFile.getAbsolutePath());
            }

            inputSource = new InputSource(stream);
            if (uri != null) {
                inputSource.setSystemId(uri);
            }

            context.setBuildFile(buildFile);
            
            parser.setContentHandler(handler);
            parser.setEntityResolver(handler);
            parser.setErrorHandler(handler);
            parser.setDTDHandler(handler);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", lexicalHandler); //$NON-NLS-1$
            parser.parse(inputSource);
        } catch (SAXParseException exc) {
        	getAntModel().fatalError(exc);
        } catch (SAXException exc) {
        	//ignore as we will be parsing incomplete source
        } catch (FileNotFoundException exc) {
            throw new BuildException(exc);
        } catch (UnsupportedEncodingException exc) {
              throw new BuildException(exc);
        } catch (IOException exc) {
            throw new BuildException(exc);
        } finally {
            if (stream != null) {
                try {
                	stream.close();
                } catch (IOException ioe) {
                    // ignore this
                }
            }
        }
    }

	/**
	 * Sets the buildfile that is about to be parsed or <code>null</code> if
	 * parsing has completed.
	 * 
	 * @param file The buildfile about to be parsed
	 */
	public void setBuildFile(File file) {
		buildFile= file;
		currentImportStackSize= 1;
	}
	
	/* (non-Javadoc)
	 * We override this method from ProjectHelper2 as we do not want to execute the implicit target or
	 * any other target for that matter as it could hang Eclipse.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=50795 for more details.
	 * 
	 * @see org.apache.tools.ant.ProjectHelper#parse(org.apache.tools.ant.Project, java.lang.Object)
	 */
	public void parse(Project project, Object source) throws BuildException {
		AntXMLContext context = (AntXMLContext) project.getReference("ant.parsing.context"); //$NON-NLS-1$
        if (context == null) {
        	getImportStack().removeAllElements();
            context = new AntXMLContext(project);
            project.addReference("ant.parsing.context", context); //$NON-NLS-1$
            project.addReference("ant.targets", context.getTargets()); //$NON-NLS-1$
            fgAntContext= context;
        }
        getImportStack().addElement(source);
		currentImportStackSize= getImportStack().size();
        if (getImportStack().size() > 1) {
            // we are in an imported file.
            context.setIgnoreProjectTag(true);
            Target currentTarget = context.getCurrentTarget();
            try {
                Target newCurrent = new Target();
                newCurrent.setProject(project);
                newCurrent.setName(""); //$NON-NLS-1$
                context.setCurrentTarget(newCurrent);
                parse(project, source, new RootHandler(context, mainHandler));
            } finally {
                context.setCurrentTarget(currentTarget);
            }
        } else {
            // top level file
            parse(project, source, new RootHandler(context, mainHandler));
        }
	}

	public static void setAntModel(AntModel antModel) {
		fgAntModel= antModel;
		((ProjectHelper.ElementHandler)elementHandler).reset();
		fu= null;
	}

	public static AntModel getAntModel() {
		return fgAntModel;
	}
	
	public static AntXMLContext getContext() {
		return fgAntContext;
	}
	
	private static FileUtils getFileUtils() {
		if (fu == null) {
			fu= FileUtils.newFileUtils();
		}
		return fu;
	}
	
	 /**
     * Returns a newly created SAX 2 XMLReader, which is namespace aware
     *
     * @return a SAX 2 XMLReader.
     * @since Ant 1.6 from org.apache.tools.ant.util.JAXPUtils
     */
    private XMLReader getNamespaceXMLReader() throws BuildException {
        try {
            return newSAXParser(getNSParserFactory()).getXMLReader();
        } catch (SAXException e) {
        }
        return null;
    }
    
    /**
     * Returns the parser factory to use to create namespace aware parsers.
     *
     * @return a SAXParserFactory to use which supports manufacture of
     * namespace aware parsers
     *
     * @since Ant 1.6 from org.apache.tools.ant.util.JAXPUtils
     */
    private SAXParserFactory getNSParserFactory() throws BuildException {

    	SAXParserFactory nsParserFactory = JAXPUtils.newParserFactory();
        nsParserFactory.setNamespaceAware(true);
        
        return nsParserFactory;
    }
    
    /**
     * @return a new SAXParser instance as helper for getParser and
     * getXMLReader.
     *
     * @since Ant 1.5 from org.apache.tools.ant.util.JAXPUtils
     */
    private SAXParser newSAXParser(SAXParserFactory factory) {
        try {
            return factory.newSAXParser();
        } catch (ParserConfigurationException e) {
          
        } catch (SAXException e) {
           
        }
        return null;
    }
}