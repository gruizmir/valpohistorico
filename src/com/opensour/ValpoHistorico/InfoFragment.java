package com.opensour.ValpoHistorico;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.maps.model.LatLng;
import com.opensour.ValpoHistorico.connection.ServiceConnection;
import com.opensour.ValpoHistorico.listeners.OnDataReceivedListener;
import com.opensour.ValpoHistorico.listeners.OnLocationClickListener;
import com.opensour.ValpoHistorico.listeners.OnRelatedSearchListener;

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
	private OnRelatedSearchListener onRelatedSearchListener;
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final int TWITTER_ACTION = 10;
	private UiLifecycleHelper uiHelper;
	private String key;
	private String val;
	private ServiceConnection sConnection;
	
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
//		img = (ImageView) rootView.findViewById(R.id.info_image);
		infoTable = (TableLayout) rootView.findViewById(R.id.info_next_table);
		extraInfo = (LinearLayout) rootView.findViewById(R.id.info_extra_data);
		uiHelper = new UiLifecycleHelper(this.getActivity(), null);
	    uiHelper.onCreate(savedInstanceState);
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
	
	public OnRelatedSearchListener getOnRelatedSearchListener() {
		return onRelatedSearchListener;
	}

	public void setOnRelatedSearchListener(OnRelatedSearchListener onRelatedSearchListener) {
		this.onRelatedSearchListener = onRelatedSearchListener;
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
		//Agregando los elementos cercanos
		String elementosCercanos = entry.getAtributos().get("Cerca de");
		extraInfo.removeAllViews();
		if(elementosCercanos!=null){
			String[] elem = elementosCercanos.split(",");
			for(int i=0; i<elem.length; i++){
				Button btn = (Button) this.getLayoutInflater(null).inflate(R.layout.text_entry, null);
				btn.setText(elem[i]);
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
		
		//Agregando los atributos del objecto.
		this.infoTable.removeAllViews();
		Set<String> col = entry.getAtributos().keySet();
		Iterator<String> it = col.iterator();
		while(it.hasNext()){
			key = it.next();
			val = entry.getAtributos().get(key);
			if(key.equals("latitud") || key.equals("longitud"))
				continue;
			TableRow tr = (TableRow) this.getLayoutInflater(null).inflate(R.layout.table_row, null);
			TextView tv = (TextView) tr.getChildAt(0);
			Button tb = (Button) tr.getChildAt(1);
			tv.setText(key);
			tb.setText(val);
			tb.setContentDescription(key);
			tb.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {				
					sendObj(((Button)arg0).getContentDescription().toString(), ((Button)arg0).getText().toString());
				}
			});
			infoTable.addView(tr);
		}
		
		this.body.setText(Html.fromHtml(entry.getTexto()));
		if(title!=null)
			title.setText(titleText);
	}
	
	@Override
	public void onReceive(Bundle data) {
		mHandler.sendEmptyMessage(0);
	}
	
	public void shareOnFacebook(){
		try {
			String direc = "http://tpsw.opensour.com/index.php/" + URLEncoder.encode(this.titleText.replace(" ", "_"), "UTF-8");
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this.getActivity())
	        .setLink(direc)
	        .setDescription("Recorre Valparaíso con otra historia")
	        .setName("ValpoHistorico")
	        .setApplicationName("ValpoHistorico")
	        .setFragment(this)
	        .build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shareOnTwitter(){
		try {
			String text = "Estuve en " + this.titleText;
			String valpoUrl ="http://tpsw.opensour.com/index.php/" + URLEncoder.encode(this.titleText.replace(" ", "_"), "UTF-8");
			String tweetUrl = "https://twitter.com/intent/tweet?text=" + text + "&url=" + valpoUrl;
			Uri uri = Uri.parse(tweetUrl);
			startActivityForResult(new Intent(Intent.ACTION_VIEW, uri), TWITTER_ACTION);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e("share", "twitter", e);
			
		}
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode==TWITTER_ACTION){
    		sConnection = new ServiceConnection();
    		Bundle info = new Bundle();
    		info.putString("name", titleText);
            info.putString("twitter_share", "on");
            info.putString("twitter_rate", "1");
            info.putString("facebook_rate", "");
            info.putString("smw_id", "");
            sConnection.setInfo(info);
            sConnection.execute();
    		return;
    	}
	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	            
	            //TODO definir "evaluacion voluntaria". Usuario elige si quiere o no poner el rating. 
	            sConnection = new ServiceConnection();
	            Bundle info = new Bundle();
	            info.putString("name", titleText);
	            info.putString("facebook_share", "on");
	            info.putString("facebook_rate", "1");
	            info.putString("twitter_rate", "");
	            info.putString("smw_id", "");
	            sConnection.setData(info);
	            sConnection.execute();
	        }
	    });
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	public void sendObj(String attr, String value){
 		SearchObject obj = new SearchObject();
		obj.setAttribute(attr);
		obj.setValue(value);
		if(attr.equals("Tiene coordenadas"))
			obj.setPosition(true);
		onRelatedSearchListener.onRelatedSearch(obj);
	}
}