package rcms.fm.app.level1;
 
import rcms.fm.fw.parameter.FunctionManagerParameter;
import rcms.fm.fw.parameter.type.IntegerT;
import rcms.fm.fw.parameter.type.StringT;
import rcms.fm.fw.user.UserActionException;
import rcms.fm.fw.user.UserEventHandler;
import rcms.stateFormat.StateNotification;
import rcms.statemachine.definition.State;
import rcms.util.logger.RCMSLogger;
import rcms.utilities.fm.task.TaskSequence;
 
 
/**
 * StateNotificationHandler for HCAL
 *
 * @author Seth I. Cooper
 */
public class HCALStateNotificationHandler extends UserEventHandler  {
 
 
    static RCMSLogger logger = new RCMSLogger(HCALStateNotificationHandler.class);
 
    HCALFunctionManager fm = null;
 
    TaskSequence taskSequence = null;
 
    Boolean isTimeoutActive = false;
 
    Thread timeoutThread = null;
 
//    public Boolean interruptedTransition = false;
    //this is active only in global mode..
 
    public HCALStateNotificationHandler() throws rcms.fm.fw.EventHandlerException {
        subscribeForEvents(StateNotification.class);
        addAnyStateAction("processNotice");
    }
 
 
    public void init() throws rcms.fm.fw.EventHandlerException {
        fm = (HCALFunctionManager) getUserFunctionManager();
    }
 
		//State notification callback
		public void processNotice( Object notice ) throws UserActionException {

			StateNotification notification = (StateNotification)notice;
			logger.warn("["+fm.FMname+"]: State notification received "+
					"from: " + notification.getFromState()
					+" to: " + notification.getToState());
      //
			String actualState = fm.getState().getStateString();
      logger.warn("["+fm.FMname+"]: FM is in state: "+actualState);

			if ( fm.getState().equals(HCALStates.ERROR) ) {
				//XXX SIC FIXME TODO add this
				//fm.forceParameterUpdate();
				return;
			}

			if ( notification.getToState().equals(HCALStates.ERROR.toString())) {

				String appName = "";
				try {
					appName = fm.findApplicationName( notification.getIdentifier() );
				} catch(Exception e){}
				String actionMsg = appName+"["+notification.getIdentifier()+"] is in Error";
				String errMsg =  actionMsg+"\nReported reason:\n "+notification.getReason();
				String errDetail = notification.getReason();
				//XXX FIXME SIC TODO
				//fm.addMsgToConsole(actionMsg);
				//                if ( _taskSequence != null ) actionMsg = "Stuck in "+_taskSequence.getDescription();
				fm.setAction(actionMsg);
				fm.fireEvent(HCALInputs.SETERROR);
				//XXX SIC FIXME TODO add this
				//fm.forceParameterUpdate();
				fm.sendCMSError(errMsg);
				//fm.setErrorDetail(errDetail);
				taskSequence = null;
				setTimeoutThread(false);
				return;
			}

			//XXX FIXME SIC TODO
			//if ( notification.getToState().equals(HCALStates.XDAQ_ERRORREPORT.toString()) ) {
			//    fm.setWarning(notification.getReason());
			//} else if ( notification.getToState().equals(HCALStates.XDAQ_CRASHED.toString()) ) {
			//    String errMsg = "Application Crash detected:\n";
			//            errMsg += "URI: "+notification.getIdentifier()+"\n";
			//            errMsg += "Reason: "+notification.getReason();
			//            fm.setError(errMsg);
			//    if ( notification.getIdentifier().contains("urn:xdaq-application:lid=0"))
			//            try {
			//                    fm.getExecManager().runPostMortemCheck(notification.getIdentifier());
			//            } catch ( Exception e ) {
			//                    logger.error("PostMortem check failed",e);
			//            }
			//    }

			// process the notification from the FM when initializing
			if ( fm.getState().equals(HCALStates.INITIALIZING) ) {

        // ignore notifications to HALTING (like from TCDS apps)
				if ( notification.getToState().equals(HCALStates.HALTING.toString()) ) {
					String msg = "HCAL is initializing ";
					fm.setAction(msg);
					logger.info(msg);
					return;
			  }
        // for level2's, we fire the set halt at the end of initAction unless there's an error, so we don't care about any notifications
        // for the level1, in this case we need to compute the new state
				else if ( notification.getToState().equals(HCALStates.HALTED.toString()) ) {
          // if it has children FMs, it's a level-1
					if(!fm.containerFMChildren.isEmpty()) {
						logger.warn("HCALStateNotificationHandler: got notification to HALTED while FM is in INITIALIZING and this is a level-1 FM: call computeNewState()");
						// calculate the updated state
						fm.theEventHandler.computeNewState(notification);
					}
				}
		  }

			// process the notification from the FM when halting
			if ( fm.getState().equals(HCALStates.HALTING) ) {

				// ignore notifications to HALTING (like from TCDS apps)
				if ( notification.getToState().equals(HCALStates.HALTING.toString()) ) {
					String msg = "HCAL is halting ";
					fm.setAction(msg);
					logger.info(msg);
					return;
				}
				else if ( notification.getToState().equals(HCALStates.HALTED.toString()) ) {
					logger.warn("HCALStateNotificationHandler: got notification to HALTED while FM is in HALTING: call computeNewState()");
					// calculate the updated state
					fm.theEventHandler.computeNewState(notification);
				}
			}



			// process the notification from the FM when configuring
			if ( fm.getState().equals(HCALStates.CONFIGURING) ) {

				if ( notification.getToState().equals(HCALStates.CONFIGURING.toString()) ) {

					String services = notification.getReason().trim();
					if ( services == null | services.length() == 0 ) return;

					//String transMsg = String.format( "services ["+fm.getConfiguredServices()+"] done : ["+services+"] in progress");
					//fm.setTransitionMessage( transMsg );
					//fm.addConfiguredServices(services);
					String msg = "HCAL is configuring "+services;
					fm.setAction(msg);
					logger.info(msg);
					//XXX FIXME SIC TODO
					//fm.addMsgToConsole(msg);

					setTimeoutThread(true);
					return;
					//XXX SIC FIXME TODO ADD ERROR STATE
					//} else if ( notification.getToState().equals(HCALStates.XDAQ_CRASHED.toString()) ) {
					//        String errMsg = "Application Crash detected:\n";
					//        errMsg += "URI: "+notification.getIdentifier()+"\n";
					//        errMsg += "Reason: "+notification.getReason();
					//    fm.sendCMSError(errMsg,logger);
					//    fm.setAction(" ");
					//    setTimeoutThread(false);
					//    fm.forceParameterUpdate();
					//    fm.fireEvent(HCALInputs.SETERROR);
					// 
					//        return;
					//}
			  }
		  }

			// process the notification from the FM when starting
			if ( fm.getState().equals(HCALStates.STARTING) ) {

				if ( notification.getToState().equals(HCALStates.STARTING.toString()) ) {

					String services = notification.getReason().trim();
					if ( services == null | services.length() == 0 ) return;

					//String transMsg = String.format( "services ["+fm.getConfiguredServices()+"] done : ["+services+"] in progress");
					//fm.setTransitionMessage( transMsg );
					//fm.addConfiguredServices(services);
					String msg = "HCAL is starting "+services;
					fm.setAction(msg);
					logger.info(msg);
					//XXX FIXME SIC TODO
					//fm.addMsgToConsole(msg);

					setTimeoutThread(true);
					return;
					//XXX SIC FIXME TODO ADD ERROR STATE
					//} else if ( notification.getToState().equals(HCALStates.XDAQ_CRASHED.toString()) ) {
					//        String errMsg = "Application Crash detected:\n";
					//        errMsg += "URI: "+notification.getIdentifier()+"\n";
					//        errMsg += "Reason: "+notification.getReason();
					//    fm.sendCMSError(errMsg,logger);
					//    fm.setAction(" ");
					//    setTimeoutThread(false);
					//    fm.forceParameterUpdate();
					//    fm.fireEvent(HCALInputs.SETERROR);
					// 
					//        return;
					//}
			  }
		  }

			if(taskSequence == null) {

				setTimeoutThread(false);
				String infomsg = "Received a State Notification while taskSequence is null \n";
				logger.warn(infomsg);

				//if (fm.isGlobal()) {
				//        //XXX SIC FIXME TODO not sure what to do here
				//        //// check if we are stepping
				//        //String toState = notification.getToState();
				//        //if ( toState.equals(HCALStates.XDAQ_STEPPING.toString())
				//        //                || toState.equals(HCALStates.RUNNING.toString()) ) {
				//        //        String msg = "HCALSupervisor is "+toState;
				//        //        fm.addMsgToConsole(msg);
				//        //        fm.setAction(msg);
				//        //} else if ( toState.equals(HCALStates.XDAQ_RUNNINGSEU.toString()) ) {
				//        //        String msg = "HCALSupervisor is "+toState+", let's sync!";
				//        //        fm.addMsgToConsole(msg);
				//        //        fm.setAction(msg);
				//        //        fm.fireEvent(HCALInputs.SETRUN_SOFTERROR);
				//        //} else {
				//        //    String errMsg = "Application performed unexpected state change to state "+toState+"\n";
				//        //errMsg += "URI: "+notification.getIdentifier()+"\n";
				//        //errMsg += "Reason: "+notification.getReason();
				//        //    fm.sendCMSError(errMsg,logger);
				//        //    fm.setAction(" ");
				//        //    fm.fireEvent(HCALInputs.SETERROR);
				//        //    fm.forceParameterUpdate();
				//        //}
				//    return;
				//} else {
				logger.debug("FM is in local mode");
				logger.warn("taskSequence==null; computeNewState");
				// calculate the updated state
				fm.theEventHandler.computeNewState(notification);
				//logger.debug("HCALFM is in state "+ fm.getState());

				////if state is already an error...
				//if (fm.getState() == new State("Error"))
				//    //fm.forceParameterUpdate();
				//    return;
				//}

				////sync with HCAL Supervisor in local mode
				//    String toState = notification.getToState();
				//if  (toState.equals(HCALStates.HALTED.toString())) {
				//        fm.fireEvent(HCALInputs.SETHALT);
				//        //fm.getParameterSet().put(new FunctionManagerParameter<IntegerT>
				//        //    (HCALParameters.RUN_NUMBER, new IntegerT(0) ));
				//} else if (toState.equals(HCALStates.CONFIGURED.toString())) {
				//    //String transMsg = String.format( "services configured ["+fm.getConfiguredServices()+"]");
				//    //fm.setTransitionMessage( transMsg );
				//    fm.fireEvent(HCALInputs.SETCONFIGURE);
				//    //fm.getParameterSet().put(new FunctionManagerParameter<IntegerT>
				//    //        (HCALParameters.RUN_NUMBER, new IntegerT(HCALParameters.NULL_RUN_NUMBER) ));
				//    //fm.forceParameterUpdate();
				//} else if  (toState.equals(HCALStates.RUNNING.toString())) {
				//    fm.fireEvent(HCALInputs.SETSTART);
				//} else if  (toState.equals(HCALStates.PAUSED.toString())) {
				//    fm.fireEvent(HCALInputs.SETPAUSE);
				//} //else if (toState.equals(HCALStates.XDAQ_RUNNINGSEU.toString())) {
				//  //          fm.fireEvent(HCALInputs.SETRUN_SOFTERROR);
				////}
				////fm.forceParameterUpdate();
				return;
				//}
		}


		try {
			if( taskSequence.isCompleted() ) {
				logger.warn("Transition completed");
				completeTransition();
			} else {
				logger.warn("[SethLog] Start executing: "+taskSequence.getDescription());
				taskSequence.startExecution();
				//                fm.setAction("Executing: "+_taskSequence.getDescription());
				//                logger.debug("_taskSequence status after a second startExecution: "+_taskSequence.isCompleted() );
				logger.warn("taskSequence not reported complete");
			}
		} catch (Exception e){
			taskSequence = null;
			String errmsg = "Exception while stepping to the next task: "+e.getMessage();
			fm.sendCMSError(errmsg);
			fm.setAction(" ");
			setTimeoutThread(false);
			//fm.forceParameterUpdate();
			fm.fireEvent(HCALInputs.SETERROR);
		}
}
 
    /*--------------------------------------------------------------------------------
     *
     */
    protected void executeTaskSequence( TaskSequence taskSequence ) {
 
        this.taskSequence = taskSequence;
   
        State SequenceState =  taskSequence.getState();
        State FMState = fm.getState();
        if ( SequenceState != FMState ) {
            String errmsg = "taskSequence does not belong to this state \n " +
                "Function Manager state = " + fm.getState() +
                "\n while taskSequence is for state = " + taskSequence.getState();
 
            taskSequence = null;
            fm.sendCMSError(errmsg);
            fm.setAction(" ");
            fm.fireEvent(HCALInputs.SETERROR);
            //fm.forceParameterUpdate();
            setTimeoutThread(false);
            return;
        }
 
        try {
            taskSequence.startExecution();
 
            logger.warn("started execution of taskSequence");
            setTimeoutThread(true);
            try {
                fm.getParameterSet().get(HCALParameters.ACTION_MSG)
                    .setValue(new StringT(""+taskSequence.getDescription()));
 
                //XXX SIC FIXME TODO add this
                //fm.getParameterSet().get(HCALParameters.COMPLETION)
                //    .setValue(new StringT(""+taskSequence.completion()));
 
            } catch (Exception e) {
                logger.warn("failed to set action or completion info parameter");
            }
 
        } catch (Exception e){
            taskSequence = null;
            String errmsg = "process notice error: "+e.getMessage();
            fm.sendCMSError(errmsg);
            fm.setAction(" ");
            //fm.forceParameterUpdate();
            fm.fireEvent(HCALInputs.SETERROR);
        }
    }
 
    /*--------------------------------------------------------------------------------
     *
     */
    protected void completeTransition() throws UserActionException, Exception {
 
        State FMState = fm.getState();
 
				//XXX SIC FIXME TODO add this
        //fm.getParameterSet().get(HCALParameters.COMPLETION).setValue(new StringT(""));
        fm.setAction("Transition Completed");
				//XXX SIC FIXME TODO add this
        //fm.forceParameterUpdate();
 
        //if in this state, execute a function to get Key Vector
        //if (taskSequence.getCompletionEvent().equals(HCALInputs.SETHALT)
        //        && ( FMState.equals(HCALStates.INITIALIZING) || FMState.equals(HCALStates.RESETTING)) )
        //    fm.getEventHandler().fetchSupervisorPars();
       
        if (taskSequence.getCompletionEvent().equals(HCALInputs.SETCONFIGURE) ) {
            //String transMsg = String.format( "services configured ["+fm.getConfiguredServices()+"]");
            //fm.setTransitionMessage( transMsg );
        }
 
        //fm.setTransitionEndTime();
        //fm.getEventHandler().updateCompletedAction();
				//XXX SIC FIXME TODO add this
        //fm.forceParameterUpdate();
        setTimeoutThread(false);
				logger.warn("completeTransition: fire taskSequence completion event "+taskSequence.getCompletionEvent().toString());
        fm.fireEvent(taskSequence.getCompletionEvent());
        taskSequence = null;
 
    }
 
    /*--------------------------------------------------------------------------------
     *
     */
    public void setTimeoutThread(Boolean action) {
 
        if (timeoutThread!=null) {
            try {
                isTimeoutActive = false;
                timeoutThread.interrupt();
                timeoutThread=null;
            } catch (Exception e) {
                logger.error("couldn't destroy timer");
                isTimeoutActive = false;
                return;
            }
        }
        if (action==false) {
            isTimeoutActive=false;
            return;
        } else {
            isTimeoutActive = true;
            timeoutThread = new Thread( new Runnable()
            //Thread watchThread = new Thread( new Runnable()
                {
                    //int milliSecondSleepTime = 1000*fm.getEventHandler().getTimeout();
                    int milliSecondSleepTime = 1000*240; // 4 mins timeout
 
                    public void run() {    
                        try {
                            Thread.sleep(this.milliSecondSleepTime);
                            if (isTimeoutActive) {
                                //CLEANUP and set error
                                String errmsg = "Application transition timeout error";
                                fm.sendCMSError(errmsg);
                                fm.setAction(" ");
																//XXX SIC FIXME TODO add this
                                //fm.forceParameterUpdate();
                                fm.fireEvent(HCALInputs.SETERROR);
                                taskSequence = null;
                                isTimeoutActive=false;
                                return;
                            }
                        }
                        catch (InterruptedException ie) {
                        }
                        catch (Exception e) {
                            logger.error( "Exception in timeout HCALFM thread");
                        }
                    }
                } );
   
 
            //Sets the thread's priority to the minimum value.
            timeoutThread.setPriority(Thread.MIN_PRIORITY);
            //Starts the thread.
            timeoutThread.start();
            return;
        }
    }
 
}
