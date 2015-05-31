package eu.hellek.viajafacil.android.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/*
 * Overlay for red circle around position when user  zooms to a certain position via link in search results
 */
public class MarkerCircleOverlay extends Overlay {

    private GeoPoint gp;

    public MarkerCircleOverlay(GeoPoint gp) {
        this.gp = gp;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        Projection projection = mapView.getProjection();
        if (shadow == false) {
	            Paint paint = new Paint();
	            Point point = new Point();
	            projection.toPixels(gp, point);
	            paint.setColor(Color.RED);
	            paint.setAlpha(64);
	            paint.setAntiAlias(true);
	            canvas.drawCircle(point.x, point.y, mapView.getProjection().metersToEquatorPixels(150), paint);
        }
        return super.draw(canvas, mapView, shadow, when);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
    }

}