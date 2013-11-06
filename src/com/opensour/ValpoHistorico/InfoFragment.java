package com.opensour.ValpoHistorico;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class InfoFragment extends Fragment implements OnDataReceivedListener {
	private TextView title;
	private TextView body;
	private ImageView img;
	private TableLayout infoTable;
	private LinearLayout extraInfo;
	private ArrayList<WikiObject> lista;
	private WikiObject entry;
	private String titleText;
	private String bodyText;
	private String imgAddress;
	private List<Pair<String, LatLng>> nextList;
	private ProgressDialog progressDialog;
	private OnLocationClickListener onLocationClickListener;
	public static final String ARG_SECTION_NUMBER = "section_number";
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(progressDialog!=null && progressDialog.isShowing())
				progressDialog.dismiss();
			showData();
		}
	};
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.info_layout, container, false);
		title = (TextView) rootView.findViewById(R.id.info_title);
		title.setText(this.titleText);
		
		body = (TextView) rootView.findViewById(R.id.info_body);
		body.setText(this.bodyText);
		
		img = (ImageView) rootView.findViewById(R.id.info_image);
		
		infoTable = (TableLayout) rootView.findViewById(R.id.info_next_table);
		extraInfo = (LinearLayout) rootView.findViewById(R.id.info_extra_data);
		
		return rootView;
	}
	
	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
		if(body!=null)
			body.setText(bodyText);
	}

	public String getImgAddress() {
		return imgAddress;
	}

	public void setImgAddress(String imgAddress) {
		this.imgAddress = imgAddress;
		if(img!=null){
			Bitmap b = BitmapFactory.decodeFile(imgAddress);
			img.setImageBitmap(b);
		}
	}

	public List<Pair<String, LatLng>> getNextList() {
		return nextList;
	}

	public void setNextList(List<Pair<String, LatLng>> nextList) {
		this.nextList = nextList;
	}

	public ArrayList<WikiObject> getLista() {
		return lista;
	}

	public void setLista(ArrayList<WikiObject> lista) {
		this.lista = lista;
	}
	
	public OnLocationClickListener getOnLocationClickListener() {
		return onLocationClickListener;
	}

	public void setOnLocationClickListener(
			OnLocationClickListener onLocationClickListener) {
		this.onLocationClickListener = onLocationClickListener;
	}
	
	public void publishInfo(){
		progressDialog = ProgressDialog.show(this.getActivity(), "", "Cargando ".concat(titleText),true);
		lista = ((ValpoApp) this.getActivity().getApplication()).getLista();
		entry = select();
		if(entry!=null)
			entry.retrieveData();
		else{
			entry = new WikiObject();
			entry.setNombre(titleText);
			entry.setOnDataReceivedListener(this);
			entry.retrieveData();
		}
	}
	
	public WikiObject select(){
		if(lista==null || lista.isEmpty())
			return null;
		Iterator<WikiObject> it = lista.iterator();
		while(it.hasNext()){
			WikiObject temp = it.next();
			if(temp.getNombre().equals(titleText)){
				temp.setOnDataReceivedListener(this);
				return temp;
			}
		}
		return null;
	}

	protected void showData() {
		String ciudades = entry.getAtributos().get("Cerca de");
		extraInfo.removeAllViews();
		if(ciudades!=null){
			String[] ciudadesCercanas = ciudades.split(",");
			for(int i=0; i<ciudadesCercanas.length; i++){
				Button btn = (Button) this.getLayoutInflater(null).inflate(R.layout.text_entry, null);
				btn.setText(ciudadesCercanas[i]);
				btn.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						if(onLocationClickListener != null){
							Bundle extras = new Bundle();
							extras.putString("name", ((Button)arg0).getText().toString());
							onLocationClickListener.onLocationClick(extras); 
						}
					}
				});
				extraInfo.addView(btn);
			}
		}
		this.body.setText(Html.fromHtml(entry.getTexto()));
		if(title!=null)
			title.setText(titleText);
	}
	
	@Override
	public void onReceive(Bundle data) {
		mHandler.sendEmptyMessage(0);
	}
}