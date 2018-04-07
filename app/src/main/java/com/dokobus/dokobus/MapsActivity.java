package com.dokobus.dokobus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.Manifest;
//import android.app.Activity;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

//    private Map<String, String> position = new HashMap<>();

    private String busstop_name;

    private ArrayList<Double> latitudeList = new ArrayList<>();
    private ArrayList<Double> longitudeList = new ArrayList<>();
    private ArrayList<String> busStopNameList = new ArrayList<>();

    public static String posinfo = "";
    public static String info_A = "";
    public static String info_B = "";
    ArrayList<LatLng> markerPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        rosen_id = intent.getStringExtra("rosen_id");

        //    protected void onCreate(Bundle savedInstanceState) {
//        // httpリクエストを入れる変数
//        Uri.Builder builder = new Uri.Builder();
//
//        //apiデータ取得して、json形式からデータ整形する
//        AsyncHttpRequest task = new AsyncHttpRequest(this);
//        task.execute(builder);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

//    public class AsyncHttpRequest extends AsyncTask<Uri.Builder, Void, String> {
//
//        private Activity mainActivity;
//        private String TAG = MapsActivity.class.getSimpleName();
//
//        public AsyncHttpRequest(Activity activity) {
//
//            // 呼び出し元のアクティビティ
//            this.mainActivity = activity;
//        }
//
//        // このメソッドは必ずオーバーライドする必要があるよ
//        // ここが非同期で処理される部分みたいたぶん。
//        @Override
//        protected String doInBackground(Uri.Builder... builder) {
//            // httpリクエスト投げる処理を書く。
//            // ちなみに私はHttpClientを使って書きましたー
//
//            Integer id = 1;
//
//            String urlStr = "http://tutujibus.com/busstopLookup.php?rosenid=" + id;  // （2）
//            HttpURLConnection con = null;  // （3）
//            InputStream is = null;  // （3）
//            String result = "";  // （3）
//            try {
//                URL url = new URL(urlStr);  // （4）
//                con = (HttpURLConnection) url.openConnection();  // （5）
//                con.setRequestMethod("GET");  // （6）
//                con.connect();  // （7）
//                is = con.getInputStream();  // （8）
//
//
//
//
//
//                result = set_bus_stop_data(is);  // （9）
//
//
//            }
//            catch(MalformedURLException ex) {
//                System.out.println(ex.getMessage());
//            }
//            catch(IOException ex) {
//                System.out.println(ex.getMessage());
//            }
//            catch(Exception ex) {
//                System.out.println(ex.getMessage());
//            }
//            finally {
//                if(con != null) {
//                    con.disconnect();  // （10）
//                }
//                if(is != null) {
//                    try {
//                        is.close();  // （11）
//                    }
//                    catch(IOException ex) {
//                    }
//                }
//            }
//
//
//            return result;  // （12）
//        }
//
//        // このメソッドは非同期処理の終わった後に呼び出されます
//        @Override
//        protected void onPostExecute(String result) {
//
//        }
//
//        /**
//         * InputStreamオブジェクトを文字列に変換するメソッド。変換文字コードはUTF-8。
//         *
//         * @param is 変換対象のInputStreamオブジェクト。
//         * @return 変換された文字列。
//         * @throws IOException 変換に失敗した時に発生。
//         */
//        public String set_bus_stop_data(InputStream is) throws IOException {
//
//            String data = "";
//
//            try{
//                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//
//                String str = reader.readLine();
//                //()の中にデータがあるので()を取り除く
//                str = str.replace("(", "");
//                str = str.replace(")", "");
//
//                // JSONObject に変換します
//                JSONObject json = new JSONObject(str);
//
//                //jsonArrayオブジェクト取得(busstopが配列の形式なので)
//                JSONArray jsonArray = json.getJSONArray("busstop");
//
//                //jsonArrayの個数分のjsonオブジェクトの配列を作成
//                int count = jsonArray.length();
//
//                JSONObject[] jsondata = new JSONObject[count];
//
//                //jsonArrayから一つずつ取り出して、jsondataに格納していく
//                for (int i=0; i<count; i++){
//                    jsondata[i] = jsonArray.getJSONObject(i);
//                }
//
//                busstop_name = jsondata[0].getString("name");
//
//                for (JSONObject jobj: jsondata){
//                    latitudeList.add(jobj.getDouble("latitude"));
//                    longitudeList.add(jobj.getDouble("longitude"));
//                    busStopNameList.add(jobj.getString("name"));
//                }
//            }
//            catch(IOException e)
//            {
//                System.out.println(e.getMessage());
//            }
//            catch (JSONException e) {
//                System.out.println(e.getMessage());
//            }
//            catch(Exception e)
//            {
//                System.out.println(e.getMessage());
//            }
//            finally {
//            }
//
//            return data;
//        }
//    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // 鯖江駅
        LatLng latlng = new LatLng(35.9461, 136.1881);

        // 起動時のカメラ位置
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latlng).zoom(12).build();

        // 所定位置にバス停画像データをマッピングする
//        Thread thread = new Thread(new Runnable(){
//
//            Bitmap bmp = null;
//            Bitmap bmpzoom = null;
//
//            @Override
//            public void run() {
//                URL url;
//                try{
//                    url = new URL("http://tutujibus.com/image/busstop32.png");
//
//                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//
//                    //画像のリサイズ　←　
//                    bmpzoom = bmp.createScaledBitmap(bmp, 100, 100, false);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try{
//
//                            // latitudeListが空の場合は1秒間sleepさせて非同期処理の中でバス停座標データの取得をまつ
//                            // 取得できていればループを抜ける
//                            while(latitudeList.isEmpty())
//                            {
//                                Thread.sleep(1000);
//                            }
//
//                        }
//                        catch(Exception e)
//                        {
//                            System.out.println(e.getMessage());
//                        }
//
//                        //バス停間にラインを引こうとしているがうまく弾けない
//                        PolylineOptions line_options = new PolylineOptions();
//
//                        //格納したバス停の位置の配列分ループしてマーカーを配置
//                        for (int i = 0; i < latitudeList.size(); i++)
//                        {
//
//                            double latitude  = latitudeList.get(i);
//                            double longitude = longitudeList.get(i);
//
//                            LatLng latlng = new LatLng(latitude, longitude);
//
//                            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bmpzoom);
//
//
//                            MarkerOptions options = new MarkerOptions();
//
//
//                            //マーカー画像変更
//                            options.icon(descriptor);
//
//                            options.position(new LatLng(latitudeList.get(i), longitudeList.get(i)));
//                            options.title(busStopNameList.get(i));
//
//                            mMap.addMarker(options);
//                        }
//                    }
//                });
//            }
//        });
//        thread.start();
//
//        LatLng fukui = new LatLng(35.969696,136.178467);
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(fukui));
//
//        CameraPosition cameraPosition = new CameraPosition.Builder().target(fukui).zoom(11).build();

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
                                        String rosenId = rootJSON.getString("rosenid");

                                        // 全路線(0)あるいは同じ路線番号はマッピングする
                                        if (rosen_id.equals("0") || rosen_id.equals(rosenId))
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