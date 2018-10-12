package br.ufpa.smartufpa.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast

import br.ufpa.smartufpa.R
import br.ufpa.smartufpa.activities.SelectCategoryActivity
import br.ufpa.smartufpa.adapters.SearchResultAdapter
import br.ufpa.smartufpa.models.smartufpa.APIResponse
import br.ufpa.smartufpa.models.smartufpa.Building
import br.ufpa.smartufpa.utils.retrofit.PlaceAPI
import br.ufpa.smartufpa.utils.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.fragment_add_building.*
import kotlinx.android.synthetic.main.fragment_add_building.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class AddBuildingFragment : NewPlaceFragment() {


//    // Keys for JSON Object
//    private val keyName = "name"
//    private val keyDescription = "description"
//    private val keyWebsite = "website"
//    private val keyOpeningTime = "opening_time"
//    private val keyClosingTime = "closing_time"
//    private val keyAdministrativeRole = "administrative_role"

    companion object {

        val TAG = AddBuildingFragment::class.java.name
        private lateinit var retrofitClient : Retrofit
        private lateinit var placeAPIClient : PlaceAPI

        @JvmStatic
        fun newInstance(context: Context?): AddBuildingFragment {
            retrofitClient = RetrofitClient.getInstance(context, null)
            placeAPIClient = retrofitClient.create(PlaceAPI::class.java)
            return AddBuildingFragment()
        }
    }

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
//    private var administrativeRole: Building.AdministrativeRole = Building.AdministrativeRole.NONE
    private var administrativeRole: String = ""


    private var listener: NewPlaceFragment.onNewPlaceListener? = null




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return inflater.inflate(R.layout.fragment_add_building, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ArrayAdapter.createFromResource(view.context,
                R.array.spinner_administrative_role, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spin_bld_administrative_role!!.adapter = adapter
        spin_bld_administrative_role!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                administrativeRole = adapter.getItem(position).toString()
                Toast.makeText(context,administrativeRole,Toast.LENGTH_LONG).show()
            }

        }

        val onClickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.btn_bld_confirm -> sendToServer()
                R.id.btn_bld_cancel -> activity!!.onBackPressed()
            }
        }

        btn_bld_confirm!!.setOnClickListener(onClickListener)
        btn_bld_cancel!!.setOnClickListener(onClickListener)

        input_bld_openingtime!!.setOnClickListener { showTimePickerDialog(input_bld_openingtime) }

        input_bld_closingtime!!.setOnClickListener { showTimePickerDialog(input_bld_closingtime) }


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bundle = arguments
        latitude = bundle?.getDouble(SelectCategoryActivity.KEY_LATITUDE) ?: 0.0
        longitude = bundle?.getDouble(SelectCategoryActivity.KEY_LONGITUDE) ?: 0.0
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is NewPlaceFragment.onNewPlaceListener) {
            listener = context
        } else {
            throw ClassCastException()
        }
    }

    override fun sendToServer(){
        val call: Call<APIResponse> = placeAPIClient.addBuilding(initBuilding())
        call.enqueue(object : Callback<APIResponse> {
            override fun onResponse(call: Call<APIResponse>, response: Response<APIResponse>) {
                val status = response.isSuccessful
                if(status){
                    Toast.makeText(context,response.message(),Toast.LENGTH_LONG).show()
                    activity?.onBackPressed()
                }
            }

            override fun onFailure(call: Call<APIResponse>, t: Throwable) {
                t.printStackTrace()
            }
        });
    }

    private fun initBuilding():Building{
        val name = input_bld_name.text.toString()
        val openingTime = input_bld_openingtime.text.toString()
        val closingTime = input_bld_closingtime.text.toString()
        val website = input_bld_website.text.toString()
        val description = input_bld_description.text.toString()
        return Building(latitude = latitude,longitude = longitude,name= name,
                openingTime = openingTime, closingTime = closingTime, website = website,
                description = description)
    }







}