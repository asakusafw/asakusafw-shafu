/**
 * Copyright 2013-2021 Asakusa Framework Team.
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
 */
package com.asakusafw.shafu.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.asakusafw.shafu.internal.ui.wizards.messages"; //$NON-NLS-1$
    public static String ImportProjectsWizard_errorProjectConflict;
    public static String ImportProjectsWizard_monitorImportProjects;
    public static String ImportProjectsWizard_monitorLoadDescription;
    public static String ImportProjectsWizard_monitorLoadProject;
    public static String ImportProjectsWizard_monitorPeformFinish;
    public static String ImportProjectsWizard_statusMultiple;
    public static String ImportProjectsWizard_title;
    public static String NewProjectWizard_dialogOverwriteMessage;
    public static String NewProjectWizard_dialogOverwriteTitle;
    public static String NewProjectWizard_errorArchiveDownload;
    public static String NewProjectWizard_errorArchiveExtract;
    public static String NewProjectWizard_errorArchiveNotSupport;
    public static String NewProjectWizard_errorProjectConflict;
    public static String NewProjectWizard_errorProjectFailedToCopyContent;
    public static String NewProjectWizard_errorTemplateBuild;
    public static String NewProjectWizard_errorTemplateInvalid;
    public static String NewProjectWizard_errorUnknown;
    public static String NewProjectWizard_monitorCopyContents;
    public static String NewProjectWizard_monitorExtractArchive;
    public static String NewProjectWizard_monitorExtractContents;
    public static String NewProjectWizard_monitorInspectProject;
    public static String NewProjectWizard_monitorLoadDescription;
    public static String NewProjectWizard_monitorLoadProject;
    public static String NewProjectWizard_monitorMain;
    public static String NewProjectWizard_selectEntryMessage;
    public static String NewProjectWizard_selectEntryTitle;
    public static String NewProjectWizard_title;
    public static String ProjectInformationPage_description;
    public static String ProjectInformationPage_errorProjectNameInconsistent;
    public static String ProjectInformationPage_title;
    public static String SelectGradleProjectsPage_buildCheckLabel;
    public static String SelectGradleProjectsPage_buildCheckTooltip;
    public static String SelectGradleProjectsPage_description;
    public static String SelectGradleProjectsPage_errorCanceledToInspectProject;
    public static String SelectGradleProjectsPage_errorConsoleNotActive;
    public static String SelectGradleProjectsPage_errorFailedToInspectProject;
    public static String SelectGradleProjectsPage_errorNotSelect;
    public static String SelectGradleProjectsPage_errorProjectNotAvailable;
    public static String SelectGradleProjectsPage_gradleGroupLabel;
    public static String SelectGradleProjectsPage_infoProjectAlreadyImport;
    public static String SelectGradleProjectsPage_openConsoleLabel;
    public static String SelectGradleProjectsPage_openConsoleTooltip;
    public static String SelectGradleProjectsPage_targetLabel;
    public static String SelectGradleProjectsPage_title;
    public static String SelectGradleProjectsPage_workingSetGroupLabel;
    public static String SelectProjectDirectoryPage_description;
    public static String SelectProjectDirectoryPage_directoryDialogTitle;
    public static String SelectProjectDirectoryPage_errorDirectoryEmpty;
    public static String SelectProjectDirectoryPage_errorDirectoryMissing;
    public static String SelectProjectDirectoryPage_errorDirectoryNotContain;
    public static String SelectProjectDirectoryPage_fieldButton;
    public static String SelectProjectDirectoryPage_fieldLabel;
    public static String SelectProjectDirectoryPage_title;
    public static String SelectProjectTemplatePage_buildCheckLabel;
    public static String SelectProjectTemplatePage_buildCheckTooltip;
    public static String SelectProjectTemplatePage_description;
    public static String SelectProjectTemplatePage_disableTemplateFilterLabel;
    public static String SelectProjectTemplatePage_errorFileEmpty;
    public static String SelectProjectTemplatePage_errorFileMissing;
    public static String SelectProjectTemplatePage_errorFileNotSupport;
    public static String SelectProjectTemplatePage_errorUrlEmpty;
    public static String SelectProjectTemplatePage_errorUrlInvalid;
    public static String SelectProjectTemplatePage_fileDialogTitle;
    public static String SelectProjectTemplatePage_gradleGroupLabel;
    public static String SelectProjectTemplatePage_monitorExtractTemplate;
    public static String SelectProjectTemplatePage_openConsoleLabel;
    public static String SelectProjectTemplatePage_openConsoleTooltip;
    public static String SelectProjectTemplatePage_openFileButton;
    public static String SelectProjectTemplatePage_templateFilterPattern;
    public static String SelectProjectTemplatePage_title;
    public static String SelectProjectTemplatePage_urlNoTemplateMessage;
    public static String SelectProjectTemplatePage_urlNoTemplateTitle;
    public static String SelectProjectTemplatePage_urlSelect;
    public static String SelectProjectTemplatePage_urlSelectTemplateMessage;
    public static String SelectProjectTemplatePage_urlSelectTemplateTitle;
    public static String SelectProjectTemplatePage_useFileLabel;
    public static String SelectProjectTemplatePage_useUrlLabel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        return;
    }
}
