using System;

namespace Arduponics.Model
{
    public static class CommonModel
    {
        public static readonly long OneSecondTick = TimeSpan.FromSeconds(1).Ticks;
        public static readonly long EightSecondTick = TimeSpan.FromSeconds(8).Ticks;
        public const int PreserveDataCount = 20;

        public const int TemperatureUpper = 25;
        public const int TemperatureLower = 22;
    }
}
