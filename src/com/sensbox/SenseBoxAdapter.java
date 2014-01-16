/*TÜRKAY BÝLÝYOR   turkaybiliyor@hotmail.com*/
package com.sensbox;
import java.util.ArrayList;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
public class SenseBoxAdapter {

	private static final String LOG_TAG = "SenseBoxAdapter";
	private static final UUID SERVICE_10 = UUID.fromString("aba27100-143b-4b81-a444-edcd0000f010");
	private static final UUID CHAR_12 = UUID.fromString("aba27100-143b-4b81-a444-edcd0000f012");
	private static final UUID SERVICE_20 = UUID.fromString("aba27100-143b-4b81-a444-edcd0000f020");	
	private static final UUID CHAR_22 = UUID.fromString("aba27100-143b-4b81-a444-edcd0000f022");
	private static final UUID CHAR_23 = UUID.fromString("aba27100-143b-4b81-a444-edcd0000f023");
	private static final UUID CHAR_24 = UUID.fromString("aba27100-143b-4b81-a444-edcd0000f024");
	private static final UUID CHAR_25 = UUID.fromString("aba27100-143b-4b81-a444-edcd0000f025");

	private static final long SLEEP = 1;
	private static final int MAX_COUNTER = 5000;
	private static final double LAT_LON_DIVIDE = 10000000; // 10^7

	private static final boolean STATE_DISCONNECTED = false;
	private static final boolean STATE_CONNECTED = true;
	private static final boolean SERVICES_NOT_DISCOVERED = false;
	private static final boolean SERVICES_DISCOVERED = true;
	private static final boolean CHARACTERISTIC_NOT_READ = false;
	private static final boolean CHARACTERISTIC_READ = true;

	private static BluetoothAdapter btadapter;
	private static BluetoothGatt btGatt;
	private static Context context;

	private ArrayList<BluetoothDevice> found_devices;
	private boolean btConnectionState = STATE_DISCONNECTED;
	private boolean btServicesDiscovered = SERVICES_NOT_DISCOVERED;
	private boolean btCharacteristicRead = CHARACTERISTIC_NOT_READ;

	private int compassDegree;
	private int sats;

	private int loglevel=0,sdfix=0,gpsfix=0,battlevel=0,battstatus=0,logstart=0;
	private double pressureAltitude=0;
	private double acceleration=0;
	private double groundSpeed=0;
	private double vario=0;
	private double temp=0;
	private String latitude;
	private String longitude;
	private String altitude;
	private String heading;
	private String readdata;
	private long gpstime;
	private String battery,status1,status2;

	public SenseBoxAdapter() {
		btadapter = BluetoothAdapter.getDefaultAdapter();
	}

	public boolean connect() {

		if (btConnectionState == STATE_CONNECTED) {
			return true;
		}

		try {
			Log.d(LOG_TAG, "Try to connect to SenseBox");

			found_devices = new ArrayList<BluetoothDevice>();
			Log.i(LOG_TAG, "Discovering device...");

			btadapter.startLeScan(btLeScanCallback);

			// wait until SenseBox has been found
			int counter = 0;
			boolean waitForCallback = true;
			while (waitForCallback) {
				if (found_devices.size() > 0 || counter > MAX_COUNTER) {
					btadapter.stopLeScan(btLeScanCallback);
					waitForCallback = false;
				}
				counter++;
				Thread.sleep(SLEEP);
			}

			btGatt = found_devices.get(0).connectGatt(context, false, btGattCallback);

			// wait until connection state changed
			counter = 0;
			waitForCallback = true;
			while (waitForCallback) {
				if (btConnectionState) {
					Log.i(LOG_TAG, "Connected to SenseBox.");					
					waitForCallback = false;
				}
				if (counter > MAX_COUNTER) {
					Log.w(LOG_TAG, "No connection to SenseBox.");					
					waitForCallback = false;
					return false;
				}
				counter++;
				Thread.sleep(SLEEP);
			}

		} catch (Exception e) {
			Log.d(LOG_TAG, "Got Exception " + e.getMessage());
			return false;
		}

		return btConnectionState;
	}

	private void addDevice(BluetoothDevice device) {
		if (!found_devices.contains(device)) {
			Log.i(LOG_TAG, "Device stored.");
			found_devices.add(device);
		}
	}
	static String devicename,deviceaddress;
	public String getDeviceName() {
		return devicename;
	}
	public String getDeviceAddress() {
		return deviceaddress;
	}
	/* Device scan callback. */
	private final BluetoothAdapter.LeScanCallback btLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device.getName().startsWith("FS")) {
				Log.i(LOG_TAG, "Device: " + device.getName());
				addDevice(device);
				devicename=device.getName();
				deviceaddress=device.getAddress();
			}
		}
	};

	private final BluetoothGattCallback btGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				btConnectionState = STATE_CONNECTED;
				Log.i(LOG_TAG, "Connected to GATT server.");
				Log.i(LOG_TAG, "Attempting to start service discovery:" + btGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				btConnectionState = STATE_DISCONNECTED;
				Log.i(LOG_TAG, "Disconnected from GATT server.");
			}
		}

		@Override
		// New services discovered
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				btServicesDiscovered = SERVICES_DISCOVERED;
				Log.d(LOG_TAG, "onServicesDiscovered received: " + status);
			} else {
				btServicesDiscovered = SERVICES_NOT_DISCOVERED;
				Log.w(LOG_TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		// Result of a characteristic read operation
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				btCharacteristicRead = CHARACTERISTIC_READ;
			} else {
				btCharacteristicRead = CHARACTERISTIC_NOT_READ;
			}
		}

	};

	public void onDestroy() {
		Log.i(LOG_TAG, "onDestroy " + this.getClass());
		btConnectionState = STATE_DISCONNECTED;
		btServicesDiscovered = SERVICES_NOT_DISCOVERED;
		btCharacteristicRead = CHARACTERISTIC_NOT_READ;
		if (btGatt != null) {
			btGatt.close();
			btGatt = null;
		}
	}

	public boolean reconnect() {
		onDestroy();
		return connect();
	}

	public void readAllNewValues() {	
		if(btConnectionState==STATE_CONNECTED)	
		{
			refreshChar22();
			refreshChar23();
			refreshChar24();
			refreshChar25();		
		}
	}

	public double getPressureAttitude() {
		return pressureAltitude;
	}

	public int getCompassDegree() {
		return compassDegree;
	}
	
	public double getAcceleration() {
		return acceleration;
	}

	public double getHeading() {
		return Integer.parseInt(heading);
	}
	public int getGroundSpeed() {
		return (int) (groundSpeed* 3.6);
	}
	public double getVario() {
		return vario;
	}
	public double getTemp() {
		return temp;
	}
	public String getGPS() {
		return latitude + "," + longitude + "," + altitude + "," + heading + "," + String.valueOf((groundSpeed * 3.6));
	}
	public String getReadData() {
		return readdata;
	}
	public double getLat() {
		return Double.parseDouble(latitude);
	}
	public double getLng() {
		return Double.parseDouble(longitude);
	}
	public int getGpsAlt() {
		return Integer.parseInt(altitude);
	}	
	public long getGpsTime() {
		return gpstime;
	}	
	public int getBatteryLevel() {
		return battlevel;
	}	
	public int getBatteryStatus() {
		return battstatus;
	}	
	public int getLogLevel() {
		return loglevel;
	}
	public int getSdFix() {
		return sdfix;
	}
	public int getGpsFix() {
		return gpsfix;
	}
	public int getLogStatus() {
		return logstart;
	}
	public int getSats() {
		return sats;
	}	
	
	private void refreshChar22() {
		try {
			BluetoothGattCharacteristic bleGattChar = getChar(SERVICE_20, CHAR_22);			
			Integer time = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
			gpstime = (long) time.intValue() *1000;
			Integer lat = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 4);
			latitude = String.valueOf(((double) lat.intValue()) / LAT_LON_DIVIDE);
			Integer lon = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 8);
			longitude = String.valueOf(((double) lon.intValue()) / LAT_LON_DIVIDE);
			Integer alt = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 12);
			altitude = String.valueOf(alt.intValue());		
			
		} catch (Exception e) {
			Log.e("SenseBoxModel - " + "refreshChar22", "Got Exception " + e.getMessage());
			reconnect();
		}
	}

	private void refreshChar23() {
		try {
			BluetoothGattCharacteristic bleGattChar = getChar(SERVICE_20, CHAR_23);
			Integer preAlt = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
			pressureAltitude = (double) preAlt.intValue()/100;
			// save compass value
			Integer yawInt = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 12);
			int degree = yawInt.intValue();
			degree /= 10;
			if (degree < 0) {
				degree += 360;
			}
			compassDegree = degree;

			// save acceleration value
			Integer acc = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 16);
			acceleration = acc.doubleValue() / 10;

			// save ground speed value
			Integer grSpeed = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 6);
			groundSpeed = grSpeed.doubleValue() / 10;

			// save vario value
			Integer var = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
			vario = var.doubleValue() / 100;

			// save heading
			Integer head = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 8);
			int h = head.intValue();
			h /= 10;
			if (h < 0) {
				h += 360;
			}
			heading = String.valueOf(h);

		} catch (Exception e) {
			Log.e("SenseBoxModel - " + "refreshChar23", "Got Exception " + e.getMessage());
			reconnect();
		}
	}
	private String[] readCharacteristic(BluetoothGattCharacteristic bleGattChar)
			{
		    int intl=0;
		    String[] parts = null;
			final byte[] data = bleGattChar.getValue();
		    if (data != null && data.length > 0) {
		        final StringBuilder stringBuilder = new StringBuilder(data.length);
		        for (byte byteChar : data)
		        {       
		        	if(intl<data.length-1)         	
		        		stringBuilder.append(String.format("%02X-", byteChar));
		        	else
		        		stringBuilder.append(String.format("%02X", byteChar));
		        		intl++;
		        }   
		        parts = stringBuilder.toString().split("-");  
		        readdata=parts.toString();
		    }
			return parts;
    }	
	private void refreshChar24() {
		try {
			BluetoothGattCharacteristic bleGattChar = getChar(SERVICE_20, CHAR_24);		
			String[] parts = readCharacteristic(bleGattChar);			 
            Integer gpssats = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 6);
			sats = gpssats.intValue();		
			String status = hexToBin(parts[7]); 
	        gpsfix=binaryToInteger(status.substring(5,8));
	        logstart=binaryToInteger(status.substring(1,2));
		} catch (Exception e) {
			Log.e("SenseBoxModel - " + "refreshChar24", "Got Exception " + e.getMessage());
			reconnect();
		}
	}
	private void refreshChar25() {
		try {
			BluetoothGattCharacteristic bleGattChar = getChar(SERVICE_20, CHAR_25);				
			String[] parts = readCharacteristic(bleGattChar);		
	            Integer logl = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 5);
				loglevel = logl.intValue();
				Integer var = bleGattChar.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 6);
				temp = var.doubleValue() / 10;
	            battery=hexToBin(parts[4]);    
            	status2=hexToBin(parts[9]);	
            	battstatus=Integer.parseInt(battery.substring(0,1));            		
            	battlevel=binaryToInteger(battery.substring(1,8));            	
	            sdfix=binaryToInteger(status2.substring(7, 8));  	           
		} catch (Exception e) {
			Log.e("SenseBoxModel - " + "refreshChar25", "Got Exception " + e.getMessage());
			reconnect();
		}
	}
	public Integer binaryToInteger(String binary){
        char[] numbers = binary.toCharArray();
        Integer result = 0;
        int count = 0;
        for(int i=numbers.length-1;i>=0;i--){
             if(numbers[i]=='1')result+=(int)Math.pow(2, count);
             count++;
        }
        return result;
    }
	private String hexToBin(String hex){
        String bin = "";
        String binFragment = "";
        int iHex;
        hex = hex.trim();
        hex = hex.replaceFirst("0x", "");

        for(int i = 0; i < hex.length(); i++){
            iHex = Integer.parseInt(""+hex.charAt(i),16);
            binFragment = Integer.toBinaryString(iHex);

            while(binFragment.length() < 4){
                binFragment = "0" + binFragment;
            }
            bin += binFragment;
        }
        return bin;
    }
	private BluetoothGattCharacteristic getChar(UUID uuidService, UUID uuidChar) throws Exception {
		BluetoothGattCharacteristic bleGattChar = null;
		boolean waitForCallback = true;

		if (!btConnectionState) {
			if (btGatt.connect()) {
				Log.d(LOG_TAG, "Reconnecting to SenseBox was successfully.");
			} else {
				Log.w(LOG_TAG, "Reconnecting to SenseBox was unsuccessfully.");
				if (!reconnect()) {
					throw new Exception("Reconnecting to SenseBox was unsuccessfully.");
				}
			}
		}

		if (btServicesDiscovered || btGatt.discoverServices()) {
			// wait until the services have been discovered
			int counter = 0;
			while (!btServicesDiscovered) {
				if (counter > MAX_COUNTER) {
					Log.e(LOG_TAG, "SenseBox services not disvovered.");
					throw new Exception("SenseBox services not disvovered.");
				}
				counter++;
				Thread.sleep(SLEEP);
			}

			Log.i(LOG_TAG, "SenseBox services disvovered.");

			BluetoothGattService service = btGatt.getService(uuidService);

			bleGattChar = service.getCharacteristic(uuidChar);

			Log.d(LOG_TAG, "Properties: " + bleGattChar.getProperties());

			btCharacteristicRead = CHARACTERISTIC_NOT_READ;
			if (btGatt.readCharacteristic(bleGattChar)) {
				// wait until the characteristic have been read
				counter = 0;
				waitForCallback = true;
				while (waitForCallback) {
					if (btCharacteristicRead) {
						Log.i(LOG_TAG, "Characteristic have been read.");
						waitForCallback = false;
					}
					if (counter > MAX_COUNTER) {
						Log.e(LOG_TAG, "Characteristic could not have been read.");
						throw new Exception("Characteristic could not have been read.");
					}
					counter++;
					Thread.sleep(SLEEP);
				}

			}

		}
		return bleGattChar;
	}

}
