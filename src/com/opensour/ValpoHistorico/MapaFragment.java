package com.opensour.ValpoHistorico;
import android.app.Dialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaFragment extends Fragment implements LocationListener, OnInfoWindowClickListener{
	public static final String ARG_SECTION_NUMBER = "section_number";
	private GoogleMap map;
	private OnLocationClickListener onLocationClickListener;
	
	public MapaFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.map_layout, container, false);
		
		SupportMapFragment fm = (SupportMapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		map = fm.getMap();
		map.setMyLocationEnabled(true);
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity().getBaseContext());
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this.getActivity(), requestCode);
            dialog.show();
        }else { // Google Play Services are available
			LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            if(location!=null){
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
            addInfo(location);
        }
		return rootView;
	}
	
	@Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        map.setOnInfoWindowClickListener(this);
    }
 
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
	
    
    public void addInfo(Location location){
    	map.addMarker(new MarkerOptions()
    			.position(new LatLng(-33.038805,-71.629406))
    			.title("Comandancia en Jefe de la Armada")
    			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

	public OnLocationClickListener getOnLocationClickListener() {
		return onLocationClickListener;
	}

	public void setOnLocationClickListener(OnLocationClickListener onLocationClickListener) {
		this.onLocationClickListener = onLocationClickListener;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		LatLng position = marker.getPosition();
		String title = marker.getTitle();
		if(onLocationClickListener != null){
			Bundle extras = new Bundle();
			extras.putString("name", title);
			extras.putDouble("latitude", position.latitude);
			extras.putDouble("longitude", position.longitude);
			onLocationClickListener.onLocationClick(extras); 
		}
	}
}