<#--
Macro: displayEntryTypeComment
Description: Display the comment of an entry
Parameters: entry, list_responses
-->
<#macro displayEntryTypeComment entry, list_responses >
		<div>
			<div>
				<#if entry.comment?exists>
						<p>${entry.comment}</p>
				</#if>
			</div>
		</div>
</#macro>
