package com.example.preeti.jsongetcustomadapater;
import android.content.Context;
import android.icu.text.LocaleDisplayNames;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {
    Button b;
    ListView lv;
    ArrayList<contacts> al;
    MyAdapter ma;
    MyTask m;


 // 9 : create two inner classes--------//1 for async task......

    public class MyTask extends AsyncTask<String,Void,String>{
        //declare all variables
        URL myurl;
        HttpURLConnection connection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result;

        //implement do in background ,connect to server ,get JSon nd return

        @Override
        protected String doInBackground(String... p1) {
            try {
                myurl = new URL(p1[0]);
                connection= (HttpURLConnection) myurl.openConnection();
                inputStream=connection.getInputStream();
                inputStreamReader=new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                result= new StringBuilder();
                line=bufferedReader.readLine();
                while(line!=null){
                    result.append(line);
                    line= bufferedReader.readLine();
                }
                return  result.toString();
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("b33","Message.."+e.getMessage());
                Log.d("b33","Cause.."+e.getCause());
                e.printStackTrace();//prints complete info about error
            }
              finally {
                //used for clean important resources..like clsing all network connections
                if(connection!=null){
                    connection.disconnect();
                    if(inputStream!=null){
                        try {
                            inputStream.close();
                            if(inputStreamReader!=null){
                                inputStreamReader.close();
                                if(bufferedReader!=null){
                                    bufferedReader.close();
                                }
                            }
                        }
                        catch (IOException e) {
                            Log.d("b33","Problem in closing connection....server issue or may be no internet");
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        //step 14 : ONPost execute for parsing JSon


        @Override
        protected void onPostExecute(String s) {
            if(s==null){
                Toast.makeText(getActivity(),"Network issue,fix",Toast.LENGTH_SHORT).show();
                return;

            }
            //start JSON Parsing
            try {
                JSONObject j = new JSONObject(s);
                JSONArray arr = j.getJSONArray("contacts");// contacts ....name given by server
                for(int i=0;i<arr.length();i++){
                    JSONObject temp = arr.getJSONObject(i);
                    String name = temp.getString("name");
                    String email = temp.getString("email");
                    JSONObject phone = temp.getJSONObject("phone");
                    String mobile = phone.getString("mobile");
                    //e=with this we got one object
                    contacts c = new contacts();
                    c.setCno(""+(i+1));
                    c.setCname(name);
                    c.setCemail(email);
                    c.setCmobile(mobile);
                    //push to array list
                    al.add(c);
                    //notify adapter
                    ma.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                Log.d("b33","JSON EXCEPTION.."+e.getMessage());
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }
    // 2.....for custom adapter
    public class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return al.size();
        }

        @Override
        public Object getItem(int i) {
            return al.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            //a.based on pos read contacts object from arraylist
            contacts c = al.get(i);
            //b. load row.xml  nd all other views
            View v= getActivity().getLayoutInflater().inflate(R.layout.row,null);
            TextView tv1 = (TextView)v.findViewById(R.id.textView1);
            TextView tv2 = (TextView)v.findViewById(R.id.textView2);
            TextView tv3 = (TextView)v.findViewById(R.id.textView3);
            TextView tv4 = (TextView)v.findViewById(R.id.textView4);
            //c. fill data onto text views-using Gettere
            tv1.setText(c.getCno());
            tv2.setText(c.getCname());
            tv3.setText(c.getCemail());
            tv4.setText(c.getCmobile());
            //d . return row.xml that is View v
            return v;
        }
    }

    public MyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_my, container, false);
        b=(Button)v.findViewById(R.id.button1);
        lv=(ListView)v.findViewById(R.id.listview1);
        al= new ArrayList<contacts>();
        ma=new MyAdapter();
        m=new MyTask();
        lv.setAdapter(ma);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInternet()==true){
                    m.execute("https://api.androidhive.info/contacts/");
                }else
                {
                  //display a dialog saying "no internet ,plz check"
                    Toast.makeText(getActivity(), "No Internet,plz Check", Toast.LENGTH_SHORT).show();
                }

            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    //check internet method
    public boolean checkInternet(){
        ConnectivityManager conn = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conn!=null){
            NetworkInfo info = conn.getActiveNetworkInfo();
            if(info!=null &&info.isConnected()){
                return true;
            }else
            {
                return false;
            }

        }else
        {
            return  false;
        }
    }

}
