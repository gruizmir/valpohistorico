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

import com.opensour.ValpoHistorico.connection.ServiceConnection;
import com.opensour.ValpoHistorico.connection.WikiConnection;
import com.opensour.ValpoHistorico.listeners.OnDataReceivedListener;
import com.opensour.ValpoHistorico.listeners.OnLocationClickListener;
import com.opensour.ValpoHistorico.parse.InfoParser;
import com.opensour.ValpoHistorico.parse.JSONParser;

public class RecommendFragment extends Fragment implements OnDataReceivedListener {
	public static final String ARG_SECTION_NUMBER = "section_number";
	private String urlBase = "http://tpsw.opensour.com/index.php/Especial:Ask&q=";
	private ProgressDialog progressDialog;
	private OnLocationClickListener onLocationClickListener;
	private ArrayList<WikiObject> lista;
	private InfoParser parser = new InfoParser();
	private LinearLayout recommendList;
	private TextView subtitle;
	private JSONParser jsonParser = new JSONParser();
	private String retrievedString=null;
	private int retrievedFlag;
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.getData().getString("flag", "").equals(WikiConnection.FLAG_BOTH)){
				if(!lista.isEmpty())
					lista = new ArrayList<WikiObject>();
				lista.addAll(parser.parseData(msg.getData().getString("api_response")));
				showData();
			}
			else if(msg.getData().getInt("flag", -1)==ServiceConnection.FLAG_BEST){
				retrievedFlag = msg.getData().getInt("flag", -1); 
				retrievedString = msg.getData().getString("api_response");
				showRecomendedList(true, jsonParser.parse(retrievedString));
			}
			else if(msg.getData().getInt("flag", -1)==ServiceConnection.FLAG_HIPSTER){
				retrievedFlag = msg.getData().getInt("flag", -1);
				retrievedString = msg.getData().getString("api_response");
				showRecomendedList(true, jsonParser.parse(retrievedString));
			}
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
		Button reloadButton = (Button) rootView.findViewById(R.id.recommend_reload);
		reloadButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				searchInitialRecommendation(true);
			}
		});
		
		Button hipsterButton = (Button) rootView.findViewById(R.id.recommend_hipster);
		hipsterButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				searchInitialRecommendation(false);
			}
		});
		
		if(savedInstanceState==null)
			searchInitialRecommendation(true);
		else{
			Message msg = new Message();
			msg.setData(savedInstanceState);
			mHandler.sendMessage(msg);
		}
		return rootView;
	}
	
	
	public void search(SearchObject obj){
		String texto = this.getString(R.string.recommend_subtitle);
		texto = texto.concat(" ")
				.concat(obj.getAttribute())
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
		if(data.getString("flag", "").equals(WikiConnection.FLAG_BOTH) ||
				data.getInt("flag", -1)==ServiceConnection.FLAG_BEST || 
				data.getInt("flag", -1)==ServiceConnection.FLAG_HIPSTER){
			Message msg = new Message();
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}
	
	
	protected void showData() {
		recommendList.removeAllViews();
	    recommendList.refreshDrawableState();
		Iterator<WikiObject> it = lista.iterator();
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
	
	
	private void searchInitialRecommendation(boolean isBest){
		String initialURL;
		int flag;
		progressDialog = ProgressDialog.show(this.getActivity(), "", "Cargando datos",true);
		if(isBest){
			initialURL = "http://ranking.opensour.com/best/";
			flag = ServiceConnection.FLAG_BEST;
		}
		else{
			initialURL = "http://ranking.opensour.com/hipster/";
			flag = ServiceConnection.FLAG_HIPSTER;
		}
		ServiceConnection sConnection = new ServiceConnection(initialURL);
		sConnection.setFlag(flag);
		sConnection.setOnDataReceivedListener(this);
		Bundle info = new Bundle();
        info.putString("cant", "6");
        sConnection.setInfo(info);
        sConnection.execute();
	}
	
	private void showRecomendedList(boolean isBest, ArrayList<WikiObject> lista){
		if(lista==null)
			return;
		if(isBest){
			subtitle.setText("Los mejores recomendados");
		}
		else{
			subtitle.setText("Los lugares menos visitados (Hipster Mode)");
		}
		recommendList.removeAllViews();
	    recommendList.refreshDrawableState();
		Iterator<WikiObject> it = lista.iterator();
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
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		//TODO agregar los datos a la instancia
		if(retrievedString!=null){
			savedInstanceState.putString("api_response", retrievedString);
			savedInstanceState.putInt("flag", retrievedFlag );
		}
	    super.onSaveInstanceState(savedInstanceState);
	}
}