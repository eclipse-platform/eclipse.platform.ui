#!/bin/bash

eclipse/eclipse \
-application org.eclipse.equinox.p2.director \
-noSplash \
-repository \
http://download.eclipse.org/egit/pde/updates-nightly,\
http://download.eclipse.org/egit/updates-nightly,\
http://download.eclipse.org/eclipse/updates/4.3-I-builds,\
http://download.eclipse.org/e4/updates/0.14-I-builds,\
http://download.eclipse.org/releases/kepler,\
http://download.eclipse.org/tools/orbit/downloads/drops/R20130118183705/repository,\
http://download.eclipse.org/technology/nebula/snapshot \
-installIUs \
org.apache.commons.jxpath/1.3.0.v200911051830,\
org.apache.batik.xml/1.6.0.v201011041432,\
org.dojotoolkit/1.6.1.v201108161253,\
org.json/1.0.0.v201011060100,\
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
org.eclipse.e4.tools.css.editor.feature.feature.group \
-vmargs -Declipse.p2.mirrors=false

