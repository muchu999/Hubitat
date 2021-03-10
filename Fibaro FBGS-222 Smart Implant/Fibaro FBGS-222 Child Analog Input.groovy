metadata {
	definition (name: "Fibaro FGBS-222 Child Analog Input", namespace: "christi999", author: "", importUrl: "https://raw.githubusercontent.com/muchu999/Hubitat/master/Fibaro%20FBGS-222%20Smart%20Implant/Fibaro%20FBGS-222%20Child%20Analog%20Input.groovy") {
		capability "Refresh"
		capability "Voltage Measurement"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Switch"
		capability "Switch Level"
		capability "Contact Sensor"
		
		attribute "rawVoltage", "decimal"

		preferences {
			input(name: "voltageEquation", type: "string", title: "<font style='font-size:16px; color:#1a77c9'>Voltage equation</font>", description: "<font style='font-size:16px; font-style: italic'>Equation to calculate 'voltage' from 'rawVoltage'.<br><br>Valid functions/operators: sqrt(x), abs(x), log(x), exp(x), sin(x), cos(x), tan(x), asin(x), acos(x), atan(x), floor(x), ceil(x), round(x), sqrt(x), max(x,y), min(x,y), gt(x,y), lt(x,y), gteq(x,y), lteq(x,y), eq(x,y), neq(x,y), *, /, +, -, ^,(,) <br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Example: log(rawVoltage*5.1+3.3^1.2)<br><br></font>", defaultValue: "rawVoltage");			
			input(name: "levelEquation", type: "string", title: "<font style='font-size:16px; color:#1a77c9'>Level equation</font>", description: "<font style='font-size:16px; font-style: italic'>Equation to calculate 'level' from 'rawVoltage'<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Example: max(min(round(rawVoltage*11+5.1),100),0)<br><br></font>", defaultValue: "");
			input(name: "temperatureEquation", type: "string", title: "<font style='font-size:16px; color:#1a77c9'>Temperature equation</font>", description: "<font style='font-size:16px; font-style: italic'>Equation to calculate 'temperature' from 'rawVoltage'<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Example: round((rawVoltage*(-68.8)/3.19+198.81-32)*5/9*100)/100<br><br></font>", defaultValue: "");			
			input(name: "contactSensorEquation", type: "string", title: "<font style='font-size:16px; color:#1a77c9'>Contact Sensor equation</font>", description: "<font style='font-size:16px; font-style: italic'>Equation to calculate 'contact' from 'rawVoltage'. Value greather than 0 is open<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Example: gt(rawVoltage,5.0)<br><br></font>", defaultValue: "");			
			input(name: "switchEquation", type: "string", title: "<font style='font-size:16px; color:#1a77c9'>Switch equation</font>", description: "<font style='font-size:16px; font-style: italic'>Equation to calculate 'switch' from 'rawVoltage'. Value greather than 0 is on<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Example: lt(rawVoltage,2.5)<br><br></font>", defaultValue: "");			
		}
	}
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}

void updated() {
	if(!state.rawVoltage)
		state.rawVoltage=0
	if(voltageEquation) {
		value = eval(voltageEquation)
		units="v"
		sendEvent(name: "voltage", value: value, unit: units, descriptionText:"voltage is ${value}${units}" )
		voltage = value
	}
	if(levelEquation) {
		value = eval(levelEquation)
		units=""
		sendEvent(name: "level", value: value, unit: units, descriptionText:"level is ${value}${units}" )
	}
	if(temperatureEquation) {
		value = eval(temperatureEquation)
		units=""
		sendEvent(name: "temperature", value: value, unit: units, descriptionText:"temperature is ${value}${units}" )
	}
	if(contactSensorEquation) {
		value = eval(contactSensorEquation) > 0 ? "open":"closed"
		units=""
		sendEvent(name: "contact", value: value, unit: units, descriptionText:"contact is ${value}${units}" )
	}
	if(switchEquation) {
		value = eval(switchEquation) > 0 ? "on":"off"
		units=""
		sendEvent(name: "switch", value: value, unit: units, descriptionText:"switch is ${value}${units}" )
	}
}

void parse(List<Map> description) {
    description.each {
        if (it.name in ["voltage"]) {
			state.rawVoltage = it.value 
			if(voltageEquation) {
				value = eval(voltageEquation)
				units="v"
				sendEvent(name: "voltage", value: value, unit: units, descriptionText:"voltage is ${value}${units}") 
			}
			if(levelEquation) {
				value = eval(levelEquation)
				units=""
				sendEvent(name: "level", value: value, unit: units, descriptionText:"level is ${value}${units}") 
			}
			if(temperatureEquation) {
				value = eval(temperatureEquation)
				units="F"
				sendEvent(name: "temperature", value: value, unit: units, descriptionText:"temperature is ${value}${units}")
			}
			if(contactSensorEquation) {
				value = eval(contactSensorEquation) > 0 ? "open":"closed"
				units=""
				sendEvent(name: "contact", value: value, unit: units, descriptionText:"contact is ${value}${units}" )
			}
			if(switchEquation) {
				value = eval(switchEquation) > 0 ? "on":"off"
				units=""
				sendEvent(name: "switch", value: value, unit: units, descriptionText:"switch is ${value}${units}" )
			}
		}
    } 
}

int pos
int ch
String str

public double eval(s) {
	
	str = s.toLowerCase()
	pos = -1
	return evalParse()
}

void nextChar() {
	pos = pos+1
	ch = (pos < str.length()) ? str.charAt(pos) : -1;
}

boolean eat(charToEat) {
	while (ch == ' ') nextChar();
	if (ch == charToEat) {
		nextChar();
			return true;
	}
	return false;
}

double evalParse() {
	nextChar();
    double x = parseExpression();
    if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
    return x;
}

double parseExpression() {
	double x = parseTerm();
	for (;;) {
		if      (eat('+')) x = x+parseTerm(); // addition
		else if (eat('-')) x = x-parseTerm(); // subtraction
		else return x;
	}
}

double parseTerm() {
	double x = parseFactor();
	for (;;) {
		if      (eat('*')) x = x*parseFactor(); // multiplication
		else if (eat('/')) x = x/parseFactor(); // division
        else return x;
	}
}

def parseFactor() {
	if (eat('+')) return parseFactor(); // unary plus
	if (eat('-')) return -parseFactor(); // unary minus
	double x;
	
	int startPos = pos;
	if (eat('(')) { // parentheses
		x = parseExpression();
		if(eat(')')) {
		}
		else if(eat(',')) {
			y = parseExpression();
			if(eat(')')) {
				return [x, y]
			}
			else {
				log.error("Missing closing parenthesis" + func);
			}
		}
		else {
			log.error("Missing closing parenthesis" + func);
		}
	} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
		while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
		x = Double.parseDouble(str.substring(startPos, this.pos));
	} else if (ch >= 'a' && ch <= 'z') { // functions
		while (ch >= 'a' && ch <= 'z') nextChar();
		String func = str.substring(startPos, this.pos);
		
		if(func =="rawvoltage") {
			x=state.rawVoltage
		} 
		else {
			
			if (func.equals("sqrt"))       {x = parseFactor(); x = Math.sqrt(x);}
			else if (func.equals("abs"))   {x = parseFactor(); x = Math.abs(x);}
			else if (func.equals("log"))   {x = parseFactor(); x = Math.log(x);}
			else if (func.equals("exp"))   {x = parseFactor(); x = Math.exp(x);}
			else if (func.equals("sin"))   {x = parseFactor(); x = Math.sin(x);}
			else if (func.equals("cos"))   {x = parseFactor(); x = Math.cos(x);}
			else if (func.equals("tan"))   {x = parseFactor(); x = Math.tan(x);}
			else if (func.equals("asin"))   {x = parseFactor(); x = Math.asin(x);}
			else if (func.equals("acos"))   {x = parseFactor(); x = Math.acos(x);}
			else if (func.equals("atan"))   {x = parseFactor(); x = Math.atan(x);}
			else if (func.equals("floor")) {x = parseFactor(); x = Math.floor(x);}
			else if (func.equals("ceil"))  {x = parseFactor(); x = Math.ceil(x);}
			else if (func.equals("round")) {x = parseFactor(); x = Math.round(x);}
			else if (func.equals("sqrt"))  {x = parseFactor(); x = Math.sqrt(x);}
			else if (func.equals("max"))   {(x,y) = parseFactor(); x = Math.max(x,y);}
			else if (func.equals("min"))   {(x,y) = parseFactor(); x = Math.min(x,y);}
			else if (func.equals("gt"))    {(x,y) = parseFactor(); x = ((x>y) ? 1:0);}
			else if (func.equals("lt"))    {(x,y) = parseFactor(); x = ((x<y) ? 1:0);}
			else if (func.equals("gteq"))  {(x,y) = parseFactor(); x = ((x>=y) ? 1:0);}
			else if (func.equals("lteq"))  {(x,y) = parseFactor(); x = ((x<=y) ? 1:0);}
			else if (func.equals("eq"))    {(x,y) = parseFactor(); x = ((x==y) ? 1:0);}
			else if (func.equals("neq"))   {(x,y) = parseFactor(); x = ((x!=y) ? 1:0);}
			else{
				log.error("Unknown function: " + func);
			}
		}
	} else {
		log.error("Unexpected:" + ch.toString());
	}

	if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
	
	return x;
}
