package kuzki.net.exercisetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


public class RouteListActivity extends Activity {

    private static final String TAG = RouteListActivity.class.getCanonicalName();
    private ListView mRouteList;
    private RouteDatabaseHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        mDbHelper = new RouteDatabaseHelper(this);

        // List<Route> routes = mDbHelper.getRoutes();

//        mRouteList = (ListView) findViewById(R.id.route_list);
//        mRouteList.setAdapter(new RouteListAdapter(this));
//        mRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.d(TAG, "onItemClicked: " + i);
//
//            }
//        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.route_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAddNewRouteClicked(View view) {
        Intent intent = MainActivity.createIntent(RouteListActivity.this);
        startActivity(intent);
    }
}
