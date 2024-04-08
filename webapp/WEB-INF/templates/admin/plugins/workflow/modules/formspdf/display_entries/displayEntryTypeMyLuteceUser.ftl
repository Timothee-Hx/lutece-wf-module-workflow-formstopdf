<#--
Macro: displayEntryTypeMyLuteceUser
Description: Display an entry of type MyLuteceUser, the plugin MyLuteceUser must be installed
Parameters: entry, list_responses
-->
<#macro displayEntryTypeMyLuteceUser entry, list_responses >
				<#if list_responses?has_content>
					<#list list_responses as response>
						<p>
							<#if form_response?? && response.entry??>
								<@link href='${base_url!""}jsp/admin/plugins/forms/DoVisualisationMyLuteceUser.jsp?id_form_response=${form_response.id}&amp;id_entry=${response.entry.idEntry}'>
									${response.toStringValueResponse!''}
								</@link>
							<#else>
								${response.toStringValueResponse!''}
							</#if>
						</p>
					</#list>
				</#if>
</#macro>
