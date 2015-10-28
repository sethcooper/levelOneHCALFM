function turn_off_visibility(tbid) {document.getElementById(tbid).style.display="none";}

function turn_on_visibility(tbid) {document.getElementById(tbid).style.display="table";}

function toggle_visibility(tbid) {
  if(document.getElementById(tbid).style.display != "table") {document.getElementById(tbid).style.display = "table";}
  else {document.getElementById(tbid).style.display="none";}
}


function copyContents(element, tgt) {
  tgt.appendChild(element);
}

function makecheckbox(checkbox, parameter) {
  document.getElementById(checkbox).innerHTML = '<input id=\"globalParameterCheckBox' + document.getElementById(parameter).getAttribute("name").substring(20) + '\" type=\"checkbox\" onclick=\"onClickGlobalParameterCheckBox(\'' + document.getElementById(parameter).getAttribute("name").substring(20) + '\', \'' + parameter + '\')\">';
}

function removeduplicatecheckbox(parameter) {
  document.getElementById("globalParameterCheckBox"+document.getElementById(parameter).getAttribute("name").substring(20)).parentNode.removeChild(document.getElementById("globalParameterCheckBox"+document.getElementById(parameter).getAttribute("name").substring(20)));
}

function hideduplicatefield(parameter) {
  document.getElementById("globalParameterName"+document.getElementById(parameter).getAttribute("name").substring(20)).parentNode.style.display="none";
}

function showsupervisorerror() {
  var errMessage = document.getElementById("SUPERVISOR_ERROR").value;
  if (errMessage!="not set" && errMessage!="") {
    document.getElementById("supervisorRow").style.display="";
    document.getElementById("supervisorError").innerHTML = errMessage;
  }
}

// The scripts below use jQuery.
$(document).ready(function() {
  var initcolor= $('#currentState').text();
  $('#currentState').attr("class", "hcal_control_"+initcolor);
  $('#commandParameterCheckBox').attr("onclick", "onClickCommandParameterCheckBox(); toggle_visibility('Blork');");
    if (currentState == "Initial" || currentState == "Initializing" || currentState == "Halted") {
      $('#newRUN_CONFIG_SELECTEDcheckbox :checkbox').show();
      $('#newMASKED_RESOURCEScheckbox :checkbox').show();
    }
    else {
      $('#newRUN_CONFIG_SELECTEDcheckbox :checkbox').hide();
      $('#newMASKED_RESOURCEScheckbox :checkbox').hide();
    }
});

$(document).ready(function() {
  setInterval(function() {
    var currentState = $('#currentState').text();
    $('#currentState').attr("class", "hcal_control_"+currentState);
    if (currentState == "Initial" || currentState == "Initializing" || currentState == "Halted") {
      $('#newRUN_CONFIG_SELECTEDcheckbox :checkbox').show();
      $('#newMASKED_RESOURCEScheckbox :checkbox').show();
    }
    else {
      $('#newRUN_CONFIG_SELECTEDcheckbox :checkbox').hide();
      $('#newMASKED_RESOURCEScheckbox :checkbox').hide();
    }
    $('#commandParameterCheckBox').attr("onclick", "onClickCommandParameterCheckBox(); toggle_visibility('Blork');");
  }, 750);
});

function setProgress(progress) {
   var progressBarWidth =progress*$(".container").width()/ 100;
   $(".progressbar").width(progressBarWidth).html(progress + "% &nbsp; &nbsp;");
}

function mirrorSelection() {
  $('#CFGSNIPPET_KEY_SELECTED').val($('#dropdown option:selected').text());
  $('#RUN_CONFIG_SELECTED').val($('#dropdown option:selected').val());
  //$('#MASKED_RESOURCES').val($('#dropdown option:selected').attr("maskedresources"));
}

function checkCheckboxes() {
  $('#newCFGSNIPPET_KEY_SELECTEDcheckbox :checkbox').prop('checked', true);
  $('#newRUN_CONFIG_SELECTEDcheckbox :checkbox').prop('checked', true);
  $('#newMASKED_RESOURCEScheckbox :checkbox').prop('checked', true);
}

function undisable(paramNumber) {
  var parameterInputBoxID = "#globalParameterName".concat(paramNumber);
  $(parameterInputBoxID).removeAttr("disabled");
}
function clickboxes() {
  $('#newCFGSNIPPET_KEY_SELECTEDcheckbox :checkbox').click();
  $('#newRUN_CONFIG_SELECTEDcheckbox :checkbox').click();
  $('#newMASKED_RESOURCEScheckbox :checkbox').click();
}

function makedropdown(availableRunConfigs) {
  availableRunConfigs = availableRunConfigs.substring(0, availableRunConfigs.length - 1);
  var array = availableRunConfigs.split(';');
  var dropdownoption = "<select id='dropdown' > <option value='' maskedresources=''> --SELECT-- </option>";

  for ( var i = 0, l = array.length; i < l; i++ ) {
    var option = array[i].split(':');
    dropdownoption = dropdownoption + "<option value='" + option[1] + "' maskedresources='" + option[2] + ";'>" + option[0] + "</option>";
    // TODO: handle multiple masks. want to split maskedresources on something besides semicolons and then iterate over them inserting semicolons
    //dropdownoption = dropdownoption + "<option value='" + option[1] + "' maskedresources='" + option[2] + "'>" + option[0] + "</option>";
  }
  dropdownoption = dropdownoption+"</select>";
  $('#dropdowndiv').html(dropdownoption);
  var cfgSnippetKeyNumber = $('#CFGSNIPPET_KEY_SELECTED').attr("name").substring(20)
  var cfgSnippetArgs = "'" + cfgSnippetKeyNumber + "', 'CFGSNIPPET_KEY_SELECTED'";
  var masterSnippetNumber = $('#RUN_CONFIG_SELECTED').attr("name").substring(20)
  var masterSnippetArgs = "'" + masterSnippetNumber + "', 'RUN_CONFIG_SELECTED'";
  var maskedResourcesNumber = $('#MASKED_RESOURCES').attr("name").substring(20)
  var maskedResourcesArgs = "'" + maskedResourcesNumber + "', 'MASKED_RESOURCES'";
  var onchanges="onClickGlobalParameterCheckBox("+ cfgSnippetArgs + "); onClickGlobalParameterCheckBox("+ masterSnippetArgs + "); onClickGlobalParameterCheckBox("+ maskedResourcesArgs + "); clickboxes(); mirrorSelection();";
  $('#dropdown').attr("onchange", onchanges);
}

function fillMask() {
  var allMasks = "";
  $('#masks :checked').each(function() {
    allMasks += $(this).val() + ";";
  });
  $('#MASKED_RESOURCES').val($('#MASKED_RESOURCES').val()+allMasks);
  $('#maskTest').html($('#MASKED_RESOURCES').val()+allMasks);
}

function makecheckboxes(availableResources) {
  availableResources = availableResources.substring(0, availableResources.length - 1);
  var array = availableResources.split(';');
  var maskDivContents = "<ul>";
  for ( var i = 0, l = array.length; i < l; i++ ) {
    var option = array[i].split(':');
    var checkbox = "<li><input type='checkbox' onchange='fillMask();' value='" + option + "'>" + option + "</li>";
    maskDivContents += checkbox;
  }
  maskDivContents += "</ul>";
  $('#masks').html(maskDivContents);
}


function hcalOnLoad() {
  onLoad();
  activate_relevant_table('AllParamTables');
  onClickCommandParameterCheckBox();
  removeduplicatecheckbox('CFGSNIPPET_KEY_SELECTED');
  removeduplicatecheckbox('RUN_CONFIG_SELECTED');
  removeduplicatecheckbox('MASKED_RESOURCES');
  removeduplicatecheckbox('NUMBER_OF_EVENTS');
  removeduplicatecheckbox('RUN_NUMBER');
  copyContents(CFGSNIPPET_KEY_SELECTED,newCFGSNIPPET_KEY_SELECTED);
  makecheckbox('newCFGSNIPPET_KEY_SELECTEDcheckbox', 'CFGSNIPPET_KEY_SELECTED');
  copyContents(RUN_CONFIG_SELECTED,newRUN_CONFIG_SELECTED);
  makecheckbox('newRUN_CONFIG_SELECTEDcheckbox', 'RUN_CONFIG_SELECTED');
  copyContents(MASKED_RESOURCES,newMASKED_RESOURCES);
  makecheckbox('newMASKED_RESOURCEScheckbox', 'MASKED_RESOURCES');
  copyContents(NUMBER_OF_EVENTS,newNUMBER_OF_EVENTS);
  makecheckbox('newNUMBER_OF_EVENTScheckbox', 'NUMBER_OF_EVENTS');
  copyContents(HCAL_EVENTSTAKEN,newHCAL_EVENTSTAKEN);
  copyContents(RUN_NUMBER,newRUN_NUMBER);
  makecheckbox('newRUN_NUMBERcheckbox', 'RUN_NUMBER');
  copyContents(HCAL_TIME_OF_FM_START,newHCAL_TIME_OF_FM_START);
  hideduplicatefield('CFGSNIPPET_KEY_SELECTED');
  hideduplicatefield('RUN_CONFIG_SELECTED');
  hideduplicatefield('MASKED_RESOURCES');
  hideduplicatefield('NUMBER_OF_EVENTS');
  hideduplicatefield('RUN_NUMBER');
  hideduplicatefield('HCAL_EVENTSTAKEN');
  hideduplicatefield('HCAL_TIME_OF_FM_START');
  removeduplicatecheckbox('USE_RESET_FOR_RECOVER');
  removeduplicatecheckbox('USE_PRIMARY_TCDS');
  getfullpath();
  showsupervisorerror();
}
