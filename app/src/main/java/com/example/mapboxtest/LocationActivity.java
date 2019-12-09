package com.example.mapboxtest;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.widget.RadioGroup;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
//import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.ImageSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



/**
 * Use the LocationComponent to easily add a device location "puck" to a Mapbox map.
 */
public class LocationActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private RadioGroup radioGroup;

    private List<POI> poilevel099 = new ArrayList<POI>();
    private List<POI> poilevel100 = new ArrayList<POI>();
    private List<POI> poilevel103 = new ArrayList<POI>();
    private List<POI> poilevel104 = new ArrayList<POI>();
    //private static final String PROFILE_NAME = "PROFILE_NAME";

    LatLngQuad quad = new LatLngQuad(
            new LatLng(32.117371709800001,118.910583712999994 ),
            new LatLng(32.117371709800001, 118.911660181000002),
            new LatLng(32.116796381200002,118.911660181000002 ),
            new LatLng(32.116796381200002,118.910583712999994 )
    );
    private static final String ID_IMAGE_SOURCE = "animated_image_source";
    private static final String ID_IMAGE_LAYER = "animated_image_layer";
    private static final String ID_ICON_LAYER = "layer-id";
    private static final String ID_ICON_SOURCE = "source-id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalysisPoiJson();

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_location);
        radioGroup = (RadioGroup) findViewById(R.id.level_choose);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.FloorP:
                        mapboxMap.clear();
                        mapboxMap.getStyle().removeLayer(ID_IMAGE_LAYER);
                        mapboxMap.getStyle().removeSource(ID_IMAGE_SOURCE);
                        mapboxMap.getStyle().addSource(new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.level_099));
                        mapboxMap.getStyle().addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));
                        AddMarkWithpois(poilevel099);
                        break;
                    case R.id.Floor1:
                        mapboxMap.clear();
                        mapboxMap.getStyle().removeLayer(ID_IMAGE_LAYER);
                        mapboxMap.getStyle().removeSource(ID_IMAGE_SOURCE);
                        mapboxMap.getStyle().addSource(new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.level_100));
                        mapboxMap.getStyle().addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));
                        AddMarkWithpois(poilevel100);
                        break;
                    case R.id.Floor4:
                        mapboxMap.clear();
                        mapboxMap.getStyle().removeLayer(ID_IMAGE_LAYER);
                        mapboxMap.getStyle().removeSource(ID_IMAGE_SOURCE);
                        mapboxMap.getStyle().addSource(new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.level_103));
                        mapboxMap.getStyle().addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));
                        AddMarkWithpois(poilevel103);
                        break;
                    case R.id.Floor5:
                        mapboxMap.clear();
                        mapboxMap.getStyle().removeLayer(ID_IMAGE_LAYER);
                        mapboxMap.getStyle().removeSource(ID_IMAGE_SOURCE);
                        mapboxMap.getStyle().addSource(new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.level_104));
                        mapboxMap.getStyle().addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));
                        AddMarkWithpois(poilevel104);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        LocationActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });

    }
    /**
     * 根据各楼层的pois添加对应的marker
     *
     * @param pois
     */
    public void AddMarkWithpois(List<POI> pois) {
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, mapboxMap.getStyle());
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setIconIgnorePlacement(true);
        for (int i = 0; i < pois.toArray().length; i++) {
            POI poi = pois.get(i);
            LatLng latlng = new LatLng(poi.latitude, poi.longitude);
            // Create an Icon object for the marker to use
            IconFactory iconFactory = IconFactory.getInstance(LocationActivity.this);
            Icon icon = iconFactory.fromResource(GetPOIDrawableId(poi.poi_type));


           // Add the marker to the map
            mapboxMap.addMarker(new MarkerOptions().title(poi.building_name).snippet(poi.name)
                    .position(latlng)
                    .icon(icon));
            /*Marker marker = mapboxMap.addMarker(new MarkerOptions().title(poi.building_name).snippet(poi.name)
                    //.icon(BitmapDescriptorFactory.fromResource(GetPOIDrawableId(poi.poi_type)))
                    .icon(GetPOIDrawableId(poi.poi_type))
                    .position(latlng));*/

            // Add symbol at specified lat/lon
            /*Symbol symbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(latlng)
                    .withIconImage()
                    .withIconSize(2.0f));*/


        }

    }

    /**
     * 按楼层分离pois
     *
     * @param pois
     */
    public void SeparatePois(List<POI> pois) {
        for (int i = 0; i < pois.toArray().length; i++) {
            POI poi = pois.get(i);
            switch (poi.level_code) {
                case "099":
                    poilevel099.add(poi);
                    break;
                case "100":
                    poilevel100.add(poi);
                    break;
                case "103":
                    poilevel103.add(poi);
                    break;
                case "104":
                    poilevel104.add(poi);
                    break;
            }
        }
    }

    /**
     * 解析poi json文件
     */

    public void AnalysisPoiJson() {
        String json = getJson(this, "poi.json");
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray tiles = jsonObject.getJSONArray("tiles");
            for (int j = 0; j < tiles.length(); j++) {
                JSONObject tile = (JSONObject) tiles.get(j);
                List<POI> pois = new ArrayList<POI>();
                JSONArray poiArray = tile.getJSONArray("pois");
                for (int k = 0; k < poiArray.length(); k++) {
                    JSONObject poiObject = (JSONObject) poiArray.get(k);
                    POI poi = new POI();
                    poi.title = poiObject.getString("title");
                    //楼层
                    poi.level_code = poiObject.getString("level_code");
                    //房间名
                    poi.name = poiObject.getString("name");
                    poi.map_scale = poiObject.getInt("map_scale");
                    poi.staff = poiObject.getString("staff");
                    //poi.visitor = poiObject.getBoolean("visitor");
                    poi.tile_code = poiObject.getString("tile_code");
                    //poi.interest_type = poiObject.getInt("interest_type");
                    //poi.parking_field = poiObject.getBoolean("parking_field");
                    JSONObject introduction = poiObject.getJSONObject("introduction");
                    //地科院
                    poi.building_name = poiObject.getString("building_name");
                    poi.descriptions_title = introduction.getString("title");
                    poi.descriptions_text = introduction.getString("text");
                    poi.website = poiObject.getString("website");
                    poi.tel = poiObject.getString("tel");
                    poi.address = poiObject.getString("address");
                    //经纬度
                    poi.latitude = poiObject.getDouble("latitude");
                    poi.longitude = poiObject.getDouble("longitude");
                    //类型
                    poi.poi_type = poiObject.getString("poi_type");
                    JSONArray imageArray = poiObject.getJSONArray("images");
                    pois.add(poi);
                }
                SeparatePois(pois);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    /**
     * 获取poi的资源图片
     *
     * @param poi_type
     * @return
     */
   public int GetPOIDrawableId(String poi_type) {
        switch (poi_type) {
            case "entrance":
                return R.drawable.entrance;
            case "lift":
                return R.drawable.lift;
            case "room":
                return R.drawable.room;
            case "stairs":
                return R.drawable.stairs;
            case "washroom":
                return R.drawable.washroom;
            case "parking":
                return R.drawable.parking;
            default:
                break;
        }
       return 0;
   }

    public static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }


    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    /*private void addImage(String id, int drawableImage) {
        Style style = mapboxMap.getStyle();
        if (style != null) {
            style.addImageAsync(id, BitmapUtils.getBitmapFromDrawable(
                    getResources().getDrawable(drawableImage)));
        }
    }*/

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}