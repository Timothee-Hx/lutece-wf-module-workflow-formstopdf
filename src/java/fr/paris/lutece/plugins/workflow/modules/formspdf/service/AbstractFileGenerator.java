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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.CollectionUtils;

import fr.paris.lutece.plugins.filegenerator.service.IFileGenerator;
import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.FormResponse;
import fr.paris.lutece.plugins.forms.business.export.FormExportConfig;
import fr.paris.lutece.plugins.forms.business.export.FormExportConfigHome;
import fr.paris.lutece.plugins.forms.export.ExportServiceManager;
import fr.paris.lutece.plugins.workflow.modules.formspdf.business.FormsPDFTaskTemplate;
import fr.paris.lutece.util.file.FileUtil;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractFileGenerator implements IFileGenerator
{
    private static final String PATTERN_TIMESTAMP = "_yyyy-MM-dd-HH-mm-ss";

    // 4: size for file extension (.pdf, .zip, .csv)
    private static final int MAX_NAME_LENGTH = 250 - PATTERN_TIMESTAMP.length( );
    protected static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );

    protected final FormResponse _formResponse;
    protected final String _fileName;
    protected final String _fileDescription;
    protected final FormsPDFTaskTemplate _formsPDFTaskTemplate;

    private List<FormExportConfig> _configList = null;
    protected Form _form;
    protected HttpServletRequest _request;

    /**
     * Constructor
     *
     * @param _listFormResponseItems
     * @param request
     */
    protected AbstractFileGenerator(String fileName, String fileDescription, FormResponse formResponse, FormsPDFTaskTemplate formsPDFTaskTemplate, HttpServletRequest request)
    {
        _formResponse = formResponse;
        _fileName = StringUtils.substring( fileName, 0, MAX_NAME_LENGTH ) + LocalDateTime.now( ).format( DateTimeFormatter.ofPattern( PATTERN_TIMESTAMP ) );
        _fileDescription = fileDescription;
        _formsPDFTaskTemplate = formsPDFTaskTemplate;
        _request = request;
    }

    @Override
    public String getDescription( )
    {
        return _fileDescription;
    }

    /**
     * Generate file name.
     *
     * @param response
     *            the response
     * @return the string
     */
    protected String generateFileName( FormResponse response )
    {
        if ( CollectionUtils.isEmpty( _configList ) )
        {
            _configList = FormExportConfigHome.findByForm( response.getFormId( ) );
            _form = FormHome.findByPrimaryKey( response.getFormId( ) );
        }

        List<String> nameValues = ExportServiceManager.getInstance( ).generateNameComponents( _form, response, _configList );
        if ( CollectionUtils.isEmpty( nameValues ) )
        {
            nameValues.add( String.valueOf( response.getId( ) ) );
        }
        return FileUtil.normalizeFileName( nameValues.stream( ).collect( Collectors.joining( "_" ) ) );
    }
}
