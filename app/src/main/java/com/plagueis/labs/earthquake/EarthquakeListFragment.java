package com.plagueis.labs.earthquake;

import android.app.ListFragment;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class EarthquakeListFragment extends ListFragment {

    private static final String TAG = "EARTHQUAKE";
    private Handler handler = new Handler();

    ArrayAdapter<Quake> aa;
    ArrayList<Quake> earthquakes = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int layoutId = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<>(getActivity(), layoutId, earthquakes);
        setListAdapter(aa);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                refreshEarthquakes();
            }
        });

        t.start();
    }

    public void refreshEarthquakes(){
        // Get the XML
        URL url;
        try {
            String quakeFeed = getString(R.string.quake_feed);
            url = new URL(quakeFeed);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream in = httpConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                // Parse the earthquake feed.
                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                // Clear the old earthquakes
                earthquakes.clear();

                // Get a list of earth earthquake entry;
                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength() > 0){
                    for( int i = 0; i<nl.getLength(); i++){
                        Element entry = (Element)nl.item(i);
                        Element title = (Element)entry.getElementsByTagName("title").item(0);
                        Element g = (Element)entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element)entry.getElementsByTagName("updated").item(0);
                        Element link = (Element)entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String linkString =link.getAttribute("href");

                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", new Locale("ES"));
                        Date qdate = new GregorianCalendar(0,0,0).getTime();
                        try {
                            qdate = sdf.parse(dt);
                        } catch (ParseException e) {
                            Log.d(TAG,"Date parsing exception.",e);
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        int end = magnitudeString.length()-1;
                        double magnitude = Double.parseDouble(magnitudeString.substring(0,end));

                        details = details.split(",")[1].trim();

                        final Quake quake = new Quake(qdate, details, l, magnitude, linkString);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                addNewQuake(quake);
                            }
                        });

                    }
                }
            }

        } catch (MalformedURLException e){
            Log.d(TAG, "MalformedURLException");
        } catch (IOException e) {
            Log.d(TAG, "IOException");
        } catch (ParserConfigurationException e) {
            Log.d(TAG, "ParserConfigurationException");
        } catch (SAXException e) {
            Log.d(TAG, "SACException");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Quake quake = earthquakes.get(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(quake.getLink()));

        startActivity(intent);
    }

    private void addNewQuake(Quake _quake){
        EarthquakeActivity earthquakeActivity = (EarthquakeActivity)getActivity();
        if( _quake.getMagnitude() > earthquakeActivity.minimumMagnitude){
            // Add the new quake to our list of earthquakes
            earthquakes.add(_quake);
        }

        // Notify the array adapter of a change
        aa.notifyDataSetChanged();
    }
}
