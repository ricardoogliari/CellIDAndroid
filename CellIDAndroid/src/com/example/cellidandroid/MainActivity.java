package com.example.cellidandroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

public class MainActivity extends Activity {

	TextView txtLatLong;
	Posicao posicao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView txt = (TextView) findViewById(R.id.txt);
		txtLatLong = (TextView) findViewById(R.id.txtLatLong);

		final TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
			final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
			if (location != null) {
				txt.setText("LAC: " + location.getLac() + " CID: "
						+ location.getCid() + " MCC: "
						+ telephony.getNetworkOperator().substring(0, 3)
						+ " MNC: "
						+ telephony.getNetworkOperator().substring(3));
			}

			AQuery aq = new AQuery(this);
			aq.ajax("http://www.opencellid.org/cell/get?key=82de36a107b90e0c6b943eba5cb1a8c5&" +
					"mnc="+telephony.getNetworkOperator().substring(3)+"&mcc="+
					telephony.getNetworkOperator().substring(0, 3)+"&lac="+location.getLac()
					+"&cellid="+location.getCid(),
					String.class, new AjaxCallback<String>() {

						@Override
						public void callback(String url, String object,
								AjaxStatus status) {
							Log.e("CELLID", object);
							XmlPullParser parser = Xml.newPullParser();
							try {
								parser.setFeature(
										XmlPullParser.FEATURE_PROCESS_NAMESPACES,
										false);
								BufferedReader br = new BufferedReader(
										new StringReader(object));
								parser.setInput(br);
								parser.nextTag();

								parser.require(XmlPullParser.START_TAG, null,
										"rsp");
								while (parser.next() != XmlPullParser.END_TAG) {
									if (parser.getEventType() != XmlPullParser.START_TAG) {
										continue;
									}
									String name = parser.getName();
									// Starts by looking for the entry tag
									if (name.equals("cell")) {
										posicao = new Posicao(
												parser.getAttributeValue(null,
														"lat"), parser
														.getAttributeValue(
																null, "lon"));
										txtLatLong.setText("Latitude: "
												+ posicao.lat + ". Longitude: "
												+ posicao.lon);
									} else {
										MainActivity.this.skip(parser);
									}
								}
							} catch (XmlPullParserException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

					});
		}
	}

	public void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	public static class Posicao {
		public final String lat;
		public final String lon;

		private Posicao(String lat, String lon) {
			this.lat = lat;
			this.lon = lon;
		}
	}

	public void noMapa(View v) {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?f=d&daddr=" + posicao.lat
						+ "," + posicao.lon));
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
