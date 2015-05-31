package eu.hellek.viajafacil.android;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.google.android.maps.GeoPoint;

import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.LineProxy;

/*
 * Activity that lists the results of a search in text representation
 */
public class ResultsActivity extends ExpandableListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);
		
		setListAdapter(new ResultListAdapter(this));
		SearchResultHolder.getInstance().getAtIndex(0).setExpanded(true);
		for(int i = 0; i < SearchResultHolder.getInstance().numResults(); i++) {
			if(SearchResultHolder.getInstance().getAtIndex(i).isExpanded()) {
				getExpandableListView().expandGroup(i);
			}
		}
	}
	
	@Override
	/*
	 * selects the result for display on the map and leaves the activity (to return to the main activity that contains the map)
	 */
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Intent data = new Intent();
		data.putExtra("resultid", groupPosition);
		setResult(RESULT_OK, data);
		finish();
		return true;
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		super.onGroupExpand(groupPosition);
		SearchResultHolder.getInstance().getAtIndex(groupPosition).setExpanded(true);
	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		super.onGroupCollapse(groupPosition);
		SearchResultHolder.getInstance().getAtIndex(groupPosition).setExpanded(false);
	}

	private class ResultListAdapter extends BaseExpandableListAdapter {
		
		private Context mContext;
		
		public ResultListAdapter (Context ctx) {
			this.mContext = ctx;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return SearchResultHolder.getInstance().getAtIndex(groupPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			ChildView cv;
            if (convertView == null) {
                cv = new ChildView(mContext, SearchResultHolder.getInstance().getAtIndex(groupPosition), groupPosition);
            } else {
            	cv = (ChildView)convertView;
                cv.updateConnection(SearchResultHolder.getInstance().getAtIndex(groupPosition), groupPosition);
            }
            return cv;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return SearchResultHolder.getInstance().getAtIndex(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return SearchResultHolder.getInstance().numResults();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			GroupView gv;
            if (convertView == null) {
                gv = new GroupView(mContext, SearchResultHolder.getInstance().getAtIndex(groupPosition));
            } else {
            	gv = (GroupView)convertView;
                gv.updateTitle(SearchResultHolder.getInstance().getAtIndex(groupPosition));
            }
            return gv;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
	}
	
	/*
	 * View for a connection-element
	 */
	private class GroupView extends LinearLayout {

		private TextView mTitle;

		public GroupView(Context context, ConnectionProxy conn) {
			super(context);

			this.setOrientation(VERTICAL);
//			this.setPadding(40, 0, 0, 0);

			mTitle = new TextView(context);
			mTitle.setTextSize(20);
			updateTitle(conn);
			LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(60, 10, 0, 10);
			addView(mTitle, layoutParams);
		}

		/*
		 * generates the String that summarizes a connection (i.e. "X minutes using colectivo 100, subte C") and sets the title to that string
		 */
		public void updateTitle(ConnectionProxy connProxy) {
			String connection = connProxy.getTime() + this.getResources().getString(R.string.min) + " " + this.getResources().getString(R.string.with) + " ";
			for(LineProxy l : connProxy.getLines()) {
				if(l.getType() != 0) {
					String lineText = l.getLinenum();
					if(l.getType() == 1 && connProxy.getLines().size() == 3) {
						String[] parts1 = l.getRamal().split("-");
						if(parts1.length == 2) {
							lineText += " " + parts1[0].substring(0, parts1[0].length() - 1);
						}
					}
					connection += l.getTypeAsString() + " ";
					connection += lineText + ", ";
				}
			}
			connection = connection.substring(0, connection.length()-2);
			mTitle.setText(connection);
		}
	}
	
	/*
	 * View for the details of a connection
	 */
	private class ChildView extends LinearLayout {

		private Context mContext;

		public ChildView(Context context, ConnectionProxy conn, int groupPosition) {
			super(context);
			this.mContext = context;
			this.setOrientation(VERTICAL);

			updateConnection(conn, groupPosition);
//			addView(mText, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}

		/*
		 * generates the text description of a connection. i.e. "Take colectivo 100 from Av. Güemes until Av. Brasil"
		 * including links that allow the user to zoom to those end-points on the map
		 */
		private void updateConnection(ConnectionProxy conn, int groupPosition) {
			this.removeAllViews();
			int col = 0;
			for(LineProxy l : conn.getLines()) {
				if(col >= ViajaFacilActivity.NUM_COLORS) {
					col = col % ViajaFacilActivity.NUM_COLORS;
				}
				if(l.getType() != 0) {
					TextView line = new TextView(mContext);
					line.setText(l.getLinenum() + " ", BufferType.SPANNABLE);
					/*lineNum.setTextColor(getColor(col));
					lineNum.setTextSize(15);*/
					line.append(l.getRamal());
					Spannable str = line.getEditableText();
					str.setSpan(new RelativeSizeSpan(1.25f), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					str.setSpan(new ForegroundColorSpan(getColor(col)), 0, l.getLinenum().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					col++;
					if(l.getAlternativeLines().size() > 0) {
						String alternativesText = " (" + mContext.getResources().getString(R.string.alternatives) + ": ";
						for(String s : l.getAlternativeLines()) {
							alternativesText += s + ", ";
						}
						alternativesText = alternativesText.substring(0, alternativesText.length() - 2);
						alternativesText += ")";
						int currLength = line.getText().length();
						line.append(alternativesText);
						Spannable str2 = line.getEditableText();
						str2.setSpan(new RelativeSizeSpan(0.75f), currLength, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						/*TextView alternatives = new TextView(mContext);
						alternatives.setText(alternativesText);
						alternatives.setTextSize(12);
						addView(alternatives, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));*/
					}
					addView(line, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					if(l.getStartStreet() != null && l.getDestStreet() != null) {
						String text = mContext.getResources().getString(R.string.from) + " " + l.getStartStreet();
						text += " " + mContext.getResources().getString(R.string.to) + " " + l.getDestStreet();
						TextView track = new TextView(mContext);
						track.setText(text, BufferType.SPANNABLE);
						track.append("");
						track.setMovementMethod(LinkMovementMethod.getInstance());
						track.setFocusable(false);
						int index1 = text.indexOf(l.getStartStreet());
						int index2 = text.indexOf(l.getDestStreet());
						Spannable str3 = track.getEditableText();
						str3.setSpan(new BackgroundColorSpan(R.color.lavender), index1, index1+l.getStartStreet().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						str3.setSpan(new BackgroundColorSpan(R.color.lavender), index2, index2+l.getDestStreet().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						str3.setSpan(new MyClickableSpan(new GeoPoint((int)(l.getRelevantPoints().get(0) * 1E6),(int)(l.getRelevantPoints().get(1) * 1E6)), groupPosition), index1, index1+l.getStartStreet().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						str3.setSpan(new MyClickableSpan(new GeoPoint((int)(l.getRelevantPoints().get(l.getRelevantPoints().size()-2) * 1E6),(int)(l.getRelevantPoints().get(l.getRelevantPoints().size()-1) * 1E6)), groupPosition), index2, index2+l.getDestStreet().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						addView(track, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					}
				}
			}
		}
	}
	
	public int getColor(int i) {
		switch(i) {
		case 0:
			return this.getResources().getColor(R.color.red);
		case 1:
			return this.getResources().getColor(R.color.blue);
		case 2:
			return this.getResources().getColor(R.color.green);
		case 3:
			return this.getResources().getColor(R.color.ff00ff);
		case 4:
			return this.getResources().getColor(R.color.yellow);
		default:
			Log.e("getColor", "This point should not be reached.");
			return this.getResources().getColor(R.color.red);
		}
	}
	
	/*
	 * the links that allow zooming to a point on a map when clicked
	 */
	private class MyClickableSpan extends ClickableSpan {
		
		private GeoPoint pos;
		private int groupPosition;
		
		public MyClickableSpan(GeoPoint pos, int groupPosition) {
			this.pos = pos;
			this.groupPosition = groupPosition;
		}

		@Override
		public void onClick(View widget) {
			Intent data = new Intent();
			data.putExtra("resultid", groupPosition);
			data.putExtra("geopoint", new int[] {pos.getLatitudeE6(), pos.getLongitudeE6()});
			setResult(RESULT_OK, data);
			finish();
		}
	}

}
