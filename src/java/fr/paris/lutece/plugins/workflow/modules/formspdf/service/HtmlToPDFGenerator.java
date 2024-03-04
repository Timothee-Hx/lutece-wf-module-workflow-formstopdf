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
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;



import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponse;
import fr.paris.lutece.plugins.forms.business.FormQuestionResponseHome;
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
    public HtmlToPDFGenerator( String fileName, String fileDescription, FormResponse formResponse, FormsPDFTaskTemplate formsPDFTaskTemplate )
    {
        super( fileName, fileDescription, formResponse, formsPDFTaskTemplate );
    }

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
    public HtmlToPDFGenerator( String fileName, String fileDescription, FormResponse formResponse, FormsPDFTaskTemplate formsPDFTaskTemplate, String baseUrl)
    {
        super( fileName, fileDescription, formResponse, formsPDFTaskTemplate, baseUrl);
    }

    private static final boolean ZIP_EXPORT = Boolean.parseBoolean( AppPropertiesService.getProperty( "workflow-formspdf.export.pdf.zip", "false" ) );
    private static final String CONSTANT_MIME_TYPE_PDF = "application/pdf";
    private static final String CONSTANT_FORM_TITLE = "form_title";
    private static final String EXTENSION_PDF = ".pdf";
    private static final String TEMPLATE_ENTRY_IMAGE = "admin/plugins/workflow/modules/formspdf/entries/image.html";
    private static final String TEMPLATE_ENTRY_FILE = "admin/plugins/workflow/modules/formspdf/entries/file.html";
    private static final String TEMPLATE_ENTRY_TERMS_OF_SERVICE = "admin/plugins/workflow/modules/formspdf/entries/terms_of_service.html";
    private static final String TEMPLATE_PUBLISHED_STATUS = "admin/plugins/workflow/modules/formspdf/published_status.html";
    private static final String MARK_ENTRY_IMAGE = "mark_entry_image";
    private static final String MARK_FILE_TITLE = "mark_file_title";
    private static final String MARK_FILE_SIZE = "mark_file_size";
    private static final String MARK_FILE_MIME_TYPE = "mark_file_mime_type";
    private static final String MARK_FILE_DATE_CREATION = "mark_file_date_creation";
    private static final String MARK_TERMS_OF_SERVICE_AGREED = "terms_of_service_agreed";
    private static final String MARK_PUBLISHED_STATUS = "published_status";


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

    /**
     * Form response list to hashmap.
     * This method is used to get a list of form question response to a hashmap, including the responses with iterations in the form
     * @param formQuestionResponseList
     *           the form question response list
     *           @return the hashmap
     */
    public  HashMap<Integer, FormQuestionResponse>  formResponseListToHashmap( List<FormQuestionResponse>  formQuestionResponseList)
    {
        HashMap<Integer, FormQuestionResponse> formResponseListByEntryId = new HashMap<>();
        for (int i = 0; i < formQuestionResponseList.size(); i++)
        {
            int idEntry = formQuestionResponseList.get(i).getQuestion().getIdEntry();
            if(formResponseListByEntryId.containsKey(idEntry))
            {
                // This is to add the response to the hashmap when there are iterations in the form (one than one time the same entry)
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
        model = markersToModel(model, collectionNotifyMarkers, formQuestionResponseList, form.getTitle());
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
    private void markersToModel( Map<String, Object> model, Collection<InfoMarker> collectionInfoMarkers )
    {
        for ( InfoMarker infoMarker : collectionInfoMarkers )
        {
            model.put( infoMarker.getMarker(), infoMarker.getValue() );
        }
    }

    private Map<String, Object> markersToModel( Map<String, Object> model, Collection<InfoMarker> collectionInfoMarkers, List<FormQuestionResponse> formQuestionResponseList, String formTitle )
    {
        HashMap<Integer, FormQuestionResponse> formResponseListByEntryId = formResponseListToHashmap(formQuestionResponseList);
        String adminBaseUrl = _baseUrl;
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
                model.put( infoMarker.getMarker(), adminBaseUrl+"/jsp/admin/plugins/forms/ManageDirectoryFormResponseDetails.jsp?view=view_form_response_details&id_form_response=" + _formResponse.getId( ) );
            }
            if(infoMarker.getMarker().equals("url_fo_forms_response_detail"))
            {
                model.put( infoMarker.getMarker(), _baseUrl+"/jsp/site/Portal.jsp?page=formsResponse&view=formResponseView&id_response=" + _formResponse.getId( ) );
            }
            if(infoMarker.getMarker().equals("creation_date"))
            {
             model.put( infoMarker.getMarker(), _formResponse.getCreation( ) );
            }
            if(infoMarker.getMarker().equals("update_date"))
            {
                model.put( infoMarker.getMarker(),  _formResponse.getUpdate( ) );
            }
            if(infoMarker.getMarker().equals("status"))
            {
                Map<String, Boolean> modelPublishedStatus = new HashMap<>();
                if(_formResponse.isPublished()) {
                    modelPublishedStatus.put( MARK_PUBLISHED_STATUS, true );
                } else {
                    modelPublishedStatus.put( MARK_PUBLISHED_STATUS, false );
                }
                HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_PUBLISHED_STATUS, Locale.getDefault(), modelPublishedStatus );
                model.put( infoMarker.getMarker(), template.getHtml() );
            }
            if(infoMarker.getMarker().equals("update_date_status"))
            {
                model.put( infoMarker.getMarker(),  _formResponse.getUpdateStatus( ) );
            }
            if(infoMarker.getMarker().equals(CONSTANT_FORM_TITLE))
            {
                model.put( infoMarker.getMarker(), formTitle);
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
                Map<String, Boolean> model = new HashMap<>();
                if ( aggrement )
                {
                    model.put(MARK_TERMS_OF_SERVICE_AGREED, true);
                }
                else
                {
                    model.put(MARK_TERMS_OF_SERVICE_AGREED, false);
                }
                HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_ENTRY_TERMS_OF_SERVICE, Locale.getDefault(), model );
                listResponseValue.add( template.getHtml());
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
                            Map<String, String> model = new HashMap<>();
                            model.put(MARK_ENTRY_IMAGE, "data:image/jpeg;base64,"+encoded);
                            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_ENTRY_IMAGE, Locale.getDefault(), model );
                            listResponseValue.add(template.getHtml() );
                        } else {
                            listResponseValue.add(StringUtils.EMPTY);
                        }
                    }
                } else if (entryTypeService instanceof EntryTypeFile && response.getFile() != null)
                {
                    fr.paris.lutece.portal.business.file.File file = FileHome.findByPrimaryKey(Integer.parseInt(response.getFile().getFileKey()));
                    if (file != null) {
                        Map<String, String> model = new HashMap<>();
                        model.put(MARK_FILE_TITLE, file.getTitle());
                        model.put(MARK_FILE_SIZE, file.getSize() + " " + "bytes");
                        model.put(MARK_FILE_MIME_TYPE, file.getMimeType());
                        model.put(MARK_FILE_DATE_CREATION, file.getDateCreation().toLocalDateTime().toString());
                        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_ENTRY_FILE, Locale.getDefault(), model );
                        listResponseValue.add(template.getHtml());
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
