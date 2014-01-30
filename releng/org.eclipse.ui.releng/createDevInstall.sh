#!/bin/bash

eclipse/eclipse \
-application org.eclipse.equinox.p2.director \
-noSplash \
-repository \
http://download.eclipse.org/egit/pde/updates-nightly,\
http://download.eclipse.org/egit/updates-nightly,\
http://download.eclipse.org/eclipse/updates/4.4-I-builds,\
http://download.eclipse.org/e4/updates/0.16-I-builds,\
http://download.eclipse.org/releases/luna,\
http://download.eclipse.org/tools/orbit/downloads/drops/R20130827064939/repository,\
http://download.eclipse.org/technology/nebula/snapshot \
-installIUs \
org.apache.commons.jxpath/1.3.0.v200911051830,\
org.apache.batik.xml/1.7.0.v201011041433,\
org.dojotoolkit/1.6.1.v201108161253,\
org.json/1.0.0.v201011060100,\
org.easymock/2.4.0.v20090202-0900,\
org.mockito/1.8.4.v201303031500,\
org.hamcrest/1.1.0.v20090501071000,\
org.hamcrest.integration/1.1.0.v201303031500,\
org.hamcrest.library/1.1.0.v20090501071000,\
org.hamcrest.text/1.1.0.v20090501071000,\
org.objenesis/1.0.0.v201105211943,\
org.eclipse.nebula.widgets.gallery.feature.feature.group,\
org.eclipse.egit.feature.group,\
org.eclipse.egit.source.feature.group,\
org.eclipse.jgit.feature.group,\
org.eclipse.jgit.source.feature.group,\
org.eclipse.egit.fetchfactory.feature.group,\
org.eclipse.emf.sdk.feature.group,\
org.eclipse.xtext.sdk.feature.group,\
org.eclipse.wst.xml_ui.feature.feature.group,\
org.eclipse.pde.api.tools.ee.feature.feature.group,\
org.eclipse.e4.core.tools.feature.feature.group,\
org.eclipse.e4.tools.orion.css.editor.feature.feature.group \
-vmargs -Declipse.p2.mirrors=false

