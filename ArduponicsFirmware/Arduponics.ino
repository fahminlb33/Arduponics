/*
    Arduponics - Open-source garden automation tool
    Copyright (C) 1027  Fahmi Noor Fiqri, Ahmad Sobari

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <SoftwareSerial.h>
#include "Definitions.h"
#include "LibDHT11.h"
#include "ResponsiveAnalogRead.h"

SoftwareSerial _bluetoothSerial(PIN_BT_RECEIVE, PIN_BT_TRANSMIT);
LibDHT11 _dhtSensor;
ResponsiveAnalogRead _heightSensor(PIN_WATER_HEIGHT, true);

void libDHT_callback();

void setup()
{
    _dhtSensor.init(PIN_DHT, libDHT_callback);
    delay(DHT_INIT_TIME);

    pinMode(PIN_WATER_PUMP, OUTPUT);

#ifdef ENABLE_BLUETOOTH
    _bluetoothSerial.begin(SERIAL_SPEED);
#endif
#ifdef ENABLE_USB
    Serial.begin(SERIAL_SPEED);
#endif
}

void loop()
{
    _heightSensor.update();
    _dhtSensor.acquireAndWait();

    float height = _heightSensor.getValue();
    sendSerial(_dhtSensor.getHumidity(), DATA_HUMIDITY);
    sendSerial(_dhtSensor.getTemperature(), DATA_AIR_TEMP);
    sendSerial(height, DATA_WATER_HEIGHT);
    sendEndSerial();
    
    if (height > WATER_HEIGHT_LOWER && height < WATER_HEIGHT_UPPER)
    {
        digitalWrite(PIN_WATER_PUMP, HIGH);
    }
    else
    {
        digitalWrite(PIN_WATER_PUMP, LOW);
    }

    delay(500);
}
