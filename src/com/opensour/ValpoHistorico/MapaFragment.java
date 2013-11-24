package com.opensour.ValpoHistorico;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaFragment extends Fragment implements LocationListener, OnInfoWindowClickListener, OnDataReceivedListener, OnMarkerDragListener {
	public static final String ARG_SECTION_NUMBER = "section_number";
	private GoogleMap map;
	private OnLocationClickListener onLocationClickListener;
	private ProgressDialog progressDialog;
	private String urlBase = "http://tpsw.opensour.com/index.php/Especial:Ask&q=";
	protected static JSONArray jsonArray;
	private ArrayList<WikiObject> lista;
	private LocationManager locationManager;
	private Location location;
	private boolean lugaresReady=false;
	private boolean hechosReady=false;
	public final String FLAG_LUGARES = "lugares";
	public final String FLAG_HECHOS = "hechos";
	public final String RADIO = "2000";
	private Marker myPosition;
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			lista.addAll(parseData(msg.getData().getString("api_response")));
			if(msg.getData().getString("flag", "default")==FLAG_LUGARES)
				lugaresReady=true;
			if(msg.getData().getString("flag", "default")==FLAG_HECHOS)
				hechosReady=true;
			if(lugaresReady && hechosReady){
				displayData();
				if(progressDialog!=null && progressDialog.isShowing())
					progressDialog.dismiss();
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.map_layout, container, false);
		//aqui debe insertar un dialog para la espera
		
		SupportMapFragment fm = (SupportMapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		map = fm.getMap();
		map.setMyLocationEnabled(true);
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity().getBaseContext());
		if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this.getActivity(), requestCode);
			dialog.show();
		}else { // Google Play Services are available
			locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, true);
			location = locationManager.getLastKnownLocation(provider);
			if(location!=null){
				onLocationChanged(location);
				
			}
			locationManager.requestLocationUpdates(provider, 20000, 0, this);
		}
		progressDialog = ProgressDialog.show(this.getActivity(), "", "Cargando datos",true);
		lista = new ArrayList<WikiObject>();
		WikiConnection conn = new WikiConnection();
		conn.setOnDataReceivedListener(this);
		String coordenadas;
		if(location!=null){
			coordenadas = Double.toString(location.getLatitude())
					.concat(",")
					.concat(Double.toString(location.getLongitude()))
					.concat(" (")
					.concat(RADIO)
					.concat(")");
			coordenadas = "Tiene coordenadas=".concat(coordenadas);
		}
		else{
			Log.e("location", "null");
			coordenadas= "ubicado en=Valparaíso";
		}
		Log.e("location_2", coordenadas);
		String[] args = {coordenadas};
		String[] categories = {"Categoría:Edificio", "Categoría:Monumento", "Categoría:Lugar", "Categoría:Museo"};
		String[] fields = {"Tiene coordenadas", "Categoría","Cerca de"};
		conn.setInfo(args, categories, fields);
		conn.setFlag(this.FLAG_LUGARES);
		conn.execute(urlBase);
		
		WikiConnection hechosConn = new WikiConnection();
		hechosConn.setOnDataReceivedListener(this);
		String hechosCoord;
		if(location!=null){
			hechosCoord = coordenadas;
		}
		else{
			hechosCoord= "ocurrido en=Valparaíso";
		}
		String[] hechosArgs = {hechosCoord};
		String[] hechosCategories = {"Categoría:Hecho"};
		String[] hechosFields = { "Categoría", "Tiene coordenadas"};
		hechosConn.setInfo(hechosArgs, hechosCategories, hechosFields);
		hechosConn.setFlag(this.FLAG_HECHOS);
		hechosConn.execute(urlBase);
		return rootView;
	}

	@Override
	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		map.animateCamera(CameraUpdateFactory.zoomTo(14));
		myPosition = map.addMarker(new MarkerOptions()
					.title("Desplázame!")
					.draggable(true)
					.position(latLng)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
				);
		map.setOnMarkerDragListener(this);
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

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
	
	private void displayData(){
		ValpoApp myApp = (ValpoApp)this.getActivity().getApplication();
		myApp.setLista(lista);
		Iterator<WikiObject> it = lista.iterator();
		LatLng latLng=null; 
		while(it.hasNext()){
			WikiObject temp = it.next();
			if(temp.getAtributos().containsKey("Tiene coordenadas")){
				latLng = new LatLng(
						Float.parseFloat(temp.searchAtributo("latitud")), 
						Float.parseFloat(temp.searchAtributo("longitud"))
						);
				if(temp.getCategoria()!=null && temp.getCategoria().equals("Hecho")){
					map.addMarker(new MarkerOptions()
								.title(temp.getNombre())
								.position(latLng)
								.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
					);
				}
				else{
					map.addMarker(new MarkerOptions()
									.title(temp.getNombre())
									.position(latLng)
									.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
					);
				}
			}
		}
		map.moveCamera(CameraUpdateFactory.newLatLng(myPosition.getPosition()));
		map.animateCamera(CameraUpdateFactory.zoomTo(14));
		map.setOnInfoWindowClickListener(this);
	}
	
	private ArrayList<WikiObject> parseData(String data){
		ArrayList<WikiObject> lista = new ArrayList<WikiObject>(); 
		String[] entradas = data.split("\"\"");
		String[] headers = entradas[0].replace("\"", "").split(";");
		
		for(int i=1; i<entradas.length; i++){
			String[] temp = entradas[i].replace("\"", "").split(";");
			WikiObject wikiTemp = new WikiObject();
			wikiTemp.setNombre(temp[0]);
			for(int j=1;j<headers.length; j++){
				try{
					if(headers[j].equals("Categoría"))
						wikiTemp.setCategoria(temp[j]);
					else
						if(!temp[j].equals(""))
							wikiTemp.addAtributo(headers[j], temp[j]);
				}catch(IndexOutOfBoundsException ioe){
					Log.e("OutOf", "Bounds", ioe);
					continue;
				}catch(NullPointerException npe){
					Log.e("NullPointer", "Except", npe);
					continue;
				}
			}
			lista.add(wikiTemp);
		}
		return lista;
	}
	
	@Override
	public void onReceive(Bundle data) {
		Message msg = new Message();
		msg.setData(data);
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		map.clear();
		myPosition = map.addMarker(new MarkerOptions()
			.title("Desplázame!")
			.draggable(true)
			.position(marker.getPosition())
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
	);
		LatLng position = marker.getPosition();
		progressDialog = ProgressDialog.show(this.getActivity(), "", "Cargando datos",true);
		lista = new ArrayList<WikiObject>();
		
		WikiConnection conn = new WikiConnection();
		conn.setOnDataReceivedListener(this);
		String coordenadas;
		if(position!=null){
			coordenadas = Double.toString(position.latitude)
					.concat(",")
					.concat(Double.toString(position.longitude))
					.concat(" (")
					.concat(RADIO)
					.concat(")");
			coordenadas = "Tiene coordenadas=".concat(coordenadas);
		}
		else{
			coordenadas= "ubicado en=Valparaíso";
		}
		String[] args = {coordenadas};
		String[] categories = {"Categoría:Edificio", "Categoría:Monumento", "Categoría:Lugar", "Categoría:Museo"};
		String[] fields = {"Tiene coordenadas", "Categoría","Cerca de"};
		conn.setInfo(args, categories, fields);
		conn.setFlag(this.FLAG_LUGARES);
		conn.execute(urlBase);
		
		WikiConnection hechosConn = new WikiConnection();
		hechosConn.setOnDataReceivedListener(this);
		String hechosCoord;
		if(location!=null){
			hechosCoord = coordenadas;
		}
		else{
			hechosCoord= "ocurrido en=Valparaíso";
		}
		String[] hechosArgs = {hechosCoord};
		String[] hechosCategories = {"Categoría:Hecho"};
		String[] hechosFields = { "Categoría", "Tiene coordenadas"};
		hechosConn.setInfo(hechosArgs, hechosCategories, hechosFields);
		hechosConn.setFlag(this.FLAG_HECHOS);
		hechosConn.execute(urlBase);
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		
	}
}