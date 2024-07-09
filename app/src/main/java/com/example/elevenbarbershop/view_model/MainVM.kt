package com.example.elevenbarbershop.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elevenbarbershop.model.BarberModel
import com.example.elevenbarbershop.model.GalleryModel
import com.example.elevenbarbershop.model.LocationModel
import com.example.elevenbarbershop.model.ServicesModel
import com.example.elevenbarbershop.model.SliderModel
import com.example.elevenbarbershop.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainVM : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> get() = _userData

    private val _bannerList = MutableLiveData<List<SliderModel>>()
    val bannerList: LiveData<List<SliderModel>> get() = _bannerList

    private val _servicesList = MutableLiveData<List<ServicesModel>>()
    val servicesList: LiveData<List<ServicesModel>> get() = _servicesList

    private val _galleryList = MutableLiveData<List<GalleryModel>>()
    val galleryList: LiveData<List<GalleryModel>> get() = _galleryList

    private val _barberList = MutableLiveData<List<BarberModel>>()
    val barberList: LiveData<List<BarberModel>> get() = _barberList

    private val _locationList = MutableLiveData<List<LocationModel>>()
    val locationList: LiveData<List<LocationModel>> get() = _locationList

    init {
        currentUser?.uid?.let { fetchUserData(it) }
        fetchBarber()
        fetchLocations()
        fetchBanners()
        fetchServices()
        fetchGallery()
    }

    private fun fetchUserData(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("Users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val address = snapshot.child("address").getValue(String::class.java) ?: ""
                val birth = snapshot.child("birth").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                val gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                val pp = snapshot.child("pp").getValue(String::class.java) ?: ""

                // Buat UserModel baru dengan data yang diambil
                val user = UserModel(email, username, name, birth, address, gender, phone, pp, userId)

                // Simpan data pengguna ke dalam LiveData
                _userData.value = user
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
            }
        })
    }

    private fun fetchBanners() {
        val database = FirebaseDatabase.getInstance()
        val bannerRef = database.getReference("Banner")

        bannerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val banners = mutableListOf<SliderModel>()
                for (bannerSnapshot in snapshot.children) {
                    val imageUrl = bannerSnapshot.child("url").getValue(String::class.java)
                    if (imageUrl != null) {
                        banners.add(SliderModel(imageUrl))
                    }
                }
                _bannerList.value = banners
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
            }
        })
    }

    private fun fetchServices() {
        val database = FirebaseDatabase.getInstance()
        val servicesRef = database.getReference("Services")

        servicesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val services = mutableListOf<ServicesModel>()
                for (serviceSnapshot in snapshot.children) {
                    val title = serviceSnapshot.child("title").getValue(String::class.java) ?: ""
                    val id = serviceSnapshot.child("id").getValue(Int::class.java) ?: 0
                    val picUrl = serviceSnapshot.child("picUrl").getValue(String::class.java) ?: ""
                    services.add(ServicesModel(title, id, picUrl))
                }
                _servicesList.value = services
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
            }
        })
    }

    fun fetchGallery() {
        val database = FirebaseDatabase.getInstance()
        val galleryRef = database.getReference("Gallery")

        galleryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val galleries = mutableListOf<GalleryModel>()
                for (gallerySnapshot in snapshot.children) {
                    val name = gallerySnapshot.child("name").getValue(String::class.java) ?: ""
                    val type = gallerySnapshot.child("type").getValue(String::class.java) ?: ""
                    val des = gallerySnapshot.child("des").getValue(String::class.java) ?: ""
                    val face = gallerySnapshot.child("face").getValue(String::class.java) ?: ""
                    val gUrl = gallerySnapshot.child("gUrl").getValue(String::class.java) ?: ""
                    galleries.add(GalleryModel(name, type, des, face, gUrl))
                }
                Log.d("MainVM", "Gallery Items fetched: $galleries")
                _galleryList.value = galleries
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
            }
        })
    }

    fun fetchBarber() {
        val database = FirebaseDatabase.getInstance()
        val barberRef = database.getReference("Barber")

        barberRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val barbers = mutableListOf<BarberModel>()
                for (barberSnapshot in snapshot.children) {
                    val name = barberSnapshot.child("name").getValue(String::class.java) ?: ""
                    val id = barberSnapshot.child("id").getValue(Int::class.java) ?: 0
                    val bPict = barberSnapshot.child("bPict").getValue(String::class.java) ?: ""
                    val loc = barberSnapshot.child("loc").getValue(String::class.java) ?: ""
                    val des = barberSnapshot.child("des").getValue(String::class.java) ?: ""
                    val skill = barberSnapshot.child("skill").getValue(String::class.java) ?: ""
                    val lg = barberSnapshot.child("lg").getValue(String::class.java) ?: ""
                    barbers.add(BarberModel(name, id, bPict, loc, des, skill, lg))
                }
                _barberList.value = barbers
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
            }
        })
    }

    private fun fetchLocations() {
        val database = FirebaseDatabase.getInstance()
        val locationRef = database.getReference("Locations")

        locationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = mutableListOf<LocationModel>()
                for (locationSnapshot in snapshot.children) {
                    val title = locationSnapshot.child("title").getValue(String::class.java) ?: ""
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                    locations.add(LocationModel(title, latitude, longitude))
                }
                _locationList.value = locations
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
            }
        })
    }

    fun updateUserData(user: UserModel) {
        val userId = currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("Users").child(userId)

        userRef.setValue(user)
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}