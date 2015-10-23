package rcms.fm.app.level1;


import rcms.fm.fw.parameter.CommandParameter;
import rcms.fm.fw.parameter.ParameterException;
import rcms.fm.fw.parameter.ParameterSet;
import rcms.fm.fw.parameter.type.IntegerT;
import rcms.fm.fw.parameter.type.BooleanT;
import rcms.fm.fw.parameter.type.StringT;
import rcms.fm.fw.user.UserStateMachineDefinition;
import rcms.statemachine.definition.State;
import rcms.statemachine.definition.StateMachineDefinitionException;

/**
 * This class defines the Finite State Machine for HCAL Function Managers
 *
 * @author Arno Heister
 */

public class HCALStateMachineDefinition extends UserStateMachineDefinition {

  public HCALStateMachineDefinition() throws StateMachineDefinitionException {
    //
    // Defines the States for this Finite State Machine.
    //

    // steady states
    addState(HCALStates.INITIAL);
    addState(HCALStates.HALTED);
    addState(HCALStates.CONFIGURED);
    addState(HCALStates.RUNNING);
    addState(HCALStates.PAUSED);
    addState(HCALStates.ERROR);
    addState(HCALStates.TTSTEST_MODE);

    // transitional states
    addState(HCALStates.INITIALIZING);
    addState(HCALStates.CONFIGURING);
    addState(HCALStates.HALTING);
    addState(HCALStates.STOPPING);
    addState(HCALStates.PAUSING);
    addState(HCALStates.RESUMING);
    addState(HCALStates.STARTING);
    addState(HCALStates.RECOVERING);
    addState(HCALStates.RESETTING);
    addState(HCALStates.TESTING_TTS);
    addState(HCALStates.PREPARING_TTSTEST_MODE);
    addState(HCALStates.COLDRESETTING);

    //
    // Defines the Initial state.
    //
    setInitialState(HCALStates.INITIAL);

    //
    // Defines the Inputs (Commands) for this Finite State Machine.
    //
    addInput(HCALInputs.INITIALIZE);
    addInput(HCALInputs.CONFIGURE);
    addInput(HCALInputs.START);
    addInput(HCALInputs.PAUSE);
    addInput(HCALInputs.RESUME);
    addInput(HCALInputs.HALT);
    addInput(HCALInputs.STOP);
    addInput(HCALInputs.RECOVER);
    addInput(HCALInputs.RESET);
    addInput(HCALInputs.SETERROR);
    addInput(HCALInputs.TTSTEST_MODE);
    addInput(HCALInputs.TEST_TTS);
    addInput(HCALInputs.COLDRESET);

    // The SETERROR Input moves the FSM in the ERROR State.
    // This command is not allowed from the GUI.
    // It is instead used inside the FSM callbacks.
    HCALInputs.SETERROR.setVisualizable(false);

    // invisible commands needed for fully asynchronous behaviour
    addInput(HCALInputs.SETCONFIGURE);
    addInput(HCALInputs.SETSTART);
    addInput(HCALInputs.SETPAUSE);
    addInput(HCALInputs.SETRESUME);
    addInput(HCALInputs.SETHALT);
    addInput(HCALInputs.SETINITIAL);
    addInput(HCALInputs.SETRESET);
    addInput(HCALInputs.SETTESTING_TTS);
    addInput(HCALInputs.SETTTSTEST_MODE);
    addInput(HCALInputs.SETCOLDRESET);

    // make these invisible
    HCALInputs.SETCONFIGURE.setVisualizable(false);
    HCALInputs.SETSTART.setVisualizable(false);
    HCALInputs.SETPAUSE.setVisualizable(false);
    HCALInputs.SETRESUME.setVisualizable(false);
    HCALInputs.SETHALT.setVisualizable(false);
    HCALInputs.SETINITIAL.setVisualizable(false);
    HCALInputs.SETRESET.setVisualizable(false);
    HCALInputs.SETTTSTEST_MODE.setVisualizable(false);
    HCALInputs.SETCOLDRESET.setVisualizable(false);

    //
    // Define command parameters.
    // These are then visible in the default GUI.
    //

    // define parameters for tts testing command
    //
    CommandParameter<IntegerT> ttsTestFedId = new CommandParameter<IntegerT>(HCALParameters.TTS_TEST_FED_ID, new IntegerT(-1));
    CommandParameter<StringT> ttsTestMode = new CommandParameter<StringT>(HCALParameters.TTS_TEST_MODE, new StringT(""));
    CommandParameter<StringT> ttsTestPattern = new CommandParameter<StringT>(HCALParameters.TTS_TEST_PATTERN, new StringT(""));
    CommandParameter<IntegerT> ttsTestSequenceRepeat = new CommandParameter<IntegerT>(HCALParameters.TTS_TEST_SEQUENCE_REPEAT, new IntegerT(-1));

    // define parameter set

    //
    // define parameters for TEST_TTS
    //
    ParameterSet<CommandParameter> ttsTestParameters = new ParameterSet<CommandParameter>();
    try {
      ttsTestParameters.add(ttsTestFedId);
      ttsTestParameters.add(ttsTestMode);
      ttsTestParameters.add(ttsTestPattern);
      ttsTestParameters.add(ttsTestSequenceRepeat);
    } catch (ParameterException nothing) {
      // Throws an exception if a parameter is duplicate
      throw new StateMachineDefinitionException( "Could not add to ttsTestParameters. Duplicate Parameter?", nothing );
    }

    // set the test parameters
    HCALInputs.TEST_TTS.setParameters(ttsTestParameters);

    //
    // define parameters for Initialize command
    //
    CommandParameter<IntegerT> initializeSid = new CommandParameter<IntegerT>(HCALParameters.SID, new IntegerT("0"));
    CommandParameter<StringT> initializeGlobalConfigurationKey = new CommandParameter<StringT>(HCALParameters.GLOBAL_CONF_KEY, new StringT(""));

    // define parameter set
    ParameterSet<CommandParameter> initializeParameters = new ParameterSet<CommandParameter>();
    try {
      initializeParameters.add(initializeSid);
      initializeParameters.add(initializeGlobalConfigurationKey);
    } catch (ParameterException nothing) {
      // Throws an exception if a parameter is duplicate
      throw new StateMachineDefinitionException( "Could not add to initializeParameters. Duplicate Parameter?", nothing );
    }

    HCALInputs.INITIALIZE.setParameters(initializeParameters);

    //
    // define parameters for Configure command
    //
    CommandParameter<StringT> configureRunType = new CommandParameter<StringT>(HCALParameters.HCAL_RUN_TYPE, new StringT(""));
    CommandParameter<StringT> configureRunKey = new CommandParameter<StringT>(HCALParameters.RUN_KEY, new StringT(""));
    CommandParameter<StringT> configureTpgKey = new CommandParameter<StringT>(HCALParameters.TPG_KEY, new StringT(""));
    CommandParameter<IntegerT> configureRunNumber = new CommandParameter<IntegerT>(HCALParameters.RUN_NUMBER, new IntegerT(0));
    CommandParameter<StringT> configurefedEnableMask = new CommandParameter<StringT>(HCALParameters.FED_ENABLE_MASK, new StringT(""));
    CommandParameter<StringT> hcalCfgScript = new CommandParameter<StringT>(HCALParameters.HCAL_CFGSCRIPT, new StringT(""));
    CommandParameter<StringT> hcalTTCciControl = new CommandParameter<StringT>(HCALParameters.HCAL_TTCCICONTROL, new StringT(""));
    CommandParameter<StringT> hcalLTCControl = new CommandParameter<StringT>(HCALParameters.HCAL_LTCCONTROL, new StringT(""));
    CommandParameter<StringT> hcalTCDSControl = new CommandParameter<StringT>(HCALParameters.HCAL_TCDSCONTROL, new StringT(""));
    CommandParameter<StringT> hcalLPMControl = new CommandParameter<StringT>(HCALParameters.HCAL_LPMCONTROL, new StringT(""));
    CommandParameter<BooleanT> configureCLOCK_CHANGED = new CommandParameter<BooleanT>(HCALParameters.CLOCK_CHANGED, new BooleanT(false));
    CommandParameter<BooleanT> configureUSE_RESET_FOR_RECOVER = new CommandParameter<BooleanT>(HCALParameters.USE_RESET_FOR_RECOVER, new BooleanT(true));
    CommandParameter<StringT> hcalPIControl = new CommandParameter<StringT>(HCALParameters.HCAL_PICONTROL, new StringT(""));
    CommandParameter<BooleanT> configureUSE_PRIMARY_TCDS = new CommandParameter<BooleanT>(HCALParameters.USE_PRIMARY_TCDS, new BooleanT(true));
    CommandParameter<StringT> configureSUPERVISOR_ERROR = new CommandParameter<StringT>(HCALParameters.SUPERVISOR_ERROR, new StringT(""));
    CommandParameter<StringT> configureAVAILABLE_RUN_CONFIGS = new CommandParameter<StringT>(HCALParameters.AVAILABLE_RUN_CONFIGS, new StringT(""));
    CommandParameter<StringT> configureRUN_CONFIG_SELECTED = new CommandParameter<StringT>(HCALParameters.RUN_CONFIG_SELECTED, new StringT(""));
    CommandParameter<StringT> configureCFGSNIPPET_KEY_SELECTED = new CommandParameter<StringT>(HCALParameters.CFGSNIPPET_KEY_SELECTED, new StringT(""));
    CommandParameter<StringT> configureAVAILABLE_RESOURCES = new CommandParameter<StringT>(HCALParameters.AVAILABLE_RESOURCES, new StringT(""));
    CommandParameter<StringT> configureMASKED_RESOURCES = new CommandParameter<StringT>(HCALParameters.MASKED_RESOURCES, new StringT(""));
    // define parameter set
    ParameterSet<CommandParameter> configureParameters = new ParameterSet<CommandParameter>();

    try {
      configureParameters.add(configureRunType);
      configureParameters.add(configureRunKey);
      configureParameters.add(configureTpgKey);
      configureParameters.add(configureRunNumber);
      configureParameters.add(configurefedEnableMask);
      configureParameters.add(hcalCfgScript);
      configureParameters.add(hcalTTCciControl);
      configureParameters.add(hcalLTCControl);
      configureParameters.add(hcalTCDSControl);
      configureParameters.add(hcalLPMControl);
      configureParameters.add(configureCLOCK_CHANGED);
      configureParameters.add(configureUSE_RESET_FOR_RECOVER);
      configureParameters.add(hcalPIControl);
      configureParameters.add(configureUSE_PRIMARY_TCDS);
      configureParameters.add(configureSUPERVISOR_ERROR);
      configureParameters.add(configureAVAILABLE_RUN_CONFIGS);
      configureParameters.add(configureRUN_CONFIG_SELECTED);
      configureParameters.add(configureCFGSNIPPET_KEY_SELECTED);
      configureParameters.add(configureAVAILABLE_RESOURCES);
      configureParameters.add(configureMASKED_RESOURCES);
    } catch (ParameterException nothing) {
      // Throws an exception if a parameter is duplicate
      throw new StateMachineDefinitionException( "Could not add to configureParameters. Duplicate Parameter?", nothing );
    }

    HCALInputs.CONFIGURE.setParameters(configureParameters);

    // define parameters for SETTTSTEST_MODE command
    //
    CommandParameter<StringT> hcalTTSCfgScript = new CommandParameter<StringT>(HCALParameters.HCAL_CFGSCRIPT, new StringT(""));
    CommandParameter<StringT> hcalTTSTTCciControl = new CommandParameter<StringT>(HCALParameters.HCAL_TTCCICONTROL, new StringT(""));
    CommandParameter<StringT> hcalTTSLTCControl = new CommandParameter<StringT>(HCALParameters.HCAL_LTCCONTROL, new StringT(""));
    CommandParameter<StringT> hcalTTSTCDSControl = new CommandParameter<StringT>(HCALParameters.HCAL_TCDSCONTROL, new StringT(""));
    CommandParameter<StringT> hcalTTSLPMControl = new CommandParameter<StringT>(HCALParameters.HCAL_LPMCONTROL, new StringT(""));
    CommandParameter<StringT> hcalTTSPIControl = new CommandParameter<StringT>(HCALParameters.HCAL_PICONTROL, new StringT(""));

    // define parameter set
    ParameterSet<CommandParameter> configureTTSParameters = new ParameterSet<CommandParameter>();

    try {
      configureTTSParameters.add(hcalTTSCfgScript);
      configureTTSParameters.add(hcalTTSTTCciControl);
      configureTTSParameters.add(hcalTTSLTCControl);
      configureTTSParameters.add(hcalTTSTCDSControl);
      configureTTSParameters.add(hcalTTSLPMControl);
      configureTTSParameters.add(hcalTTSPIControl);
    } catch (ParameterException nothing) {
      // Throws an exception if a parameter is duplicate
      throw new StateMachineDefinitionException( "Could not add to configureTTSParameters. Duplicate Parameter?", nothing );
    }

    HCALInputs.SETTTSTEST_MODE.setParameters(configureTTSParameters);

    //
    // define parameters for Start command
    //
    CommandParameter<IntegerT> startRunNumber = new CommandParameter<IntegerT>(HCALParameters.RUN_NUMBER, new IntegerT(0));
    CommandParameter<IntegerT> startRunSeqNumber = new CommandParameter<IntegerT>(HCALParameters.RUN_SEQ_NUMBER, new IntegerT(0));
    CommandParameter<IntegerT> requestedEvents   = new CommandParameter<IntegerT>(HCALParameters.NUMBER_OF_EVENTS, new IntegerT(1));

    // define parameter set
    ParameterSet<CommandParameter> startParameters = new ParameterSet<CommandParameter>();
    try {
      startParameters.add(startRunNumber);
      startParameters.add(startRunSeqNumber);
      startParameters.add(requestedEvents);
    } catch (ParameterException nothing) {
      // Throws an exception if a parameter is duplicate
      throw new StateMachineDefinitionException( "Could not add to startParameters. Duplicate Parameter?", nothing );
    }

    HCALInputs.START.setParameters(startParameters);

    //
    // Define the State Transitions
    //

    // INIT Command:
    // The INIT input is allowed only in the INITIAL state, and moves the
    // FSM in the INITIALIZING state.
    //
    addTransition(HCALInputs.INITIALIZE, HCALStates.INITIAL,HCALStates.INITIALIZING);

    // TEST_MODE Command:
    // The TEST_MODE input is allowed in the HALTED state and moves
    // the FSM in the PREPARING_TEST_MODE state.
    //
    addTransition(HCALInputs.TTSTEST_MODE, HCALStates.HALTED, HCALStates.PREPARING_TTSTEST_MODE);

    // Reach the TEST_MODE State
    addTransition(HCALInputs.SETTTSTEST_MODE, HCALStates.PREPARING_TTSTEST_MODE, HCALStates.TTSTEST_MODE);
    addTransition(HCALInputs.SETTTSTEST_MODE, HCALStates.TESTING_TTS, HCALStates.TTSTEST_MODE);

    // TEST_TTS Command:
    // The TEST_TTS input is allowed in the TEST_MODE state and moves
    // the FSM in the TESTING_TTS state.
    addTransition(HCALInputs.TEST_TTS, HCALStates.TTSTEST_MODE, HCALStates.TESTING_TTS);

    // COLDRESET Command
    addTransition(HCALInputs.COLDRESET, HCALStates.HALTED, HCALStates.COLDRESETTING);
    addTransition(HCALInputs.SETCOLDRESET, HCALStates.COLDRESETTING, HCALStates.HALTED);

    // CONFIGURE Command:
    // The CONFIGURE input is allowed only in the HALTED state, and moves
    // the FSM in the CONFIGURING state.
    //
    addTransition(HCALInputs.CONFIGURE, HCALStates.HALTED, HCALStates.CONFIGURING);

    // START Command:
    // The START input is allowed only in the CONFIGURED state, and moves
    // the FSM in the STARTING state.
    //
    addTransition(HCALInputs.START, HCALStates.CONFIGURED, HCALStates.STARTING);

    // PAUSE Command:
    // The PAUSE input is allowed only in the RUNNING state, and moves
    // the FSM in the PAUSING state.
    //
    addTransition(HCALInputs.PAUSE, HCALStates.RUNNING, HCALStates.PAUSING);

    // RESUME Command:
    // The RESUME input is allowed only in the PAUSED state, and moves
    // the FSM in the RESUMING state.
    //
    addTransition(HCALInputs.RESUME, HCALStates.PAUSED, HCALStates.RESUMING);

    // HALT Command:
    // The HALT input is allowed in the RUNNING, CONFIGURED and PAUSED
    // state, and moves the FSM in the HALTING state.
    //
    addTransition(HCALInputs.HALT,    HCALStates.RUNNING, HCALStates.HALTING);
    addTransition(HCALInputs.HALT,    HCALStates.CONFIGURED, HCALStates.HALTING);
    addTransition(HCALInputs.HALT,    HCALStates.PAUSED, HCALStates.HALTING);
    addTransition(HCALInputs.HALT,    HCALStates.TTSTEST_MODE, HCALStates.HALTING);

    // STOP Command:
    // The STOP input is allowed in the RUNNING
    // state, and moves the FSM in the STOPPING state.
    //
    addTransition(HCALInputs.STOP,    HCALStates.RUNNING, HCALStates.STOPPING);

    // RECOVER Command:
    // The RECOVER input is allowed from ERROR and moves the FSM in to
    // RECOVERING state.
    //
    addTransition(HCALInputs.RECOVER, HCALStates.ERROR, HCALStates.RECOVERING);

    // RESET Command:
    // The RESET input is allowed from any steady state and moves the FSM in the
    // RESETTING state.
    //
    addTransition(HCALInputs.RESET, HCALStates.HALTED , HCALStates.RESETTING);
    addTransition(HCALInputs.RESET, HCALStates.CONFIGURED , HCALStates.RESETTING);
    addTransition(HCALInputs.RESET, HCALStates.RUNNING , HCALStates.RESETTING);
    addTransition(HCALInputs.RESET, HCALStates.PAUSED , HCALStates.RESETTING);
    addTransition(HCALInputs.RESET, HCALStates.TTSTEST_MODE , HCALStates.RESETTING);
    addTransition(HCALInputs.RESET, HCALStates.ERROR , HCALStates.RESETTING);

    //
    // The following transitions are not triggered from the GUI.
    //

    // Transition for going to ERROR
    addTransition(HCALInputs.SETERROR, State.ANYSTATE, HCALStates.ERROR);

    //
    // add transitions for transitional States
    //

    // Reach the INITIAL State
    addTransition(HCALInputs.SETINITIAL, HCALStates.RECOVERING, HCALStates.INITIAL);

    // Reach the HALTED State
    addTransition(HCALInputs.SETHALT, HCALStates.INITIALIZING, HCALStates.HALTED);
    addTransition(HCALInputs.SETHALT, HCALStates.HALTING, HCALStates.HALTED);
    addTransition(HCALInputs.SETHALT, HCALStates.RECOVERING, HCALStates.HALTED);
    addTransition(HCALInputs.SETHALT, HCALStates.RESETTING, HCALStates.HALTED);

    // Reach the CONFIGURED State
    addTransition(HCALInputs.SETCONFIGURE, HCALStates.INITIALIZING, HCALStates.CONFIGURED);
    addTransition(HCALInputs.SETCONFIGURE, HCALStates.RECOVERING, HCALStates.CONFIGURED);
    addTransition(HCALInputs.SETCONFIGURE, HCALStates.CONFIGURING, HCALStates.CONFIGURED);
    addTransition(HCALInputs.SETCONFIGURE, HCALStates.STOPPING, HCALStates.CONFIGURED);
    addTransition(HCALInputs.SETCONFIGURE, HCALStates.RUNNING, HCALStates.CONFIGURED);

    // Reach the RUNNING State
    addTransition(HCALInputs.SETSTART, HCALStates.INITIALIZING, HCALStates.RUNNING);
    addTransition(HCALInputs.SETSTART, HCALStates.RECOVERING, HCALStates.RUNNING);
    addTransition(HCALInputs.SETSTART, HCALStates.STARTING, HCALStates.RUNNING);

    // Reach the PAUSED State
    addTransition(HCALInputs.SETPAUSE, HCALStates.PAUSING, HCALStates.PAUSED);
    addTransition(HCALInputs.SETPAUSE, HCALStates.RECOVERING, HCALStates.PAUSED);

    // Reach the RUNNING from RESUMING State
    addTransition(HCALInputs.SETRESUME, HCALStates.RESUMING, HCALStates.RUNNING);
  }
}
