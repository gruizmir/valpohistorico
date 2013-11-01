package com.opensour.ValpoHistorico;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class InfoFragment extends Fragment {
	private TextView title;
	private TextView body;
	private ImageView img;
	private TableLayout infoTable;
	
	private String titleText;
	private String bodyText;
	private String imgAddress;
	private List<Pair<String, LatLng>> lista;
	
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	public InfoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.info_layout, container, false);
		title = (TextView) rootView.findViewById(R.id.info_title);
		title.setText(this.titleText);
		
		body = (TextView) rootView.findViewById(R.id.info_body);
		body.setText(this.bodyText);
		
		img = (ImageView) rootView.findViewById(R.id.info_image);
		
		infoTable = (TableLayout) rootView.findViewById(R.id.info_next_table);
		
		return rootView;
	}
	
	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
		if(title!=null)
			title.setText(titleText);
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

	public List<Pair<String, LatLng>> getLista() {
		return lista;
	}

	public void setLista(List<Pair<String, LatLng>> lista) {
		this.lista = lista;
	}
	
}