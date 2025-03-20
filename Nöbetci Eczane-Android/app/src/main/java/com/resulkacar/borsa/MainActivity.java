package com.resulkacar.borsa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.airbnb.lottie.LottieAnimationView;


public class MainActivity extends AppCompatActivity {

    String selectedCity ="";
    ListView listView;
    Button  onayla,eTelefon,eLoc;
    TextView eAdi,eAdres,eKonum;
   String[] items= {"Adana","Adıyaman","Afyon","Akşehir","Ankara"};

    AutoCompleteTextView sehir;
    AutoCompleteTextView ilce;
    String[] telno;
    String[] location;
    String selectedDistrict ;
    ArrayAdapter<String> arrayAdapter;
    MenuItem item;
    LinearLayout containerL;
    LinearLayout linearLayoutE;
    LinearLayout linearLayoutEC;
    TextView textViewA;
    TextView textViewb;
    TextView textViewc;
    TextView textViewd;
    LinearLayout line;


    CardView nobetcisonuc_cardview;
    DatabaseReference mReference;
    public HashMap<String, Objects> mData;

    LottieAnimationView beklemeanim;
    ProgressBar progressBar;


    private static String API_URL;
    private static String API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        onayla = findViewById(R.id.onayla);
        sehir = findViewById(R.id.sehir);
        ilce = findViewById(R.id.ilce);
        line = findViewById(R.id.lineEC);
        nobetcisonuc_cardview = findViewById(R.id.eczanesonuc_cardview);
        beklemeanim = findViewById(R.id.beklemeanim);
        progressBar = findViewById(R.id.progressBar);



        mReference = FirebaseDatabase.getInstance().getReference();
        mReference.child("1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String guncellenmisString = dataSnapshot.getValue(String.class);
                // Güncellenmiş veriyi kullanın, örneğin TextView'e gösterin
               API_KEY = guncellenmisString;
                System.out.println("KEY:" + guncellenmisString);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("KEY HATA: " + databaseError.getMessage());
            }
        });


        PopupMenu popupMenu = new PopupMenu(MainActivity.this, sehir);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.city_menu, popupMenu.getMenu());


        // PopupMenu'deki şehir verilerini al ve items dizisine ekle
        Menu menu = popupMenu.getMenu();
        List<String> cityList = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            cityList.add(item.getTitle().toString());
        }

        // Listeyi diziye çevir ve AutoCompleteTextView'a ata
        items = cityList.toArray(new String[0]);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_item, items);
        sehir.setAdapter(arrayAdapter);

        sehir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 selectedCity = parent.getItemAtPosition(position).toString();
                updateDistricts(selectedCity);


                Toast.makeText(getApplicationContext(), selectedCity, Toast.LENGTH_SHORT).show();

              //  Toast.makeText(getApplicationContext(), selectedDistrict, Toast.LENGTH_SHORT).show();

            }
        });

        ilce.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               selectedDistrict = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Seçilen İlçe: " + selectedDistrict, Toast.LENGTH_SHORT).show();
              //  onayla.setText(selectedDistrict);
                API_URL="https://nobetci-eczane-api-turkiye.p.rapidapi.com/pharmacies-on-duty?city=" +selectedCity   +"&district=" + selectedDistrict;
            }
        });



        onayla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Her tıklamada yeni bir linearLayoutE oluştur

              //  nobetcisonuc_cardview.setVisibility(View.VISIBLE);
                // linearLayoutE null değilse içeriğini sıfırla
                if (line!= null) {
                   line.removeAllViews();


                }

                APIgetir();
            }
        });




    }



    void APIgetir()
    {
        beklemeanim.setVisibility(View.GONE);
        beklemeanim.playAnimation();
        progressBar.setVisibility(View.VISIBLE);

        // Volley kuyruğunu oluştur
        RequestQueue queue = Volley.newRequestQueue(this);

        // JSON nesnesi talebi oluştur
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.has("data")) {
                                // beklemeanim.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                JSONArray pharmacies = response.getJSONArray("data");


                                // Eğer eczane dizisi boşsa kullanıcıya bilgi ver
                                if (pharmacies.length() == 0) {
                                    Toast.makeText(getApplicationContext(), "Eczane Bulunamadı", Toast.LENGTH_SHORT).show();
                                    beklemeanim.setVisibility(View.VISIBLE);
                                    beklemeanim.playAnimation();
                                    return; // Eğer dizide veri yoksa daha fazla işlem yapma
                                } else {


                                    List<String> pharmacyInfoList = new ArrayList<>();

                                    telno = new String[pharmacies.length()];
                                    location = new String[pharmacies.length()];
                                    // Her eczane için
                                    for (int i = 0; i < pharmacies.length(); i++) {
                                        JSONObject pharmacy = pharmacies.getJSONObject(i);

                                        String name = pharmacy.getString("pharmacyName");
                                        String dist = pharmacy.getString("district");
                                        String address = pharmacy.getString("address");
                                        String phone = pharmacy.getString("phone");
                                        String loc1 = pharmacy.getString("latitude");
                                        String loc2 = pharmacy.getString("longitude");

                                        String loc = loc1 + " " + loc2;

                                        String pharmacyInfo = "Eczane Adı:" + name + "\n" +
                                                "İlçe: " + dist + "\n" +
                                                "Adres: " + address + "\n" +
                                                "Telefon: " + phone + "\n" +
                                                "Loc: " + loc;

                                        System.out.println(pharmacyInfo);

                                        if (name.equals(""))
                                            Toast.makeText(MainActivity.this, "Eczane Bulunamadı", Toast.LENGTH_LONG).show();


                                        pharmacyInfoList.add(pharmacyInfo);
                                        LinearLayout linearLayoutE = new LinearLayout(MainActivity.this);
                                        LinearLayout linearLayoutEC = new LinearLayout(MainActivity.this);


                                        ScrollView scrollView = new ScrollView(MainActivity.this);

                                        textViewA = new TextView(MainActivity.this);
                                        textViewb = new TextView(MainActivity.this);
                                        textViewc = new TextView(MainActivity.this);
                                        LottieAnimationView animationView = new LottieAnimationView(MainActivity.this);
                                        animationView.setLayoutParams(new LinearLayout.LayoutParams(
                                                300, // Genişlik (px)
                                                450  // Yükseklik (px)
                                        ));

                                        Button telefon = new Button(MainActivity.this);
                                        Button harita = new Button(MainActivity.this);
                                        //     textViewd = new TextView(MainActivity.this);

                                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );

                                        int wrapContentInDp = 3; // 3 dp
                                        int wrapContentInPx = (int) TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP,
                                                wrapContentInDp,
                                                getResources().getDisplayMetrics()
                                        );

                                        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                wrapContentInPx

                                        );

                                        linearLayoutE.setOrientation(LinearLayout.VERTICAL);
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        int marginInDp = 3;
                                        layoutParams.setMargins(
                                                dpToPx(marginInDp),
                                                dpToPx(10),
                                                dpToPx(marginInDp),
                                                dpToPx(5)
                                        );

                                        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );

                                        int marginInDp5 = 9;
                                        layoutParams5.setMargins(
                                                dpToPx(marginInDp),
                                                dpToPx(marginInDp5),
                                                dpToPx(marginInDp),
                                                dpToPx(marginInDp5)
                                        );


                                        int marginInDp1 = 3;
                                        layoutParams1.setMargins(
                                                dpToPx(marginInDp),
                                                dpToPx(marginInDp),
                                                dpToPx(marginInDp),
                                                dpToPx(marginInDp)
                                        );


                                        try {
                                            telno[i] = phone;
                                            location[i] = loc;

                                            telefon.setText(String.valueOf(telno[i]));
                                            harita.setText(String.valueOf(location[i]));
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                            // Telefon numarasını veya konumu çevirirken hata oluştu
                                            // Hata durumunda alınacak aksiyonları burada belirleyebilirsiniz
                                            Toast.makeText(MainActivity.this, "Telefon numarasını veya konumu çevirirken hata oluştu", Toast.LENGTH_SHORT).show();
                                        }

                                        textViewA.setText("Eczane:" + name);
                                        textViewb.setText("İlçe:" + dist);
                                        textViewc.setText("Adres:" + address);
                                        telefon.setText("Ara");
                                        harita.setTypeface(null, Typeface.BOLD); // Metni kalın yapın
                                        telefon.setTypeface(null, Typeface.BOLD); // Metni kalın yapın

                                        harita.setText("Haritaya Git");

                                        int finalI = i;
                                        telefon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                callPhoneNumber(telno[finalI]);
                                            }
                                        });

                                        int finalI1 = i;
                                        harita.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showLocationOnMap(location[finalI1]);
                                            }
                                        });

                                        LinearLayout linearLayout1 = new LinearLayout(MainActivity.this);
                                        LinearLayout linearLayout2 = new LinearLayout(MainActivity.this);
                                        LinearLayout linearLayout3 = new LinearLayout(MainActivity.this);
                                        LinearLayout linearLayout4 = new LinearLayout(MainActivity.this);
                                        linearLayout1.setBackgroundColor(R.color.black);
                                        linearLayout2.setBackgroundColor(R.color.black);
                                        linearLayout3.setBackgroundColor(R.color.black);
                                        linearLayout4.setBackgroundColor(R.color.black);


                                        animationView.setAnimation(R.raw.animasyon); // JSON dosya adınızı belirtin


                                        Handler handler = new Handler();

                                        // İsteğe bağlı olarak animasyonu otomatik başlatmak ve döngü yapmak

                                        // Animasyonu başlatma ve sürekli döngü sağlama
                                        animationView.addAnimatorListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {
                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                // Animasyon sona erdiğinde yeniden başlat
                                                handler.postDelayed(() -> animationView.playAnimation(), 0);
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animation) {
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animation) {
                                            }
                                        });
                                        LinearLayout linearLayoutI;
                                        LinearLayout linearLayouti;

                                        // Ana LinearLayout'u oluşturma
                                        linearLayoutI = new LinearLayout(MainActivity.this);
                                        linearLayoutI.setOrientation(LinearLayout.HORIZONTAL); // Yatay düzen
                                        linearLayoutI.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.MATCH_PARENT
                                        ));

                                        // Ana LinearLayout'u oluşturma
                                        linearLayouti = new LinearLayout(MainActivity.this);
                                        linearLayouti.setOrientation(LinearLayout.VERTICAL); // Yatay düzen
                                        linearLayouti.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.MATCH_PARENT
                                        ));


                                        // İlk başlatma
                                        animationView.playAnimation();
                                        linearLayoutE.addView(linearLayoutI);
                                        textViewA.setTextSize(20);
                                        textViewb.setTextSize(16);
                                        textViewc.setTextSize(16);

                                        textViewA.setTypeface(null, Typeface.BOLD); // Metni kalın yapın
                                        textViewb.setTypeface(null, Typeface.BOLD); // Metni kalın yapın
                                        textViewc.setTypeface(null, Typeface.BOLD); // Metni kalın yapın

                                        textViewc.setPadding(0, 0, 0, 0);

                                        linearLayoutI.addView(animationView);

                                        linearLayoutI.addView(linearLayouti);
                                        linearLayouti.addView(textViewA);
                                        linearLayouti.addView(linearLayout1);
                                        linearLayouti.addView(textViewb);
                                        linearLayouti.addView(linearLayout2);

                                        linearLayouti.addView(textViewc);
                                        linearLayouti.addView(linearLayout3);

                                        linearLayoutE.addView(telefon);
                                        linearLayoutE.addView(harita);
                                        telefon.setBackground(getDrawable(R.drawable.opak));
                                        harita.setBackground(getDrawable(R.drawable.opak));
                                        harita.setLayoutParams(layoutParams5);
                                        telefon.setLayoutParams(layoutParams5);
                                        linearLayoutE.addView(linearLayout4);


                                        textViewA.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));
                                        textViewb.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));
                                        textViewc.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));


                                        textViewA.setLayoutParams(layoutParams5);
                                        textViewb.setLayoutParams(layoutParams5);
                                        textViewc.setLayoutParams(layoutParams5);


                                        linearLayoutE.setBackground(getDrawable(R.drawable.opak1));
                                        //  linearLayoutEC.setBackground(getDrawable(R.drawable.opak));
                                        linearLayoutE.setLayoutParams(layoutParams);
                                        //  linearLayoutEC.setLayoutParams(layoutParams);
                                        linearLayout1.setLayoutParams(layoutParams3);
                                        linearLayout2.setLayoutParams(layoutParams3);
                                        linearLayout3.setLayoutParams(layoutParams3);
                                        linearLayout4.setLayoutParams(layoutParams3);


                                        linearLayoutEC.addView(linearLayoutE);
                                        line.addView(linearLayoutEC);

                                        //linearLayoutEC.removeAllViews();


                                        // İstediğiniz işlemleri burada gerçekleştirin
                                        Log.d("Pharmacy", "Name: " + name + ", Dist: " + dist + ", Address: " + address + phone + "location" + loc);
                                    }

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                                            android.R.layout.simple_list_item_1, pharmacyInfoList);

                                    //    listView.setAdapter(adapter);


                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Log.e("Volley Hatası", "HTTP Hata Kodu: " + error.networkResponse.statusCode);
                    String errorMessage = new String(error.networkResponse.data);
                    Log.e("Volley Hatası", "Hata Mesajı: " + errorMessage);
                } else {
                    Log.e("Volley Hatası", "Hata: " + error.getMessage());
                }
                Toast.makeText(MainActivity.this, "Hata: " + error.toString(), Toast.LENGTH_SHORT).show();
            }

        }) {
            // API anahtarını header'a eklemek için özel bir sınıf
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("x-rapidapi-key" ,API_KEY);
                headers.put("x-rapidapi-host", "nobetci-eczane-api-turkiye.p.rapidapi.com");

                return headers;
            }
        };

        // JSON nesnesi talebini kuyruğa ekle
        queue.add(jsonObjectRequest);
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    public void callPhoneNumber(String tel) {
        // Burada arama işlemlerini gerçekleştirin
        String phoneNumber = "123456789"; // Buraya aranacak fontello numarasını ekleyin

        // Ara işlemini başlatmak için Intent kullanın
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + tel));

        // Ara işlemini başlatın
        startActivity(dialIntent);
    }
    // Haritada konumu gösterme fonksiyonu
    public void showLocationOnMap(String coordinates) {
        // Haritada konumu gösterme işlemlerini gerçekleştirin

        // Harita uygulamasını başlatmak için bir Intent oluşturun
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + coordinates);

        // Harita uygulamasını başlatın
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Google Haritalar uygulamasını zorunlu kılın

        // Harita uygulamasını başlatın
        startActivity(mapIntent);
    }


    void updateDistricts(String selectedCity) {
        String[] selectedCityDistricts;

        switch (selectedCity) {
            case "Adana":
                selectedCityDistricts = new String[]{
                        "Aladag", "Ceyhan", "Cukurova", "Feke", "imamoglu", "Karaisali", "Karatas", "Kozan", "Pozanti", "Saimbeyli", "Saricam", "Seyhan", "Tufanbeyli", "Yumurtalik", "Yuregir"};
                break;
            case "Adiyaman":
                selectedCityDistricts = new String[]{"Merkez", "Besni", "Celikhan", "Gerger", "Golbasi", "Kahta", "Samsat", "sincik", "Tut"};
                break;
            case "Afyonkarahisar":
                selectedCityDistricts = new String[]{"Merkez", "Basmakci", "Bayat", "Bolvadin", "Cay", "Cobanlar", "Dazkiri", "Dinar", "Emirdag", "Evciler", "Hocalar", "ihsaniye", "iscehisar", "Kiziloren", "Sandikli", "Sinanpasa", "Sultandagi", "Suhut"};
                break;
            case "Agri":
                selectedCityDistricts = new String[]{"Merkez", "Diyadin", "Dogubayazit", "Eleskirt", "Hamur", "Patnos", "Taslicay", "Tutak"};
                break;
            case "Aksaray":
                selectedCityDistricts = new String[]{"Merkez", "Agacoren", "Eskil", "Gulagac", "Guzelyurt", "Ortakoy", "Sariyahsi", "Sultanhani"};
                break;
            case "Amasya":
                selectedCityDistricts = new String[]{"Merkez", "Goynucek", "Gumushacikoy", "Hamamozu", "Merzifon", "Suluova", "Tasova"};
                break;
            case "Ankara":
                selectedCityDistricts = new String[]{"Akyurt", "Altindag", "Ayas", "Bala", "Beypazari", "Camlidere", "Cankaya", "Cubuk", "Elmadag", "Etimesgut", "Evren", "Golbasi", "Gudul", "Haymana", "Kalecik", "Kahramankazan", "Kecioren", "Kizilcahamam", "Mamak", "Nallihan", "Polatli", "Pursaklar", "Sincan", "Sereflikochisar", "Yenimahalle"};
                break;
            case "Antalya":
                selectedCityDistricts = new String[]{"Akseki", "Aksu", "Alanya", "Demre", "Dosemealti", "Elmali", "Finike", "Gazipasa", "Gundogmus", "ibradi", "Kas", "Kemer", "Kepez", "Konyaalti", "Korkuteli", "Kumluca", "Manavgat", "Muratpasa", "Serik"};
                break;
            case "Ardahan":
                selectedCityDistricts = new String[]{"Merkez", "Cildir", "Damal", "Gole", "Hanak", "Posof"};
                break;
            case "Artvin":
                selectedCityDistricts = new String[]{"Merkez","Ardanuc", "Arhavi", "Borcka", "Hopa", "Kemalpasa", "Murgul", "Savsat", "Yusufeli"};
                break;
            case "Aydin":
                selectedCityDistricts = new String[]{"Bozdogan", "Buharkent", "Cine", "Didim", "Efeler", "Germencik", "incirliova", "Karacasu", "Karpuzlu", "Kocarli", "Kosk", "Kusadasi", "Kuyucak", "Nazilli", "Soke", "Sultanhisar", "Yenipazar"};
                break;
            case "Balikesir":
                selectedCityDistricts = new String[]{"Altieylul", "Ayvalik", "Balya", "Bandirma", "Bigadic", "Burhaniye", "Dursunbey", "Edremit", "Erdek", "Gomec", "Gonen", "Havran", "ivrindi", "Karesi", "Kepsut", "Manyas", "Marmara", "Savastepe", "Sindirgi", "Susurluk"};
                break;
            case "Bartin":
                selectedCityDistricts = new String[]{"Merkez", "Amasra", "Kurucasile", "Ulus"};
                break;
            case "Batman":
                selectedCityDistricts = new String[]{"Merkez", "Besiri", "Gercus", "Hasankeyf", "Kozluk", "Sason"};
                break;
            case "Bayburt":
                selectedCityDistricts = new String[]{"Merkez", "Aydintepe", "Demirozu"};
                break;
            case "Bilecik":
                selectedCityDistricts = new String[]{"Merkez", "Bozuyuk", "Golpazari", "inhisar", "Osmaneli", "Pazaryeri", "Sogut", "Yenipazar"};
                break;
            case "Bingol":
                selectedCityDistricts = new String[]{"Merkez", "Adakli", "Genc", "Karliova", "Kigi", "Solhan", "Yayladere", "Yedisu"};
                break;
            case "Bitlis":
                selectedCityDistricts = new String[]{"Merkez","Adilcevaz", "Ahlat", "Guroymak", "Hizan", "Mutki", "Tatvan"};
                break;
            case "Bolu":
                selectedCityDistricts = new String[]{"Merkez", "Dortdivan", "Gerede", "Goynuk", "Kibriscik", "Mengen", "Mudurnu", "Seben", "Yenicaga"};
                break;
            case "Burdur":
                selectedCityDistricts = new String[]{"Merkez", "Aglasun", "Altinyayla", "Bucak", "Cavdir", "Celtikci", "Golhisar", "Karamanli", "Kemer", "Tefenni", "Yesilova"};
                break;
            case "Bursa":
                selectedCityDistricts = new String[]{"Buyukorhan", "Gemlik", "Gursu", "Harmancik", "inegol", "iznik", "Karacabey", "Keles", "Kestel", "Mudanya", "Mustafakemalpasa", "Nilufer", "Orhaneli", "Orhangazi", "Osmangazi", "Yenisehir", "Yildirim"};
                break;
            case "Canakkale":
                selectedCityDistricts = new String[]{"Merkez", "Ayvacik", "Bayramic", "Biga", "Bozcaada", "Can", "Eceabat", "Ezine", "Gelibolu", "Gokceada", "Lapseki", "Yenice"};
                break;
            case "Cankiri":
                selectedCityDistricts = new String[]{"Merkez", "Atkaracalar", "Bayramoren", "Cerkes", "Eldivan", "ilgaz", "Kizilirmak", "Korgun", "Kursunlu", "Orta", "Sabanozu", "Yaprakli"};
                break;
            case "Corum":
                selectedCityDistricts = new String[]{"Merkez", "Alaca", "Bayat", "Bogazkale", "Dodurga", "iskilip", "Kargi", "Lacin", "Mecitozu", "Oguzlar", "Ortakoy", "Osmancik", "Sungurlu", "Ugurludag"};
                break;
            case "Denizli":
                selectedCityDistricts = new String[]{"Acipayam", "Babadag", "Baklan", "Bekilli", "Beyagac", "Bozkurt", "Buldan", "Cal", "Cameli", "Cardak", "Civril", "Guney", "Honaz", "Kale", "Merkezefendi", "Pamukkale", "Saraykoy", "Serinhisar", "Tavas"};
                break;
            case "Diyarbakir":
                selectedCityDistricts = new String[]{"Baglar", "Bismil", "Cermik", "Cinar", "Cungus", "Dicle", "Egil", "Ergani", "Hani", "Hazro", "Kayapinar", "Kocakoy", "Kulp", "Lice", "Silvan", "Sur", "Yenisehir"};
                break;
            case "Duzce":
                selectedCityDistricts = new String[]{"Merkez", "Akcakoca", "Cumayeri", "Cilimli", "Golyaka", "Gumusova", "Kaynasli", "Yigilca"};
                break;
            case "Edirne":
                selectedCityDistricts = new String[]{"Merkez", "Enez", "Havsa", "ipsala", "Kesan", "Lalapasa", "Meric", "Suloglu", "Uzunkopru"};
                break;
            case "Elazig":
                selectedCityDistricts = new String[]{"Merkez", "Agin", "Alacakaya", "Aricak", "Baskil", "Karakocan", "Keban", "Kovancilar", "Maden", "Palu", "Sivrice"};
                break;
            case "Erzincan":
                selectedCityDistricts = new String[]{"Merkez", "Cayirli", "ilic", "Kemah", "Kemaliye", "Otlukbeli", "Refahiye", "Tercan", "Uzumlu"};
                break;
            case "Erzurum":
                selectedCityDistricts = new String[]{"Askale", "Aziziye", "Cat", "Hinis", "Horasan", "ispir", "Karacoban", "Karayazi", "Koprukoy", "Narman", "Oltu", "Olur", "Palandoken", "Pasinler", "Pazaryolu", "Senkaya", "Tekman", "Tortum", "Uzundere", "Yakutiye"};
                break;
            case "Eskisehir":
                selectedCityDistricts = new String[]{"Alpu", "Beylikova", "Cifteler", "Gunyuzu", "Han", "inonu", "Mahmudiye", "Mihalgazi", "Mihalıccik", "Odunpazari", "Saricakaya", "Seyitgazi", "Sivrihisar", "Tepebasi"};
                break;
            case "Gaziantep":
                selectedCityDistricts = new String[]{"Araban", "islahiye", "Karkamis", "Nizip", "Nurdagi", "Oguzeli", "Sahinbey", "Sehitkamil", "Yavuzeli"};
                break;
            case "Giresun":
                selectedCityDistricts = new String[]{"Merkez", "Alucra", "Bulancak", "Camoluk", "Canakcı", "Dereli", "Dogankent", "Espiye", "Eynesil", "Gorele", "Guce", "Kesap", "Piraziz", "Sebinkarahisar", "Tirebolu", "Yaglidere"};
                break;
            case "Gumushane":
                selectedCityDistricts = new String[]{"Merkez", "Kelkit", "Kose", "Kurtun", "Siran", "Torul"};
                break;
            case "Hakkari":
                selectedCityDistricts = new String[]{"Merkez", "Cukurca", "Derecik", "Semdinli", "Yuksekova"};
                break;
            case "Hatay":
                selectedCityDistricts = new String[]{"Altinozu", "Antakya-Defne", "Arsuz", "Belen", "Dortyol", "Erzin", "Hassa", "iskenderun", "Kirikhan", "Kumlu", "Payas", "Reyhanli", "SamandaG", "Yayladagi"};
                break;
            case "igdir":
                selectedCityDistricts = new String[]{"Merkez", "Aralık", "Karakoyunlu", "Tuzluca"};
                break;
            case "isparta":
                selectedCityDistricts = new String[]{"Merkez", "Aksu", "Atabey", "Egirdir", "Gelendost", "Gonen", "Keciborlu", "Senirkent", "Sarkikaraagac", "Uluborlu", "Yalvac", "Yenisarbademli"};
                break;
            case "istanbul":
                selectedCityDistricts = new String[]{"Adalar", "Arnavutkoy", "Atasehir", "Avcilar", "Bagcilar", "Bahcelievler", "Bakirkoy", "Basaksehir", "Bayrampasa", "Besiktas", "Beykoz", "Beylikduzu", "Beyoglu", "Buyukcekmece", "Catalca", "Cekmekoy", "Esenler", "Esenyurt", "Eyupsultan", "Fatih", "Gaziosmanpasa", "Gungoren", "Kadikoy", "Kagithane", "Kartal", "Kucukcekmece", "Maltepe", "Pendik", "Sancaktepe", "Sariyer", "Silivri", "Sultanbeyli", "Sultangazi", "Sile", "Sisli", "Tuzla", "Umraniye", "Uskudar", "Zeytinburnu"};
                break;
            case "izmir":
                selectedCityDistricts = new String[]{"Aliaga", "Balcova", "Bayindir", "Bayrakli", "Bergama", "Beydag", "Bornova", "Buca", "Cesme", "Cigli", "Dikili", "Foca", "Gaziemir", "Guzelbahce", "Karabaglar", "Karaburun", "Karsiyaka", "Kemalpasa", "Kinik", "Kiraz", "Konak", "Menderes", "Menemen", "Narlidere", "Odemis", "Seferihisar", "Selcuk", "Tire", "Torbali", "Urla"};
                break;
            case "Kahramanmaras":
                selectedCityDistricts = new String[]{"Afsin", "Andirin", "Caglayancerit", "Dulkadiroglu", "Ekinozu", "Elbistan", "Goksun", "Nurhak", "Onikisubat", "Pazarcik", "Turkoglu"};
                break;
            case "Karabuk":
                selectedCityDistricts = new String[]{"Merkez", "Eflani", "Eskipazar", "Ovacik", "Safranbolu", "Yenice"};
                break;
            case "Karaman":
                selectedCityDistricts = new String[]{"Merkez", "Ayranci", "Basyayla", "Ermenek", "Kazimkarabekir", "Sariveliler"};
                break;
            case "Kars":
                selectedCityDistricts = new String[]{"Merkez", "Akyaka", "Arpacay", "Digor", "Kagizman", "Sarikamis", "Selim", "Susuz"};
                break;
            case "Kastamonu":
                selectedCityDistricts = new String[]{"Merkez", "Abana", "Agli", "Arac", "Azdavay", "Bozkurt", "Cide", "Catalzeytin", "Daday", "Devrekani", "Doganyurt", "Hanonu", "ihsangazi", "inebolu", "Kure", "Pinarbasi", "Seydiler", "Senpazar", "Taskopru", "Tosya"};
                break;
            case "Kayseri":
                selectedCityDistricts = new String[]{"Akkisla", "Bunyan", "Develi", "Felahiye", "Hacilar", "incesu", "Kocasinan", "Melikgazi", "Ozvatan", "Pinarbasi", "Sarioglan", "Sariz", "Talas", "Tomarza", "Yahyali", "Yesilhisar"};
                break;
            case "Kirikkale":
                selectedCityDistricts = new String[]{"Merkez", "Bahsili", "Baliseyh", "Celebi", "Delice", "Karakecili", "Keskin", "Sulakyurt", "Yahsihan"};
                break;
            case "Kirklareli":
                selectedCityDistricts = new String[]{"Merkez", "Babaeski", "Demirkoy", "Kofcaz", "Luleburgaz", "Pehlivankoy", "Pinarhisar", "Vize"};
                break;
            case "Kirsehir":
                selectedCityDistricts = new String[]{"Merkez", "Akcakent", "Akpinar", "Boztepe", "Cicekdagi", "Kaman", "Mucur"};
                break;
            case "Kilis":
                selectedCityDistricts = new String[]{"Merkez","Elbeyli", "Musabeyli", "Polateli"};
                break;
            case "Kocaeli":
                selectedCityDistricts = new String[]{"Basiskele", "Cayirova", "Darica", "Derince", "Dilovası", "Gebze", "Golcuk", "izmit", "Kandira", "Karamursel", "Kartepe", "Korfez"};
                break;
            case "Konya":
                selectedCityDistricts = new String[]{"Merkez","Ahirli", "Akoren", "Aksehir", "Altinekin", "Beysehir", "Bozkir", "Cihanbeyli", "Celtik", "Cumra", "Derbent", "Derebucak", "Doganhisar", "Emirgazi", "Eregli", "Guneysinir", "Hadim", "Halkapinar", "Huyuk", "ilgin", "Kadinhani", "Karapinar", "Karatay", "Kulu", "Meram", "Sarayonu", "Selcuklu", "Seydisehir", "Taskent", "Tuzlukcu", "Yunak"};
                break;
            case "Kutahya":
                selectedCityDistricts = new String[]{"Merkez", "Altintas", "Aslanapa", "Cavdarhisar", "Domanic", "Emet", "Gediz", "Hisarcik",  "Saphane", "Simav", "Tavsanli"};
                break;
            case "Malatya":
                selectedCityDistricts = new String[]{"Akcadag", "Arapgir", "Arguvan", "Battalgazi", "Darende", "Dogansehir", "Doganyol", "Hekimhan", "Kale", "Kuluncak", "Puturge", "Yazihan", "Yesilyurt"};
                break;
            case "Manisa":
                selectedCityDistricts = new String[]{"Merkez","Ahmetli", "Akhisar", "Alasehir", "Demirci", "Golmarmara", "Gordes", "Kirkagac", "Koprubasi", "Kula", "Salihli", "Sarigol", "Saruhanli", "Selendi", "Soma", "Sehzadeler", "Turgutlu", "Yunusemre"};
                break;
            case "Mardin":
                selectedCityDistricts = new String[]{"Merkez","Artuklu", "Dargecit", "Derik", "Kiziltepe", "Mazidagi", "Midyat", "Nusaybin", "Omerli", "Savur", "Yesilli"};
                break;
            case "Mersin":
                selectedCityDistricts = new String[]{"Merkez","Akdeniz", "Anamur", "Aydincik", "Bozyazi",  "Erdemli", "Gulnar", "Mezitli", "Mut", "Silifke", "Tarsus", "Toroslar", "Yenisehir"};
                break;
            case "Mugla":
                selectedCityDistricts = new String[]{"Bodrum", "Dalaman", "Datca", "Fethiye", "Kavaklidere", "Koycegiz", "Marmaris", "Mentese", "Milas", "Ortaca", "Seydikemer", "Ula", "Yatagan"};
                break;
            case "Mus":
                selectedCityDistricts = new String[]{"Merkez", "Bulanik", "Haskoy",  "Malazgirt", "Varto"};
                break;
            case "Nevsehir":
                selectedCityDistricts = new String[]{"Merkez", "Acigol", "Avanos", "Derinkuyu", "Gulsehir", "Hacibektas", "Kozakli", "Urgup"};
                break;
            case "Nigde":
                selectedCityDistricts = new String[]{"Merkez", "Altunhisar", "Bor", "Camardi", "Ciftlik", "Ulukisla"};
                break;
            case "Ordu":
                selectedCityDistricts = new String[]{"Akkus", "Altinordu", "Aybasti", "Catalpinar",  "Fatsa", "Golkoy",  "Gurgentepe", "ikizce",  "Kabatas", "Korgan", "Kumru", "Persembe", "Ulubey", "Unye"};
                break;
            case "Osmaniye":
                selectedCityDistricts = new String[]{"Merkez", "Bahce", "Duzici", "Hasanbeyli", "Kadirli", "Sumbas", "Toprakkale"};
                break;
            case "Rize":
                selectedCityDistricts = new String[]{"Merkez", "Ardesen", "Camlihemsin", "Cayeli", "Derepazari", "Findikli", "Guneysu", "Hemsin", "ikizdere", "iyidere", "Kalkandere", "Pazar"};
                break;
            case "Sakarya":
                selectedCityDistricts = new String[]{"Adapazari", "Akyazi", "Arifiye", "Erenler", "Ferizli", "Geyve", "Hendek", "Karapurcek", "Karasu", "Kaynarca", "Kocaali", "Pamukova", "Sapanca", "Serdivan", "Sogutlu", "Tarakli"};
                break;
            case "Samsun":
                selectedCityDistricts = new String[]{"19-mayis", "Alacam", "Asarcik", "Atakum", "Ayvacik", "Bafra", "Canik", "Carsamba", "Havza", "ilkadim", "Kavak", "Ladik", "Salipazari", "Tekkekoy", "Terme", "Vezirkopru", "Yakakent"};
                break;
            case "Siirt":
                selectedCityDistricts = new String[]{"Merkez", "Baykan", "Eruh", "Kurtalan", "Pervari", "Sirvan", "Tillo"};
                break;
            case "Sinop":
                selectedCityDistricts = new String[]{"Merkez", "Ayancik", "Boyabat", "Dikmen", "Duragan", "Erfelek", "Gerze", "Sarayduzu", "Turkeli"};
                break;
            case "Sivas":
                selectedCityDistricts = new String[]{"Merkez", "Akincilar", "Altinyayla", "Divrigi", "Dogansar", "Gemerek", "Golova", "Gurun", "Hafik", "imranli", "Kangal", "Koyulhisar", "Susehri", "Sarkisla", "Ulas", "Yildizeli", "Zara"};
                break;
            case "Sanliurfa":
                selectedCityDistricts = new String[]{"Merkez","Akcakale", "Birecik", "Bozova", "Ceylanpinar", "Eyyubiye", "Halfeti", "Haliliye", "Harran", "Hilvan", "Karakopru", "Siverek", "Suruc", "Viransehir"};
                break;
            case "Sirnak":
                selectedCityDistricts = new String[]{"Merkez", "Beytussebap", "Cizre", "Guclukonak", "idil", "Silopi", "Uludere"};
                break;
            case "Tekirdag":
                selectedCityDistricts = new String[]{"Cerkezkoy", "Corlu", "Ergene", "Hayrabolu", "Kapakli", "Malkara", "Marmaraereglisi", "Muratli", "Saray", "Suleymanpasa", "Sarkoy"};
                break;
            case "Tokat":
                selectedCityDistricts = new String[]{"Merkez", "Almus", "Artova", "Basciftlik", "Erbaa", "Niksar", "Pazar", "Resadiye", "Sulusaray", "Turhal", "Yesilyurt", "Zile"};
                break;
            case "Trabzon":
                selectedCityDistricts = new String[]{"Akcaabat", "Arakli", "Arsin", "Besikduzu", "Carsibasi", "Caykara", "Dernekpazari", "Duzkoy", "Hayrat", "Koprubasi", "Macka", "Of", "Ortahisar", "Surmene", "Salipazari", "Tonya", "Vakfikebir", "Yomra"};
                break;
            case "Tunceli":
                selectedCityDistricts = new String[]{"Merkez", "Cemisgezek", "Hozat", "Mazgirt", "Nazimiye", "Ovacik", "Pertek", "Pulumur"};
                break;
            case "Usak":
                selectedCityDistricts = new String[]{"Merkez", "Banaz", "Esme", "Karahalli", "Sivasli", "Ulubey"};
                break;
            case "Van":
                selectedCityDistricts = new String[]{ "Bahcesaray", "Baskale", "Caldiran", "Catak", "Edremit", "Ercis", "Gevas", "Gurpinar", "ipekyolu", "Muradiye", "Ozalp", "Saray", "Tusba"};
                break;
            case "Yalova":
                selectedCityDistricts = new String[]{"Merkez", "Altinova", "Armutlu", "Cinarcik", "Ciftlikkoy", "Termal"};
                break;
            case "Yozgat":
                selectedCityDistricts = new String[]{"Merkez", "Akdagmadeni", "Aydincik", "Bogazliyan", "Candir", "Cayiralan", "Cekerek", "Kadisehri", "Saraykent", "Sarikaya", "Sorgun", "Sefaatli", "Yenifakili", "Yerkoy"};
                break;
            case "Zonguldak":
                selectedCityDistricts = new String[]{"Merkez", "Alapli", "Caycuma", "Devrek", "Gokcebey", "Eregli", "Kilimli", "Kozlu"};
                break;
            // Diğer şehirler için gerekli kontrolleri ekleyin
            default:
                selectedCityDistricts = new String[]{};
        }

        // Ilce AutoCompleteTextView'nin veri adaptörünü güncelle
        ArrayAdapter<String> ilceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, selectedCityDistricts);
        ilce.setAdapter(ilceAdapter);
    }



}
