package eu.hellek.viajafacil.android.map;

import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import eu.hellek.viajafacil.android.ViajaFacilActivity;

/*
 * used together with MapGestureDetectorOverlay to allow setting positions from map by long-pressing
 */
public class MyOnGestureListener implements OnGestureListener {
	
	private ViajaFacilActivity ctx;
	
	public MyOnGestureListener(ViajaFacilActivity ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		ctx.showDialog(ViajaFacilActivity.DIALOG_FROMMAP);
		ctx.setMapLongTouchXY((int)e.getX(), (int)e.getY());
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
