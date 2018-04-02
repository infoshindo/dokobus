package com.dokobus.dokobus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String bus = "";
    private Integer bus_id = 1;
    private String rosen_id = null;
    private Marker busPositionMarker = null;
    private Map<Integer, Marker> busPositionMarkers = new HashMap<>();
    private Map<String, String> result = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        rosen_id = intent.getStringExtra("rosen_id");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // 鯖江駅
        LatLng latlng = new LatLng(35.9461, 136.1881);

        // 起動時のカメラ位置
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latlng).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        Timer timer = new Timer(false);

        // 1秒おきに1台ずつバスデータを取得する（合計8台）
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                Observable
                    .create(new Observable.OnSubscribe<Map>()
                    {
                        @Override
                        public void call(Subscriber<? super Map> subscriber)
                        {
                            // 初期値に戻す
                            if (bus_id > 8) bus_id = 1;

                            // 既に配置されているマーカーがなければ、NULLになる
                            busPositionMarker = busPositionMarkers.get(bus_id);

                            HttpURLConnection con = null;
                            InputStream is = null;

                            // つつじバスロケーションWEB API
                            try {
                                String urlStr = "http://tutujibus.com/busLookup.php?busid=" + bus_id;

                                URL url = new URL(urlStr);
                                con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");
                                con.connect();
                                is = con.getInputStream();

                                bus = is2String(is);
                            } catch(MalformedURLException ex) {
                            } catch(IOException ex) {
                            } finally {
                                if (con != null) con.disconnect();

                                if (is != null)
                                {
                                    try {
                                        is.close();
                                    } catch(IOException ex) {
                                    }
                                }
                            }

                            bus = bus.replace("(", "");
                            bus = bus.replace(")", "");

                            if (bus == "{}")
                            {
                                // マーカーがあれば削除
                                if (busPositionMarker != null)
                                {
                                    result.put("isRunning", "false");
                                    subscriber.onNext(result);
                                }
                            }
                            else
                            {
                                // JSONデータ解析
                                try {
                                    JSONObject rootJSON = new JSONObject(bus);

                                    String isRunning = rootJSON.getString("isRunning");

                                    if (isRunning == "true")
                                    {
                                        String rosenId = rootJSON.getString("rosen_id");

                                        // 全路線(0)あるいは同じ路線番号はマッピングする
                                        if (rosen_id == "0" || rosen_id == rosenId)
                                        {
                                            result.put("isRunning", "true");
                                            result.put("latitude", rootJSON.getString("latitude"));
                                            result.put("longitude", rootJSON.getString("longitude"));
                                            result.put("direction", rootJSON.getString("direction"));
                                            subscriber.onNext(result);
                                        }
                                    }
                                    else
                                    {
                                        // マーカーがあれば削除
                                        if (busPositionMarker != null)
                                        {
                                            result.put("isRunning", "false");
                                            subscriber.onNext(result);
                                        }
                                    }
                                } catch(JSONException ex) {
                                    System.out.println(ex.getMessage());
                                }
                            }
                            subscriber.onCompleted();
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Map>()
                    {
                        @Override
                        public void onCompleted() {}

                        @Override
                        public void onError(Throwable e) {}

                        @Override
                        public void onNext(final Map result)
                        {
                            Thread thread = new Thread(new Runnable()
                            {
                                Bitmap bmp = null;

                                @Override
                                public void run()
                                {
                                    // マーカー画像を取得する
                                    if (result.get("isRunning") == "true")
                                    {
                                        // 全方位
                                        final int[] primeArray = {0, 45, 90, 135, 180, 225, 270, 315, 360};
                                        TreeSet<Integer> primes = new TreeSet<Integer>();
                                        for (int n : primeArray) primes.add(n);

                                        // 方位
                                        int direction = (int) Math.ceil(Double.parseDouble((String) result.get("direction")));

                                        Integer directionObject = direction;

                                        int floor = primes.floor(directionObject); // 25以下で、いちばん近い値。
                                        int ceiling = primes.ceiling(directionObject); // 25以上で、いちばん近い値。

                                        // 25からの距離がいちばん近い値（等距離なら小さい方優先）。
                                        int nearestDirection = Math.abs(floor - direction) <= Math.abs(ceiling - direction) ? floor : ceiling;

                                        String directionNum = "";

                                        switch (nearestDirection)
                                        {
                                            case 0:
                                            case 360: // 北
                                                directionNum = "1";
                                                break;
                                            case 45: // 北東
                                                directionNum = "2";
                                                break;
                                            case 90: // 東
                                                directionNum = "3";
                                                break;
                                            case 135: // 南東
                                                directionNum = "4";
                                                break;
                                            case 180: // 南
                                                directionNum = "5";
                                                break;
                                            case 225: // 南西
                                                directionNum = "6";
                                                break;
                                            case 270: // 西
                                                directionNum = "7";
                                                break;
                                            case 315: // 北西
                                                directionNum = "8";
                                                break;
                                        }

                                        // 方角を示すIDから画像を取得する
                                        try {
                                            URL url = new URL("http://tutujibus.com/image/bus/" + bus_id + "/" + bus_id + "_" + directionNum + ".png");
                                            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    // マッピングする
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            if (result.get("isRunning") == "true")
                                            {
                                                // マーカー位置
                                                double latitude  = Double.parseDouble((String) result.get("latitude"));
                                                double longitude = Double.parseDouble((String) result.get("longitude"));

                                                // マーカー画像
                                                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bmp.createScaledBitmap(bmp, 300, 300, false));

                                                // 既にマーカーがあれば更新、なければ追加する
                                                if (busPositionMarker == null)
                                                {
                                                    MarkerOptions options = new MarkerOptions();

                                                    options.icon(descriptor);
                                                    options.position(new LatLng(latitude, longitude));

                                                    busPositionMarkers.put(bus_id, mMap.addMarker(options));
                                                }
                                                else
                                                {
                                                    busPositionMarker.setIcon(descriptor);
                                                    busPositionMarker.setPosition(new LatLng(latitude, longitude));
                                                }
                                            }
                                            else
                                            {
                                                // マーカー削除
                                                busPositionMarkers.remove(bus_id);
                                                busPositionMarker.remove();
                                            }
                                        }
                                    });
                                }
                            });
                            thread.start();
                        }
                    });

                bus_id++;
            }
        };
        timer.schedule(task, 0, 1000);
    }

    /**
     * InputStreamオブジェクトを文字列に変換するメソッド。変換文字コードはUTF-8。
     *
     * @param is 変換対象のInputStreamオブジェクト。
     * @return 変換された文字列。
     * @throws IOException 変換に失敗した時に発生。
     */
    private String is2String(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while(0 <= (line = reader.read(b))) {
            sb.append(b, 0, line);
        }
        return sb.toString();
    }
}