@if (@This==@IsBatch) @then
rem **** batch zone *********************************************************

    setlocal enableextensions disabledelayedexpansion

    rem Batch file will delegate all the work to the script engine 
        wscript //E:JScript "%~dpnx0" 


    rem End of batch area. Ensure batch ends execution before reaching
    rem javascript zone
    exit /b

@end
// **** Javascript zone *****************************************************
// Instantiate the needed component to make url queries
var http = WScript.CreateObject('Msxml2.XMLHTTP.6.0');

// Retrieve the url parameter

    // Make the request

    http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=ardrone1-sender&password=jasonpassword&name=ardrone1-sender&email=none@none.com", false);
    http.send();
    http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=ardrone1-vehicle&password=jasonpassword&name=ardrone1-vehicle&email=none@none.com", false);
    http.send();

    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=ardrone1-vehicle-receiver&password=jasonpassword&name=ardrone1-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=ardrone1-vehicle-simsender&password=jasonpassword&name=ardrone1-vehicle-simsender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=central&password=jasonpassword&name=central&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=central-vehicle-receiver&password=jasonpassword&name=central-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1&password=jasonpassword&name=centralmember1&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1-inst-sender&password=jasonpassword&name=centralmember1-inst-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1-jstate&password=jasonpassword&name=centralmember1-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1-sender&password=jasonpassword&name=centralmember1-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1-vehicle&password=jasonpassword&name=centralmember1-vehicle&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1-vehicle-receiver&password=jasonpassword&name=centralmember1-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1push&password=jasonpassword&name=centralmember1push&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1push-jstate&password=jasonpassword&name=centralmember1push-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1push-sender&password=jasonpassword&name=centralmember1push-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1push-vehicle&password=jasonpassword&name=centralmember1push-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember1push-vehicle-receiver&password=jasonpassword&name=centralmember1push-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2&password=jasonpassword&name=centralmember2&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2-inst-sender&password=jasonpassword&name=centralmember2-inst-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2-jstate&password=jasonpassword&name=centralmember2-jstate&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2-sender&password=jasonpassword&name=centralmember2-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2-vehicle&password=jasonpassword&name=centralmember2-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2-vehicle-receiver&password=jasonpassword&name=centralmember2-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2push&password=jasonpassword&name=centralmember2push&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2push-jstate&password=jasonpassword&name=centralmember2push-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2push-sender&password=jasonpassword&name=centralmember2push-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2push-vehicle&password=jasonpassword&name=centralmember2push-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember2push-vehicle-receiver&password=jasonpassword&name=centralmember2push-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3&password=jasonpassword&name=centralmember3&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3-inst-sender&password=jasonpassword&name=centralmember3-inst-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3-jstate&password=jasonpassword&name=centralmember3-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3-sender&password=jasonpassword&name=centralmember3-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3-vehicle&password=jasonpassword&name=centralmember3-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3-vehicle-receiver&password=jasonpassword&name=centralmember13-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3push&password=jasonpassword&name=centralmember3push&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3push-jstate&password=jasonpassword&name=centralmember3push-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3push-sender&password=jasonpassword&name=centralmember3push-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3push-vehicle&password=jasonpassword&name=centralmember3push-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember3push-vehicle-receiver&password=jasonpassword&name=centralmember3push-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4&password=jasonpassword&name=centralmember4&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4-inst-sender&password=jasonpassword&name=centralmember4-inst-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4-jstate&password=jasonpassword&name=centralmember4-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4-sender&password=jasonpassword&name=centralmember4-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4-vehicle&password=jasonpassword&name=centralmember4-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4-vehicle-receiver&password=jasonpassword&name=centralmember4-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4push&password=jasonpassword&name=centralmember4push&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4push-jstate&password=jasonpassword&name=centralmember4push-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4push-sender&password=jasonpassword&name=centralmember4push-sender&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4push-vehicle&password=jasonpassword&name=centralmember4push-vehicle&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember4push-vehicle-receiver&password=jasonpassword&name=centralmember4push-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5&password=jasonpassword&name=centralmember5&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5-inst-sender&password=jasonpassword&name=centralmember5-inst-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5-jstate&password=jasonpassword&name=centralmember5-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5-sender&password=jasonpassword&name=centralmember5-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5-vehicle&password=jasonpassword&name=centralmember5-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5-vehicle-receiver&password=jasonpassword&name=centralmember5-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5push&password=jasonpassword&name=centralmember5push&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5push-jstate&password=jasonpassword&name=centralmember5push-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5push-sender&password=jasonpassword&name=centralmember5push-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5push-vehicle&password=jasonpassword&name=centralmember5push-vehicle&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember5push-vehicle-receiver&password=jasonpassword&name=centralmember5push-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6&password=jasonpassword&name=centralmember6&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6-inst-sender&password=jasonpassword&name=centralmember6-inst-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6-jstate&password=jasonpassword&name=centralmember6-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6-sender&password=jasonpassword&name=centralmember6-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6-vehicle&password=jasonpassword&name=centralmember6-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6-vehicle-receiver&password=jasonpassword&name=centralmember6-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6push&password=jasonpassword&name=centralmember6push&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6push-jstate&password=jasonpassword&name=centralmember6push-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6push-sender&password=jasonpassword&name=centralmember16ush-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6push-vehicle&password=jasonpassword&name=centralmember6push-vehicle&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=centralmember6push-vehicle-receiver&password=jasonpassword&name=centralmember6push-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=debug&password=jasonpassword&name=debug&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=debug-sender&password=jasonpassword&name=debug-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=examplepublisher&password=jasonpassword&name=examplepublisher&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=examplesubscriber&password=jasonpassword&name=examplesubscriber&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=instbot&password=jasonpassword&name=instbot&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=instmanager&password=jasonpassword&name=instmanager&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=jsonexamplepublisher&password=jasonpassword&name=jsonexamplepublisher&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=jsonexamplesubscriber&password=jasonpassword&name=jsonexamplesubscriber&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdfmonitor&password=jasonpassword&name=rdfmonitor&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdfreplay&password=jasonpassword&name=rdfreplay&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdfreplaysimstatesender&password=jasonpassword&name=rdfreplaysimstatesender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdftest&password=jasonpassword&name=rdftest&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdftest-jstate&password=jasonpassword&name=rdftest-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdftest-jstate-rec&password=jasonpassword&name=rdftest-jstate-rec&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdftest-sender&password=jasonpassword&name=rdftest-sender&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdftest-simsender&password=jasonpassword&name=rdftest-simsender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdftest-vehicle&password=jasonpassword&name=rdftest-vehicle&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=rdftest-vehicle-receiver&password=jasonpassword&name=rdftest-vehicle-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=sumo&password=jasonpassword&name=sumo&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=sumo-sender&password=jasonpassword&name=sumo-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=sumo-receiver&password=jasonpassword&name=sumo-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=sumo-inst-sender&password=jasonpassword&name=sumo-inst-sender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=triplestore&password=jasonpassword&name=triplestore&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=triplestore-jstate&password=jasonpassword&name=triplestore-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppsimsender&password=jasonpassword&name=xmppsimsender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppsimsender-jstate&password=jasonpassword&name=xmppsimsender-jstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppviewer&password=jasonpassword&name=xmppviewer&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppviewer-inst&password=jasonpassword&name=xmppviewer-inst&email=none@none.com", false);
    http.send();
     http.open("GET", "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppviewerjstate&password=jasonpassword&name=xmppviewerjstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppviewersimstate&password=jasonpassword&name=xmppviewersimstate&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppviewersimstatesender&password=jasonpassword&name=xmppviewersimstatesender&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppworld&password=jasonpassword&name=xmppworld&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=xmppworldinstclient&password=jasonpassword&name=xmppworldinstclient&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=AOI&password=jasonpassword&name=AOI&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=AOI-receiver&password=jasonpassword&name=AOI-receiver&email=none@none.com", false);
    http.send();
    http.open("GET",  "http://localhost:9090/plugins/userService/userservice?type=add&secret=EIEIEIO&username=AOI-sender&password=jasonpassword&name=AOI-sender&email=none@none.com", false);
    http.send();



    // All done. Exit
    WScript.Quit(0);