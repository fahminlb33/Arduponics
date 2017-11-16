#include "LibDHT11.h"

// constrcutor
LibDHT11::LibDHT11() {
}

void LibDHT11::init(int pin, void(*callback_wrapper) ()) {
	// assign settings
	this->intNumber = digitalPinToInterrupt(pin);
	this->pin = pin;
	this->isrCallback_wrapper = callback_wrapper;

	// assign zero values
	hum = 0;
	temp = 0;

	// set pin mode
	pinMode(pin, OUTPUT);
	digitalWrite(pin, HIGH);

	// set state
	state = STOPPED;
	status = DHT11_ERROR_NOTSTARTED;
}

int LibDHT11::acquire() {
	if (state == STOPPED || state == ACQUIRED) {

		//set the state machine for interruptions analisis of the signal
		state = RESPONSE;

		// empty buffers
		for (int i = 0; i< 5; i++) bits[i] = 0;
		cnt = 7;
		idx = 0;
		hum = 0;
		temp = 0;

		// REQUEST SAMPLE
		pinMode(pin, OUTPUT);
		digitalWrite(pin, LOW);
		delay(18);
		digitalWrite(pin, HIGH);
		delayMicroseconds(40);
		pinMode(pin, INPUT);

		// Analize the data in an interrupt
		us = micros();
		attachInterrupt(intNumber, isrCallback_wrapper, FALLING);

		return DHT11_ACQUIRING;
	}
	else
		return DHT11_ERROR_ACQUIRING;
}

/* Private */
void LibDHT11::isrCallback() {
	int newUs = micros();
	int delta = (newUs - us);
	us = newUs;
	if (delta>6000) {
		status = DHT11_ERROR_TIMEOUT;
		state = STOPPED;
		detachInterrupt(intNumber);
		return;
	}
	switch (state) {
	case RESPONSE:
		if (delta<25) {
			us -= delta;
			break; //do nothing, it started the response signal
		} if (125<delta && delta<190) {
			state = DATA;
		}
		else {
			detachInterrupt(intNumber);
			status = DHT11_ERROR_TIMEOUT;
			state = STOPPED;
		}
		break;

	case DATA:
		if (delta<10) {
			detachInterrupt(intNumber);
			status = DHT11_ERROR_DELTA;
			state = STOPPED;
		}
		else if (60<delta && delta<155) { //valid in timing
			if (delta>90) //is a one
				bits[idx] |= (1 << cnt);
			if (cnt == 0) {  // whe have fullfilled the byte, go to next
				cnt = 7;    // restart at MSB
				if (idx++ == 4) {      // go to next byte, if whe have got 5 bytes stop.
					detachInterrupt(intNumber);
					// WRITE TO RIGHT VARS
					// as bits[1] and bits[3] are allways zero they are omitted in formulas.
					hum = bits[0];
					temp = bits[2];
					uint8_t sum = bits[0] + bits[2];
					if (bits[4] != sum) {
						status = DHT11_ERROR_CHECKSUM;
						state = STOPPED;
					}
					else {
						status = DHT11_OK;
						state = ACQUIRED;
					}
					break;
				}
			}
			else cnt--;
		}
		else {
			detachInterrupt(intNumber);
			status = DHT11_ERROR_TIMEOUT;
			state = STOPPED;
		}
		break;

	default:
		break;
	}
}
