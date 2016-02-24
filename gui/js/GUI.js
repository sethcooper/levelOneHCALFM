/**
 * GUI class in OO JavaScript. Uses jQuery for document access and AJAX
 * requests. Could also be using the AJAXRequest library.
 * 
 * @constructor
 */
function GUI() {
}

/**
 * Attaches the GUI to a page.
 * 
 * @param {Document}
 *            document - The page's document.
 */
GUI.prototype.attach = function(document) {
	this.document = document;

	/*
	 * the groupID element is inserted into the JSP by the
	 * <rcms.control:configurationKeyRenderer [...] /> tag (as part of the
	 * FMPilotForm)
	 */
	this.groupID = $(this.document.getElementById('groupID')).val();

	/*
	 * the TIMESTAMP element is inserted into the JSP by the
	 * <rcms.notification:hiddenInputRenderer [...] /> tag and gets updated by
	 * the notification system
	 */
	this.timestampElement = $('#TIMESTAMP');
};

/**
 * The target URL for AJAX requests. By default, the current page's URL without
 * parameters is used.
 */
GUI.prototype.AJAX_URL = location.protocol + '//' + location.host + location.pathname;

/**
 * The name of the FM's GUI_COMMAND parameter.
 */
GUI.prototype.GUI_COMMAND_PARAMETER = 'GUI_COMMAND';

/**
 * The full name of the RCMS's StringT parameter type.
 */
GUI.prototype.PARAMETER_TYPE_STRING = 'rcms.fm.fw.parameter.type.StringT';

/**
 * The full name of the RCMS's IntegerT parameter type.
 */
GUI.prototype.PARAMETER_TYPE_INTEGER = 'rcms.fm.fw.parameter.type.IntegerT';

/**
 * The full name of the RCMS's BooleanT parameter type.
 */
GUI.prototype.PARAMETER_TYPE_BOOLEAN = 'rcms.fm.fw.parameter.type.BooleanT';

/**
 * Sets a string parameter in the FM.
 */
GUI.prototype.setFMStringParameter = function(parameterName, parameterValue) {
	this.setFMParameter(parameterName, parameterValue, this.PARAMETER_TYPE_STRING);
};

GUI.prototype.sendGUICommand = function(parameterValue) {
	this.setFMParameter(this.GUI_COMMAND_PARAMETER, parameterValue, this.PARAMETER_TYPE_STRING);
};

/**
 * Sets a parameter in the FM. This is the only required function to do so with
 * jQuery.
 * 
 * @param {String}
 *            parameterName - The parameter to update.
 * @param {Object}
 *            parameterValue - The page's document.
 * @param {String}
 *            parameterType - The parameter's ParameterType.
 */
GUI.prototype.setFMParameter = function(parameterName, parameterValue, parameterType) {
	var parameters = {};
	parameters['ACTION'] = 'SET_GLOBAL_PARAMETERS';
	parameters['COMMAND'] = '';
	parameters['TIMESTAMP'] = this.timestampElement.val();
	parameters['groupID'] = this.groupID;
	parameters['NO_RESPONSE'] = 'true';
	parameters['globalParameterName1'] = parameterName;
	parameters['globalParameterValue1'] = parameterValue;
	parameters['globalParameterType1'] = parameterType;
	$.post(this.AJAX_URL, parameters);
};