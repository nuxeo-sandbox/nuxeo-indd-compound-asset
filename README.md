nuxeo-indd-compound-asset
=============================

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Sandbox/sandbox_nuxeo-indd-compound-asset-master)](https://qa.nuxeo.org/jenkins/job/Sandbox/job/sandbox_nuxeo-indd-compound-asset-master/)

## List of Features

This plugin provides a filemanager plugin to import InDesign packages in the Nuxeo Platform.

Input: an InDesign package (a zip file as described [here](https://helpx.adobe.com/indesign/how-to/indesign-package-files-for-handoff.html)). Note that there is a sample included [in this repo](nuxeo-indd-compound-asset-core/src/test/resources/files/sample.zip).

Result: the plug-in creates the following:

* A Workspace that contains the documents extracted from the package:
    * A File document, corresponding to the `indd` file, that contains
      * The original package zip in `compound:archive`
      * The PDF preview, if provided, in `compound:renditions`
      * A list of related/linked asset document ids in `compound:docs`
      * The `CompoundDocument` facet
    * Copies of all package files. Each document has:
      * A link to the parent indd file in `componentdoc:usedin`
      * The `ComponentDocument` facet
      * Note that the `document fonts` folder, `txt`, and `imdl` files are ignored
        
Note that all extracted documents are stored within a Workspace (i.e. there is no folder hiearchy). Also note that when creating an InDesign package there is an option to include a PDF rendition. This is recommended as the OOTB preview of an `indd` file has low fidelity.

## Build

Assuming maven is correctly setup on your computer:

```
git clone
mvn package
```

## Install

Install the package on your instance.


## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information at <http://www.nuxeo.com/>
