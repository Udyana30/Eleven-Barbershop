package com.example.elevenbarbershop.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileAct : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var skipButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var imageUrl: Uri? = null
    private val maxFileSizeInMB = 3
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profile)
        uploadButton = findViewById(R.id.btn_upload)
        skipButton = findViewById(R.id.btn_skip)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
        storage = FirebaseStorage.getInstance()

        // Register activity result launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data!!.data != null) {
                imageUrl = result.data!!.data
                profileImageView.setImageURI(imageUrl)

                // Validate file size
                val fileSizeInMB = getFileSizeInMB(imageUrl!!)
                if (fileSizeInMB > maxFileSizeInMB) {
                    Toast.makeText(this, "File tidak boleh melebihi 3 MB", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                uploadFile()
            }
        }

        uploadButton.setOnClickListener {
            openFileChooser()
        }

        skipButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uploadFile() {
        if (imageUrl != null) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Menyimpan file di folder userprofile dengan nama userId.jpg
                val fileReference: StorageReference = storage.reference.child("userprofile/${userId}.jpg")

                fileReference.putFile(imageUrl!!)
                    .addOnSuccessListener {
                        fileReference.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            saveProfileImageUrl(imageUrl)
                            skipButton.text = getString(R.string.save)
                            updateProfileImageView(imageUrl)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Upload gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Tidak ada file yang dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child(userId).child("pp").setValue(imageUrl)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile image URL saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save profile image URL", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private var isDestroyed = false // tambahkan variabel untuk menandai apakah aktivitas sudah dihancurkan

    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true // tandai bahwa aktivitas telah dihancurkan
    }

    private fun updateProfileImageView(imageUrl: String) {
        if (!isDestroyed && !isFinishing) { // periksa apakah aktivitas masih valid
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_2) // Placeholder image while loading
                .error(R.drawable.profile_2) // Error image if failed to load
                .into(profileImageView)
        }
    }

    private fun saveUserProfile() {
        Toast.makeText(this, "Profile data saved", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainAct::class.java)
        startActivity(intent)
        finish()
    }

    private fun getFileSizeInMB(uri: Uri): Double {
        val cursor = contentResolver.query(uri, null, null, null, null)
        val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
        cursor?.moveToFirst()
        val size = sizeIndex?.let { cursor.getLong(it) } ?: 0
        cursor?.close()
        return size.toDouble() / (1024 * 1024)
    }
}