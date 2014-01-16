/*TÜRKAY BÝLÝYOR   turkaybiliyor@hotmail.com*/
package com.sensbox;

import javax.microedition.khronos.opengles.GL10;
import com.nutiteq.MapView;
import com.nutiteq.geometry.VectorElement;
import com.nutiteq.ui.MapListener;

public class MapEventListener extends MapListener {

    private static SensBoxActivity activity;
    private MapView mapView;
              // activity is often useful to handle click events 
    public MapEventListener(SensBoxActivity activity, MapView mapView) {
        this.activity = activity;
        this.mapView = mapView;
    }    
    // Reset activity and map view
    public void reset(SensBoxActivity activity, MapView mapView) {
        this.activity = activity;
        this.mapView = mapView;   
    }
      // Map drawing callbacks for OpenGL manipulations
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {  
    	 
    }    
    @Override
    public void onDrawFrameAfter3D(GL10 gl, float zoomPow2) {       	  
    }
    @Override
    public void onDrawFrame(GL10 gl) {     	
    }
   
    @Override
    public void onDrawFrameBefore3D(GL10 gl, float zoomPow2) {        	     
           mapView.requestRender();     
    }
    @Override
    public void onMapMoved() {
    	//activity.setInfo(mapView.getZoom());
    }    
    
	@Override
	public void onLabelClicked(VectorElement arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onMapClicked(double arg0, double arg1, boolean arg2) {
		// TODO Auto-generated method stub		
	}
	@Override
	public void onVectorElementClicked(VectorElement arg0, double arg1,
			double arg2, boolean arg3) {
		// TODO Auto-generated method stub		
	}	
}
