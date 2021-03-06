package com.test.firebaseauthenanonymous

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.test.firebaseauthenanonymous.adapter.MenuRecyclerAdapter
import com.test.firebaseauthenanonymous.databinding.ActivityMainBinding
import com.test.firebaseauthenanonymous.extension.fromJson
import com.test.firebaseauthenanonymous.model.MenuCountModel
import com.test.firebaseauthenanonymous.model.MenuModel

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseFirestore.getInstance()
    private val gson = Gson()
    private lateinit var recyclerAdapter: MenuRecyclerAdapter
    private var menus: ArrayList<MenuModel>? = null
    private lateinit var progressDialog: ProgressDialog

    companion object {

        private const val COLLECTION_ID = "id"
        private const val MENU_COUNT_LIST_KEY = "menuCountList"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initInstance()
        initProgressDialog()
        initRecyclerMenu()
        setUpFirebaseAuth()
        getMenusFromFirestore()
    }

    private fun initInstance() {
        binding.btnFirst.setOnClickListener(this)
        binding.btnSecond.setOnClickListener(this)
        binding.btnThird.setOnClickListener(this)
        binding.btnForth.setOnClickListener(this)
    }

    private fun initProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
        }
    }

    private fun initRecyclerMenu() {
        recyclerAdapter = MenuRecyclerAdapter()
        binding.recyclerMenu.apply {
            adapter = recyclerAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 4)
        }
        recyclerAdapter.listener = {
            getDataFromDocumentId(binding.edtId.text.toString(), it)
        }
    }

    private fun setUpFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.currentUser?.let {
            Log.d("Prew", "alreadySignIn")
        } ?: run {
            firebaseAuth.signInAnonymously()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("Prew", "signInCompleteSuccess")
                        } else {
                            Log.d("Prew", "signInCompleteFail")
                        }
                    }
                    .addOnCanceledListener {
                        Log.d("Prew", "signInCancel")
                    }
                    .addOnFailureListener {
                        Log.d("Prew", "signInFail")
                    }
        }
    }

    private fun getMenusFromFirestore() {
        progressDialog.show()
        database.collection("menu")
                .document("menus")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val rootData = it.result?.data
                        if (rootData != null) {
                            menus = gson.fromJson(gson.toJson(rootData["data"]))
                            progressDialog.dismiss()
                        }
                    } else {
                        Log.d("Prew", "Get menus fail.")
                    }
                }
                .addOnFailureListener {
                    Log.d("Prew", "GetDataFromDocumentIdFailureException: ${it.message}")
                }
    }

    private fun getMenusFromFirestore(menuCountList: ArrayList<MenuCountModel>, menus: ArrayList<MenuModel>? = this@MainActivity.menus) {
        val newMenuShow: ArrayList<MenuModel> = arrayListOf()
        menuCountList.sortByDescending { it.count }
        menuCountList.forEach { menuCount ->
            menus?.forEach {menu ->
                if (menuCount.menu_id == menu.key) {
                    newMenuShow.add(menu)
                }
            }
        }
        recyclerAdapter.menuList = newMenuShow
        recyclerAdapter.notifyDataSetChanged()
        progressDialog.dismiss()
    }

    private fun getDataFromDocumentId(id: String) {
        database.collection(COLLECTION_ID)
                .document(id)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result?.data != null) {
                            val rootData = it.result?.data
                            if (rootData != null) {
                                val menuCountList: ArrayList<MenuCountModel>? = gson.fromJson(gson.toJson(rootData[MENU_COUNT_LIST_KEY]))
                                getMenusFromFirestore(menuCountList ?: arrayListOf())
                            }
                        }
                    } else {
                        Log.d("Prew", "getFail")
                    }
                }
                .addOnFailureListener {
                    Log.d("Prew", "GetDataFromDocumentIdFailureException: ${it.message}")
                }
    }

    private fun getDataFromDocumentId(id: String, menuId: String) {
        progressDialog.show()
        database.collection(COLLECTION_ID)
                .document(id)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result?.data != null) {
                            val rootData = it.result?.data
                            if (rootData != null) {
                                val menuCountList: ArrayList<MenuCountModel>? = gson.fromJson(gson.toJson(rootData[MENU_COUNT_LIST_KEY]))
                                val menuCount = menuCountList?.find { menuCountModel ->
                                    menuCountModel.menu_id == menuId
                                }
                                menuCount?.let {
                                    increaseMenuCount(id, rootData, menuCountList, menuCount)
                                } ?: run {
                                    addMenuCount(id, menuId, rootData, menuCountList)
                                }
                            } else {
                                createDocumentWithId(id, menuId)
                            }
                        } else {
                            createDocumentWithId(id, menuId)
                        }
                    } else {
                        Log.d("Prew", "getFail")
                    }
                }
                .addOnFailureListener {
                    Log.d("Prew", "GetDataFromDocumentIdFailureException: ${it.message}")
                }
    }

    private fun increaseMenuCount(id: String, rootData: MutableMap<String, Any>, menuCountList: ArrayList<MenuCountModel>, menuCount: MenuCountModel) {
        menuCount.count += 1
        rootData[MENU_COUNT_LIST_KEY] = menuCountList
        database.collection(COLLECTION_ID)
                .document(id)
                .set(rootData)
                .addOnSuccessListener {
                    Log.d("Prew", "IncreaseMenuCountSuccess")
                    getMenusFromFirestore(menuCountList)
                }
                .addOnFailureListener {
                    Log.d("Prew", "IncreaseMenuFailSuccess")
                }
    }

    private fun addMenuCount(id: String, menuId: String, rootData: MutableMap<String, Any>, menuCountList: ArrayList<MenuCountModel>?) {
        val list = menuCountList ?: arrayListOf()
        list.add(MenuCountModel(1, menuId))
        rootData[MENU_COUNT_LIST_KEY] = list
        database.collection(COLLECTION_ID)
                .document(id)
                .set(rootData)
                .addOnCompleteListener {
                    Log.d("Prew", "AddMenuCountSuccess")
                    getMenusFromFirestore(list)
                }
                .addOnFailureListener {
                    Log.d("Prew", "AddMenuCountFailureException: ${it.message}")
                }
    }

    private fun createDocumentWithId(id: String, menuId: String) {
        val data: HashMap<String, ArrayList<MenuCountModel>> = hashMapOf()
        data.apply {
            put(MENU_COUNT_LIST_KEY, arrayListOf())
        }
        database.collection(COLLECTION_ID)
                .document(id)
                .set(data)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("Prew", "Add document success")
                        getDataFromDocumentId(id, menuId)
                    } else {
                        Log.d("Prew", "Add document fail")
                    }
                }
                .addOnFailureListener {
                    Log.d("Prew", "CreateDocumentFailureException: ${it.message}")
                }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnFirst -> {
                getDataFromDocumentId(binding.edtId.text.toString(), "first")
            }
            R.id.btnSecond -> {
                getDataFromDocumentId(binding.edtId.text.toString(), "second")
            }
            R.id.btnThird -> {
                getDataFromDocumentId(binding.edtId.text.toString(), "third")
            }
            R.id.btnForth -> {
                getDataFromDocumentId(binding.edtId.text.toString(), "forth")
            }
        }
    }
}