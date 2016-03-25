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
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {

        section( "Turn on/off this switche(s)" ) {
            input( 	"SelectedSwitches", "capability.switch", title: "Select Lights/Switchs", multiple: true, required: true )
        }
        section("Execution"){
            input(	"ExecutionEnable", "bool", title: "Enabled execution?", required: true, defaultValue: "true" )
            input( 	"RunningMinutes", "number", title: "Running Minutes ?", range: "1..*", required: true, defaultValue: "60" )
            input( 	"WaitingMinutes", "number", title: "Waiting Minutes ?",  range: "1..*", required: true, defaultValue: "60" )
        }

}

def installed() {

	log.debug "==========================================================="
    log.debug "Installing SmartApp"
    
	Initialize()
  
}

def updated() {

	log.debug "==========================================================="
    log.debug "Updating SmartApp";

    unschedule()
	unsubscribe()
    
    Initialize()

}

def Initialize () {

    log.debug "Initializing SmartApp"

    subscribe(SelectedSwitches, "switch", Switch_Change)

    if ( nvl( ExecutionEnable, true ) ) {

        state.SwitchesStatus = "off"
        state.StatusChangeNext = GetEpochWithoutSecond()

        Toggle_Switches()

        //schedule("*/5 * * * * ?", FiveMinutes_Cron)
        schedule("* * * * * ?", CronWakeUp)            

    } else {

        log.debug "Initialize: Execution is not enabled"

    }

}

def Switch_Change( pEvent ) {

	//log.debug "Executing Switch_Change with event at [${pEvent.value}]"

}

def Toggle_Switches() {

    if ( nvl( ExecutionEnable, true ) ) {

        if ( nvl( state.StatusChangeNext, 0 ) <= now() ) {    

            state.StatusChangeEpoch = GetEpochWithoutSecond()

            switch ( nvl( state.SwitchesStatus, "off" ) ) {
            
                case "on" : 
                
                    state.SwitchesStatus = "off"
                    state.StatusChangeNext = state.StatusChangeEpoch + ( WaitingMinutes * 60 * 1000 )       

                    log.info "Turning OFF ${SelectedSwitches.size()} switche(s) for ${WaitingMinutes} minute(s)"
                    SelectedSwitches.off()
                    
                    break;
                    
                case "off" :  
                
                    state.SwitchesStatus = "on"
                    state.StatusChangeNext = state.StatusChangeEpoch + ( RunningMinutes * 60 * 1000 )

                    log.info "Turning ON ${SelectedSwitches.size()} switche(s) for ${WaitingMinutes} minute(s)"
                    SelectedSwitches.on()
                    
                    break;       
                    
                default: 
                	log.error "Unknown switche(s) status [${state.SwitchesStatus}]"
                    
					break;
                    
            }

        } else {
        
            log.debug "Status will be change at ${GetDateFromEpoch(state.StatusChangeNext)}"
            
        }

    } else {
    
        log.debug "Execution is disable"
        
    }    
        
}

def CronWakeUp() {

    log.debug "==========================================================="
	log.debug "Executing [ CronWakeUp ] with switche(s) status at [ ${state.SwitchesStatus} ]"		
    //log.debug "Next execution at [ ${state.StatusChangeNext.gettime()} ]"
         
    // returns a list of the values for all switches
    def currSwitches = SelectedSwitches.currentSwitch
    def onSwitches = currSwitches.findAll { 
        switchVal -> switchVal == "on" ? true : false
    }
    log.debug "${onSwitches.size()} out of ${SelectedSwitches.size()} switches are on"
    
    Toggle_Switches()
    
}

def GetEpochWithoutSecond() {

    Calendar wCalendarInstance = Calendar.getInstance( )
      
    wCalendarInstance.setTime( new Date() )
    wCalendarInstance.set( Calendar.SECOND, 0 )
    
    return wCalendarInstance.getTimeInMillis()

}

def GetDateFromEpoch( long pTimeInMillis = 0 ) {
    
	return new Date( pTimeInMillis )
    
}

public <T> T nvl(T arg0, T arg1) {

    return ( arg0 == null ) ? arg1 : arg0;

}
