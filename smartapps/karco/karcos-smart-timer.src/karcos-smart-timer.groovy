/**
 *  Smart Timer
 *  Loosely based on "Light Follows Me"
 *  based on "Smart Light Timer, X minutes unless already on" (andersheie@gmail.com, )
 *
 *  This prevent them from turning off when the timer expires, if they were already turned on
 *
 *  If the switch is already on, if won't be affected by the timer  (Must be turned of manually)
 *  If the switch is toggled while in timeout-mode, it will remain on and ignore the timer (Must be turned of manually)
 *
 *  The timeout period begins when the contact is closed, or motion stops, so leaving a door open won't start the timer until it's closed.
 *
 *  Author: karco@olan.ca
 *  Date: 2016-03-08
 */

definition(
    name: "Karco's Smart Timer",
    namespace: "karco",
    author: "karco@olan.ca",
    description: "Turns on a switch for X minutes, then turns it off for X minutes.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "Karco ", displayLink: ""]
)

preferences {

//	page(name: "OptionsPage1", title: "Options",) {

        section("Turn on/off this switche(s)") {
            //paragraph ""
            input( 
            		name:			"switches", 
                    title: 			"Select Lights/Switchs", 
                    type: 			"capability.switch", 
                    multiple: 		true
			)
        }
        section("Execution"){
            input(	name:			"ExecutionEnable", 
            		title: 			"Enabled execution?", 
                    type: 			"bool",   
					required: 		true, 
					defaultValue: 	true
            )
            input( 	name: 			"RunningMinutes",  
            		title: 			"Running Minutes ?",  
                    type: 			"number", 
                    range: 			"1..*", 
                    required: 		true, 
                    defaultValue: 	60 
            )
            input( 	name: 			"WaitingMinutes",  
                    title: 			"Waiting Minutes ?",  
                    type: 			"number", 
                    range: 			"1..*", 
                    required: 		true, 
                    defaultValue: 	60 
            )
        }
//        section("Global") {
//            icon(title: "choose an application icon",
//                 required: true)
//        }

//	}

}

def installed() {

    log.debug "==========================================================="
    log.debug "Executing [installed]"
    
	Initialize()
  
}

def updated() {

    log.debug "==========================================================="
    log.debug "Executing [updated]"

    unschedule()
	unsubscribe()
    
    Initialize()

}

def Initialize () {

    log.debug "==========================================================="
    log.debug("Initialize: ExecutionEnable is set to [ ${ExecutionEnable} ]")

	subscribe(switches, "switch", Switch_Change)
    
    // Consider null value as true by default
    if ( ExecutionEnable || ExecutionEnable == null ) {
    
        log.debug("Initialize: Sending ON command to ${switches.size()} switche(s)")
    	switches.on()
	
    } else {
    
    	log.debug("Initialize: Execution is not enabled")
    
    }

}

def Switch_Change ( evt ) {

	log.debug "==========================================================="
    log.debug "Switch_Change : $evt.name : $evt.value"

	// Consider null value as true by default
	if ( ExecutionEnable == true || ExecutionEnable == null ) {
        if ( evt.value == "on" ) {

            log.info("Switch_Change : Turning ON switche(s) for ${RunningMinutes} minute(s)")

            state.SwitchesStatus = "Started"
            runIn( 60 * RunningMinutes, Toggle_Switches )

        } else if ( evt.value == "off" ) {

            log.info("Switch_Change : Turning OFF switche(s) for ${WaitingMinutes} minute(s)")

            state.SwitchesStatus = "Stopped"
            runIn( 60 * WaitingMinutes, Toggle_Switches )

        }
    } else {
        log.debug "Switch_Change : Execution is disable"
    }        
    
    log.debug "Switch_Change : New State : ${state.SwitchesStatus}"
    
}

def Toggle_Switches() {

	log.debug "==========================================================="
	log.debug "Executing [Toggle_Switches] with execution status at [${state.SwitchesStatus}]"
    
	if ( state.SwitchesStatus == "Started" ) {
		switches.off()
    } else if ( state.SwitchesStatus == "Stopped" ) {
    	switches.on()
    }

}
