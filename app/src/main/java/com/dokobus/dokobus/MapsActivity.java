package com.dokobus.dokobus;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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





public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

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
    protected void onCreate(Bundle savedInstanceState) {
        // httpリクエストを入れる変数
        Uri.Builder builder = new Uri.Builder();

        //apiデータ取得して、json形式からデータ整形する
        AsyncHttpRequest task = new AsyncHttpRequest(this);
        task.execute(builder);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public class AsyncHttpRequest extends AsyncTask<Uri.Builder, Void, String> {

        private Activity mainActivity;
        private String TAG = MapsActivity.class.getSimpleName();

        public AsyncHttpRequest(Activity activity) {

            // 呼び出し元のアクティビティ
            this.mainActivity = activity;
        }

        // このメソッドは必ずオーバーライドする必要があるよ
        // ここが非同期で処理される部分みたいたぶん。
        @Override
        protected String doInBackground(Uri.Builder... builder) {
            // httpリクエスト投げる処理を書く。
            // ちなみに私はHttpClientを使って書きましたー

            Integer id = 1;

            String urlStr = "http://tutujibus.com/busstopLookup.php?rosenid=" + id;  // （2）
            HttpURLConnection con = null;  // （3）
            InputStream is = null;  // （3）
            String result = "";  // （3）
            try {
                URL url = new URL(urlStr);  // （4）
                con = (HttpURLConnection) url.openConnection();  // （5）
                con.setRequestMethod("GET");  // （6）
                con.connect();  // （7）
                is = con.getInputStream();  // （8）





                result = set_bus_stop_data(is);  // （9）


            }
            catch(MalformedURLException ex) {
                System.out.println(ex.getMessage());
            }
            catch(IOException ex) {
                System.out.println(ex.getMessage());
            }
            catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
            finally {
                if(con != null) {
                    con.disconnect();  // （10）
                }
                if(is != null) {
                    try {
                        is.close();  // （11）
                    }
                    catch(IOException ex) {
                    }
                }
            }


            return result;  // （12）
        }

        // このメソッドは非同期処理の終わった後に呼び出されます
        @Override
        protected void onPostExecute(String result) {

        }

        /**
         * InputStreamオブジェクトを文字列に変換するメソッド。変換文字コードはUTF-8。
         *
         * @param is 変換対象のInputStreamオブジェクト。
         * @return 変換された文字列。
         * @throws IOException 変換に失敗した時に発生。
         */
        public String set_bus_stop_data(InputStream is) throws IOException {

            String data = "";

            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String str = reader.readLine();
                //()の中にデータがあるので()を取り除く
                str = str.replace("(", "");
                str = str.replace(")", "");

                // JSONObject に変換します
                JSONObject json = new JSONObject(str);

                //jsonArrayオブジェクト取得(busstopが配列の形式なので)
                JSONArray jsonArray = json.getJSONArray("busstop");

                //jsonArrayの個数分のjsonオブジェクトの配列を作成
                int count = jsonArray.length();

                JSONObject[] jsondata = new JSONObject[count];

                //jsonArrayから一つずつ取り出して、jsondataに格納していく
                for (int i=0; i<count; i++){
                    jsondata[i] = jsonArray.getJSONObject(i);
                }

                busstop_name = jsondata[0].getString("name");

                for (JSONObject jobj: jsondata){
                    latitudeList.add(jobj.getDouble("latitude"));
                    longitudeList.add(jobj.getDouble("longitude"));
                    busStopNameList.add(jobj.getString("name"));

                }


            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            catch (JSONException e) {
                System.out.println(e.getMessage());
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }

            finally {
            }

            return data;
        }



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // 所定位置にバス停画像データをマッピングする
        Thread thread = new Thread(new Runnable(){

            Bitmap bmp = null;
            Bitmap bmpzoom = null;

            @Override
            public void run() {


                URL url;
                try{
                    url = new URL("http://tutujibus.com/image/busstop32.png");

                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                    //画像のリサイズ　←　
                    bmpzoom = bmp.createScaledBitmap(bmp, 100, 100, false);


                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        try{

                            // latitudeListが空の場合は1秒間sleepさせて非同期処理の中でバス停座標データの取得をまつ
                            // 取得できていればループを抜ける
                            while(latitudeList.isEmpty())
                            {
                                Thread.sleep(1000);
                            }

                        }
                        catch(Exception e)
                        {
                            System.out.println(e.getMessage());
                        }

                        //バス停間にラインを引こうとしているがうまく弾けない
                        PolylineOptions line_options = new PolylineOptions();

                        //格納したバス停の位置の配列分ループしてマーカーを配置
                        for (int i = 0; i < latitudeList.size(); i++)
                        {

                            double latitude  = latitudeList.get(i);
                            double longitude = longitudeList.get(i);

                            LatLng latlng = new LatLng(latitude, longitude);

                            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bmpzoom);


                            MarkerOptions options = new MarkerOptions();


                            //マーカー画像変更
                            options.icon(descriptor);

                            options.position(new LatLng(latitudeList.get(i), longitudeList.get(i)));
                            options.title(busStopNameList.get(i));

                            mMap.addMarker(options);



//
//                            line_options.add(new LatLng(latitudeList.get(i), longitudeList.get(i)));
//                            line_options.geodesic(true);
//
//
//                            line_options.color(Color.BLUE);
//                            line_options.width(3);


                        }
//                        mMap.addPolyline(line_options);
                    }
                });

            }



        });
        thread.start();




        LatLng fukui = new LatLng(35.969696,136.178467);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(fukui));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(fukui).zoom(11).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


//        routeSearch();



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    private void routeSearch(){

//        int last = latitudeList.size()-1;
        int last = 8;

        DownloadTask downloadTask = new DownloadTask();

        String[] urls = new String[(latitudeList.size() / last) + 1];

        ArrayList<String> urlist = new ArrayList<>();

        for (int i = 0; i <= latitudeList.size() / last; i++)
        {

            int start = i*last;
            int end = start+8;

            if(i == latitudeList.size() / last)
            {
                end = latitudeList.size() - 1;
            }

            LatLng origin = new LatLng(latitudeList.get(start), longitudeList.get(start));
            LatLng dest = new LatLng(latitudeList.get(end), longitudeList.get(end));

            ArrayList<LatLng> waypoints = new ArrayList<>();



            String str_waypoints = "waypoints=";
//            str_waypoints += latitudeList.get(1)+","+longitudeList.get(1);


            for (int j = start+1; j < end; j++)
            {

                str_waypoints += latitudeList.get(i)+","+longitudeList.get(i);

                if(j != end-1)
                {
                    str_waypoints += "|";
                }
            }




            String str_origin = "origin="+origin.latitude+","+origin.longitude;


            String str_dest = "destination="+dest.latitude+","+dest.longitude;




            String sensor = "sensor=false";

            //パラメータ
//            String parameters = str_origin+"&"+str_dest+"&"+str_waypoints+"&"+sensor + "&language=ja" + "&mode=driving";
//        String parameters = str_origin+"&"+str_dest+"&"+sensor + "&language=ja" + "&mode=driving";

            String parameters = "";

            if((end - start) > 1)
            {
                parameters = str_origin+"&"+str_dest+"&"+str_waypoints+"&"+sensor + "&language=ja" + "&mode=driving";
            }
            else
            {
                parameters = str_origin+"&"+str_dest+"&"+sensor + "&language=ja" + "&mode=driving";
            }

            //JSON指定
            String output = "json";


            String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

            urls[i] = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

            urlist.add("https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters);


//            downloadTask.execute(url);



        }

//        int data[] = {85, 72, 89};
//
//        for (int seiseki: data){
//            System.out.println(seiseki);
//        }

//
//        for(String url: urls)
//        {
//            downloadTask.execute(url);
//        }

//        urlist.add("aa");

//        downloadTask.doInBackground(urlist);


//          downloadTask.execute("", "");

        // urllistの数に寄って、パラメータを追加した形で呼び出す(強引すぎる)
        switch(urlist.size())
        {
            case 1:
                downloadTask.execute(urlist.get(0));
                break;
            case 2:
                downloadTask.execute(urlist.get(0), urlist.get(1));
                break;
            case 3:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2));
                break;
            case 4:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2), urlist.get(3));
                break;
            case 5:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2), urlist.get(3), urlist.get(4));
                break;
            case 6:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2), urlist.get(3), urlist.get(4), urlist.get(5));
                break;
            case 7:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2), urlist.get(3), urlist.get(4), urlist.get(5), urlist.get(6));
                break;
            case 8:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2), urlist.get(3), urlist.get(4), urlist.get(5), urlist.get(6), urlist.get(7));
                break;
            case 9:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2), urlist.get(3), urlist.get(4), urlist.get(5), urlist.get(6), urlist.get(7), urlist.get(8));
                break;
            case 10:
                downloadTask.execute(urlist.get(0), urlist.get(1), urlist.get(2), urlist.get(3), urlist.get(4), urlist.get(5), urlist.get(6), urlist.get(7), urlist.get(8), urlist.get(9));
                break;
        }



//
//        LatLng origin = new LatLng(latitudeList.get(0), longitudeList.get(0));
//        LatLng dest = new LatLng(latitudeList.get(last), longitudeList.get(last));
//
//        ArrayList<LatLng> waypoints = new ArrayList<>();
//
//        String str_waypoints = "waypoints=";
//        str_waypoints += latitudeList.get(1)+","+longitudeList.get(1);
//
//        for (int i = 2; i < last; i++)
//        {
//            waypoints.add(new LatLng(latitudeList.get(i), longitudeList.get(i)));
//
//            str_waypoints += "|"+latitudeList.get(i)+","+longitudeList.get(i);
//        }
//
//
//
//
//        String str_origin = "origin="+origin.latitude+","+origin.longitude;
//
//
//        String str_dest = "destination="+dest.latitude+","+dest.longitude;
//
//
//
//
//        String sensor = "sensor=false";
//
//        //パラメータ
//        String parameters = str_origin+"&"+str_dest+"&"+str_waypoints+"&"+sensor + "&language=ja" + "&mode=driving";
////        String parameters = str_origin+"&"+str_dest+"&"+sensor + "&language=ja" + "&mode=driving";
//
//        //JSON指定
//        String output = "json";
//
//
//        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
//
////        Uri route = Uri.parse(url);
//
//
////        DownloadTask downloadTask = new DownloadTask();
//
//
//        downloadTask.execute(url);













//        DownloadTask downloadTask = new DownloadTask();
//
//
//        downloadTask.execute(url);

    }

    private class DownloadTask extends AsyncTask<String, Void, String[]>{
        //非同期で取得

        @Override
        protected String[] doInBackground(String... url) {


//            String data = "";
            String[] datas = new String[url.length];
//            ArrayList<String> datas = new ArrayList<>();

            try{
                // Fetching the data from web service
//                data = downloadUrl(url[0]);

//                for(String ur: url)
//                {
//                    datas.add(downloadUrl(ur));
//                }

                for(int i=0; i<url.length; i++)
                {
                    datas[i] = downloadUrl(url[i]);
                }

                System.out.println("a");

            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return datas;
        }

//        protected ArrayList<String> doInBackground(ArrayList<String> url) {
//
//
//            ArrayList<String> data = new ArrayList<>();
//
//            try{
//
////                data = downloadUrl(url[0]);
//
//                for(String ur : url)
//                {
//                    data.add(downloadUrl(ur));
//                }
//            }catch(Exception e){
//                Log.d("Background Task",e.toString());
//            }
//            return data;
//        }


        // doInBackground()
        @Override
        protected void onPostExecute(String[] result) {
//            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

//            parserTask.execute(result);



            try{

//                for (String url : result)
//                {
//                    parserTask.execute(url);
//                }

                parserTask.execute(result);

            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);


            urlConnection = (HttpURLConnection) url.openConnection();


            urlConnection.connect();


            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            System.out.println(e.getMessage());
//            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }




    /*parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<List<HashMap<String, String>>>> >{


        @Override
        protected List<List<List<HashMap<String, String>>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<List<HashMap<String, String>>>> routes = null;

            try{
                for(String json : jsonData)
                {
                    jObject = new JSONObject(json);
                    parseJsonpOfDirectionAPI parser = new parseJsonpOfDirectionAPI();

                    routes.add(parser.parse(jObject));
                }

//                jObject = new JSONObject(jsonData[0]);
//                parseJsonpOfDirectionAPI parser = new parseJsonpOfDirectionAPI();
//
//
//                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        //ルート検索で得た座標を使って経路表示
        @Override
        protected void onPostExecute(List<List<List<HashMap<String, String>>>> results) {



            for(List<List<HashMap<String, String>>> result : results)
            {


                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                if(result.size() != 0){

                    for(int i=0;i<result.size();i++){
                        points = new ArrayList<LatLng>();
                        lineOptions = new PolylineOptions();


                        List<HashMap<String, String>> path = result.get(i);


                        for(int j=0;j<path.size();j++){
                            HashMap<String,String> point = path.get(j);

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);

                            points.add(position);
                        }

                        //ポリライン
                        lineOptions.addAll(points);
                        lineOptions.width(10);
                        lineOptions.color(0x550000ff);

                    }

                    //描画
                    mMap.addPolyline(lineOptions);

                }else{
                    mMap.clear();
//                Toast.makeText(mapsa.this, "ルート情報を取得できませんでした", Toast.LENGTH_LONG).show();
                }

            }



//            progressDialog.hide();

        }
    }


}