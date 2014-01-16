/*TÜRKAY BÝLÝYOR   turkaybiliyor@hotmail.com*/
package com.sensbox;

public interface PositionWriter {
    void emitProlog();
    void emitPosition(long time, double latitude, double longitude, float altitude, int bearing,
                    float groundSpeed);  
    void emitEpilog();
    int getLWCount();
	void emitSessionlessPosition(long time, double latitude, double longitude,
			float altitude, int bearing, float groundSpeed);	
}