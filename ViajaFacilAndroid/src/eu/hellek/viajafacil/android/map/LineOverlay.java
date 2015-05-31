package eu.hellek.viajafacil.android.map;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/*
 * Map Overlay to display a line (without stations)
 */
public class LineOverlay extends Overlay {

    private List<GeoPoint> points;
    private String color;
    private int level;

    public LineOverlay(List<GeoPoint> points, String color, int level) {
        this.points = points;
        this.color = color;
        this.level = level;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        Projection projection = mapView.getProjection();
        int pointsSize = points.size();
        if (shadow == false && pointsSize > 1) {
        	GeoPoint lastPoint = points.get(0);
        	for(int i = 1; i < pointsSize; i++) {
        		GeoPoint currentPoint = points.get(i);
	            Paint paint = new Paint();
	            paint.setAntiAlias(true);
	            Point point1 = new Point();
	            projection.toPixels(lastPoint, point1);
	            paint.setColor(Color.parseColor(color));
	            Point point2 = new Point();
	            projection.toPixels(currentPoint, point2);
	            if(level == 0) {
	            	paint.setStrokeWidth(2);
	            	paint.setAlpha(64);
	            } else {
	            	paint.setStrokeWidth(3);
	            }
	            canvas.drawLine((float) point1.x, (float) point1.y, (float) point2.x, (float) point2.y, paint);
	            lastPoint = currentPoint;
        	}/*
        	GeoPoint lastPoint = points.get(0);
        	Path path = new Path();
        	for(int i = 1; i < pointsSize; i++) {
	            Paint paint = new Paint();
	            paint.setDither(true);
	            paint.setColor(Color.RED);
	            paint.setStyle(Paint.Style.FILL_AND_STROKE);
	            paint.setStrokeJoin(Paint.Join.ROUND);
	            paint.setStrokeCap(Paint.Cap.ROUND);
	            paint.setStrokeWidth(2);
	            Point point1 = new Point();
	            projection.toPixels(lastPoint, point1);
	            paint.setColor(Color.BLUE);
	            Point point2 = new Point();
	            projection.toPixels(points.get(i), point2);
	            paint.setStrokeWidth(2);
//	            canvas.drawLine((float) point.x, (float) point.y, (float) point2.x, (float) point2.y, paint);
	            path.moveTo(point1.x, point1.y);
	            path.lineTo(point2.x, point2.y);
	            canvas.drawPath(path, paint);
        	}*/
        }
        return super.draw(canvas, mapView, shadow, when);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
    }

}