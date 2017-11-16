
#ifndef LIBDHT11_H
#define LIBDHT11_H

#if defined(ARDUINO) && ARDUINO >= 100
#include "Arduino.h"
#endif

// state codes
#define DHT11_OK                0
#define DHT11_ACQUIRING         1
#define DHT11_ACQUIRED          2
#define DHT11_RESPONSE_OK       3

// error codes
#define DHT11_ERROR_CHECKSUM    -1
#define DHT11_ERROR_TIMEOUT     -2
#define DHT11_ERROR_ACQUIRING   -3
#define DHT11_ERROR_DELTA       -4
#define DHT11_ERROR_NOTSTARTED  -5

#define DHT11_CHECK_STATE       \
    if (state == STOPPED)       \
        return status;          \
    else if (state != ACQUIRED) \
        return DHT11_ERROR_ACQUIRING;

class LibDHT11
{
  public:
    LibDHT11();
    void init(int pin, void (*isrCallback_wrapper)());
    void isrCallback();
    int acquire();
    inline float getTemperature() {DHT11_CHECK_STATE; return temp;};
    inline float getHumidity(){DHT11_CHECK_STATE; return hum;};
    inline void acquireAndWait(){acquire(); while (acquiring());};
    inline bool acquiring() {if (state != ACQUIRED && state != STOPPED) return true; return false;};
    inline int getStatus() {return status;};

  private:
    // callback wrapper
    void (*isrCallback_wrapper)(void);

    // states
    enum states
    {
        RESPONSE    = 0,
        DATA        = 1,
        ACQUIRED    = 2,
        STOPPED     = 3,
        ACQUIRING   = 4
    };

    // variables
    volatile states state;
    volatile int status;
    volatile byte bits[5];
    volatile byte cnt;
    volatile byte idx;
    volatile int us;
    int intNumber;
    int pin;
    volatile float hum;
    volatile float temp;
};

#endif