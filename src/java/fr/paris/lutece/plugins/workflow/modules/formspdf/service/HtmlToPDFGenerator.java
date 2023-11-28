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
package fr.paris.lutece.plugins.workflow.modules.formspdf.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import javax.servlet.http.HttpServletRequest;


import fr.paris.lutece.plugins.forms.business.*;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeCamera;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeFile;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeImage;
import fr.paris.lutece.plugins.forms.service.entrytype.EntryTypeTermsOfService;
import fr.paris.lutece.plugins.forms.service.provider.GenericFormsProvider;


import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.AbstractEntryTypeComment;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.EntryTypeServiceManager;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.IEntryTypeService;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppPathService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;

import fr.paris.lutece.plugins.html2pdf.service.PdfConverterService;
import fr.paris.lutece.plugins.html2pdf.service.PdfConverterServiceException;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.plugins.workflowcore.service.provider.InfoMarker;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;

// TODO: Auto-generated Javadoc
/**
 * The Class HtmlToPDFGenerator.
 */
public class HtmlToPDFGenerator extends AbstractFileGenerator
{

    /**
     * Instantiates a new html to PDF generator.
     *
     * @param fileName
     *            the file name
     * @param fileDescription
     *            the file description
     * @param formResponse
     *            the form response
     * @param formsPDFTaskTemplate
     *            the template
     */
    public HtmlToPDFGenerator( String fileName, String fileDescription, FormResponse formResponse, FormsPDFTaskTemplate formsPDFTaskTemplate, HttpServletRequest request)
    {
        super( fileName, fileDescription, formResponse, formsPDFTaskTemplate, request);
    }

    private static final boolean ZIP_EXPORT = Boolean.parseBoolean( AppPropertiesService.getProperty( "workflow-formspdf.export.pdf.zip", "false" ) );
    private static final String CONSTANT_MIME_TYPE_PDF = "application/pdf";
    private static final String EXTENSION_PDF = ".pdf";
    private static final String KEY_LABEL_YES = "portal.util.labelYes";
    private static final String KEY_LABEL_NO = "portal.util.labelNo";
    private static final String LINK_MESSAGE_FO = "module.workflow.formspdf.task_formspdf_info.label.link_FO";
    private static final String LINK_MESSAGE_BO = "module.workflow.formspdf.task_formspdf_info.label.link_BO";
    private static final String PUBLISHED = "module.workflow.formspdf.task_formspdf_info.label.published";
    private static final String NOT_PUBLISHED = "module.workflow.formspdf.task_formspdf_info.label.not_published";


    /**
     * Generate file.
     *
     * @return the path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public Path generateFile( ) throws IOException
    {
        Path directoryFile = Paths.get( TMP_DIR, _fileName );
        if ( !directoryFile.toFile( ).exists( ) )
        {
            directoryFile.toFile( ).mkdir( );
        }
        writeExportFile( directoryFile );
        if ( hasMultipleFiles( ) )
        {
            return directoryFile;
        }
        File [ ] files = directoryFile.toFile( ).listFiles( ( File f ) -> f.getName( ).endsWith( EXTENSION_PDF ) );
        return files [0].toPath( );
    }
    /**
     * Gets the file name.
     *
     * @return the file name
     */
    @Override
    public String getFileName( )
    {
        return _fileName + EXTENSION_PDF;
    }

    /**
     * Gets the mime type.
     *
     * @return the mime type
     */
    @Override
    public String getMimeType( )
    {
        return CONSTANT_MIME_TYPE_PDF;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    @Override
    public String getDescription( )
    {
        return _fileDescription;
    }

    /**
     * Checks if is zippable.
     *
     * @return true, if is zippable
     */
    @Override
    public boolean isZippable( )
    {
        return ZIP_EXPORT;
    }

    public  HashMap<Integer, FormQuestionResponse>  formResponseListToHashmap( List<FormQuestionResponse>  formQuestionResponseList)
    {
        HashMap<Integer, FormQuestionResponse> formResponseListByEntryId = new HashMap<>();
        for (int i = 0; i < formQuestionResponseList.size(); i++)
        {
            int idEntry = formQuestionResponseList.get(i).getQuestion().getIdEntry();
            if(formResponseListByEntryId.containsKey(idEntry))
            {
                // This is to had the response to the hashmap when there are iterations in the form (one than one time the same entry)
                List <Response> presentResponses = formResponseListByEntryId.get(formQuestionResponseList.get(i).getQuestion().getIdEntry()).getEntryResponse();
                List <Response> newResponses = formQuestionResponseList.get(i).getEntryResponse();
                presentResponses.addAll(newResponses);
                formQuestionResponseList.get(i).setEntryResponse(presentResponses);
                formResponseListByEntryId.put(idEntry, formQuestionResponseList.get(i));
            }
            else
            {
                formResponseListByEntryId.put(idEntry, formQuestionResponseList.get(i));
            }
        }
        return formResponseListByEntryId;
    }
    /**
     * Fill template with form question response.
     *
     * @param template
     *            the template
     * @param formQuestionResponseList
     *            the form question response list
     * @param listQuestions
     *            the list questions
     * @return the html template
     */
    private HtmlTemplate fillTemplateWithFormQuestionResponse (String template, List<FormQuestionResponse> formQuestionResponseList, List<Question> listQuestions) {
        Map<String, Object> model = new HashMap<>();
        Form form = FormHome.findByPrimaryKey(_formsPDFTaskTemplate.getIdForm());
        Collection<InfoMarker> collectionNotifyMarkers = GenericFormsProvider.getProviderMarkerDescriptions(form);
        listQuestions.sort(Comparator.comparingInt(Question::getIdEntry));
        model = markersToModel(model, collectionNotifyMarkers, formQuestionResponseList);
        return AppTemplateService.getTemplateFromStringFtl(template, Locale.getDefault(), model);
    }


    /**
     * Write export file.
     *
     * @param directoryFile
     *            the directory file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void writeExportFile( Path directoryFile ) throws IOException
    {
        String strError = "";
        List<Question> listQuestions = QuestionHome.getListQuestionByIdForm(_formResponse.getFormId());
        List<FormQuestionResponse> formQuestionResponseList = FormQuestionResponseHome.getFormQuestionResponseListByFormResponse( _formResponse.getId( ) );
        HtmlTemplate htmlTemplate = fillTemplateWithFormQuestionResponse(_formsPDFTaskTemplate.getContent(), formQuestionResponseList, listQuestions);
        try ( OutputStream outputStream = Files.newOutputStream( directoryFile.resolve( generateFileName( _formResponse ) + ".pdf" ) ) )
        {
            Document doc = Jsoup.parse( htmlTemplate.getHtml( ), "UTF-8" );
            doc.outputSettings( ).syntax( Document.OutputSettings.Syntax.xml );
            doc.outputSettings( ).escapeMode( EscapeMode.base.xhtml );
            doc.outputSettings( ).charset( "UTF-8" );

            PdfConverterService.getInstance( ).getPdfBuilder( ).reset( ).withHtmlContent( doc.html( ) ).notEditable( ).render( outputStream );
        }
        catch( PdfConverterServiceException e )
        {
            strError = "Une erreur s'est produite lors de la generation de l'edition";
            AppLogService.error( strError, e );
           throw new RuntimeException( strError, e );
        }
        catch( IOException e )
        {
            strError = "Une erreur s'est produite lors de la generation de l'edition";
            AppLogService.error( strError, e );
           throw new RuntimeException( strError, e );
        }

    }

    private Map<String, Object> markersToModel( Map<String, Object> model, Collection<InfoMarker> collectionInfoMarkers, List<FormQuestionResponse> formQuestionResponseList )
    {
        String baseUrl = AppPathService.getProdUrl(  _request );
        HashMap<Integer, FormQuestionResponse> formResponseListByEntryId = formResponseListToHashmap(formQuestionResponseList);
        String adminBaseUrl = "";
         if(StringUtils.isBlank(AppPropertiesService.getProperty( "lutece.admin.prod.url" )))
           {
               adminBaseUrl = AppPropertiesService.getProperty( "lutece.admin.prod.url" );
           }
         else
           {
                AppLogService.info( "lutece.admin.prod.url property not found" );
                adminBaseUrl = baseUrl;
           }
        for ( InfoMarker infoMarker : collectionInfoMarkers )
        {
            model.put( infoMarker.getMarker(), infoMarker.getValue() );
            if(infoMarker.getMarker().contains("position_"))
            {
                String position = infoMarker.getMarker().replace("position_", "");
                int positionInt = Integer.parseInt(position);
                List<String> responseValue = getResponseValue(formResponseListByEntryId.get(positionInt));

                String responseValueString = "";
                for (String response : responseValue)
                {
                    responseValueString += response + " ";
                }
                model.put( infoMarker.getMarker(), responseValueString );

            }
            if(infoMarker.getMarker().equals("url_admin_forms_response_detail"))
            {
                String linkMessage = I18nService.getLocalizedString( LINK_MESSAGE_BO, Locale.getDefault( ) );
                model.put( infoMarker.getMarker(), "<a href=\""+ adminBaseUrl+"/jsp/admin/plugins/forms/ManageDirectoryFormResponseDetails.jsp?view=view_form_response_details&id_form_response=" + _formResponse.getId( ) + "\">" + linkMessage + "</a>" );
            }
            if(infoMarker.getMarker().equals("url_fo_forms_response_detail"))
            {
                String linkMessage = I18nService.getLocalizedString( LINK_MESSAGE_FO, Locale.getDefault( ) );
                model.put( infoMarker.getMarker(), "<a href=\""+ baseUrl+"/jsp/site/Portal.jsp?page=formsResponse&view=formResponseView&id_response=" + _formResponse.getId( ) + "\">" + linkMessage + "</a>" );
            }
            if(infoMarker.getMarker().equals("creation_date"))
            {
                String creationDate = _formResponse.getCreation( ).toLocalDateTime().toString();
                String[] parts = creationDate.split("T");
                String date = parts[0];
                String time = parts[1];
             model.put( infoMarker.getMarker(), date + " " + time );
            }
            if(infoMarker.getMarker().equals("update_date"))
            {
                String updateDate = _formResponse.getUpdate( ).toLocalDateTime().toString();
                String[] parts = updateDate.split("T");
                String date = parts[0];
                String time = parts[1];
                model.put( infoMarker.getMarker(), date + " " + time );
            }
            if(infoMarker.getMarker().equals("status"))
            {
                if(_formResponse.isPublished()) {
                    String published = I18nService.getLocalizedString( PUBLISHED, Locale.getDefault( ) );
                model.put( infoMarker.getMarker(), published );
                } else {
                    String notPublished = I18nService.getLocalizedString( NOT_PUBLISHED, Locale.getDefault( ) );
                    model.put( infoMarker.getMarker(), notPublished );
                }
            }
            if(infoMarker.getMarker().equals("update_date_status"))
            {
                String updateDate = _formResponse.getUpdateStatus( ).toLocalDateTime().toString();
                String[] parts = updateDate.split("T");
                String date = parts[0];
                String time = parts[1];
                model.put( infoMarker.getMarker(), date + " " + time );
            }
        }
        return model;
    }

    /**
     * Gets the response value.
     *
     * @param formQuestionResponse
     *            the form question response
     * @return List<String> response value
     */
    public List<String> getResponseValue( FormQuestionResponse formQuestionResponse )
    {

        IEntryTypeService entryTypeService ;
        List<String> listResponseValue = new ArrayList<>( );
        if(formQuestionResponse != null && formQuestionResponse.getQuestion( ) != null && formQuestionResponse.getQuestion( ).getEntry( ) != null && formQuestionResponse.getEntryResponse() != null)
        {
            Entry entry = formQuestionResponse.getQuestion( ).getEntry( );
            entryTypeService = EntryTypeServiceManager.getEntryTypeService( entry );
            if ( entryTypeService instanceof AbstractEntryTypeComment )
            {
                return listResponseValue;
            }
            if ( entryTypeService instanceof EntryTypeTermsOfService )
            {
                boolean aggrement = formQuestionResponse.getEntryResponse( ).stream( )
                        .filter( response -> response.getField( ).getCode( ).equals( EntryTypeTermsOfService.FIELD_AGREEMENT_CODE ) )
                        .map( Response::getResponseValue ).map( Boolean::valueOf ).findFirst( ).orElse( false );

                if ( aggrement )
                {
                    listResponseValue.add( I18nService.getLocalizedString( KEY_LABEL_YES, I18nService.getDefaultLocale( ) ) );
                }
                else
                {
                    listResponseValue.add( I18nService.getLocalizedString( KEY_LABEL_NO, I18nService.getDefaultLocale( ) ) );
                }
                return listResponseValue;

            }
            for ( Response response : formQuestionResponse.getEntryResponse( ) )
            {
                if ((entryTypeService instanceof EntryTypeImage || entryTypeService instanceof EntryTypeCamera))
                {
                    PhysicalFile physicalFile = PhysicalFileHome.findByPrimaryKey(Integer.parseInt(response.getFile().getFileKey()));
                    if (response.getFile() != null)
                    {
                        if (physicalFile != null)
                        {
                            String encoded = Base64.getEncoder().encodeToString(physicalFile.getValue());
                            listResponseValue.add("<div style=\"margin-top: 10px; margin-bottom: 10px;\">"
                                    + "<center><img src=\"data:image/jpeg;base64, " + encoded + " \" width=\"500px\" height=\"auto\" /></center> "
                                    + "</div>"
                            );
                        } else {
                            listResponseValue.add(StringUtils.EMPTY);
                        }
                    }
                } else if (entryTypeService instanceof EntryTypeFile && response.getFile() != null)
                {
                    fr.paris.lutece.portal.business.file.File file = FileHome.findByPrimaryKey(Integer.parseInt(response.getFile().getFileKey()));
                    if (file != null) {
                        String space = "                ";
                        String textInFileLink = file.getTitle() + space + file.getSize() + "Bytes" + space + file.getMimeType() + space + file.getDateCreation().toLocalDateTime().toString();
                        String htmlDisplayFile = "<center><p>" + textInFileLink + "</p></center>";
                        listResponseValue.add(htmlDisplayFile);
                    }
                }
                else
                {
                    String strResponseValue = entryTypeService.getResponseValueForExport(entry, null, response, I18nService.getDefaultLocale());
                    if (strResponseValue != null) {
                        listResponseValue.add(strResponseValue);
                    }
                }
            }
        }
        if(listResponseValue.isEmpty())
        {
            listResponseValue.add(StringUtils.EMPTY);
        }
        return listResponseValue;
    }

}
