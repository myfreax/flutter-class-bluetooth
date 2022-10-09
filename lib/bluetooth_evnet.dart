import 'package:class_bluetooth/bluetooth_device.dart';

class BluetoothEvent {
  BluetoothDevice bluetoothDevice;
  String eventName;
  BluetoothEvent(this.bluetoothDevice, this.eventName);

  BluetoothEvent.fromJson(Map<String, dynamic> map)
      : eventName = map["eventName"],
        bluetoothDevice = BluetoothDevice.fromJson(map["bluetoothDevice"]);
}
