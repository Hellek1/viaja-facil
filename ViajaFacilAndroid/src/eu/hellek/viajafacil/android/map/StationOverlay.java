package eu.hellek.viajafacil.android.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/*
 * Overlay that displays a station name
 */
public class StationOverlay extends Overlay {
	
	private static final int baseTextSize = 10;

    private String text;
    private GeoPoint gp;
    private int minZoom;
    private String color;

    public StationOverlay(String text, GeoPoint gp, String color, int minZoom) {
        this.text = text;
        this.gp = gp;
        this.minZoom = minZoom;
        this.color = color;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        Projection projection = mapView.getProjection();
        if (shadow == false && mapView.getZoomLevel() >= minZoom) {
        		int zoomExtra = mapView.getZoomLevel() - minZoom;
	            Paint rectPaint = new Paint();
	            Paint textPaint = new Paint();
	            Point point = new Point();
	            projection.toPixels(gp, point);
	            rectPaint.setColor(Color.GRAY);
	            rectPaint.setAlpha(128);
	            rectPaint.setStrokeWidth(2);
	            textPaint.setTextSize(baseTextSize + (zoomExtra * 2));
	            textPaint.setAntiAlias(true);
	            //textPaint.setColor(Color.RED);
	            textPaint.setColor(Color.parseColor(color));
	            textPaint.setTextAlign(Align.LEFT);
	            float textwidth = textPaint.measureText(text);
	            canvas.drawRect(point.x, point.y, point.x + 4 + textwidth, point.y + 4 + textPaint.getTextSize(), rectPaint);
	            rectPaint.setColor(Color.WHITE);
	            canvas.drawCircle(point.x + 2, point.y + 2, 4, rectPaint);
	            canvas.drawText(text, point.x + 1, point.y + textPaint.getTextSize(), textPaint);
        }
        return super.draw(canvas, mapView, shadow, when);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
    }

}