/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflow.modules.formspdf.service.task;

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.filegenerator.service.TemporaryFileGeneratorService;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.FormResponseHome;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.*;
import fr.paris.lutece.plugins.workflow.modules.formspdf.service.HtmlToPDFGenerator;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.ResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.task.Task;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
/**
 * @author norbert.le.garrec
 *
 */
public class FormsPDFTask extends Task {

    /**
     * The task title
     */
    private static final String PROPERTY_LABEL_TITLE = "module.workflow.formspdf.title";
    private static final String PROPERTY_LABEL_DESCRIPTION = "module.workflow.formspdf.export.pdf.description";

    /**
     * the FormJasperConfigService to manage the task configuration
     */
    private static final ITaskConfigService _formsPDFTaskConfigService = SpringContextService.getBean( "workflow-formspdf.formsPDFTaskConfigService" );

    /**
     * the ResourceHistoryService to get the forms to process
     */
    private static final IResourceHistoryService _resourceHistoryService = SpringContextService.getBean( ResourceHistoryService.BEAN_SERVICE );

    @Override
    public void processTask( int nIdResourceHistory, HttpServletRequest request, Locale locale )
    {

            // Get the resourceHistory to find the resource to work with
            ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey(nIdResourceHistory);
            // Get the task configuration
            FormsPDFTaskConfig formsPDFTaskConfig = _formsPDFTaskConfigService.findByPrimaryKey(getId());
            // Id of the Form to modify
            int nIdFormResponse = 0;
            String strError = "";
            nIdFormResponse = resourceHistory.getIdResource();

            FormResponse frep = FormResponseHome.findByPrimaryKey(nIdFormResponse);
            Form form = FormHome.findByPrimaryKey(frep.getFormId());

            // TODO Gerer le cas null quand il s'agit d'une action automatique
            AdminUser user = null;
            if (request != null) {
                user = AdminUserService.getAdminUser(request);
            }
            I18nService.getLocalizedString(PROPERTY_LABEL_DESCRIPTION, locale);
            HtmlToPDFGenerator htmltopdf = new HtmlToPDFGenerator(form.getTitle(), I18nService.getLocalizedString(PROPERTY_LABEL_DESCRIPTION, locale), frep,
                    FormsPDFTaskTemplateHome.findByPrimaryKey(formsPDFTaskConfig.getIdTemplate()),  request);

            TemporaryFileGeneratorService.getInstance().generateFile(htmltopdf, user);


    }

    @Override
    public String getTitle( Locale locale )
    {
        return I18nService.getLocalizedString( PROPERTY_LABEL_TITLE, locale );
    }

    @Override
    public Map<String, String> getTaskFormEntries( Locale locale )
    {
        return null;
    }

    @Override
    public void init( )
    {
        // Do nothing

    }

    @Override
    public void doRemoveTaskInformation( int nIdHistory )
    {
        // Do Nothing

    }

    @Override
    public void doRemoveConfig( )
    {
        // _formsJasperTaskConfigService.remove( getId( ) );
        _formsPDFTaskConfigService.remove( getId( ) );
    }

}
