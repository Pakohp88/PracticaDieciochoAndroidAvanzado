package dgtic.unam.oauth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import dgtic.unam.oauth.databinding.ActivityOpcionesBinding

enum class TipoProvedor {
    CORREO,
    GOGGLE,
    FACEBOOK
}

class OpcionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpcionesBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignOptions: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var bundle: Bundle? = intent.extras

        var email: String? = bundle?.getString("email")
        var provedor: String? = bundle?.getString("provedor")

        inicio(email ?: "", provedor ?: "")

        var preferences =
            getSharedPreferences(getString(R.string.file_preferencia), Context.MODE_PRIVATE).edit()
        preferences.putString("email", email)
        preferences.putString("provedor", provedor)
        preferences.apply()
    }


    private fun inicio(email: String, provedor: String) {
        binding.mail.text = email
        binding.provedor.text = provedor

        binding.closeSesion.setOnClickListener {
            var preferences = getSharedPreferences(
                getString(R.string.file_preferencia),
                Context.MODE_PRIVATE
            ).edit()
            preferences.clear()
            preferences.apply()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
        }

        if(provedor== TipoProvedor.GOGGLE.name){
            googleSignOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
            googleSignInClient=GoogleSignIn.getClient(this, googleSignOptions)

            val data=GoogleSignIn.getLastSignedInAccount(this)
            if(data != null){
                Picasso.get().load(data.photoUrl).into(binding.img)

            }
        }
    }
}