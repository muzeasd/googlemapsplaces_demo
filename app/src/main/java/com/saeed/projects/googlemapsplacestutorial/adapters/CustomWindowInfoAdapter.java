package com.saeed.projects.googlemapsplacestutorial.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.saeed.projects.googlemapsplacestutorial.R;

public class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter
{
    private final View window;
    private Context context;
    public CustomWindowInfoAdapter(Context context)
    {
        this.context = context;
        window = LayoutInflater.from(context).inflate(R.layout.marker_windows, null);
    }

    private void renderWindowText(Marker marker, View view){
        String title = marker.getTitle();
        String snippet = marker.getSnippet();

        TextView markerTitle = (TextView) window.findViewById(R.id.txtTitle);
        TextView markerSnipet = (TextView) window.findViewById(R.id.txtSnippet);

        markerTitle.setText(title == null ? "" : title);
        markerSnipet.setText(snippet == null ? "" : snippet) ;
    }

    @Override
    public View getInfoWindow(Marker marker)
    {
        renderWindowText(marker, window);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker)
    {
        renderWindowText(marker, window);
        return window;
    }
}
