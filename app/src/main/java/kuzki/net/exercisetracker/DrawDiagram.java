package kuzki.net.exercisetracker;

import android.app.Activity;


/*
This class is just a stub showing how to use the DrawUtil class.
 */
public class DrawDiagram extends Activity {

    /*
    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_diagram);

        System.out.println("Activity--->onCreate");

        // Getting reference to the button btn_chart
        Button btnChart = (Button) findViewById(R.id.btn_chart);

        // Defining click event listener for the button btn_chart
        OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {

                List<TimedDistance> tDistance = new ArrayList<TimedDistance>();
                tDistance.add(new TimedDistance(1L, 30.0));
                tDistance.add(new TimedDistance(2L, 31.0));
                tDistance.add(new TimedDistance(8L, 35.0));
                tDistance.add(new TimedDistance(14L, 36.0));
                tDistance.add(new TimedDistance(15L, 37.0));
                tDistance.add(new TimedDistance(26L, 37.5));
                tDistance.add(new TimedDistance(37L, 69.0));

                List<TimedDistance> base = new ArrayList<TimedDistance>();
                base.add(new TimedDistance(2L, 30.0));
                base.add(new TimedDistance(3L, 31.0));
                base.add(new TimedDistance(5L, 35.0));
                base.add(new TimedDistance(10L, 36.0));
                base.add(new TimedDistance(17L, 37.0));
                base.add(new TimedDistance(26L, 37.5));
                base.add(new TimedDistance(37L, 39.0));
                base.add(new TimedDistance(47L, 47.0));
                base.add(new TimedDistance(57L, 87.5));
                base.add(new TimedDistance(67L, 90.0));


                DrawUtil.drawChart(
                        tDistance,
                        base,
                        (LinearLayout) findViewById(R.id.right_layout),
                        DrawDiagram.this);
            }
        };

        // Setting event click listener for the button btn_chart of the MainActivity layout
        btnChart.setOnClickListener(clickListener);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        System.out.println("Activity--->onResume");
    }
    */
}


