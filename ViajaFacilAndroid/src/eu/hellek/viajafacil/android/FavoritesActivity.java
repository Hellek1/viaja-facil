package eu.hellek.viajafacil.android;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo.UserFavouritePositionProxy;

/*
 * Activity that lists the users favorite positions. It only allows selecting a position or deleting one (long-press context-menu)
 */
public class FavoritesActivity extends ListActivity {
	
	private FavoritesDbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_list);
        mDbHelper = new FavoritesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }
    
    private void fillData() {
        Cursor favoritesCursor = mDbHelper.fetchAllFavorites();
        startManagingCursor(favoritesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{FavoritesDbAdapter.KEY_ADDR};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.favorites_row, favoritesCursor, from, to);
        setListAdapter(notes);
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor c = mDbHelper.fetchFavorite(id);
		startManagingCursor(c);
		String addr = c.getString(c.getColumnIndexOrThrow(FavoritesDbAdapter.KEY_ADDR));
		float lat = c.getFloat(c.getColumnIndexOrThrow(FavoritesDbAdapter.KEY_LAT));
		float lon = c.getFloat(c.getColumnIndexOrThrow(FavoritesDbAdapter.KEY_LON));
		Intent data = new Intent();
        data.putExtra("addr", addr);
        data.putExtra("lat", lat);
        data.putExtra("lon", lon);
        setResult(RESULT_OK, data);
        finish();
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 1:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                Cursor c = mDbHelper.fetchFavorite(info.id);
                startManagingCursor(c);
                deleteFavorite(c.getString(c.getColumnIndexOrThrow(FavoritesDbAdapter.KEY_AE_KEY)));
                mDbHelper.deleteFavorite(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    mDbHelper.close();
	}
	
	private Context getContext() {
		return this;
	}
	
	private void deleteFavorite(String key) {
		UserFavouritePositionProxy.Builder builder = UserFavouritePositionProxy.newBuilder();
        builder.setKey(key);
        builder.setName("dontcare");
        builder.setLat(1);
        builder.setLon(2);
        UserFavouritePositionProxy fpp = builder.build();
		new DeleteFavoriteTask().execute(fpp);
	}
	
	private class DeleteFavoriteTask extends AsyncTask<UserFavouritePositionProxy, Void, UserFavouritePositionProxy> {
		
		@Override
		protected UserFavouritePositionProxy doInBackground(UserFavouritePositionProxy... params) {
			Log.d("DeleteFavoriteTask", "DeleteFavoriteTask called.");
			try {
				DefaultHttpClient http_client = new DefaultHttpClient();
				http_client.getCookieStore().addCookie(ViajaFacilActivity.appEngineCookie);
				HttpPost method = new HttpPost("https://"+ViajaFacilActivity.appurlssl+"/rm/RemoveFavoriteServlet");
				ByteArrayEntity byteArrEntity = new ByteArrayEntity(params[0].toByteArray());
				byteArrEntity.setContentType("application/x-protobuf");
				method.setEntity(byteArrEntity);
				HttpResponse response = http_client.execute(method);
				response.getEntity().getContentLength();
				return params[0];
			} catch(Exception e) {
				Log.e("DeleteFavoriteTask", "Error", e);
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(UserFavouritePositionProxy result) {
			if(result == null) {
				Toast.makeText(getContext(), "Error: " + getContext().getResources().getString(R.string.failed_deleting_fav), Toast.LENGTH_LONG).show();
			}
		}
	}
	
}
