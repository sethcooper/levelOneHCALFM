package rcms.fm.app.level1;

import rcms.fm.fw.EventHandlerException;
import rcms.fm.fw.user.UserActionException;
import rcms.fm.fw.user.UserErrorHandler;
import rcms.statemachine.definition.State;
import rcms.util.logger.RCMSLogger;
import rcms.errorFormat.CMS.CMSError;

/**
	* Error Event Handler class for HCAL Function Managers
	* 
	* @author Arno Heister
	*
	*/

public class HCALErrorHandler extends UserErrorHandler {

	static RCMSLogger logger = new RCMSLogger(HCALErrorHandler.class);

	protected HCALFunctionManager functionManager = null;

	public HCALErrorHandler() throws EventHandlerException {
		// this handler inherits UserErrorHandler so it is already registered for Error events
		
		// error handler
		//addAction(HCALStates.ERROR,"errorHandler");	
		
		addAction(State.ANYSTATE,"errorHandler");
			
	}

	public void init() throws rcms.fm.fw.EventHandlerException {
		functionManager = (HCALFunctionManager) getUserFunctionManager();
		logger.debug("[HCAL ErrorHandler] init() called: functionManager = " + functionManager );
	}

	public void errorHandler(Object obj) throws UserActionException {	

		String errMessage = "[HCAL ErrorHandler] The error handler got an event from: " + obj.getClass();		
		System.out.println(errMessage);		
		logger.debug(errMessage);

	}	

}
