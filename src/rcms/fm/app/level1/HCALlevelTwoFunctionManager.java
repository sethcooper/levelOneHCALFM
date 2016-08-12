package rcms.fm.app.level1;

import java.util.Iterator;
import java.util.List;

import rcms.fm.fw.parameter.FunctionManagerParameter;
import rcms.fm.fw.parameter.type.StringT;
import rcms.fm.fw.user.UserActionException;
import rcms.fm.fw.user.UserFunctionManager;
import rcms.fm.resource.QualifiedResource;
import rcms.fm.resource.QualifiedResourceContainer;
import rcms.fm.resource.qualifiedresource.XdaqApplication;
import rcms.fm.resource.qualifiedresource.XdaqApplicationContainer;
import rcms.fm.resource.qualifiedresource.XdaqExecutive;
import rcms.statemachine.definition.State;
import rcms.statemachine.definition.StateMachineDefinitionException;
import rcms.util.logger.RCMSLogger;
import rcms.util.logsession.LogSessionConnector;

import rcms.resourceservice.db.Group;
import rcms.resourceservice.db.RSConnectorIF;
import rcms.resourceservice.db.resource.fm.FunctionManagerResource;

import rcms.utilities.runinfo.RunInfo;
import rcms.utilities.runinfo.RunInfoException;

import rcms.xdaqctl.XDAQParameter;
import net.hep.cms.xdaqctl.XDAQTimeoutException;
import net.hep.cms.xdaqctl.XDAQException;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
	* Function Machine to control a Level 2 HCAL Function Manager
	* 
	* @author Arno Heister
	*
	*/

public class HCALlevelTwoFunctionManager extends HCALFunctionManager {

	static RCMSLogger logger = new RCMSLogger(HCALlevelTwoFunctionManager.class);

	public HCALlevelTwoFunctionManager() {}

	public void init() throws StateMachineDefinitionException,rcms.fm.fw.EventHandlerException {

		super.init();

		// add event handler
		theEventHandler = new HCALlevelTwoEventHandler();
		addEventHandler(theEventHandler);
	}

	/**----------------------------------------------------------------------
	 * get supervisor error messages
	 */
  public String getSupervisorErrorMessage() {
    XDAQParameter pam = null;
		String supervisorError = "";
		for (QualifiedResource qr : containerhcalSupervisor.getApplications() ){
			try {
				pam =((XdaqApplication)qr).getXDAQParameter();
				pam.select(new String[] {"Partition", "overallErrorMessage","StateTransitionMessage"});
				pam.get();
				supervisorError = "(" + pam.getValue("Partition") + ") " + pam.getValue("overallErrorMessage") + "; transitionMessage=" + pam.getValue("StateTransitionMessage");
				getHCALparameterSet().put(new FunctionManagerParameter<StringT>("SUPERVISOR_ERROR", new StringT(supervisorError)));
			}
			catch (XDAQTimeoutException e) {
				String errMessage = "[HCAL " + FMname + "] Error! XDAQTimeoutException: getSupervisorErrorMessage(): couldn't get xdaq parameters";
				goToError(errMessage,e);
			}
			catch (XDAQException e) {
				String errMessage = "[HCAL " + FMname + "] Error! XDAQException: getSupervisorErrorMessage(): couldn't get xdaq parameters";
				goToError(errMessage,e);
			}
		}
    return supervisorError;
	}

}
