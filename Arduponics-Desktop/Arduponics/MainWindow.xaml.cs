using System;
using System.IO.Ports;
using System.Text;
using System.Windows;
using Arduponics.Model;
using Arduponics.Services;
using LiveCharts;
using LiveCharts.Configurations;
using MahApps.Metro.Controls;
using MahApps.Metro.Controls.Dialogs;

namespace Arduponics
{
    /// <summary>
    /// Interaction logic for MainWindow.xamls
    /// </summary>
    public partial class MainWindow : MetroWindow
    {
        private ArduinoSerialPort _arduinoSerialPort;
        private MainWindowModel Model { get; set; }
        
        public MainWindow()
        {
            InitializeComponent();
            var mapper = Mappers.Xy<ChartValueModel>()
                .X(model => model.Timestamp.Ticks)
                .Y(model => model.Value);
            Charting.For<ChartValueModel>(mapper);

            Model = new MainWindowModel();      
            SetAxisLimits(DateTime.Now);

            _arduinoSerialPort = new ArduinoSerialPort(Dispatcher);
            _arduinoSerialPort.LogMessage += ArduinoSerialPort_LogMessage;
            _arduinoSerialPort.DataArrived += ArduinoSerialPort_DataArrived;

            DataContext = Model;
        }

        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            AddLog("Application loaded.");
            CboPorts.Items.Clear();

            var arr = SerialPort.GetPortNames();
            foreach (var portName in arr)
            {
                CboPorts.Items.Add(portName);
            }

            if (arr.Length > 0)
            {
                CboPorts.SelectedIndex = 0;
            }
        }

        private async void cmdAbout_Click(object sender, RoutedEventArgs e)
        {
            await this.ShowMessageAsync("About Arduponics", "Arduponics is made by Ahmad Sobari and Fahmi Noor Fiqri.");
        }

        private void cmdConnect_Click(object sender, RoutedEventArgs e)
        {
            if (!_arduinoSerialPort.IsConnected)
            {
                _arduinoSerialPort.Open(CboPorts.Text);
                Model.BtnConnectText = "DISCONNECT";
                Model.StatusText = "Connected.";
            }
            else
            {
                _arduinoSerialPort.Close();
                Model.BtnConnectText = "CONNECT";
                Model.StatusText = "Connect now.";
            }
        }

        private void ArduinoSerialPort_DataArrived(ArduponicsValues obj)
        {

            var now = DateTime.Now;
            Model.ChartWaterHeightData.Add(new ChartValueModel
            {
                Timestamp = now,
                Value = obj.WaterHeight
            });
            Model.ChartHumidityData.Add(new ChartValueModel
            {
                Timestamp = now,
                Value = obj.Humidity
            });
            Model.ChartTemperatureData.Add(new ChartValueModel
            {
                Timestamp = now,
                Value = obj.Temperature
            });

            SetAxisLimits(now);

            if (obj.Temperature > CommonModel.TemperatureLower && obj.Temperature < CommonModel.TemperatureUpper)
            {
                Model.StatusText = "Seems good.";
            }
            else if (obj.Temperature > CommonModel.TemperatureUpper)
            {
                Model.StatusText = "What a hot day!";
            }
            else if (obj.Temperature < CommonModel.TemperatureLower)
            {
                Model.StatusText = "Freezing here.";
            }

            Model.HumidityText = obj.Humidity + "%";
            Model.WaterHeightText = obj.WaterHeight + "%";
            Model.TemperatureText = obj.Temperature + "°C";

            if (Model.ChartWaterHeightData.Count <= CommonModel.PreserveDataCount) return;
            Model.ChartWaterHeightData.RemoveAt(0);
            Model.ChartHumidityData.RemoveAt(0);
            Model.ChartTemperatureData.RemoveAt(0);
        }

        private void ArduinoSerialPort_LogMessage(string obj)
        {
            AddLog(obj);
        }

        #region Helpers
        private void AddLog(string message)
        {
            var sb = new StringBuilder();
            sb.Append("[");
            sb.Append(DateTime.Now.ToLocalTime());
            sb.Append("] ");
            sb.Append(message);
            Dispatcher.Invoke(() =>
            {
                LstDebugLog.Items.Add(sb);
                
                LstDebugLog.SelectedIndex = LstDebugLog.Items.Count - 1;
                LstDebugLog.ScrollIntoView(LstDebugLog.SelectedItem);
            });
        }

        private void SetAxisLimits(DateTime now)
        {
            Model.AxisMax = now.Ticks + CommonModel.OneSecondTick; 
            Model.AxisMin = now.Ticks - CommonModel.EightSecondTick; 
        }
        #endregion
    }
}
