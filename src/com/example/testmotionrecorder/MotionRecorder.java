package com.example.testmotionrecorder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

public class MotionRecorder extends Activity implements SensorEventListener{
	
	
    private long last_update = 0, last_movement = 0;
	private float prevX = 0, prevY = 0, prevZ = 0;
	private float curX = 0, curY = 0, curZ = 0;
	private ArrayList<Float> X;            
	private ArrayList<Float> Y;
	private ArrayList<Float> Z;
	private ArrayList<Float> XR;            
	private ArrayList<Float> YR;
	private ArrayList<Float> ZR;
	private final static String databaseDir = "/storage/sdcard0/";	
	private Timer T;
	private int counter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_motion_recorder);
        X = new ArrayList<Float>();            
        Y = new ArrayList<Float>();
        Z = new ArrayList<Float>();
        XR = new ArrayList<Float>();            
        YR = new ArrayList<Float>();
        ZR = new ArrayList<Float>();
        counter =0;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.motion_recorder, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_write) {
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Title");
			alert.setMessage("Message");

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);			
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  Editable value = input.getText();
			  
				writeSignal(X, value+"XAccelerometer.txt");
				writeSignal(Y, value+"YAccelerometer.txt");
				writeSignal(Z, value+"ZAccelerometer.txt");
				
				writeSignal(XR, value+"XRotation.txt");
				writeSignal(YR, value+"YRotation.txt");
				writeSignal(ZR, value+"ZRotation.txt");
				
				System.out.println(counter);
			  }
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});

			alert.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected void onResume() {
	        super.onResume();
	        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
	        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);        
	        if (sensors.size() > 0) {
	        	sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
	        }
	        SensorManager mSGi = (SensorManager) getSystemService(SENSOR_SERVICE);
	        List<Sensor> sen = sm.getSensorList(Sensor.TYPE_GYROSCOPE);
	        if (sen.size() > 0) {
	        	mSGi.registerListener(this, sen.get(0), SensorManager.SENSOR_DELAY_GAME);
	        }
	        T=new Timer();
	        T.scheduleAtFixedRate(new TimerTask() {         
	            @Override
	            public void run() {
	            	counter ++;
	            }
	        }, 1000, 1000); 
        
    }
	    
    @Override
    protected void onStop() {
	    	SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);    	
	        sm.unregisterListener(this);
	        super.onStop();        
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
	        synchronized (this) {
	        	long current_time = event.timestamp;
	            
	        	switch(event.sensor.getType()){
	        		case Sensor.TYPE_ACCELEROMETER:
	    	            
	        			curX = event.values[0];
	    	            curY = event.values[1];
	    	            curZ = event.values[2];
	    	            
	    	            
	    	            X.add(curX);
	    	            Y.add(curY);
	    	            Z.add(curZ);
	    	            
	    	            if (prevX == 0 && prevY == 0 && prevZ == 0) {
	    	                last_update = current_time;
	    	                last_movement = current_time;
	    	                prevX = curX;
	    	                prevY = curY;
	    	                prevZ = curZ;
	    	            }

	    	            long time_difference = current_time - last_update;
	    	            if (time_difference > 0) {
	    	                prevX = curX;
	    	                prevY = curY;
	    	                prevZ = curZ;
	    	                last_update = current_time;
	    	            }	        
	        			break;
	        		case Sensor.TYPE_GYROSCOPE:
	        			curX = event.values[0];
	    	            curY = event.values[1];
	    	            curZ = event.values[2];
	    	            
	    	            
	    	            XR.add(curX);
	    	            YR.add(curY);
	    	            ZR.add(curZ);
	        			break;
	        	}
	        	
    

	        }
			
	}
			   
	private void writeSignal(ArrayList<Float> puntos, String fileName) {
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;		
			String signal = null;
			if (puntos != null) {
				try {
					fos = new FileOutputStream(databaseDir + fileName);
					osw = new OutputStreamWriter(fos);
					
					signal = buildStringWithList(puntos);
					osw.write(signal);
				} catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} finally {
					try {
						osw.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
	}	
		
	private String buildStringWithList(ArrayList<Float> puntos) {
			StringBuilder coeff = null;
			if (puntos != null && puntos.size() > 0) {
				coeff = new StringBuilder();
				for (float s : puntos) {
					coeff.append(s);
					coeff.append("\n");
				}
			}
			return coeff.toString();
		}
	}
