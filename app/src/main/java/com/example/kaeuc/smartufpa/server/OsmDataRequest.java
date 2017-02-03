package com.example.kaeuc.smartufpa.server;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.example.kaeuc.smartufpa.utils.Constants;
import com.example.kaeuc.smartufpa.utils.JsonParser;
import com.example.kaeuc.smartufpa.models.Place;

import java.util.List;

/**
 * Created by kaeuc on 10/22/2016.
 * Classe responsável por executar busca de Pontos de Interesse Específicos baseado na buca do usuário
 * no servidor do OSM (XAPI - API direta de busca) de maneira assíncrona
 * Referência: http://wiki.openstreetmap.org/wiki/Xapi#Overpass_API
 * Recebe uma String contendo qual informação/filtro deseja ser buscado e retorna uma objeto XML
 * em formato String que deve ser analisado para retirar as localizações relevantes
 */

public class OsmDataRequest extends AsyncTask<String,Void,String> {

    private static final String TAG = "OsmDataRequest" ;
    // interface resposável por devolver o resultado da task pra atividade principal
    private OsmDataRequestResponse callBack = null;
    private ProgressBar progressBar;
    private String filtro;

    private Context parentContext;

    public OsmDataRequest(Context parentContext, ProgressBar progressBar) {
        this.parentContext = parentContext;
        this.callBack = (OsmDataRequestResponse) parentContext;
        this.progressBar = progressBar;
    }

    @Override
    protected String doInBackground(String... params) {
        filtro = params[0];
        String query = null;
        String jsonResponse = null;

        if(filtro.equalsIgnoreCase(Constants.RESTAURANT_FILTER))
            query = Constants.OVERPASS_RESTAURANT_QUERY;
        else if(filtro.equalsIgnoreCase(Constants.TOILETS_FILTER))
            query = Constants.OVERPASS_TOILETS_QUERY;
        else if(filtro.equalsIgnoreCase(Constants.XEROX_FILTER))
            query = Constants.OVERPASS_XEROX_QUERY;

        jsonResponse = HttpRequest.makeGetRequest(Constants.OVERPASS_SERVER_URL,query);


        return jsonResponse;
    }
    // Mostra a barra de progresso
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
    }

    // executa após a operação ser finalizada
    @Override
    protected void onPostExecute(String jsonResponse) {
        super.onPostExecute(jsonResponse);
        // Recebe o xml em forma de uma String e e analisa as informções relevantes
        final List<Place> locais = JsonParser.parseOsmResponse(jsonResponse);
        // Retorna os valores para a activity que chamou a ASyncTask
        callBack.osmTaskCompleted(locais,filtro);
        // esconde a barra de progresso
        progressBar.setVisibility(View.GONE);


    }

}