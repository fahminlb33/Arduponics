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


#ifndef DEFINITIONS_H
#define DEFINITIONS_H

// pins
#define PIN_DHT             2
#define PIN_WATER_HEIGHT    A0
#define PIN_BT_TRANSMIT     8
#define PIN_BT_RECEIVE      9
#define PIN_WATER_PUMP      12

// serial
#define ENABLE_BLUETOOTH
#define ENABLE_USB
#define SERIAL_SPEED        9600

// etc.
#define DHT_INIT_TIME		2000

// settings
const int WATER_HEIGHT_LOWER = 80;
const int WATER_HEIGHT_UPPER = 106;
enum SERIAL_MESSAGE {
    DATA_WATER_HEIGHT,    
    DATA_HUMIDITY,    
    DATA_AIR_TEMP
};

#endif
