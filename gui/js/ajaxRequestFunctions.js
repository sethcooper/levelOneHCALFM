function ajaxRequestSetFMParameter(paramName, paramValue, paramType) {
	// set the new value in the form element and update the FM.
	document.FMPilotForm.globalParameterName1.value = paramName;
	document.FMPilotForm.globalParameterValue1.value = paramValue;
	document.FMPilotForm.globalParameterType1.value = paramType;

	var form = document.getElementById('FMPilotForm');
	form.ACTION.value = 'SET_GLOBAL_PARAMETERS';
	form.NO_RESPONSE.value = 'true';
	AjaxRequest.submit(form);

	// reset selection to avoid that with the next POST the same command is send
	form.NO_RESPONSE.value = 'x';
	document.FMPilotForm.globalParameterName1.value = "x";
	document.FMPilotForm.globalParameterValue1.value = "x";
	document.FMPilotForm.globalParameterType1.value = "x";
}

function ajaxRequestSetFMStringParameter(paramName, paramValue) {
	ajaxRequestSetFMParameter(paramName, paramValue, 'rcms.fm.fw.parameter.type.StringT');
}