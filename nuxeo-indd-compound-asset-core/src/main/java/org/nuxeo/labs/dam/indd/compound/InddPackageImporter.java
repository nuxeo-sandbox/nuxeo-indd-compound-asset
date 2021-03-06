/*
 * (C) Copyright 2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 *
 */

package org.nuxeo.labs.dam.indd.compound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.filemanager.api.FileImporterContext;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.filemanager.service.extension.AbstractFileImporter;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Simple Plugin that imports INDD Zip package into Nuxeo.
 */
public class InddPackageImporter extends AbstractFileImporter {

    public static final String INDD_EXT = ".indd";
    public static final String PDF_EXT = ".pdf";

    public static final String COMPOUND_FACET = "CompoundDocument";
    public static final String COMPONENT_FACET = "ComponentDocument";

    public static final String COMPONENTS_XPATH = "compound:docs";
    public static final String ARCHIVE_XPATH = "compound:archive";
    public static final String RENDITIONS_XPATH = "compound:renditions";

    public static final String COMPOUNDS_XPATH = "componentdoc:usedin";

    public static final String CONTAINER_TYPE = "Workspace";


    private static final Log log = LogFactory.getLog(InddPackageImporter.class);

    public boolean isValid(ZipFile zip) {
        //look for indesign file
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().toLowerCase().endsWith(INDD_EXT)) {
                return true;
            }
        }
        return false;
    }

    public String getFilename(String path) {
        return path.split("/")[path.split("/").length - 1];
    }


    public DocumentModel unzip(CoreSession session, DocumentModel workspace, ZipFile zipFile, Blob blob)
            throws IOException {
        FileManager fileManager = Framework.getService(FileManager.class);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry inddEntry = null;
        List<Blob> renditions = new ArrayList<>();

        DocumentModelList components = new DocumentModelListImpl();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String fileName = entry.getName();
            if (fileName.startsWith("__MACOSX/")
                 || fileName.startsWith(".")
                 || fileName.contentEquals("../") //Avoid hacks trying to access a directory outside the current one
                 || fileName.endsWith(".DS_Store")
                 || fileName.toLowerCase().contains("document fonts/")
                 || fileName.toLowerCase().endsWith(".txt")
                 || fileName.toLowerCase().endsWith(".idml")) {
             continue;
            }

            if (entry.isDirectory()) {
             continue;
            }

            if (fileName.toLowerCase().endsWith(INDD_EXT)) {
                inddEntry = entry;
                continue;
            }

            if (fileName.toLowerCase().endsWith(PDF_EXT)) {
                Blob fileBlob = new FileBlob(zipFile.getInputStream(entry),"application/pdf");
                fileBlob.setFilename(getFilename(fileName));
                renditions.add(fileBlob);
                continue;
            }

            String name = getFilename(fileName);

            //check if duplicates
            String query = String.format("Select * FROM Document Where dc:title='%s' AND ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0",name);
            DocumentModelList duplicates = session.query(query);
            if (duplicates.size()>0) {
                components.add(duplicates.get(0));
            } else {
                Blob fileBlob = new FileBlob(zipFile.getInputStream(entry));
                fileBlob.setFilename(name);
                FileImporterContext context = FileImporterContext.builder(session,
                        fileBlob, workspace.getPathAsString())
                        .fileName(fileBlob.getFilename())
                        .overwrite(true)
                        .build();
                DocumentModel element = fileManager.createOrUpdateDocument(context);
                components.add(element);
            }
        }

        DocumentModel inddDoc;

        Blob inddBlob = new FileBlob(zipFile.getInputStream(inddEntry));
        inddBlob.setFilename(getFilename(inddEntry.getName()));
        inddBlob.setMimeType("application/x-indesign");

        FileImporterContext context = FileImporterContext.builder(session,
                inddBlob, workspace.getPathAsString())
                .fileName(inddBlob.getFilename())
                .overwrite(true)
                .build();

        inddDoc = fileManager.createOrUpdateDocument(context);
        inddDoc.setPropertyValue("file:content", (Serializable) inddBlob);
        inddDoc.setPropertyValue("dc:title",inddBlob.getFilename());
        inddDoc = session.saveDocument(inddDoc);


        inddDoc.addFacet(COMPOUND_FACET);

        List<String> componentIds = new ArrayList<>();
        for(DocumentModel component: components) {
            componentIds.add(component.getId());
        }

        inddDoc.setPropertyValue(COMPONENTS_XPATH, (Serializable) componentIds);
        inddDoc.setPropertyValue(ARCHIVE_XPATH, (Serializable) blob);
        inddDoc.setPropertyValue(RENDITIONS_XPATH, (Serializable) renditions);
        session.saveDocument(inddDoc);

        //update components
        for(DocumentModel component:components) {
             if (!component.hasFacet(COMPONENT_FACET)) {
                 component.addFacet(COMPONENT_FACET);
            }
            String[] compounds = (String[]) component.getPropertyValue(COMPOUNDS_XPATH);
            List<String> compoundList = compounds != null ? new ArrayList<>(Arrays.asList(compounds)) : new ArrayList<>();
            if (!compoundList.contains(inddDoc.getId())) {
                compoundList.add(inddDoc.getId());
                component.setPropertyValue(COMPOUNDS_XPATH, (Serializable) compoundList);
            }
        }

        session.saveDocuments(components.toArray(new DocumentModel[]{}));
        return inddDoc;

    }

    @Override
    public DocumentModel createOrUpdate(FileImporterContext context) throws IOException {
        try (CloseableFile source = context.getBlob().getCloseableFile()) {
            try (ZipFile zip = new ZipFile(source.getFile())) {
                if (!isValid(zip)) {
                    return null;
                }
                String name;

                if (context.getFileName()!=null) {
                    name = context.getFileName();
                } else {
                    name = context.getBlob().getFilename();
                    if (name.lastIndexOf(".")>0) {
                        name = name.substring(0, name.lastIndexOf("."));
                    }
                }

                DocumentModel workspace = context.getSession().createDocumentModel(
                        context.getParentPath(),name,CONTAINER_TYPE);
                workspace.setPropertyValue("dc:title",name);
                workspace = context.getSession().createDocument(workspace);
                return unzip(context.getSession(),workspace,zip,context.getBlob());
            }
        }
    }
}