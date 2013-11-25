package com.opensour.ValpoHistorico;

import java.util.ArrayList;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecommendFragment extends Fragment implements OnDataReceivedListener {
	public static final String ARG_SECTION_NUMBER = "section_number";
	private String urlBase = "http://tpsw.opensour.com/index.php/Especial:Ask&q=";
	private ProgressDialog progressDialog;
	private OnLocationClickListener onLocationClickListener;
	private ArrayList<WikiObject> lista;
	private InfoParser parser = new InfoParser();
	private LinearLayout recommendList;
	private TextView subtitle;
			
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			lista.addAll(parser.parseData(msg.getData().getString("api_response")));
			showData();
			if(progressDialog!=null && progressDialog.isShowing())
				progressDialog.dismiss();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.recommend_layout, container, false);
		recommendList = (LinearLayout)rootView.findViewById(R.id.recommend_entry_list);
		lista = new ArrayList<WikiObject>();
		subtitle = (TextView) rootView.findViewById(R.id.recommend_label);
		return rootView;
	}
	
	
	public void search(SearchObject obj){
		String texto = this.getString(R.string.recommend_subtitle);
		texto = texto.concat(obj.getAttribute())
				.concat(" ")
				.concat(obj.getValue());
		subtitle.setText(texto);
		progressDialog = ProgressDialog.show(this.getActivity(), "", "Cargando datos",true);
		WikiConnection relatedConn = new WikiConnection();
		relatedConn.setOnDataReceivedListener(this);
		String arg = "";
		arg = arg.concat(obj.getAttribute())
				.concat("=")
				.concat(obj.getValue());
		if(obj.isPosition())
			arg = arg.concat(" (2000)");
		String[] relatedArgs = {arg};
		String[] relatedCategories = {};
		String[] relatedFields = {"Categoría", obj.getAttribute()};
		relatedConn.setInfo(relatedArgs, relatedCategories, relatedFields);
		relatedConn.setFlag(WikiConnection.FLAG_BOTH);
		relatedConn.execute(urlBase);
	}

	@Override
	public void onReceive(Bundle data) {
		if(data.getSerializable("flag").equals(WikiConnection.FLAG_BOTH)){
			Message msg = new Message();
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}
	
	
	protected void showData() {
		Iterator<WikiObject> it = lista.iterator();
		recommendList.removeAllViews();
		while(it.hasNext()){
			WikiObject temp = it.next();
			Button btn = (Button) this.getLayoutInflater(null).inflate(R.layout.recommend_entry, null);
			btn.setText(temp.getNombre());
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
			recommendList.addView(btn);
		}
	}
	
	
	public OnLocationClickListener getOnLocationClickListener() {
		return onLocationClickListener;
	}

	public void setOnLocationClickListener(
			OnLocationClickListener onLocationClickListener) {
		this.onLocationClickListener = onLocationClickListener;
	}
}