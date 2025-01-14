/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.util.ClassLoaderUtil;

/**
 * An XML document container reads and parses XML only when it is
 * accessed.  JXPath traverses Containers transparently -
 * you use the same paths to access objects in containers as you
 * do to access those objects directly.  You can create
 * XMLDocumentContainers for various XML documents that may or
 * may not be accessed by XPaths.  If they are, they will be automatically
 * read, parsed and traversed. If they are not - they won't be
 * read at all.
 */
public class DocumentContainer extends XMLParser2 implements Container {

    /** DOM constant */
    public static final String MODEL_DOM = "DOM";

    /** JDOM constant */
    public static final String MODEL_JDOM = "JDOM";

    private static final long serialVersionUID = -8713290334113427066L;

    private static HashMap parserClasses = new HashMap();
    static {
        parserClasses.put(MODEL_DOM,
                          "org.apache.commons.jxpath.xml.DOMParser");
        parserClasses.put(MODEL_JDOM,
                          "org.apache.commons.jxpath.xml.JDOMParser");
    }

    private static HashMap parsers = new HashMap();

    private Object document;
    private final URL xmlURL;
    private final String model;

    /**
     * Add an XML parser.  Parsers for the models "DOM" and "JDOM" are
     * pre-registered.
     * @param model model name
     * @param parser parser
     */
    public static void registerXMLParser(final String model, final XMLParser parser) {
        parsers.put(model, parser);
    }

    /**
     * Add a class of a custom XML parser.
     * Parsers for the models "DOM" and "JDOM" are pre-registered.
     * @param model model name
     * @param parserClassName parser class name
     */
    public static void registerXMLParser(final String model, final String parserClassName) {
        parserClasses.put(model, parserClassName);
    }

    /**
     * Use this constructor if the desired model is DOM.
     *
     * @param xmlURL is a URL for an XML file.
     * Use getClass().getResource(resourceName) to load XML from a
     * resource file.
     */
    public DocumentContainer(final URL xmlURL) {
        this(xmlURL, MODEL_DOM);
    }

    /**
     * Constructs a new DocumentContainer.
     * @param xmlURL is a URL for an XML file. Use getClass().getResource
     *               (resourceName) to load XML from a resource file.
     *
     * @param model is one of the MODEL_* constants defined in this class. It
     *              determines which parser should be used to load the XML.
     */
    public DocumentContainer(final URL xmlURL, final String model) {
        this.xmlURL = xmlURL;
        if (xmlURL == null) {
            throw new JXPathException("XML URL is null");
        }
        this.model = model;
    }

    /**
     * Reads XML, caches it internally and returns the Document.
     * @return Object
     */
    @Override
    public Object getValue() {
        if (document == null) {
            try {
                InputStream stream = null;
                try {
                    if (xmlURL != null) {
                        stream = xmlURL.openStream();
                    }
                    document = parseXML(stream);
                }
                finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
            catch (final IOException ex) {
                throw new JXPathException(
                    "Cannot read XML from: " + xmlURL.toString(),
                    ex);
            }
        }
        return document;
    }

    /**
     * Parses XML using the parser for the specified model.
     * @param stream InputStream
     * @return Object
     */
    @Override
    public Object parseXML(final InputStream stream) {
        final XMLParser parser = getParser(model);
        if (parser instanceof XMLParser2) {
            final XMLParser2 parser2 = (XMLParser2) parser;
            parser2.setValidating(isValidating());
            parser2.setNamespaceAware(isNamespaceAware());
            parser2.setIgnoringElementContentWhitespace(
                    isIgnoringElementContentWhitespace());
            parser2.setExpandEntityReferences(isExpandEntityReferences());
            parser2.setIgnoringComments(isIgnoringComments());
            parser2.setCoalescing(isCoalescing());
        }
        return parser.parseXML(stream);
    }

    /**
     * Throws an UnsupportedOperationException.
     * @param value value (not) to set
     */
    @Override
    public void setValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Maps a model type to a parser.
     * @param model input model type
     * @return XMLParser
     */
    private static XMLParser getParser(final String model) {
        return (XMLParser) parsers.computeIfAbsent(model, k -> {
            final String className = (String) parserClasses.get(model);
            if (className == null) {
                throw new JXPathException("Unsupported XML model: " + model);
            }
            try {
                final Class clazz = ClassLoaderUtil.getClass(className, true);
                return (XMLParser) clazz.getConstructor().newInstance();
            }
            catch (final Exception ex) {
                throw new JXPathException("Cannot allocate XMLParser: " + className, ex);
            }
        });
    }
}
