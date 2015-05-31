package eu.hellek.viajafacil.android.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo.UserFavouritePositionProxy;
import eu.hellek.viajafacil.android.R;
import eu.hellek.viajafacil.android.ViajaFacilActivity;

/*
 * overlays for from/to markers (A/B)
 */
public class MarkersOverlay extends ItemizedOverlay<OverlayItem> {

	private OverlayItem [] mOverlays = new OverlayItem[2];
	private ViajaFacilActivity mContext;

	public MarkersOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public MarkersOverlay(ViajaFacilActivity context) {
		this(context.getResources().getDrawable(R.drawable.markera));
		mContext = context;
/*		GeoPoint point1 = new GeoPoint((int)(-34.62671 * 1E6),(int)(-58.380489 * 1E6));
		GeoPoint point2 = new GeoPoint((int)(-34.609122 * 1E6),(int)(-58.407869 * 1E6));*/
		GeoPoint point1 = new GeoPoint(0, 0);
		GeoPoint point2 = new GeoPoint(0, 0);
		OverlayItem markerA = new OverlayItem(point1, mContext.getResources().getString(R.string.from), mContext.getResources().getString(R.string.from));
		OverlayItem markerB = new OverlayItem(point2, mContext.getResources().getString(R.string.to), mContext.getResources().getString(R.string.to));
		setOverlay(markerA, mContext.getResources().getDrawable(R.drawable.markera), ViajaFacilActivity.FROM);
		setOverlay(markerB, mContext.getResources().getDrawable(R.drawable.markerb), ViajaFacilActivity.TO);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays[i];
	}

	@Override
	public int size() {
		return 2;
	}

	private void setOverlay(OverlayItem overlay, Drawable image, int pos) {
		boundCenterBottom(image);
		overlay.setMarker(image);
		mOverlays[pos] = overlay;
	}

	public void updateMarkerPosition(int fromOrTo, GeoPoint pos) {
		OverlayItem marker;
		if(fromOrTo == ViajaFacilActivity.FROM) {
			marker = new OverlayItem(pos, mContext.getResources().getString(R.string.from), mContext.getResources().getString(R.string.from));
			setOverlay(marker, mContext.getResources().getDrawable(R.drawable.markera), ViajaFacilActivity.FROM);
		} else {
			marker = new OverlayItem(pos, mContext.getResources().getString(R.string.to), mContext.getResources().getString(R.string.to));
			setOverlay(marker, mContext.getResources().getDrawable(R.drawable.markerb), ViajaFacilActivity.TO);
		}
		populate();
	}

	public GeoPoint getFromPos() {
		return mOverlays[ViajaFacilActivity.FROM].getPoint();
	}

	public GeoPoint getToPos() {
		return mOverlays[ViajaFacilActivity.TO].getPoint();
	}

	@Override
	protected boolean onTap(int index) {
		if(ViajaFacilActivity.downloadedFavs) {
			final OverlayItem item = mOverlays[index];
			final TextView tv;
			if(index == ViajaFacilActivity.FROM) {
				tv = (TextView)mContext.findViewById(R.id.txtFrom);
			} else {
				tv = (TextView)mContext.findViewById(R.id.txtTo);
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.store_favorite);
			builder.setMessage(tv.getText());
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					UserFavouritePositionProxy.Builder builder = UserFavouritePositionProxy.newBuilder();
					builder.setKey("dontcare");
					builder.setName(tv.getText().toString());
					builder.setLat((double)(item.getPoint().getLatitudeE6() / 1E6));
					builder.setLon((double)(item.getPoint().getLongitudeE6() / 1E6));
					UserFavouritePositionProxy fpp = builder.build();
					mContext.addFavorite(fpp);
				}
			});
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return true;
	}

}
