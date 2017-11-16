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

void sendSerial(float data, SERIAL_MESSAGE message)
{
#ifdef ENABLE_BLUETOOTH
    _bluetoothSerial.print(String(message) + "-" + String(data) + "X");
#endif
#ifdef ENABLE_USB
    Serial.print(String(message) + "-" + String(data) + "X");
#endif
}

void sendEndSerial(){
#ifdef ENABLE_BLUETOOTH
    _bluetoothSerial.println();
#endif
#ifdef ENABLE_USB
    Serial.println();
#endif
}

void libDHT_callback(){
    _dhtSensor.isrCallback();
}
