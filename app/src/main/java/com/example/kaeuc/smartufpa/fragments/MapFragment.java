package com.example.kaeuc.smartufpa.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.kaeuc.smartufpa.BuildConfig;
import com.example.kaeuc.smartufpa.R;
import com.example.kaeuc.smartufpa.customviews.CustomMapView;
import com.example.kaeuc.smartufpa.models.Place;
import com.example.kaeuc.smartufpa.models.PlaceFactory;
import com.example.kaeuc.smartufpa.utils.Constants;
import com.example.kaeuc.smartufpa.utils.Constants.MarkerStatuses;
import com.example.kaeuc.smartufpa.utils.Constants.MarkerTypes;
import com.example.kaeuc.smartufpa.utils.Constants.OverlayTags;
import com.example.kaeuc.smartufpa.utils.MapUtils;
import com.example.kaeuc.smartufpa.utils.SystemServicesManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashMap;
import java.util.List;


public class MapFragment extends Fragment implements LocationListener{

    private static final String TAG = MapFragment.class.getSimpleName() ;
    public static String FRAGMENT_TAG = MapFragment.class.getName();

    // MAP
    private MyLocationNewOverlay myLocationOverlay;
    private IMapController mapCamera;
    private static final int DEFAULT_ZOOM = 16;
    private static final int MIN_ZOOM = 15;
    private static final int MAX_ZOOM = 18;
    private final XYTileSource MAPA_UFPA = new XYTileSource("ufpa_mapa", 15, 18, 256, ".png", new String[] {});

    private final XYTileSource MAPA_UFPA_TRANSPORTE = new XYTileSource("ufpa_transporte", 15, 18, 256, ".png", new String[] {});

    // VIEWS
    private CustomMapView mapView;
    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabBusLocation;
    private Button btnClearMap;


    private Context parentContext;

    // TODO: BUSCAR ESSES VALORES A PARTIR DA CONFIGURAÇÃO
    private final Place defaultLocation = new Place(-1.47485, -48.45651, "UFPA");
    // Restrição da região mostrada do mapa usando coordenadas
    private final BoundingBox mapBoundaries = new BoundingBox(-1.457886, -48.437957, -1.479967, -48.459779);


    private Place userLocation;


    private LocationManager locationManager;

      /* CLICK LISTENERS */

    private Button.OnClickListener myLocationListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            moveCameraToMyLocation();
        }
    };

    private Button.OnClickListener busLocationListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Toast.makeText(parentContext, "Adicionar lógica da função de localizar o circular", Toast.LENGTH_SHORT).show();
        }
    };

    private Button.OnClickListener clearMapListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            clearMap();
        }
    };



    // Required empty public constructor
    public MapFragment() {}

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        parentContext = getContext();

        /* Configura o caminho do armazenamento em cache do mapa, se o device não possui cartão SD,
         * ele deve ser configurado para o caminho de arquivos do device
         */
        OpenStreetMapTileProviderConstants.setCachePath(getActivity().getFilesDir().getAbsolutePath());

        /* Importante! Configure o user agent para previnir ser banido dos servidores do OSM
         * O user agent deve ser uma identificação única do seu aplicativo
         * Um exemplo mostra a utilização de "BuildConfig.APPLICATION_ID"
         */
        OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

    }

    private void initializeMap(){
        // Configuração da câmera do mapa
        GeoPoint startCameraPoint = new GeoPoint
                (defaultLocation.getLatitude(),defaultLocation.getLongitude());
        mapCamera = mapView.getController();
        mapCamera.setZoom(DEFAULT_ZOOM);
        mapCamera.animateTo(startCameraPoint);
        // Configuração do Mapa
        mapView.setTilesScaledToDpi(true);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        //  Atribui o mapa offline em mapView
//        if (checar configurações para saber se o download do mapa foi realizado){
//        mapView.setTileSource(MAPA_UFPA);
//        }

        /* Desabilita o uso da internet (opcional, mas uma boa forma de previnir que o mapa
         * seja carregado via rede e de testar se o zip está carregando
         */
//        mapView.setUseDataConnection(false);
        mapView.setBuiltInZoomControls(false);
        mapView.setMinZoomLevel(MIN_ZOOM);
        mapView.setMaxZoomLevel(MAX_ZOOM);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.setScrollableAreaLimitDouble(mapBoundaries); // Restringe a área do mapa à região escolhida
    }

    private void enableMyLocationOverlay(){
        // Camada de posição do usuário
        if(userLocation == null){
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(parentContext),mapView);
        }
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.setOptionsMenuEnabled(true);
        mapView.addTileOverlay(myLocationOverlay, OverlayTags.MY_LOCATION);
    }

    public boolean enableBusOverlay(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO : SALVAR ESSE OVERLAY OU REQUISIÇÃO PARA NÃO REFAZER A REQUISIÇÃO EVERY TIME
                // TODO: TRATAR RESPOSTA INVÁLIDA
                // TODO: https://github.com/MKergall/osmbonuspack/wiki/Tutorial_4
                OverpassAPIProvider overpassProvider = new OverpassAPIProvider();
                MapUtils mapUtils = new MapUtils(getContext());
                String url = mapUtils.getBusRouteURL();
                KmlDocument kmlDocument = new KmlDocument();
                boolean ok = overpassProvider.addInKmlFolder(kmlDocument.mKmlRoot, url);
                Style style = new Style(null, Color.argb(150,50,50,255),10,Color.argb(150,50,50,255));
                FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mapView,style, null, kmlDocument);
                mapView.addTileOverlay(kmlOverlay,OverlayTags.BUS_ROUTE);
            }
        }).start();
        mapView.postInvalidate();
        btnClearMap.setVisibility(View.VISIBLE);
        return true;
    }

    private boolean areMapsInStorage(){

        return false;
    }

    private void clearMap(){
        mapView.clearMap();
        if(btnClearMap.getVisibility() == View.VISIBLE) btnClearMap.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View view =  inflater.inflate(R.layout.fragment_map, container, false);

        // Adiciona a CustomMapView ao layout na posição 0 por conta do eixo Z do CoordinatorLayout
        mapView = new CustomMapView(parentContext);
        final CoordinatorLayout cl = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);
        cl.addView(mapView,0);

        // Encontra as views
        fabMyLocation = (FloatingActionButton) view.findViewById(R.id.fab_my_location);
        fabBusLocation = (FloatingActionButton) view.findViewById(R.id.fab_bus_location);
        btnClearMap = (Button) view.findViewById(R.id.btn_clear_map);


        // Atrela os listerners aos botões
        fabMyLocation.setOnClickListener(myLocationListener);
        fabBusLocation.setOnClickListener(busLocationListener);
        btnClearMap.setOnClickListener(clearMapListener);

        initializeMap();


          return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        locationManager = (LocationManager) parentContext.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(parentContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(parentContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0l, 0f, this);
        }
        // Adiciona a camada de localização do usuário
        // TODO: ERRO COM BITMAP!!!!! AAAAAAAAH
        enableMyLocationOverlay();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"onPause()");
        if (ActivityCompat.checkSelfPermission(parentContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(parentContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
        myLocationOverlay.disableMyLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        userLocation = null;
        myLocationOverlay = null;
        mapView = null;
        mapCamera = null;
    }

    // TODO: Implementar onSavedInstance
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void moveCameraToMyLocation(){
        try {
            double latitude = userLocation.getLatitude(),
                    longitude = userLocation.getLongitude();

            // Checa se o usuário se encontra dentro da area do mapa
            if(!mapBoundaries.contains(latitude,longitude)) Toast.makeText(parentContext,
                    R.string.msg_out_of_covered_region, Toast.LENGTH_SHORT).show();
            else{
                Log.i(TAG,"Current Location: (" + latitude + "," + longitude + ")" );
                mapCamera.animateTo(new GeoPoint(latitude,longitude));
            }
        // Se capturar a exception, o sistema não pôde recuperar as informações do GPS
        }catch (NullPointerException e){
            Log.e(TAG,"Could not get current location.",e);
            // Checa se o GPS está ligado
            if(!SystemServicesManager.isGPSEnabled(parentContext)) {
                Toast.makeText(parentContext, R.string.msg_turn_on_gps, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(parentContext, R.string.msg_loading_current_position, Toast.LENGTH_SHORT).show();
        }
    }


    public boolean isLayerEnabled(final OverlayTags layerTag){
        return mapView.containsOverlay(layerTag);
    }


    public void addLayerToMap(final List<Place> places, MarkerTypes markersType, OverlayTags overlayTag){
        // Cria e adiciona a camada de marcadores ao mapa
        final FolderOverlay poiMarkers = new FolderOverlay();

        MapUtils mapUtils = new MapUtils(getContext());
        final HashMap<MarkerStatuses, Drawable> markerDrawables = mapUtils.getMarkerDrawable(markersType);


        // Cria um marcador para cada local encontrado
        for (final Place place : places) {
            Marker.OnMarkerClickListener markerClick = new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    // Sets up a PlaceDetailsFragment to show specific information about the selected Place
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FrameLayout bottomSheetContainer = (FrameLayout) getActivity().findViewById(R.id.bottom_sheet_container);

                    BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheetContainer);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    PlaceDetailsFragment placeDetailsFragment = PlaceDetailsFragment.newInstance(place,userLocation);
                    fragmentManager.beginTransaction()
                            .replace(R.id.bottom_sheet_container,placeDetailsFragment,PlaceDetailsFragment.FRAGMENT_TAG)
                            .commit();

                    // Will change the marker to its clicked icon
                    marker.setIcon(markerDrawables.get(MarkerStatuses.CLICKED));
                    mapView.postInvalidate();
                    return true;
                }
            };

            final Marker customMarker = mapUtils.createCustomMarker(mapView,
                    markerDrawables.get(MarkerStatuses.NOT_CLICKED),
                    new GeoPoint(place.getLatitude(), place.getLongitude()),
                    markerClick);

            poiMarkers.add(customMarker);

        }

        mapView.addTileOverlay(poiMarkers,overlayTag);
        btnClearMap.setVisibility(View.VISIBLE);

    }


    public CustomMapView getMapView() {
        return mapView;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        if(userLocation == null)
            userLocation = new Place(location.getLatitude(),location.getLongitude(),"user_location");
        else {
            userLocation.setLongitude(location.getLongitude());
            userLocation.setLatitude(location.getLatitude());
        }
        final PlaceDetailsFragment mapFragment = (PlaceDetailsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PlaceDetailsFragment.FRAGMENT_TAG);
        if(mapFragment != null){
            mapFragment.updateUserLocation(userLocation);
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    public Place getUserLocation() {
        return userLocation;
    }
}
