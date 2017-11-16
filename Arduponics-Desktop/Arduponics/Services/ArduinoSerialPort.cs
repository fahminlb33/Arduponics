using System;
using System.Diagnostics;
using System.IO.Ports;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Threading;
using Arduponics.Model;

namespace Arduponics.Services
{
    class ArduinoSerialPort
    {
        private readonly SerialPort _serialPort;
        private readonly StringBuilder _buffer;
        private readonly Dispatcher _dispatcher;

        public event Action<string> LogMessage;
        public event Action<ArduponicsValues> DataArrived;

        public bool IsConnected => _serialPort.IsOpen;

        public ArduinoSerialPort(Dispatcher dispatcher)
        {
            _dispatcher = dispatcher;
            _buffer = new StringBuilder();
            _serialPort = new SerialPort();
            _serialPort.DataReceived += serialPort_DataReceived;
        }

        public void Open(string portName)
        {
            Task.Run(() =>
            {
                try
                {
                    Close();
                    OnLogMessage("Connecting to host...");
                    _serialPort.PortName = portName;
                    _serialPort.BaudRate = 9600;
                    _serialPort.Handshake = Handshake.None;
                    _serialPort.Parity = Parity.None;
                    _serialPort.StopBits = StopBits.One;
                    _serialPort.NewLine = "\r\n";
                    _serialPort.Open();
                    OnLogMessage("Connected to host!");
                    OnLogMessage("Receiving data...");
                }
                catch
                {
                    OnLogMessage("Could not connect to host.");
                }
            });
        }

        public void Close()
        {
            if (_serialPort.IsOpen)
            {
                OnLogMessage("Closing existing serial port...");
                _serialPort.Close();
            }
            OnLogMessage("Port closed.");
        }

        private void serialPort_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            _buffer.Append(_serialPort.ReadExisting());
            if (_buffer.ToString().EndsWith("\r\n"))
            {
                Task.Run(() =>
                {
                    var buff = _buffer.ToString();
                    _buffer.Clear();

                    ParseData(buff);
                });
            }
        }

        private void ParseData(string data)
        {
            try
            {
                var entries = data.Split('X');
                var report = new ArduponicsValues();

                for (int i = 0; i < entries.Length - 1; i++)
                {
                    Debug.Print(entries[i]);
                    var splitted = entries[i].Split('-');
                    var value = Convert.ToInt32(splitted[1].Replace(".00", ""));
                    switch (Convert.ToInt32(splitted[0]))
                    {
                        case 0:
                            var result=Convert.ToInt32((double)value / 1024 * 100);
                            Debug.Print(result.ToString());
                            report.WaterHeight = result;
                            break;
                        case 1:
                            report.Humidity = value;
                            break;
                        case 2:
                            report.Temperature = value;
                            break;
                    }
                }
                OnDataArrived(report);
            }
            catch (Exception e)
            {
                Debug.WriteLine(e);
            }
        }

        protected virtual void OnLogMessage(string obj)
        {
            _dispatcher.BeginInvoke(DispatcherPriority.DataBind, new Action(() => LogMessage?.Invoke(obj)));
        }

        protected virtual void OnDataArrived(ArduponicsValues obj)
        {
            _dispatcher.BeginInvoke(DispatcherPriority.DataBind, new Action(() => DataArrived?.Invoke(obj)));
        }
    }
}
