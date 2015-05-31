package eu.hellek.viajafacil.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/*
 * Activity that lists the user's accounts so that he can select which one he wants to use to log in (to view or store his favorites)
 */
public class AccountListActivity extends ListActivity {

	protected AccountManager accountManager;
	protected Intent intent;
	private Account[] accounts;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountManager = AccountManager.get(getApplicationContext());
		accounts = accountManager.getAccountsByType("com.google");
		String[] names = new String[accounts.length];
		for(int i = 0; i < accounts.length; i++) {
			names[i] = accounts[i].name;
		}
		this.setListAdapter(new ArrayAdapter<Object>(this, R.layout.accounts_row, names));        
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Account account = accounts[position];
		final SharedPreferences preferences = getSharedPreferences(ViajaFacilActivity.PREFS_ACCOUNT_PREFS, Activity.MODE_PRIVATE);
		preferences.edit().putString(ViajaFacilActivity.PREFS_ACCOUNT_NAME, account.toString()).commit();
		Intent intent = new Intent(this, ViajaFacilActivity.class);
		intent.putExtra("account", account);
		setResult(RESULT_OK, intent);
		finish();
	}
}