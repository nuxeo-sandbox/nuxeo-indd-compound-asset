nuxeo-indd-compound-asset
=============================

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Sandbox/sandbox_nuxeo-indd-compound-asset-master)](https://qa.nuxeo.org/jenkins/job/Sandbox/job/sandbox_nuxeo-indd-compound-asset-master/)

## List of Features

This plugin provides a [filemanager](https://doc.nuxeo.com/nxdoc/file-manager/) plugin to import InDesign packages into the Nuxeo Platform.

Input: an InDesign package (which is a zip, as described [here](https://helpx.adobe.com/indesign/how-to/indesign-package-files-for-handoff.html)). Note that there is a sample included [in this repo](nuxeo-indd-compound-asset-core/src/test/resources/files/sample.zip).

Result: the plug-in creates the following:

* A Workspace that contains the documents extracted from the package:
    * A File document, corresponding to the `indd` file, that contains
      * The original package zip in `compound:archive`
      * The PDF preview, if provided, in `compound:renditions`
      * A list of related/linked asset document ids in `compound:docs`
      * The `CompoundDocument` facet
    * Copies of some package files. Each document has:
      * A link to the parent indd file in `componentdoc:usedin`
      * The `ComponentDocument` facet
    * Note that the `document fonts` folder, `txt`, and `imdl` files [are ignored](https://github.com/nuxeo-sandbox/nuxeo-indd-compound-asset/blob/c8a0c5184ebabdaaa73665f69a05ca601e0c5499/nuxeo-indd-compound-asset-core/src/main/java/org/nuxeo/labs/dam/indd/compound/InddPackageImporter.java#L96)

Note:

* The extracted content is flat. Any folder hierarchy is *not* preserved.
* The import checks to see if any extracted content already exists in the repository. This is done using a [simple search](https://github.com/nuxeo-sandbox/nuxeo-indd-compound-asset/blob/c8a0c5184ebabdaaa73665f69a05ca601e0c5499/nuxeo-indd-compound-asset-core/src/main/java/org/nuxeo/labs/dam/indd/compound/InddPackageImporter.java#L124) based on file name, comparing it to the `dc:title` field. If a match is found, the matching document is linked via `compound:docs` instead of ingesting the content again.
* When creating an InDesign package, there is an option to include a PDF rendition. Turn this on. The OOTB preview of an `indd` file has low fidelity, whereas the PDF rendition should support full resolution preview.
* This plug-in does *not* handle INDD preview. For that install [Nuxeo Adobe InDesign Preview](https://connect.nuxeo.com/nuxeo/site/marketplace/package/nuxeo-indd-rendition)

## Build

Assuming maven is correctly setup on your computer:

```
git clone
mvn package
```

## Install

Install the package on your instance.


# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).

