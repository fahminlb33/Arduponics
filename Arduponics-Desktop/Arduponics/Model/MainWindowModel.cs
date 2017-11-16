using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using LiveCharts;

namespace Arduponics.Model
{
    public class MainWindowModel : INotifyPropertyChanged
    {
        private double _axisMax;
        private double _axisMin;
        private string _waterHeightText;
        private string _humidityText;
        private string _temperatureText;
        private string _statusText;
        private string _btnConnectText;

        public MainWindowModel()
        {
            StatusText = "Connect now.";
            WaterHeightText = "0%";
            HumidityText = "0%";
            TemperatureText = "0°C";

            BtnConnectText = "CONNECT";

            ChartWaterHeightData = new ChartValues<ChartValueModel>();
            ChartHumidityData = new ChartValues<ChartValueModel>();
            ChartTemperatureData = new ChartValues<ChartValueModel>();

            AxisStep = CommonModel.OneSecondTick;
            AxisUnit = TimeSpan.TicksPerSecond;

            DateTimeFormatter = value => new DateTime((long)value).ToString("mm:ss");
        }

        public string StatusText
        {
            get => _statusText;
            set
            {
                _statusText = value; 
                OnPropertyChanged();
            }
        }

        public string WaterHeightText
        {
            get => _waterHeightText;
            set
            {
                _waterHeightText = value;
                OnPropertyChanged();
            }
        }

        public string HumidityText
        {
            get => _humidityText;
            set
            {
                _humidityText = value; 
                OnPropertyChanged();
            }
        }

        public string TemperatureText
        {
            get => _temperatureText;
            set
            {
                _temperatureText = value; 
                OnPropertyChanged();
            }
        }

        public string BtnConnectText
        {
            get => _btnConnectText;
            set
            {
                _btnConnectText = value; 
                OnPropertyChanged();
            }
        }

        #region Charting
        public ChartValues<ChartValueModel> ChartWaterHeightData { get; set; }

        public ChartValues<ChartValueModel> ChartHumidityData { get; set; }

        public ChartValues<ChartValueModel> ChartTemperatureData { get; set; }

        public Func<double, string> DateTimeFormatter { get; set; }

        public Func<double, string> YFormatter { get; set; }

        public bool IsReading { get; set; }

        public string[] Labels { get; set; }

        public double AxisStep { get; set; }

        public double AxisUnit { get; set; }

        public double AxisMax
        {
            get => _axisMax;
            set
            {
                _axisMax = value;
                OnPropertyChanged();
            }
        }

        public double AxisMin
        {
            get => _axisMin;
            set
            {
                _axisMin = value;
                OnPropertyChanged();
            }
        }
        #endregion
        
        #region INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));

        } 
        #endregion
    }
}
