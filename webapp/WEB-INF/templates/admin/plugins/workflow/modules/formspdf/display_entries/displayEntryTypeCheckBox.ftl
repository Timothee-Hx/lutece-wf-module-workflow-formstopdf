<#--
Macro: displayEntryTypeCheckBox
Description: Display an entry of type Check Box
Parameters: entry, list_responses
-->
<#macro displayEntryTypeCheckBox entry, list_responses >
	<div style="display: flex; flex-wrap: wrap;">
		<div style="flex: 0 0 75%; max-width: 75%;">
			<#if list_responses?has_content>
				<#list list_responses as response>
					<p>
						<#if response.field??>${response.field.title!''}</#if>
					</p>
				</#list>
			</#if>
		</div>
	</div>
</#macro>
