package rcms.fm.app.level1;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import rcms.util.logger.RCMSLogger;
import rcms.common.db.DBConnectorException;
import rcms.resourceservice.db.Group;
import rcms.resourceservice.db.resource.Resource;
import rcms.fm.resource.QualifiedGroup;
import rcms.fm.resource.QualifiedResource;
import rcms.fm.resource.qualifiedresource.FunctionManager;
import rcms.fm.fw.parameter.type.StringT;
import rcms.fm.fw.parameter.FunctionManagerParameter;
import rcms.fm.fw.user.UserActionException;

/**
 *  @author John Hakala
 *
 */

public class HCALMasker {

  protected HCALFunctionManager functionManager = null;
  static RCMSLogger logger = null;

  public HCALMasker(HCALFunctionManager parentFunctionManager) {
    this.logger = new RCMSLogger(HCALFunctionManager.class);
    logger.warn("Constructing masker.");
    this.functionManager = parentFunctionManager;
    logger.warn("Done constructing masker.");
  }

  protected Map<String, Boolean> isEvmTrigCandidate(List<Resource> level2Children) {
    boolean hasAtriggerAdapter = false;
    boolean hasAdummy = false;
    boolean hasAnEventBuilder = false;
    boolean hasAnFU = false;
    for (Resource level2resource : level2Children) {
      if (level2resource.getName().contains("TriggerAdapter") || level2resource.getName().contains("FanoutTTCciTA")) {
        logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has a TA.");
        hasAtriggerAdapter=true;
        if (level2resource.getName().contains("DummyTriggerAdapter")) {
          logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has a DummyTriggerAdapter.");
          hasAdummy=true;
        }
      }
      if (level2resource.getName().contains("hcalTrivialFU")) {
        logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has a FU.");
        hasAnFU=true;
      }
      if (level2resource.getName().contains("hcalEventBuilder")) {
        logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has an eventBuilder.");
        hasAnEventBuilder=true;
      }
    }
    Map<String, Boolean> response = new HashMap<String, Boolean>();
    Boolean isAcandidate = new Boolean( hasAtriggerAdapter && hasAnFU && hasAnEventBuilder );
    response.put("isAcandidate", isAcandidate);
    Boolean isAdummyCandidate = new Boolean( hasAtriggerAdapter && hasAnFU && hasAnEventBuilder && hasAdummy);
    response.put("isAdummyCandidate", isAdummyCandidate);
    return response;
  }

  protected Map<String, Resource> getEvmTrigResources(List<Resource> level2Children) throws UserActionException { 
    if (isEvmTrigCandidate(level2Children).get("isAcandidate")) {
      // This implementation assumes no level2 function managers will have no more than one TA.
      Map<String, Resource> evmTrigResources = new HashMap<String, Resource>();
      for (Resource level2resource : level2Children) {
        if (level2resource.getName().contains("TriggerAdapter") || level2resource.getName().contains("FanoutTTCciTA")) {
          evmTrigResources.put("TriggerAdapter", level2resource);
        }
        if (level2resource.getName().contains("hcalTrivialFU")) {
          evmTrigResources.put("hcalTrivialFU", level2resource);
        }
        if (level2resource.getName().contains("hcalEventBuilder")) {
          evmTrigResources.put("hcalEventBuilder", level2resource);
        }
      }
      return evmTrigResources;
    }
    else {
      String errMessage = "getEvmTrigResources was called on a level2 that does not have the required apps (TA, eventbuilder, trivialFU).";
      throw new UserActionException(errMessage);
    }
  }

  protected Map<String, Resource> pickEvmTrig() {
    // Function to pick an FM that has the needed applications for triggering and eventbuilding, and put it in charge of those duties
    // This will prefer an FM with a DummyTriggerAdapter to other kinds of trigger adapters.

    Map<String, Resource> candidates = new HashMap<String, Resource>();

    Boolean theresAcandidate = false;
    Boolean theresAdummyCandidate = false;


    QualifiedGroup qg = functionManager.getQualifiedGroup();
    String MaskedFMs =  ((StringT)functionManager.getHCALparameterSet().get(HCALParameters.MASKED_RESOURCES).getValue()).getString();
    if (MaskedFMs.length() > 0) {
      MaskedFMs = MaskedFMs.substring(0, MaskedFMs.length()-1);
    }

    List<QualifiedResource> level2list = qg.seekQualifiedResourcesOfType(new FunctionManager());

    for (QualifiedResource level2 : level2list) {
      if (!MaskedFMs.contains(level2.getName())) {
        try {
          QualifiedGroup level2group = ((FunctionManager)level2).getQualifiedGroup();
          logger.debug("[HCAL " + functionManager.FMname + "]: the qualified group has this DB connector" + level2group.rs.toString());

          Group fullConfig = level2group.rs.retrieveLightGroup(level2.getResource());
          List<Resource> level2Children = fullConfig.getChildrenResources();

          logger.warn("[JohnLog2] " + functionManager.FMname + ": the result of isEvmTrigCandidate()  on " + level2.getName() + " has isAcandidate: " + isEvmTrigCandidate(level2Children).get("isAcandidate").toString());
          logger.warn("[JohnLog2] " + functionManager.FMname + ": the result of isEvmTrigCandidate() has isAdummyCandidate: " + isEvmTrigCandidate(level2Children).get("isAdummyCandidate").toString());

          try {
            if (!theresAcandidate && isEvmTrigCandidate(level2Children).get("isAcandidate")) {
              logger.warn("[JohnLog2] found a non-dummy candidate.");
              candidates = getEvmTrigResources(level2Children);
              candidates.put("EvmTrigFM", level2.getResource());
              theresAcandidate = true;
            }
            if (!theresAdummyCandidate && isEvmTrigCandidate(level2Children).get("isAdummyCandidate")) {
              logger.warn("[JohnLog2] found a dummy candidate.");
              candidates = getEvmTrigResources(level2Children);
              candidates.put("EvmTrigFM", level2.getResource());
              theresAcandidate = true;
              theresAdummyCandidate = true;
            }
          }
          catch (UserActionException ex) {
            logger.error("[JohnLog2] " + functionManager.FMname + ": got an exception while getting the EvmTrig resources for " + level2.getName() + ": " + ex.getMessage());
          }
        }
        catch (DBConnectorException ex) {
          logger.error("[HCAL " + functionManager.FMname + "]: Got a DBConnectorException when trying to retrieve level2s' children resources: " + ex.getMessage());
        }
      }
    }
    //logger.warn("[JohnLog2] The following resources were picked as evmTrig resources: " + candidates.get("EvmTrigFM") +  ", " + candidates.get("TriggerAdapter").getName() + ", " + candidates.get("hcalTrivialFU").getName() + ", " + candidates.get("hcalEventBuilder").getName());


    for (Map.Entry<String, Resource> entry : candidates.entrySet()) {
      String key = entry.getKey();
      logger.warn("[JohnLog2] key:" + key);
   }
  
  
    return candidates;
  }

  protected void setMaskedFMs() {

    // functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.MASKED_APPLICATIONS,new StringT(MaskedApplications)));

    QualifiedGroup qg = functionManager.getQualifiedGroup();
    // TODO send all masked applications defined in global parameter 
    String MaskedFMs =  ((StringT)functionManager.getHCALparameterSet().get(HCALParameters.MASKED_RESOURCES).getValue()).getString();
    if (MaskedFMs.length() > 0) {
      MaskedFMs = MaskedFMs.substring(0, MaskedFMs.length()-1);
    }
    List<QualifiedResource> level2list = qg.seekQualifiedResourcesOfType(new FunctionManager());
    //boolean somebodysHandlingTA = false;
    //boolean itsThisLvl2 = false;
    //boolean itsAdummy = false;
    String allMaskedResources = "";
    //String ruInstance = "";
    //String lpmSupervisor = "";
    //String EvmTrigsApps = "";
    Map<String, Resource> evmTrigResources = pickEvmTrig();

    String eventBuilder   = "none";
    String trivialFU      = "none";
    String triggerAdapter = "none";
    String EvmTrigFM      = "none";


    if (evmTrigResources.get("hcalEventBuilder") != null) {
      eventBuilder = evmTrigResources.get("hcalEventBuilder").getName();
      trivialFU = evmTrigResources.get("hcalTrivialFU").getName();
      triggerAdapter = evmTrigResources.get("TriggerAdapter").getName();
      EvmTrigFM = evmTrigResources.get("EvmTrigFM").getName();
    }

    for (QualifiedResource qr : level2list) {
      if (qr.getName().equals(EvmTrigFM)) { 
         qr.getResource().setRole("EvmTrig");
         functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.EVM_TRIG_FM, new StringT(qr.getName())));
      }
      //itsThisLvl2 = false;
      try {
        QualifiedGroup level2group = ((FunctionManager)qr).getQualifiedGroup();
        logger.debug("[HCAL " + functionManager.FMname + "]: the qualified group has this DB connector" + level2group.rs.toString());
        Group fullConfig = level2group.rs.retrieveLightGroup(qr.getResource());
        // TODO see here
        List<Resource> fullconfigList = fullConfig.getChildrenResources();
        if (MaskedFMs.length() > 0) {
          logger.info("[HCAL " + functionManager.FMname + "]:: Got MaskedFMs " + MaskedFMs);
          String[] MaskedResourceArray = MaskedFMs.split(";");
          for (String MaskedFM: MaskedResourceArray) {
            logger.debug("[HCAL " + functionManager.FMname + "]: " + functionManager.FMname + ": Starting to mask FM " + MaskedFM);
            if (qr.getName().equals(MaskedFM)) {
              logger.debug("[HCAL " + functionManager.FMname + "]: Going to call setActive(false) on "+qr.getName());
              qr.setActive(false);

              //logger.info("[HCAL " + functionManager.FMname + "]: LVL2 " + qr.getName() + " has rs group " + level2group.rs.toString());
              allMaskedResources = ((StringT)functionManager.getHCALparameterSet().get(HCALParameters.MASKED_RESOURCES).getValue()).getString();
              for (Resource level2resource : fullconfigList) {
                logger.debug("[HCAL " + functionManager.FMname + "]: The masked level 2 function manager " + qr.getName() + " has this in its XdaqExecutive list: " + level2resource.getName());
                allMaskedResources+=level2resource.getName();
                allMaskedResources+=";";
                logger.info("[HCAL " + functionManager.FMname + "]: The new list of all masked resources is: " + allMaskedResources);
              }
            }
          }
        }
        for (Resource level2resource : fullconfigList) {
          if (level2resource.getName().contains("FanoutTTCciTA") || level2resource.getName().contains("TriggerAdapter") || level2resource.getName().contains("hcalTrivialFU") || level2resource.getName().contains("hcalEventBuilder")) {
            if (!level2resource.getName().equals(eventBuilder) && !level2resource.getName().equals(trivialFU) && !level2resource.getName().equals(triggerAdapter)) { 
              allMaskedResources += level2resource.getName();
              allMaskedResources+=";";
            }
          }
        }
        logger.debug("[HCAL " + functionManager.FMname + "]: About to set the new MASKED_RESOURCES list.");
        functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.MASKED_RESOURCES, new StringT(allMaskedResources)));
        logger.info("[HCAL " + functionManager.FMname + "]: Just set the new MASKED_RESOURCES list.");
        logger.debug("[HCAL " + functionManager.FMname + "]: About to set the RU_INSTANCE.");
        functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.RU_INSTANCE, new StringT(eventBuilder)));
        logger.info("[HCAL " + functionManager.FMname + "]: Just set the RU_INSTANCE to " + eventBuilder);
        logger.debug("[HCAL " + functionManager.FMname + "]: About to set the LPM_SUPERVISOR.");
      }
      catch (DBConnectorException ex) {
        logger.error("[HCAL " + functionManager.FMname + "]: Got a DBConnectorException when trying to retrieve level2s' children resources: " + ex.getMessage());
      }
    }
  }
}
